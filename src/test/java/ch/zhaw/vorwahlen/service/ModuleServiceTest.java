package ch.zhaw.vorwahlen.service;

import ch.zhaw.vorwahlen.exception.ModuleNotFoundException;
import ch.zhaw.vorwahlen.mapper.Mapper;
import ch.zhaw.vorwahlen.model.dto.EventoDataDTO;
import ch.zhaw.vorwahlen.model.dto.ModuleDTO;
import ch.zhaw.vorwahlen.model.modules.EventoData;
import ch.zhaw.vorwahlen.model.modules.Module;
import ch.zhaw.vorwahlen.model.modules.ModuleCategory;
import ch.zhaw.vorwahlen.model.modules.Student;
import ch.zhaw.vorwahlen.repository.EventoDataRepository;
import ch.zhaw.vorwahlen.repository.ModuleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("dev")
class ModuleServiceTest {

    private static final String MODULE_LIST_FILE_NAME = "Liste_alle_Module_SM2025_SGL_Def_1.7-2021-03-29.xlsx";
    private static final String WORK_SHEET_NAME = "Module 2025";
    private static final String MULTIPART_FILE_REQUEST_PARAMETER = "file";
    private static final String MODULE_NO = "t.BA.WM.DAST-EN.19HS";
    private static final String MODULE_NO_SHORT = "WM.DAST-EN";

    private final ModuleRepository moduleRepository;
    private final EventoDataRepository eventoDataRepository;
    private final Mapper<ModuleDTO, Module> moduleMapper;
    private final Mapper<EventoDataDTO, EventoData> eventoDataMapper;
    private ModuleService moduleService;

    @Autowired
    public ModuleServiceTest(ModuleRepository moduleRepository,
                             EventoDataRepository eventoDataRepository,
                             Mapper<ModuleDTO, Module> moduleMapper,
                             Mapper<EventoDataDTO, EventoData> eventoDataMapper) {
        this.moduleRepository = moduleRepository;
        this.eventoDataRepository = eventoDataRepository;
        this.moduleMapper = moduleMapper;
        this.eventoDataMapper = eventoDataMapper;
    }

    @BeforeEach
    void setUp() {
        moduleService = new ModuleService(moduleRepository, eventoDataRepository, moduleMapper, eventoDataMapper);
    }

    @AfterEach
    void tearDown() {
        moduleRepository.deleteAll();
        eventoDataRepository.deleteAll();
    }

    @Test
    @Sql("classpath:sql/modules.sql")
    void testGetAllModules() {
        var result = moduleService.getAllModules(null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(75, result.size());

        var studentMock = mock(Student.class);
        when(studentMock.isTZ()).thenReturn(true);
        when(studentMock.isSecondElection()).thenReturn(false);

        result = moduleService.getAllModules(studentMock);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(59, result.size());

        when(studentMock.isSecondElection()).thenReturn(true);

        result = moduleService.getAllModules(studentMock);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(67, result.size());
    }

    @Test
    void testAddAndReturnLocation() throws URISyntaxException {
        var moduleDto = ModuleDTO.builder()
                        .moduleNo("t.BA.WM.HELLO.19HS")
                        .shortModuleNo("WM.HELLO")
                        .moduleTitle("Hello")
                        .moduleId(1)
                        .moduleGroup("AV6,DS6,ET5,EU6,IT6,MT7,ST5,VS6,WI6")
                        .isIPModule(true)
                        .institute("INIT")
                        .credits((byte) 4)
                        .language("English")
                        .executionSemester(new ModuleDTO.ExecutionSemester(List.of(5), List.of(7)))
                        .consecutiveModuleNo("")
                        .build();

        assertThrows(ModuleNotFoundException.class, () -> moduleService.getModuleById(moduleDto.getModuleNo()));
        var result = moduleService.addAndReturnLocation(moduleDto);
        assertNotNull(moduleService.getModuleById(moduleDto.getModuleNo()));
        assertEquals(new URI("/module/".concat(moduleDto.getModuleNo())), result);
    }

    @Test
    @Sql("classpath:sql/modules.sql")
    void testReplaceModule() {
        var module = moduleService.getModuleById(MODULE_NO);
        assertEquals(MODULE_NO_SHORT, module.getShortModuleNo());

        var moduleDto = ModuleDTO.builder()
                .moduleNo(MODULE_NO)
                .shortModuleNo("WM.HELLO")
                .moduleTitle("Hello")
                .moduleId(1)
                .moduleGroup("AV6,DS6,ET5,EU6,IT6,MT7,ST5,VS6,WI6")
                .isIPModule(true)
                .institute("INIT")
                .credits((byte) 4)
                .language("English")
                .executionSemester(new ModuleDTO.ExecutionSemester(List.of(5), List.of(7)))
                .consecutiveModuleNo("")
                .build();
        moduleService.replaceModule(MODULE_NO, moduleDto);

        module = moduleService.getModuleById(MODULE_NO);
        assertEquals("WM.HELLO", module.getShortModuleNo());
    }

    @Test
    @Sql("classpath:sql/modules.sql")
    void testDeleteModuleById() {
        assertNotNull(moduleService.getModuleById(MODULE_NO));
        moduleService.deleteModuleById(MODULE_NO);
        assertThrows(ModuleNotFoundException.class, () -> moduleService.getModuleById(MODULE_NO));
    }

    @Test
    void testImportModuleExcel() throws IOException {
        // prepare
        var fis = getClass().getClassLoader().getResourceAsStream(MODULE_LIST_FILE_NAME);
        var mockMultipartFile = new MockMultipartFile(MULTIPART_FILE_REQUEST_PARAMETER, MODULE_LIST_FILE_NAME, "", fis);

        // execute
        assertDoesNotThrow(() -> moduleService.importModuleExcel(mockMultipartFile, WORK_SHEET_NAME));

        // verify
        var result = moduleService.getAllModules(null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(75, result.size());
        assertEquals(24, result.stream().filter(moduleDTO -> !moduleDTO.getConsecutiveModuleNo().isBlank()).count());
    }

    @Test
    @Sql("classpath:sql/modules.sql")
    void testGetModuleById() {
        var expected = ModuleDTO.builder()
                .executionSemester(new ModuleDTO.ExecutionSemester(List.of(5), List.of(7)))
                .moduleNo("t.BA.WM.DHEAL-EN.19HS")
                .shortModuleNo("WM.DHEAL-EN")
                .moduleId(1653946)
                .moduleGroup("DS6,ET5,IT6,MT7,ST5,WI6")
                .isIPModule(true)
                .institute("INIT")
                .category(ModuleCategory.INTERDISCIPLINARY_MODULE)
                .credits((byte) 4)
                .language("Englisch")
                .moduleTitle("Digital Health")
                .consecutiveModuleNo("")
                .build();
        var result = moduleService.getModuleById(expected.getModuleNo());
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void testGetModuleById_Null() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> moduleService.getModuleById(null));
    }

    @Test
    void testGetModuleById_Not_Existing() {
        assertThrows(ModuleNotFoundException.class, () -> moduleService.getModuleById("invalid"));
    }

    @Disabled
    @Test
    @Sql("classpath:sql/")
    void testGetEventoDataById() {
        // todo: create sql data
    }

}
