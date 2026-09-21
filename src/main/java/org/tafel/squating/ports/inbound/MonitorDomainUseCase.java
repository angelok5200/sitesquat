package org.tafel.squating.ports.inbound;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.tafel.squating.domain.model.Brand;
import org.tafel.squating.domain.value.MonitoringPolicy;

public interface MonitorDomainUseCase {
    
    Brand registerBrand(
        String name,
        String primaryDomain,
        MonitoringPolicy monitoringPolicy,
        Set<String> monitoredIds,
        String referenceUrl
    );

    Brand findBrandById (UUID brandId);

    List<Brand> listBrands();
    void updateBrandPolicy (
        UUID brandId,
        MonitoringPolicy monitoringPolicy
    );
}
