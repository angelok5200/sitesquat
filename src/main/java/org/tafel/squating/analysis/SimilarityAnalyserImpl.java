package org.tafel.squating.analysis;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class SimilarityAnalyserImpl {
    public SimilarityResult analyze(String candidateText, String brandText) {
        // Implement the logic to analyze similarity between candidateDomain and brand
        // This is a placeholder implementation
        if (candidateText == null || brandText == null || candidateText.isEmpty() || brandText.isEmpty()) {
            return  new SimilarityResult(0.0, 0.0, false);
        }
        double textSimilarity = calculateTextSimilarity(candidateText, brandText);
         // Placeholder for actual text similarity calculation
        
        return new SimilarityResult(textSimilarity, 0.0, false);
    }

    private double calculateTextSimilarity(String candidateText, String brandText) {
        // Implement the logic to calculate text similarity
        // This is a placeholder implementation
        Set<String> candidateTokens = tokenize(candidateText);
        Set<String> brandTokens = tokenize(brandText);
        if (candidateTokens.isEmpty() || brandTokens.isEmpty()) {
            return 0.0; 
        }
        Set<String> intersection = candidateTokens.stream().filter(brandTokens::contains).collect(Collectors.toSet());
        Set<String> union = Set.copyOf(java.util.stream.Stream.concat(candidateTokens.stream(), brandTokens.stream()).collect(Collectors.toSet()));
        return (double) intersection.size() / union.size();
    }

    private Set<String> tokenize(String text){
        return Arrays.stream(
            text.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "").trim().split("\\s+")
        ).filter(token -> !token.isBlank()).collect(Collectors.toSet());
    }
}
