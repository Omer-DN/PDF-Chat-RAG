package org.handson.ragllm.repository;

import org.handson.ragllm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * חיפוש משתמש לפי כתובת האימייל שלו.
     * ישמש אותנו בשלבי ההתחברות (Login).
     */
    Optional<User> findByEmail(String email);

    /**
     * בדיקה האם אימייל מסוים כבר תפוס במערכת.
     * ישמש אותנו בשלב הרישום (Registration).
     */
    boolean existsByEmail(String email);
}