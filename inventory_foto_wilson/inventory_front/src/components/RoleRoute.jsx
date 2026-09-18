import { Navigate } from "react-router-dom";
import PropTypes from "prop-types";
import useAuth from "@/hooks/useAuth";
import { getHomeByRole } from "@/auth/menuByRole";

// Protege una ruta del dashboard según el/los rol(es) permitidos. Antes
// existía pero nunca se usaba en ningún lado (ver layouts/dashboard.jsx),
// así que cualquier usuario autenticado podía navegar a cualquier ruta sin
// importar el array `roles` ya declarado en routes.jsx.
export default function RoleRoute({ children, roles }) {
  const { roles: userRoles, loading } = useAuth();

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

  const isAllowed = !roles || roles.length === 0 || roles.some((role) => userRoles.includes(role));

  if (!isAllowed) {
    // Redirigir al landing real del rol del usuario, no a un valor fijo:
    // /dashboard/home es en sí una ruta solo-Admin, así que mandar ahí a
    // un Encargado sin permiso volvería a chocar con este mismo chequeo.
    return <Navigate to={getHomeByRole(userRoles)} replace />;
  }

  return children;
}

RoleRoute.propTypes = {
  children: PropTypes.node.isRequired,
  roles: PropTypes.arrayOf(PropTypes.string),
};
