package com.bankbitcoinow.models;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "\"user\"")
public class User {
    private Long id;
    private String password;
    private String email;
    private String otpKeyid;
    private Timestamp createdAt;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    @SequenceGenerator(name = "user_seq_gen", sequenceName = "user_id_seq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpKeyid() {
        return otpKeyid;
    }

    public void setOtpKeyid(String otpKeyid) {
        this.otpKeyid = otpKeyid;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
