import { useState, useEffect } from 'react';
import { productService } from '../services/productService';

export const useProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const data = await productService.getAll();
      setProducts(data);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const createProduct = async (productData) => {
    try {
      const newProduct = await productService.create(productData);
      setProducts([...products, newProduct]);
      return newProduct;
    } catch (err) {
      throw err;
    }
  };

  const updateProduct = async (id, productData) => {
    try {
      const updated = await productService.update(id, productData);
      setProducts(products.map(p => p.productId === id ? updated : p));
      return updated;
    } catch (err) {
      throw err;
    }
  };

  const deleteProduct = async (id) => {
    try {
      await productService.delete(id);
      setProducts(products.filter(p => p.productId !== id));
    } catch (err) {
      throw err;
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  return { products, loading, error, fetchProducts, createProduct, updateProduct, deleteProduct };
};