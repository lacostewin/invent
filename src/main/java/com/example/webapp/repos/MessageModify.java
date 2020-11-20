package com.example.webapp.repos;
import com.example.webapp.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface MessageModify extends JpaRepository<Message, Long> {
    @Modifying
    @Query("update Message m set m.owner = ?1,  m.text = ?2, m.sn = ?3, m.author = ?4, m.invid = ?5 where m.id = ?6")
    String setFixedOwnerFor(String owner, String text, String sn, String author, String invid, Long id);
}