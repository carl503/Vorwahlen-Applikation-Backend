package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.mailtemplate.MailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link MailTemplate}.
 */
@Repository
public interface MailTemplateRepository extends JpaRepository<MailTemplate, Long> {
}
