package com.fawry.routing.repository;

import com.fawry.routing.dto.response.GatewayHistoryResponse;
import com.fawry.routing.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;


public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    void deleteByGatewayId(Integer gatewayId);

    @EntityGraph(attributePaths = "gateway")
    List<Transaction> findByBillerIdOrderByCreatedAtDescIdDesc(Integer billerId, Pageable pageable);

    @EntityGraph(attributePaths = "gateway")
    List<Transaction> findByBillerIdAndBusinessDateOrderByCreatedAtDescIdDesc(Integer billerId,
                                                                              LocalDate businessDate,
                                                                              Pageable pageable);

    @EntityGraph(attributePaths = "gateway")
    List<Transaction> findByBillerIdAndGatewayIdOrderByCreatedAtDescIdDesc(Integer billerId,
                                                                           Integer gatewayId,
                                                                           Pageable pageable);

    @EntityGraph(attributePaths = "gateway")
    List<Transaction> findByBillerIdAndBusinessDateAndGatewayIdOrderByCreatedAtDescIdDesc(Integer billerId,
                                                                                          LocalDate businessDate,
                                                                                          Integer gatewayId,
                                                                                          Pageable pageable);

    @Query("""
            SELECT new com.fawry.routing.dto.response.GatewayHistoryResponse(
                   g.id,
                   g.name,
                   COUNT(t),
                   SUM(t.amount),
                   SUM(t.commission),
                   g.dailyLimitPerBiller)
            FROM Transaction t
            JOIN t.gateway g
            WHERE t.biller.id = :billerId
            GROUP BY g.id
            ORDER BY g.id
            """)
    List<GatewayHistoryResponse> groupByGateway(Integer billerId);

    @Query("""
            SELECT new com.fawry.routing.dto.response.GatewayHistoryResponse(
                   g.id,
                   g.name,
                   COUNT(t),
                   SUM(t.amount),
                   SUM(t.commission),
                   g.dailyLimitPerBiller)
            FROM Transaction t
            JOIN t.gateway g
            WHERE t.biller.id = :billerId AND t.businessDate = :businessDate
            GROUP BY g.id
            ORDER BY g.id
            """)
    List<GatewayHistoryResponse> groupByGatewayAndBusinessDate(Integer billerId,
                                                               LocalDate businessDate);

    @Query("""
            SELECT new com.fawry.routing.dto.response.GatewayHistoryResponse(
                   g.id,
                   g.name,
                   COUNT(t),
                   SUM(t.amount),
                   SUM(t.commission),
                   g.dailyLimitPerBiller)
            FROM Transaction t
            JOIN t.gateway g
            WHERE t.biller.id = :billerId AND g.id = :gatewayId
            GROUP BY g.id
            ORDER BY g.id
            """)
    List<GatewayHistoryResponse> groupByGatewayAndGatewayId(Integer billerId,
                                                            Integer gatewayId);

    @Query("""
            SELECT new com.fawry.routing.dto.response.GatewayHistoryResponse(
                   g.id,
                   g.name,
                   COUNT(t),
                   SUM(t.amount),
                   SUM(t.commission),
                   g.dailyLimitPerBiller)
            FROM Transaction t
            JOIN t.gateway g
            WHERE t.biller.id = :billerId
              AND t.businessDate = :businessDate
              AND g.id = :gatewayId
            GROUP BY g.id
            ORDER BY g.id
            """)
    List<GatewayHistoryResponse> groupByGatewayAndBusinessDateAndGatewayId(Integer billerId,
                                                                           LocalDate businessDate,
                                                                           Integer gatewayId);
}
