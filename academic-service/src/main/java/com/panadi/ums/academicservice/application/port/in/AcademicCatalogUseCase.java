package com.panadi.ums.academicservice.application.port.in;

import com.panadi.ums.academicservice.application.PageResult;
import com.panadi.ums.academicservice.application.command.Commands.DepartmentCommand;
import com.panadi.ums.academicservice.application.command.Commands.ProgramCommand;
import com.panadi.ums.academicservice.application.command.Commands.SectionCommand;
import com.panadi.ums.academicservice.application.command.Commands.SemesterCommand;
import com.panadi.ums.academicservice.application.command.Commands.SubjectCommand;
import com.panadi.ums.academicservice.application.command.Commands.TeacherCommand;
import com.panadi.ums.academicservice.domain.model.AcademicProgram;
import com.panadi.ums.academicservice.domain.model.AcademicStatus;
import com.panadi.ums.academicservice.domain.model.Department;
import com.panadi.ums.academicservice.domain.model.Section;
import com.panadi.ums.academicservice.domain.model.Semester;
import com.panadi.ums.academicservice.domain.model.SemesterStatus;
import com.panadi.ums.academicservice.domain.model.Subject;
import com.panadi.ums.academicservice.domain.model.Teacher;

import java.util.UUID;

public interface AcademicCatalogUseCase {
    Department createDepartment(DepartmentCommand command);
    Department updateDepartment(UUID id, DepartmentCommand command);
    Department getDepartment(UUID id);
    PageResult<Department> listDepartments(AcademicStatus status, int page, int size);
    Department activateDepartment(UUID id);
    Department deactivateDepartment(UUID id);

    AcademicProgram createProgram(ProgramCommand command);
    AcademicProgram updateProgram(UUID id, ProgramCommand command);
    AcademicProgram getProgram(UUID id);
    PageResult<AcademicProgram> listPrograms(UUID departmentId, AcademicStatus status, int page, int size);
    AcademicProgram activateProgram(UUID id);
    AcademicProgram deactivateProgram(UUID id);

    Teacher createTeacher(TeacherCommand command);
    Teacher updateTeacher(UUID id, TeacherCommand command);
    Teacher getTeacher(UUID id);
    PageResult<Teacher> listTeachers(UUID departmentId, AcademicStatus status, int page, int size);
    Teacher activateTeacher(UUID id);
    Teacher deactivateTeacher(UUID id);

    Semester createSemester(SemesterCommand command);
    Semester updateSemester(UUID id, SemesterCommand command);
    Semester getSemester(UUID id);
    PageResult<Semester> listSemesters(SemesterStatus status, int page, int size);
    Semester activateSemester(UUID id);
    Semester deactivateSemester(UUID id);

    Subject createSubject(SubjectCommand command);
    Subject updateSubject(UUID id, SubjectCommand command);
    Subject getSubject(UUID id);
    PageResult<Subject> listSubjects(UUID programId, AcademicStatus status, int page, int size);
    Subject activateSubject(UUID id);
    Subject deactivateSubject(UUID id);

    Section createSection(SectionCommand command);
    Section updateSection(UUID id, SectionCommand command);
    Section getSection(UUID id);
    PageResult<Section> listSections(UUID subjectId, UUID teacherId, UUID semesterId, AcademicStatus status, int page, int size);
    Section activateSection(UUID id);
    Section deactivateSection(UUID id);
}
