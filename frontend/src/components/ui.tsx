import type { ButtonHTMLAttributes, ReactNode } from "react";
import { AlertCircle, BookOpen, LoaderCircle, X } from "lucide-react";
import styles from "./ui.module.css";

export function Button({
  variant = "primary",
  className = "",
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "danger";
}) {
  return (
    <button
      className={`${styles.button} ${styles[variant]} ${className}`}
      {...props}
    />
  );
}
export function PageHeader({
  eyebrow,
  title,
  description,
  action,
}: {
  eyebrow: string;
  title: string;
  description: string;
  action?: ReactNode;
}) {
  return (
    <header className={styles.pageHeader}>
      <div>
        <span className={styles.eyebrow}>{eyebrow}</span>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>
      {action}
    </header>
  );
}
export function Panel({
  title,
  description,
  action,
  children,
}: {
  title?: string;
  description?: string;
  action?: ReactNode;
  children: ReactNode;
}) {
  return (
    <section className={styles.panel}>
      {(title || action) && (
        <div className={styles.panelHeader}>
          <div>
            {title && <h2>{title}</h2>}
            {description && <p>{description}</p>}
          </div>
          {action}
        </div>
      )}
      {children}
    </section>
  );
}
export function DataTable({ children }: { children: ReactNode }) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>{children}</table>
    </div>
  );
}
export function PrimaryCell({
  title,
  detail,
}: {
  title: ReactNode;
  detail?: ReactNode;
}) {
  return (
    <div className={styles.primaryCell}>
      <strong>{title}</strong>
      {detail && <small>{detail}</small>}
    </div>
  );
}
export function StatusBadge({ value }: { value: string }) {
  const tone = /active|present|published|graded|released|completed/i.test(value)
    ? "positive"
    : /suspend|absent|cancel|closed|inactive/i.test(value)
      ? "negative"
      : /late|draft|planned|upcoming/i.test(value)
        ? "warning"
        : "neutral";
  return (
    <span className={`${styles.status} ${styles[tone]}`}>
      {value.replaceAll("_", " ")}
    </span>
  );
}
export function Field({
  label,
  error,
  className = "",
  children,
}: {
  label: string;
  error?: string;
  className?: string;
  children: ReactNode;
}) {
  return (
    <label className={`${styles.field} ${className}`}>
      <span>{label}</span>
      {children}
      {error && <small className={styles.fieldError}>{error}</small>}
    </label>
  );
}
export function EmptyState({
  title,
  description,
  action,
}: {
  title: string;
  description: string;
  action?: ReactNode;
}) {
  return (
    <section className={styles.empty}>
      <span className={styles.emptyIcon}>
        <BookOpen size={22} />
      </span>
      <h2>{title}</h2>
      <p>{description}</p>
      {action}
    </section>
  );
}
export function LoadingState({
  label = "Loading university records…",
}: {
  label?: string;
}) {
  return (
    <section className={styles.loading} role="status">
      <LoaderCircle className={styles.spinner} aria-hidden="true" />
      <span>{label}</span>
    </section>
  );
}
export function ErrorState({
  error,
  retry,
}: {
  error: unknown;
  retry?: () => void;
}) {
  return (
    <section className={styles.error}>
      <AlertCircle size={25} />
      <h2>We couldn’t load this information</h2>
      <p>
        {error instanceof Error
          ? error.message
          : "An unexpected error occurred."}
      </p>
      {retry && (
        <Button variant="secondary" onClick={retry}>
          Try again
        </Button>
      )}
    </section>
  );
}
export function Dialog({
  title,
  description,
  onClose,
  children,
}: {
  title: string;
  description?: string;
  onClose: () => void;
  children: ReactNode;
}) {
  return (
    <div className={styles.dialogBackdrop} onMouseDown={onClose}>
      <section
        className={styles.dialog}
        role="dialog"
        aria-modal="true"
        aria-labelledby="dialog-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <header className={styles.dialogHeader}>
          <div>
            <h2 id="dialog-title">{title}</h2>
            {description && <p>{description}</p>}
          </div>
          <Button
            variant="secondary"
            className={styles.iconButton}
            onClick={onClose}
            aria-label="Close dialog"
          >
            <X size={18} />
          </Button>
        </header>
        <div className={styles.dialogBody}>{children}</div>
      </section>
    </div>
  );
}
export { styles as uiStyles };
