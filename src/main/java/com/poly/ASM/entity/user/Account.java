package com.poly.ASM.entity.user;

import com.poly.ASM.entity.order.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullname;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(length = 255)
    private String photo;

    @Column(nullable = false)
    private Boolean activated;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<Authority> authorities = new ArrayList<>();

    @PrePersist
    private void applyDefaults() {
        if (activated == null) {
            activated = true;
        }
    }
}
