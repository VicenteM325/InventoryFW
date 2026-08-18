import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Card,
  Typography,
  Button,
  Input,
  Chip,
  IconButton,
  Tooltip,
  Spinner,
} from "@material-tailwind/react";
import {
  PencilIcon,
  TrashIcon,
  EyeIcon,
  PlusIcon,
} from "@heroicons/react/24/outline";
import { productService } from "@/services/productService";
import { AuthContext } from "@/context/AuthContext"; 

const TABLE_HEAD = ['Producto', 'Código', 'Stock', 'Estado', 'Acciones'];

export function Products() {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const { roles } = React.useContext(AuthContext);

  const isAdmin = roles?.includes('ADMIN') || roles?.includes('ROLE_ADMIN');

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const data = await productService.getAll();
      setProducts(data);
    } catch (error) {
      console.error('Error fetching products:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Estás seguro de eliminar este producto?')) {
      try {
        await productService.delete(id);
        await fetchProducts();
      } catch (error) {
        console.error('Error deleting product:', error);
        alert(error.response?.data?.message || 'Error al eliminar el producto');
      }
    }
  };

  const getStockStatus = (stock) => {
    if (stock <= 0) return { label: 'Agotado', color: 'red' };
    if (stock <= 5) return { label: 'Stock Crítico', color: 'orange' };
    if (stock <= 10) return { label: 'Stock Bajo', color: 'amber' };
    return { label: 'Disponible', color: 'green' };
  };

  const filteredProducts = products.filter(product =>
    product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    product.barcode.includes(searchTerm)
  );

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Spinner className="h-8 w-8" />
      </div>
    );
  }

  return (
    <div className="mt-12">
      <div className="mb-8 flex flex-col justify-between gap-4 md:flex-row">
        <div>
          <Typography variant="h5" color="blue-gray">
            Productos
          </Typography>
          <Typography color="gray" className="mt-1 font-normal">
            {filteredProducts.length} productos encontrados
          </Typography>
        </div>
        <div className="flex w-full shrink-0 gap-2 md:w-max">
          <div className="w-full md:w-72">
            <Input
              label="Buscar producto..."
              icon={<i className="fas fa-search" />}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <Button
            color="blue"
            className="flex items-center gap-2"
            onClick={() => navigate('/dashboard/products/create')}
          >
            <PlusIcon className="h-4 w-4" />
            Nuevo
          </Button>
        </div>
      </div>

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
            {filteredProducts.map((product) => {
              const status = getStockStatus(product.stock);
              
              return (
                <tr key={product.productId}>
                  <td className="p-4">
                    <div>
                      <Typography variant="small" color="blue-gray" className="font-semibold">
                        {product.name}
                      </Typography>
                      <Typography variant="small" color="gray" className="text-xs">
                        {product.description || 'Sin descripción'}
                      </Typography>
                    </div>
                  </td>
                  <td className="p-4">
                    <Typography variant="small" color="blue-gray">
                      {product.barcode}
                    </Typography>
                  </td>
                  <td className="p-4">
                    <Typography variant="small" color="blue-gray">
                      {product.stock} unidades
                    </Typography>
                  </td>
                  <td className="p-4">
                    <Chip
                      value={status.label}
                      color={status.color}
                      size="sm"
                      className="w-fit"
                    />
                  </td>
                  <td className="p-4">
                    <div className="flex items-center gap-2">
                      <Tooltip content="Ver detalles">
                        <IconButton
                          variant="text"
                          color="blue-gray"
                          onClick={() => navigate(`/products/${product.productId}`)}
                        >
                          <EyeIcon className="h-4 w-4" />
                        </IconButton>
                      </Tooltip>
                      <Tooltip content="Editar">
                        <IconButton
                          variant="text"
                          color="blue-gray"
                          onClick={() => navigate(`/dashboard/products/edit/${product.productId}`)}
                        >
                          <PencilIcon className="h-4 w-4" />
                        </IconButton>
                      </Tooltip>
                      {isAdmin && (
                        <Tooltip content="Eliminar">
                          <IconButton
                            variant="text"
                            color="red"
                            onClick={() => handleDelete(product.productId)}
                          >
                            <TrashIcon className="h-4 w-4" />
                          </IconButton>
                        </Tooltip>
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
