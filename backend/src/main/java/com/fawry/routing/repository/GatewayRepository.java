package com.fawry.routing.repository;

import com.fawry.routing.entity.Gateway;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GatewayRepository extends JpaRepository<Gateway, Integer> {

    List<Gateway> findAllByActiveTrueOrderByIdAsc();

    List<Gateway> findAllByOrderByIdAsc();
}
