package com.IBCSolutions.TransferApp.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @ToString.Exclude
    private String password;

    @EqualsAndHashCode.Exclude 
    @ToString.Exclude 
    @JsonIgnore
    @OneToOne(mappedBy = "user", orphanRemoval = true, fetch = FetchType.EAGER)
    private Account account;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.account = null;
    }
}