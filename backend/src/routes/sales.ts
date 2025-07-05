import { Router } from 'express';
import { 
    createSale,
    getAllSales,
    getSaleById,
    updateSale,
    deleteSale
} from '../controllers/salesController';

const router = Router();

router.post('/', createSale);
router.get('/', getAllSales);
router.get('/:id', getSaleById);
router.put('/:id', updateSale);
router.delete('/:id', deleteSale);

export default router; 