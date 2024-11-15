package com.blc.kpiReport.schema.ghl;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "sales_person_conversation")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SalesPersonConversation extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @OneToMany(mappedBy = "salesPersonConversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<ConversationMessage> conversationMessages;

    @Column(name = "last_manual_message_date")
    private Instant lastManualMessageDate;

    @Size(max = 500)
    @Column(name = "last_message_type", length = 500)
    private String lastMessageType;

    @ManyToOne
    @JoinColumn(name = "ghl_user_id")
    private GhlUser ghlUser;

    @OneToOne
    @JoinColumn(name = "ghl_contact_id")
    private GhlContact ghlContact;

    @ManyToOne
    @JoinColumn(name = "go_high_level_report_id", nullable = false)
    @JsonBackReference
    private GoHighLevelReport goHighLevelReport;
}
