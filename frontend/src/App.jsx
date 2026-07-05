import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./components/AppShell";
import { useAuth } from "./context/useAuth";
import { LandingPage, LoginPage, SignupPage } from "./pages/AuthPages";
import {
  StudentJobsDashboardPage,
  StudentNotesPage,
  StudentReportsAnalyticsPage,
  StudentAnalyticsPage,
  StudentDashboardPage,
  StudentProfilePage,
  StudentSectionPage,
  StudentTodoPage,
} from "./pages/StudentPages";
import { CodingProblemsPage } from "./pages/CodingProblemsPage";
import { ProblemSolvePage } from "./pages/ProblemSolvePage";
import { AiMentorPage } from "./pages/AiMentorPage";
import {
  RecruiterDashboardPage,
  RecruiterMonitoringPage,
  RecruiterTopStudentsPage,
  StaffDashboardPage,
  StaffMonitoringPage,
  StaffTalentPoolPage,
} from "./pages/StaffPages";

const homeByRole = (portalRole) => {
  if (portalRole === "STUDENT") return "/student/dashboard";
  if (portalRole === "STAFF") return "/staff/dashboard";
  return "/recruiter/dashboard";
};

const Protected = ({ portalRole, children }) => {
  const { session } = useAuth();
  if (!session) return <Navigate to="/login" replace />;
  if (portalRole && session.portalRole !== portalRole) {
    return <Navigate to={homeByRole(session.portalRole)} replace />;
  }
  return children;
};

export default function App() {
  const { session } = useAuth();

  return (
    <Routes>
      <Route path="/" element={session ? <Navigate to={homeByRole(session.portalRole)} replace /> : <LandingPage />} />
      <Route path="/login" element={session ? <Navigate to={homeByRole(session.portalRole)} replace /> : <LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />

      <Route
        path="/student/jobs"
        element={(
          <Protected portalRole="STUDENT">
            <AppShell role="STUDENT"><StudentJobsDashboardPage /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/student/dashboard"
        element={(
          <Protected portalRole="STUDENT">
            <AppShell role="STUDENT"><StudentDashboardPage /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/student/ai-mentor"
        element={(
          <Protected portalRole="STUDENT">
            <AppShell role="STUDENT"><AiMentorPage /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/student/reports"
        element={(
          <Protected portalRole="STUDENT">
            <AppShell role="STUDENT"><StudentReportsAnalyticsPage /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/student/todo"
        element={(
          <Protected portalRole="STUDENT">
            <AppShell role="STUDENT"><StudentTodoPage /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/student/notes"
        element={(
          <Protected portalRole="STUDENT">
            <AppShell role="STUDENT"><StudentNotesPage /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/student/aptitude"
        element={(
          <Protected portalRole="STUDENT">
            <AppShell role="STUDENT"><StudentSectionPage section="aptitude" /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/student/dsa"
        element={(
          <Protected portalRole="STUDENT">
            <AppShell role="STUDENT"><CodingProblemsPage /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/student/dsa/problem/:problemId"
        element={(
          <Protected portalRole="STUDENT">
            <AppShell role="STUDENT"><ProblemSolvePage /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/student/mock"
        element={(
          <Protected portalRole="STUDENT">
            <AppShell role="STUDENT"><StudentSectionPage section="mock" /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/student/profile"
        element={(
          <Protected portalRole="STUDENT">
            <AppShell role="STUDENT"><StudentProfilePage /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/student/analytics"
        element={(
          <Protected portalRole="STUDENT">
            <AppShell role="STUDENT"><StudentAnalyticsPage /></AppShell>
          </Protected>
        )}
      />

      <Route
        path="/recruiter/dashboard"
        element={(
          <Protected portalRole="RECRUITER">
            <AppShell role="RECRUITER"><RecruiterDashboardPage /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/recruiter/top"
        element={(
          <Protected portalRole="RECRUITER">
            <AppShell role="RECRUITER"><RecruiterTopStudentsPage /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/recruiter/monitoring"
        element={(
          <Protected portalRole="RECRUITER">
            <AppShell role="RECRUITER"><RecruiterMonitoringPage /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/staff/dashboard"
        element={(
          <Protected portalRole="STAFF">
            <AppShell role="STAFF"><StaffDashboardPage /></AppShell>
          </Protected>
        )}
      />
      <Route
        path="/staff/talent"
        element={(
          <Protected portalRole="STAFF">
            <AppShell role="STAFF"><StaffTalentPoolPage /></AppShell>
          </Protected>
        )}
      />
      <Route path="/staff/top" element={<Navigate to="/staff/talent" replace />} />
      <Route
        path="/staff/monitoring"
        element={(
          <Protected portalRole="STAFF">
            <AppShell role="STAFF"><StaffMonitoringPage /></AppShell>
          </Protected>
        )}
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
