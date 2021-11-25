package ch.zhaw.vorwahlen.model.modules;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter @Setter
@NoArgsConstructor
public class ValidationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "tinyint(1) default 0")
    private boolean isRepetent;

    @Getter(AccessLevel.NONE)
    @Column(columnDefinition = "tinyint(1) default 0")
    private boolean alreadyElectedTwoConsecutiveModules;

    @Column(columnDefinition = "tinyint(1) default 0")
    private boolean isSkipConsecutiveModuleCheck;

    public boolean hadAlreadyElectedTwoConsecutiveModules() {
        return alreadyElectedTwoConsecutiveModules;
    }

}