// routes/images.js

const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const multer = require('multer');

// Firebase Storage
const storage = admin.storage();
const bucket = storage.bucket();

// Middleware untuk menangani unggahan file dengan Multer
const upload = multer({
  storage: multer.memoryStorage(), // Penyimpanan sementara di memori untuk contoh ini
  limits: {
    fileSize: 5 * 1024 * 1024 // Batas ukuran file 5 MB
  }
});

// Controllers
const {
  getImages,
  getImageByName,
  uploadImage,
  deleteImage
} = require('../controllers/imagesController');

// Routes untuk images
router.get('/', getImages);
router.get('/:name', getImageByName);
router.post('/', upload.single('image'), uploadImage); // Memastikan upload.single('image') ditambahkan di sini
router.delete('/:name', deleteImage);

module.exports = router;
