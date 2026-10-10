package com.grosshaeuser.olmp.members.service;

import com.grosshaeuser.olmp.members.dto.MemberDTO;
import com.grosshaeuser.olmp.members.mapper.MemberMapper;
import com.grosshaeuser.olmp.members.repository.MemberContactInformationRepository;
import com.grosshaeuser.olmp.members.repository.MemberRepository;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
import com.grosshaeuser.olmp.security.SecurityConstants;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{
    private final MemberRepository memberRepository;
    private final MemberAccountRepository memberAccountRepository;
    private final MemberContactInformationRepository memberContactInformationRepository;
    private final AuthenticationContext authenticationContext;

    @Override
    public Slice<MemberDTO> getMembers(String searchTerm, Pageable pageable) {
        if (authenticationContext.hasAuthority(SecurityConstants.Privileges.READ_MEMBER_ACCOUNT)) {
            return memberRepository.search(searchTerm, pageable).map(MemberMapper::mapMemberToMemberDTO);
        }
        return new SliceImpl<>(List.of(), pageable, false);
    }
}
