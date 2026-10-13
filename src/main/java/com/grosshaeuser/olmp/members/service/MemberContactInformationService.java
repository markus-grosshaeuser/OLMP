package com.grosshaeuser.olmp.members.service;

import com.grosshaeuser.olmp.members.dto.MemberContactInformationCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberContactInformationEditDTO;
import com.grosshaeuser.olmp.members.entity.MemberContactInformation;

import java.util.Optional;

public interface MemberContactInformationService {
    Optional<MemberContactInformation> create(MemberContactInformationCreateDTO dto);
    Optional<MemberContactInformation> findByMemberId(Long memberId);

    Optional<MemberContactInformation> updateContactInformation(MemberContactInformationEditDTO dto);
}
