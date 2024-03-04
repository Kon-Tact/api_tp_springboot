package com.tp_api.API.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column (name = "phone_number")
    private int phoneNumber;
    private String email;
    private String address;
}
