const admin = require('firebase-admin');

const db = admin.firestore();
const usersCollection = db.collection('users');

// GET all users
const getUsers = async (req, res) => {
  try {
    const snapshot = await usersCollection.get();
    const users = [];
    snapshot.forEach(doc => {
      users.push({
        id: doc.id,
        data: doc.data()
      });
    });
    res.json(users);
  } catch (error) {
    console.error('Error getting users', error);
    res.status(500).send('Error fetching users');
  }
};

// GET user by ID
const getUserById = async (req, res) => {
  try {
    const userId = req.params.id;
    const userDoc = await usersCollection.doc(userId).get();

    if (!userDoc.exists) {
      res.status(404).json({ message: 'User not found' });
    } else {
      res.json({
        id: userDoc.id,
        data: userDoc.data()
      });
    }
  } catch (error) {
    console.error('Error getting user by ID', error);
    res.status(500).send('Error fetching user');
  }
};

// POST create new user
const createUser = async (req, res) => {
  try {
    const newUser = req.body;

    // Tambahkan pengguna ke Firestore
    const docRef = await usersCollection.add(newUser);
    const doc = await docRef.get();
    const userId = doc.id;

    // Buat pengguna di Firebase Auth
    const { email, password } = newUser;
    const userRecord = await admin.auth().createUser({
      email,
      password,
    });

    // Tanggapan ke klien
    res.status(201).json({
      id: userId,
      data: doc.data(),
      firebaseUid: userRecord.uid, // Optional: kirim UID pengguna Firebase sebagai bagian dari respons
    });
  } catch (error) {
    console.error('Error creating user', error);
    res.status(500).send('Error creating user');
  }
};

// PUT update user by ID
const updateUser = async (req, res) => {
  try {
    const userId = req.params.id;
    const updatedUser = req.body;

    await usersCollection.doc(userId).set(updatedUser, { merge: true });
    res.json({ message: 'User updated successfully' });
  } catch (error) {
    console.error('Error updating user', error);
    res.status(500).send('Error updating user');
  }
};

// DELETE user by ID
const deleteUser = async (req, res) => {
  try {
    const userId = req.params.id;
    await usersCollection.doc(userId).delete();
    res.json({ message: 'User deleted successfully' });
  } catch (error) {
    console.error('Error deleting user', error);
    res.status(500).send('Error deleting user');
  }
};

module.exports = {
  getUsers,
  getUserById,
  createUser,
  updateUser,
  deleteUser
};
