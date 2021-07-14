package com.example.webapp.repos;
import com.example.webapp.domain.Journal;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional

public interface JournalFind extends CrudRepository<Journal, Long> {
    List<Journal> findByMessageid(Long messageid);
    void deleteByMessageid(Long messageid);
}
