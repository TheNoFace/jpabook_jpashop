package jpabook.jpashop.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

  @Id @GeneratedValue
  @Column(name = "order_id")
  private Long id;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  // OrderItem과 Delivery는 Order에서만 사용하기 때문에 cascade 설정하는게 좋음
  // cascade 옵션 사용 시 order가 저장되면 cascade가 설정된 orderItems도 저장됨
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
  private List<OrderItem> orderItems = new ArrayList<>();

  // 자주 조회되는 Order 테이블에 delivery_id 외래 키 저장
  // 연관관계의 주인은 Deliver 테이블
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "delivery_id")
  private Delivery delivery;

  private LocalDateTime orderDate;  // 주문 시간

  // 기본은 ORDINAL; 이후 enum 목록이 변경될 경우 인덱스가 변경될 수 있음
  @Enumerated(EnumType.STRING)
  private OrderStatus status;   // 주문 상태 [ORDER, CANCEL]

  // 연관 관계 편의 메서드
  public void setMember(Member member) {
    this.member = member;
    member.getOrders().add(this);
  }

  public static void main(String[] args) {
    Member member = new Member();
    Order order = new Order();

    // member.getOrders().add(order);
    order.setMember(member);
  }

  public void addOrderItem(OrderItem orderItem) {
    orderItems.add(orderItem);
    orderItem.setOrder(this);
  }

  public void setDelivery(Delivery delivery) {
    this.delivery = delivery;
    delivery.setOrder(this);
  }

  // 생성 메서드
  public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
    Order order = new Order();
    order.setMember(member);
    order.setDelivery(delivery);
    for (OrderItem orderItem : orderItems) {
      order.addOrderItem(orderItem);
    }
    order.setStatus(OrderStatus.ORDER);
    order.setOrderDate(LocalDateTime.now());
    return order;
  }

  // 비즈니스 로직
  /*
   * 주문 취소
   */
  public void cancel() {
    if (delivery.getStatus() == DeliveryStatus.COMPLETED) {
      throw new IllegalStateException("배송 완료된 상품은 취소 불가");
    }

    this.setStatus(OrderStatus.CANCEL);
    for (OrderItem orderItem : orderItems) {
      // 강조할 필요가 있을 경우가 아니면 this 사용할 필요 없음
      orderItem.cancel();
    }
  }

  // 조회 로직
  /*
   * 주문 전체 가격 조회
   */
  public int getTotalPrice() {
    // int totalPrice = 0;
    // for (OrderItem orderItem : orderItems) {
    //   totalPrice += orderItem.getTotalPrice();
    // }
    // return totalPrice;
    return orderItems.stream()
                    .mapToInt(OrderItem::getTotalPrice)
                    .sum();
  }
}
