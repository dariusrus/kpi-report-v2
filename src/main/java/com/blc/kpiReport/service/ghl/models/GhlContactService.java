package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.repository.ghl.GhlContactRepository;
import com.blc.kpiReport.schema.ghl.GhlContact;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GhlContactService {

    private final GhlContactRepository ghlContactRepository;

    public GhlContactService(GhlContactRepository ghlContactRepository) {
        this.ghlContactRepository = ghlContactRepository;
    }

    public GhlContact saveOrUpdate(GhlContact ghlContact) {
        return ghlContactRepository.save(ghlContact);
    }

    public List<GhlContact> saveAll(List<GhlContact> ghlContacts) {
        return ghlContactRepository.saveAll(ghlContacts);
    }

    public Optional<GhlContact> findById(Long id) {
        return ghlContactRepository.findById(id);
    }

    @Transactional
    public void deleteAll(List<GhlContact> contacts) {
        ghlContactRepository.deleteAll(contacts);
    }
}
