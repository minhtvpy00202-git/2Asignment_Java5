package com.poly.ASM.service.notification.impl;

import com.poly.ASM.entity.notification.Notification;
import com.poly.ASM.entity.order.Order;
import com.poly.ASM.entity.user.Account;
import com.poly.ASM.entity.user.Authority;
import com.poly.ASM.repository.notification.NotificationRepository;
import com.poly.ASM.repository.user.AuthorityRepository;
import com.poly.ASM.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificationRepository notificationRepository;
    private final AuthorityRepository authorityRepository;

    @Override
    public Notification createNotification(Account account, Order order, String title, String content) {
        Notification notification = new Notification();
        notification.setAccount(account);
        notification.setOrder(order);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Override
    public void notifyOrderPlacedForUser(Account account, Order order) {
        String time = FORMATTER.format(order.getCreateDate());
        String title = "Đặt hàng thành công";
        String content = "Bạn đã đặt hàng thành công đơn #" + order.getId() + " vào lúc " + time;
        createNotification(account, order, title, content);
    }

    @Override
    public void notifyOrderPlacedForAdmins(Order order) {
        String time = FORMATTER.format(order.getCreateDate());
        String title = "Đơn hàng mới";
        String content = "Bạn có đơn hàng mới #" + order.getId() + " vào lúc " + time;
        List<Authority> admins = authorityRepository.findByRoleId("ADMIN");
        for (Authority authority : admins) {
            Account account = authority.getAccount();
            if (account != null) {
                createNotification(account, order, title, content);
            }
        }
    }

    @Override
    public void notifyOrderStatusChange(Order order, String status) {
        if (order.getAccount() == null) {
            return;
        }
        String title = "Cập nhật đơn hàng";
        String content;
        if ("DELIVERED_SUCCESS".equals(status) || "DONE".equals(status)) {
            content = "Đơn hàng #" + order.getId() + " đã được giao thành công";
        } else if ("DELIVERY_FAILED".equals(status) || "CANCEL".equals(status)) {
            content = "Đơn hàng #" + order.getId() + " giao hàng thất bại";
        } else {
            return;
        }
        createNotification(order.getAccount(), order, title, content);
    }

    @Override
    public long countUnread(String username) {
        return notificationRepository.countByAccountUsernameAndReadFalse(username);
    }

    @Override
    public List<Notification> getLatest(String username, int limit) {
        return notificationRepository.findLatestByUsername(username, PageRequest.of(0, limit));
    }

    @Override
    public Optional<Notification> findByIdAndUsername(Long id, String username) {
        return notificationRepository.findByIdAndUsername(id, username);
    }

    @Override
    public Notification markRead(Notification notification) {
        notification.setRead(true);
        return notificationRepository.save(notification);
    }
}
