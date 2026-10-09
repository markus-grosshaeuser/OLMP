package com.grosshaeuser.olmp.members.service;

import com.grosshaeuser.olmp.members.repository.MemberContactInformationRepository;
import com.grosshaeuser.olmp.members.repository.MemberRepository;
import com.grosshaeuser.olmp.members.repository.MemberAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberAccountRepository memberAccountRepository;
    private final MemberContactInformationRepository memberContactInformationRepository;

}
