package jpabook.jpashop.domain.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

// import com.querydsl.core.Query;
// import com.querydsl.core.types.dsl.BooleanExpression;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
// import jpabook.jpashop.domain.OrderStatus;
// import jpabook.jpashop.domain.QMember;
// import jpabook.jpashop.domain.QOrder;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

  private final EntityManager em;

  public void save(Order order) {
    em.persist(order);
  }

  public Order findOne(Long id) {
    return em.find(Order.class, id);
  }

  // 주문 검색 기능
  // // Querydsl
  // public List<Order> findAllDsl(OrderSearch orderSearch) {

  //   QOrder order = QOrder.order;
  //   QMember member = QMember.member;

  //   return query.select(order)
  //               .from(order)
  //               .join(order.member, member)
  //               .where(statusEq(orderSearch.getOrderStatus()),
  //                      nameLike(orderSearch.getMemberName()))
  //               .limit(1000)
  //               .fetch();
  // }
  
  // private BooleanExpression statusEq(OrderStatus orderStatus) {
  //   if (orderStatus == null) {
  //     return null;
  //   }
  //   return order.status.eq(orderStatus);
  // }

  // private BooleanExpression nameLike(String nameCond) {
  //   if (!StringUtils.hasText(nameCond)) {
  //     return null;
  //   }
  //   return member.name.like(nameCond);
  // }

  
  // JPA Criteria
  public List<Order> findAll(OrderSearch orderSearch) {

    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Order> cq = cb.createQuery(Order.class);
    Root<Order> o = cq.from(Order.class);
    Join<Order, Member> m = o.join("member", JoinType.INNER);  // Member 테이블과 Join

    List<Predicate> criteria = new ArrayList<>();

    // 주문 상태 검색
    if (orderSearch.getOrderStatus() != null) {
      Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
      criteria.add(status);
    }

    // 회원 이름 검색
    if (StringUtils.hasText(orderSearch.getMemberName())) {
      Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
      criteria.add(name);
    }

    cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
    TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);

    return query.getResultList();
  }
}
