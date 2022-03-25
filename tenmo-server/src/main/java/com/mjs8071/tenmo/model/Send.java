package com.mjs8071.tenmo.model;

import java.math.BigDecimal;

public class Send {
    private String usernameTo;
    private BigDecimal amount;

    public String getUsernameTo() {
        return usernameTo;
    }

    public void setUsernameTo(String usernameTo) {
        this.usernameTo = usernameTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
