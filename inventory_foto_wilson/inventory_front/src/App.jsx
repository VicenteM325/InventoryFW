import { Routes, Route, Navigate } from 'react-router-dom';
import { Dashboard, Auth } from '@/layouts';
import PrivateRoute from '@/components/PrivateRoute';
import GuestRoute from '@/components/GuestRoute';
import { useAuth } from '@/hooks/useAuth';
import { getHomeByRole } from '@/auth/menuByRole';
import { Landing } from '@/pages/landing';

function App() {
  const { user, roles, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto"></div>
          <p className="mt-4 text-gray-600">Verificando autenticación...</p>
        </div>
      </div>
    );
  }

  return (
    <Routes>
      {/* RUTAS PRIVADAS */}
      <Route
        path="/dashboard/*"
        element={
          <PrivateRoute>
            <Dashboard />
          </PrivateRoute>
        }
      />

      {/* RUTAS PUBLICAS SOLO PARA INVITADOS */}
      <Route
        path="/auth/*"
        element={
          <GuestRoute>
            <Auth />
          </GuestRoute>
        }
      />

      {/* RUTA RAIZ - Landing pública para visitantes, dashboard para quien ya inició sesión */}
      <Route
        path="/"
        element={
          user ? (
            <Navigate to={getHomeByRole(roles)} replace />
          ) : (
            <Landing />
          )
        }
      />

      {/* CUALQUIER OTRA RUTA */}
      <Route 
        path="*" 
        element={
          user ? (
            <Navigate to={getHomeByRole(roles)} replace />
          ) : (
            <Navigate to="/auth/sign-in" replace />
          )
        } 
      />
    </Routes>
  );
}

export default App;