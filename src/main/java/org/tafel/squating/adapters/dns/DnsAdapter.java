package org.tafel.squating.adapters.dns;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.value.DnsSnapshot;
import org.tafel.squating.ports.outbound.DnsInspector;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

@Component
public class DnsAdapter implements DnsInspector {

    @Override
    public DnsSnapshot inspect(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain must not be blank");
        }

        String normalizedDomain = normalizeDomain(domain);

        return new DnsSnapshot(
                lookupA(normalizedDomain),
                lookupAaaa(normalizedDomain),
                lookupTxt(normalizedDomain),
                lookupMx(normalizedDomain),
                lookupNs(normalizedDomain),
                lookupCname(normalizedDomain)
        );
    }

    private List<String> lookupA(String domain) {
        return lookup(domain, Type.A, ARecord.class)
                .stream()
                .map(record -> ((ARecord) record).getAddress().getHostAddress())
                .toList();
    }

    private List<String> lookupAaaa(String domain) {
        return lookup(domain, Type.AAAA, AAAARecord.class)
                .stream()
                .map(record -> ((AAAARecord) record).getAddress().getHostAddress())
                .toList();
    }

    private List<String> lookupTxt(String domain) {
        return lookup(domain, Type.TXT, TXTRecord.class)
                .stream()
                .map(record -> ((TXTRecord) record).getStrings())
                .flatMap(List::stream)
                .toList();
    }

    private List<String> lookupMx(String domain) {
        return lookup(domain, Type.MX, MXRecord.class)
                .stream()
                .map(record -> ((MXRecord) record).getTarget().toString())
                .map(this::removeTrailingDot)
                .toList();
    }

    private List<String> lookupNs(String domain) {
        return lookup(domain, Type.NS, NSRecord.class)
                .stream()
                .map(record -> ((NSRecord) record).getTarget().toString())
                .map(this::removeTrailingDot)
                .toList();
    }

    private List<String> lookupCname(String domain) {
        return lookup(domain, Type.CNAME, CNAMERecord.class)
                .stream()
                .map(record -> ((CNAMERecord) record).getTarget().toString())
                .map(this::removeTrailingDot)
                .toList();
    }

    private <T extends Record> List<T> lookup(
            String domain,
            int type,
            Class<T> recordType
    ) {
        try {
            Lookup lookup = new Lookup(domain, type);
            Record[] records = lookup.run();

            if (records == null) {
                return List.of();
            }

            List<T> result = new ArrayList<>();

            for (Record record : records) {
                if (recordType.isInstance(record)) {
                    result.add(recordType.cast(record));
                }
            }

            return result;
        } catch (TextParseException | IllegalArgumentException e) {
            return List.of();
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
