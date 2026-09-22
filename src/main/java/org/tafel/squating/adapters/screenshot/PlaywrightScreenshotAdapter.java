package org.tafel.squating.adapters.screenshot;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.stereotype.Component;
import org.tafel.squating.ports.outbound.ScreenshotService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Component
public class PlaywrightScreenshotAdapter implements ScreenshotService {

    private static final int NAVIGATION_TIMEOUT_MILLIS = 10_000;

    @Override
    public String capture(
            String domain,
            String outputDirectory
    ) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException(
                    "domain must not be blank"
            );
        }

        if (outputDirectory == null || outputDirectory.isBlank()) {
            throw new IllegalArgumentException(
                    "outputDirectory must not be blank"
            );
        }

        String normalizedDomain = normalizeDomain(domain);

        Path directory = Path.of(outputDirectory);

        try {
            Files.createDirectories(directory);

            String fileName =
                    sanitizeFileName(normalizedDomain)
                            + "-"
                            + Instant.now().toEpochMilli()
                            + ".png";

            Path screenshotPath = directory.resolve(fileName);

            byte[] screenshotBytes;

            try (Playwright playwright = Playwright.create();
                 Browser browser = playwright.chromium().launch(
                         new BrowserType.LaunchOptions()
                                 .setHeadless(true)
                 )) {

                Page page = browser.newPage();

                page.setDefaultNavigationTimeout(
                        NAVIGATION_TIMEOUT_MILLIS
                );

                page.navigate(
                        "https://" + normalizedDomain,
                        new Page.NavigateOptions()
                                .setWaitUntil(
                                        WaitUntilState.DOMCONTENTLOADED
                                )
                );

                screenshotBytes = page.screenshot(
                        new Page.ScreenshotOptions()
                                .setPath(screenshotPath)
                                .setFullPage(true)
                );
            }

            return calculateSha256(screenshotBytes);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to save screenshot for "
                            + normalizedDomain,
                    e
            );
        }
    }

    private String normalizeDomain(String domain) {
        String normalized = domain.trim().toLowerCase();

        if (normalized.startsWith("https://")) {
            normalized = normalized.substring(8);
        } else if (normalized.startsWith("http://")) {
            normalized = normalized.substring(7);
        }

        int slashIndex = normalized.indexOf('/');

        if (slashIndex >= 0) {
            normalized = normalized.substring(
                    0,
                    slashIndex
            );
        }

        return normalized;
    }

    private String sanitizeFileName(String domain) {
        return domain.replaceAll(
                "[^a-zA-Z0-9.-]",
                "_"
        );
    }

    private String calculateSha256(byte[] data) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(data);

            StringBuilder result =
                    new StringBuilder();

            for (byte value : hash) {
                if (!result.isEmpty()) {
                    result.append(':');
                }

                result.append(
                        String.format("%02X", value)
                );
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    e
            );
        }
    }
}
