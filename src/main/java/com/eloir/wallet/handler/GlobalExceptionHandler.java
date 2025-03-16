package com.eloir.wallet.handler;

import com.eloir.wallet.exception.WalletLockedException;
import com.eloir.wallet.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletLockedException.class)
    public ResponseEntity<ErrorResponse> handleWalletLockedException(WalletLockedException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                "422",
                "The wallet is temporarily locked due to another operation. Please try again later.",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                "500",
                "Internal server error",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
