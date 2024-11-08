 package com.blc.kpiReport.repository.ghl;

import com.blc.kpiReport.schema.ghl.ConversationMessage;
import com.blc.kpiReport.schema.ghl.SalesPersonConversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {
}
