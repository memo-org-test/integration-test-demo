package com.document.example.entity.model;

import com.document.example.commons.DocumentState;
import lombok.*;
import lombok.experimental.Wither;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
@Entity
@Data
@ToString
public class UserDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NotNull
    private UUID id;

    @NotNull
    @EqualsAndHashCode.Include
    private UUID userId;

    @Column
    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Column
    @Wither
    @Enumerated(EnumType.STRING)
    private DocumentState state;

    @Column
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        state = DocumentState.REQUESTED;
    }
}
