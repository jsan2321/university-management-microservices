package com.panadi.ums.identityservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.Locale;

@Service
class IdentityGenerator {
    private final IdentifierSequenceRepository sequences;
    private final String domain;
    private final int width;

    IdentityGenerator(IdentifierSequenceRepository sequences,
                      @Value("${ums.identity.institution-domain:ums.local}") String domain,
                      @Value("${ums.identity.sequence-width:5}") int width) {
        this.sequences = sequences; this.domain = domain; this.width = width;
    }

    @Transactional
    synchronized IdentityBundle next(String role, String firstName, String lastName) {
        int year = Year.now().getValue();
        String prefix = "STUDENT".equals(role) ? "STU" : "TCH";
        
        // 1. Generate Academic Code
        String codeKey = prefix + "-" + year;
        IdentifierSequence sequence = sequences.findById(codeKey).orElseGet(() -> { IdentifierSequence value = new IdentifierSequence(); value.sequenceKey = codeKey; value.nextValue = 0; return value; });
        sequence.nextValue++;
        sequences.save(sequence);
        String code = "%s-%d-%0" + width + "d";
        code = String.format(Locale.ROOT, code, prefix, year, sequence.nextValue);

        // 2. Generate friendly Username
        String rolePrefix = "STUDENT".equals(role) ? "s." : "t.";
        String cleanFirstName = firstName.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        String cleanLastName = lastName.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        String baseName = rolePrefix + cleanFirstName + "." + cleanLastName;

        IdentifierSequence nameSeq = sequences.findById(baseName).orElseGet(() -> { IdentifierSequence value = new IdentifierSequence(); value.sequenceKey = baseName; value.nextValue = 0; return value; });
        String username;
        if (nameSeq.nextValue == 0) {
            username = baseName;
            nameSeq.nextValue++;
        } else {
            username = baseName + nameSeq.nextValue;
            nameSeq.nextValue++;
        }
        sequences.save(nameSeq);

        return new IdentityBundle(code, username, username + "@" + domain);
    }

    record IdentityBundle(String code, String username, String universityEmail) {}
}
