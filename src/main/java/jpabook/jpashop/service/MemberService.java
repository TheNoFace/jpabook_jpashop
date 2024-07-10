package jpabook.jpashop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
// 데이터 변경 없는 조회일 경우 readOnly 옵션 사용시 최적화 가능
@Transactional(readOnly = true)
// final이 붙은 필드만 이용하여 생성자 생성
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;

  /*
   * // Setter Injection
   * // 테스트 코드 작성 시 주입이 편함
   * 
   * @Autowired
   * public void setMemberRepository(MemberRepository memberRepository) {
   * this.memberRepository = memberRepository;
   * }
   */

  // @RquiredArgsConstructor Annotation 사용하여 Lombok으로 생성
  /* 
   * // Constructor Injection
   * // @Autowired 자동으로 붙임
   * public MemberService(MemberRepository memberRepository) {
   * this.memberRepository = memberRepository;
   * }
   */

  // 회원 가입
  // 데이터 변경이 수반되기에 readOnly 해제
  @Transactional
  public Long join(Member member) {

    validateDuplicateMember(member);
    memberRepository.save(member);
    return member.getId();
  }

  // 멀티스레드 DB일 경우 추가 validation 필요
  private void validateDuplicateMember(Member member) {

    List<Member> findMembers = memberRepository.findByName(member.getName());
    if (!findMembers.isEmpty()) {
      throw new IllegalStateException("이미 존재하는 회원입니다.");
    }
  }

  // 회원 전체 조회
  public List<Member> findMembers() {
    return memberRepository.findAll();
  }

  // 회원 단일 조회
  public Member findOne(Long memberId) {
    return memberRepository.findOne(memberId);
  }
}
