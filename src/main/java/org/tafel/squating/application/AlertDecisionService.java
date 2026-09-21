package org.tafel.squating.application;

import org.springframework.stereotype.Service;
import org.tafel.squating.domain.enums.AlertType;
import org.tafel.squating.domain.enums.RiskLevel;
import org.tafel.squating.domain.model.Alert;
import org.tafel.squating.domain.model.DomainObservation;
import org.tafel.squating.domain.model.RiskAssessment;

import java.time.Instant;
import java.util.UUID;

@Service
public class AlertDecisionService {

    public Alert decide(
            DomainObservation previousObservation,
            DomainObservation currentObservation,
            RiskAssessment previousAssessment,
            RiskAssessment currentAssessment,
            boolean loginFormDetected
    ) {
        if (currentObservation == null) {
            throw new IllegalArgumentException(
                    "currentObservation must not be null"
            );
        }

        if (currentAssessment == null) {
            throw new IllegalArgumentException(
                    "currentAssessment must not be null"
            );
        }

        UUID candidateId = currentObservation.getCandidateId();

        if (candidateId == null) {
            throw new IllegalArgumentException(
                    "candidateId must not be null"
            );
        }

        AlertType alertType = determineAlertType(
                previousObservation,
                currentObservation,
                previousAssessment,
                currentAssessment,
                loginFormDetected
        );

        if (alertType == null) {
            return null;
        }

        return new Alert(
                null,
                candidateId,
                currentObservation.getId(),
                currentAssessment.getId(),
                alertType,
                buildTitle(alertType),
                buildMessage(
                        alertType,
                        currentObservation,
                        currentAssessment
                ),
                Instant.now(),
                null,
                null
        );
    }

    private AlertType determineAlertType(
            DomainObservation previousObservation,
            DomainObservation currentObservation,
            RiskAssessment previousAssessment,
            RiskAssessment currentAssessment,
            boolean loginFormDetected
    ) {
        /*
         * First discovery has the highest priority.
         */
        if (previousObservation == null) {
            return AlertType.NEW_CANDIDATE;
        }

        /*
         * Risk escalation.
         */
        if (isRiskEscalation(
                previousAssessment,
                currentAssessment
        )) {
            return AlertType.RISK_ESCALATION;
        }

        /*
         * New MX configuration.
         */
        if (mxAppeared(
                previousObservation,
                currentObservation
        )) {
            return AlertType.MX_APPEARED;
        }

        /*
         * New certificate.
         */
        if (certificateAppeared(
                previousObservation,
                currentObservation
        )) {
            return AlertType.CERTIFICATE_APPEARED;
        }

        /*
         * Login form appeared.
         *
         * The observation model does not store content indicators,
         * therefore this signal is supplied by Content Analysis.
         */
        if (loginFormDetected) {
            return AlertType.LOGIN_FORM_APPEARED;
        }

        /*
         * Content changed.
         */
        if (contentChanged(
                previousObservation,
                currentObservation
        )) {
            return AlertType.CONTENT_CHANGED;
        }

        return null;
    }

    private boolean isRiskEscalation(
            RiskAssessment previousAssessment,
            RiskAssessment currentAssessment
    ) {
        if (previousAssessment == null
                || previousAssessment.getRiskLevel() == null
                || currentAssessment.getRiskLevel() == null) {
            return false;
        }

        RiskLevel previousLevel =
                previousAssessment.getRiskLevel();

        RiskLevel currentLevel =
                currentAssessment.getRiskLevel();

        return (previousLevel == RiskLevel.MEDIUM
                    && currentLevel == RiskLevel.HIGH)
                || (previousLevel == RiskLevel.HIGH
                    && currentLevel == RiskLevel.CRITICAL);
    }

    private boolean mxAppeared(
            DomainObservation previousObservation,
            DomainObservation currentObservation
    ) {
        if (currentObservation.getMail() == null
                || !currentObservation.getMail().mxConfigured()) {
            return false;
        }

        if (previousObservation == null
                || previousObservation.getMail() == null) {
            return true;
        }

        return !previousObservation.getMail().mxConfigured();
    }

    private boolean certificateAppeared(
            DomainObservation previousObservation,
            DomainObservation currentObservation
    ) {
        boolean currentHasCertificate =
                currentObservation.getTls() != null;

        if (!currentHasCertificate) {
            return false;
        }

        return previousObservation == null
                || previousObservation.getTls() == null;
    }

    private boolean contentChanged(
            DomainObservation previousObservation,
            DomainObservation currentObservation
    ) {
        String previousHash =
                previousObservation.getContentHash();

        String currentHash =
                currentObservation.getContentHash();

        if (previousHash == null || currentHash == null) {
            return false;
        }

        return !previousHash.equals(currentHash);
    }

    private String buildTitle(AlertType alertType) {
        return switch (alertType) {
            case NEW_CANDIDATE ->
                    "New suspicious candidate detected";

            case RISK_ESCALATION ->
                    "Risk level escalated";

            case CONTENT_CHANGED ->
                    "Candidate content changed";

            case MX_APPEARED ->
                    "Mail configuration appeared";

            case LOGIN_FORM_APPEARED ->
                    "Login form detected";

            case CERTIFICATE_APPEARED ->
                    "TLS certificate appeared";
        };
    }

    private String buildMessage(
            AlertType alertType,
            DomainObservation observation,
            RiskAssessment assessment
    ) {
        return String.format(
                "Alert type: %s. Candidate: %s. Risk level: %s. Score: %d.",
                alertType,
                observation.getCandidateId(),
                assessment.getRiskLevel(),
                assessment.getTotalScore()
        );
    }
}