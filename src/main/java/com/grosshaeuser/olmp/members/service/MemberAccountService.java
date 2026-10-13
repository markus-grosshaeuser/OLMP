package com.grosshaeuser.olmp.members.service;

import com.grosshaeuser.olmp.members.dto.MemberAccountCreateDTO;
import com.grosshaeuser.olmp.members.entity.MemberAccount;

import java.util.Optional;

public interface MemberAccountService {
    Optional<MemberAccount> createMemberAccount(MemberAccountCreateDTO memberAccountCreateDTO);
}
