import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Eye, ShieldAlert, Filter } from "lucide-react";
import { useServiceApi } from "../../../api/use-service-api";
import { useAuth } from "../../../auth/AuthProvider";
import type { AuditRecord } from "../../../api/generated/contracts";
import {
  Button,
  DataTable,
  Dialog,
  EmptyState,
  ErrorState,
  LoadingState,
  PageHeader,
  Panel,
  PrimaryCell,
  StatusBadge,
  uiStyles,
} from "../../../components/ui";

const sampleAuditRecords: AuditRecord[] = [
  {
    eventId: "e1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    eventType: "StudentProvisioned",
    producer: "identity-service",
    aggregateType: "STUDENT",
    aggregateId: "a1029384-b5c6-d7e8-f901-234567890abc",
    actorId: "99008877-6655-4433-2211-001122334455",
    occurredAt: new Date().toISOString(),
    traceId: "trace-987654321",
    payload: JSON.stringify(
      {
        userId: "99008877-6655-4433-2211-001122334455",
        profileId: "a1029384-b5c6-d7e8-f901-234567890abc",
        status: "COMPLETED",
      },
      null,
      2
    ),
  },
  {
    eventId: "f2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e",
    eventType: "TeacherProvisioned",
    producer: "identity-service",
    aggregateType: "TEACHER",
    aggregateId: "b2039485-c6d7-e8f9-0123-4567890abc12",
    actorId: "99008877-6655-4433-2211-001122334455",
    occurredAt: new Date(Date.now() - 3600000).toISOString(),
    traceId: "trace-123456789",
    payload: JSON.stringify(
      {
        userId: "99008877-6655-4433-2211-001122334455",
        profileId: "b2039485-c6d7-e8f9-0123-4567890abc12",
        departmentId: "d1234567-89ab-cdef-0123-456789abcdef",
      },
      null,
      2
    ),
  },
];

export function AdminAuditLogsPage() {
  const [producerFilter, setProducerFilter] = useState<string>("");
  const [eventTypeFilter, setEventTypeFilter] = useState<string>("");
  const [selectedAudit, setSelectedAudit] = useState<AuditRecord | null>(null);

  const api = useServiceApi();
  const { session } = useAuth();

  const query = useQuery({
    queryKey: ["admin", "audit-logs", producerFilter, eventTypeFilter],
    queryFn: async () => {
      if (session?.demo) {
        let list = [...sampleAuditRecords];
        if (producerFilter) list = list.filter((r) => r.producer === producerFilter);
        if (eventTypeFilter) list = list.filter((r) => r.eventType === eventTypeFilter);
        return {
          content: list,
          page: 0,
          size: 20,
          totalElements: list.length,
          totalPages: 1,
        };
      }
      return api.audits(0, 50, producerFilter || undefined, eventTypeFilter || undefined);
    },
  });

  const formatPayload = (rawPayload: string) => {
    try {
      const parsed = JSON.parse(rawPayload);
      return JSON.stringify(parsed, null, 2);
    } catch {
      return rawPayload;
    }
  };

  return (
    <>
      <PageHeader
        eyebrow="System Security & Auditing"
        title="Audit Logs"
        description="View real-time, immutable audit events generated across all university microservices via Kafka."
      />

      <Panel title="Filters">
        <div style={{ display: "flex", gap: "1rem", flexWrap: "wrap", alignItems: "center" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
            <Filter size={16} />
            <span className={uiStyles.label}>Producer Service:</span>
            <select
              value={producerFilter}
              onChange={(e) => setProducerFilter(e.target.value)}
              className={uiStyles.select}
              style={{ minWidth: "180px" }}
            >
              <option value="">All Services</option>
              <option value="identity-service">identity-service</option>
              <option value="enrollment-service">enrollment-service</option>
              <option value="attendance-service">attendance-service</option>
              <option value="assignment-service">assignment-service</option>
              <option value="academic-service">academic-service</option>
            </select>
          </div>

          <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
            <span className={uiStyles.label}>Event Action:</span>
            <select
              value={eventTypeFilter}
              onChange={(e) => setEventTypeFilter(e.target.value)}
              className={uiStyles.select}
              style={{ minWidth: "180px" }}
            >
              <option value="">All Actions</option>
              <option value="StudentProvisioned">Student Provisioned</option>
              <option value="TeacherProvisioned">Teacher Provisioned</option>
              <option value="EnrollmentCreated">Enrollment Created</option>
              <option value="AttendanceRecorded">Attendance Recorded</option>
              <option value="GradeReleased">Grade Released</option>
            </select>
          </div>
        </div>
      </Panel>

      {query.isLoading && <LoadingState />}
      {query.isError && <ErrorState message="Failed to load audit logs from audit-service." />}
      {query.isSuccess && (
        <>
          {query.data.content.length === 0 ? (
            <EmptyState
              icon={ShieldAlert}
              title="No Audit Logs Found"
              description="No security or system events match your criteria."
            />
          ) : (
            <DataTable
              headers={["Timestamp", "Service", "Event Action", "Target Entity", "Target ID", "Details"]}
              rows={query.data.content.map((item) => (
                <tr key={item.eventId}>
                  <td>
                    <PrimaryCell
                      title={new Date(item.occurredAt).toLocaleString()}
                      subtitle={`ID: ${item.eventId.substring(0, 8)}...`}
                    />
                  </td>
                  <td>
                    <StatusBadge status="ACTIVE">{item.producer}</StatusBadge>
                  </td>
                  <td>
                    <strong style={{ color: "var(--color-primary, #0f172a)" }}>{item.eventType}</strong>
                  </td>
                  <td>{item.aggregateType}</td>
                  <td>
                    <span style={{ fontFamily: "monospace", fontSize: "0.85rem", opacity: 0.8 }}>
                      {item.aggregateId ? `${item.aggregateId.substring(0, 13)}...` : "—"}
                    </span>
                  </td>
                  <td>
                    <Button variant="secondary" onClick={() => setSelectedAudit(item)}>
                      <Eye size={15} />
                      View Payload
                    </Button>
                  </td>
                </tr>
              ))}
            />
          )}
        </>
      )}

      {selectedAudit && (
        <Dialog
          onClose={() => setSelectedAudit(null)}
          title={`Audit Event Details: ${selectedAudit.eventType}`}
        >
          <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
            <div>
              <strong>Event Metadata</strong>
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0.5rem", marginTop: "0.5rem", fontSize: "0.9rem" }}>
                <div><strong>Event ID:</strong> <code style={{ wordBreak: "break-all" }}>{selectedAudit.eventId}</code></div>
                <div><strong>Producer:</strong> <code>{selectedAudit.producer}</code></div>
                <div><strong>Event Type:</strong> <code>{selectedAudit.eventType}</code></div>
                <div><strong>Aggregate Type:</strong> <code>{selectedAudit.aggregateType}</code></div>
                <div><strong>Aggregate ID:</strong> <code style={{ wordBreak: "break-all" }}>{selectedAudit.aggregateId || "N/A"}</code></div>
                <div><strong>Actor ID:</strong> <code style={{ wordBreak: "break-all" }}>{selectedAudit.actorId || "System"}</code></div>
                <div><strong>Occurred At:</strong> {new Date(selectedAudit.occurredAt).toUTCString()}</div>
                <div><strong>Trace ID:</strong> <code>{selectedAudit.traceId || "N/A"}</code></div>
              </div>
            </div>

            <div>
              <strong>Event Payload (JSON)</strong>
              <pre
                style={{
                  background: "#0f172a",
                  color: "#f8fafc",
                  padding: "1rem",
                  borderRadius: "8px",
                  fontSize: "0.85rem",
                  overflowX: "auto",
                  marginTop: "0.5rem",
                }}
              >
                {formatPayload(selectedAudit.payload)}
              </pre>
            </div>

            <div style={{ display: "flex", justifyContent: "flex-end", marginTop: "1rem" }}>
              <Button onClick={() => setSelectedAudit(null)}>Close</Button>
            </div>
          </div>
        </Dialog>
      )}
    </>
  );
}
