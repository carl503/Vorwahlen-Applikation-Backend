package ch.zhaw.vorwahlen.controller;

import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.service.ModuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("dev")
@SpringBootTest(properties = "classpath:settings.properties")
@AutoConfigureMockMvc
class ModuleControllerTest {

    private static final String LANGUAGE_DE = "de";
    private static final String LANGUAGE_EN = "en";
    private static final String REQUEST_MAPPING_PREFIX = "/module";
    private static final String MULTIPART_FILE_REQUEST_PARAMETER = "file";
    private static final String MODULE_LIST_FILE_NAME = "Liste_alle_Module_SM2025_SGL_Def_1.7-2021-03-29.xlsx";
    private static final String WORKSHEET = "Module 2025";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ModuleService moduleService;

    static {
        System.setProperty("ADMIN", "dev@zhaw.ch");
    }

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */
    
    @Test
    void testGetAllModules() {
        // prepare
        var expectedList = new ArrayList<ModuleDTO>();
        expectedList.add(new ModuleDTO("nr1", "title1", LANGUAGE_DE));
        expectedList.add(new ModuleDTO("nr2", "title2", LANGUAGE_EN));
        expectedList.add(new ModuleDTO("nr3", "title3", LANGUAGE_DE));

        when(moduleService.getAllModules()).thenReturn(expectedList);

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .get(REQUEST_MAPPING_PREFIX)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$.[*].module_no").isNotEmpty())
                    .andExpect(jsonPath("$.[*].module_no", anyOf(
                            hasItem(expectedList.get(0).getModuleNo()),
                            hasItem(expectedList.get(1).getModuleNo()),
                            hasItem(expectedList.get(2).getModuleNo())
                    )))
                    .andExpect(jsonPath("$.[*].module_title").isNotEmpty())
                    .andExpect(jsonPath("$.[*].module_title", anyOf(
                            hasItem(expectedList.get(0).getModuleTitle()),
                            hasItem(expectedList.get(1).getModuleTitle()),
                            hasItem(expectedList.get(2).getModuleTitle())
                    )))
                    .andExpect(jsonPath("$.[*].language").isNotEmpty())
                    .andExpect(jsonPath("$.[*].language", anyOf(
                            hasItem(LANGUAGE_DE),
                            hasItem(LANGUAGE_EN)
                    )))
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(1)).getAllModules();
    }

    @Test
    void testSaveModulesFromExcel() throws IOException {
        // prepare
        var fis = getClass().getClassLoader().getResourceAsStream(MODULE_LIST_FILE_NAME);
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, MODULE_LIST_FILE_NAME, "", fis);

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .multipart(REQUEST_MAPPING_PREFIX)
                    .file(mockMultipartFile)
                    .param("worksheet", WORKSHEET)
                    .with(csrf())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(1)).importModuleExcel(mockMultipartFile, WORKSHEET);
    }


    @Test
    void testFetchAdditionalModuleData() {
        // prepare

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .post(REQUEST_MAPPING_PREFIX + "/scrape")
                    .with(csrf())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(1)).fetchAdditionalModuleData();
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */


    @Test
    void testSaveModulesFromExcel_WithoutAFile() {
        // prepare
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, "", "", "".getBytes());

        // execute
        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .multipart(REQUEST_MAPPING_PREFIX)
                    .file(mockMultipartFile)
                    .param("worksheet", WORKSHEET)
                    .with(csrf())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        } catch (Exception e) {
            fail(e);
        }

        // verify
        verify(moduleService, times(0)).importModuleExcel(mockMultipartFile, WORKSHEET);
    }

}
