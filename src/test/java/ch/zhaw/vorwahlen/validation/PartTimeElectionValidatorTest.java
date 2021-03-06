package ch.zhaw.vorwahlen.validation;

import ch.zhaw.vorwahlen.model.ExecutionSemester;
import ch.zhaw.vorwahlen.model.core.module.Module;
import ch.zhaw.vorwahlen.model.core.module.ModuleCategory;
import ch.zhaw.vorwahlen.model.core.election.Election;
import ch.zhaw.vorwahlen.model.core.validationsetting.ValidationSetting;
import ch.zhaw.vorwahlen.modules.ModuleCategoryTest;
import ch.zhaw.vorwahlen.parser.ModuleParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PartTimeElectionValidatorTest extends AbstractElectionValidatorTest {

    public static final ExecutionSemester SEMESTER_6 = ExecutionSemester.SPRING;
    public static final ExecutionSemester SEMESTER_5 = ExecutionSemester.AUTUMN;
    public static final int NUM_NON_CONSECUTIVE_SUBJECT_MODULES = 2;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        validator = new PartTimeElectionValidator(studentMock);
    }

    /* **************************************************************************************************************
     * Positive tests
     * ************************************************************************************************************** */

    @Test
    void testValidateElectionPartTime() {
        var isFistElection = true;
        validElectionSet = generateValidPartTimeElectionSet(isFistElection);
        when(studentMock.isSecondElection()).thenReturn(!isFistElection);
        testValidateElectionPartTimeCheck(isFistElection);

        isFistElection = false;
        validElectionSet = generateValidPartTimeElectionSet(isFistElection);
        when(studentMock.isSecondElection()).thenReturn(!isFistElection);
        testValidateElectionPartTimeCheck(isFistElection);
    }

    void testValidateElectionPartTimeCheck(boolean isFistElection){
        //===== Returns valid
        var validationSettingMock = mock(ValidationSetting.class);
        when(validationSettingMock.hadAlreadyElectedTwoConsecutiveModules()).thenReturn(false);
        when(validationSettingMock.isSkipConsecutiveModuleCheck()).thenReturn(false);
        when(validationSettingMock.isRepetent()).thenReturn(true);
        when(validationSettingMock.getElectedContextModulesInFirstElection()).thenReturn(PartTimeElectionValidator.NUM_CONTEXT_MODULES_FIRST_ELECTION);

        when(electionMock.getValidationSetting()).thenReturn(validationSettingMock);

        assertTrue(validator.validate(electionMock).isValid());

        when(validationSettingMock.isRepetent()).thenReturn(false);
        when(electionMock.getElectedModules()).thenReturn(validElectionSet);

        // Case Non-IP, No Dispensations
        when(studentMock.isTZ()).thenReturn(true);
        when(studentMock.isIP()).thenReturn(false);
        when(studentMock.getWpmDispensation()).thenReturn(0);
        assertTrue(validator.validate(electionMock).isValid());

        // Case IP, No Dispensations
        when(studentMock.isIP()).thenReturn(true);
        assertTrue(validator.validate(electionMock).isValid());

        // Case IP, Some Dispensations
        if(!isFistElection) {
            removeNonConsecutiveSubjectModulesFromSet(validElectionSet);
        }
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        assertTrue(validator.validate(electionMock).isValid());

        // Case Non-IP, Some Dispensations
        when(studentMock.isIP()).thenReturn(false);
        assertTrue(validator.validate(electionMock).isValid());

        //===== Returns invalid
        // Case Non-IP, No Dispensations (Not enough selected)
        when(studentMock.getWpmDispensation()).thenReturn(0);
        for (var mode = 1; mode < 3; mode++) {
            if(isFistElection && mode == 2) continue; // interdisciplinary count in first election is zero and valid
            assertInvalidElection(electionMock, validator, mode, isFistElection);
        }

        // Case Non-IP, No Dispensations (Too much selected)
        for (var mode = 3; mode < 5; mode++) {
            assertInvalidElection(electionMock, validator, mode, isFistElection);
        }
    }

    @Override
    @Test
    void testValidConsecutiveModulePairsInElection() {
        var validationSettingMock = mock(ValidationSetting.class);
        when(validationSettingMock.hadAlreadyElectedTwoConsecutiveModules()).thenReturn(false);
        when(validationSettingMock.isSkipConsecutiveModuleCheck()).thenReturn(false);
        when(electionMock.getValidationSetting()).thenReturn(validationSettingMock);

        // first election
        when(studentMock.isSecondElection()).thenReturn(false);
        when(electionMock.getElectedModules()).thenReturn(validElectionSet);
        assertTrue(validator.validConsecutiveModulePairsInElection(electionMock));

        // second election
        when(studentMock.isSecondElection()).thenReturn(true);
        // AI 1
        var m1 = mock(Module.class);
        when(m1.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(1));
        when(m1.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(1));

        // AI 2
        var m2 = mock(Module.class);
        when(m2.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(0));
        when(m2.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(0));

        // FUP
        var m3 = mock(Module.class);
        when(m3.getShortModuleNo()).thenReturn(subjectModulesShort.get(1));

        // PSPP
        var m4 = mock(Module.class);
        when(m4.getShortModuleNo()).thenReturn(MODULE_WV_PSPP);

        // case first and second election has a mixture of consecutive modules
        when(validationSettingMock.isSkipConsecutiveModuleCheck()).thenReturn(true);
        assertTrue(validator.validConsecutiveModulePairsInElection(electionMock));

        // case first election had no consecutive modules
        when(validationSettingMock.isSkipConsecutiveModuleCheck()).thenReturn(false);

        // AI1, AI2, FUP, PSPP
        when(electionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m3, m4));
        assertTrue(validator.validConsecutiveModulePairsInElection(electionMock));

        // AI1, AI2, PSPP
        when(electionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m4));
        assertFalse(validator.validConsecutiveModulePairsInElection(electionMock));

        // case first election had two consecutive modules
        when(validationSettingMock.hadAlreadyElectedTwoConsecutiveModules()).thenReturn(true);

        // AI1, AI2
        when(electionMock.getElectedModules()).thenReturn(Set.of(m1, m2));
        assertTrue(validator.validConsecutiveModulePairsInElection(electionMock));

        // FUP, PSPP
        when(electionMock.getElectedModules()).thenReturn(Set.of(m3, m4));
        assertTrue(validator.validConsecutiveModulePairsInElection(electionMock));

        // PSPP
        when(electionMock.getElectedModules()).thenReturn(Set.of(m4));
        assertFalse(validator.validConsecutiveModulePairsInElection(electionMock));
    }

    @Test
    void testValidInterdisciplinaryElection() {
        // first election
        when(studentMock.isSecondElection()).thenReturn(false);
        var m1 = mock(Module.class);
        var m2 = mock(Module.class);

        var allMocksList = List.of(m1, m2);
        for (Module mock : allMocksList) {
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock.getModuleNo()).thenReturn(interdisciplinaryModules.get(0));
            when(mock.getShortModuleNo()).thenReturn(interdisciplinaryModulesShort.get(0));
        }

        when(electionMock.getElectedModules()).thenReturn(new HashSet<>());
        assertTrue(validator.validInterdisciplinaryElection(electionMock));

        when(electionMock.getElectedModules()).thenReturn(Set.of(m1));
        assertFalse(validator.validInterdisciplinaryElection(electionMock));

        // second election
        when(studentMock.isSecondElection()).thenReturn(true);

        when(electionMock.getElectedModules()).thenReturn(new HashSet<>());
        assertFalse(validator.validInterdisciplinaryElection(electionMock));

        when(electionMock.getElectedModules()).thenReturn(Set.of(m1));
        assertTrue(validator.validInterdisciplinaryElection(electionMock));

        when(electionMock.getElectedModules()).thenReturn(new HashSet<>(allMocksList));
        assertFalse(validator.validInterdisciplinaryElection(electionMock));
    }

    @Test
    void testValidSubjectElection() {
        // first election
        when(studentMock.isSecondElection()).thenReturn(false);
        var m1 = mock(Module.class);
        var m2 = mock(Module.class);
        var m3 = mock(Module.class);
        var m4 = mock(Module.class);
        var m5 = mock(Module.class);
        var m6 = mock(Module.class);
        var m7 = mock(Module.class);

        var allMocksList = new ArrayList<Module>();
        allMocksList.add(m1);
        allMocksList.add(m2);
        allMocksList.add(m3);
        allMocksList.add(m4);
        allMocksList.add(m5);
        allMocksList.add(m6);
        allMocksList.add(m7);

        for (Module mock : allMocksList) {
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock.getModuleNo()).thenReturn(subjectModules.get(0));
            when(mock.getShortModuleNo()).thenReturn(subjectModulesShort.get(0));
        }

        when(electionMock.getElectedModules()).thenReturn(Set.of(m1, m2));
        assertTrue(validator.validSubjectElection(electionMock));

        when(electionMock.getElectedModules()).thenReturn(Set.of(m1));
        assertFalse(validator.validSubjectElection(electionMock));

        when(electionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m3));
        assertFalse(validator.validSubjectElection(electionMock));

        // second election
        when(studentMock.isSecondElection()).thenReturn(true);

        // one too much
        when(electionMock.getElectedModules()).thenReturn(new HashSet<>(allMocksList));
        assertFalse(validator.validSubjectElection(electionMock));

        // exact amount needed
        allMocksList.remove(0);
        when(electionMock.getElectedModules()).thenReturn(new HashSet<>(allMocksList));
        assertTrue(validator.validSubjectElection(electionMock));

        // one missing
        allMocksList.remove(0);
        when(electionMock.getElectedModules()).thenReturn(new HashSet<>(allMocksList));
        assertFalse(validator.validSubjectElection(electionMock));
    }

    @Test
    void testValidContextElection() {
        // first election
        when(studentMock.isTZ()).thenReturn(true);
        when(studentMock.isSecondElection()).thenReturn(false);
        var m1 = mock(Module.class);
        var m2 = mock(Module.class);
        var m3 = mock(Module.class);
        var m4 = mock(Module.class);

        var allMocksList = List.of(m1, m2, m3, m4);
        for (Module mock : allMocksList) {
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_5);
            when(mock.getModuleNo()).thenReturn(contextModules.get(0));
            when(mock.getShortModuleNo()).thenReturn(contextModules.get(0));
        }

        var validationSetting = mock(ValidationSetting.class);
        when(electionMock.getValidationSetting()).thenReturn(validationSetting);

        when(electionMock.getElectedModules()).thenReturn(Set.of());
        assertFalse(validator.validContextElection(electionMock));

        when(electionMock.getElectedModules()).thenReturn(Set.of(m1));
        assertFalse(validator.validContextElection(electionMock));

        when(electionMock.getElectedModules()).thenReturn(Set.of(m1, m2));
        assertTrue(validator.validContextElection(electionMock));

        when(electionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m3));
        assertTrue(validator.validContextElection(electionMock));

        when(electionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m3, m4));
        assertFalse(validator.validContextElection(electionMock));

        // second election
        when(studentMock.isSecondElection()).thenReturn(true);

        when(validationSetting.getElectedContextModulesInFirstElection()).thenReturn(3);
        when(electionMock.getElectedModules()).thenReturn(Set.of());
        assertTrue(validator.validContextElection(electionMock));

        when(validationSetting.getElectedContextModulesInFirstElection()).thenReturn(2);
        assertFalse(validator.validContextElection(electionMock));

        when(electionMock.getElectedModules()).thenReturn(Set.of(m1));
        assertTrue(validator.validContextElection(electionMock));

        when(validationSetting.getElectedContextModulesInFirstElection()).thenReturn(1);
        assertFalse(validator.validContextElection(electionMock));

        when(electionMock.getElectedModules()).thenReturn(Set.of(m1, m2));
        assertTrue(validator.validContextElection(electionMock));

        when(validationSetting.getElectedContextModulesInFirstElection()).thenReturn(0);
        assertFalse(validator.validContextElection(electionMock));

        when(electionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m3));
        assertTrue(validator.validContextElection(electionMock));

        when(electionMock.getElectedModules()).thenReturn(Set.of(m1, m2, m3, m4));
        assertFalse(validator.validContextElection(electionMock));
    }

    @Test
    void testIsCreditSumValid_FirstElectionWithoutDispensation() {
        var mockMap = generateMocks();
        var subjectMocks = mockMap.get(ModuleCategory.SUBJECT_MODULE);
        var interdisciplinaryMocks = mockMap.get(ModuleCategory.INTERDISCIPLINARY_MODULE);
        var contextMocks = mockMap.get(ModuleCategory.CONTEXT_MODULE);

        when(studentMock.isSecondElection()).thenReturn(false);

        var set = new HashSet<Module>();
        set.add(subjectMocks.get(0));
        when(electionMock.getElectedModules()).thenReturn(set);
        assertFalse(validator.isCreditSumValid(electionMock));

        set.add(subjectMocks.get(1));
        assertFalse(validator.isCreditSumValid(electionMock));

        set.add(contextMocks.get(0));
        assertFalse(validator.isCreditSumValid(electionMock));
        set.add(contextMocks.get(1));
        assertTrue(validator.isCreditSumValid(electionMock));
        set.add(contextMocks.get(2));
        assertTrue(validator.isCreditSumValid(electionMock));

        set.add(subjectMocks.get(2));
        assertFalse(validator.isCreditSumValid(electionMock));

        set.remove(subjectMocks.get(2));
        assertTrue(validator.isCreditSumValid(electionMock));

        set.add(contextMocks.get(3));
        assertFalse(validator.isCreditSumValid(electionMock));
    }

    @Test
    void testIsCreditSumValid_FirstElectionWithDispensation() {
        var mockMap = generateMocks();
        var subjectMocks = mockMap.get(ModuleCategory.SUBJECT_MODULE);
        var contextMocks = mockMap.get(ModuleCategory.CONTEXT_MODULE);

        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        var set = new HashSet<Module>();
        set.add(contextMocks.get(0));
        when(electionMock.getElectedModules()).thenReturn(set);
        assertFalse(validator.isCreditSumValid(electionMock));

        set.add(contextMocks.get(1));
        assertFalse(validator.isCreditSumValid(electionMock));

        set.add(contextMocks.get(2));
        assertFalse(validator.isCreditSumValid(electionMock));

        set.add(subjectMocks.get(0));
        assertFalse(validator.isCreditSumValid(electionMock));

        set.add(subjectMocks.get(1));
        assertTrue(validator.isCreditSumValid(electionMock));

        set.add(subjectMocks.get(2));
        assertFalse(validator.isCreditSumValid(electionMock));

        set.remove(subjectMocks.get(2));
        assertTrue(validator.isCreditSumValid(electionMock));

        set.add(contextMocks.get(3));
        assertFalse(validator.isCreditSumValid(electionMock));
    }

    @Test
    void testIsCreditSumValid_SecondElectionWithoutDispensation() {
        var mockMap = generateMocks();
        var subjectMocks = mockMap.get(ModuleCategory.SUBJECT_MODULE);
        var interdisciplinaryMocks = mockMap.get(ModuleCategory.INTERDISCIPLINARY_MODULE);
        var contextMocks = mockMap.get(ModuleCategory.CONTEXT_MODULE);

        when(studentMock.isSecondElection()).thenReturn(true);
        when(studentMock.getWpmDispensation()).thenReturn(0);

        var validationSettingsMock = mock(ValidationSetting.class);
        when(validationSettingsMock.getElectedContextModulesInFirstElection()).thenReturn(2);

        var set = new HashSet<Module>();
        set.add(interdisciplinaryMocks.get(0));
        set.add(subjectMocks.get(2));
        set.add(subjectMocks.get(3));
        set.add(subjectMocks.get(4));
        set.add(subjectMocks.get(5));
        set.add(subjectMocks.get(6));
        set.add(subjectMocks.get(7));

        when(electionMock.getElectedModules()).thenReturn(set);
        when(electionMock.getValidationSetting()).thenReturn(validationSettingsMock);
        assertFalse(validator.isCreditSumValid(electionMock));

        set.add(contextMocks.get(0));
        assertTrue(validator.isCreditSumValid(electionMock));

        set.add(contextMocks.get(1));
        assertFalse(validator.isCreditSumValid(electionMock));

        set.remove(contextMocks.get(1));
        set.add(subjectMocks.get(0));
        assertFalse(validator.isCreditSumValid(electionMock));
    }

    @Test
    void testIsCreditSumValid_SecondElectionWithDispensation() {
        var mockMap = generateMocks();
        var subjectMocks = mockMap.get(ModuleCategory.SUBJECT_MODULE);
        var interdisciplinaryMocks = mockMap.get(ModuleCategory.INTERDISCIPLINARY_MODULE);
        var contextMocks = mockMap.get(ModuleCategory.CONTEXT_MODULE);

        when(studentMock.isSecondElection()).thenReturn(true);
        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);

        var validationSettingsMock = mock(ValidationSetting.class);
        when(validationSettingsMock.getElectedContextModulesInFirstElection()).thenReturn(3);

        var set = new HashSet<Module>();
        set.add(interdisciplinaryMocks.get(0));
        set.add(subjectMocks.get(1));
        set.add(subjectMocks.get(2));
        set.add(subjectMocks.get(3));
        set.add(subjectMocks.get(4));

        when(electionMock.getElectedModules()).thenReturn(set);
        when(electionMock.getValidationSetting()).thenReturn(validationSettingsMock);
        assertTrue(validator.isCreditSumValid(electionMock));

        set.add(contextMocks.get(0));
        assertFalse(validator.isCreditSumValid(electionMock));

        set.remove(contextMocks.get(0));
        assertTrue(validator.isCreditSumValid(electionMock));

        set.add(subjectMocks.get(0));
        assertFalse(validator.isCreditSumValid(electionMock));
    }

    private Map<ModuleCategory, List<Module>> generateMocks() {
        var contextMocks = generateModuleMockList(PartTimeElectionValidator.NUM_CONTEXT_MODULES_FIRST_ELECTION
                                                          + PartTimeElectionValidator.NUM_CONTEXT_MODULES_SECOND_ELECTION
                                                          + 1);

        var interdisciplinaryMocks = generateModuleMockList(PartTimeElectionValidator.NUM_INTERDISCIPLINARY_MODULES_FIRST_ELECTION
                                                                    + PartTimeElectionValidator.NUM_INTERDISCIPLINARY_MODULES_SECOND_ELECTION);

        var subjectMocks = generateModuleMockList(PartTimeElectionValidator.NUM_SUBJECT_MODULES_FIRST_ELECTION
                                                          + PartTimeElectionValidator.NUM_SUBJECT_MODULES_SECOND_ELECTION);

        for (var context: contextMocks) {
            when(context.getCredits()).thenReturn((byte) CREDITS_PER_CONTEXT_MODULE);
        }

        for (var interdisciplinary: interdisciplinaryMocks) {
            when(interdisciplinary.getCredits()).thenReturn((byte) CREDITS_PER_INTERDISCIPLINARY_MODULE);
        }

        for (var subject: subjectMocks) {
            when(subject.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
        }

        return Map.of(ModuleCategory.CONTEXT_MODULE, contextMocks,
                      ModuleCategory.INTERDISCIPLINARY_MODULE, interdisciplinaryMocks,
                      ModuleCategory.SUBJECT_MODULE, subjectMocks);
    }

    /* **************************************************************************************************************
     * Negative tests
     * ************************************************************************************************************** */

    @Test
    void testValidIpElection_Null() {
        when(studentMock.isIP()).thenReturn(true);
        assertTrue(validator.validIpElection(null));
    }

    @Test
    void testValidIpElection_NullElection() {
        when(studentMock.isIP()).thenReturn(true);
        when(electionMock.getElectedModules()).thenReturn(null);
        assertTrue(validator.validIpElection(null));
    }

    @Test
    void testValidIpElection_NullStudent() {
        validator = new PartTimeElectionValidator(null);
        assertTrue(validator.validIpElection(electionMock));
    }

    @Test
    void testValidInterdisciplinaryElection_Null() {
        assertThrows(NullPointerException.class, () -> validator.validInterdisciplinaryElection(null));
    }

    @Test
    void testValidSubjectElection_Null() {
        when(studentMock.getWpmDispensation()).thenReturn(0);
        assertThrows(NullPointerException.class, () -> validator.validSubjectElection(null));
    }

    @Test
    void testValidSubjectElection_NullStudent() {
        validator = new PartTimeElectionValidator(null);
        assertThrows(NullPointerException.class, () -> validator.validSubjectElection(electionMock));
    }

    @Test
    void testValidContextElection_Null() {
        assertThrows(NullPointerException.class, () -> validator.validContextElection(null));
    }

    @Override
    @Test
    void testIsCreditSumValid_NullArgument() {
        assertThrows(NullPointerException.class, () -> validator.isCreditSumValid(null));
    }

    @Override
    @Test
    void testIsCreditSumValid_NullElectionSet() {
        when(electionMock.getElectedModules()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> validator.isCreditSumValid(electionMock));
    }

    @Test
    void testIsCreditSumValid_NullElection() {
        var set = new HashSet<Module>();
        set.add(null);
        when(electionMock.getElectedModules()).thenReturn(set);
        assertThrows(NullPointerException.class, () -> validator.isCreditSumValid(electionMock));
    }

    @Test
    void testIsCreditSumValid_NullStudent() {
        validator = new FullTimeElectionValidator(null);

        when(studentMock.getWpmDispensation()).thenReturn(WPM_DISPENSATION);
        when(electionMock.getElectedModules()).thenReturn(validElectionSet);

        assertThrows(NullPointerException.class, () -> validator.isCreditSumValid(electionMock));
    }

    /* **************************************************************************************************************
     * Helper methods
     * ************************************************************************************************************** */

    Set<Module> generateValidPartTimeElectionSet(boolean isFirstElection) {
        var contextMocks = generateModuleMockList(isFirstElection
                                                         ? PartTimeElectionValidator.NUM_CONTEXT_MODULES_FIRST_ELECTION
                                                         : PartTimeElectionValidator.NUM_CONTEXT_MODULES_SECOND_ELECTION);

        var interdisciplinaryMocks = generateModuleMockList(isFirstElection
                                                                   ? PartTimeElectionValidator.NUM_INTERDISCIPLINARY_MODULES_FIRST_ELECTION
                                                                   : PartTimeElectionValidator.NUM_INTERDISCIPLINARY_MODULES_SECOND_ELECTION);

        var subjectMocks = generateModuleMockList(isFirstElection
                                                         ? PartTimeElectionValidator.NUM_SUBJECT_MODULES_FIRST_ELECTION
                                                         : PartTimeElectionValidator.NUM_SUBJECT_MODULES_SECOND_ELECTION);

        var i = 0;
        for (var context: contextMocks) {
            when(context.getSemester()).thenReturn((i % 3 == 0) ? SEMESTER_6 : SEMESTER_5);
            when(context.getCredits()).thenReturn((byte) CREDITS_PER_CONTEXT_MODULE);
            when(context.getLanguage()).thenReturn(LANGUAGE_ENGLISCH);
            when(context.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_5);
            when(context.getModuleNo()).thenReturn(contextModules.get(i));
            when(context.getShortModuleNo()).thenReturn(contextModulesShort.get(i));
            i++;
        }

        for (var interdisciplinary: interdisciplinaryMocks) {
            when(interdisciplinary.getSemester()).thenReturn(SEMESTER_5);
            when(interdisciplinary.getCredits()).thenReturn((byte) CREDITS_PER_INTERDISCIPLINARY_MODULE);
            when(interdisciplinary.getLanguage()).thenReturn(LANGUAGE_DEUTSCH);
            when(interdisciplinary.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(interdisciplinary.getModuleNo()).thenReturn(interdisciplinaryModules.get(0));
            when(interdisciplinary.getShortModuleNo()).thenReturn(interdisciplinaryModulesShort.get(0));
        }

        i = 0;
        for (var subject: subjectMocks) {
            when(subject.getSemester()).thenReturn(SEMESTER_6);
            when(subject.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            when(subject.getLanguage()).thenReturn(LANGUAGE_ENGLISCH);
            when(subject.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(subject.getModuleNo()).thenReturn(consecutiveSubjectModules.get(i));
            when(subject.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(i));
            i++;
        }

        var mockCounter = 0;
        for (i = 0; i < NUM_NON_CONSECUTIVE_SUBJECT_MODULES; i++) {
            var mock = subjectMocks.get(mockCounter);
            mockCounter++;
            when(mock.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            when(mock.getLanguage()).thenReturn(LANGUAGE_ENGLISCH);
            when(mock.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock.getModuleNo()).thenReturn(subjectModules.get(i));
            when(mock.getShortModuleNo()).thenReturn(subjectModulesShort.get(i));
        }


        mockCounter = 0;
        for (i = NUM_NON_CONSECUTIVE_SUBJECT_MODULES + 1; i < subjectMocks.size(); i += 2) {
            var mock1 = subjectMocks.get(mockCounter);
            mockCounter++;
            when(mock1.getSemester()).thenReturn(SEMESTER_6);
            when(mock1.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            when(mock1.getLanguage()).thenReturn(LANGUAGE_ENGLISCH);
            when(mock1.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock1.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(i - 1));
            when(mock1.getModuleNo()).thenReturn(consecutiveSubjectModules.get(i - 1));
            when(mock1.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(i));

            var mock2 = subjectMocks.get(mockCounter);
            mockCounter++;
            when(mock2.getSemester()).thenReturn(SEMESTER_6);
            when(mock2.getCredits()).thenReturn((byte) CREDITS_PER_SUBJECT_MODULE);
            when(mock2.getLanguage()).thenReturn(LANGUAGE_ENGLISCH);
            when(mock2.getModuleGroup()).thenReturn(ModuleParser.MODULE_GROUP_IT_6);
            when(mock2.getShortModuleNo()).thenReturn(consecutiveSubjectModulesShort.get(i));
            when(mock2.getModuleNo()).thenReturn(consecutiveSubjectModules.get(i));
            when(mock2.getConsecutiveModuleNo()).thenReturn(consecutiveSubjectModules.get(i - 1));
        }

        var set = new HashSet<Module>();
        set.addAll(contextMocks);
        set.addAll(interdisciplinaryMocks);
        set.addAll(subjectMocks);
        return set;
    }

    @Override
    void addModule(Set<Module> set, String moduleNo, Module module, int credits) {
        when(module.getSemester()).thenReturn(ExecutionSemester.SPRING);
        super.addModule(set, moduleNo, module, credits);
    }

    Set<Module> invalidElectionSet(int mode, boolean isFistElection) {
        var set = generateValidPartTimeElectionSet(isFistElection);
        var module = mock(Module.class);
        switch (mode) {
            case 1 -> removeOneModuleByCategory(set, ModuleCategory.SUBJECT_MODULE);
            case 2 -> removeOneModuleByCategory(set, ModuleCategory.INTERDISCIPLINARY_MODULE);
            case 3 -> addModule(set, ModuleCategoryTest.possibleSubjectPrefixes.get(0), module, CREDITS_PER_SUBJECT_MODULE);
            case 4 -> addModule(set, ModuleCategoryTest.INTERDISCIPLINARY_PREFIX_WM, module, CREDITS_PER_INTERDISCIPLINARY_MODULE);
        }
        return set;
    }

    void assertInvalidElection(Election electionMock, ElectionValidator validator, int mode, boolean isFistElection) {
        var invalidElection = invalidElectionSet(mode, isFistElection);
        when(electionMock.getElectedModules()).thenReturn(invalidElection);
        assertFalse(validator.validate(electionMock).isValid());
    }
}
