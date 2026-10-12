package com.grosshaeuser.olmp.members.service;

import com.grosshaeuser.olmp.members.dto.MemberCreateDTO;
import com.grosshaeuser.olmp.members.dto.MemberDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MemberService {
    Page<MemberDTO> getMembers(String searchTerm, Pageable pageable);

    Optional<MemberDTO> createMember(MemberCreateDTO dto);
}
