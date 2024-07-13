package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    // 엔티티 조회: 권장
    // 주문 v1: 엔티티 직접 노출
    // Hibernate5 모듈과 @JsonIgnore 사용하여 무한 루프 제한
    @SuppressWarnings("rawtypes")
    @GetMapping("/api/v1/orders")
    public Result ordersV1() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        for (Order order : orders) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return new Result<>(orders);
    }

    // 주문 v2: 단순 쿼리 결과 DTO 감싸기
    @SuppressWarnings("rawtypes")
    @GetMapping("/api/v2/orders")
    public Result ordersV2() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return new Result<>(collect);
    }

    // 주문 v3: fetch join 후 DTO 감싸기
    // 단점: 페이징 불가
    // 강제 페이징 시 우선 모든 쿼리를 진행하여 메모리에 적재 후 메모리 내에서 페이징 진행
    @SuppressWarnings("rawtypes")
    @GetMapping("/api/v3/orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        // for (Order order : orders) {
        // System.out.println("Order ref=" + order + " id=" + order.getId());
        // }

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return new Result<>(collect);
    }

    /**
     * 주문 v3.1: 엔티티를 조회해서 DTO로 변환 (페이징 고려)
     * - ToOne 관계만 우선 모두 페치 조인으로 최적화: Batch size만큼 IN 쿼리로 조회
     * - 쿼리 수: N + 1 -> 1 + 1로 감소
     * - 컬렉션 관계는 hibernate.default_batch_fetch_size (전역), @BatchSize(개별)로 최적화
     */
    @SuppressWarnings("rawtypes")
    @GetMapping("/api/v3.1/orders")
    public Result ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {

        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return new Result<>(collect);
    }

    // DTO 조회
    // 주문 v4: JPA에서 DTO 직접 조회
    @SuppressWarnings("rawtypes")
    @GetMapping("/api/v4/orders")
    public Result ordersV4() {
        return new Result<>(orderQueryRepository.findOrderQueryDtos());
    }

    // 주문 v5: 컬렉션 조회 최적화
    // 1:N 관계 컬렉션은 IN 구문을 사용하여 메모리에 미리 조회해서 최적화
    @SuppressWarnings("rawtypes")
    @GetMapping("/api/v5/orders")
    public Result ordersV5() {
        return new Result<>(orderQueryRepository.findAllByDto_optimized());
    }

    /*
     * 주문 v6: 플랫 데이터 최적화 -> Query: 1번
     * JOIN 결과를 그대로 조회 후 애플리케이션에서 원하는 모양으로 직접 변환
     * 단점
     * - 쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되므로
     * - 상황에 따라 V5 보다 더 느릴 수도 있다.
     * - 애플리케이션에서 추가 작업이 크다.
     * - 페이징 불가능
     */
    @SuppressWarnings("rawtypes")
    @GetMapping("/api/v6/orders")
    public Result ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return new Result<>(flats.stream()
                .collect(Collectors.groupingBy(
                        o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(),
                                o.getAddress()),
                        Collectors.mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(),
                                o.getOrderPrice(), o.getCount()), Collectors.toList())))
                .entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(Collectors.toList()));
    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();

            // DTO 내부에서 엔티티에 접근: 외부에 엔티티의 모든 정보가 노출됨
            // order.getOrderItems().stream().forEach(o -> o.getItem().getName());
            // orderItems = order.getOrderItems();

            // DTO 내부에서 엔티티를 DTO로 감싸서 반환
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }
}
