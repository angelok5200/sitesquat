package org.tafel.squating.ports.outbound;

import org.tafel.squating.analysis.SimilarityResult;

public interface SimilarityAnalyzer {

    SimilarityResult analyze(
            String candidateText,
            String referenceText,
            byte[] candidateLogo,
            byte[] referenceLogo,
            byte[] candidateFavicon,
            byte[] referenceFavicon
    );
}