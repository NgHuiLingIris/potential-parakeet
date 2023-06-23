const express = require('express');

const financeControllers = require('../controllers/finance-controller');

const router = express.Router();

router.get('/currentbalance/get/:userId', financeControllers.getFinanceCurrentBalanceForUser);

module.exports = router;