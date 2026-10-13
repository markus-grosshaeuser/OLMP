package com.grosshaeuser.olmp.members.service.impl;

import com.grosshaeuser.olmp.members.dto.MemberContactInformationCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberContactInformationEditDTO;
import com.grosshaeuser.olmp.members.entity.MemberContactInformation;
import com.grosshaeuser.olmp.members.repository.MemberContactInformationRepository;
import com.grosshaeuser.olmp.members.service.MemberContactInformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberContactInformationServiceImpl implements MemberContactInformationService {

    private final MemberContactInformationRepository memberContactInformationRepository;

    @Override
    public Optional<MemberContactInformation> create(MemberContactInformationCreateDTO dto) {
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
        return memberContactInformationRepository.findByMemberId(memberId);
    }

    @Override
    public Optional<MemberContactInformation> updateContactInformation(MemberContactInformationEditDTO dto) {
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
}
