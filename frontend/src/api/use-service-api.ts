import { useMemo } from "react";
import { useAuth } from "../auth/AuthProvider";
import { createServiceApi } from "./service-api";
export function useServiceApi() {
  const { getToken } = useAuth();
  return useMemo(() => createServiceApi(getToken), [getToken]);
}
