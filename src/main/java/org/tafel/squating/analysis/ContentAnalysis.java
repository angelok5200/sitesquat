package org.tafel.squating.analysis;

import java.util.Set;

public record ContentAnalysis (
     boolean brandMentioned,
     boolean loginFormDetected,
     boolean passwordFieldDetected,
     boolean walletKeywordsDetected,
     boolean paymentKeywordsDetected,
     Set<ContentIndicator> matchedIndicators
){
    public ContentAnalysis {
        matchedIndicators = matchedIndicators != null ? Set.copyOf(matchedIndicators) : Set.of();
    }
}
