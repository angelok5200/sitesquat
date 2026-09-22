package org.tafel.squating.application;

import org.springframework.stereotype.Service;
import org.tafel.squating.domain.enums.AlertType;
import org.tafel.squating.domain.enums.ContentIndicator;
import org.tafel.squating.domain.enums.RiskLevel;
import org.tafel.squating.domain.model.Alert;
import org.tafel.squating.domain.model.CandidateDomain;
import org.tafel.squating.domain.model.DomainObservation;
import org.tafel.squating.domain.model.RiskAssessment;

@Service
public class AlertDecisionService {

    public Alert decide(
            CandidateDomain candidate,
            DomainObservation previousObservation,
            DomainObservation currentObservation,
            RiskAssessment previousAssessment,
            RiskAssessment currentAssessment
    ) {
        if (candidate == null) {
            throw new IllegalArgumentException(
                    "candidate must not be null"
            );
        }

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

        if (!candidate.getId().equals(
                currentObservation.getCandidateId())) {
            throw new IllegalArgumentException(
                    "Candidate id does not match observation"
            );
        }

        if (!candidate.getId().equals(
                currentAssessment.getCandidateId())) {
            throw new IllegalArgumentException(
                    "Candidate id does not match assessment"
            );
        }

        AlertType alertType = determineAlertType(
                previousObservation,
                currentObservation,
                previousAssessment,
                currentAssessment
        );

        if (alertType == null) {
            return null;
        }

        return new Alert(
                null,
                candidate.getBrandId(),
                candidate.getId(),
                candidate.getDomain(),
                alertType,
                currentAssessment.getRiskLevel(),
                buildTitle(alertType),
                buildMessage(
                        alertType,
                        candidate,
                        currentAssessment
                )
        );
    }

    private AlertType determineAlertType(
            DomainObservation previousObservation,
            DomainObservation currentObservation,
            RiskAssessment previousAssessment,
            RiskAssessment currentAssessment
    ) {
        if (previousObservation == null) {
            return AlertType.NEW_CANDIDATE;
        }

        if (isRiskEscalation(
                previousAssessment,
                currentAssessment
        )) {
            return AlertType.RISK_ESCALATION;
        }

        if (mxAppeared(
                previousObservation,
                currentObservation
        )) {
            return AlertType.MX_APPEARED;
        }

        if (certificateAppeared(
                previousObservation,
                currentObservation
        )) {
            return AlertType.CERTIFICATE_APPEARED;
        }

        if (loginFormAppeared(
                previousObservation,
                currentObservation
        )) {
            return AlertType.LOGIN_FORM_APPEARED;
        }

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

        if (previousObservation.getMail() == null) {
            return true;
        }

        return !previousObservation.getMail().mxConfigured();
    }

    private boolean certificateAppeared(
            DomainObservation previousObservation,
            DomainObservation currentObservation
    ) {
        String currentFingerprint =
                currentObservation.getTls() != null
                        ? currentObservation.getTls()
                            .certificateFingerprint()
                        : null;

        if (currentFingerprint == null
                || currentFingerprint.isBlank()) {
            return false;
        }

        if (previousObservation.getTls() == null) {
            return true;
        }

        String previousFingerprint =
                previousObservation.getTls()
                        .certificateFingerprint();

        if (previousFingerprint == null
                || previousFingerprint.isBlank()) {
            return true;
        }

        return !previousFingerprint.equals(currentFingerprint);
    }

    private boolean loginFormAppeared(
            DomainObservation previousObservation,
            DomainObservation currentObservation
    ) {
        boolean previousLogin =
                previousObservation.getContentIndicators()
                        .contains(ContentIndicator.LOGIN_FORM);

        boolean currentLogin =
                currentObservation.getContentIndicators()
                        .contains(ContentIndicator.LOGIN_FORM);

        return !previousLogin && currentLogin;
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
            CandidateDomain candidate,
            RiskAssessment assessment
    ) {
        return String.format(
                "Alert type: %s. Candidate: %s. Risk level: %s. Score: %d.",
                alertType,
                candidate.getDomain(),
                assessment.getRiskLevel(),
                assessment.getTotalScore()
        );
    }
}