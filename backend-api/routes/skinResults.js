// routes/skinResults.js

const express = require('express');
const router = express.Router();
const skinResultsController = require('../controllers/skinResultsController');

// GET all skin results
router.get('/', skinResultsController.getSkinResults);

// GET skin result by ID
router.get('/:id', skinResultsController.getSkinResultById);

// POST create new skin result
router.post('/', skinResultsController.createSkinResult);

// PUT update skin result by ID
router.put('/:id', skinResultsController.updateSkinResult);

// DELETE skin result by ID
router.delete('/:id', skinResultsController.deleteSkinResult);

module.exports = router;
