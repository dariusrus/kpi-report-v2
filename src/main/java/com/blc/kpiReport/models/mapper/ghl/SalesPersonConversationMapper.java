package com.blc.kpiReport.models.mapper.ghl;

import com.blc.kpiReport.models.response.ghl.ConversationMessageResponse;
import com.blc.kpiReport.models.response.ghl.SalesPersonConversationResponse;
import com.blc.kpiReport.schema.ghl.ConversationMessage;
import com.blc.kpiReport.schema.ghl.SalesPersonConversation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

@Component
public class SalesPersonConversationMapper {

    public SalesPersonConversationResponse toResponse(SalesPersonConversation conversation) {
        if (conversation == null) {
            return null;
        }

        List<ConversationMessageResponse> messageResponses = conversation.getConversationMessages().stream()
                .sorted(Comparator.comparing(ConversationMessage::getDateAdded))
                .map(this::toMessageResponse)
                .collect(Collectors.toList());

        return SalesPersonConversationResponse.builder()
                .salesPersonId(conversation.getSalesPersonId())
                .salesPersonName(conversation.getSalesPersonName())
                .ownerPhotoUrl(conversation.getOwnerPhotoUrl())
                .contactId(conversation.getContactId())
                .contactName(conversation.getContactName())
                .contactEmail(conversation.getContactEmail())
                .contactPhone(conversation.getContact_phone())
                .lastManualMessageDate(conversation.getLastManualMessageDate())
                .lastMessageType(conversation.getLastMessageType())
                .conversationMessages(messageResponses)
                .build();
    }

    public List<SalesPersonConversationResponse> toResponseList(List<SalesPersonConversation> conversations) {
        return conversations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ConversationMessageResponse toMessageResponse(ConversationMessage message) {
        return ConversationMessageResponse.builder()
                .messageType(message.getMessageType())
                .direction(message.getDirection())
                .status(message.getStatus())
                .callDuration(message.getCallDuration())
                .messageBody(message.getMessageBody())
                .dateAdded(message.getDateAdded())
                .build();
    }
}