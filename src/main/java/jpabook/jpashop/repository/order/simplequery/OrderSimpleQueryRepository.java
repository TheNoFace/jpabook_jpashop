package jpabook.jpashop.repository.order.simplequery;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;
    
    // Repository는 절대로 Controller과 의존관계를 가지면 안됨!
    public List<OrderSimpleQueryDto> findOrderDtos() {
      return em.createQuery("select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                            " from Order o" +
                            " join o.member m" +
                            " join o.delivery d", OrderSimpleQueryDto.class)
                            .getResultList();
    }
}
