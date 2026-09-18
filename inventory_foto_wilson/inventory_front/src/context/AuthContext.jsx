import React, { createContext, useState, useEffect } from 'react';
import { getUserDetails, logout as logoutService } from '@/services/authService';

export const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [roles, setRoles] = useState([]);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    const storedRoles = localStorage.getItem('roles');
    const storedToken = localStorage.getItem('token');

    if (storedUser && storedToken) {
      try {
        const parsedUser = JSON.parse(storedUser);
        const parsedRoles = storedRoles ? JSON.parse(storedRoles) : [];

        setUser(parsedUser);
        setRoles(parsedRoles);
        setToken(storedToken);
        setLoading(false);
      } catch (error) {
        console.error('Error al parsear los datos del localStorage', error);
        setLoading(false);
      }
    } else {
      verifySession();
    }
  }, []);

  const verifySession = async () => {
    try {
      const data = await getUserDetails();

      if (data) {
        // Extraer el rol del objeto role
        const userData = {
          userId: data.userId,
          name: data.name,
          userName: data.userName
        };

        // Extraer el nombre del rol del objeto role
        const roleName = data.role?.name || data.role || '';
        const rolesData = roleName ? [roleName] : [];

        const tokenData = data.token || localStorage.getItem('token');

        setUser(userData);
        setRoles(rolesData);
        setToken(tokenData);

        if (tokenData) {
          localStorage.setItem('token', tokenData);
        }
        if (userData) {
          localStorage.setItem('user', JSON.stringify(userData));
        }
        if (rolesData.length > 0) {
          localStorage.setItem('roles', JSON.stringify(rolesData));
        }
      }
    } catch (error) {
      // No hay sesión activa; se queda deslogueado sin considerarlo un error.
    } finally {
      setLoading(false);
    }
  };

  const login = (data) => {
    // Extraer el rol del objeto role
    const userData = data.user || data;
    const roleName = data.role?.name || data.role || '';
    const rolesData = roleName ? [roleName] : [];
    const tokenData = data.token || data.accessToken;

    setUser(userData);
    setRoles(rolesData);
    setToken(tokenData);

    try {
      localStorage.setItem('user', JSON.stringify(userData));
      localStorage.setItem('roles', JSON.stringify(rolesData));
      if (tokenData) {
        localStorage.setItem('token', tokenData);
      }
    } catch (error) {
      console.error('Error al guardar los datos en el localStorage', error);
    }
  };

  const logout = async () => {
    try {
      await logoutService();
    } catch (error) {
      // El token ya pudo haber expirado en el backend; igual limpiamos localmente.
    }

    setUser(null);
    setRoles([]);
    setToken(null);

    localStorage.removeItem('user');
    localStorage.removeItem('roles');
    localStorage.removeItem('token');
  };

  return (
    <AuthContext.Provider value={{ user, roles, token, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}