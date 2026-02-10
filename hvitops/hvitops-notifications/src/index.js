import express from 'express';
import redis from 'redis';
import { v4 as uuidv4 } from 'uuid';
import cors from 'cors';

const app = express();
const PORT = process.env.PORT || 8084;

// Redis client setup
const redisClient = redis.createClient({
  host: process.env.REDIS_HOST || 'redis',
  port: process.env.REDIS_PORT || 6379
});

redisClient.on('error', (err) => console.log('Redis Client Error', err));
redisClient.on('connect', () => console.log('Connected to Redis'));

await redisClient.connect();

// Middleware
app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS', 'PATCH'],
  allowedHeaders: ['Content-Type', 'Authorization'],
  credentials: false,
  maxAge: 3600
}));
app.use(express.json());

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'hvitops-notifications' });
});

// Create notification endpoint
app.post('/notifications', async (req, res) => {
  try {
    const { recipient, subject, body, type } = req.body;
    
    if (!recipient || !subject || !body) {
      return res.status(400).json({ error: 'Missing required fields' });
    }
    
    const notification = {
      id: uuidv4(),
      recipient,
      subject,
      body,
      type: type || 'email',
      status: 'pending',
      createdAt: new Date().toISOString()
    };
    
    // Push to Redis queue
    await redisClient.lPush('notification_queue', JSON.stringify(notification));
    
    console.log(`Notification queued: ${notification.id}`);
    
    res.status(201).json({
      message: 'Notification queued successfully',
      notificationId: notification.id
    });
  } catch (error) {
    console.error('Error creating notification:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Get notification status endpoint
app.get('/notifications/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const notification = await redisClient.get(`notification:${id}`);
    
    if (!notification) {
      return res.status(404).json({ error: 'Notification not found' });
    }
    
    res.json(JSON.parse(notification));
  } catch (error) {
    console.error('Error retrieving notification:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Process notifications from queue (simulated)
async function processNotifications() {
  while (true) {
    try {
      const notification = await redisClient.rPop('notification_queue');
      
      if (notification) {
        const notificationObj = JSON.parse(notification);
        
        // Simulate email sending
        console.log(`Sending ${notificationObj.type} to ${notificationObj.recipient}:`);
        console.log(`Subject: ${notificationObj.subject}`);
        console.log(`Body: ${notificationObj.body}`);
        
        // Store processed notification
        notificationObj.status = 'sent';
        notificationObj.sentAt = new Date().toISOString();
        await redisClient.set(
          `notification:${notificationObj.id}`,
          JSON.stringify(notificationObj),
          { EX: 86400 } // Expire after 24 hours
        );
      }
      
      // Wait before checking queue again
      await new Promise(resolve => setTimeout(resolve, 1000));
    } catch (error) {
      console.error('Error processing notification:', error);
    }
  }
}

// Start server
app.listen(PORT, () => {
  console.log(`HVitOps Notifications Service running on port ${PORT}`);
  processNotifications().catch(console.error);
});
