import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Card,
  Typography,
  Input,
  Textarea,
  Button,
} from "@material-tailwind/react";
import { productService } from "@/services/productService";

export function CreateProduct() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    barcode: '',
    description: '',
    stock: 0,
  });
  const [errors, setErrors] = useState({});

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

    setLoading(true);
    try {
      await productService.create(formData);
      navigate('/products');
    } catch (error) {
      console.error('Error creating product:', error);
      alert(error.response?.data?.message || 'Error al crear el producto');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mt-12">
      <Card className="p-8">
        <Typography variant="h4" color="blue-gray" className="mb-6">
          Nuevo Producto
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
              label="Stock inicial"
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
            <Button type="submit" color="blue" disabled={loading}>
              {loading ? 'Guardando...' : 'Crear Producto'}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}