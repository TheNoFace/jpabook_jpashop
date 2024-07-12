package jpabook.jpashop.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.annotation.Rollback;
// import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.service.MemberService;

// @SpringJUnitConfig
@SpringBootTest
@Transactional
public class MemberServiceTest {

  @Autowired MemberService memberService;
  @Autowired MemberRepository memberRepository;
  @Autowired EntityManager em;

  @Test
  // @Rollback(false)
  public void 회원가입() throws Exception {
    // given
    Member member = new Member();
    member.setName("Kim");

    // when
    Long saveId = memberService.join(member);

    // then
    em.flush();  // 실제 insert 실행 후 rollback
    assertEquals(member, memberRepository.findOne(saveId));
  }

  @Test
  public void 중복예외() throws Exception {
    // given
    Member member1 = new Member();
    member1.setName("Kim");

    Member member2 = new Member();
    member2.setName("Kim");

    // when, then
    memberService.join(member1);
    assertThrows(IllegalStateException.class, () -> memberService.join(member2));

    // assertThrows 통해 예외를 검증하므로 삭제
    // fail("예외 발생");
  }
}
