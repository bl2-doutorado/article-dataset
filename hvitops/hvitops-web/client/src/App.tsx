import { Toaster } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { Route, Switch, useLocation } from "wouter";
import ErrorBoundary from "./components/ErrorBoundary";
import { ThemeProvider } from "./contexts/ThemeContext";
import { AuthProvider, useAuth } from "./contexts/AuthContext";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Appointments from "./pages/Appointments";
import AppointmentDetails from "./pages/AppointmentDetails";
import BookAppointment from "./pages/BookAppointment";
import LabTests from "./pages/LabTests";
import MedicalRecords from "./pages/MedicalRecords";
import Admin from "./pages/Admin";
import NotFound from "./pages/NotFound";

function Router() {
  const { isAuthenticated } = useAuth();
  const [location] = useLocation();

  if (!isAuthenticated && location !== '/login') {
    return <Login />;
  }

  return (
    <Switch>
      <Route path="/login" component={Login} />
      <Route path="/dashboard">
        {isAuthenticated ? <Dashboard /> : <Login />}
      </Route>
      <Route path="/appointments/new">
        {isAuthenticated ? <BookAppointment /> : <Login />}
      </Route>
      <Route path="/appointments/:id">
        {isAuthenticated ? <AppointmentDetails /> : <Login />}
      </Route>
      <Route path="/appointments">
        {isAuthenticated ? <Appointments /> : <Login />}
      </Route>
      <Route path="/lab-tests">
        {isAuthenticated ? <LabTests /> : <Login />}
      </Route>
      <Route path="/medical-records">
        {isAuthenticated ? <MedicalRecords /> : <Login />}
      </Route>
      <Route path="/admin">
        {isAuthenticated ? <Admin /> : <Login />}
      </Route>
      <Route path="/">
        {isAuthenticated ? <Dashboard /> : <Login />}
      </Route>
      <Route path="/404" component={NotFound} />
      <Route component={NotFound} />
    </Switch>
  );
}

function App() {
  return (
    <ErrorBoundary>
      <ThemeProvider defaultTheme="light">
        <AuthProvider>
          <TooltipProvider>
            <Toaster />
            <Router />
          </TooltipProvider>
        </AuthProvider>
      </ThemeProvider>
    </ErrorBoundary>
  );
}

export default App;
