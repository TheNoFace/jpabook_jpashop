package jpabook.jpashop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
  
  public final ItemRepository itemRepository;

  @Transactional
  public void save(Item item) {
    itemRepository.save(item);
  }

  // 준영속 엔티티 수정 1: 변경 감지 사용
  // 영속성 컨텍스트에서 엔티티를 다시 조회한 후에 데이터를 수정
  // Transactional에서 엔티티를 재조회 후 데이터 수정
  @Transactional
  // public void updateItem(Long itemId, Book bookParam) { // 파라미터로 넘어온 준영속 상태의 엔티티
  public void updateItem(Long itemId, String name, int price, int stockQuantity) {

    // 엔티티 조회 -> 영속성 엔티티
    Item foundItem = itemRepository.findOne(itemId);

    // // 엔티티 데이터 수정
    // foundItem.setPrice(bookParam.getPrice());
    // foundItem.setName(bookParam.getName());
    // foundItem.setStockQuantity(bookParam.getStockQuantity());

    // 컨트롤러 계층에서 받아온 값을 이용하여 서비스 계층에서 수정
    // Setter 사용하지 말고 Setter 역할을 대체할 메서드를 생성하는 것을 권장
    // foundItem.changeData(name, price, stockQuantity);
    foundItem.setName(name);
    foundItem.setPrice(price);
    foundItem.setStockQuantity(stockQuantity);

    // 조회한 엔티티는 영속성 엔티티이므로 커밋 시점에 변경 감지 (Dirty Checking) 동작하여 자동으로 업데이트
  }

  public List<Item> findItems() {
    return itemRepository.findAll();
  }

  public Item findOne(Long itemId) {
    return itemRepository.findOne(itemId);
  }
}
