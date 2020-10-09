package com.example.webapp.repos;

import com.example.webapp.domain.Message;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MessageRepo2 extends CrudRepository<Message, Long> {
    List<Message> findBySn(String sn);
    boolean existsMessageBySnIgnoreCase(String sn);
}
