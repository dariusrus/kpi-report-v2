package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.repository.ghl.SalesPersonConversionRepository;
import com.blc.kpiReport.schema.ghl.SalesPersonConversion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SalesPersonConversionService {

    private final SalesPersonConversionRepository salesPersonConversionRepository;

    @Transactional
    public SalesPersonConversion saveOrUpdate(SalesPersonConversion salesPersonConversion) {
        return salesPersonConversionRepository.save(salesPersonConversion);
    }

    @Transactional
    public List<SalesPersonConversion> saveAll(List<SalesPersonConversion> salesPersonConversions) {
        return salesPersonConversionRepository.saveAll(salesPersonConversions);
    }

    public Optional<SalesPersonConversion> findById(Long id) {
        return salesPersonConversionRepository.findById(id);
    }

    public void delete(SalesPersonConversion salesPersonConversion) {
        salesPersonConversionRepository.delete(salesPersonConversion);
    }
}