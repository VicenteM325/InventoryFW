import React, { useState, useEffect } from 'react';
import {
  Card,
  Typography,
  Button,
  Input,
  Select,
  Option,
  Spinner,
} from "@material-tailwind/react";
import { saleService } from "@/services/saleService";
import { productService } from "@/services/productService";

export function Sales() {
  const [products, setProducts] = useState([]);
  const [recentSales, setRecentSales] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [items, setItems] = useState([{ productId: '', quantity: 1, unitPrice: 0 }]);
  const [customerName, setCustomerName] = useState('');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [productsData, salesData] = await Promise.all([
        productService.getAll(),
        saleService.getRecent()
      ]);
      setProducts(productsData);
      setRecentSales(salesData);
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  const addItem = () => {
    setItems([...items, { productId: '', quantity: 1, unitPrice: 0 }]);
  };

  const removeItem = (index) => {
    setItems(items.filter((_, i) => i !== index));
  };

  const updateItem = (index, field, value) => {
    const newItems = [...items];
    newItems[index][field] = value;
    
    // Si se selecciona un producto, obtener su precio automáticamente
    if (field === 'productId' && value) {
      const selectedProduct = products.find(p => p.productId === parseInt(value));
      if (selectedProduct) {
        newItems[index].unitPrice = selectedProduct.price || 0;
      }
    }
    
    setItems(newItems);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!customerName.trim()) {
      alert('Por favor ingresa el nombre del cliente');
      return;
    }
    
    // Validar que todos los items tengan producto y precio
    for (let i = 0; i < items.length; i++) {
      if (!items[i].productId) {
        alert(`Por favor selecciona un producto para la fila ${i + 1}`);
        return;
      }
      if (!items[i].unitPrice || items[i].unitPrice <= 0) {
        alert(`Por favor ingresa el precio unitario para el producto ${i + 1}`);
        return;
      }
    }

    setSubmitting(true);
    try {
      // Calcular el total de la venta
      const total = items.reduce((sum, item) => {
        return sum + (item.unitPrice * item.quantity);
      }, 0);

      const saleData = {
        customerName,
        total: total,
        items: items.map(item => ({
          productId: parseInt(item.productId),
          quantity: parseInt(item.quantity),
          unitPrice: parseFloat(item.unitPrice)
        }))
      };

      console.log('Enviando datos de venta:', saleData); // Debug

      await saleService.create(saleData);
      
      setItems([{ productId: '', quantity: 1, unitPrice: 0 }]);
      setCustomerName('');
      await fetchData();
      alert('Venta registrada exitosamente');
    } catch (error) {
      console.error('Error creating sale:', error);
      const errorMsg = error.response?.data?.message || 
                       error.response?.data?.error || 
                       'Error al registrar la venta';
      alert(errorMsg);
    } finally {
      setSubmitting(false);
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
      <Typography variant="h5" color="blue-gray" className="mb-6">
        Registrar Venta
      </Typography>

      <Card className="p-6 mb-8">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <Input
              label="Nombre del cliente"
              value={customerName}
              onChange={(e) => setCustomerName(e.target.value)}
              required
            />
          </div>

          <Typography variant="h6" color="blue-gray">
            Productos
          </Typography>

          {items.map((item, index) => (
            <div key={index} className="grid grid-cols-1 gap-4 md:grid-cols-4">
              <Select
                label="Producto"
                value={item.productId}
                onChange={(value) => updateItem(index, 'productId', value)}
              >
                {products.map((product) => (
                  <Option key={product.productId} value={product.productId.toString()}>
                    {product.name} (Stock: {product.stock})
                  </Option>
                ))}
              </Select>

              <Input
                label="Cantidad"
                type="number"
                min="1"
                value={item.quantity}
                onChange={(e) => updateItem(index, 'quantity', e.target.value)}
              />

              <Input
                label="Precio Unitario"
                type="number"
                min="0.01"
                step="0.01"
                value={item.unitPrice}
                onChange={(e) => updateItem(index, 'unitPrice', e.target.value)}
                required
              />

              <div className="flex items-center gap-2">
                <Typography variant="small" color="blue-gray" className="font-medium">
                  Total: Q{(item.unitPrice * item.quantity).toFixed(2)}
                </Typography>
                {items.length > 1 && (
                  <Button
                    color="red"
                    variant="text"
                    size="sm"
                    onClick={() => removeItem(index)}
                  >
                    Eliminar
                  </Button>
                )}
              </div>
            </div>
          ))}

          <Button
            variant="outlined"
            color="blue-gray"
            onClick={addItem}
            className="w-full"
          >
            Agregar Producto
          </Button>

          <div className="flex justify-between items-center">
            <Typography variant="h6" color="blue-gray">
              Total de la Venta: Q{items.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0).toFixed(2)}
            </Typography>
            <Button type="submit" color="green" disabled={submitting}>
              {submitting ? 'Registrando...' : 'Registrar Venta'}
            </Button>
          </div>
        </form>
      </Card>

      <Typography variant="h5" color="blue-gray" className="mb-4">
        Ventas Recientes
      </Typography>

      <Card className="p-6">
        {recentSales.length === 0 ? (
          <Typography color="gray">No hay ventas registradas</Typography>
        ) : (
          <div className="space-y-2">
            {recentSales.map((sale, index) => (
              <div key={index} className="flex items-center justify-between border-b py-2">
                <div>
                  <Typography variant="small" color="blue-gray">
                    {sale.customerName || 'Cliente Anónimo'}
                  </Typography>
                  <Typography variant="small" color="gray">
                    {new Date(sale.date).toLocaleString()}
                  </Typography>
                  <Typography variant="small" color="gray">
                    {sale.items?.length || 0} productos
                  </Typography>
                </div>
                <Typography variant="h6" color="blue-gray">
                  Q{sale.total?.toFixed(2) || '0.00'}
                </Typography>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}