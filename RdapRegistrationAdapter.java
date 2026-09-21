```java
package org.tafel.squating.application;

import org.springframework.stereotype.Service;
import org.tafel.squating.domain.enums.AlertType;
import org.tafel.squating.domain.enums.RiskLevel;
import org.tafel.squating.domain.model.Alert;
import org.tafel.squating.domain.model.RiskAssessment;

import java.util.UUID;

@Service
public class AlertDecisionService {

    public Alert decide(
            UUID candidateId,
            RiskAssessment previousAssessment,
            RiskAssessment currentAssessment
    ) {
        if (candidateId == null) {
            throw new IllegalArgumentException(
                    "candidateId must not be null"
            );
        }

        if (currentAssessment == null) {
            throw new IllegalArgumentException(
                    "currentAssessment must not be null"
            );
        }

        AlertType alertType = determineAlertType(
                previousAssessment,
                currentAssessment
        );

        if (alertType == null) {
            return null;
        }

        return new Alert(
                null,
                candidateId,
                alertType,
                currentAssessment.getRiskLevel(),
                currentAssessment.getTotalScore()
        );
    }

    private AlertType determineAlertType(
            RiskAssessment previousAssessment,
            RiskAssessment currentAssessment
    ) {
        if (previousAssessment == null) {
            return AlertType.FIRST_DISCOVERY;
        }

        RiskLevel previousLevel =
                previousAssessment.getRiskLevel();

        RiskLevel currentLevel =
                currentAssessment.getRiskLevel();

        if (previousLevel == RiskLevel.MEDIUM
                && currentLevel == RiskLevel.HIGH) {
            return AlertType.RISK_ESCALATED;
        }

        if (previousLevel == RiskLevel.HIGH
                && currentLevel == RiskLevel.CRITICAL) {
            return AlertType.RISK_ESCALATED;
        }

        return null;
    }
}
```

**Важно:** здесь я не добавляю `PAYMENT_FIELDS_APPEARED` или другие новые enum-значения вслепую. Если твоего `AlertType` старого формата недостаточно, исправим это отдельно после просмотра фактического enum.
