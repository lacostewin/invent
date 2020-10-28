package com.example.webapp.repos;
import com.example.webapp.domain.Journal;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface JournalAdd extends CrudRepository<Journal, Long> {
        @Modifying
        @Query(value = "insert into journals (messageid, newowner) select m.id, m.owner from message m where m.id = ?", nativeQuery = true)
        void updateJournalNewownerMessageid(Long id);
}