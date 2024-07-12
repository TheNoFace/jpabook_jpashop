package jpabook.jpashop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;

@SpringBootTest
@Transactional
public class OrderServiceTest {

  @Autowired EntityManager em;
  @Autowired OrderService orderService;
  @Autowired OrderRepository orderRepository;

  @Test
  public void 상품주문() throws Exception {
    // given
    Member member = createMember();
    Book book = createBook("책 1", 10000, 10);
    int orderCount = 2;

    // when
    Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

    // then
    Order getOrder = orderRepository.findOne(orderId);

    assertEquals(OrderStatus.ORDER, getOrder.getStatus(), "상품 주문시 상태는 ORDER");
    assertEquals(1, getOrder.getOrderItems().size(), "주문 상품 수 확인");
    assertEquals(book.getPrice() * orderCount, getOrder.getTotalPrice(), "주문 가격: 수량 * 가격");
    assertEquals(8, book.getStockQuantity(), "주문 수량만큼 재고 감소");
  }

  @Test
  public void 재고수량초과() throws Exception {
    // given
    Member member = createMember();
    Book book = createBook("책 1", 10000, 10);
    
    int orderCount = 11;
    
    // when, then
    assertThrows(NotEnoughStockException.class,
    () -> orderService.order(member.getId(), book.getId(), orderCount));
  }
  
  @Test
  public void 주문취소() throws Exception {
    // given
    Member member = createMember();
    Book book = createBook("책 1", 10000, 10);

    int orderCount = 2;

    Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

    // when
    orderService.cancelOrder(orderId);
    
    // then
    Order getOrder = orderRepository.findOne(orderId);

    assertEquals(OrderStatus.CANCEL, getOrder.getStatus(), "주문 취소 상태는 CANCEL");
    assertEquals(10, book.getStockQuantity(), "취소 상품은 재고 원복");

  }

  private Book createBook(String name, int price, int StockQuantity) {
    Book book = new Book();
    book.setName(name);
    book.setPrice(price);
    book.setStockQuantity(StockQuantity);
    em.persist(book);
    return book;
  }

  private Member createMember() {
    Member member = new Member();
    member.setName("회원");
    member.setAddress(new Address("서울", "강가", "123123"));
    em.persist(member);
    return member;
  }
}
