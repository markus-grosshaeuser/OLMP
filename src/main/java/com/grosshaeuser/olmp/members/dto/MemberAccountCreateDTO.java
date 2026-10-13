package com.grosshaeuser.olmp.members.dto;

import com.grosshaeuser.olmp.members.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberAccountCreateDTO {
    private Member member;
    private String email;
    private String password;
    private boolean verified = true;
    private boolean enabled = true;
}
