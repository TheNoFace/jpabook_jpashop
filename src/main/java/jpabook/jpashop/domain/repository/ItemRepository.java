package jpabook.jpashop.domain.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
// import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ItemRepository {
  
  // @PersistenceContext
  private final EntityManager em;

  public void save(Item item) {
    // JPA에 저장하기 전까지는 ID 값이 없음
    // ID 값이 없음 == 새로 생성한 객체
    if (item.getId() == null) {
      // persist 사용하여 새로 등록
      em.persist(item);
    // ID 값이 있음 == 이전에 등록된 객체
    } else {
      // merge 사용하여 DB에 저장(업데이트)
      em.merge(item);
    }
  }

  public Item findOne(Long id) {
    return em.find(Item.class, id);
  }

  public List<Item> findAll() {
    return em.createQuery("select i from Item i", Item.class).getResultList();
  }
}
