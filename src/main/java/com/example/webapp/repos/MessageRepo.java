package com.example.webapp.repos;

import com.example.webapp.domain.Message;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface MessageRepo extends CrudRepository <Message, Long> {
    Streamable<Message> findBySnContainingIgnoreCase(String sn);
    Streamable<Message> findByTextContainingIgnoreCase(String text);
    Streamable<Message> findByOwnerContainingIgnoreCase(String owner);
    Streamable<Message> findByInvidContainingIgnoreCase(String invid);

    List<Message> findBySn(String sn);
    boolean existsMessageBySnIgnoreCase(String sn);
    boolean existsMessageByInvidIgnoreCase(String invid);

    List<Message> deleteBySn (String sn);

    Streamable<Message> findByOwner(String owner);
    Streamable<Message> findById(Integer id);
}

