package jpabook.jpashop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
  
  private final OrderRepository orderRepository;
  private final MemberRepository memberRepository;
  private final ItemRepository itemRepository;

  // 주문
  @Transactional
  public Long order(Long memberId, Long itemId, int count) {
    
    // Entity 조회
    Member member = memberRepository.findOne(memberId);
    Item item = itemRepository.findOne(itemId);

    // 배송 정보 생성
    Delivery delivery = new Delivery();
    delivery.setAddress(member.getAddress());

    // 주문 상품 생성
    OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

    // 주문 생성
    Order order = Order.createOrder(member, delivery, orderItem);

    // 주문 저장
    // Order 클래스에서 orderItem과 delivery가 cascade 설정되어 있어 모두 persist가 적용되어
    // 따로 Repository를 생성 후 저장하지 않아도 됨
    orderRepository.save(order);

    return order.getId();
  }

  // 취소
  @Transactional
  public void cancelOrder(Long orderId) {
    // 주문 엔티티 조회
    Order order = orderRepository.findOne(orderId);
    // 주문 취소
    order.cancel();
  }

  // 검색
  public List<Order> findOrders(OrderSearch orderSearch) {
    return orderRepository.findAll(orderSearch);
  }
}
