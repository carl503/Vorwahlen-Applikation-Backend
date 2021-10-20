package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.parser.DispensationParser;
import ch.zhaw.vorwahlen.repository.ClassListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Business logic for the modules.
 */
@RequiredArgsConstructor
@Service
public class DispensationService {

    private final Logger logger = Logger.getLogger(DispensationService.class.getName());

    private final ClassListRepository classListRepository;

    /**
     * Importing the Excel file and storing the needed content into the database.
     * @param file the Excel file to be parsed and stored.
     */
    public void importDispensationExcel(MultipartFile file, String worksheet) {
        try {
            var dispensationParser = new DispensationParser(file.getInputStream(), worksheet);
            var parsedList = dispensationParser.parseModulesFromXLSX();
            parsedList.forEach(student -> {
                try {
                    var dbStudent = classListRepository.getById(student.getEmail());
                    dbStudent.setPaDispensation(student.getPaDispensation());
                    dbStudent.setWpmDispensation(student.getWpmDispensation());
                    classListRepository.save(dbStudent);
                } catch (EntityNotFoundException e) {
                    logger.warning(e.getMessage());
                }
            });
        } catch (IOException e) {
            var message = String.format("Die Datei %s konnte nicht abgespeichert werden. Error: %s",
                    file.getOriginalFilename(), e.getMessage());
            logger.severe(message);
            // Todo throw custom Exception
        }
    }

}