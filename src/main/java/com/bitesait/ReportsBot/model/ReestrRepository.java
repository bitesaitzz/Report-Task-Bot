package com.bitesait.ReportsBot.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ReestrRepository extends CrudRepository<Reestr, Long> {
    Optional<List<Reestr>> findByType(int type);
}
