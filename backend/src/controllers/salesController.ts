import { Request, Response } from 'express';
import * as admin from 'firebase-admin';

const db = admin.database();
const salesRef = db.ref('sales');
const productsRef = db.ref('products');

// Interface based on your screenshots
interface SaleProduct {
    productId: string;
    quantityUsed: number;
}

interface Sale {
    id: string;
    serviceName: string;
    category: string;
    customerName: string;
    date: string;
    paymentStatus: 'paid' | 'unpaid';
    productsUsed: SaleProduct[];
    totalAmount: number;
    amountPaid: number;
    paymentDate?: string;
    notes?: string;
}

export const createSale = async (req: Request, res: Response) => {
    try {
        const saleData: Omit<Sale, 'id' | 'date'> = req.body;

        // --- Data Validation ---
        if (!saleData.serviceName || !saleData.category || !saleData.paymentStatus || !saleData.totalAmount) {
            return res.status(400).json({ message: 'Missing required sale data.' });
        }

        // --- Transaction to update product stock and create sale ---
        if (saleData.productsUsed && saleData.productsUsed.length > 0) {
            for (const item of saleData.productsUsed) {
                const productRef = productsRef.child(item.productId);

                await productRef.transaction((product) => {
                    if (product) {
                        if (product.quantity >= item.quantityUsed) {
                            product.quantity -= item.quantityUsed;
                        } else {
                            // Abort transaction by returning undefined
                            return;
                        }
                    }
                    return product;
                });
            }
        }

        // --- Create the sale record ---
        const now = new Date().toISOString();
        const newSaleRef = salesRef.push();
        const newSale: Sale = {
            id: newSaleRef.key!,
            date: now,
            ...saleData,
            paymentDate: saleData.paymentStatus === 'paid' ? now : undefined,
        };
        await newSaleRef.set(newSale);

        res.status(201).json(newSale);

    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "An unknown error occurred";
        res.status(500).json({ message: "Failed to create sale.", error: errorMessage });
    }
};

export const getAllSales = async (req: Request, res: Response) => {
    try {
        const snapshot = await salesRef.once('value');
        res.status(200).json(snapshot.val());
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "An unknown error occurred";
        res.status(500).json({ message: "Failed to get sales.", error: errorMessage });
    }
};

export const getSaleById = async (req: Request, res: Response) => {
    try {
        const { id } = req.params;
        const snapshot = await salesRef.child(id).once('value');
        if (!snapshot.exists()) {
            return res.status(404).json({ message: 'Sale not found' });
        }
        res.status(200).json(snapshot.val());
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "An unknown error occurred";
        res.status(500).json({ message: "Failed to get sale.", error: errorMessage });
    }
};

export const updateSale = async (req: Request, res: Response) => {
    try {
        const { id } = req.params;
        const updates: Partial<Sale> = req.body;

        const snapshot = await salesRef.child(id).once('value');
        if (!snapshot.exists()) {
            return res.status(404).json({ message: 'Sale not found' });
        }

        await salesRef.child(id).update(updates);
        res.status(200).json({ message: 'Sale updated successfully' });
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "An unknown error occurred";
        res.status(500).json({ message: "Failed to update sale.", error: errorMessage });
    }
};

export const deleteSale = async (req: Request, res: Response) => {
    try {
        const { id } = req.params;
        
        const snapshot = await salesRef.child(id).once('value');
        if (!snapshot.exists()) {
            return res.status(404).json({ message: 'Sale not found' });
        }
        
        // Note: This does not automatically restock products.
        // A more complex implementation would be needed for that.
        await salesRef.child(id).remove();
        res.status(200).json({ message: 'Sale deleted successfully' });
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "An unknown error occurred";
        res.status(500).json({ message: "Failed to delete sale.", error: errorMessage });
    }
}; 