package com.leostar.wealthpilot.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leostar.wealthpilot.model.Fund;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FundDataRepository {

    private List<Fund> funds = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("data/funds.json");
        try (InputStream inputStream = resource.getInputStream()) {
            funds = mapper.readValue(inputStream, new TypeReference<List<Fund>>() {});
        }
    }

    public List<Fund> getAllFunds() {
        return funds;
    }
}
