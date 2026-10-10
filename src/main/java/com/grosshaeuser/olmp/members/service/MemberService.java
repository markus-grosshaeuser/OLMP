package com.grosshaeuser.olmp.members.service;

import com.grosshaeuser.olmp.members.dto.MemberDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MemberService {
    Slice<MemberDTO> getMembers(String searchTerm, Pageable pageable);
}
