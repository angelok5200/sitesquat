package org.tafel.squating.analysis;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.tafel.squating.ports.outbound.SimilarityAnalyzer;

@Component
public class SimilarityAnalyserImpl implements SimilarityAnalyzer {

    @Override
    public SimilarityResult analyze(
            String candidateText,
            String referenceText,
            byte[] candidateLogo,
            byte[] referenceLogo,
            byte[] candidateFavicon,
            byte[] referenceFavicon
    ) {
        double textSimilarity =
                calculateTextSimilarity(candidateText, referenceText);

        double logoSimilarity =
                calculateLogoSimilarity(candidateLogo, referenceLogo);

        boolean faviconMatch =
                calculateFaviconMatch(candidateFavicon, referenceFavicon);

        return new SimilarityResult(
                textSimilarity,
                logoSimilarity,
                faviconMatch
        );
    }

    private double calculateTextSimilarity(
            String candidate,
            String reference
    ) {
        Set<String> candidateWords = tokenize(candidate);
        Set<String> referenceWords = tokenize(reference);

        if (candidateWords.isEmpty() && referenceWords.isEmpty()) {
            return 1.0;
        }

        if (candidateWords.isEmpty() || referenceWords.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection =
                new HashSet<>(candidateWords);
        intersection.retainAll(referenceWords);

        Set<String> union =
                new HashSet<>(candidateWords);
        union.addAll(referenceWords);

        return (double) intersection.size() / union.size();
    }

    private Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }

        String normalized = text
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim();

        if (normalized.isBlank()) {
            return Set.of();
        }

        return new HashSet<>(
                Arrays.asList(normalized.split("\\s+"))
        );
    }

    private double calculateLogoSimilarity(
            byte[] candidateLogo,
            byte[] referenceLogo
    ) {
        if (candidateLogo == null
                || referenceLogo == null
                || candidateLogo.length == 0
                || referenceLogo.length == 0) {
            return 0.0;
        }

        /*
         * The actual logo comparison is intentionally represented
         * as a byte-level similarity here.
         *
         * Image pHash/Hamming-distance comparison belongs in the
         * image-processing adapter and can replace this calculation
         * without changing SimilarityResult.
         */
        int commonLength =
                Math.min(candidateLogo.length, referenceLogo.length);

        if (commonLength == 0) {
            return 0.0;
        }

        int equalBytes = 0;

        for (int i = 0; i < commonLength; i++) {
            if (candidateLogo[i] == referenceLogo[i]) {
                equalBytes++;
            }
        }

        double lengthPenalty =
                (double) commonLength
                        / Math.max(candidateLogo.length, referenceLogo.length);

        return ((double) equalBytes / commonLength) * lengthPenalty;
    }

    private boolean calculateFaviconMatch(
            byte[] candidateFavicon,
            byte[] referenceFavicon
    ) {
        if (candidateFavicon == null
                || referenceFavicon == null
                || candidateFavicon.length == 0
                || referenceFavicon.length == 0) {
            return false;
        }

        byte[] candidateHash = sha256(candidateFavicon);
        byte[] referenceHash = sha256(referenceFavicon);

        return Arrays.equals(candidateHash, referenceHash);
    }

    private byte[] sha256(byte[] data) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    e
            );
        }
    }
}