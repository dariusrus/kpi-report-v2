package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.schema.ghl.GhlUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GhlUserRepository extends JpaRepository<GhlUser, Long> {
    Optional<GhlUser> findByUserId(String userId);

    List<GhlUser> findByGhlLocationId(Long ghlLocationId);
}
