package ch.zhaw.vorwahlen.model.evento;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dto for the {@link EventoData}.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Data
@Builder
public class EventoDataDTO {
    private String shortDescription;
    private String coordinator;
    private String learningObjectives;
    private String moduleContents;
    private String literature;
    private String suppLiterature;
    private String prerequisites;
    private String moduleStructure;
    private String exams;
    private String remarks;
}
