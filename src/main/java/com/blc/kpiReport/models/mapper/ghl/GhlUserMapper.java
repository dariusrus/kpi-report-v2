package com.blc.kpiReport.models.mapper.ghl;

import com.blc.kpiReport.models.response.ghl.GhlUserResponse;
import com.blc.kpiReport.schema.ghl.GhlUser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GhlUserMapper {

    public GhlUserResponse toResponse(GhlUser salesPerson) {
        if (salesPerson == null) {
            return null;
        }

        return GhlUserResponse.builder()
                .userId(salesPerson.getUserId())
                .name(salesPerson.getName())
                .photoUrl(salesPerson.getPhotoUrl())
                .build();
    }

    public List<GhlUserResponse> toResponseList(List<GhlUser> salesPersons) {
        return salesPersons.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}