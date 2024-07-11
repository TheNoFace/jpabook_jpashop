package jpabook.jpashop.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    // - Member 엔티티를 직접 노출 시 Member의 모든 속성이 반환됨
    // - @JsonIgnore 사용하면 반환 속성을 제한할 수 있으나, 다른 API 개발 시 제한됨
    // - 엔티티가 변경되면 API 스펙 역시 변경됨
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> foundMembers = memberService.findMembers();
        // Entity -> DTO Conversion
        List<MemberDto> collect = foundMembers.stream().map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result<>(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    @PostMapping("/api/v1/members")
    // @RequestBody: JSON으로 받은 데이터를 member 객체에 알아서 넣어줌
    /**
     * 등록 V1: 요청 값으로 Member 엔티티를 직접 받는다 -> API와 엔티티가 1:1 연결
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등등)
     * - 회원 엔티티에 대한 API가 다양해질 경우, 한 엔티티에 각 API를 위한 모든 요청 요구사항 충족시키기 어려움
     * - 엔티티가 변경되면 API 스펙도 변경됨
     * 결론
     * - API가 엔티티를 직접 바인딩하지 않고 DTO 통해 접근해야함
     * - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
     * - 엔티티는 절대로 직접 접근하지 않기
     */
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    // 요청 값으로 Member 엔티티 대신에 별도의 DTO를 받음
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName());
        Member foundMember = memberService.findOne(id);
        return new UpdateMemberResponse(foundMember.getId(), foundMember.getName());
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }
}
