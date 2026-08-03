import type { TeacherSection } from "../../api/generated/contracts";

export function teacherSectionLabel(section: TeacherSection) {
  return `${section.subject.name} · ${section.sectionCode}`;
}
