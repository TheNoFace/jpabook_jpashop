package jpabook.jpashop.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.save(book);
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    @GetMapping("items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);

        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(form.getPrice());
        form.setStockQuantity(form.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    @PostMapping("items/{itemId}/edit")
    // PathVariable은 form에 담겨서 오기 때문에 굳이 쓰지 않아도 됨
    // updateItemForm의 form 객체 정보를 사용하기 위해 ModelAttribute Annotation 사용
    public String updateItem(@PathVariable("itemId") Long itemId, @ModelAttribute("form") BookForm form) {

        // // 준영속 엔티티: DB를 한 번 조회한 엔티티는 (새로운 객체이지만) 영속성 컨텍스트가 관리하지 않음
        // // 임의로 만들어낸 엔티티도 기존 식별자를 가지고 있으면 준영속 엔티티
        // Book book = new Book();
        // book.setId(form.getId());
        // book.setName(form.getName());
        // book.setPrice(form.getPrice());
        // book.setStockQuantity(form.getStockQuantity());
        // book.setAuthor(form.getAuthor());
        // book.setIsbn(form.getIsbn());
        
        // // 별도로 DB 저장을 하지 않는 경우 준영속 엔티티의 값은 수정해도 DB에 반영되지 않음
        // // 준영속 엔티티를 저장하는 방법: 1. 변경 감지 / 2. 병합(merge) 사용
        // itemService.save(book);

        // 컨트롤러 계층에서 엔티티를 직접 수정하지 않고, 서비스 계층에서 수정할 것
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());
        return "redirect:/items";
    }
}
