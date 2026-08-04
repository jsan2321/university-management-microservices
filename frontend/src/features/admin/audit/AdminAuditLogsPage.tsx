import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Eye, ShieldAlert } from "lucide-react";
import { useServiceApi } from "../../../api/use-service-api";
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

export function AdminAuditLogsPage() {
  const [producerFilter, setProducerFilter] = useState<string>("");
  const [eventTypeFilter, setEventTypeFilter] = useState<string>("");
  const [selectedAudit, setSelectedAudit] = useState<AuditRecord | null>(null);

  const api = useServiceApi();
  const query = useQuery({
    queryKey: ["admin", "audit-logs", producerFilter, eventTypeFilter],
    queryFn: async () => {
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
    <div style={{ display: "flex", flexDirection: "column", gap: "32px", paddingBottom: "40px" }}>
      <PageHeader
        eyebrow="System Security & Auditing"
        title="Audit Logs"
        description="View real-time, immutable audit events generated across all university microservices via Kafka."
      />

      <div style={{ position: "relative" }}>
        {query.isLoading && (
          <div style={{ padding: "40px 0" }}>
            <LoadingState label="Fetching latest audit logs..." />
          </div>
        )}
        
        {query.isError && (
          <div style={{ padding: "40px 0" }}>
            <ErrorState message="Failed to load audit logs from audit-service. Please ensure the service is running." />
          </div>
        )}
        
        {query.isSuccess && (
          <Panel
            title="Event Records"
            description="Recent security and system events."
            action={
              <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
                <select
                  aria-label="Producer Service Filter"
                  value={producerFilter}
                  onChange={(e) => setProducerFilter(e.target.value)}
                  className={uiStyles.select}
                  style={{ minWidth: "180px", cursor: "pointer", background: "var(--paper-50)" }}
                >
                  <option value="">All Services</option>
                  <option value="identity-service">identity-service</option>
                  <option value="enrollment-service">enrollment-service</option>
                  <option value="attendance-service">attendance-service</option>
                  <option value="assignment-service">assignment-service</option>
                  <option value="academic-service">academic-service</option>
                </select>
                <select
                  aria-label="Event Action Filter"
                  value={eventTypeFilter}
                  onChange={(e) => setEventTypeFilter(e.target.value)}
                  className={uiStyles.select}
                  style={{ minWidth: "180px", cursor: "pointer", background: "var(--paper-50)" }}
                >
                  <option value="">All Actions</option>
                  <option value="StudentProvisioned">Student Provisioned</option>
                  <option value="TeacherProvisioned">Teacher Provisioned</option>
                  <option value="EnrollmentCreated">Enrollment Created</option>
                  <option value="AttendanceRecorded">Attendance Recorded</option>
                  <option value="GradeReleased">Grade Released</option>
                </select>
              </div>
            }
          >
            {query.data.content.length === 0 ? (
              <EmptyState
                icon={ShieldAlert}
                title="No Audit Logs Found"
                description="No security or system events match your current filter criteria."
              />
            ) : (
              <DataTable>
                <thead>
                  <tr>
                    <th>Timestamp</th>
                    <th>Service</th>
                    <th>Event Action</th>
                    <th>Target Entity</th>
                    <th>Target ID</th>
                    <th style={{ textAlign: "right" }}>Details</th>
                  </tr>
                </thead>
                <tbody>
                  {query.data.content.map((item) => (
                    <tr key={item.eventId} style={{ transition: "background 0.2s ease" }}>
                      <td>
                        <PrimaryCell
                          title={new Date(item.occurredAt).toLocaleString()}
                          subtitle={`Event ID: ${item.eventId.substring(0, 8)}...`}
                        />
                      </td>
                      <td>
                        <StatusBadge value={item.producer} />
                      </td>
                      <td>
                        <strong style={{ color: "var(--blue-700)", fontSize: "13px", letterSpacing: "0.02em" }}>
                          {item.eventType}
                        </strong>
                      </td>
                      <td>
                        <span style={{ fontWeight: 500, color: "var(--ink-700)" }}>{item.aggregateType}</span>
                      </td>
                      <td>
                        <span style={{ 
                          fontFamily: "var(--font-mono, monospace)", 
                          fontSize: "12px", 
                          background: "var(--paper-100)",
                          padding: "4px 8px",
                          borderRadius: "4px",
                          color: "var(--ink-800)"
                        }}>
                          {item.aggregateId ? `${item.aggregateId.substring(0, 13)}...` : "N/A"}
                        </span>
                      </td>
                      <td style={{ textAlign: "right" }}>
                        <Button 
                          variant="secondary" 
                          onClick={() => setSelectedAudit(item)}
                          style={{ fontSize: "12px", padding: "6px 12px" }}
                        >
                          <Eye size={14} style={{ opacity: 0.7 }} />
                          Inspect
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </DataTable>
            )}
          </Panel>
        )}
      </div>

      {selectedAudit && (
        <Dialog
          onClose={() => setSelectedAudit(null)}
          title={`Inspect Event: ${selectedAudit.eventType}`}
          description={`Detailed view for Event ID: ${selectedAudit.eventId}`}
        >
          <div style={{ display: "flex", flexDirection: "column", gap: "24px" }}>
            <div style={{ background: "var(--paper-50)", border: "1px solid var(--line-200)", borderRadius: "8px", padding: "16px" }}>
              <strong style={{ display: "block", marginBottom: "12px", color: "var(--ink-950)", fontSize: "14px", textTransform: "uppercase", letterSpacing: "0.05em" }}>Event Metadata</strong>
              <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: "12px", fontSize: "13px" }}>
                <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                  <span style={{ color: "var(--ink-500)", fontWeight: 600 }}>Producer</span>
                  <StatusBadge value={selectedAudit.producer} />
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                  <span style={{ color: "var(--ink-500)", fontWeight: 600 }}>Aggregate Type</span>
                  <span style={{ color: "var(--ink-950)", fontWeight: 500 }}>{selectedAudit.aggregateType}</span>
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                  <span style={{ color: "var(--ink-500)", fontWeight: 600 }}>Aggregate ID</span>
                  <code style={{ background: "var(--paper-100)", padding: "2px 6px", borderRadius: "4px" }}>{selectedAudit.aggregateId || "N/A"}</code>
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                  <span style={{ color: "var(--ink-500)", fontWeight: 600 }}>Actor ID</span>
                  <code style={{ background: "var(--paper-100)", padding: "2px 6px", borderRadius: "4px" }}>{selectedAudit.actorId || "System"}</code>
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                  <span style={{ color: "var(--ink-500)", fontWeight: 600 }}>Occurred At</span>
                  <span style={{ color: "var(--ink-950)", fontWeight: 500 }}>{new Date(selectedAudit.occurredAt).toUTCString()}</span>
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                  <span style={{ color: "var(--ink-500)", fontWeight: 600 }}>Trace ID</span>
                  <code style={{ background: "var(--paper-100)", padding: "2px 6px", borderRadius: "4px" }}>{selectedAudit.traceId || "N/A"}</code>
                </div>
              </div>
            </div>

            <div>
              <strong style={{ display: "block", marginBottom: "8px", color: "var(--ink-950)", fontSize: "14px", textTransform: "uppercase", letterSpacing: "0.05em" }}>Decoded Payload</strong>
              <div style={{ 
                background: "#0f172a", 
                borderRadius: "8px", 
                overflow: "hidden",
                border: "1px solid #1e293b",
                boxShadow: "inset 0 2px 4px rgba(0,0,0,0.2)"
              }}>
                <div style={{ background: "#1e293b", padding: "8px 16px", borderBottom: "1px solid #334155", color: "#94a3b8", fontSize: "11px", fontWeight: 600, letterSpacing: "0.05em", textTransform: "uppercase" }}>JSON Data</div>
                <pre
                  style={{
                    color: "#e2e8f0",
                    padding: "16px",
                    margin: 0,
                    fontSize: "13px",
                    lineHeight: "1.5",
                    fontFamily: "var(--font-mono, monospace)",
                    overflowX: "auto",
                  }}
                >
                  {formatPayload(selectedAudit.payload)}
                </pre>
              </div>
            </div>

            <div className={uiStyles.actions} style={{ marginTop: "8px" }}>
              <Button onClick={() => setSelectedAudit(null)}>Close Inspector</Button>
            </div>
          </div>
        </Dialog>
      )}
    </div>
  );
}
