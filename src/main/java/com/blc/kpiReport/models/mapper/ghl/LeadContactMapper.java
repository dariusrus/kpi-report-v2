package com.blc.kpiReport.models.mapper.ghl;

import com.blc.kpiReport.models.response.ghl.LeadContactResponse;
import com.blc.kpiReport.schema.ghl.LeadContact;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LeadContactMapper {

    public LeadContactResponse toResponse(LeadContact leadContact) {
        if (leadContact == null) {
            return null;
        }
        return LeadContactResponse.builder()
            .contactName(formatString(leadContact.getContactName()))
            .contactSource(formatString(leadContact.getContactSource()))
            .createdBySource(formatString(leadContact.getCreatedBySource()))
            .attributionSource(leadContact.getAttributionSource())
            .attributionMedium(formatString(leadContact.getAttributionMedium()))
            .dateAdded(leadContact.getDateAdded())
            .ownerName(leadContact.getOwnerName())
            .ownerPhotoUrl(leadContact.getOwnerPhotoUrl())
            .status(leadContact.getStatus())
            .build();
    }

    public List<LeadContactResponse> toResponseList(List<LeadContact> leadContacts) {
        return leadContacts.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private String formatString(String input) {
        if (input == null || input.length() == 0 || input.trim().isEmpty()) {
            return "-";
        }
        try {
            return Arrays.stream(input.split("[_\\s]+"))
                .filter(word -> !word.isEmpty())
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
        } catch (Exception e) {
            return input;
        }
    }
}