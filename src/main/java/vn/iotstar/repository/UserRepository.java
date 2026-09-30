package vn.iotstar.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.iotstar.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(
            String username,
            String email
    );

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String username,
            String email,
            String fullName,
            Pageable pageable
    );

    @Query("""
            SELECT u
            FROM User u
            JOIN FETCH u.role
            WHERE LOWER(u.username) = LOWER(:login)
               OR LOWER(u.email) = LOWER(:login)
            """)
    Optional<User> findForLogin(
            @Param("login") String login
    );

    @Query("""
            SELECT COUNT(p)
            FROM Product p
            WHERE p.user.id = :userId
            """)
    long countProductsByUserId(
            @Param("userId") Long userId
    );
}