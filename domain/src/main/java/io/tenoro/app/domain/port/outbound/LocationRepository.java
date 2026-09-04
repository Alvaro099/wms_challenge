package io.tenoro.app.domain.port.outbound;

import io.tenoro.app.domain.model.Location;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {
    Location save(Location location);
    Optional<Location> findByCode(String code);
    List<Location> findAll();
    boolean existsByCode(String code);
    void deleteAll();
}
