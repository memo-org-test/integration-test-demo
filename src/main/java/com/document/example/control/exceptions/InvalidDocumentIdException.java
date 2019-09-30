package com.document.example.control.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class InvalidDocumentIdException extends IllegalArgumentException {
    public InvalidDocumentIdException(String msg) {
        super(msg);
    }
}
