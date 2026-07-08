import express from 'express';
import path from 'path';
import fs from 'fs';
import dotenv from 'dotenv';
import mysql, { Connection } from 'mysql2/promise';
import { WebSocketServer, WebSocket } from 'ws';
import crypto from 'crypto';
import cors from 'cors';

dotenv.config();

const app = express();
app.use(cors());

/**
 * Utility function to hash password using SHA-256
 */
function hashPassword(password: string): string {
  return crypto.createHash('sha256').update(password).digest('hex');
}

const PORT = 3000;

app.use(express.json({ limit: '10mb' }));

// File-based Database Path for Sandbox Local Fallback
const LOCAL_DB_PATH = path.join(process.cwd(), 'data_store.json');

// --- DATABASE STATE & MIGRATION SCRIPT ---
let isUsingMySQL = false;
let mysqlPool: mysql.Pool | null = null;

// Default initial data for the application
const initialData = {
  users: [
    {
      id: 'usr-1',
      name: 'Habibie',
      email: 'habibie@bibcommunity.com',
      phone: '+62 812 3456 7890',
      dob: '2001-08-17',
      bio: 'BIB Awardee 2023 - MIT. Student interested in sustainable design and green technology.',
      city: 'Bandung, West Java',
      campus: 'Bandung Institute of Technology',
      faculty: 'Faculty of Art and Design',
      studyProgram: 'Visual Communication Design',
      classYear: '2022',
      points: 12450,
      nongkiHours: 8.75,
      ktmStatus: 'Verified' as 'Unverified' | 'Pending' | 'Verified',
      avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&q=80',
      authProvider: 'local' as 'local' | 'google',
      password: hashPassword('password123'), // default password hashed
      ktmPhoto: '' as string | undefined,
      selfiePhoto: '' as string | undefined
    }
  ],
  spots: [
    {
      id: 'spot-1',
      name: 'Cafe Sudut',
      type: 'Cafe' as const,
      address: 'Jl. Senopati No.12, Kebayoran Baru, Jakarta Selatan',
      lat: -6.2235,
      lng: 106.8080,
      wifi: true,
      rating: 4.8,
      image: 'https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=500&q=80'
    },
    {
      id: 'spot-2',
      name: 'Perpus Nasional',
      type: 'Perpus' as const,
      address: 'Jl. Medan Merdeka Sel. No.11, Gambir, Jakarta Pusat',
      lat: -6.1805,
      lng: 106.8272,
      wifi: true,
      rating: 4.9,
      image: 'https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=500&q=80'
    },
    {
      id: 'spot-3',
      name: 'Taman Suropati',
      type: 'Taman' as const,
      address: 'Jl. Taman Suropati, Menteng, Jakarta Pusat',
      lat: -6.2008,
      lng: 106.8326,
      wifi: false,
      rating: 4.6,
      image: 'https://images.unsplash.com/photo-1519331379826-f10be5486c6f?w=500&q=80'
    }
  ],
  events: [
    {
      id: 'event-1',
      title: 'Networking Brunch',
      type: 'event' as const,
      category: 'Local' as const,
      description: 'Experience a casual networking brunch with other BIB awardees in South Jakarta. Discuss study hacks, exchange stories, and connect.',
      dateOrDeadline: 'This Weekend',
      locationName: 'Senopati Spot',
      lat: -6.2235,
      lng: 106.8080,
      image: 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=500&q=80',
      status: 'open' as 'open' | 'closed' | 'joined' | 'registered',
      organizer: 'BIB Community'
    },
    {
      id: 'event-2',
      title: 'Design Thinking Workshop',
      type: 'event' as const,
      category: 'Online' as const,
      description: 'Learn the foundations of user-centered design and creative problem-solving from industry experts.',
      dateOrDeadline: 'Fri, 30 Oct',
      locationName: 'Virtual Hub',
      lat: -6.2088,
      lng: 106.8456,
      image: 'https://images.unsplash.com/photo-1531403009284-440f080d1e12?w=500&q=80',
      status: 'open' as const,
      organizer: 'Creative Hub Asia'
    },
    {
      id: 'event-3',
      title: 'Morning Coffee Walk',
      type: 'event' as const,
      category: 'Local' as const,
      description: 'Join us for a relaxing morning stroll through the botanical gardens followed by artisanal coffee and connections.',
      dateOrDeadline: 'Sat, 24 Oct',
      locationName: 'Central Park Pavilion (Offline)',
      lat: -6.1754,
      lng: 106.8272,
      image: 'https://images.unsplash.com/photo-1498804103079-a6351b050096?w=500&q=80',
      status: 'open' as const,
      organizer: 'BIB Sports Group'
    },
    {
      id: 'event-4',
      title: 'Tech Innovators Scholarship 2024',
      type: 'scholarship' as const,
      category: 'Online' as const,
      description: 'The premier funding and acceleration program organized by Global Tech Foundation for outstanding technology students in Indonesia.',
      dateOrDeadline: 'Deadline: 15 Nov 2024',
      locationName: 'Global Tech Foundation',
      lat: -6.1954,
      lng: 106.8222,
      image: 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=500&q=80',
      status: 'open' as const,
      organizer: 'Global Tech Foundation'
    },
    {
      id: 'event-5',
      title: 'Design Leadership Program',
      type: 'scholarship' as const,
      category: 'Online' as const,
      description: 'A cohort-based scholarship designed for promising product design students who want to develop essential project management and leadership skills.',
      dateOrDeadline: 'Deadline: 01 Oct 2024',
      locationName: 'Creative Hub Asia',
      lat: -6.1954,
      lng: 106.8222,
      image: '',
      status: 'closed' as const,
      organizer: 'Creative Hub Asia'
    }
  ],
  chats: [
    {
      id: 'chat-1',
      isGroup: false,
      name: 'Zhou Koo Wii',
      avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&q=80',
      lastMessage: 'Besok jadi nongki di tempat biasa?',
      lastTime: '10:42 AM',
      unreadCount: 2
    },
    {
      id: 'chat-2',
      isGroup: false,
      name: 'Prabowo',
      avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&q=80',
      lastMessage: 'Jangan lupa besok kumpul j...',
      lastTime: 'Yesterday',
      unreadCount: 0
    },
    {
      id: 'chat-3',
      isGroup: true,
      name: 'Grup BIB Tebet',
      avatar: 'https://images.unsplash.com/photo-1582213782179-e0d53f98f2ca?w=150&q=80',
      lastMessage: 'Andi: Siap, nanti saya bawa doku...',
      lastTime: 'Tuesday',
      unreadCount: 0
    }
  ],
  messages: [
    {
      id: 'msg-1',
      chatId: 'chat-1',
      senderName: 'Zhou Koo Wii',
      senderAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&q=80',
      text: 'Halo Habibie! Bagaimana kabarmu?',
      timestamp: '09:00 AM',
      isMe: false
    },
    {
      id: 'msg-2',
      chatId: 'chat-1',
      senderName: 'Habibie',
      senderAvatar: '',
      text: 'Kabar baik Zhou! Ada apa?',
      timestamp: '09:05 AM',
      isMe: true
    },
    {
      id: 'msg-3',
      chatId: 'chat-1',
      senderName: 'Zhou Koo Wii',
      senderAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&q=80',
      text: 'Besok jadi nongki di tempat biasa?',
      timestamp: '10:42 AM',
      isMe: false
    },
    {
      id: 'msg-4',
      chatId: 'chat-2',
      senderName: 'Prabowo',
      senderAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&q=80',
      text: 'Halo Bib, jangan lupa besok kumpul jam 1 siang ya!',
      timestamp: 'Yesterday',
      isMe: false
    },
    {
      id: 'msg-5',
      chatId: 'chat-3',
      senderName: 'Andi',
      senderAvatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&q=80',
      text: 'Siap, nanti saya bawa dokumen yang diperlukan untuk pendaftaran event.',
      timestamp: 'Tuesday',
      isMe: false
    }
  ],
  discussions: [
    {
      id: 'disc-1',
      title: 'Photography Enthusiasts',
      snippet: 'Anyone joining the photowalk tomorrow?',
      lastTime: '10:42 AM',
      iconColor: 'bg-lime-100 text-lime-700',
      category: 'Hub'
    },
    {
      id: 'disc-2',
      title: 'Weekend Coffee Club',
      snippet: 'New spot opening downtown, lets check it out!',
      lastTime: 'Yesterday',
      iconColor: 'bg-amber-100 text-amber-700',
      category: 'Hub'
    },
    {
      id: 'disc-3',
      title: 'Hiking & Nature',
      snippet: 'Trail conditions were perfect this morning.',
      lastTime: 'Mon',
      iconColor: 'bg-emerald-100 text-emerald-700',
      category: 'Hub'
    }
  ],
  user_locations: [] as Array<{
    userId: string;
    name: string;
    avatar: string;
    lat: number;
    lng: number;
    updatedAt: string;
  }>
};

// Initial state loaded in memory
let dbState = { ...initialData };

// Load local database or initialize it
function loadLocalDatabase() {
  try {
    if (fs.existsSync(LOCAL_DB_PATH)) {
      const data = fs.readFileSync(LOCAL_DB_PATH, 'utf-8');
      dbState = JSON.parse(data);
      console.log('Successfully loaded local JSON database state.');
    } else {
      fs.writeFileSync(LOCAL_DB_PATH, JSON.stringify(initialData, null, 2));
      console.log('Initialized default data_store.json file.');
    }
  } catch (err) {
    console.error('Error handling local JSON database. Using memory fallback.', err);
  }
}

function saveLocalDatabase() {
  try {
    fs.writeFileSync(LOCAL_DB_PATH, JSON.stringify(dbState, null, 2));
  } catch (err) {
    console.error('Failed to save to local database file.', err);
  }
}

// Attempt Connection to MySQL if Env variables provided
async function initializeDatabase() {
  loadLocalDatabase();

  const dbHost = process.env.DB_HOST;
  const dbUser = process.env.DB_USER;
  const dbPass = process.env.DB_PASSWORD;
  const dbName = process.env.DB_DATABASE || 'nongkibib_db';
  const dbPort = parseInt(process.env.DB_PORT || '3306');

  if (dbHost && dbUser) {
    try {
      console.log(`Attempting connection to MySQL Database at ${dbHost}:${dbPort}...`);
      mysqlPool = mysql.createPool({
        host: dbHost,
        user: dbUser,
        password: dbPass,
        database: dbName,
        port: dbPort,
        waitForConnections: true,
        connectionLimit: 10,
        queueLimit: 0
      });

      // Simple test query to ensure connection works
      await mysqlPool.query('SELECT 1');
      isUsingMySQL = true;
      console.log('CONNECTED successfully to MySQL database!');

      // Let\'s run initial table creations if needed (Relational Mode)
      await setupMySQLTables();
    } catch (err) {
      console.error('MySQL Connection failed. Falling back to JSON database state.', err);
      isUsingMySQL = false;
    }
  } else {
    console.log('No DB_HOST / DB_USER credentials provided in environment. Operating in JSON File Fallback mode.');
  }
}

async function setupMySQLTables() {
  if (!mysqlPool) return;
  try {
    await mysqlPool.query(`
      CREATE TABLE IF NOT EXISTS users (
        id VARCHAR(255) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        email VARCHAR(255) UNIQUE NOT NULL,
        phone VARCHAR(50),
        dob VARCHAR(50),
        bio TEXT,
        city VARCHAR(255),
        campus VARCHAR(255),
        faculty VARCHAR(255),
        study_program VARCHAR(255),
        class_year VARCHAR(10),
        points INT DEFAULT 0,
        nongki_hours DOUBLE DEFAULT 0,
        ktm_status VARCHAR(50) DEFAULT 'Unverified',
        ktm_photo LONGTEXT,
        selfie_photo LONGTEXT,
        avatar LONGTEXT,
        auth_provider VARCHAR(50) DEFAULT 'local',
        password VARCHAR(255)
      )
    `);

    // Ensure missing columns are added if the table already existed
    try {
      await mysqlPool.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255)');
      await mysqlPool.query('ALTER TABLE users ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(50) DEFAULT "local"');
    } catch (colErr) {
      // Ignore if columns already exist or if IF NOT EXISTS isn't supported (use older syntax if needed)
      console.log('Column check finished.');
    }

    await mysqlPool.query(`
      CREATE TABLE IF NOT EXISTS spots (
        id VARCHAR(255) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        type VARCHAR(50) NOT NULL,
        address TEXT NOT NULL,
        lat DOUBLE NOT NULL,
        lng DOUBLE NOT NULL,
        wifi BOOLEAN DEFAULT TRUE,
        rating DOUBLE DEFAULT 4.5,
        image TEXT
      )
    `);

    await mysqlPool.query(`
      CREATE TABLE IF NOT EXISTS events (
        id VARCHAR(255) PRIMARY KEY,
        title VARCHAR(255) NOT NULL,
        type VARCHAR(50) NOT NULL,
        category VARCHAR(50) NOT NULL,
        description TEXT,
        date_or_deadline VARCHAR(100) NOT NULL,
        location_name VARCHAR(255) NOT NULL,
        lat DOUBLE NOT NULL,
        lng DOUBLE NOT NULL,
        image TEXT,
        status VARCHAR(50) DEFAULT 'open',
        organizer VARCHAR(255)
      )
    `);

    await mysqlPool.query(`
      CREATE TABLE IF NOT EXISTS chats (
        id VARCHAR(255) PRIMARY KEY,
        is_group BOOLEAN DEFAULT FALSE,
        name VARCHAR(255) NOT NULL,
        avatar TEXT,
        last_message TEXT,
        last_time VARCHAR(50),
        unread_count INT DEFAULT 0
      )
    `);

    await mysqlPool.query(`
      CREATE TABLE IF NOT EXISTS messages (
        id VARCHAR(255) PRIMARY KEY,
        chat_id VARCHAR(255) NOT NULL,
        sender_name VARCHAR(255) NOT NULL,
        sender_avatar TEXT,
        text TEXT NOT NULL,
        timestamp VARCHAR(50) NOT NULL
      )
    `);

    await mysqlPool.query(`
      CREATE TABLE IF NOT EXISTS discussions (
        id VARCHAR(255) PRIMARY KEY,
        title VARCHAR(255) NOT NULL,
        snippet TEXT NOT NULL,
        last_time VARCHAR(50) NOT NULL,
        icon_color VARCHAR(50) NOT NULL,
        category VARCHAR(100) NOT NULL
      )
    `);

    await mysqlPool.query(`
      CREATE TABLE IF NOT EXISTS scholarship_registrations (
        id VARCHAR(255) PRIMARY KEY,
        user_id VARCHAR(255) NOT NULL,
        scholarship_id VARCHAR(255) NOT NULL,
        status VARCHAR(50) DEFAULT 'Pending',
        motivation_letter TEXT,
        gpa_score DOUBLE,
        document_url TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    await mysqlPool.query(`
      CREATE TABLE IF NOT EXISTS event_registrations (
        id VARCHAR(255) PRIMARY KEY,
        user_id VARCHAR(255) NOT NULL,
        event_id VARCHAR(255) NOT NULL,
        ticket_code VARCHAR(100) NOT NULL,
        status VARCHAR(50) DEFAULT 'Joined',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    // Let's populate the MySQL tables if they are empty
    const [userRows] = await mysqlPool.query<any[]>('SELECT COUNT(*) as count FROM users');
    if (userRows[0].count === 0) {
      console.log('Seeding MySQL Database with default NongkiBib records...');
      // Seed Users
      for (const u of dbState.users) {
        await mysqlPool.query(
          'INSERT INTO users (id, name, email, phone, dob, bio, city, campus, faculty, study_program, class_year, points, nongki_hours, ktm_status, avatar, auth_provider, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
          [u.id, u.name, u.email, u.phone, u.dob, u.bio, u.city, u.campus, u.faculty, u.studyProgram, u.classYear, u.points, u.nongkiHours, u.ktmStatus, u.avatar, u.authProvider, u.password]
        );
      }
    }

    const [spotsCountRows] = await mysqlPool.query<any[]>('SELECT COUNT(*) as count FROM spots');
    if (spotsCountRows[0].count === 0) {
      console.log('Seeding spots table in MySQL...');
      // Seed Spots
      for (const s of dbState.spots) {
        await mysqlPool.query(
          'INSERT INTO spots (id, name, type, address, lat, lng, wifi, rating, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
          [s.id, s.name, s.type, s.address, s.lat, s.lng, s.wifi, s.rating, s.image]
        );
      }
    }

    const [eventsCountRows] = await mysqlPool.query<any[]>('SELECT COUNT(*) as count FROM events');
    if (eventsCountRows[0].count === 0) {
      console.log('Seeding events table in MySQL...');
      // Seed Events
      for (const e of dbState.events) {
        await mysqlPool.query(
          'INSERT INTO events (id, title, type, category, description, date_or_deadline, location_name, lat, lng, image, status, organizer) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
          [e.id, e.title, e.type, e.category, e.description, e.dateOrDeadline, e.locationName, e.lat, e.lng, e.image, e.status, e.organizer]
        );
      }
    }

    const [chatsCountRows] = await mysqlPool.query<any[]>('SELECT COUNT(*) as count FROM chats');
    if (chatsCountRows[0].count === 0) {
      console.log('Seeding MySQL Database with default chats...');
      for (const c of dbState.chats) {
        await mysqlPool.query(
          'INSERT INTO chats (id, is_group, name, avatar, last_message, last_time, unread_count) VALUES (?, ?, ?, ?, ?, ?, ?)',
          [c.id, c.isGroup ? 1 : 0, c.name, c.avatar, c.lastMessage, c.lastTime, c.unreadCount]
        );
      }
    }

    const [messagesCountRows] = await mysqlPool.query<any[]>('SELECT COUNT(*) as count FROM messages');
    if (messagesCountRows[0].count === 0) {
      console.log('Seeding MySQL Database with default messages...');
      for (const m of dbState.messages) {
        await mysqlPool.query(
          'INSERT INTO messages (id, chat_id, sender_name, sender_avatar, text, timestamp) VALUES (?, ?, ?, ?, ?, ?)',
          [m.id, m.chatId, m.senderName, m.senderAvatar, m.text, m.timestamp]
        );
      }
    }

    const [discussionsCountRows] = await mysqlPool.query<any[]>('SELECT COUNT(*) as count FROM discussions');
    if (discussionsCountRows[0].count === 0) {
      console.log('Seeding MySQL Database with default discussions...');
      for (const d of dbState.discussions) {
        await mysqlPool.query(
          'INSERT INTO discussions (id, title, snippet, last_time, icon_color, category) VALUES (?, ?, ?, ?, ?, ?)',
          [d.id, d.title, d.snippet, d.lastTime, d.iconColor, d.category]
        );
      }
    }
  } catch (err) {
    console.error('Error migrating/seeding MySQL database.', err);
  }
}

// --- ACTIVE SESSION SIMULATOR ---
let currentSessionUser: typeof dbState.users[0] | null = null;

// Helper to resolve the user
async function getLoggedUser() {
  if (!currentSessionUser) return null;
  if (isUsingMySQL && mysqlPool) {
    try {
      const [rows] = await mysqlPool.query<any[]>('SELECT * FROM users WHERE id = ?', [currentSessionUser.id]);
      if (rows[0]) {
        const u = rows[0];
        return {
          id: u.id,
          name: u.name,
          email: u.email,
          phone: u.phone,
          dob: u.dob,
          bio: u.bio,
          city: u.city,
          campus: u.campus,
          faculty: u.faculty,
          studyProgram: u.study_program,
          classYear: u.class_year,
          points: u.points,
          nongkiHours: u.nongki_hours,
          ktmStatus: u.ktm_status,
          avatar: u.avatar,
          authProvider: u.auth_provider,
        };
      }
    } catch (err) {
      console.error('MySQL current user fetch failed. Falling back to local state.', err);
    }
  }
  // Fallback
  return dbState.users.find(u => u.id === currentSessionUser?.id) || null;
}

// Start DB Initialization
initializeDatabase();


// --- GOOGLE OAUTH FLOW API ROUTES (OAUTH-INTEGRATION SKILL) ---

app.get('/api/auth/google/url', (req, res) => {
  const isOAuthConfigured = Boolean(process.env.GOOGLE_CLIENT_ID && process.env.GOOGLE_CLIENT_SECRET);
  const redirectUri = `${req.protocol}://${req.get('host')}/auth/callback`;

  if (isOAuthConfigured) {
    // Generate actual Google OAuth authorization URL
    const googleAuthUrl = 'https://accounts.google.com/o/oauth2/v2/auth';
    const params = new URLSearchParams({
      client_id: process.env.GOOGLE_CLIENT_ID!,
      redirect_uri: redirectUri,
      response_type: 'code',
      scope: 'openid profile email',
      prompt: 'consent',
    });
    res.json({ url: `${googleAuthUrl}?${params}` });
  } else {
    // No secrets configured: return a simulated MOCK_OAUTH code parameter directly
    console.log('Google Client Secrets not set. Using beautiful Simulated Auth Portal.');
    const mockAuthUrl = `/api/auth/google/mock-login?redirect_uri=${encodeURIComponent(redirectUri)}`;
    res.json({ url: mockAuthUrl });
  }
});

// Mock login portal used when actual keys are absent (safe development UX)
app.get('/api/auth/google/mock-login', (req, res) => {
  const redirectUri = req.query.redirect_uri as string;
  res.send(`
    <html>
      <head>
        <title>Google Sign In Simulator</title>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
        <script src="https://cdn.tailwindcss.com"></script>
      </head>
      <body class="bg-gray-50 flex items-center justify-center min-h-screen font-['Inter']">
        <div class="bg-white p-8 rounded-2xl shadow-xl max-w-sm w-full border border-gray-100 text-center">
          <div class="flex justify-center mb-4">
            <svg class="w-12 h-12" viewBox="0 0 24 24">
              <path fill="#EA4335" d="M12 5.04c1.61 0 3.06.56 4.2 1.64l3.15-3.15C17.43 1.68 14.9 1 12 1 7.24 1 3.22 3.73 1.34 7.74l3.74 2.9C6.01 7.15 8.78 5.04 12 5.04z"/>
              <path fill="#4285F4" d="M23.49 12.27c0-.81-.07-1.59-.2-2.36H12v4.51h6.46c-.29 1.48-1.14 2.73-2.4 3.58l3.73 2.89c2.18-2.01 3.7-4.99 3.7-8.62z"/>
              <path fill="#FBBC05" d="M5.08 14.84a7.087 7.087 0 0 1 0-4.32L1.34 7.62a11.977 11.977 0 0 0 0 8.76l3.74-1.54z"/>
              <path fill="#34A853" d="M12 23c3.24 0 5.97-1.07 7.96-2.91l-3.73-2.89c-1.04.7-2.37 1.1-4.23 1.1-3.22 0-5.99-2.11-6.92-5.6l-3.74 2.9C3.22 20.27 7.24 23 12 23z"/>
            </svg>
          </div>
          <h2 class="text-xl font-semibold text-gray-800 mb-2">Google Sign In Simulator</h2>
          <p class="text-sm text-gray-500 mb-6 leading-relaxed">Sign in with a pre-configured BIB scholarship awardee account directly without API secrets.</p>

          <div class="space-y-3">
            <button onclick="selectUser('Habibie', 'habibie@bibcommunity.com', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&amp;q=80')" class="w-full py-2.5 px-4 border border-gray-200 rounded-xl hover:bg-gray-50 transition-colors flex items-center justify-between text-left">
              <div>
                <p class="text-sm font-medium text-gray-800">Habibie (Awardee)</p>
                <p class="text-xs text-gray-400">habibie@bibcommunity.com</p>
              </div>
              <span class="text-xs text-amber-600 bg-amber-50 px-2.5 py-1 rounded-full font-medium">Awardee</span>
            </button>
            <button onclick="selectUser('Arjuna Pratama', 'arjuna.pratama@itb.ac.id', 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150&amp;q=80')" class="w-full py-2.5 px-4 border border-gray-200 rounded-xl hover:bg-gray-50 transition-colors flex items-center justify-between text-left">
              <div>
                <p class="text-sm font-medium text-gray-800">Arjuna Pratama</p>
                <p class="text-xs text-gray-400">arjuna.pratama@itb.ac.id</p>
              </div>
              <span class="text-xs text-indigo-600 bg-indigo-50 px-2.5 py-1 rounded-full font-medium">New User</span>
            </button>
          </div>

          <script>
            function selectUser(name, email, avatar) {
              const url = new URL('${redirectUri}');
              url.searchParams.set('code', 'mock-code-123');
              url.searchParams.set('mock_name', name);
              url.searchParams.set('mock_email', email);
              url.searchParams.set('mock_avatar', avatar);
              window.location.href = url.toString();
            }
          </script>
        </div>
      </body>
    </html>
  `);
});

// OAuth Callback handler with pop-up close trigger
app.get(['/auth/callback', '/auth/callback/'], async (req, res) => {
  const { code, mock_name, mock_email, mock_avatar } = req.query;

  let loggedUser: any = null;

  if (code === 'mock-code-123' && mock_email) {
    // Handle mock login scenario (safe sandbox mode)
    const emailStr = String(mock_email);
    const existingUser = dbState.users.find(u => u.email.toLowerCase() === emailStr.toLowerCase());

    if (existingUser) {
      loggedUser = existingUser;
    } else {
      // Register a brand new simulated Google user on the fly
      loggedUser = {
        id: `usr-google-${Date.now()}`,
        name: String(mock_name || 'BIB Awardee'),
        email: emailStr,
        phone: '+62 812 9999 8888',
        dob: '2002-12-10',
        bio: 'Freshly registered Google BIB Awardee.',
        city: 'Bandung, West Java',
        campus: 'Bandung Institute of Technology',
        faculty: 'Faculty of Art and Design',
        studyProgram: 'Visual Communication Design',
        classYear: '2022',
        points: 0,
        nongkiHours: 0,
        ktmStatus: 'Pending' as const,
        avatar: String(mock_avatar || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&q=80'),
        authProvider: 'google' as const,
        ktmPhoto: '',
        selfiePhoto: ''
      };
      dbState.users.push(loggedUser);
      saveLocalDatabase();

      if (isUsingMySQL && mysqlPool) {
        try {
          await mysqlPool.query(
            'INSERT INTO users (id, name, email, phone, dob, bio, city, campus, faculty, study_program, class_year, points, nongki_hours, ktm_status, avatar, auth_provider) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [loggedUser.id, loggedUser.name, loggedUser.email, loggedUser.phone, loggedUser.dob, loggedUser.bio, loggedUser.city, loggedUser.campus, loggedUser.faculty, loggedUser.studyProgram, loggedUser.classYear, loggedUser.points, loggedUser.nongkiHours, loggedUser.ktmStatus, loggedUser.avatar, loggedUser.authProvider]
          );
        } catch (err) {
          console.error('MySQL user insert error during mock auth callback', err);
        }
      }
    }
  } else if (code) {
    // Live Google OAuth flow
    try {
      const redirectUri = `${req.protocol}://${req.get('host')}/auth/callback`;
      // 1. Exchange OAuth code for tokens
      const tokenResponse = await fetch('https://oauth2.googleapis.com/token', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
          code: String(code),
          client_id: process.env.GOOGLE_CLIENT_ID!,
          client_secret: process.env.GOOGLE_CLIENT_SECRET!,
          redirect_uri: redirectUri,
          grant_type: 'authorization_code'
        })
      });

      if (!tokenResponse.ok) {
        throw new Error('Failed to exchange code for tokens');
      }

      const tokens = await tokenResponse.json();

      // 2. Fetch user profile from google apis
      const profileResponse = await fetch('https://www.googleapis.com/oauth2/v3/userinfo', {
        headers: { Authorization: `Bearer ${tokens.access_token}` }
      });

      if (profileResponse.ok) {
        const googleProfile = await profileResponse.json();
        const emailStr = googleProfile.email.toLowerCase();

        const existingUser = dbState.users.find(u => u.email.toLowerCase() === emailStr);
        if (existingUser) {
          loggedUser = existingUser;
        } else {
          loggedUser = {
            id: `usr-google-${Date.now()}`,
            name: googleProfile.name || 'Google Awardee',
            email: emailStr,
            phone: '+62 811 1111 2222',
            dob: '2001-01-01',
            bio: 'BIB Awardee joined via official Google Auth.',
            city: 'Jakarta, Indonesia',
            campus: 'Universitas Indonesia',
            faculty: 'Faculty of Engineering',
            studyProgram: 'Computer Science',
            classYear: '2021',
            points: 100,
            nongkiHours: 0,
            ktmStatus: 'Pending' as const,
            avatar: googleProfile.picture || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&q=80',
            authProvider: 'google' as const,
            ktmPhoto: '',
            selfiePhoto: ''
          };
          dbState.users.push(loggedUser);
          saveLocalDatabase();

          if (isUsingMySQL && mysqlPool) {
            try {
              await mysqlPool.query(
                'INSERT INTO users (id, name, email, phone, dob, bio, city, campus, faculty, study_program, class_year, points, nongki_hours, ktm_status, avatar, auth_provider) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
                [loggedUser.id, loggedUser.name, loggedUser.email, loggedUser.phone, loggedUser.dob, loggedUser.bio, loggedUser.city, loggedUser.campus, loggedUser.faculty, loggedUser.studyProgram, loggedUser.classYear, loggedUser.points, loggedUser.nongkiHours, loggedUser.ktmStatus, loggedUser.avatar, loggedUser.authProvider]
              );
            } catch (err) {
              console.error('MySQL user insert error during live Google callback', err);
            }
          }
        }
      }
    } catch (err) {
      console.error('Google OAuth Exchange failed', err);
    }
  }

  // Set the current session user reference
  if (loggedUser) {
    currentSessionUser = loggedUser;
  }

  // Send communication event to React app in iframe, then auto close popup
  res.send(`
    <html>
      <body class="flex flex-col items-center justify-center min-h-screen font-sans bg-gray-50 text-gray-700 text-center">
        <script>
          if (window.opener) {
            window.opener.postMessage({ type: 'OAUTH_AUTH_SUCCESS' }, '*');
            window.close();
          } else {
            window.location.href = '/';
          }
        </script>
        <div class="p-6 bg-white rounded-2xl shadow-md border border-gray-100 max-w-sm">
          <p class="text-emerald-500 font-semibold text-lg mb-2">✓ Autentikasi Berhasil</p>
          <p class="text-sm text-gray-500">Menghubungkan akun Anda kembali ke aplikasi. Jendela ini akan tertutup otomatis...</p>
        </div>
      </body>
    </html>
  `);
});


// --- STANDARD BACKEND CRUD ENDPOINTS ---

// Get current logged-in user session
app.get('/api/auth/current-user', async (req, res) => {
  const user = await getLoggedUser();
  if (user) {
    res.json({ success: true, user });
  } else {
    res.json({ success: false, message: 'Not authenticated' });
  }
});

// Email-Password Login fallback (Local auth)
app.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ success: false, message: 'Silakan isi email dan password.' });
  }

  const hashedInput = hashPassword(password);
  const user = dbState.users.find(
    u => u.email.toLowerCase() === email.toLowerCase() && u.password === hashedInput
  );

  if (user) {
    currentSessionUser = user;
    res.json({ success: true, user });
  } else {
    res.status(401).json({ success: false, message: 'Email atau password salah.' });
  }
});

// Sign Up / Register new account (Local auth)
app.post('/api/auth/register', async (req, res) => {
  const { name, email, phone, password } = req.body;

  if (!name || !email || !password) {
    return res.status(400).json({ success: false, message: 'Nama, Email, dan Password wajib diisi.' });
  }

  const userExists = dbState.users.some(u => u.email.toLowerCase() === email.toLowerCase());
  if (userExists) {
    return res.status(400).json({ success: false, message: 'Email sudah terdaftar.' });
  }

  const newUser = {
    id: `usr-${Date.now()}`,
    name,
    email,
    phone: phone || '',
    dob: '',
    bio: 'Penerima beasiswa Beasiswa Indonesia Bangkit (BIB).',
    city: 'Bandung, West Java',
    campus: 'Bandung Institute of Technology',
    faculty: 'Faculty of Art and Design',
    studyProgram: 'Visual Communication Design',
    classYear: '2022',
    points: 100, // 100 registration points bonus
    nongkiHours: 0,
    ktmStatus: 'Pending' as const,
    avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&q=80',
    authProvider: 'local' as const,
    password: hashPassword(password),
    ktmPhoto: '',
    selfiePhoto: ''
  };

  dbState.users.push(newUser);
  saveLocalDatabase();

  if (isUsingMySQL && mysqlPool) {
    try {
      await mysqlPool.query(
        'INSERT INTO users (id, name, email, phone, dob, bio, city, campus, faculty, study_program, class_year, points, nongki_hours, ktm_status, avatar, auth_provider, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
        [newUser.id, newUser.name, newUser.email, newUser.phone, newUser.dob, newUser.bio, newUser.city, newUser.campus, newUser.faculty, newUser.studyProgram, newUser.classYear, newUser.points, newUser.nongkiHours, newUser.ktmStatus, newUser.avatar, newUser.authProvider, newUser.password]
      );
    } catch (err) {
      console.error('MySQL user insert error during custom signup', err);
    }
  }

  currentSessionUser = newUser;
  res.json({ success: true, user: newUser });
});

// Logout
app.post('/api/auth/logout', (req, res) => {
  currentSessionUser = null;
  res.json({ success: true, message: 'Logged out successfully' });
});

// Get Spots (Cafe, Library, Parks)
app.get('/api/spots', async (req, res) => {
  if (isUsingMySQL && mysqlPool) {
    try {
      const [rows] = await mysqlPool.query<any[]>('SELECT * FROM spots');
      const mapped = rows.map(r => ({
        id: r.id,
        name: r.name,
        type: r.type,
        address: r.address,
        lat: r.lat,
        lng: r.lng,
        wifi: Boolean(r.wifi),
        rating: r.rating,
        image: r.image
      }));
      return res.json(mapped);
    } catch (err) {
      console.error('Failed to select from MySQL spots table. Falling back to local state.', err);
    }
  }
  res.json(dbState.spots);
});

// Get Programs and events
app.get('/api/events', async (req, res) => {
  if (isUsingMySQL && mysqlPool) {
    try {
      const [rows] = await mysqlPool.query<any[]>('SELECT * FROM events');
      const mapped = rows.map(r => ({
        id: r.id,
        title: r.title,
        type: r.type,
        category: r.category,
        description: r.description,
        dateOrDeadline: r.date_or_deadline,
        locationName: r.location_name,
        lat: r.lat,
        lng: r.lng,
        image: r.image,
        status: r.status,
        organizer: r.organizer
      }));
      return res.json(mapped);
    } catch (err) {
      console.error('MySQL events fetch failed', err);
    }
  }
  res.json(dbState.events);
});

// Join Event / Register Scholarship
app.post('/api/events/join', async (req, res) => {
  const { eventId, motivationLetter, gpaScore, documentUrl } = req.body;
  if (!eventId) {
    return res.status(400).json({ success: false, message: 'Event ID required' });
  }

  const eventIndex = dbState.events.findIndex(e => e.id === eventId);
  if (eventIndex !== -1) {
    const e = dbState.events[eventIndex];
    if (e.status === 'open') {
      e.status = e.type === 'scholarship' ? 'registered' : 'joined';

      // Update User points (joining an event gives 150 BIB points!)
      const user = await getLoggedUser();
      if (user) {
        const uIdx = dbState.users.findIndex(u => u.id === user.id);
        if (uIdx !== -1) {
          dbState.users[uIdx].points += 150;
        }

        if (isUsingMySQL && mysqlPool) {
          try {
            await mysqlPool.query('UPDATE users SET points = points + 150 WHERE id = ?', [user.id]);
            await mysqlPool.query('UPDATE events SET status = ? WHERE id = ?', [e.status, eventId]);

            const regId = `reg-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
            if (e.type === 'scholarship') {
              // Pendaftaran Beasiswa dengan data Dinamis
              await mysqlPool.query(
                'INSERT INTO scholarship_registrations (id, user_id, scholarship_id, status, motivation_letter, gpa_score, document_url) VALUES (?, ?, ?, ?, ?, ?, ?)',
                [
                  regId,
                  user.id,
                  eventId,
                  'Pending',
                  motivationLetter || 'Motivation Letter default.',
                  parseFloat(gpaScore || '0.0'),
                  documentUrl || ''
                ]
              );
              console.log(`[MySQL] Pendaftaran BEASISWA sukses untuk: ${user.name}`);
            } else {
              // Pendaftaran Acara Biasa
              const ticketCode = `TKT-${Date.now()}`;
              await mysqlPool.query(
                'INSERT INTO event_registrations (id, user_id, event_id, ticket_code, status) VALUES (?, ?, ?, ?, ?)',
                [regId, user.id, eventId, ticketCode, 'Joined']
              );
              console.log(`[MySQL] Pendaftaran ACARA sukses untuk: ${user.name}`);
            }
          } catch (err) {
            console.error('MySQL join event updates failed', err);
          }
        }
      }
      saveLocalDatabase();
      return res.json({ success: true, event: e });
    }
  }
  res.status(400).json({ success: false, message: 'Unable to join event or event closed.' });
});

// Get Active Discussions list
app.get('/api/discussions', async (req, res) => {
  if (isUsingMySQL && mysqlPool) {
    try {
      const [rows] = await mysqlPool.query<any[]>('SELECT * FROM discussions');
      return res.json(rows.map(r => ({
        id: r.id,
        title: r.title,
        snippet: r.snippet,
        lastTime: r.last_time,
        iconColor: r.icon_color,
        category: r.category
      })));
    } catch (err) {
      console.error('MySQL discussions fetch failed', err);
    }
  }
  res.json(dbState.discussions);
});

// Get Active Chats list
app.get('/api/chats', async (req, res) => {
  if (isUsingMySQL && mysqlPool) {
    try {
      const [rows] = await mysqlPool.query<any[]>('SELECT * FROM chats');
      return res.json(rows.map(r => ({
        id: r.id,
        isGroup: Boolean(r.is_group),
        name: r.name,
        avatar: r.avatar,
        lastMessage: r.last_message,
        lastTime: r.last_time,
        unreadCount: r.unread_count
      })));
    } catch (err) {
      console.error('MySQL chats fetch failed', err);
    }
  }
  res.json(dbState.chats);
});

// Get Chat Messages
app.get('/api/chats/:id/messages', async (req, res) => {
  const { id } = req.params;
  if (isUsingMySQL && mysqlPool) {
    try {
      const [rows] = await mysqlPool.query<any[]>('SELECT * FROM messages WHERE chat_id = ? ORDER BY id ASC', [id]);
      return res.json(rows.map(r => ({
        id: r.id,
        chatId: r.chat_id,
        senderName: r.sender_name,
        senderAvatar: r.sender_avatar,
        text: r.text,
        timestamp: r.timestamp,
        isMe: r.sender_name === (currentSessionUser?.name || 'Habibie')
      })));
    } catch (err) {
      console.error('MySQL messages fetch failed', err);
    }
  }
  const filtered = dbState.messages.filter(m => m.chatId === id);
  res.json(filtered);
});

// Post a Chat Message (Mock and dynamic interactive responses)
app.post('/api/chats/:id/messages', async (req, res) => {
  const { id } = req.params;
  const { text } = req.body;
  const user = await getLoggedUser();

  if (!text) {
    return res.status(400).json({ success: false, message: 'Pesan tidak boleh kosong.' });
  }

  const newMessage = {
    id: `msg-${Date.now()}`,
    chatId: id,
    senderName: user ? user.name : 'Habibie',
    senderAvatar: user ? user.avatar : '',
    text,
    timestamp: new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }),
    isMe: true
  };

  dbState.messages.push(newMessage);

  // Update chat last message
  const chatIdx = dbState.chats.findIndex(c => c.id === id);
  if (chatIdx !== -1) {
    dbState.chats[chatIdx].lastMessage = text;
    dbState.chats[chatIdx].lastTime = newMessage.timestamp;
  }

  saveLocalDatabase();

  if (isUsingMySQL && mysqlPool) {
    try {
      await mysqlPool.query(
        'INSERT INTO messages (id, chat_id, sender_name, sender_avatar, text, timestamp) VALUES (?, ?, ?, ?, ?, ?)',
        [newMessage.id, newMessage.chatId, newMessage.senderName, newMessage.senderAvatar, newMessage.text, newMessage.timestamp]
      );
      await mysqlPool.query(
        'UPDATE chats SET last_message = ?, last_time = ? WHERE id = ?',
        [text, newMessage.timestamp, id]
      );
    } catch (err) {
      console.error('MySQL message save failed', err);
    }
  }

  // If chat is with Zhou Koo Wii, trigger an automated simulated response after 1.5 seconds!
  if (id === 'chat-1') {
    setTimeout(async () => {
      const responses = [
        "Mantap Habibie! Sampai jumpa besok di Cafe Sudut ya. Jangan lupa bawa KTM juga.",
        "Oke, nanti aku bagikan lokasi koworking terbaruku lewat aplikasi.",
        "Siap, aku sudah pesan meja buat kita nongki sambil nugas nanti siang.",
        "Bagus! Mari kita ajak awardee BIB yang lain juga biar makin ramai."
      ];
      const botText = responses[Math.floor(Math.random() * responses.length)];
      const botMessage = {
        id: `msg-${Date.now() + 1}`,
        chatId: id,
        senderName: 'Zhou Koo Wii',
        senderAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&q=80',
        text: botText,
        timestamp: new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }),
        isMe: false
      };

      dbState.messages.push(botMessage);
      if (chatIdx !== -1) {
        dbState.chats[chatIdx].lastMessage = botText;
        dbState.chats[chatIdx].lastTime = botMessage.timestamp;
        dbState.chats[chatIdx].unreadCount += 1;
      }
      saveLocalDatabase();

      if (isUsingMySQL && mysqlPool) {
        try {
          await mysqlPool.query(
            'INSERT INTO messages (id, chat_id, sender_name, sender_avatar, text, timestamp) VALUES (?, ?, ?, ?, ?, ?)',
            [botMessage.id, botMessage.chatId, botMessage.senderName, botMessage.senderAvatar, botMessage.text, botMessage.timestamp]
          );
          await mysqlPool.query(
            'UPDATE chats SET last_message = ?, last_time = ?, unread_count = unread_count + 1 WHERE id = ?',
            [botText, botMessage.timestamp, id]
          );
        } catch (err) {
          console.error('MySQL bot message save failed', err);
        }
      }
    }, 1500);
  }

  res.json({ success: true, message: newMessage });
});

// --- GOOGLE OAUTH SYSTEMS (WEB POPUP & DIRECT LINKS) ---

// Get Google OAuth Url for starting sign-in flow
app.get('/api/auth/google/url', (req, res) => {
  const client_id = process.env.GOOGLE_CLIENT_ID || 'MOCK_GOOGLE_CLIENT_ID';
  // Use protocol and host from request or APP_URL env variable if configured
  const appHost = process.env.APP_URL || `${req.protocol}://${req.get('host')}`;
  const redirect_uri = (req.query.redirect_uri as string) || `${appHost}/api/auth/google/callback`;
  const scopes = ['openid', 'email', 'profile'];

  const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?` + new URLSearchParams({
    client_id,
    redirect_uri,
    response_type: 'code',
    scope: scopes.join(' '),
    access_type: 'offline',
    prompt: 'consent'
  }).toString();

  res.json({ success: true, url: authUrl });
});

// Google OAuth callback endpoint
app.get('/api/auth/google/callback', async (req, res) => {
  const { code } = req.query;
  if (!code) {
    return res.status(400).send('<h1>Error: Authorization code is missing.</h1>');
  }

  const client_id = process.env.GOOGLE_CLIENT_ID || 'MOCK_GOOGLE_CLIENT_ID';
  const client_secret = process.env.GOOGLE_CLIENT_SECRET || 'MOCK_GOOGLE_CLIENT_SECRET';
  const appHost = process.env.APP_URL || `${req.protocol}://${req.get('host')}`;
  const redirect_uri = `${appHost}/api/auth/google/callback`;

  try {
    let email = 'mock.google.user@example.com';
    let name = 'Mock Google User';
    let picture = 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&q=80';

    // If real credentials are set, do the exchange
    if (client_id !== 'MOCK_GOOGLE_CLIENT_ID' && client_secret !== 'MOCK_GOOGLE_CLIENT_SECRET') {
      const tokenResponse = await fetch('https://oauth2.googleapis.com/token', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
          code: code as string,
          client_id,
          client_secret,
          redirect_uri,
          grant_type: 'authorization_code'
        }).toString()
      });

      if (!tokenResponse.ok) {
        throw new Error(`Google token exchange failed: ${tokenResponse.statusText}`);
      }

      const tokenData = await tokenResponse.json();
      const accessToken = tokenData.access_token;

      // Fetch user profile info
      const userinfoResponse = await fetch('https://www.googleapis.com/oauth2/v3/userinfo', {
        headers: { Authorization: `Bearer ${accessToken}` }
      });

      if (userinfoResponse.ok) {
        const profile = await userinfoResponse.json();
        email = profile.email || email;
        name = profile.name || name;
        picture = profile.picture || picture;
      }
    } else {
      console.warn('[NongkiBib] Google Auth using Mock data since GOOGLE_CLIENT_ID/SECRET is not configured.');
    }

    const userEmail = email.toLowerCase();
    let loggedUser = dbState.users.find(u => u.email.toLowerCase() === userEmail);

    if (!loggedUser) {
      // Create a new user with Google details
      loggedUser = {
        id: `usr-google-${Date.now()}`,
        name,
        email: userEmail,
        phone: '+62 811 0000 1111',
        dob: '2001-01-01',
        bio: 'Penerima beasiswa BIB, bergabung melalui Google OAuth.',
        city: 'Jakarta',
        campus: 'Universitas Indonesia',
        faculty: 'Fakultas Teknik',
        studyProgram: 'Teknik Informatika',
        classYear: '2021',
        points: 100,
        nongkiHours: 0,
        ktmStatus: 'Pending',
        avatar: picture,
        authProvider: 'google',
        ktmPhoto: '',
        selfiePhoto: '',
        password: ''
      };
      dbState.users.push(loggedUser);
      saveLocalDatabase();

      if (isUsingMySQL && mysqlPool) {
        try {
          await mysqlPool.query(
            'INSERT INTO users (id, name, email, phone, dob, bio, city, campus, faculty, study_program, class_year, points, nongki_hours, ktm_status, avatar, auth_provider) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [loggedUser.id, loggedUser.name, loggedUser.email, loggedUser.phone, loggedUser.dob, loggedUser.bio, loggedUser.city, loggedUser.campus, loggedUser.faculty, loggedUser.studyProgram, loggedUser.classYear, loggedUser.points, loggedUser.nongkiHours, loggedUser.ktmStatus, loggedUser.avatar, loggedUser.authProvider]
          );
        } catch (err) {
          console.error('MySQL user insert failed in Google OAuth callback', err);
        }
      }
    }

    currentSessionUser = loggedUser;

    // Send postMessage back if opened in popup window, otherwise redirect to homepage
    res.send(`
      <html>
        <head>
          <title>Google Sign-In Successful</title>
          <style>
            body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; background-color: #0F172A; color: white; }
            .card { text-align: center; background: #1E293B; padding: 2.5rem; border-radius: 1rem; box-shadow: 0 10px 25px rgba(0,0,0,0.5); border: 1px solid #334155; max-width: 400px; }
            h2 { color: #D97706; margin-top: 0; }
            p { color: #94A3B8; font-size: 0.95rem; line-height: 1.5; }
            .avatar { width: 80px; height: 80px; border-radius: 50%; border: 3px solid #D97706; margin-bottom: 1rem; object-fit: cover; }
          </style>
        </head>
        <body>
          <div class="card">
            <img class="avatar" src="${picture}" alt="Profile" />
            <h2>Google Sign-In Sukses!</h2>
            <p>Selamat datang, <strong>${name}</strong>.</p>
            <p>Otentikasi berhasil. Jendela ini akan tertutup secara otomatis.</p>
            <script>
              if (window.opener) {
                window.opener.postMessage({
                  type: 'OAUTH_AUTH_SUCCESS',
                  user: ${JSON.stringify(loggedUser)}
                }, '*');
                setTimeout(() => window.close(), 1500);
              } else {
                setTimeout(() => {
                  window.location.href = '/';
                }, 1500);
              }
            </script>
          </div>
        </body>
      </html>
    `);
  } catch (error: any) {
    console.error('OAuth Callback Error:', error);
    res.status(500).send(`
      <html>
        <body style="font-family: sans-serif; background: #0F172A; color: white; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0;">
          <div style="background: #1E293B; padding: 2rem; border-radius: 0.5rem; border: 1px solid #EF4444; text-align: center; max-width: 450px;">
            <h2 style="color: #EF4444; margin-top: 0;">Login Gagal</h2>
            <p>${error.message || 'Kesalahan otentikasi Google'}</p>
            <p style="color: #94A3B8; font-size: 0.9rem;">Pastikan GOOGLE_CLIENT_ID dan GOOGLE_CLIENT_SECRET terkonfigurasi dengan benar di Secrets Anda.</p>
          </div>
        </body>
      </html>
    `);
  }
});

// Alias for general oauth-integration guideline compatibility
app.get('/auth/callback', (req, res) => {
  const queryStr = new URLSearchParams(req.query as any).toString();
  res.redirect(`/api/auth/google/callback?${queryStr}`);
});


// --- GOOGLE OAUTH FOR MOBILE FRONTEND (ANDROID KOTLIN DETECTOR) ---
app.post('/api/auth/google/verify-token', async (req, res) => {
  const { idToken, email, name, avatar } = req.body;
  if (!idToken && !email) {
    return res.status(400).json({ success: false, message: 'Google ID Token or Email required' });
  }

  let userEmail = email ? email.toLowerCase() : '';
  let userName = name || 'Google User';
  let userAvatar = avatar || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&q=80';

  // If a real ID Token is passed and Google Client ID is configured, we can try verifying it
  if (idToken && !email) {
    try {
      const response = await fetch(`https://oauth2.googleapis.com/tokeninfo?id_token=${idToken}`);
      if (response.ok) {
        const payload = await response.json();
        userEmail = payload.email.toLowerCase();
        userName = payload.name || userName;
        userAvatar = payload.picture || userAvatar;
      } else {
        // Fallback to decode it if it's a simulated jwt or verify fails in offline testing
        if (idToken.startsWith('mock-google-id-token-')) {
          userEmail = idToken.replace('mock-google-id-token-', '').toLowerCase();
          userName = userName || 'Simulated User';
        } else {
          return res.status(401).json({ success: false, message: 'Invalid Google ID Token' });
        }
      }
    } catch (err) {
      console.warn('ID Token verify fetch failed. Using fallback decoder for offline testing.', err);
      if (idToken.startsWith('mock-')) {
        userEmail = 'simulated.awardee@itb.ac.id';
      } else {
        return res.status(500).json({ success: false, message: 'Failed to verify Google ID Token' });
      }
    }
  }

  let loggedUser = dbState.users.find(u => u.email.toLowerCase() === userEmail);
  if (!loggedUser) {
    // Register as a new user
    loggedUser = {
      id: `usr-google-${Date.now()}`,
      name: userName,
      email: userEmail,
      phone: '+62 811 0000 1111',
      dob: '2001-01-01',
      bio: 'Penerima beasiswa BIB, bergabung melalui Google OAuth di Android.',
      city: 'Jakarta',
      campus: 'Universitas Indonesia',
      faculty: 'Fakultas Teknik',
      studyProgram: 'Teknik Informatika',
      classYear: '2021',
      points: 100,
      nongkiHours: 0,
      ktmStatus: 'Pending',
      avatar: userAvatar,
      authProvider: 'google',
      ktmPhoto: '',
      selfiePhoto: '',
      password: ''
    };
    dbState.users.push(loggedUser);
    saveLocalDatabase();

    if (isUsingMySQL && mysqlPool) {
      try {
        await mysqlPool.query(
          'INSERT INTO users (id, name, email, phone, dob, bio, city, campus, faculty, study_program, class_year, points, nongki_hours, ktm_status, avatar, auth_provider) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
          [loggedUser.id, loggedUser.name, loggedUser.email, loggedUser.phone, loggedUser.dob, loggedUser.bio, loggedUser.city, loggedUser.campus, loggedUser.faculty, loggedUser.studyProgram, loggedUser.classYear, loggedUser.points, loggedUser.nongkiHours, loggedUser.ktmStatus, loggedUser.avatar, loggedUser.authProvider]
        );
      } catch (err) {
        console.error('MySQL user insert failed in Google ID token verification', err);
      }
    }
  }

  currentSessionUser = loggedUser;
  res.json({ success: true, user: loggedUser, message: 'Google Sign-In Sukses!' });
});

// --- GOOGLE MAPS REALTIME LOCATION TRACKING SYSTEM (HTTP FALLBACKS) ---

// Update real-time location (Google Maps Integration)
app.post('/api/location/update', async (req, res) => {
  const { userId, lat, lng } = req.body;
  const user = await getLoggedUser();

  const activeUserId = userId || (user ? user.id : null);
  const activeName = user ? user.name : (userId ? `User-${userId.substring(0, 5)}` : 'Guest');
  const activeAvatar = user ? user.avatar : 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&q=80';

  if (!activeUserId) {
    return res.status(400).json({ success: false, message: 'User ID or authenticated session required' });
  }

  if (lat === undefined || lng === undefined) {
    return res.status(400).json({ success: false, message: 'Latitude (lat) and Longitude (lng) are required' });
  }

  const updatedLoc = {
    userId: activeUserId,
    name: activeName,
    avatar: activeAvatar,
    lat: parseFloat(lat),
    lng: parseFloat(lng),
    updatedAt: new Date().toISOString()
  };

  // 1. Update in local memory dbState
  const locIdx = dbState.user_locations.findIndex(l => l.userId === activeUserId);
  if (locIdx !== -1) {
    dbState.user_locations[locIdx] = updatedLoc;
  } else {
    dbState.user_locations.push(updatedLoc);
  }
  saveLocalDatabase();

  // 2. Update in MySQL Database if available
  if (isUsingMySQL && mysqlPool) {
    try {
      await mysqlPool.query(
        'INSERT INTO user_locations (user_id, name, avatar, lat, lng) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name), avatar = VALUES(avatar), lat = VALUES(lat), lng = VALUES(lng)',
        [activeUserId, activeName, activeAvatar, parseFloat(lat), parseFloat(lng)]
      );
    } catch (err) {
      console.error('Failed to update user location in MySQL', err);
    }
  }

  // Broadcast location update to all connected WebSocket clients
  broadcastLocationUpdate();

  res.json({ success: true, location: updatedLoc });
});

// Retrieve all active locations on Google Maps
app.get('/api/location/active', async (req, res) => {
  if (isUsingMySQL && mysqlPool) {
    try {
      const [rows] = await mysqlPool.query<any[]>('SELECT * FROM user_locations WHERE updated_at > NOW() - INTERVAL 15 MINUTE');
      const mapped = rows.map(r => ({
        userId: r.user_id,
        name: r.name,
        avatar: r.avatar,
        lat: r.lat,
        lng: r.lng,
        updatedAt: r.updated_at
      }));
      return res.json(mapped);
    } catch (err) {
      console.error('Failed to query user locations from MySQL, falling back', err);
    }
  }

  // For local database: return locations updated in the last 15 minutes
  const fifteenMinutesAgo = new Date(Date.now() - 15 * 60 * 1000).toISOString();
  const activeLocations = dbState.user_locations.filter(l => l.updatedAt > fifteenMinutesAgo);
  res.json(activeLocations);
});

// Google Maps Platform integration: Places Search (with local DB fallback)
app.get('/api/maps/search', async (req, res) => {
  const query = (req.query.query as string) || '';
  const latParam = req.query.lat ? parseFloat(req.query.lat as string) : null;
  const lngParam = req.query.lng ? parseFloat(req.query.lng as string) : null;

  const apiKey = process.env.GOOGLE_MAPS_PLATFORM_KEY;

  if (apiKey) {
    try {
      const fieldMask = 'places.id,places.displayName,places.formattedAddress,places.location,places.rating,places.types';
      const body: any = { textQuery: query };

      if (latParam !== null && lngParam !== null) {
        body.locationBias = {
          circle: {
            center: { latitude: latParam, longitude: lngParam },
            radius: 10000 // 10 km
          }
        };
      }

      const response = await fetch('https://places.googleapis.com/v1/places:searchText', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Goog-Api-Key': apiKey,
          'X-Goog-FieldMask': fieldMask
        },
        body: JSON.stringify(body)
      });

      if (!response.ok) {
        throw new Error(`Google Places API returned status ${response.status}`);
      }

      const data = await response.json();
      const rawPlaces = data.places || [];

      const mapped = rawPlaces.map((p: any) => {
        // Determine type based on place categories
        let type: 'Cafe' | 'Perpus' | 'Taman' = 'Cafe';
        if (p.types?.includes('library') || p.types?.includes('book_store')) {
          type = 'Perpus';
        } else if (p.types?.includes('park') || p.types?.includes('tourist_attraction')) {
          type = 'Taman';
        }

        return {
          id: p.id,
          name: p.displayName?.text || 'Tempat Tanpa Nama',
          type,
          address: p.formattedAddress || 'Alamat tidak diketahui',
          lat: p.location?.latitude || -6.2088,
          lng: p.location?.longitude || 106.8456,
          wifi: true, // Default to true for premium spots
          rating: p.rating || 4.5,
          image: `https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=500&q=80` // standard beautiful placeholder
        };
      });

      return res.json({ success: true, source: 'google_maps_api', spots: mapped });
    } catch (err: any) {
      console.error('Google Places Search failed, falling back to local DB:', err.message);
    }
  }

  // Fallback local search
  const queryLower = query.toLowerCase();
  const filtered = dbState.spots.filter(
    s => s.name.toLowerCase().includes(queryLower) || s.address.toLowerCase().includes(queryLower)
  );

  res.json({ success: true, source: 'local_database', spots: filtered });
});

// Google Maps Platform integration: Directions & Polyline Routing (with local line math fallback)
app.get('/api/maps/directions', async (req, res) => {
  const { originLat, originLng, destLat, destLng } = req.query;

  if (!originLat || !originLng || !destLat || !destLng) {
    return res.status(400).json({ success: false, message: 'Harap sediakan koordinat asal dan tujuan (originLat, originLng, destLat, destLng)' });
  }

  const apiKey = process.env.GOOGLE_MAPS_PLATFORM_KEY;

  if (apiKey) {
    try {
      // Use Routes API (New) computeRoutes
      const response = await fetch('https://routes.googleapis.com/v1/directions:computeRoutes', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Goog-Api-Key': apiKey,
          'X-Goog-FieldMask': 'routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline'
        },
        body: JSON.stringify({
          origin: {
            location: {
              latLng: {
                latitude: parseFloat(originLat as string),
                longitude: parseFloat(originLng as string)
              }
            }
          },
          destination: {
            location: {
              latLng: {
                latitude: parseFloat(destLat as string),
                longitude: parseFloat(destLng as string)
              }
            }
          },
          travelMode: 'DRIVING',
          routingPreference: 'TRAFFIC_AWARE'
        })
      });

      if (!response.ok) {
        throw new Error(`Google Routes API returned status ${response.status}`);
      }

      const data = await response.json();
      const route = data.routes?.[0];

      if (route) {
        return res.json({
          success: true,
          source: 'google_routes_api',
          duration: route.duration,
          distanceMeters: route.distanceMeters,
          polyline: route.polyline?.encodedPolyline
        });
      }
    } catch (err: any) {
      console.error('Google Routes API compute failed, falling back to line calculation:', err.message);
    }
  }

  // Fallback simple straight line distance calculation
  const distance = Math.sqrt(
    Math.pow(parseFloat(destLat as string) - parseFloat(originLat as string), 2) +
    Math.pow(parseFloat(destLng as string) - parseFloat(originLng as string), 2)
  ) * 111000; // rough meters in latitude conversion

  const mockDuration = `${Math.round(distance / 250)}s`; // rough estimation at ~15km/h

  res.json({
    success: true,
    source: 'local_estimation',
    duration: mockDuration,
    distanceMeters: Math.round(distance),
    polyline: null // Client can render straight line
  });
});

// Update User profile details
app.put('/api/user/profile', async (req, res) => {
  const user = await getLoggedUser();
  if (!user) {
    return res.status(401).json({ success: false, message: 'Not authenticated' });
  }

  const { name, phone, dob, bio, city, campus, faculty, studyProgram, classYear, avatar } = req.body;
  const idx = dbState.users.findIndex(u => u.id === user.id);

  if (idx !== -1) {
    const updated = {
      ...dbState.users[idx],
      name: name || dbState.users[idx].name,
      phone: phone !== undefined ? phone : dbState.users[idx].phone,
      dob: dob !== undefined ? dob : dbState.users[idx].dob,
      bio: bio !== undefined ? bio : dbState.users[idx].bio,
      city: city !== undefined ? city : dbState.users[idx].city,
      campus: campus !== undefined ? campus : dbState.users[idx].campus,
      faculty: faculty !== undefined ? faculty : dbState.users[idx].faculty,
      studyProgram: studyProgram !== undefined ? studyProgram : dbState.users[idx].studyProgram,
      classYear: classYear !== undefined ? classYear : dbState.users[idx].classYear,
      avatar: avatar !== undefined ? avatar : dbState.users[idx].avatar,
    };

    dbState.users[idx] = updated;
    saveLocalDatabase();

    if (isUsingMySQL && mysqlPool) {
      try {
        await mysqlPool.query(
          'UPDATE users SET name = ?, phone = ?, dob = ?, bio = ?, city = ?, campus = ?, faculty = ?, study_program = ?, class_year = ?, avatar = ? WHERE id = ?',
          [updated.name, updated.phone, updated.dob, updated.bio, updated.city, updated.campus, updated.faculty, updated.studyProgram, updated.classYear, updated.avatar, user.id]
        );
      } catch (err) {
        console.error('MySQL user profile update failed', err);
      }
    }

    return res.json({ success: true, user: updated });
  }

  res.status(400).json({ success: false, message: 'User not found in state' });
});

// Change Password Endpoint (Security & Privacy)
app.put('/api/user/change-password', async (req, res) => {
  const user = await getLoggedUser();
  if (!user) {
    return res.status(401).json({ success: false, message: 'Not authenticated' });
  }

  const { oldPassword, newPassword } = req.body;
  if (!oldPassword || !newPassword) {
    return res.status(400).json({ success: false, message: 'Old and new password required' });
  }

  const idx = dbState.users.findIndex(u => u.id === user.id);
  if (idx === -1) {
    return res.status(404).json({ success: false, message: 'User not found' });
  }

  const hashedOld = hashPassword(oldPassword);
  if (dbState.users[idx].password !== hashedOld) {
    return res.status(400).json({ success: false, message: 'Password lama salah.' });
  }

  const hashedNew = hashPassword(newPassword);
  dbState.users[idx].password = hashedNew;
  saveLocalDatabase();

  if (isUsingMySQL && mysqlPool) {
    try {
      await mysqlPool.query('UPDATE users SET password = ? WHERE id = ?', [hashedNew, user.id]);
    } catch (err) {
      console.error('MySQL password update failed', err);
    }
  }

  res.json({ success: true, message: 'Password berhasil diperbarui!' });
});

// Submit KTM student Verification photo
app.post('/api/user/ktm', async (req, res) => {
  const user = await getLoggedUser();
  if (!user) {
    return res.status(401).json({ success: false, message: 'Not authenticated' });
  }

  const { ktmPhoto, selfiePhoto } = req.body;
  const idx = dbState.users.findIndex(u => u.id === user.id);

  if (idx !== -1) {
    dbState.users[idx].ktmStatus = 'Verified'; // Auto approve in local demo for premium UX
    dbState.users[idx].points += 1000; // Add 1000 premium verification points!
    if (ktmPhoto) dbState.users[idx].ktmPhoto = ktmPhoto;
    if (selfiePhoto) dbState.users[idx].selfiePhoto = selfiePhoto;

    saveLocalDatabase();

    if (isUsingMySQL && mysqlPool) {
      try {
        await mysqlPool.query(
          'UPDATE users SET ktm_status = "Verified", points = points + 1000, ktm_photo = ?, selfie_photo = ? WHERE id = ?',
          [ktmPhoto || null, selfiePhoto || null, user.id]
        );
      } catch (err) {
        console.error('MySQL KTM update failed', err);
      }
    }

    return res.json({ success: true, user: dbState.users[idx], message: 'KTM Sukses Terverifikasi! Anda mendapatkan 1.000 BIB Points.' });
  }

  res.status(400).json({ success: false, message: 'User not found' });
});

// Log study timer hours and gain points
app.post('/api/user/timer', async (req, res) => {
  const user = await getLoggedUser();
  if (!user) {
    return res.status(401).json({ success: false, message: 'Not authenticated' });
  }

  const { elapsedMinutes } = req.body;
  const idx = dbState.users.findIndex(u => u.id === user.id);

  if (idx !== -1) {
    const hoursEarned = parseFloat((elapsedMinutes / 60).toFixed(2));
    const pointsEarned = Math.round(elapsedMinutes * 10); // 10 points per minute spent co-working!

    dbState.users[idx].nongkiHours += hoursEarned;
    dbState.users[idx].points += pointsEarned;
    saveLocalDatabase();

    if (isUsingMySQL && mysqlPool) {
      try {
        await mysqlPool.query(
          'UPDATE users SET nongki_hours = nongki_hours + ?, points = points + ? WHERE id = ?',
          [hoursEarned, pointsEarned, user.id]
        );
      } catch (err) {
        console.error('MySQL timer increment failed', err);
      }
    }

    return res.json({ success: true, user: dbState.users[idx], pointsEarned, hoursEarned });
  }
  res.status(400).json({ success: false, message: 'User not found' });
});

// Create Event
app.post('/api/events', async (req, res) => {
  const { title, type, category, description, dateOrDeadline, locationName, lat, lng, image } = req.body;
  if (!title || !type || !category || !dateOrDeadline || !locationName) {
    return res.status(400).json({ success: false, message: 'Semua kolom bertanda bintang wajib diisi.' });
  }

  const user = await getLoggedUser();

  const newEvent = {
    id: `event-${Date.now()}`,
    title,
    type: type as any,
    category: category as any,
    description: description || '',
    dateOrDeadline,
    locationName,
    lat: parseFloat(lat || '-6.2088'),
    lng: parseFloat(lng || '106.8456'),
    image: image || 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=500&q=80',
    status: 'open' as const,
    organizer: user ? user.name : 'BIB Community'
  };

  dbState.events.unshift(newEvent);
  saveLocalDatabase();

  if (isUsingMySQL && mysqlPool) {
    try {
      await mysqlPool.query(
        'INSERT INTO events (id, title, type, category, description, date_or_deadline, location_name, lat, lng, image, status, organizer) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
        [newEvent.id, newEvent.title, newEvent.type, newEvent.category, newEvent.description, newEvent.dateOrDeadline, newEvent.locationName, newEvent.lat, newEvent.lng, newEvent.image, newEvent.status, newEvent.organizer]
      );
    } catch (err) {
      console.error('MySQL event creation error', err);
    }
  }

  res.json({ success: true, event: newEvent });
});


// --- REALTIME WEBSOCKET SERVICES & STATE STORES ---

// Active WebSocket connections set
const activeSockets = new Set<WebSocket>();

function broadcastLocationUpdate() {
  const fifteenMinutesAgo = new Date(Date.now() - 15 * 60 * 1000).toISOString();
  const activeLocations = dbState.user_locations.filter(l => l.updatedAt > fifteenMinutesAgo);

  const payload = JSON.stringify({
    type: 'location_broadcast',
    locations: activeLocations
  });

  for (const ws of activeSockets) {
    if (ws.readyState === WebSocket.OPEN) {
      ws.send(payload);
    }
  }
}

function initWebSocketHandler(wss: WebSocketServer) {
  wss.on('connection', (ws: WebSocket) => {
    activeSockets.add(ws);
    console.log(`[WS Connection] New client connected. Total clients: ${activeSockets.size}`);

    // Send initial locations upon connection
    const fifteenMinutesAgo = new Date(Date.now() - 15 * 60 * 1000).toISOString();
    const activeLocations = dbState.user_locations.filter(l => l.updatedAt > fifteenMinutesAgo);
    ws.send(JSON.stringify({
      type: 'location_broadcast',
      locations: activeLocations
    }));

    ws.on('message', async (messageData) => {
      try {
        const payload = JSON.parse(messageData.toString());
        console.log('[WS Message Received]', payload);

        if (payload.type === 'register') {
          ws.send(JSON.stringify({ type: 'registered', success: true }));
        }

        if (payload.type === 'location_update') {
          const { userId, name, avatar, lat, lng } = payload;
          if (userId && lat !== undefined && lng !== undefined) {
            const updatedLoc = {
              userId,
              name: name || `User-${userId.substring(0, 5)}`,
              avatar: avatar || '',
              lat: parseFloat(lat),
              lng: parseFloat(lng),
              updatedAt: new Date().toISOString()
            };

            const idx = dbState.user_locations.findIndex(l => l.userId === userId);
            if (idx !== -1) {
              dbState.user_locations[idx] = updatedLoc;
            } else {
              dbState.user_locations.push(updatedLoc);
            }
            saveLocalDatabase();

            if (isUsingMySQL && mysqlPool) {
              try {
                await mysqlPool.query(
                  'INSERT INTO user_locations (user_id, name, avatar, lat, lng) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name), avatar = VALUES(avatar), lat = VALUES(lat), lng = VALUES(lng)',
                  [userId, updatedLoc.name, updatedLoc.avatar, updatedLoc.lat, updatedLoc.lng]
                );
              } catch (err) {
                console.error('MySQL WS location update failed', err);
              }
            }

            // Broadcast to all sockets
            broadcastLocationUpdate();
          }
        }

        if (payload.type === 'chat_message') {
          const { chatId, userId, userName, userAvatar, text } = payload;
          if (chatId && text) {
            const newMessage = {
              id: `msg-${Date.now()}`,
              chatId,
              senderName: userName || 'Anonymous',
              senderAvatar: userAvatar || '',
              text,
              timestamp: new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }),
              isMe: false
            };

            dbState.messages.push(newMessage);

            const chatIdx = dbState.chats.findIndex(c => c.id === chatId);
            if (chatIdx !== -1) {
              dbState.chats[chatIdx].lastMessage = text;
              dbState.chats[chatIdx].lastTime = newMessage.timestamp;
            }
            saveLocalDatabase();

            if (isUsingMySQL && mysqlPool) {
              try {
                await mysqlPool.query(
                  'INSERT INTO messages (id, chat_id, sender_name, sender_avatar, text, timestamp) VALUES (?, ?, ?, ?, ?, ?)',
                  [newMessage.id, newMessage.chatId, newMessage.senderName, newMessage.senderAvatar, newMessage.text, newMessage.timestamp]
                );
                await mysqlPool.query(
                  'UPDATE chats SET last_message = ?, last_time = ? WHERE id = ?',
                  [text, newMessage.timestamp, chatId]
                );
              } catch (err) {
                console.error('MySQL WS message save failed', err);
              }
            }

            // Broadcast message to all connected clients
            const broadcastPayload = JSON.stringify({
              type: 'new_message',
              message: newMessage
            });

            for (const client of activeSockets) {
              if (client.readyState === WebSocket.OPEN) {
                client.send(broadcastPayload);
              }
            }
          }
        }
      } catch (err) {
        console.error('Failed to parse WebSocket message data', err);
      }
    });

    ws.on('close', () => {
      activeSockets.delete(ws);
      console.log(`[WS Connection] Client disconnected. Remaining clients: ${activeSockets.size}`);
    });

    ws.on('error', (err) => {
      console.error('[WS Error]', err);
      activeSockets.delete(ws);
    });
  });
}


// --- INTEGRATE BACKEND APP ENDPOINTS & WELCOME ROUTE ---

async function startServer() {
  // Root URL displays backend status and API documentation
  app.get('/', (req, res) => {
    res.json({
      status: "online",
      name: "NongkiBib Backend Engine",
      version: "1.0.0",
      description: "Backend API and WebSocket services for NongkiBib application.",
      endpoints: [
        "/api/auth/current-user",
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/logout",
        "/api/spots",
        "/api/events",
        "/api/events/join",
        "/api/discussions",
        "/api/chats",
        "/api/chats/:id/messages",
        "/api/auth/google/url",
        "/api/auth/google/callback",
        "/api/auth/google/verify-token",
        "/api/location/update",
        "/api/location/active",
        "/api/maps/search",
        "/api/maps/directions"
      ],
      websocket: "ws://[host]/ws"
    });
  });

  const server = app.listen(PORT, '0.0.0.0', () => {
    console.log(`[NongkiBib Engine] Listening on port ${PORT}`);
  });

  // --- GRACEFUL SHUTDOWN LOGIC ---
  process.on('SIGINT', async () => {
    console.log('\n[NongkiBib Engine] Shutting down gracefully...');
    if (mysqlPool) {
      await mysqlPool.end();
      console.log('[MySQL] Connection pool closed.');
    }
    server.close(() => {
      console.log('[NongkiBib Engine] Server closed. Goodbye!');
      process.exit(0);
    });
  });

  const wss = new WebSocketServer({ noServer: true });
  initWebSocketHandler(wss);

  server.on('upgrade', (request, socket, head) => {
    if (request.url?.startsWith('/ws')) {
      wss.handleUpgrade(request, socket, head, (ws) => {
        wss.emit('connection', ws, request);
      });
    } else {
      socket.destroy();
    }
  });
}

startServer().catch(err => {
  console.error('Failed to start NongkiBib Engine server:', err);
});
