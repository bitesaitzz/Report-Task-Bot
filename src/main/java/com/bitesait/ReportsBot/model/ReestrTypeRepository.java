package com.bitesait.ReportsBot.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ReestrTypeRepository extends CrudRepository<ReestrType, Long> {

    List<ReestrType> findAllByIdIn(List<Integer> ids);

   //напиши запрос который получает все типы реестров, у которых id больше чем х, но меньше чем x+50
    List<ReestrType> findAllByIdBetween(int x, int y);


    @Query("SELECT r.name FROM ReestrType r WHERE r.id = :id")
    String findNameById(int id);
}
