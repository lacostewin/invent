package com.example.webapp.repos;
import com.example.webapp.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface QRModify extends JpaRepository<Message, Long> {
    @Modifying
    @Query("update Message m set m.qrcode = ?1 where m.id = ?2")
    void setQRCodeFor(String qrcode, Long id);
}