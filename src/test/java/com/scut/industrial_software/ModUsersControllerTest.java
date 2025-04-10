package com.scut.industrial_software;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ModUsersControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /*@Test
    public void testGetAllUsers() {
        ResponseEntity<String> response = restTemplate.getForEntity("/modUsers", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }*/
}