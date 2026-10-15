package com.grosshaeuser.olmp.members.service.impl;

import com.grosshaeuser.olmp.members.dto.MemberContactInformationCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberContactInformationEditDTO;
import com.grosshaeuser.olmp.members.entity.MemberContactInformation;
import com.grosshaeuser.olmp.members.repository.MemberContactInformationRepository;
import com.grosshaeuser.olmp.members.service.MemberContactInformationService;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberContactInformationServiceImpl implements MemberContactInformationService {

    private final MemberContactInformationRepository memberContactInformationRepository;
    private final AuthenticationContext authenticationContext;

    @Override
    public Optional<MemberContactInformation> create(MemberContactInformationCreateDTO dto) {
        checkPermission(SecurityConstants.Privileges.CREATE_MEMBER_ACCOUNT, "You have no permission to create member contact information.");
        MemberContactInformation contactInformation = MemberContactInformation.builder()
                .member(dto.getMember())
                .phoneNumber(dto.getPhoneNumber())
                .street(dto.getStreet())
                .houseNumber(dto.getHouseNumber())
                .postalCode(dto.getPostalCode())
                .city(dto.getCity())
                .country(dto.getCountry())
                .build();
        return Optional.of(memberContactInformationRepository.save(contactInformation));
    }

    @Override
    public Optional<MemberContactInformation> findByMemberId(Long memberId) {
        checkPermission(SecurityConstants.Privileges.READ_MEMBER_ACCOUNT, "You have no permission to read member contact information.");
        return memberContactInformationRepository.findByMemberId(memberId);
    }

    @Override
    public Optional<MemberContactInformation> updateContactInformation(MemberContactInformationEditDTO dto) {
        checkPermission(SecurityConstants.Privileges.UPDATE_MEMBER_ACCOUNT, "You have no permission to update member contact information.");
        return memberContactInformationRepository.findById(dto.getId())
                .map(contactInformation -> {
                    contactInformation.setPhoneNumber(dto.getPhoneNumber());
                    contactInformation.setStreet(dto.getStreet());
                    contactInformation.setHouseNumber(dto.getHouseNumber());
                    contactInformation.setPostalCode(dto.getPostalCode());
                    contactInformation.setCity(dto.getCity());
                    contactInformation.setCountry(dto.getCountry());
                    return memberContactInformationRepository.save(contactInformation);
                });
    }

    private void checkPermission(String privilege, String errorMessage) {
        if (!authenticationContext.hasAuthority(privilege)) {
            throw new AccessDeniedException(errorMessage);
        }
    }
}
