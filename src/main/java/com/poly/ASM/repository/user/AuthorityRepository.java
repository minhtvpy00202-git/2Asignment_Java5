package com.poly.ASM.repository.user;

import com.poly.ASM.entity.user.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    boolean existsByAccountUsernameAndRoleId(String username, String roleId);

    List<Authority> findByAccountUsername(String username);

    @Query("""
            select a
            from Authority a
            join fetch a.account acc
            join fetch a.role r
            where r.id = :roleId
            """)
    List<Authority> findByRoleId(@Param("roleId") String roleId);

    void deleteByAccountUsername(String username);
}
