package org.tafel.squating.adapters.dns;


import org.springframework.stereotype.Component;
import org.tafel.squating.domain.value.DnsSnapshot;
import org.tafel.squating.ports.outbound.DnsInspector;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

import java.util.ArrayList;
import java.util.List;

@Component
public class DnsAdapter implements DnsInspector {

    @Override
    public DnsSnapshot inspect(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException(
                "domain must not be blank"
            );
        }

        String normalizedDomain = normalizeDomain(domain);

        List<String> a = lookup(
            normalizedDomain,
            Type.A
        );

        List<String> aaa = lookup(
            normalizedDomain,
            Type.AAAA
        );

        List<String> txt = lookup(
            normalizedDomain,
            Type.TXT
        );

        List<String> mx = lookup(
            normalizedDomain,
            Type.MX
        );

        List<String> ns = lookup(
            normalizedDomain,
            Type.NS
        );

        List<String> cname = lookup(
            normalizedDomain,
            Type.CNAME
        );

        return new DnsSnapshot(
            a,
            aaa,
            txt,
            mx,
            ns,
            cname
        );
    }

    private List<String> lookup(
        String domain,
        int recordType
    ) {
        try {
            Lookup lookup = new Lookup(domain, recordType);

            Record[] records = lookup.run();

            if (records == null) {
                return List.of();
            }

            List<String> result = new ArrayList<>();

            for (Record record : records) {
                result.add(formatRecord(record));
            }

            return List.copyOf(result);

        } catch (Exception e) {
            return List.of();
        }
    }

    private String formatRecord(Record record) {

        if (record instanceof ARecord aRecord) {
            return aRecord.getAddress().getHostAddress();
        }

        if (record instanceof CNAMERecord cnameRecord) {
            return cnameRecord.getTarget().toString();
        }

        if (record instanceof MXRecord mxRecord) {
            return mxRecord.getPriority()
                + " "
                + mxRecord.getTarget();
        }

        if (record instanceof NSRecord nsRecord) {
            return nsRecord.getTarget().toString();
        }

        if (record instanceof TXTRecord txtRecord) {
            return txtRecord.rdataToString();
        }

        return record.rdataToString();
    }

    private String normalizeDomain(String domain) {
        String normalized = domain.trim().toLowerCase();

        if (normalized.endsWith(".")) {
            normalized =
                normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }
}
