package com.poly.ASM.service.notification;

import com.poly.ASM.entity.notification.Notification;
import com.poly.ASM.entity.order.Order;
import com.poly.ASM.entity.user.Account;

import java.util.List;
import java.util.Optional;

public interface NotificationService {

    Notification createNotification(Account account, Order order, String title, String content);

    void notifyOrderPlacedForUser(Account account, Order order);

    void notifyOrderPlacedForAdmins(Order order);

    void notifyOrderStatusChange(Order order, String status);

    long countUnread(String username);

    List<Notification> getLatest(String username, int limit);

    Optional<Notification> findByIdAndUsername(Long id, String username);

    Notification markRead(Notification notification);
}
