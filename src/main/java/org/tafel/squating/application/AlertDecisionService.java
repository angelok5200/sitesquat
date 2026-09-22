package org.tafel.squating.application;

import java.util.ArrayList;
import java.util.List;

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

    public List<Alert> decide(
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

        List<Alert> alerts = new ArrayList<>();

        if (previousObservation == null) {
            alerts.add(createAlert(
                    AlertType.NEW_CANDIDATE,
                    candidate,
                    currentAssessment
            ));

            return alerts;
        }

        if (isRiskEscalation(
                previousAssessment,
                currentAssessment
        )) {
            alerts.add(createAlert(
                    AlertType.RISK_ESCALATION,
                    candidate,
                    currentAssessment
            ));
        }

        if (mxAppeared(
                previousObservation,
                currentObservation
        )) {
            alerts.add(createAlert(
                    AlertType.MX_APPEARED,
                    candidate,
                    currentAssessment
            ));
        }

        if (certificateAppeared(
                previousObservation,
                currentObservation
        )) {
            alerts.add(createAlert(
                    AlertType.CERTIFICATE_APPEARED,
                    candidate,
                    currentAssessment
            ));
        }

        if (certificateReplaced(
                previousObservation,
                currentObservation
        )) {
            alerts.add(createAlert(
                    AlertType.CERTIFICATE_REPLACED,
                    candidate,
                    currentAssessment
            ));
        }

        if (loginFormAppeared(
                previousObservation,
                currentObservation
        )) {
            alerts.add(createAlert(
                    AlertType.LOGIN_FORM_APPEARED,
                    candidate,
                    currentAssessment
            ));
        }

        if (contentChanged(
                previousObservation,
                currentObservation
        )) {
            alerts.add(createAlert(
                    AlertType.CONTENT_CHANGED,
                    candidate,
                    currentAssessment
            ));
        }

        return alerts;
    }

    private Alert createAlert(
            AlertType alertType,
            CandidateDomain candidate,
            RiskAssessment assessment
    ) {
        return new Alert(
                null,
                candidate.getBrandId(),
                candidate.getId(),
                candidate.getDomain(),
                alertType,
                assessment.getRiskLevel(),
                buildTitle(alertType),
                buildMessage(
                        alertType,
                        candidate,
                        assessment
                )
        );
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
                getCertificateFingerprint(currentObservation);

        if (currentFingerprint == null) {
            return false;
        }

        String previousFingerprint =
                getCertificateFingerprint(previousObservation);

        return previousFingerprint == null;
    }

    private boolean certificateReplaced(
            DomainObservation previousObservation,
            DomainObservation currentObservation
    ) {
        String previousFingerprint =
                getCertificateFingerprint(previousObservation);

        String currentFingerprint =
                getCertificateFingerprint(currentObservation);

        if (previousFingerprint == null
                || currentFingerprint == null) {
            return false;
        }

        return !previousFingerprint.equals(currentFingerprint);
    }

    private String getCertificateFingerprint(
            DomainObservation observation
    ) {
        if (observation == null
                || observation.getTls() == null) {
            return null;
        }

        String fingerprint =
                observation.getTls().certificateFingerprint();

        if (fingerprint == null || fingerprint.isBlank()) {
            return null;
        }

        return fingerprint;
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

            case CERTIFICATE_REPLACED ->
                    "TLS certificate replaced";
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