package com.rental.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OwnerPrincipal {
    private Long id;
    private String email;
    private String role;
}