import mysql from 'mysql2/promise';
import dotenv from 'dotenv';

// Memuat variabel lingkungan dari file .env
dotenv.config();

// Konfigurasi koneksi database MySQL dari .env dengan fallback nilai default
const dbConfig = {
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_DATABASE || 'nongkibib_db',
  port: process.env.DB_PORT ? parseInt(process.env.DB_PORT) : 3306,
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
};

// Membuat Connection Pool untuk performa dan efisiensi query yang lebih baik
let pool: mysql.Pool | null = null;

export function getDatabasePool(): mysql.Pool {
  if (!pool) {
    console.log(`[Database] Menghubungkan ke MySQL di ${dbConfig.host}:${dbConfig.port} sebagai user "${dbConfig.user}"...`);

    try {
      pool = mysql.createPool(dbConfig);

      // Lakukan uji coba koneksi awal untuk memastikan kredensial valid
      pool.getConnection()
        .then((conn) => {
          console.log('✅ [Database] Koneksi ke MySQL berhasil terhubung!');
          conn.release();
        })
        .catch((err) => {
          console.error('❌ [Database] Gagal menghubungkan ke MySQL server:', err.message);
          console.warn('[Database] Silakan periksa apakah server MySQL Anda sudah menyala dan kredensial di file .env sudah sesuai.');
        });

    } catch (err: any) {
      console.error('❌ [Database] Kesalahan fatal saat menginisialisasi MySQL pool:', err.message);
      throw err;
    }
  }
  return pool;
}

// Helper function untuk mempermudah eksekusi query SQL dengan asinkronus (async/await)
export async function query(sql: string, params?: any[]): Promise<any> {
  const activePool = getDatabasePool();
  const [results] = await activePool.execute(sql, params);
  return results;
}

export default {
  getDatabasePool,
  query
};
