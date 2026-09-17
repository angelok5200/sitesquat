package org.tafel.squating.ports.outbound;

import org.tafel.squating.domain.value.DnsSnapshot;

public interface DnsInspector {
    DnsSnapshot inspect(String domain);
}
