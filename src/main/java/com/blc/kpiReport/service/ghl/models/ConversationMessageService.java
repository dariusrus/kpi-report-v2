package com.blc.kpiReport.service.ghl.models;

import com.blc.kpiReport.repository.ghl.ConversationMessageRepository;
import com.blc.kpiReport.repository.ghl.SalesPersonConversationRepository;
import com.blc.kpiReport.schema.ghl.ConversationMessage;
import com.blc.kpiReport.schema.ghl.SalesPersonConversation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationMessageService {

    private final ConversationMessageRepository repository;

    public ConversationMessageService(ConversationMessageRepository repository) {
        this.repository = repository;
    }

    public List<ConversationMessage> saveAll(List<ConversationMessage> conversationMessages) {
        return repository.saveAll(conversationMessages);
    }
}
