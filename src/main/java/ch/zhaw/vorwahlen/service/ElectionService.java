
package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.model.dto.ElectionTransferDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleElectionDTO;
import ch.zhaw.vorwahlen.model.dto.ElectionStructureDTO;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleElection;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructure;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureFullTime;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructureGenerator;
import ch.zhaw.vorwahlen.model.modulestructure.ModuleStructurePartTime;
import ch.zhaw.vorwahlen.modulevalidation.FullTimeElectionValidator;
import ch.zhaw.vorwahlen.modulevalidation.PartTimeElectionValidator;
import ch.zhaw.vorwahlen.repository.ElectionRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Business logic for the election.
 */
@RequiredArgsConstructor
@Service
@Log
public class ElectionService {

    public static final int NUM_CONTEXT_MODULES = 3;
    public static final int NUM_SUBJECT_MODULES = 8;
    public static final int NUM_INTERDISCIPLINARY_MODULES = 1;

    private final ElectionRepository electionRepository;
    private final Function<Set<String>, Set<Module>> mapModuleSet;
    private final ModuleStructure structureFullTime;
    private final ModuleStructure structurePartTime;

    @Autowired
    public ElectionService(ElectionRepository electionRepository, ModuleRepository moduleRepository,
                           ModuleStructureFullTime structureFullTime, ModuleStructurePartTime structurePartTime) {
        this.electionRepository = electionRepository;
        this.mapModuleSet = list -> list.stream()
                                        .map(moduleRepository::getById)
                                        .collect(Collectors.toSet());
        this.structureFullTime = structureFullTime;
        this.structurePartTime = structurePartTime;
    }

    public ElectionTransferDTO getElection(Student student) {
        var isElectionValid = false;
        var isElectionSaved = false;
        var structure = getModuleStructure(student);
        if (student.getElection() != null) {
            isElectionSaved = true;
            isElectionValid = student.getElection().isElectionValid();
        }
        return new ElectionTransferDTO(structure, isElectionSaved, isElectionValid);
    }
        var structure = student.isTZ() ? structurePartTime : structureFullTime;
        var election = student.getElection();
        return new ModuleStructureGenerator(structure, election, student).generateStructure();
    }

    /**
     * Gets the stored election from student.
     * @param student student in session
     * @return current election
     */
    public ModuleElectionDTO getModuleElectionByStudent(Student student) {
        var optional = Optional.ofNullable(student.getElection());
        if(optional.isPresent()) {
            return optional.map(DTOMapper.mapElectionToDto).get();
        }
        return null;
    }

    /**
     * Saves the election to the database.
     * @param student student in session
     * @param moduleElectionDTO his current election
     * @return true - if save successful<br>
     *         false - if arguments invalid
     */
    public ElectionTransferDTO saveElection(Student student, String moduleNo) {
        if(student == null || moduleNo == null || moduleNo.isBlank()
                || student.getEmail() == null || student.getEmail().isBlank()) {
            //todo throw custom exception
            throw new RuntimeException("");
        }

        ModuleElection moduleElection;

        if (student.getElection() != null) {
            moduleElection = student.getElection();
        } else {
            moduleElection = new ModuleElection();
        }


        var electionValidator = student.isTZ()
                ? new PartTimeElectionValidator(student)
                : new FullTimeElectionValidator(student);

        var isValid = electionValidator.validate(moduleElection);
        moduleElection.setElectionValid(isValid);
        student.setElection(electionRepository.save(moduleElection));
        return new ElectionTransferDTO(getModuleStructure(student), true, isValid);
    }
}
