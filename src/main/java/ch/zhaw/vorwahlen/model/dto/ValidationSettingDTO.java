package ch.zhaw.vorwahlen.model.dto;

import javax.validation.constraints.NotNull;

public record ValidationSettingDTO (@NotNull(message = "{validation.repetent.null}")
                                    boolean isRepetent,
                                    @NotNull(message = "{validation.previous.consecutive.modules.null}")
                                    boolean hadAlreadyElectedTwoConsecutiveModules,
                                    @NotNull(message = "{validation.skip.consecutive.modules.check.null}")
                                    boolean isSkipConsecutiveModuleCheck) {}
