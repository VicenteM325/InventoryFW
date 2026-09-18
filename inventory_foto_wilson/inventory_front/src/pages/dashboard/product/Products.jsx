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
  EyeIcon,
  PlusIcon,
} from "@heroicons/react/24/outline";
import { productService } from "@/services/productService";
import { AuthContext } from "@/context/AuthContext"; 

const TABLE_HEAD = ['Producto', 'Código', 'Categoría', 'Precio', 'Proveedor', 'Stock', 'Estado', 'Acciones'];

const STOCK_STATUS_LABELS = {
  DISPONIBLE: { label: 'Disponible', color: 'green' },
  STOCK_BAJO: { label: 'Stock Bajo', color: 'amber' },
  AGOTADO: { label: 'Agotado', color: 'red' },
};

const CATEGORY_LABELS = {
  CELULAR: 'Celular',
  ACCESORIO: 'Accesorio',
};

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

  const toggleActive = async (product) => {
    const action = product.active ? 'desactivar' : 'activar';
    if (!window.confirm(`¿Seguro que quieres ${action} este producto?`)) return;
    try {
      if (product.active) {
        await productService.delete(product.productId);
      } else {
        await productService.activate(product.productId);
      }
      await fetchProducts();
    } catch (error) {
      console.error('Error toggling product active state:', error);
      alert(error.response?.data?.message || `Error al ${action} el producto`);
    }
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
              const status = STOCK_STATUS_LABELS[product.stockStatus] || { label: product.stockStatus, color: 'blue-gray' };

              return (
                <tr key={product.productId} className={product.active ? '' : 'opacity-60'}>
                  <td className="p-4">
                    <div>
                      <Typography variant="small" color="blue-gray" className="font-semibold">
                        {product.name}
                        {!product.active && (
                          <span className="ml-2 text-xs font-normal text-red-500">(Inactivo)</span>
                        )}
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
                      {product.category ? (CATEGORY_LABELS[product.category] || product.category) : '-'}
                    </Typography>
                  </td>
                  <td className="p-4">
                    <Typography variant="small" color="blue-gray">
                      Q{Number(product.price || 0).toFixed(2)}
                    </Typography>
                  </td>
                  <td className="p-4">
                    <Typography variant="small" color="blue-gray">
                      {product.supplierName || '-'}
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
                        <Button
                          size="sm"
                          variant="outlined"
                          color={product.active ? 'red' : 'green'}
                          onClick={() => toggleActive(product)}
                        >
                          {product.active ? 'Desactivar' : 'Activar'}
                        </Button>
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
