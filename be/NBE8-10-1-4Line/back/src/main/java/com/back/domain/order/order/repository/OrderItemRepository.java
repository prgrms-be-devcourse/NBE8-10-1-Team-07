package com.back.domain.order.order.repository;

import com.back.domain.order.order.dto.OrderProductDetailDto;
import com.back.domain.order.order.dto.OrderProductSummaryDto;
import com.back.domain.order.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 상품별 요약 바: (상품id, 이름, 총수량, 총금액)
    @Query("""
        select new com.back.domain.order.order.dto.OrderProductSummaryDto(
            p.id,
            p.name,
            sum(oi.quantity),
            sum(1L * oi.quantity * p.price)
        )
        from OrderItem oi
        join oi.product p
        join oi.order o
        join o.customer c
        where c.email = :email
        group by p.id, p.name
    """)
    List<OrderProductSummaryDto> findProductSummaries(@Param("email") String email);

    // 바 클릭 상세: (주문정보 + 해당 상품 수량/소계)
    @Query("""
        select new com.back.domain.order.order.dto.OrderProductDetailDto(
            o.id,
            o.orderTime,
            o.orderStatus,
            o.shippingAddress,
            o.shippingCode,
            oi.quantity,
            p.price,
            (1L * oi.quantity * p.price)
        )
        from OrderItem oi
        join oi.product p
        join oi.order o
        join o.customer c
        where c.email = :email
          and p.id = :productId
        order by o.orderTime desc
    """)
    List<OrderProductDetailDto> findProductDetails(
            @Param("email") String email,
            @Param("productId") Long productId
    );
}
