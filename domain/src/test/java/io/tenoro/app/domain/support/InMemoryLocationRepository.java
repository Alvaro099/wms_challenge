package io.tenoro.app.domain.support;

import io.tenoro.app.domain.model.Location;
import io.tenoro.app.domain.port.outbound.LocationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLocationRepository implements LocationRepository {

    private final Map<String, Location> locations = new ConcurrentHashMap<>();

    @Override
    public Location save(Location location) {
        locations.put(location.getCode(), location);
        return location;
    }

    @Override
    public Optional<Location> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(locations.get(code.toUpperCase()));
    }

    @Override
    public List<Location> findAll() {
        return new ArrayList<>(locations.values());
    }

    @Override
    public boolean existsByCode(String code) {
        if (code == null) {
            return false;
        }
        return locations.containsKey(code.toUpperCase());
    }

    @Override
    public void deleteAll() {
        locations.clear();
    }
}
