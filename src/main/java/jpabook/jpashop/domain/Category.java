package jpabook.jpashop.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Category {
  
  @Id @GeneratedValue
  @Column(name = "category_id")
  private Long id;

  private String name;

  @ManyToMany
  @JoinTable(name = "category_item",                       // category_item 중계 테이블 생성
      joinColumns = @JoinColumn(name = "category_id"),     // 중계 테이블 category_id 컬럼 등록
      inverseJoinColumns = @JoinColumn(name = "item_id"))  // 중계 테이블 역참조 컬럼 item_id 등록
  private List<Item> items = new ArrayList<>();

  // 자기 자신을 부모-자식으로 맵핑
  // 모든 엔티티는 지연 로딩으로 설정해야함 (중요)
  // 일대일, 다대일 관계는 기본적으로 EAGER 로딩이므로 지연 로딩으로 설정해야함
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Category parent;

  @OneToMany(mappedBy = "parent")
  private List<Category> child = new ArrayList<>();

  // 연관 관계 편의 메서드
  public void addChildCategory(Category child) {
    this.child.add(child);
    child.setParent(this);
  }
}
