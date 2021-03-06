package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.core.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Student}.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
}
