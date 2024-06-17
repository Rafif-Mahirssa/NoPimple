// controllers/skinResultsController.js

const admin = require('firebase-admin');

const db = admin.firestore();
const skinResultsCollection = db.collection('skinResults');

// GET all skin results
const getSkinResults = async (req, res) => {
  try {
    const snapshot = await skinResultsCollection.get();
    const skinResults = [];
    snapshot.forEach(doc => {
      skinResults.push({
        id: doc.id,
        data: doc.data()
      });
    });
    res.json(skinResults);
  } catch (error) {
    console.error('Error getting skin results', error);
    res.status(500).send('Error fetching skin results');
  }
};

// GET skin result by ID
const getSkinResultById = async (req, res) => {
  try {
    const skinResultId = req.params.id;
    const skinResultDoc = await skinResultsCollection.doc(skinResultId).get();

    if (!skinResultDoc.exists) {
      res.status(404).json({ message: 'Skin result not found' });
    } else {
      res.json({
        id: skinResultDoc.id,
        data: skinResultDoc.data()
      });
    }
  } catch (error) {
    console.error('Error getting skin result by ID', error);
    res.status(500).send('Error fetching skin result');
  }
};

// POST create new skin result
const createSkinResult = async (req, res) => {
  try {
    const newSkinResult = req.body;
    const docRef = await skinResultsCollection.add(newSkinResult);
    const doc = await docRef.get();

    res.status(201).json({
      id: doc.id,
      data: doc.data()
    });
  } catch (error) {
    console.error('Error creating skin result', error);
    res.status(500).send('Error creating skin result');
  }
};

// PUT update skin result by ID
const updateSkinResult = async (req, res) => {
  try {
    const skinResultId = req.params.id;
    const updatedSkinResult = req.body;

    await skinResultsCollection.doc(skinResultId).set(updatedSkinResult, { merge: true });
    res.json({ message: 'Skin result updated successfully' });
  } catch (error) {
    console.error('Error updating skin result', error);
    res.status(500).send('Error updating skin result');
  }
};

// DELETE skin result by ID
const deleteSkinResult = async (req, res) => {
  try {
    const skinResultId = req.params.id;
    await skinResultsCollection.doc(skinResultId).delete();
    res.json({ message: 'Skin result deleted successfully' });
  } catch (error) {
    console.error('Error deleting skin result', error);
    res.status(500).send('Error deleting skin result');
  }
};

module.exports = {
  getSkinResults,
  getSkinResultById,
  createSkinResult,
  updateSkinResult,
  deleteSkinResult
};
