package com.panadi.ums.identityservice;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "identifier_sequences")
class IdentifierSequence {
    @Id String sequenceKey;
    long nextValue;
}
