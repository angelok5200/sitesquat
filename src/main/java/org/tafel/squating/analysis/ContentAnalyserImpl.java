package org.tafel.squating.analysis;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.enums.ContentIndicator;
import org.tafel.squating.ports.outbound.ContentAnalyser;

@Component
public class ContentAnalyserImpl implements ContentAnalyser {

    private static final Pattern PASSWORD_FIELD =
            Pattern.compile(
                    "<input[^>]*type\\s*=\\s*[\"']password[\"'][^>]*>",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern LOGIN_FORM =
            Pattern.compile(
                    "<form[^>]*>.*?(login|signin|sign-in|sign in|log in).*?</form>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );

    @Override
    public ContentAnalysis analyse(String html, String brand) {
        if (html == null || html.isBlank()) {
            return new ContentAnalysis(
                    false,
                    false,
                    false,
                    false,
                    false,
                    Set.of()
            );
        }

        String normalizedHtml = html.toLowerCase(Locale.ROOT);
        String visibleText = extractVisibleText(html);

        boolean brandMentioned =
                brand != null
                        && !brand.isBlank()
                        && visibleText.contains(
                                brand.toLowerCase(Locale.ROOT)
                        );

        boolean loginFormDetected =
                LOGIN_FORM.matcher(normalizedHtml).find()
                        || containsKeyword(
                                visibleText,
                                ContentRules.LOGIN_KEYWORDS
                        );

        boolean passwordFieldDetected =
                PASSWORD_FIELD.matcher(normalizedHtml).find();

        boolean walletKeywordsDetected =
                containsKeyword(
                        visibleText,
                        ContentRules.WALLET_KEYWORDS
                );

        boolean paymentKeywordsDetected =
                containsKeyword(
                        visibleText,
                        ContentRules.PAYMENT_KEYWORDS
                );

        EnumSet<ContentIndicator> indicators =
                EnumSet.noneOf(ContentIndicator.class);

        if (brandMentioned) {
            indicators.add(ContentIndicator.BRAND_MENTIONED);
        }

        if (loginFormDetected) {
            indicators.add(ContentIndicator.LOGIN_FORM);
        }

        if (passwordFieldDetected) {
            indicators.add(ContentIndicator.PASSWORD_FIELD);
        }

        if (walletKeywordsDetected) {
            indicators.add(ContentIndicator.WALLET_KEYWORDS);
        }

        if (paymentKeywordsDetected) {
            indicators.add(ContentIndicator.PAYMENT_KEYWORDS);
        }

        return new ContentAnalysis(
                brandMentioned,
                loginFormDetected,
                passwordFieldDetected,
                walletKeywordsDetected,
                paymentKeywordsDetected,
                indicators
        );
    }

    private boolean containsKeyword(
            String text,
            Set<String> keywords
    ) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private String extractVisibleText(String html) {
        return html
                .replaceAll(
                        "(?is)<script[^>]*>.*?</script>",
                        " "
                )
                .replaceAll(
                        "(?is)<style[^>]*>.*?</style>",
                        " "
                )
                .replaceAll(
                        "(?is)<noscript[^>]*>.*?</noscript>",
                        " "
                )
                .replaceAll(
                        "(?is)<[^>]+>",
                        " "
                )
                .replaceAll(
                        "&nbsp;",
                        " "
                )
                .replaceAll(
                        "&amp;",
                        "&"
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}