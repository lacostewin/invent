package com.example.webapp.repos;
import com.example.webapp.domain.Message;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Transactional
public interface MessageDel extends CrudRepository<Message, Long>{
    List<Message> deleteBySn (String sn);
}
