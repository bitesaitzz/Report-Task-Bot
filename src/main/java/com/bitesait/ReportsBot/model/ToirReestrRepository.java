package com.bitesait.ReportsBot.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ToirReestrRepository extends CrudRepository<ToirReestr, Long> {

    Optional<ToirReestr> findByReestr(Reestr reestr);

    List<ToirReestr> findByType(int type);


    @Query("SELECT t FROM ToirReestrTable t WHERE t.type > 1")
    List<ToirReestr> findMingaz();
}
