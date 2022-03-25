package com.mjs8071.tenmo.controller;

import com.mjs8071.tenmo.dao.TransferDao;
import com.mjs8071.tenmo.exception.TransferNotFoundException;
import com.mjs8071.tenmo.model.Account;
import com.mjs8071.tenmo.model.Send;
import com.mjs8071.tenmo.model.Transfer;
import com.mjs8071.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountException;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class TransferController {
    private final TransferDao transferDao;


    public TransferController(TransferDao transferDao) {
        this.transferDao = transferDao;
    }

    @GetMapping("/transfer")
    public List<Transfer> listTransfers(Principal principal) {
        return transferDao.listTransfers(principal.getName());
    }

    @GetMapping("/transfer/pending")
    public List<Transfer> listPendingTransfers(Principal principal) {
        return transferDao.listPendingTransfers(principal.getName());

    }



    @GetMapping("/transfer/{transferId}")
    public Transfer getTransfer(@PathVariable Long transferId, Principal principal) throws TransferNotFoundException {
        return transferDao.getTransfer(transferId, principal.getName());
    }

    @GetMapping("/account")
    public Account getAccountDetails(Principal principal) throws AccountException {
        Account account = transferDao.getAccountDetails(principal.getName());
        return account;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/transfer/send")
    @Transactional
    public Transfer send(@RequestBody Send send, Principal principal) throws AccountException, TransferNotFoundException {
        return transferDao.send(send, principal.getName());
    }


    @GetMapping("/users")
    public List<User> findAllUsers(Principal principal) {
        return transferDao.findAllUsers(principal.getName());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/transfer/request")
    @Transactional
    public Transfer request(@RequestBody Send send, Principal principal) throws TransferNotFoundException {
        return transferDao.request(send, principal.getName());
    }

    @PutMapping("/transfer/request")
    public Transfer approveOrDecline(@RequestBody Transfer transfer, Principal principal) throws TransferNotFoundException, AccountException {
        return transferDao.approveOrDecline(transfer, principal.getName());
    }








}
