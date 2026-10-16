package com.grosshaeuser.olmp.member.repository;

import com.grosshaeuser.olmp.TestcontainersConfiguration;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();

        memberRepository.saveAll(List.of(
                member("Doe", "James", LocalDate.of(1990, 1, 1)),
                member("Smith", "Jane", LocalDate.of(1991, 2, 2)),
                member("Johnson", "Alice", LocalDate.of(1992, 3, 3)),
                member("Brown", "Robert", LocalDate.of(1993, 4, 4)),
                member("McDonald", "Donald", LocalDate.of(1994, 5, 5))
        ));

        memberRepository.flush();
    }

    @Test
    void searchFindsMembersByFirstNameContainingSearchTerm() {
        Page<Member> result = memberRepository.search("ame", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(Member::getFirstName)
                .containsExactly("James");
    }

    @Test
    void searchFindsMembersByLastNameContainingSearchTerm() {
        Page<Member> result = memberRepository.search("son", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(Member::getLastName)
                .containsExactly("Johnson");
    }

    @Test
    void searchIsCaseInsensitiveForFirstName() {
        Page<Member> result = memberRepository.search("jAnE", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(Member::getFirstName)
                .containsExactly("Jane");
    }

    @Test
    void searchIsCaseInsensitiveForLastName() {
        Page<Member> result = memberRepository.search("mCdOnAlD", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(Member::getLastName)
                .containsExactly("McDonald");
    }

    @Test
    void searchFindsMembersWhenSearchTermMatchesEitherFirstNameOrLastName() {
        Page<Member> result = memberRepository.search("do", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Member::getLastName)
                .containsExactlyInAnyOrder("Doe", "McDonald");
    }

    @Test
    void searchReturnsEmptyPageWhenNoMemberMatches() {
        Page<Member> result = memberRepository.search("missing", PageRequest.of(0, 10));

        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void searchSupportsPagination() {
        Page<Member> firstPage = memberRepository.search("o", PageRequest.of(0, 2));
        Page<Member> secondPage = memberRepository.search("o", PageRequest.of(1, 2));

        assertThat(firstPage.getTotalElements()).isEqualTo(4);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent()).hasSize(2);

        assertThat(secondPage.getTotalElements()).isEqualTo(4);
        assertThat(secondPage.getTotalPages()).isEqualTo(2);
        assertThat(secondPage.getContent()).hasSize(2);

        assertThat(firstPage.getContent())
                .extracting(Member::getId)
                .doesNotContainAnyElementsOf(
                        secondPage.getContent().stream()
                                .map(Member::getId)
                                .toList()
                );
    }

    @Test
    void searchWithEmptySearchTermReturnsAllMembers() {
        Page<Member> result = memberRepository.search("", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getContent())
                .extracting(Member::getLastName)
                .containsExactlyInAnyOrder("Doe", "Smith", "Johnson", "Brown", "McDonald");
    }

    private static Member member(
            String lastName,
            String firstName,
            LocalDate dateOfBirth
    ) {
        return Member.builder()
                .lastName(lastName)
                .firstName(firstName)
                .dateOfBirth(dateOfBirth)
                .build();
    }
}