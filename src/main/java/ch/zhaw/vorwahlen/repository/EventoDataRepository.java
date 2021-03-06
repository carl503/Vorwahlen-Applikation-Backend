package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.evento.EventoData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link EventoData}.
 */
@Repository
public interface EventoDataRepository extends JpaRepository<EventoData, String> {
}
