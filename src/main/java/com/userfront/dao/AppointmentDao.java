package com.userfront.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.userfront.domain.Appointment;

public interface AppointmentDao extends CrudRepository<Appointment, Long> {

    List<Appointment> findAll();
    
    @Query(value = "SELECT * FROM appointment WHERE user_id = :userId", nativeQuery = true)
    List<Appointment> findByUserId(@Param("userId") Long userId);
}
