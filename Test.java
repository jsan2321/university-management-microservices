package com.example;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;
import java.sql.Timestamp;
import java.time.Instant;
public class Test {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/identity_db", "postgres", "postgres");
        PreparedStatement ps = conn.prepareStatement("insert into audit_outbox (event_id, event_type, payload, occurred_at) values (?, ?, cast(? as jsonb), ?)");
        ps.setObject(1, UUID.randomUUID());
        ps.setString(2, "Test");
        ps.setString(3, "{}");
        ps.setObject(4, Instant.now());
        ps.executeUpdate();
        System.out.println("Success!");
    }
}
