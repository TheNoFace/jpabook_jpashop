package jpabook.jpashop.repository.order.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /**
     * 컬렉션은 별도로 조회
     * Query: 루트 1번, 컬렉션 N 번
     * 단건 조회에서 많이 사용하는 방식
     */
    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders(); // 쿼리 1번 -> N개 주문 반환

        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); // 주문 개수만큼 쿼리 N번: N + 1 문제 발생
            o.setOrderItems(orderItems);
        });

        return result;
    }

    /**
     * 1:N 관계(컬렉션)를 제외한 나머지를 한번에 조회
     */
    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o" +
                " join o.member m" +
                " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    /**
     * 1:N 관계인 orderItems 조회
     */
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public List<OrderQueryDto> findAllByDto_optimized() {
        List<OrderQueryDto> result = findOrders();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream().collect(Collectors.groupingBy(OrderItemQueryDto -> OrderItemQueryDto.getOrderId()));
        return orderItemMap;
    }

    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream().map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                            " from Order o" +
                            " join o.member m" +
                            " join o.delivery d" + 
                            " join o.orderItems oi" + 
                            " join oi.item i", OrderFlatDto.class)
                            .getResultList();
    }
}
