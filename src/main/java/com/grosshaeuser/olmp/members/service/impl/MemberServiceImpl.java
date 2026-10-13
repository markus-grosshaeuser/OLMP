package com.grosshaeuser.olmp.members.service.impl;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.members.dto.MemberRegistrationDTO;
import com.grosshaeuser.olmp.members.dto.MemberDTO;
import com.grosshaeuser.olmp.members.entity.Member;
import com.grosshaeuser.olmp.members.entity.MemberAccount;
import com.grosshaeuser.olmp.members.entity.MemberContactInformation;
import com.grosshaeuser.olmp.members.mapper.MemberMapper;
import com.grosshaeuser.olmp.members.repository.MemberRepository;
import com.grosshaeuser.olmp.members.service.MemberAccountService;
import com.grosshaeuser.olmp.members.service.MemberContactInformationService;
import com.grosshaeuser.olmp.members.service.MemberService;
import com.grosshaeuser.olmp.memberships.entity.Membership;
import com.grosshaeuser.olmp.memberships.mapper.MembershipMapper;
import com.grosshaeuser.olmp.memberships.service.MembershipService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberAccountService memberAccountService;
    private final MemberContactInformationService memberContactInformationService;
    private final MembershipService membershipService;
    private final AuthenticationContext authenticationContext;

    @Override
    public Page<MemberDTO> getMembers(String searchTerm, Pageable pageable) {
        if (!authenticationContext.hasAuthority(SecurityConstants.Privileges.READ_MEMBER_ACCOUNT)) {
            throw new AccessDeniedException("Access denied");
        }
        return memberRepository.search(searchTerm, pageable).map(MemberMapper::mapMemberToMemberDTO);
    }

    @Override
    @Transactional
    public Optional<MemberDTO> createMember(MemberRegistrationDTO dto) {
        if (!authenticationContext.hasAuthority(SecurityConstants.Privileges.CREATE_MEMBER_ACCOUNT)) {
            throw new AccessDeniedException("You have no permission to create a member account.");
        }
        Member persistedMember = memberRepository.save(MemberMapper.createMemberFromMemberRegistrationData(dto));
        memberAccountService.createMemberAccount(MemberMapper.createAccountDtoFromMemberRegistrationData(dto, persistedMember));
        if (dto.isWithContactDetails()) {
            memberContactInformationService.create(MemberMapper.createContactDetailsDtoFromMemberRegistrationData(dto, persistedMember));
        }
        if (dto.isWithMembership()) {
            membershipService.createMembership(MembershipMapper.createMembershipCreateDtoFromMembershipRegistrationData(dto, persistedMember));
        }
        return Optional.of(MemberMapper.mapMemberToMemberDTO(persistedMember));
    }

    @Override
    public Optional<MemberEditDTO> getDetailedMember(Long memberId) {
        return memberRepository.findById(memberId).flatMap(member -> {
            MemberAccount account = memberAccountService.findByMemberId(member.getId()).orElse(new MemberAccount());
            MemberContactInformation contact = memberContactInformationService.findByMemberId(member.getId()).orElse(new MemberContactInformation());
            Membership membership = membershipService.findByMemberId(member.getId()).orElse(new Membership());
            MemberEditDTO dto = MemberMapper.mergeMemberInformationToEditDto(member, account, contact);
            dto.setMembership(MembershipMapper.mapMembershipToEditDto(membership));
            return Optional.of(dto);
        });
    }

    @Override
    public Optional<MemberEditDTO> updateMember(MemberEditDTO dto) {
        return memberRepository.findById(dto.getId())
                .map(member -> {
                    member.setFirstName(dto.getFirstName());
                    member.setLastName(dto.getLastName());
                    member.setDateOfBirth(dto.getBirthDate());

                    Member updatedMember = memberRepository.save(member);

                    MemberAccount account = memberAccountService.update(dto.getAccount()).orElse(new MemberAccount());

                    MemberContactInformation contactInformation = dto.getContact() != null
                            ? memberContactInformationService.updateContactInformation(dto.getContact()).orElse(new MemberContactInformation())
                            : null;

                    Membership membership = dto.getMembership() != null
                            ? membershipService.updateMembership(dto.getMembership()).orElse(new Membership())
                            : null;

                    MemberEditDTO result = MemberMapper.mergeMemberInformationToEditDto(updatedMember, null, null);
                    result.setAccount(MemberMapper.mapMemberAccountToEditDto(account));
                    result.setContact(MemberMapper.mapMemberContactInformationToEditDto(contactInformation));
                    result.setMembership(MembershipMapper.mapMembershipToEditDto(membership));
                    return result;
                });
    }

    @Override
    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }
}
