package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

/*
 * nTOOne(ManyToOne, OneToOne)에 적용
 * Order
 * Order -> Member
 * Order -> Delivery 연결
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /*
     * 양방향 직접 연결 시 nTOOne(ManyToOne, OneToOne) 관계일 경우
     * 한 쪽에 @JsonIgnore 사용하지 않으면 무한 루프에 빠짐
     * Hibernate5Module 사용하여 지연 로딩을 강제 초기화할 수 있으나
     * 애초에 엔티티를 API에 직접 노출시키지 않는 것이 좋음
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        return all;
    }

    // DTO로 감싸서 반환
    @SuppressWarnings("rawtypes")
    @GetMapping("/api/v2/simple-orders")
    public Result ordersV2() {
        // ORDER 2개
        // 지연 로딩 시 N + 1 문제 발생
        // 주문 쿼리 1 (주문 2개 반환) + 회원 N번 조회 + 배송 N번 조회 -> 최악의 경우 총 5회의 쿼리
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream().map(o -> new SimpleOrderDto(o)).collect(Collectors.toList());

        return new Result<>(result);
    }

    // fetch join
    // 엔티티에 직접 접근해야하는 것이 단점
    @SuppressWarnings("rawtypes")
    @GetMapping("/api/v3/simple-orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream().map(o -> new SimpleOrderDto(o)).collect(Collectors.toList());

        return new Result<>(result);
    }

    // SQL과 같이 원하는 값을 선택하여 조회
    // createQuery 내 JPQL 문장에서 new 명령어를 사용하여 JPQL의 결과를 즉시 DTO로 변환
    // 약간의 최적화가 가능하나, 레포지토리 재사용성이 떨어지며 API 스펙에 맞춰 레포지토리 코드가 작성됨
    @SuppressWarnings("rawtypes")
    @GetMapping("/api/v4/simple-orders")
    public Result ordersV4() {
        List<OrderSimpleQueryDto> orderSimpleQueryDtos = orderSimpleQueryRepository.findOrderDtos();
        return new Result<>(orderSimpleQueryDtos);
    }

    // Result 배열 객체로 한 번 더 감싸서 반환하기
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }
}
