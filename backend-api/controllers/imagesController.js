// controllers/imagesController.js

const admin = require('firebase-admin');
const serviceAccount = require('../serviceAccountKey.json');

// Inisialisasi Firebase Admin (jangan lakukan inisialisasi di sini)
const storage = admin.storage();
const bucket = storage.bucket();

// GET semua gambar
const getImages = async (req, res) => {
  try {
    const [files] = await bucket.getFiles();

    const images = files.map(file => {
      return {
        name: file.name,
        url: `https://storage.googleapis.com/${bucket.name}/${file.name}`
      };
    });

    res.json(images);
  } catch (error) {
    console.error('Error getting images', error);
    res.status(500).send('Error fetching images');
  }
};

// GET gambar berdasarkan nama
const getImageByName = async (req, res) => {
  try {
    const fileName = req.params.name;
    const file = bucket.file(fileName);

    const exists = await file.exists();
    if (!exists[0]) {
      res.status(404).json({ message: 'Image not found' });
      return;
    }

    const [metadata] = await file.getMetadata();
    const imageUrl = `https://storage.googleapis.com/${bucket.name}/${fileName}`;

    res.json({
      name: fileName,
      url: imageUrl,
      metadata: metadata
    });
  } catch (error) {
    console.error('Error getting image by name', error);
    res.status(500).send('Error fetching image');
  }
};

// POST unggah gambar baru
const uploadImage = async (req, res) => {
  try {
    if (!req.file) {
      res.status(400).send('No file uploaded');
      return;
    }

    const fileName = `images/${req.file.originalname}`; // Menambahkan 'images/' sebagai prefix
    const fileUpload = bucket.file(fileName);
    const blobStream = fileUpload.createWriteStream({
      metadata: {
        contentType: req.file.mimetype
      }
    });

    blobStream.on('error', err => {
      console.error('Error uploading image', err);
      res.status(500).send('Error uploading image');
    });

    blobStream.on('finish', () => {
      fileUpload.makePublic().then(() => {
        const imageUrl = `https://storage.googleapis.com/${bucket.name}/${fileUpload.name}`;
        res.status(201).json({
          name: fileUpload.name,
          url: imageUrl
        });
      });
    });

    blobStream.end(req.file.buffer);
  } catch (error) {
    console.error('Error uploading image', error);
    res.status(500).send('Error uploading image');
  }
};

// DELETE gambar berdasarkan nama
const deleteImage = async (req, res) => {
  try {
    const fileName = req.params.name;
    const file = bucket.file(fileName);

    const exists = await file.exists();
    if (!exists[0]) {
      res.status(404).json({ message: 'Image not found' });
      return;
    }

    await file.delete();

    res.json({ message: 'Image deleted successfully' });
  } catch (error) {
    console.error('Error deleting image', error);
    res.status(500).send('Error deleting image');
  }
};

module.exports = {
  getImages,
  getImageByName,
  uploadImage,
  deleteImage
};
