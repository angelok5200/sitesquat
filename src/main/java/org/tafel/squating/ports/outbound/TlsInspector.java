package org.tafel.squating.ports.outbound;

import org.tafel.squating.domain.value.TlsSnapshot;

public interface TlsInspector {
    TlsSnapshot inspect(String domain);
}
