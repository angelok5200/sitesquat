package org.tafel.squating.analysis;

public record SimilarityResult(
    double textSimilarity,
    double logoSimilarity,
    boolean faviconMatch
) {
    public SimilarityResult {
        if (textSimilarity < 0 || textSimilarity > 1) {
            throw new IllegalArgumentException("Text similarity must be between 0 and 1");
        }
        if (logoSimilarity < 0 || logoSimilarity > 1) {
            throw new IllegalArgumentException("Logo similarity must be between 0 and 1");
        }
    }
}
