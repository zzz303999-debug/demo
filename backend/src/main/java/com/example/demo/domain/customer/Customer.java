package com.example.demo.domain.customer;

import com.example.demo.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 客户实体。
 */
@Entity
@Table(name = "customer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    // ========== 领域行为 ==========

    /**
     * 修改客户信息（只允许修改邮箱、手机号、地址）。
     */
    public void updateProfile(String email, String phone, String address) {
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
}
