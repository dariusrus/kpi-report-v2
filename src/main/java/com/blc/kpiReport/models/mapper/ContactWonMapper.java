package com.blc.kpiReport.models.mapper;

import com.blc.kpiReport.models.response.ContactsWonResponse;
import com.blc.kpiReport.schema.ContactWon;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContactWonMapper {

    public ContactsWonResponse toResponse(ContactWon contactWon) {
        return ContactsWonResponse.builder()
            .contactName(contactWon.getContactName())
            .contactEmail(contactWon.getContactEmail())
            .build();
    }

    public List<ContactsWonResponse> toResponseList(List<ContactWon> contactsWon) {
        return contactsWon.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}