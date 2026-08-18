import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Card,
  Typography,
  Input,
  Textarea,
  Button,
  Spinner,
} from "@material-tailwind/react";
import { productService } from "@/services/productService";

export function EditProduct() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    barcode: '',
    description: '',
    stock: 0,
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    fetchProduct();
  }, [id]);

  const fetchProduct = async () => {
    try {
      setLoading(true);
      const data = await productService.getById(id);
      setFormData({
        name: data.name,
        barcode: data.barcode,
        description: data.description || '',
        stock: data.stock,
      });
    } catch (error) {
      console.error('Error fetching product:', error);
      alert('Error al cargar el producto');
      navigate('/products');
    } finally {
      setLoading(false);
    }
  };

  const validateForm = () => {
    const newErrors = {};
    if (!formData.name.trim()) newErrors.name = 'El nombre es obligatorio';
    if (!formData.barcode.trim()) newErrors.barcode = 'El código de barras es obligatorio';
    if (formData.stock < 0) newErrors.stock = 'El stock no puede ser negativo';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'stock' ? parseInt(value) || 0 : value
    }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setSaving(true);
    try {
      await productService.update(id, formData);
      navigate('/products');
    } catch (error) {
      console.error('Error updating product:', error);
      alert(error.response?.data?.message || 'Error al actualizar el producto');
    } finally {
      setSaving(false);
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
      <Card className="p-8">
        <Typography variant="h4" color="blue-gray" className="mb-6">
          Editar Producto
        </Typography>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div>
              <Input
                label="Nombre del producto"
                name="name"
                value={formData.name}
                onChange={handleChange}
                error={!!errors.name}
              />
              {errors.name && (
                <Typography variant="small" color="red" className="mt-1">
                  {errors.name}
                </Typography>
              )}
            </div>

            <div>
              <Input
                label="Código de barras"
                name="barcode"
                value={formData.barcode}
                onChange={handleChange}
                error={!!errors.barcode}
              />
              {errors.barcode && (
                <Typography variant="small" color="red" className="mt-1">
                  {errors.barcode}
                </Typography>
              )}
            </div>
          </div>

          <div>
            <Textarea
              label="Descripción"
              name="description"
              value={formData.description}
              onChange={handleChange}
            />
          </div>

          <div>
            <Input
              label="Stock"
              type="number"
              name="stock"
              value={formData.stock}
              onChange={handleChange}
              error={!!errors.stock}
            />
            {errors.stock && (
              <Typography variant="small" color="red" className="mt-1">
                {errors.stock}
              </Typography>
            )}
          </div>

          <div className="flex justify-end gap-4">
            <Button
              variant="text"
              color="blue-gray"
              onClick={() => navigate('/products')}
            >
              Cancelar
            </Button>
            <Button type="submit" color="blue" disabled={saving}>
              {saving ? 'Guardando...' : 'Actualizar Producto'}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}