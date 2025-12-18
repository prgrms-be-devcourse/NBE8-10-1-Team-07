package com.back.domain.order.order.repository;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findTopByOrderByIdDesc();
    List<Order> findAllByOrderStatusAndOrderTimeBefore(OrderStatus orderStatus, LocalDateTime time);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Order o SET o.orderStatus = :status WHERE o.id IN :ids")
    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") OrderStatus status);
}