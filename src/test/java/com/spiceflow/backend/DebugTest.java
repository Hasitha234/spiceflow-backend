package com.spiceflow.backend;

import com.spiceflow.backend.auth.controller.TenantUserController;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class DebugTest {

    @Autowired
    private TenantUserController tenantUserController;

    @Test
    public void testGetDriverUsers() {
        AuthenticatedUser user = AuthenticatedUser.builder()
            .id(1L)
            .email("hasitha@bussmanager.com")
            .tenantId(1L)
            .build();
            
        try {
            System.out.println("--- CALLING GET DRIVERS ---");
            tenantUserController.getDriverUsers(user);
            System.out.println("--- SUCCESS ---");
        } catch (Exception e) {
            System.out.println("--- EXCEPTION ---");
            e.printStackTrace();
        }
    }
}
