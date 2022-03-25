package com.mjs8071.tenmo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus ( code = HttpStatus.NOT_FOUND, reason = "Transfer not found.")
public class TransferNotFoundException extends Throwable {
    private static final long serialVersionUID = 1L;

    public TransferNotFoundException() {
        super("Transfer not found.");
    }
}
