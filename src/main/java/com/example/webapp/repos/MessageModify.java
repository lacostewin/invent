package com.example.webapp.repos;
import com.example.webapp.domain.Message;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MessageModify extends CrudRepository<Message, Long>{
    @Modifying
    @Query("update Message m set m.owner = ?1,  m.text = ?2, m.sn = ?3 where m.id = ?4")
    String setFixedOwnerFor(String owner, String text, String sn, Integer id);
}