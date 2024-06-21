// controllers/acneDetectorController.js

const admin = require('firebase-admin');

// Inisialisasi Firebase Storage
const storage = admin.storage();
const bucket = storage.bucket();

// GET file acne_detector.tflite
const getAcneDetector = async (req, res) => {
  try {
    const fileName = 'machineLearning/acne_detector.tflite';
    const file = bucket.file(fileName);

    const exists = await file.exists();
    if (!exists[0]) {
      res.status(404).json({ message: 'File not found' });
      return;
    }

    const [metadata] = await file.getMetadata();
    const fileUrl = `https://storage.googleapis.com/${bucket.name}/${fileName}`;

    res.json({
      name: fileName,
      url: fileUrl,
      metadata: metadata
    });
  } catch (error) {
    console.error('Error getting file', error);
    res.status(500).send('Error fetching file');
  }
};

// POST unggah file acne_detector.tflite baru
const uploadAcneDetector = async (req, res) => {
  try {
    if (!req.file) {
      res.status(400).send('No file uploaded');
      return;
    }

    const fileName = 'machineLearning/acne_detector.tflite'; // Nama file tetap dengan folder machineLearning
    const fileUpload = bucket.file(fileName);
    const blobStream = fileUpload.createWriteStream({
      metadata: {
        contentType: req.file.mimetype
      }
    });

    blobStream.on('error', err => {
      console.error('Error uploading file', err);
      res.status(500).send('Error uploading file');
    });

    blobStream.on('finish', () => {
      fileUpload.makePublic().then(() => {
        const fileUrl = `https://storage.googleapis.com/${bucket.name}/${fileUpload.name}`;
        res.status(201).json({
          name: fileUpload.name,
          url: fileUrl
        });
      });
    });

    blobStream.end(req.file.buffer);
  } catch (error) {
    console.error('Error uploading file', error);
    res.status(500).send('Error uploading file');
  }
};

// DELETE file acne_detector.tflite
const deleteAcneDetector = async (req, res) => {
  try {
    const fileName = 'machineLearning/acne_detector.tflite';
    const file = bucket.file(fileName);

    const exists = await file.exists();
    if (!exists[0]) {
      res.status(404).json({ message: 'File not found' });
      return;
    }

    await file.delete();

    res.json({ message: 'File deleted successfully' });
  } catch (error) {
    console.error('Error deleting file', error);
    res.status(500).send('Error deleting file');
  }
};

module.exports = {
  getAcneDetector,
  uploadAcneDetector,
  deleteAcneDetector
};
