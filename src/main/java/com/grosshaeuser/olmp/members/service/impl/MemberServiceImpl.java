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
        checkPermission(SecurityConstants.Privileges.READ_MEMBER_ACCOUNT, "You have no permission to read memberships.");
        return memberRepository.search(searchTerm, pageable).map(MemberMapper::mapMemberToMemberDTO);
    }

    @Override
    @Transactional
    public Optional<MemberDTO> createMember(MemberRegistrationDTO dto) {
        checkPermission(SecurityConstants.Privileges.CREATE_MEMBER_ACCOUNT, "You have no permission to create a member account.");
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
        checkPermission(SecurityConstants.Privileges.READ_MEMBER_ACCOUNT, "You have no permission to read memberships.");
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
        checkPermission(SecurityConstants.Privileges.UPDATE_MEMBER_ACCOUNT, "You have no permission to update memberships.");
        return memberRepository.findById(dto.getId())
                .map(member -> {
                    member.setFirstName(dto.getFirstName());
                    member.setLastName(dto.getLastName());
                    member.setDateOfBirth(dto.getBirthDate());

                    Member updatedMember = memberRepository.save(member);

                    MemberAccount updatedAccount = dto.getAccount() != null
                            ? memberAccountService.update(dto.getAccount()).orElse(new MemberAccount())
                            : memberAccountService.findByMemberId(updatedMember.getId()).orElse(null);

                    MemberContactInformation updatedContactInfo = dto.getContact() != null
                            ? memberContactInformationService.updateContactInformation(dto.getContact()).orElse(new MemberContactInformation())
                            : memberContactInformationService.findByMemberId(updatedMember.getId()).orElse(null);

                    Membership umdatedMembership = dto.getMembership() != null
                            ? membershipService.updateMembership(dto.getMembership()).orElse(new Membership())
                            : membershipService.findByMemberId(updatedMember.getId()).orElse(null);

                    MemberEditDTO result = MemberMapper.mergeMemberInformationToEditDto(updatedMember, updatedAccount, updatedContactInfo);
                    result.setMembership(MembershipMapper.mapMembershipToEditDto(umdatedMembership));
                    return result;
                });
    }

    @Override
    public void deleteMember(Long memberId) {
        checkPermission(SecurityConstants.Privileges.DELETE_MEMBER_ACCOUNT, "You have no permission to delete memberships.");
        memberRepository.deleteById(memberId);
    }

    private void checkPermission(String privilege, String errorMessage) {
        if (!authenticationContext.hasAuthority(privilege)) {
            throw new AccessDeniedException(errorMessage);
        }
    }
}
