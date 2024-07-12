package jpabook.jpashop.repository;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;

// Spring Bean으로 등록
@Repository
// 데이터 변경이 수반되는 모든 행동은 Transactional Annotation이 필수
@Transactional
@RequiredArgsConstructor
public class MemberRepository {

  // 서비스는 PersistenceContext로 인젝션 해야 함
  @PersistenceContext
  private final EntityManager em;

  // RequiredArgsConstructor 사용하여 생성자 생략 가능
  // public MemberRepository(EntityManager em) {
  //   this.em = em;
  // }

  public void save(Member member) {
    // persist라고 해서 DB에 Insert 하는 것이 아님
    em.persist(member);
  }

  public Member findOne(Long id) {
    return em.find(Member.class, id);
  }

  public List<Member> findAll() {
    return em.createQuery("select m from Member m", Member.class)
        .getResultList();
  }

  public List<Member> findByName(String name) {
    return em.createQuery("select m from Member m where m.name = :name", Member.class)
        .setParameter("name", name)
        .getResultList();
  }
}
