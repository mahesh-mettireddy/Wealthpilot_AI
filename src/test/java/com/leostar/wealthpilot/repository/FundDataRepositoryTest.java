package com.leostar.wealthpilot.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class FundDataRepositoryTest {

    @Autowired
    private FundDataRepository fundDataRepository;

    @Test
    public void testFundsLoadedAtStartup() {
        assertNotNull(fundDataRepository.getAllFunds(), "Funds list should not be null");
        assertFalse(fundDataRepository.getAllFunds().isEmpty(), "Funds list should not be empty");
        
        // Assert minimum 15 funds as per Data Model
        assert(fundDataRepository.getAllFunds().size() >= 15);
    }
}
