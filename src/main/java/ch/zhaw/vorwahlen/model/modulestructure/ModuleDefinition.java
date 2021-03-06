package ch.zhaw.vorwahlen.model.modulestructure;

import ch.zhaw.vorwahlen.model.core.module.ModuleCategory;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Class defining which semester for which category has how many modules to be selected.
 */
@Data
public class ModuleDefinition {
    private Map<Integer, Integer> contextModules;
    private Map<Integer, Integer> projectModule;
    private Map<Integer, Integer> interdisciplinaryModules;
    private Map<Integer, Integer> subjectModules;
    private Map<Integer, Integer> bachelorModule;

    /**
     * Get the definition by category
     * @param category the category to return the definition.
     * @return {@code Map<Integer, Integer>}
     */
    Map<Integer, Integer> getDefinitionByCategory(ModuleCategory category) {
        return switch (category) {
            case CONTEXT_MODULE -> contextModules;
            case PROJECT_MODULE -> projectModule;
            case INTERDISCIPLINARY_MODULE -> interdisciplinaryModules;
            case BACHELOR_MODULE -> bachelorModule;
            case SUBJECT_MODULE -> subjectModules;
            case DISPENSED_WPM_MODULE, DISPENSED_PA_MODULE -> new HashMap<>();
        };
    }
}
