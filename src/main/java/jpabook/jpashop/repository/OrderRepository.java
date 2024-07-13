package jpabook.jpashop.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

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
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QOrder;
import jpabook.jpashop.domain.OrderStatus;
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
  // Querydsl
  public List<Order> findAll(OrderSearch orderSearch) {
    QOrder order = QOrder.order;
    QMember member = QMember.member;

    JPAQueryFactory query = new JPAQueryFactory(em);

    return query.select(order)
        .from(order)
        .join(order.member, member)
        .where(statusEq(orderSearch.getOrderStatus()),
            nameLike(orderSearch.getMemberName()))
        .limit(1000)
        .fetch();
  }

  private BooleanExpression statusEq(OrderStatus orderCond) {
    if (orderCond == null) {
      return null;
    }
    return QOrder.order.status.eq(orderCond);
  }

  private BooleanExpression nameLike(String nameCond) {
    if (!StringUtils.hasText(nameCond)) {
      return null;
    }
    return QMember.member.name.like(nameCond);
  }

  // // JPA Criteria
  public List<Order> findAllJpa(OrderSearch orderSearch) {

    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Order> cq = cb.createQuery(Order.class);
    Root<Order> o = cq.from(Order.class);
    Join<Order, Member> m = o.join("member", JoinType.INNER); // Member 테이블과 Join

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

  // fetch join: 주문 조회화면서 동시에 멤버 정보와 배달 정보를 fetch 후 join
  // nToOne 관계는 fetch join시 페이징 문제가 발생하지 않음
  public List<Order> findAllWithMemberDelivery() {
    return em.createQuery("select o from Order o" +
        " join fetch o.member m" +
        " join fetch o.delivery d", Order.class)
        .getResultList();
  }

  // fetch join with collection query
  // Order와 OrderItems join시 쿼리 결과가 Order 개수 * OrderItems 개수가 됨
  // distinct 사용하여 중복 제거: DB 쿼리는 행의 모든 값이 동일해야 중복이 제거됨
  // JPA 반환값에서만 중복 제거
  // - 근데 왜 distinct 없이도 중복이 제거되지?
  public List<Order> findAllWithItem() {
    return em.createQuery("select distinct o from Order o" +
        " join fetch o.member m" +
        " join fetch o.delivery d" +
        " join fetch o.orderItems oi" +
        " join fetch oi.item i", Order.class)
        .getResultList();
  }

  // fetch join with paging
  public List<Order> findAllWithMemberDelivery(int offset, int limit) {
    return em.createQuery("select o from Order o" +
        " join fetch o.member m" +
        " join fetch o.delivery d", Order.class)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }
}