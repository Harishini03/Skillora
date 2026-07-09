import { useState } from "react";
import api from "../lib/api";
import { AuthContext } from "./AuthContextStore";

const PORTAL_ROLES = new Set(["STUDENT", "RECRUITER", "STAFF"]);

const toApiRole = (role) => role;

const normalizePortalRole = (sessionRole, preferredRole) => {
  if (PORTAL_ROLES.has(sessionRole)) {
    return sessionRole;
  }
  if (PORTAL_ROLES.has(preferredRole)) {
    return preferredRole;
  }
  return "STUDENT";
};

const readSession = () => {
  const raw = localStorage.getItem("pi_session");
  if (!raw) return null;
  try {
    const parsed = JSON.parse(raw);
    return {
      ...parsed,
      portalRole: normalizePortalRole(parsed.role, parsed.portalRole),
    };
  } catch {
    return null;
  }
};

export const AuthProvider = ({ children }) => {
  const [session, setSession] = useState(readSession());

  const commitSession = (data, preferredRole) => {
    const portalRole = normalizePortalRole(data.role, preferredRole);
    const auth = {
      token: data.token,
      role: data.role,
      portalRole,
      studentId: data.studentId,
      userId: data.userId,
      name: data.name,
      email: data.email,
    };
    localStorage.setItem("pi_token", auth.token);
    localStorage.setItem("pi_session", JSON.stringify(auth));
    setSession(auth);
  };

  const clearStoredSession = () => {
    localStorage.removeItem("pi_token");
    localStorage.removeItem("pi_session");
  };

  const login = async ({ usernameOrEmail, password, role: portalRole }) => {
    clearStoredSession();
    const { data } = await api.post("/api/auth/login", { usernameOrEmail, password, role: toApiRole(portalRole) });
    commitSession(data, portalRole);
    return data;
  };

  const signup = async (payload) => {
    clearStoredSession();
    const { role, ...rest } = payload;
    const { data } = await api.post("/api/auth/signup", { ...rest, role: toApiRole(role) });
    commitSession(data, role);
    return data;
  };

  const googleLogin = async ({ email, name, role, departmentId, departmentName }) => {
    clearStoredSession();
    const { data } = await api.post("/api/auth/google-login", {
      email,
      name,
      role: toApiRole(role),
      departmentId,
      departmentName,
    });
    commitSession(data, role);
    return data;
  };

  const firebaseLogin = async ({ firebaseUser, role, departmentName, departmentId, cgpa, level, interests }) => {
    clearStoredSession();
    const idToken = await firebaseUser.getIdToken();
    const { data } = await api.post("/api/auth/firebase-login", {
      idToken,
      role: toApiRole(role),
      departmentName,
      departmentId,
      cgpa,
      level,
      interests,
    });
    commitSession(data, role);
    return data;
  };

  const logout = () => {
    localStorage.removeItem("pi_token");
    localStorage.removeItem("pi_session");
    setSession(null);
  };

  return (
    <AuthContext.Provider value={{ session, login, signup, googleLogin, firebaseLogin, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
