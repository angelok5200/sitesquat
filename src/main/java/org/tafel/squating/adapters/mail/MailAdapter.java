package org.tafel.squating.adapters.mail;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.value.MailSnapshot;
import org.tafel.squating.ports.outbound.MailInspector;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.util.ArrayList;
import java.util.List;

@Component
public class MailAdapter implements MailInspector {

    @Override
    public MailSnapshot inspect(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException(
                "domain must not be blank"
            );
        }

        String normalizedDomain =
            normalizeDomain(domain);

        List<String> mxRecords =
            lookupMxRecords(normalizedDomain);

        return new MailSnapshot(
            mxRecords,
            !mxRecords.isEmpty()
        );
    }

    private List<String> lookupMxRecords(
        String domain
    ) {
        try {
            Lookup lookup =
                new Lookup(domain, Type.MX);

            Record[] records =
                lookup.run();

            if (records == null) {
                return List.of();
            }

            List<String> result =
                new ArrayList<>();

            for (Record record : records) {
                if (!(record instanceof MXRecord mxRecord)) {
                    continue;
                }

                result.add(
                    mxRecord.getPriority()
                        + " "
                        + mxRecord.getTarget()
                );
            }

            return List.copyOf(result);

        } catch (Exception e) {
            return List.of();
        }
    }

    private String normalizeDomain(String domain) {
        String normalized =
            domain.trim().toLowerCase();

        if (normalized.endsWith(".")) {
            normalized =
                normalized.substring(
                    0,
                    normalized.length() - 1
                );
        }

        return normalized;
    }
}