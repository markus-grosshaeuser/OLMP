package com.grosshaeuser.olmp.members.service.impl;

import com.grosshaeuser.olmp.members.dto.*;
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
        checkPermission(SecurityConstants.Privileges.READ_MEMBER_ACCOUNT, "You have no permission to read members.");
        searchTerm = searchTerm == null ? "" : searchTerm.trim();
        return memberRepository.search(searchTerm, pageable).map(MemberMapper::mapMemberToMemberDTO);
    }

    @Override
    @Transactional
    public Optional<MemberDTO> createMember(MemberRegistrationDTO dto) {
        checkPermission(SecurityConstants.Privileges.CREATE_MEMBER_ACCOUNT, "You have no permission to create a member.");
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
        checkPermission(SecurityConstants.Privileges.READ_MEMBER_ACCOUNT, "You have no permission to read members.");
        return memberRepository.findById(memberId).flatMap(member -> {
            MemberAccount account = memberAccountService.findByMemberId(member.getId()).orElse(new MemberAccount());
            MemberContactInformation contact = memberContactInformationService.findByMemberId(member.getId()).orElse(new MemberContactInformation());
            Membership membership = membershipService.findActiveByMemberId(member.getId()).orElse(new Membership());
            MemberEditDTO dto = MemberMapper.mergeMemberInformationToEditDto(member, account, contact);
            dto.setMembership(MembershipMapper.mapMembershipToEditDto(membership));
            return Optional.of(dto);
        });
    }

    @Override
    public Optional<MemberEditDTO> updateMember(MemberEditDTO dto) {
        checkPermission(SecurityConstants.Privileges.UPDATE_MEMBER_ACCOUNT, "You have no permission to update members.");
        return memberRepository.findById(dto.getId())
                .map(member -> {
                    member.setFirstName(dto.getFirstName());
                    member.setLastName(dto.getLastName());
                    member.setDateOfBirth(dto.getBirthDate());

                    Member updatedMember = memberRepository.save(member);

                    MemberAccount updatedAccount = dto.getAccount() != null
                            ? memberAccountService.update(dto.getAccount()).orElse(new MemberAccount())
                            : memberAccountService.findByMemberId(updatedMember.getId()).orElse(null);

                    MemberContactInformation updatedContactInfo = handleContactInformationUpdate(dto, updatedMember);
                    Membership updatedMembership = handleMembershipUpdate(dto, updatedMember);

                    MemberEditDTO result = MemberMapper.mergeMemberInformationToEditDto(updatedMember, updatedAccount, updatedContactInfo);
                    result.setMembership(MembershipMapper.mapMembershipToEditDto(updatedMembership));
                    return result;
                });
    }

    private MemberContactInformation handleContactInformationUpdate(MemberEditDTO dto, Member member) {
        if (dto.isWithContactInformation() && dto.getContact() != null) {
            if (dto.getContact().getId() != null) {
                return memberContactInformationService.updateContactInformation(dto.getContact()).orElse(new MemberContactInformation());
            } else {
                return memberContactInformationService.create(
                        MemberMapper.createDtoForNewContactInformationInUpdateProcess(member, dto.getContact())
                ).orElse(null);
            }
        } else {
            return memberContactInformationService.findByMemberId(member.getId()).orElse(null);
        }
    }

    private Membership handleMembershipUpdate(MemberEditDTO dto, Member member) {
        if (dto.isWithMembershipDetails() && dto.getMembership() != null) {
            if (dto.getMembership().getId() != null) {
                return membershipService.updateMembership(dto.getMembership()).orElse(new Membership());
            } else {
                return membershipService.createMembership(
                        MembershipMapper.createDtoForNewMembershipInUpdateProcess(member, dto.getMembership())
                ).orElse(null);
            }
        } else {
            return membershipService.findActiveByMemberId(member.getId()).orElse(null);
        }
    }

    @Override
    public void deleteMember(Long memberId) {
        checkPermission(SecurityConstants.Privileges.DELETE_MEMBER_ACCOUNT, "You have no permission to delete members.");
        memberRepository.deleteById(memberId);
    }

    @Override
    public long countMembers(String searchTerm) {
        return memberRepository.countWithSearchTerm(searchTerm);
    }

    private void checkPermission(String privilege, String errorMessage) {
        if (!authenticationContext.hasAuthority(privilege)) {
            throw new AccessDeniedException(errorMessage);
        }
    }

}
