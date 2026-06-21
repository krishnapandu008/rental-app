package com.rental.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "owners")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Owner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;
    private String phone;
}