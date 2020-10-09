package com.example.webapp.repos;

import com.example.webapp.domain.Message;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.List;

public interface MessageRepo extends CrudRepository <Message, Long> {
    Streamable<Message> findBySnContainingIgnoreCase(String sn);
    Streamable<Message> findByTextContainingIgnoreCase(String text);
    Streamable<Message> findByOwnerContainingIgnoreCase(String owner);
}

