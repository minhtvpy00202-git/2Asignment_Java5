package com.poly.ASM.repository.order;

import com.poly.ASM.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByAccountUsernameOrderByCreateDateDesc(String username);

    @Query("""
            select a.fullname as fullname,
                   sum(d.price * d.quantity) as totalAmount,
                   min(o.createDate) as firstOrderDate,
                   max(o.createDate) as lastOrderDate
            from Order o
            join o.account a
            join o.orderDetails d
            group by a.fullname
            order by sum(d.price * d.quantity) desc
            """)
    List<VipReport> getVipCustomers(Pageable pageable);
}
