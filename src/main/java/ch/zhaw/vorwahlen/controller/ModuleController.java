package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.EventoDataDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

/**
 * Controller for a module.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("module")
public class ModuleController {
    private final ModuleService moduleService;

    /**
     * Return all modules.
     * @return {@link ResponseEntity} containing list of {@link ModuleDTO}.
     */
    @GetMapping(path = {"/", ""})
    public ResponseEntity<List<ModuleDTO>> getAllModules() {
        return ResponseEntity.ok().body(moduleService.getAllModules());
    }

    /**
     * Add a module.
     * @param moduleDTO to be added module.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @PostMapping(path = {"/", ""}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addModule(@Valid @RequestBody ModuleDTO moduleDTO) {
        return ResponseEntity.created(moduleService.addAndReturnLocation(moduleDTO)).build();
    }

    /**
     * Returns the module by his id.
     * @param id of module.
     * @return {@link ResponseEntity} containing the {@link ModuleDTO}.
     */
    @GetMapping(path = {"/{id}", "/{id}/"})
    public ResponseEntity<ModuleDTO> getModuleById(@PathVariable String id) {
        return ResponseEntity.ok(moduleService.getModuleById(id));
    }

    /**
     * Deletes a module by his id.
     * @param id of module.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @DeleteMapping(path = {"/{id}", "/{id}/"})
    public ResponseEntity<Void> deleteModuleById(@PathVariable String id) {
        moduleService.deleteModuleById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Replace a module by his id.
     * @param id of module.
     * @param moduleDTO the new module.
     * @return {@link ResponseEntity} containing the new stored module.
     */
    @PutMapping(path = {"/{id}", "/{id}/"})
    public ResponseEntity<ModuleDTO> replaceModuleById(@PathVariable String id,
                                                       @Valid @RequestBody ModuleDTO moduleDTO) {
        return ResponseEntity.ok(moduleService.replaceModule(id, moduleDTO));
    }

    /**
     * Import module list from Excel.
     * @param file the Excel file.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @PostMapping(path = {"/", ""}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveModulesFromExcel(@RequestParam("file") MultipartFile file,
                                                       @RequestParam("worksheet") String worksheet) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        moduleService.importModuleExcel(file, worksheet);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns the additional data of a module.
     * @return {@link ResponseEntity} containing the {@link EventoDataDTO}.
     */
    @GetMapping(path = {"/eventodata/{moduleKuerzel}"})
    public ResponseEntity<EventoDataDTO> getAdditionalModuleDataByKuerzel(@PathVariable String moduleKuerzel) {
        return ResponseEntity.ok().body(moduleService.getEventoDataById(moduleKuerzel));
    }

    /**
     * Scrape external website to retrieve additional data from the module list.
     * @return {@link ResponseEntity} containing {@link Void}.
     */
    @PostMapping(path = "/scrape")
    public ResponseEntity<Void> fetchAdditionalModuleData() {
        moduleService.fetchAdditionalModuleData();
        return ResponseEntity.ok().build();
    }

}
