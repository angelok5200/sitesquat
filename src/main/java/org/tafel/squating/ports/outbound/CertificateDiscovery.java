package org.tafel.squating.ports.outbound;

import java.util.List;

public interface CertificateDiscovery {
    List<String> findCertificate(String domain);
}
