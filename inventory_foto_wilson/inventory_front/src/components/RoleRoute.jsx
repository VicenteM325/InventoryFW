import { Navigate } from "react-router-dom";
import useAuth from "@/hooks/useAuth";

export default function RoleRoute({ children, role }) {
  const { roles, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto"></div>
          <p className="mt-4 text-gray-600">Verificando permisos...</p>
        </div>
      </div>
    );
  }

  if (!roles.includes(role)) {
    return <Navigate to="/dashboard/home" replace />;
  }

  return children;
}