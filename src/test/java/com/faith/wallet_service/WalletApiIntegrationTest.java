package com.faith.wallet_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class WalletApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    void setMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String extractId(MvcResult createResult) throws Exception {
        JsonNode root = objectMapper.readTree(createResult.getResponse().getContentAsString());
        return root.path("data").path("id").asText();
    }

    @Test
    @DisplayName("POST /wallets creates wallet")
    void createWallet() throws Exception {
        String requestBody = "{\"initialBalance\":100,\"description\":\"Test wallet\"}";
        MvcResult result = mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Wallet created successfully"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.balance").value(100))
                .andExpect(jsonPath("$.data.description").value("Test wallet"))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andReturn();
        String id = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText();
        assertThat(id).isNotBlank();
    }

    @Test
    @DisplayName("GET /wallets/:id returns wallet")
    void getWallet() throws Exception {
        String requestBody = "{\"initialBalance\":50}";
        MvcResult create = mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();
        String walletId = extractId(create);

        mockMvc.perform(get("/wallets/" + walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Wallet retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(Long.parseLong(walletId)))
                .andExpect(jsonPath("$.data.balance").value(50));
    }

    @Test
    @DisplayName("POST /transactions credits wallet; duplicate idempotency key returns cached response")
    void creditAndIdempotency() throws Exception {
        String walletRequest = "{\"initialBalance\":100}";
        MvcResult create = mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(walletRequest))
                .andExpect(status().isCreated())
                .andReturn();
        String walletId = extractId(create);

        String body = "{\"walletId\":" + walletId + ",\"amount\":200,\"type\":\"CREDIT\",\"idempotencyKey\":\"1f6e3a2b-9c7d-4f1a-8e5b-3a2d4f9c0e12\"}";

        // First request
        MvcResult firstResponse = mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Transaction completed successfully"))
                .andExpect(jsonPath("$.data.transactionId").isNotEmpty())
                .andExpect(jsonPath("$.data.walletId").value(Long.parseLong(walletId)))
                .andExpect(jsonPath("$.data.balance").value(300))
                .andExpect(jsonPath("$.data.timestamp").isNotEmpty())
                .andReturn();

        String firstResponseBody = firstResponse.getResponse().getContentAsString();

        mockMvc.perform(get("/wallets/" + walletId))
                .andExpect(jsonPath("$.data.balance").value(300));

        // Second request with same idempotency key should return cached response
        MvcResult secondResponse = mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();

        String secondResponseBody = secondResponse.getResponse().getContentAsString();

        // Responses should be identical (same transaction ID, timestamp, etc.)
        assertThat(firstResponseBody).isEqualTo(secondResponseBody);

        // Balance should still be 300 (no double application)
        mockMvc.perform(get("/wallets/" + walletId))
                .andExpect(jsonPath("$.data.balance").value(300));
    }

    @Test
    @DisplayName("POST /transactions rejects debit when insufficient balance")
    void debitRejectedWhenInsufficientBalance() throws Exception {
        String walletRequest = "{\"initialBalance\":10}";
        MvcResult create = mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(walletRequest))
                .andExpect(status().isCreated())
                .andReturn();
        String walletId = extractId(create);

        String body = "{\"walletId\":" + walletId + ",\"amount\":20,\"type\":\"DEBIT\",\"idempotencyKey\":\"7b2c4f8a-6d3e-4b5f-9a7e-1d2c3b4f5a6e\"}";
        mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Insufficient balance"));

        mockMvc.perform(get("/wallets/" + walletId))
                .andExpect(jsonPath("$.data.balance").value(10));
    }

    @Test
    @DisplayName("POST /transactions/transfer debits sender and credits receiver")
    void transfer() throws Exception {
        String walletARequest = "{\"initialBalance\":100}";
        String walletBRequest = "{\"initialBalance\":0}";
        MvcResult a = mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(walletARequest))
                .andExpect(status().isCreated()).andReturn();
        MvcResult b = mockMvc.perform(post("/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(walletBRequest))
                .andExpect(status().isCreated()).andReturn();
        String walletAId = extractId(a);
        String walletBId = extractId(b);

        String transferBody = "{\"fromWalletId\":" + walletAId + ",\"toWalletId\":" + walletBId + ",\"amount\":40,\"idempotencyKey\":\"3e9f1b4c-8d2a-4c7f-9b5d-2f1a6c3e7b8d\"}";
        mockMvc.perform(post("/transactions/transfer").contentType(MediaType.APPLICATION_JSON).content(transferBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Transfer completed successfully"))
                .andExpect(jsonPath("$.data.transferId").isNotEmpty())
                .andExpect(jsonPath("$.data.fromWalletId").value(Long.parseLong(walletAId)))
                .andExpect(jsonPath("$.data.fromBalance").value(60))
                .andExpect(jsonPath("$.data.toWalletId").value(Long.parseLong(walletBId)))
                .andExpect(jsonPath("$.data.toBalance").value(40))
                .andExpect(jsonPath("$.data.timestamp").isNotEmpty());

        mockMvc.perform(get("/wallets/" + walletAId)).andExpect(jsonPath("$.data.balance").value(60));
        mockMvc.perform(get("/wallets/" + walletBId)).andExpect(jsonPath("$.data.balance").value(40));

        // Test idempotency for transfers
        mockMvc.perform(post("/transactions/transfer").contentType(MediaType.APPLICATION_JSON).content(transferBody))
                .andExpect(status().isOk()); // Should return cached response, no error

        // Balances should remain the same (no double transfer)
        mockMvc.perform(get("/wallets/" + walletAId)).andExpect(jsonPath("$.data.balance").value(60));
        mockMvc.perform(get("/wallets/" + walletBId)).andExpect(jsonPath("$.data.balance").value(40));
    }
}
