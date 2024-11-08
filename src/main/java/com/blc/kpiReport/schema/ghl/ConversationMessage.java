package com.blc.kpiReport.schema.ghl;

import com.blc.kpiReport.schema.shared.DateAudit;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "conversation_message")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ConversationMessage extends DateAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @JsonIgnore
    private Long id;

    @NotNull
    @Size(max = 500)
    @Column(name = "message_type", length = 500)
    private String messageType;

    @Size(max = 500)
    @Column(name = "direction", length = 500)
    private String direction;

    @Size(max = 500)
    @Column(name = "status", length = 500)
    private String status;

    @Column(name = "callDuration")
    private int callDuration;

    @Column(name = "date_added")
    private Instant dateAdded;

    @Column(name = "message_body", length = 5000)
    private String messageBody;

    @ManyToOne
    @JoinColumn(name = "sales_person_conversation_id", nullable = false)
    @JsonBackReference
    private SalesPersonConversation salesPersonConversation;
}
