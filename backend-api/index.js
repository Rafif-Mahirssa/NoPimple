const express = require('express');
const bodyParser = require('body-parser');
const admin = require('firebase-admin');
const multer = require('multer');
const { Storage } = require('@google-cloud/storage');

const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  storageBucket: 'acne-capstone.appspot.com', // Nama bucket Firebase Anda (tanpa gs://)
  databaseURL: 'https://acne-capstone.firebaseio.com'
});

const app = express();
const storage = new Storage({
  projectId: serviceAccount.project_id,
  credentials: {
    client_email: serviceAccount.client_email,
    private_key: serviceAccount.private_key.replace(/\\n/g, '\n')
  }
});

const bucket = storage.bucket('acne-capstone.appspot.com'); // Nama bucket Firebase Anda

app.use(bodyParser.json());

// Middleware untuk menangani unggahan file dengan Multer
const upload = multer({
  storage: multer.memoryStorage(), // Penyimpanan sementara di memori untuk contoh ini
  limits: {
    fileSize: 5 * 1024 * 1024 // Batas ukuran file 5 MB
  }
});

// Routes
const usersRoute = require('./routes/users');
const skinResultsRoute = require('./routes/skinResults');
const imagesRoute = require('./routes/images');

// Gunakan routes
app.use('/api/users', usersRoute);
app.use('/api/skin-results', skinResultsRoute);
app.use('/api/images', imagesRoute);

// Server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
});
