package com.blc.kpiReport.schema.ghl;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @Size(max = 500)
    @Column(name = "sales_person_id", length = 500)
    private String salesPersonId;

    @NotNull
    @Size(max = 500)
    @Column(name = "sales_person_name", length = 500)
    private String salesPersonName;

    @Size(max = 5000)
    @Column(name = "owner_photo_url", length = 500)
    private String ownerPhotoUrl;

    @NotNull
    @Size(max = 500)
    @Column(name = "contact_id", length = 500)
    private String contactId;

    @NotNull
    @Size(max = 500)
    @Column(name = "contact_name", length = 500)
    private String contactName;

    @NotNull
    @Size(max = 500)
    @Column(name = "contact_email", length = 500)
    private String contactEmail;

    @NotNull
    @Size(max = 500)
    @Column(name = "contact_phone", length = 500)
    private String contact_phone;

    @OneToMany(mappedBy = "salesPersonConversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<ConversationMessage> conversationMessages;

    @Column(name = "last_manual_message_date")
    private Instant lastManualMessageDate;

    @Size(max = 500)
    @Column(name = "last_message_type", length = 500)
    private String lastMessageType;

    @ManyToOne
    @JoinColumn(name = "go_high_level_report_id", nullable = false)
    @JsonBackReference
    private GoHighLevelReport goHighLevelReport;
}
