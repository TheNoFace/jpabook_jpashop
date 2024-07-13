package jpabook.jpashop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jpabook.jpashop.domain.Member;

public interface MemberRepositoryJpa extends JpaRepository<Member, Long> {
    List<Member> findByName(String name);
}
