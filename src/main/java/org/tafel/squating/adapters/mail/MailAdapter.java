package org.tafel.squating.adapters.mail;

import java.util.List;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.value.MailSnapshot;
import org.tafel.squating.ports.outbound.MailInspector;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

@Component
public class MailAdapter implements MailInspector {

    @Override
    public MailSnapshot inspect(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain must not be blank");
        }

        String normalizedDomain = normalizeDomain(domain);

        try {
            Lookup lookup = new Lookup(normalizedDomain, Type.MX);
            Record[] records = lookup.run();

            if (records == null || records.length == 0) {
                return new MailSnapshot(List.of(), false);
            }

            List<String> mxRecords = List.of(records)
                    .stream()
                    .filter(MXRecord.class::isInstance)
                    .map(MXRecord.class::cast)
                    .map(record -> record.getTarget().toString())
                    .map(this::removeTrailingDot)
                    .toList();

            return new MailSnapshot(
                    mxRecords,
                    !mxRecords.isEmpty()
            );

        } catch (Exception e) {
            return new MailSnapshot(List.of(), false);
        }
    }

    private String normalizeDomain(String domain) {
        String normalized = domain.trim().toLowerCase();

        if (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private String removeTrailingDot(String value) {
        if (value == null) {
            return null;
        }

        return value.endsWith(".")
                ? value.substring(0, value.length() - 1)
                : value;
    }
}