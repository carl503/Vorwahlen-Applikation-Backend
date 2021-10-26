package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.StudentDTO;
import ch.zhaw.vorwahlen.service.ClassListService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ch.zhaw.vorwahlen.service.ClassListService.PA_DISPENSATION;
import static ch.zhaw.vorwahlen.service.ClassListService.WPM_DISPENSATION;

@SpringBootTest
@AutoConfigureMockMvc
class ClassListControllerTest {

    private static final String CLASS_1 = "class1";
    private static final String CLASS_2 = "class2";
    private static final String REQUEST_MAPPING_PREFIX = "/class";
    private static final String MULTIPART_FILE_REQUEST_PARAMETER = "file";
    private static final String CLASS_LIST_FILE_NAME = "Vorlage_Klassenzuteilungen.xlsx";
    private static final String WORKSHEET = "Sheet1";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ClassListService classListService;

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */
    
    @Test
    void testGetAllModules() {
        // prepare
        var expectedList = new ArrayList<StudentDTO>();
        expectedList.add(StudentDTO.builder().email("mail1").name("name1").clazz(CLASS_1).paDispensation(PA_DISPENSATION).wpmDispensation(WPM_DISPENSATION).build());
        expectedList.add(StudentDTO.builder().email("mail2").name("name2").clazz(CLASS_1).paDispensation(PA_DISPENSATION).wpmDispensation(WPM_DISPENSATION).build());
        expectedList.add(StudentDTO.builder().email("mail3").name("name3").clazz(CLASS_1).paDispensation(PA_DISPENSATION).wpmDispensation(WPM_DISPENSATION).build());

        when(classListService.getAllClassLists()).thenReturn(expectedList);

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .get(REQUEST_MAPPING_PREFIX)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$.[*].email").isNotEmpty())
                    .andExpect(jsonPath("$.[*].email", anyOf(
                            hasItem(expectedList.get(PA_DISPENSATION).getEmail()),
                            hasItem(expectedList.get(1).getEmail()),
                            hasItem(expectedList.get(2).getEmail())
                    )))
                    .andExpect(jsonPath("$.[*].name").isNotEmpty())
                    .andExpect(jsonPath("$.[*].name", anyOf(
                            hasItem(expectedList.get(PA_DISPENSATION).getName()),
                            hasItem(expectedList.get(1).getName()),
                            hasItem(expectedList.get(2).getName())
                    )))
                    .andExpect(jsonPath("$.[*].class").isNotEmpty())
                    .andExpect(jsonPath("$.[*].class", anyOf(
                            hasItem(CLASS_1),
                            hasItem(CLASS_2)
                    )))
                    .andExpect(jsonPath("$.[*].dispensation_pa").isNotEmpty())
                    .andExpect(jsonPath("$.[*].dispensation_pa", anyOf(hasItem(PA_DISPENSATION))))
                    .andExpect(jsonPath("$.[*].dispensation_wpm").isNotEmpty())
                    .andExpect(jsonPath("$.[*].dispensation_wpm", anyOf(hasItem(PA_DISPENSATION))))
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(classListService, times(1)).getAllClassLists();
    }

    @Test
    void testSaveClassListsFromExcel() throws IOException {
        // prepare
        var fis = getClass().getClassLoader().getResourceAsStream(CLASS_LIST_FILE_NAME);
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, CLASS_LIST_FILE_NAME, "", fis);

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .multipart(REQUEST_MAPPING_PREFIX)
                    .file(mockMultipartFile)
                    .param("worksheet", WORKSHEET)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(classListService, times(1)).importClassListExcel(mockMultipartFile, WORKSHEET);
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

    @Test
    void testSaveClassListsFromExcel_WithoutAFile() {
        // prepare
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, "", "", "".getBytes());

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .multipart(REQUEST_MAPPING_PREFIX)
                    .file(mockMultipartFile)
                    .param("worksheet", WORKSHEET)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(classListService, times(0)).importClassListExcel(mockMultipartFile, WORKSHEET);
    }

}
