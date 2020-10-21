package com.example.webapp.repos;
import com.example.webapp.domain.Message;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.List;

public interface MessageByOwner extends CrudRepository<Message, Long> {
//    List<Message> findByOwner(String owner);

    Streamable<Message> findByOwner(String owner);
}
