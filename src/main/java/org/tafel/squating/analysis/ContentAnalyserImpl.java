package org.tafel.squating.analysis;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.tafel.squating.ports.outbound.ContentAnalyser;

@Component
public class ContentAnalyserImpl implements ContentAnalyser {

    @Override 
    public ContentAnalysis analyse(String html, String brand) {
        // Implementation of content analysis logic goes here
        if (html == null || html.isBlank()) {
            return new ContentAnalysis(false, false, false, false, false, Set.of());
        }
    
        String content = html.toLowerCase(Locale.ROOT);
        String normalizedBrand = brand==null ? "" : brand.toLowerCase(Locale.ROOT);

        Set<ContentIndicator> indicators = new HashSet<>();
        boolean brandMentioned = !normalizedBrand.isBlank() && content.contains(normalizedBrand);
        if (brandMentioned) {
            indicators.add(ContentIndicator.BRAND_MENTIONED);
        }
        boolean loginFormDetected = content.contains("<form") && containsAnyOf(content, ContentRules.LOGIN_KEYWORDS);
        if (loginFormDetected) {
            indicators.add(ContentIndicator.LOGIN_FORM);
        }
        boolean passwordFieldDetected = content.contains("type=\"password\"")||content.contains("type='password'");
        if (passwordFieldDetected) {
            indicators.add(ContentIndicator.PASSWORD_FIELD);
        }
        boolean walletKeywordsDetected = containsAnyOf(content, ContentRules.WALLET_KEYWORDS);
        if (walletKeywordsDetected) {
            indicators.add(ContentIndicator.WALLET_KEYWORDS);
        }
        boolean paymentKeywordsDetected = containsAnyOf(content, ContentRules.PAYMENT_KEYWORDS);
        if (paymentKeywordsDetected) {
            indicators.add(ContentIndicator.PAYMENT_KEYWORDS);
        }
        // Continue implementing other detection logic...
        return new ContentAnalysis(brandMentioned, loginFormDetected, passwordFieldDetected, walletKeywordsDetected, paymentKeywordsDetected, indicators);
    }
    private boolean containsAnyOf(String content, Set<String> keywords) {
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}

