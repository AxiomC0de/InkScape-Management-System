import { Request, Response } from 'express';
import * as admin from 'firebase-admin';

const db = admin.database();
const productsRef = db.ref('products');

interface Product {
    id?: string;
    name: string;
    quantity: number;
    price: number;
    category: string;
    status: 'available' | 'unavailable';
    description?: string;
    lowStockThreshold?: number;
    imageUrl?: string;
    createdAt?: string;
    updatedAt?: string;
}

export const addProduct = async (req: Request, res: Response) => {
    try {
        const newProductData: Omit<Product, 'status' | 'id' | 'createdAt' | 'updatedAt'> = req.body;
        if (!newProductData.name || newProductData.quantity == null || newProductData.price == null || !newProductData.category) {
            return res.status(400).json({ message: 'Missing required fields: name, quantity, price, category' });
        }

        const now = new Date().toISOString();
        const newProductRef = productsRef.push();
        const newProduct: Product = {
            id: newProductRef.key,
            ...newProductData,
            description: newProductData.description || '',
            lowStockThreshold: newProductData.lowStockThreshold || 0,
            imageUrl: newProductData.imageUrl || '',
            status: newProductData.quantity > 0 ? 'available' : 'unavailable',
            createdAt: now,
            updatedAt: now
        };
        await newProductRef.set(newProduct);

        res.status(201).json(newProduct);
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "An unknown error occurred";
        res.status(500).json({ message: "Failed to add product.", error: errorMessage });
    }
};

export const getAllProducts = async (req: Request, res: Response) => {
    try {
        const snapshot = await productsRef.once('value');
        res.status(200).json(snapshot.val());
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "An unknown error occurred";
        res.status(500).json({ message: "Failed to get products.", error: errorMessage });
    }
};

export const getProductById = async (req: Request, res: Response) => {
    try {
        const { id } = req.params;
        const snapshot = await productsRef.child(id).once('value');
        if (!snapshot.exists()) {
            return res.status(404).json({ message: 'Product not found' });
        }
        res.status(200).json(snapshot.val());
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "An unknown error occurred";
        res.status(500).json({ message: "Failed to get product.", error: errorMessage });
    }
};

export const updateProduct = async (req: Request, res: Response) => {
    try {
        const { id } = req.params;
        const updates: Partial<Omit<Product, 'status' | 'id' | 'createdAt' | 'updatedAt'>> = req.body;

        const snapshot = await productsRef.child(id).once('value');
        if (!snapshot.exists()) {
            return res.status(404).json({ message: 'Product not found' });
        }

        const existingProduct = snapshot.val() as Product;
        const newQuantity = updates.quantity ?? existingProduct.quantity;
        const finalUpdates: Partial<Product> = {
            ...updates,
            status: newQuantity > 0 ? 'available' : 'unavailable',
            updatedAt: new Date().toISOString()
        };

        await productsRef.child(id).update(finalUpdates);
        res.status(200).json({ message: 'Product updated successfully' });
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "An unknown error occurred";
        res.status(500).json({ message: "Failed to update product.", error: errorMessage });
    }
};

export const deleteProduct = async (req: Request, res: Response) => {
    try {
        const { id } = req.params;

        const snapshot = await productsRef.child(id).once('value');
        if (!snapshot.exists()) {
            return res.status(404).json({ message: 'Product not found' });
        }

        await productsRef.child(id).remove();
        res.status(200).json({ message: 'Product deleted successfully' });
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "An unknown error occurred";
        res.status(500).json({ message: "Failed to delete product.", error: errorMessage });
    }
}; 