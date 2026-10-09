package com.grosshaeuser.olmp.ui.external_ui_rest_api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/members/")
@RequiredArgsConstructor
public class MemberController {

    @GetMapping()
    public String get() {
        return "Dummy";
    }

    @RequestMapping(value = {"*"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public String handleAllUnmatched() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
    }
}
