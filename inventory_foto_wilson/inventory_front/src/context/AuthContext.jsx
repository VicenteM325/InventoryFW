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

    console.log('AuthProvider - storedRoles:', storedRoles);

    if (storedUser && storedToken) {
      try {
        const parsedUser = JSON.parse(storedUser);
        const parsedRoles = storedRoles ? JSON.parse(storedRoles) : [];
        
        console.log('AuthProvider - parsedRoles:', parsedRoles);
        
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
      console.log('AuthProvider - Verificando sesión...');
      const data = await getUserDetails();
      console.log('AuthProvider - Datos de sesión:', data);
      
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

        console.log('AuthProvider - userData:', userData);
        console.log('AuthProvider - rolesData:', rolesData);
        console.log('AuthProvider - tokenData:', tokenData);

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
      console.log('No hay sesión activa');
    } finally {
      setLoading(false);
    }
  };

  const login = (data) => {
    console.log('AuthProvider - Login - Datos recibidos:', data);
    
    // Extraer el rol del objeto role
    const userData = data.user || data;
    const roleName = data.role?.name || data.role || '';
    const rolesData = roleName ? [roleName] : [];
    const tokenData = data.token || data.accessToken;

    console.log('AuthProvider - Login - userData:', userData);
    console.log('AuthProvider - Login - rolesData:', rolesData);

    setUser(userData);
    setRoles(rolesData);
    setToken(tokenData);

    try {
      localStorage.setItem('user', JSON.stringify(userData));
      localStorage.setItem('roles', JSON.stringify(rolesData));
      if (tokenData) {
        localStorage.setItem('token', tokenData);
      }
      console.log('AuthProvider - Login - roles guardados en localStorage:', rolesData);
    } catch (error) {
      console.error('Error al guardar los datos en el localStorage', error);
    }
  };

  const logout = async () => {
    try {
      if (token) {
        await logoutService(token);
      }
    } catch (error) {
      console.log('Token ya expirado o inválido', error);
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