package com.example.webapp.repos;
import com.example.webapp.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface MessageRepo extends CrudRepository <Message, Long> {
    Streamable<Message> findBySnContainingIgnoreCase(String sn);
    Streamable<Message> findByTextContainingIgnoreCase(String text);
    Streamable<Message> findByOwnerContainingIgnoreCase(String owner);
    Streamable<Message> findByInvidContainingIgnoreCase(String invid);
    boolean existsMessageByInvidIgnoreCase(String invid);

    void deleteById (Long id);

    Streamable<Message> findByOwner(String owner);
    Streamable<Message> findByInvid(String invid);
    Iterable<Message> findAll(Pageable limit);

    @Query("select qrcode from Message m where m.id = ?1")
    String selectQrcodeById(Long id);
}