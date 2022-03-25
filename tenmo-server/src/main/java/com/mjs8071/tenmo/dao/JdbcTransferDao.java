package com.mjs8071.tenmo.dao;


import com.mjs8071.tenmo.exception.TransferNotFoundException;
import com.mjs8071.tenmo.model.Account;
import com.mjs8071.tenmo.model.Send;
import com.mjs8071.tenmo.model.Transfer;
import com.mjs8071.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {
    //deals with transfer table and account table
    //sql queries
@Autowired
private JdbcTemplate jdbcTemplate;

    @Override
    public Transfer send(Send send, String username) throws AccountException, TransferNotFoundException {
        if (username.equals(send.getUsernameTo())) {
            throw new AccountException("Account Error.");
        }
        String sqlFrom = "UPDATE account " +
                "SET balance = balance - ? " +
                "WHERE balance >= ? AND user_id = (SELECT user_id FROM tenmo_user WHERE username = ?);";
        String sqlTo = "UPDATE account " +
                "SET balance = balance + ? " +
                "WHERE user_id = (SELECT user_id FROM tenmo_user WHERE username = ?);";
        try {
            //returns 1 if success, 0 if no row updated (int)
            int output1 = jdbcTemplate.update(sqlFrom, send.getAmount(), send.getAmount(), username);
            Integer output2 = null;
            if (output1 == 1) {
                output2 = jdbcTemplate.update(sqlTo, send.getAmount(), send.getUsernameTo());
            }
            if (output2 != 1 || output2 == null) {
                throw new AccountException("Account Error.");
            }
        } catch (Exception e) {
            throw new AccountException("Account ID not found or balance is insufficient!");
        }
        Long transferId = postToTransfer(2, 2, username, send.getUsernameTo(), send.getAmount());
        return getTransfer(transferId, username);
    }

    @Override
    public Transfer request(Send send, String username) throws TransferNotFoundException {
        Long transferId = postToTransfer(1, 1, send.getUsernameTo(), username, send.getAmount());
        return getTransfer(transferId, username);
    }



    public Long postToTransfer(int transferTypeId, int transferStatusId, String usernameFrom, String usernameTo, BigDecimal amount) {
        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, (SELECT account_id FROM account WHERE user_id = (SELECT user_id FROM tenmo_user WHERE username = ?))," +
                " (SELECT account_id FROM account WHERE user_id = (SELECT user_id FROM tenmo_user WHERE username = ?)), ?) " +
                "RETURNING transfer_id;";
        return jdbcTemplate.queryForObject(sql, Long.class, transferTypeId, transferStatusId, usernameFrom, usernameTo, amount);
    }




    @Override
    public List<Transfer> listTransfers(String username) {
        List<Transfer> transfers = new ArrayList<>();

         String sql = "SELECT transfer.*, tenmo_user_to.username AS username_to, tenmo_user_from.username AS username_from, transfer_type_desc, transfer_status_desc " +
                "FROM transfer " +
                 "JOIN transfer_status USING (transfer_status_id) " +
                 "JOIN transfer_type USING (transfer_type_id) " +
                "JOIN account AS account_to ON transfer.account_to = account_to.account_id " +
                "JOIN tenmo_user AS tenmo_user_to ON account_to.user_id = tenmo_user_to.user_id " +
                 "JOIN account AS account_from ON transfer.account_from = account_from.account_id " +
                 "JOIN tenmo_user AS tenmo_user_from ON account_from.user_id = tenmo_user_from.user_id " +
                "WHERE tenmo_user_to.username = ? OR tenmo_user_from.username = ?;";
         SqlRowSet results = jdbcTemplate.queryForRowSet(sql, username, username);
        while(results.next()) {
            Transfer transfer = mapRowToTransfer(results);
            transfers.add(transfer);
        }
        return transfers;
    }

    @Override
    public Transfer approveOrDecline(Transfer transfer, String username) throws TransferNotFoundException, AccountException {
        if (username.equals(transfer.getUsernameTo())) {
            throw new TransferNotFoundException();
        }

        if (!transfer.getTransferId().equals(getTransfer(transfer.getTransferId(), username).getTransferId())) {
            throw new TransferNotFoundException();
        }
        if(transfer.getTransferStatusId().equals(2L)) {
            String sqlFrom = "UPDATE account " +
                    "SET balance = balance - ? " +
                    "WHERE balance >= ? AND user_id = (SELECT user_id FROM tenmo_user WHERE username = ?);";
            String sqlTo = "UPDATE account " +
                    "SET balance = balance + ? " +
                    "WHERE user_id = (SELECT user_id FROM tenmo_user WHERE username = ?);";
            try {
                //returns 1 if success, 0 if no row updated (int)
                int output1 = jdbcTemplate.update(sqlFrom, transfer.getAmount(), transfer.getAmount(), username);
                Integer output2 = null;
                if (output1 == 1) {
                    output2 = jdbcTemplate.update(sqlTo, transfer.getAmount(), transfer.getUsernameTo());
                }
                if (output2 != 1 || output2 == null) {
                    throw new AccountException("Account Error.");
                }
            } catch (Exception e) {
                throw new AccountException("Account ID not found or balance is insufficient!");
            }
        }
            String sql = "UPDATE transfer " +
                    "SET transfer_status_id = ? " +
                    "WHERE transfer_id = ?;";
            try {
                jdbcTemplate.update(sql, transfer.getTransferStatusId(), transfer.getTransferId());
            } catch (Exception e) {
                throw new TransferNotFoundException();
            }
            return getTransfer(transfer.getTransferId(), username);
    }

    @Override
    public List<Transfer> listPendingTransfers(String username) {
        List<Transfer> transfers = new ArrayList<>();

        String sql = "SELECT transfer.*, tenmo_user_to.username AS username_to, tenmo_user_from.username AS username_from, transfer_type_desc, transfer_status_desc " +
                "FROM transfer " +
                "JOIN transfer_status USING (transfer_status_id) " +
                "JOIN transfer_type USING (transfer_type_id) " +
                "JOIN account AS account_to ON transfer.account_to = account_to.account_id " +
                "JOIN tenmo_user AS tenmo_user_to ON account_to.user_id = tenmo_user_to.user_id " +
                "JOIN account AS account_from ON transfer.account_from = account_from.account_id " +
                "JOIN tenmo_user AS tenmo_user_from ON account_from.user_id = tenmo_user_from.user_id " +
                "WHERE transfer.transfer_status_id = 1 AND (tenmo_user_to.username = ? OR tenmo_user_from.username = ?);";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, username, username);
        while(results.next()) {
            Transfer transfer = mapRowToTransfer(results);
            transfers.add(transfer);
        }
        return transfers;
    }

    @Override
    public Transfer getTransfer(Long transferId, String username) throws TransferNotFoundException {
        String sql = "SELECT transfer.*, tenmo_user_to.username AS username_to, tenmo_user_from.username AS username_from, transfer_type_desc, transfer_status_desc " +
                "FROM transfer " +
                "JOIN transfer_status USING (transfer_status_id) " +
                "JOIN transfer_type USING (transfer_type_id) " +
                "JOIN account AS account_to ON transfer.account_to = account_to.account_id " +
                "JOIN tenmo_user AS tenmo_user_to ON account_to.user_id = tenmo_user_to.user_id " +
                "JOIN account AS account_from ON transfer.account_from = account_from.account_id " +
                "JOIN tenmo_user AS tenmo_user_from ON account_from.user_id = tenmo_user_from.user_id " +
                "WHERE transfer_id = ? AND (tenmo_user_to.username = ? OR tenmo_user_from.username = ?)";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId, username, username);
        if(results.next()) {
            return mapRowToTransfer(results);
        }
        throw new TransferNotFoundException();
    }

    @Override
    public Account getAccountDetails(String username) throws AccountException {
        Account account = new Account();
        String sql = "SELECT account.*, username " +
            "FROM account " +
                "JOIN tenmo_user USING(user_id) " +
            "WHERE username = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, username);
        if (results.next()) {
            return mapRowToAccount(results);
        }
        throw new AccountException("Account not found or unauthorized");
    }

    @Override
    public List<User> findAllUsers(String username) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username FROM tenmo_user;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            User user = mapRowToUser(results);
            if (user.getUsername().equals(username)) {
                continue;
            }
            users.add(user);
        }
        return users;
    }

    private Transfer mapRowToTransfer(SqlRowSet results) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(results.getLong("transfer_id"));
        transfer.setTransferTypeId(results.getLong("transfer_type_id"));
        transfer.setTransferStatusId(results.getLong("transfer_status_id"));
        transfer.setAccountFrom(results.getLong("account_from"));
        transfer.setAccountTo(results.getLong("account_to"));
        transfer.setAmount(results.getBigDecimal("amount"));
        transfer.setUsernameFrom(results.getString("username_from"));
        transfer.setUsernameTo(results.getString("username_to"));
        transfer.setTransferStatusDesc(results.getString("transfer_status_desc"));
        transfer.setTransferTypeDesc(results.getString("transfer_type_desc"));
        return transfer;
    }

    private Account mapRowToAccount(SqlRowSet results) {
        Account account = new Account();
        account.setUsername(results.getString("username"));
        account.setAccountId(results.getLong("account_id"));
        account.setBalance(results.getBigDecimal("balance"));
        account.setUserID(results.getLong("user_id"));
        return account;
    }

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setActivated(true);
        user.setAuthorities("USER");
        return user;
    }


}
