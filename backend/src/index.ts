import express, { Request, Response } from 'express';
import * as admin from 'firebase-admin';

// eslint-disable-next-line @typescript-eslint/no-var-requires
const serviceAccount = require('../serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://inkscape-ac4c3-default-rtdb.asia-southeast1.firebasedatabase.app'
});

const app = express();
const port = process.env.PORT || 3000;

app.use(express.json());

app.get('/', (req: Request, res: Response) => {
  res.send('Hello World! This is the Inkscape backend.');
});

import productRoutes from './routes/products';
app.use('/api/products', productRoutes);

import salesRoutes from './routes/sales';
app.use('/api/sales', salesRoutes);

app.get('/api/test', async (req: Request, res: Response) => {
    try {
        const db = admin.database();
        const ref = db.ref('/');
        const snapshot = await ref.once('value');
        res.status(200).json({
            message: "Successfully connected to Firebase and read data.",
            data: snapshot.val()
        });
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "An unknown error occurred";
        res.status(500).json({
            message: "Failed to connect to Firebase.",
            error: errorMessage
        });
    }
});

app.listen(port, () => {
  console.log(`Server is running at http://localhost:${port}`);
}); 