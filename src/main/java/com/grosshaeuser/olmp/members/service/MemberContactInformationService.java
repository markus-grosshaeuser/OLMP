package com.grosshaeuser.olmp.members.service;

import com.grosshaeuser.olmp.members.dto.MemberContactInformationCreateDTO;
import com.grosshaeuser.olmp.members.entity.MemberContactInformation;

import java.util.Optional;

public interface MemberContactInformationService {
    Optional<MemberContactInformation> create(MemberContactInformationCreateDTO dto);
}
