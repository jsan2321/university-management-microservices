import "@testing-library/jest-dom/vitest";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { EmptyState, StatusBadge } from "../components/ui";
describe("shared UI states", () => {
  it("explains an empty task in context", () => {
    render(
      <EmptyState
        title="No enrollments yet"
        description="Create an enrollment after the academic records are active."
      />,
    );
    expect(
      screen.getByRole("heading", { name: "No enrollments yet" }),
    ).toBeInTheDocument();
    expect(screen.getByText(/academic records/i)).toBeInTheDocument();
  });
  it("renders status as text, not color alone", () => {
    render(<StatusBadge value="PUBLISHED" />);
    expect(screen.getByText("PUBLISHED")).toBeInTheDocument();
  });
});
