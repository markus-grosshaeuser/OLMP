package com.grosshaeuser.olmp;

import org.springframework.boot.SpringApplication;

public class TestOlmpApplication {

    public static void main(String[] args) {
        SpringApplication.from(OlmpApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
