package com.example.webapp.repos;
import com.example.webapp.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface MessageRepo extends CrudRepository <Message, Long> {
    Streamable<Message> findBySnContainingIgnoreCase(String sn);
    Streamable<Message> findByTextContainingIgnoreCase(String text);
    Streamable<Message> findByOwnerContainingIgnoreCase(String owner);
    Streamable<Message> findByInvidContainingIgnoreCase(String invid);
    boolean existsMessageByInvidIgnoreCase(String invid);

//    List<Message> findBySn(String sn);
//    List<Message> deleteBySn (Long sn);

    void deleteById (Long id);

    Streamable<Message> findByOwner(String owner);
    Streamable<Message> findByInvid(String invid);
    Iterable<Message> findAll(Pageable limit);
}