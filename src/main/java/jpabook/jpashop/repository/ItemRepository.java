package jpabook.jpashop.repository;

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
      // persist를 통해 객체를 영속화 (새로 등록)
      em.persist(item);
      // ID 값이 있음 == 이전에 등록된 객체
    } else {
      // 준영속 엔티티 수정 2: 병합(merge)
      // 파라미터로 넘어온 준영속 상태의 엔티티를 영속 상태로 변경
      // 병합 후 반환된 객체는 영속성 엔티티이지만, 기존 파라미터로 넘어온 엔티티는 준영속 엔티티로 유지됨
      em.merge(item);

      /*
       * 병합의 동작 방식은 아래의 코드와 동일
       * @Transactional
       * public Item updateItem(Long itemId, Book bookParam) {
       * 
       * Item foundItem = itemRepository.findOne(itemId);
       * 
       * foundItem.setPrice(bookParam.getPrice());
       * foundItem.setName(bookParam.getName());
       * foundItem.setStockQuantity(bookParam.getStockQuantity());
       * 
       * return foundItem;
       * }
       */
    }
  }

  public Item findOne(Long id) {
    return em.find(Item.class, id);
  }

  public List<Item> findAll() {
    return em.createQuery("select i from Item i", Item.class).getResultList();
  }
}
