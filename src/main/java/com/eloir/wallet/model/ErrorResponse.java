package com.eloir.wallet.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ErrorResponse {

    private String code;
    private String message;
    private String details;
    private String path;
}

