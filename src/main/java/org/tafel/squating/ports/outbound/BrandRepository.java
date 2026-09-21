package org.tafel.squating.ports.outbound;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.tafel.squating.domain.model.Brand;

public interface BrandRepository {
    Optional<Brand> findById(UUID Id);
    List<Brand> findAll();
    Brand save(Brand brand);
}
