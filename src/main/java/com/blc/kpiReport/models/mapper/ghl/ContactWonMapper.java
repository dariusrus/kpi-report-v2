package com.blc.kpiReport.models.mapper.ghl;

import com.blc.kpiReport.models.response.ghl.ContactsWonResponse;
import com.blc.kpiReport.schema.ghl.ContactWon;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContactWonMapper {

    public ContactsWonResponse toResponse(ContactWon contactWon) {
        return ContactsWonResponse.builder()
                .contactName(contactWon.getGhlContact() != null && contactWon.getGhlContact().getName() != null ? contactWon.getGhlContact().getName() : "")
                .contactEmail(contactWon.getGhlContact() != null && contactWon.getGhlContact().getEmail() != null ? contactWon.getGhlContact().getEmail() : "")
                .contactSource(contactWon.getSource() != null ? contactWon.getSource() : "")
                .attributionSource(contactWon.getAttributionSource() != null ? contactWon.getAttributionSource() : "")
                .build();
    }

    public List<ContactsWonResponse> toResponseList(List<ContactWon> contactsWon) {
        return contactsWon.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}
