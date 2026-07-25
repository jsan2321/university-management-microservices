import { Navigate, Link } from "react-router-dom";
import { ShieldCheck } from "lucide-react";
import { useAuth } from "../auth/AuthProvider";
import { roleHome } from "../auth/session";
import { Brand } from "../app/AppShell";
import { Button, uiStyles } from "../components/ui";
import styles from "./SystemPages.module.css";
export function LandingPage() {
  const { session, login } = useAuth();
  if (session) return <Navigate to={roleHome[session.role]} replace />;
  return (
    <main className={styles.signin}>
      <section className={styles.intro}>
        <Brand />
        <div className={styles.introMessage}>
          <span>University operations, clearly connected</span>
          <h1>Your academic day, in one place.</h1>
          <p>
            Plan sections, support students, record attendance, and keep the
            semester moving without managing identities by hand.
          </p>
        </div>
        <footer className={styles.introFoot}>
          <span>Academic services</span>
          <span>Secure institutional access</span>
        </footer>
      </section>
      <section className={styles.signPanel}>
        <div className={styles.signCard}>
          <span className={styles.signIcon}>
            <ShieldCheck />
          </span>
          <span className={uiStyles.eyebrow}>University account</span>
          <h2>Welcome back</h2>
          <p>
            Continue with the account issued by your university. Your role
            determines the workspace you can access.
          </p>
          <Button onClick={login}>Sign in with university account</Button>
          <small>Your credentials are handled securely by Keycloak.</small>
        </div>
      </section>
    </main>
  );
}
export function UnauthorizedPage() {
  const { session } = useAuth();
  return (
    <SystemCard
      title="This workspace isn’t available"
      text="Your account is signed in, but it does not have permission to open this page."
      href={session ? roleHome[session.role] : "/"}
      label="Return to my workspace"
    />
  );
}
export function NotFoundPage() {
  const { session } = useAuth();
  return (
    <SystemCard
      title="Page not found"
      text="The page may have moved, or the address may be incomplete."
      href={session ? roleHome[session.role] : "/"}
      label="Return to the portal"
    />
  );
}
function SystemCard({
  title,
  text,
  href,
  label,
}: {
  title: string;
  text: string;
  href: string;
  label: string;
}) {
  return (
    <main className={styles.system}>
      <section className={styles.systemCard}>
        <h1>{title}</h1>
        <p>{text}</p>
        <Link className={`${uiStyles.button} ${uiStyles.primary}`} to={href}>
          {label}
        </Link>
      </section>
    </main>
  );
}
