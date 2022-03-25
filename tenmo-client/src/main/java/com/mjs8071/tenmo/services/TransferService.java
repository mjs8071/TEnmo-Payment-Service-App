package com.mjs8071.tenmo.services;

import com.mjs8071.tenmo.model.Account;
import com.mjs8071.tenmo.model.Send;
import com.mjs8071.tenmo.model.Transfer;
import com.mjs8071.tenmo.model.User;
import com.mjs8071.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class TransferService {
    private static final String API_BASE_URL = "http://localhost:8080/";
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Transfer[] listTransfers() {
        Transfer[] transfers = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(API_BASE_URL + "transfer", HttpMethod.GET, makeAuthEntity(), Transfer[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e){
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public Transfer[] listPendingTransfers() {
        Transfer[] transfers = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(API_BASE_URL + "transfer/pending", HttpMethod.GET, makeAuthEntity(), Transfer[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e){
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }



    public Transfer getTransfer(int transferId) {
        Transfer transfer = null;
        try {
            ResponseEntity<Transfer> response =
                    restTemplate.exchange(API_BASE_URL + "transfer/" + transferId, HttpMethod.GET, makeAuthEntity(), Transfer.class);
            transfer = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfer;
    }


    public BigDecimal getBalance() {
        Account account = new Account();
        try {
            ResponseEntity<Account> response =
                    restTemplate.exchange(API_BASE_URL + "account", HttpMethod.GET, makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account.getBalance();
    }

    public Transfer sendBucks(Send send) {
        HttpEntity<Send> entity = makeSendEntity(send);
        Transfer transfer = new Transfer();
        try {
                    transfer = restTemplate.postForObject(API_BASE_URL + "transfer/send", entity, Transfer.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfer;
    }

    public Transfer requestBucks(Send send) {
        HttpEntity<Send> entity = makeSendEntity(send);
        Transfer transfer = new Transfer();
        try {
            transfer = restTemplate.postForObject(API_BASE_URL + "transfer/request", entity, Transfer.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfer;
    }

    public User[] getAllUsers() {
        User[] users = null;
        try {
            ResponseEntity<User[]> response = restTemplate.exchange(API_BASE_URL + "users", HttpMethod.GET, makeAuthEntity(), User[].class);
            users = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return users;
    }


    public boolean updateTransfer(Transfer updatedTransfer) {
        boolean success = false;
        try {
            restTemplate.put(API_BASE_URL + "transfer/request",
                    makeTransferEntity(updatedTransfer));
            success = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success;
    }

    public Transfer approveOrDenyTransfer(int choice, Transfer transfer) {
        if (choice == 0) {
            return null;
        } else if (choice == 1) {
            transfer.setTransferStatusDesc("Approved");
            transfer.setTransferStatusId(2L);
            return transfer;
        } else if (choice == 2) {
            transfer.setTransferStatusDesc("Rejected");
            transfer.setTransferStatusId(3L);
            return transfer;
        }
        return null;
    }


    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Send> makeSendEntity(Send send) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(send, headers);
    }

    private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(transfer, headers);
    }


}
