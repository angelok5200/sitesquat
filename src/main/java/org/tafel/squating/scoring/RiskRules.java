package org.tafel.squating.scoring;

import java.util.Map;

import org.tafel.squating.domain.enums.ContentIndicator;

public final class RiskRules {

    private RiskRules() {
    }

    /*
     * Content weights
     */
    public static final int BRAND_MENTIONED_WEIGHT = 15;
    public static final int LOGIN_FORM_WEIGHT = 25;
    public static final int PASSWORD_FIELD_WEIGHT = 25;
    public static final int WALLET_KEYWORDS_WEIGHT = 25;
    public static final int PAYMENT_KEYWORDS_WEIGHT = 20;

    /*
     * Infrastructure / registration weights
     */
    public static final int MX_CONFIGURED_WEIGHT = 10;
    public static final int CERTIFICATE_FOUND_WEIGHT = 10;
    public static final int CERTIFICATE_TRANSPARENCY_WEIGHT = 15;
    public static final int RECENT_REGISTRATION_WEIGHT = 15;

    /*
     * Risk level thresholds
     *
     * 0-29   LOW
     * 30-59  MEDIUM
     * 60-79  HIGH
     * 80+    CRITICAL
     */
    public static final int LOW_MAX_SCORE = 29;
    public static final int MEDIUM_MAX_SCORE = 59;
    public static final int HIGH_MAX_SCORE = 79;

    public static final Map<ContentIndicator, Integer> CONTENT_WEIGHTS = Map.of(
            ContentIndicator.BRAND_MENTIONED,
            BRAND_MENTIONED_WEIGHT,

            ContentIndicator.LOGIN_FORM,
            LOGIN_FORM_WEIGHT,

            ContentIndicator.PASSWORD_FIELD,
            PASSWORD_FIELD_WEIGHT,

            ContentIndicator.WALLET_KEYWORDS,
            WALLET_KEYWORDS_WEIGHT,

            ContentIndicator.PAYMENT_KEYWORDS,
            PAYMENT_KEYWORDS_WEIGHT
    );
}