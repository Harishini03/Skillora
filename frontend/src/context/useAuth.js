import { useContext } from "react";
import { AuthContext } from "./AuthContextStore";

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("Auth context unavailable");
  }
  return ctx;
};
