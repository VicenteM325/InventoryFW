import React, { useState, useEffect } from 'react';
import {
  Card,
  Typography,
  Button,
  Input,
  IconButton,
  Tooltip,
  Spinner,
} from "@material-tailwind/react";
import { PencilIcon, PlusIcon, TrashIcon } from "@heroicons/react/24/outline";
import { supplierService } from "@/services/supplierService";
import { AuthContext } from "@/context/AuthContext";

const TABLE_HEAD = ['Nombre', 'Contacto', 'Teléfono', 'Acciones'];

const EMPTY_CREATE_FORM = { name: '', contactName: '', phone: '' };

export function Suppliers() {
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { roles } = React.useContext(AuthContext);
  const isAdmin = roles?.includes('ADMIN') || roles?.includes('ROLE_ADMIN');

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [createForm, setCreateForm] = useState(EMPTY_CREATE_FORM);
  const [saving, setSaving] = useState(false);

  const [editingSupplierId, setEditingSupplierId] = useState(null);
  const [editForm, setEditForm] = useState(EMPTY_CREATE_FORM);

  useEffect(() => {
    fetchSuppliers();
  }, []);

  const fetchSuppliers = async () => {
    try {
      setLoading(true);
      const data = await supplierService.getAll();
      setSuppliers(data);
    } catch (err) {
      console.error('Error fetching suppliers:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await supplierService.create(createForm);
      setCreateForm(EMPTY_CREATE_FORM);
      setShowCreateForm(false);
      await fetchSuppliers();
    } catch (err) {
      setError(err.response?.data?.message || 'Error al crear el proveedor');
    } finally {
      setSaving(false);
    }
  };

  const startEdit = (supplier) => {
    setEditingSupplierId(supplier.supplierId);
    setEditForm({
      name: supplier.name,
      contactName: supplier.contactName || '',
      phone: supplier.phone || '',
    });
  };

  const handleUpdate = async (supplierId) => {
    setSaving(true);
    setError('');
    try {
      await supplierService.update(supplierId, editForm);
      setEditingSupplierId(null);
      await fetchSuppliers();
    } catch (err) {
      setError(err.response?.data?.message || 'Error al actualizar el proveedor');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (supplierId) => {
    if (!window.confirm('¿Eliminar este proveedor?')) return;
    setError('');
    try {
      await supplierService.delete(supplierId);
      await fetchSuppliers();
    } catch (err) {
      setError(err.response?.data?.message || 'Error al eliminar el proveedor');
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
            Proveedores
          </Typography>
          <Typography color="gray" className="mt-1 font-normal">
            {suppliers.length} proveedores registrados
          </Typography>
        </div>
        <Button
          color="blue"
          className="flex items-center gap-2 w-fit"
          onClick={() => setShowCreateForm((prev) => !prev)}
        >
          <PlusIcon className="h-4 w-4" />
          Nuevo proveedor
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
            Nuevo proveedor
          </Typography>
          <form onSubmit={handleCreate} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
              <Input
                label="Nombre del proveedor"
                value={createForm.name}
                onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
                required
              />
              <Input
                label="Contacto (opcional)"
                value={createForm.contactName}
                onChange={(e) => setCreateForm({ ...createForm, contactName: e.target.value })}
              />
              <Input
                label="Teléfono (opcional)"
                value={createForm.phone}
                onChange={(e) => setCreateForm({ ...createForm, phone: e.target.value })}
              />
            </div>
            <div className="flex justify-end gap-4">
              <Button type="button" variant="text" color="blue-gray" onClick={() => setShowCreateForm(false)}>
                Cancelar
              </Button>
              <Button type="submit" color="green" disabled={saving}>
                {saving ? 'Guardando...' : 'Crear proveedor'}
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
            {suppliers.map((supplier) => {
              const isEditing = editingSupplierId === supplier.supplierId;
              return (
                <tr key={supplier.supplierId}>
                  <td className="p-4">
                    {isEditing ? (
                      <Input
                        value={editForm.name}
                        onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                      />
                    ) : (
                      <Typography variant="small" color="blue-gray" className="font-semibold">
                        {supplier.name}
                      </Typography>
                    )}
                  </td>
                  <td className="p-4">
                    {isEditing ? (
                      <Input
                        value={editForm.contactName}
                        onChange={(e) => setEditForm({ ...editForm, contactName: e.target.value })}
                      />
                    ) : (
                      <Typography variant="small" color="blue-gray">
                        {supplier.contactName || '-'}
                      </Typography>
                    )}
                  </td>
                  <td className="p-4">
                    {isEditing ? (
                      <Input
                        value={editForm.phone}
                        onChange={(e) => setEditForm({ ...editForm, phone: e.target.value })}
                      />
                    ) : (
                      <Typography variant="small" color="blue-gray">
                        {supplier.phone || '-'}
                      </Typography>
                    )}
                  </td>
                  <td className="p-4">
                    <div className="flex items-center gap-2">
                      {isEditing ? (
                        <>
                          <Button size="sm" color="green" onClick={() => handleUpdate(supplier.supplierId)} disabled={saving}>
                            Guardar
                          </Button>
                          <Button size="sm" variant="text" color="blue-gray" onClick={() => setEditingSupplierId(null)}>
                            Cancelar
                          </Button>
                        </>
                      ) : (
                        <>
                          <Tooltip content="Editar">
                            <IconButton variant="text" color="blue-gray" onClick={() => startEdit(supplier)}>
                              <PencilIcon className="h-4 w-4" />
                            </IconButton>
                          </Tooltip>
                          {isAdmin && (
                            <Tooltip content="Eliminar">
                              <IconButton variant="text" color="red" onClick={() => handleDelete(supplier.supplierId)}>
                                <TrashIcon className="h-4 w-4" />
                              </IconButton>
                            </Tooltip>
                          )}
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

export default Suppliers;
