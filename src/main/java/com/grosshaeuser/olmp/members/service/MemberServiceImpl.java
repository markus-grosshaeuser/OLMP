package com.grosshaeuser.olmp.members.service;

import com.grosshaeuser.olmp.members.dto.MemberCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberDTO;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.entity.MemberContactInformation;
import com.grosshaeuser.olmp.members.mapper.MemberMapper;
import com.grosshaeuser.olmp.members.repository.MemberContactInformationRepository;
import com.grosshaeuser.olmp.members.repository.MemberRepository;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
import com.grosshaeuser.olmp.memberships.dto.MembershipCreateDto;
import com.grosshaeuser.olmp.memberships.service.MembershipService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{
    private final MemberRepository memberRepository;
    private final MemberAccountRepository memberAccountRepository;
    private final MemberContactInformationRepository memberContactInformationRepository;
    private final MembershipService membershipService;
    private final AuthenticationContext authenticationContext;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<MemberDTO> getMembers(String searchTerm, Pageable pageable) {
        if (!authenticationContext.hasAuthority(SecurityConstants.Privileges.READ_MEMBER_ACCOUNT)) {
            throw new AccessDeniedException("Access denied");
        }
        return memberRepository.search(searchTerm, pageable).map(MemberMapper::mapMemberToMemberDTO);
    }

    @Override
    @Transactional
    public Optional<MemberDTO> createMember(MemberCreateDTO dto) {
        if (!authenticationContext.hasAuthority(SecurityConstants.Privileges.CREATE_MEMBER_ACCOUNT)) {
            throw new AccessDeniedException("You have no permission to create a member account.");
        }

        Member persistedMember = memberRepository.save(
                Member.builder()
                        .lastName(dto.getLastName())
                        .firstName(dto.getFirstName())
                        .dateOfBirth(dto.getDateOfBirth())
                        .build());

        memberAccountRepository.save(
                MemberAccount.builder()
                        .member(persistedMember)
                        .email(dto.getEmail())
                        .passwordHash(passwordEncoder.encode(dto.getPassword()))
                        .verified(dto.isVerified())
                        .enabled(dto.isEnabled())
                        .build());

        if (dto.isWithContactDetails()) {
            memberContactInformationRepository.save(
                    MemberContactInformation.builder()
                            .member(persistedMember)
                            .phoneNumber(dto.getPhoneNumber())
                            .street(dto.getStreet())
                            .houseNumber(dto.getHouseNumber())
                            .postalCode(dto.getPostalCode())
                            .city(dto.getCity())
                            .country(dto.getCountry())
                            .build());
        }

        if (dto.isWithMembership()) {
            MembershipCreateDto membershipCreateDto = new MembershipCreateDto(
                    persistedMember,
                    dto.getMembershipType(),
                    dto.getMembershipStatus(),
                    dto.getMembershipStartDate(),
                    dto.getMembershipEndDate(),
                    dto.getMembershipRenewalDate(),
                    dto.getMembershipCancellationDate()
            );

            membershipService.createMembership(membershipCreateDto);
        }

        return Optional.of(MemberMapper.mapMemberToMemberDTO(persistedMember));
    }
}
