package com.grosshaeuser.olmp.rest_api_sru;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sru")
public class SruController {
    @GetMapping("/protected")
    public String sru() {
        return "Not yet implemented.";
    }
}
