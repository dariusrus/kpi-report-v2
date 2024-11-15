package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.repository.ghl.GhlUserRepository;
import com.blc.kpiReport.schema.ghl.GhlUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GhlUserService {

    private final GhlUserRepository salesPersonRepository;

    public Optional<GhlUser> findByUserId(String userId) {
        return salesPersonRepository.findByUserId(userId);
    }

    public GhlUser saveOrUpdate(GhlUser ghlUser) {
        return salesPersonRepository.save(ghlUser);
    }

    public List<GhlUser> findByGhlLocationId(Long ghlLocationId) {
        return salesPersonRepository.findByGhlLocationId(ghlLocationId);
    }
}
