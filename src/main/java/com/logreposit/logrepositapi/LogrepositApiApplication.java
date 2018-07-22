package com.logreposit.logrepositapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {RepositoryRestMvcAutoConfiguration.class, ErrorMvcAutoConfiguration.class})
public class LogrepositApiApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(LogrepositApiApplication.class, args);
    }
}
