package com.mjs8071.tenmo.dao;

import com.mjs8071.tenmo.exception.TransferNotFoundException;
import com.mjs8071.tenmo.model.Account;
import com.mjs8071.tenmo.model.Send;
import com.mjs8071.tenmo.model.Transfer;
import com.mjs8071.tenmo.model.User;

import javax.security.auth.login.AccountException;
import java.util.List;

public interface TransferDao {

    Transfer send(Send send, String username) throws AccountException, TransferNotFoundException;

    List<Transfer> listTransfers(String username);

    Transfer approveOrDecline(Transfer transfer, String username) throws TransferNotFoundException, AccountException;

    List<Transfer> listPendingTransfers(String username);

    Transfer getTransfer(Long transferId, String username) throws TransferNotFoundException;

    public Account getAccountDetails(String username) throws AccountException;

    public List<User> findAllUsers(String username);

    public Transfer request(Send send, String username) throws TransferNotFoundException;



}
