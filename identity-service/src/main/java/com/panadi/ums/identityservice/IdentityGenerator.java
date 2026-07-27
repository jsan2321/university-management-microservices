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
    synchronized IdentityBundle next(String role) {
        int year = Year.now().getValue();
        String prefix = "STUDENT".equals(role) ? "STU" : "TCH";
        String key = prefix + "-" + year;
        IdentifierSequence sequence = sequences.findById(key).orElseGet(() -> { IdentifierSequence value = new IdentifierSequence(); value.sequenceKey = key; value.nextValue = 0; return value; });
        sequence.nextValue++;
        sequences.save(sequence);
        String code = "%s-%d-%0" + width + "d";
        code = String.format(Locale.ROOT, code, prefix, year, sequence.nextValue);
        String username = code.toLowerCase(Locale.ROOT).replace("-", "");
        return new IdentityBundle(code, username, username + "@" + domain);
    }

    record IdentityBundle(String code, String username, String universityEmail) {}
}
