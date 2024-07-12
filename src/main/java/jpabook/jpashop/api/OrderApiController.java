package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    // 주문 V1: 엔티티 직접 노출
    // Hibernate5 모듈과 @JsonIgnore 사용하여 무한 루프 제한
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        for (Order order : orders) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return orders;
    }

    // 주문 V2: 단순 쿼리 결과 DTO 감싸기
    @SuppressWarnings("rawtypes")
    @GetMapping("/api/v2/orders")
    public Result ordersV2() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return new Result<>(collect);
    }

    // 주문 V3: fetch join 후 DTO 감싸기
    // 단점: 페이징 불가
    // 강제 페이징 시 우선 모든 쿼리를 진행하여 메모리에 적재 후 메모리 내에서 페이징 진행
    @SuppressWarnings("rawtypes")
    @GetMapping("/api/v3/orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        // for (Order order : orders) {
        //     System.out.println("Order ref=" + order + " id=" + order.getId());
        // }

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return new Result<>(collect);
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
