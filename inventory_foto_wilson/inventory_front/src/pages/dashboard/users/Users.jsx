import React, { useState, useEffect } from 'react';
import {
  Card,
  Typography,
  Button,
  Input,
  Select,
  Option,
  Chip,
  IconButton,
  Tooltip,
  Spinner,
} from "@material-tailwind/react";
import { PencilIcon, PlusIcon } from "@heroicons/react/24/outline";
import { userService } from "@/services/userService";

const TABLE_HEAD = ['Nombre', 'Usuario', 'Rol', 'Estado', 'Acciones'];

const EMPTY_CREATE_FORM = { name: '', userName: '', password: '', roleName: 'ROLE_EMPLOYEE' };

export function Users() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [createForm, setCreateForm] = useState(EMPTY_CREATE_FORM);
  const [saving, setSaving] = useState(false);

  const [editingUserId, setEditingUserId] = useState(null);
  const [editForm, setEditForm] = useState({ name: '', roleName: '' });

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const data = await userService.getAll();
      setUsers(data);
    } catch (err) {
      console.error('Error fetching users:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await userService.create(createForm);
      setCreateForm(EMPTY_CREATE_FORM);
      setShowCreateForm(false);
      await fetchUsers();
    } catch (err) {
      setError(err.response?.data?.message || 'Error al crear el usuario');
    } finally {
      setSaving(false);
    }
  };

  const startEdit = (user) => {
    setEditingUserId(user.userId);
    setEditForm({ name: user.name, roleName: user.roleName });
  };

  const handleUpdate = async (userId) => {
    setSaving(true);
    setError('');
    try {
      await userService.update(userId, editForm);
      setEditingUserId(null);
      await fetchUsers();
    } catch (err) {
      setError(err.response?.data?.message || 'Error al actualizar el usuario');
    } finally {
      setSaving(false);
    }
  };

  const toggleActive = async (user) => {
    try {
      if (user.active) {
        await userService.deactivate(user.userId);
      } else {
        await userService.activate(user.userId);
      }
      await fetchUsers();
    } catch (err) {
      setError(err.response?.data?.message || 'Error al cambiar el estado del usuario');
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Spinner className="h-8 w-8" />
      </div>
    );
  }

  return (
    <div className="mt-12">
      <div className="mb-8 flex flex-col justify-between gap-4 md:flex-row md:items-center">
        <div>
          <Typography variant="h5" color="blue-gray">
            Usuarios
          </Typography>
          <Typography color="gray" className="mt-1 font-normal">
            {users.length} cuentas registradas
          </Typography>
        </div>
        <Button
          color="blue"
          className="flex items-center gap-2 w-fit"
          onClick={() => setShowCreateForm((prev) => !prev)}
        >
          <PlusIcon className="h-4 w-4" />
          Nuevo usuario
        </Button>
      </div>

      {error && (
        <Typography variant="small" color="red" className="mb-4">
          {error}
        </Typography>
      )}

      {showCreateForm && (
        <Card className="mb-6 p-6">
          <Typography variant="h6" color="blue-gray" className="mb-4">
            Nuevo usuario
          </Typography>
          <form onSubmit={handleCreate} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <Input
                label="Nombre completo"
                value={createForm.name}
                onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
                required
              />
              <Input
                label="Nombre de usuario"
                value={createForm.userName}
                onChange={(e) => setCreateForm({ ...createForm, userName: e.target.value })}
                required
              />
              <Input
                label="Contraseña"
                type="password"
                value={createForm.password}
                onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })}
                required
              />
              <Select
                label="Rol"
                value={createForm.roleName}
                onChange={(value) => setCreateForm({ ...createForm, roleName: value })}
              >
                <Option value="ROLE_EMPLOYEE">Encargado</Option>
                <Option value="ROLE_ADMIN">Administrador</Option>
              </Select>
            </div>
            <div className="flex justify-end gap-4">
              <Button type="button" variant="text" color="blue-gray" onClick={() => setShowCreateForm(false)}>
                Cancelar
              </Button>
              <Button type="submit" color="green" disabled={saving}>
                {saving ? 'Guardando...' : 'Crear usuario'}
              </Button>
            </div>
          </form>
        </Card>
      )}

      <Card className="h-full w-full overflow-scroll p-6">
        <table className="w-full min-w-max table-auto">
          <thead>
            <tr>
              {TABLE_HEAD.map((head) => (
                <th key={head} className="border-b border-blue-gray-100 bg-blue-gray-50 p-4">
                  <Typography variant="small" color="blue-gray" className="font-bold">
                    {head}
                  </Typography>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {users.map((user) => {
              const isEditing = editingUserId === user.userId;
              return (
                <tr key={user.userId}>
                  <td className="p-4">
                    {isEditing ? (
                      <Input
                        value={editForm.name}
                        onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                      />
                    ) : (
                      <Typography variant="small" color="blue-gray" className="font-semibold">
                        {user.name}
                      </Typography>
                    )}
                  </td>
                  <td className="p-4">
                    <Typography variant="small" color="blue-gray">
                      {user.userName}
                    </Typography>
                  </td>
                  <td className="p-4">
                    {isEditing ? (
                      <Select
                        value={editForm.roleName}
                        onChange={(value) => setEditForm({ ...editForm, roleName: value })}
                      >
                        <Option value="ROLE_EMPLOYEE">Encargado</Option>
                        <Option value="ROLE_ADMIN">Administrador</Option>
                      </Select>
                    ) : (
                      <Typography variant="small" color="blue-gray">
                        {user.roleName === 'ROLE_ADMIN' ? 'Administrador' : 'Encargado'}
                      </Typography>
                    )}
                  </td>
                  <td className="p-4">
                    <Chip
                      value={user.active ? 'Activo' : 'Desactivado'}
                      color={user.active ? 'green' : 'red'}
                      size="sm"
                      className="w-fit"
                    />
                  </td>
                  <td className="p-4">
                    <div className="flex items-center gap-2">
                      {isEditing ? (
                        <>
                          <Button size="sm" color="green" onClick={() => handleUpdate(user.userId)} disabled={saving}>
                            Guardar
                          </Button>
                          <Button size="sm" variant="text" color="blue-gray" onClick={() => setEditingUserId(null)}>
                            Cancelar
                          </Button>
                        </>
                      ) : (
                        <>
                          <Tooltip content="Editar">
                            <IconButton variant="text" color="blue-gray" onClick={() => startEdit(user)}>
                              <PencilIcon className="h-4 w-4" />
                            </IconButton>
                          </Tooltip>
                          <Button
                            size="sm"
                            variant="outlined"
                            color={user.active ? 'red' : 'green'}
                            onClick={() => toggleActive(user)}
                          >
                            {user.active ? 'Desactivar' : 'Activar'}
                          </Button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </Card>
    </div>
  );
}

export default Users;
