package com.poly.ASM.repository.notification;

import com.poly.ASM.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByAccountUsernameAndReadFalse(String username);

    @Query("""
            select n
            from Notification n
            left join fetch n.order o
            where n.account.username = :username
            order by n.createdAt desc
            """)
    List<Notification> findLatestByUsername(@Param("username") String username, Pageable pageable);

    @Query("""
            select n
            from Notification n
            left join fetch n.order o
            where n.id = :id and n.account.username = :username
            """)
    Optional<Notification> findByIdAndUsername(@Param("id") Long id, @Param("username") String username);
}
