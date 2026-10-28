package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.InventoryItem
import com.example.data.model.ItemCondition
import com.example.data.model.ItemSource
import com.example.data.model.KasTransaction
import com.example.data.model.MushallaProfile
import com.example.data.model.TransactionType
import com.example.data.model.WakafDonation
import com.example.data.model.WakafProject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        KasTransaction::class,
        WakafDonation::class,
        WakafProject::class,
        InventoryItem::class,
        MushallaProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kasDao(): KasDao
    abstract fun wakafDao(): WakafDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mushalla_finance.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            // 1. Initial Mushalla Profile with Bank Nagari Syariah & BSI
            val profile = MushallaProfile(
                id = 1,
                name = "Mushalla Ar-Rahman",
                address = "Jl. Khatib Sulaiman No. 45",
                city = "Padang, Sumatera Barat",
                phone = "+62 812-3456-7890",
                leaderName = "H. Syahril Efendi",
                treasurerName = "Ahmad Fauzi, S.E.",
                bankNagariAccount = "71000201021831",
                bankNagariName = "Bank Nagari Syariah",
                bankBsiAccount = "7167978112",
                bankBsiName = "Bank Syariah Indonesia",
                qrisCode = "NMID: ID10200304050607 - MUSHALLA AR-RAHMAN",
                avatarId = 0
            )
            database.profileDao().insertOrUpdateProfile(profile)

            // 2. Default Wakaf Project
            val defaultProject = WakafProject(
                id = 1,
                title = "Pembangunan Tempat Wudhu & Sarana Air Bersih Mushalla",
                targetAmount = 75000000L, // Rp 75.000.000
                description = "Renovasi dan perluasan tempat wudhu pria & wanita, sumur bor sanitasi, dan toilet modern ramah lansia.",
                deadline = "31 Desember 2026",
                isActive = true
            )
            database.wakafDao().insertOrUpdateProject(defaultProject)

            // 3. Initial Wakaf Donations
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            val donations = listOf(
                WakafDonation(
                    donorName = "H. Ramli Sutan Malano",
                    address = "Koto Baru, Padang",
                    nominal = 15000000L,
                    date = now - 12 * dayMs,
                    projectId = 1,
                    note = "Semoga berkah untuk kelancaran ibadah jamaah",
                    phoneNumber = "081267891234",
                    securityHash = SecurityHelper.generateIntegrityHash("H. Ramli", "15000000", "1")
                ),
                WakafDonation(
                    donorName = "Ibu Hj. Maryam & Keluarga",
                    address = "Jl. Proklamasi No. 18",
                    nominal = 10000000L,
                    date = now - 9 * dayMs,
                    projectId = 1,
                    note = "Wakaf atas nama almarhum H. Hasan Basri",
                    phoneNumber = "081398765432",
                    securityHash = SecurityHelper.generateIntegrityHash("Ibu Hj. Maryam", "10000000", "1")
                ),
                WakafDonation(
                    donorName = "Keluarga Besar dr. Ridwan",
                    address = "Komplek Perumahan Indah",
                    nominal = 7500000L,
                    date = now - 6 * dayMs,
                    projectId = 1,
                    note = "Wakaf semen dan keramik wudhu",
                    phoneNumber = "081122334455",
                    securityHash = SecurityHelper.generateIntegrityHash("dr. Ridwan", "7500000", "1")
                ),
                WakafDonation(
                    donorName = "Hamba Allah (Transfer BSI)",
                    address = "Ulugadut",
                    nominal = 5000000L,
                    date = now - 3 * dayMs,
                    projectId = 1,
                    note = "Lillahita'ala untuk sarana air bersih",
                    phoneNumber = "-",
                    securityHash = SecurityHelper.generateIntegrityHash("Hamba Allah", "5000000", "1")
                ),
                WakafDonation(
                    donorName = "Bapak Hendra Saputra",
                    address = "Jl. Veteran No. 5",
                    nominal = 2500000L,
                    date = now - 1 * dayMs,
                    projectId = 1,
                    note = "Semoga sarana wudhu segera rampung",
                    phoneNumber = "085278901234",
                    securityHash = SecurityHelper.generateIntegrityHash("Bapak Hendra", "2500000", "1")
                )
            )
            donations.forEach { database.wakafDao().insertDonation(it) }

            // 4. Initial Kas Transactions
            val transactions = listOf(
                KasTransaction(
                    type = TransactionType.MASUK,
                    category = "Infaq Sholat Jumat",
                    amount = 3850000L,
                    date = now - 7 * dayMs,
                    description = "Perolehan kotak infaq sholat Jumat berjamaah",
                    recipientOrDonor = "Jamaah Jumat",
                    paymentMethod = "Tunai",
                    securityHash = SecurityHelper.generateIntegrityHash("MASUK", "Infaq Sholat Jumat", "3850000")
                ),
                KasTransaction(
                    type = TransactionType.MASUK,
                    category = "Kotak Amal Tromol Harian",
                    amount = 1420000L,
                    date = now - 5 * dayMs,
                    description = "Pembukaan kotak tromol harian dan subuh",
                    recipientOrDonor = "Kotak Amal Mushalla",
                    paymentMethod = "Tunai",
                    securityHash = SecurityHelper.generateIntegrityHash("MASUK", "Kotak Amal Tromol", "1420000")
                ),
                KasTransaction(
                    type = TransactionType.MASUK,
                    category = "Donasi Operasional (Transfer Nagari)",
                    amount = 2000000L,
                    date = now - 4 * dayMs,
                    description = "Infaq bulanan operasional mushalla dari donatur tetap",
                    recipientOrDonor = "H. Rusli Bahar",
                    paymentMethod = "Bank Nagari Syariah",
                    securityHash = SecurityHelper.generateIntegrityHash("MASUK", "Donasi Operasional", "2000000")
                ),
                KasTransaction(
                    type = TransactionType.KELUAR,
                    category = "Listrik & Air (PLN & PDAM)",
                    amount = 875000L,
                    date = now - 4 * dayMs,
                    description = "Pembayaran tagihan listrik token dan air PDAM mushalla",
                    recipientOrDonor = "PLN & PDAM Padang",
                    paymentMethod = "Transfer BSI",
                    securityHash = SecurityHelper.generateIntegrityHash("KELUAR", "Listrik & Air", "875000")
                ),
                KasTransaction(
                    type = TransactionType.KELUAR,
                    category = "Honor Marbot & Imam Rawatib",
                    amount = 1500000L,
                    date = now - 3 * dayMs,
                    description = "Mukafaah bulanan Marbot & Imam rawatib mushalla",
                    recipientOrDonor = "Ustadz Lukman & Marbot",
                    paymentMethod = "Tunai",
                    securityHash = SecurityHelper.generateIntegrityHash("KELUAR", "Honor Marbot", "1500000")
                ),
                KasTransaction(
                    type = TransactionType.KELUAR,
                    category = "Kebersihan & Perlengkapan",
                    amount = 350000L,
                    date = now - 2 * dayMs,
                    description = "Pembelian sabun cuci tangan, pembersih lantai, dan pengharum ruangan",
                    recipientOrDonor = "Toko Sahabat Berkah",
                    paymentMethod = "Tunai",
                    securityHash = SecurityHelper.generateIntegrityHash("KELUAR", "Kebersihan", "350000")
                ),
                KasTransaction(
                    type = TransactionType.MASUK,
                    category = "Infaq Sholat Jumat",
                    amount = 4120000L,
                    date = now - 1 * dayMs,
                    description = "Perolehan kotak infaq jumat pekan ini",
                    recipientOrDonor = "Jamaah Jumat",
                    paymentMethod = "Tunai",
                    securityHash = SecurityHelper.generateIntegrityHash("MASUK", "Infaq Sholat Jumat 2", "4120000")
                )
            )
            transactions.forEach { database.kasDao().insertTransaction(it) }

            // 5. Initial Inventory Items
            val inventory = listOf(
                InventoryItem(
                    itemName = "Karpet Sajadah Shaf Turki Tebal",
                    category = "Perlengkapan Ibadah",
                    quantity = 10,
                    unit = "Gulung",
                    condition = ItemCondition.BAIK,
                    acquisitionDate = now - 60 * dayMs,
                    source = ItemSource.WAKAF,
                    estimatedValue = 25000000L,
                    location = "Ruang Sholat Utama",
                    notes = "Ukuran 1.2m x 6m per gulung warna hijau motif pilar",
                    securityHash = SecurityHelper.generateIntegrityHash("Karpet", "10", "BAIK")
                ),
                InventoryItem(
                    itemName = "Amplifier & Sound System TOA ZA-2240",
                    category = "Sound System",
                    quantity = 2,
                    unit = "Unit",
                    condition = ItemCondition.BAIK,
                    acquisitionDate = now - 90 * dayMs,
                    source = ItemSource.BELI_KAS,
                    estimatedValue = 6800000L,
                    location = "Mihrab & Menara",
                    notes = "Dilengkapi mixer 4 channel dan proteksi petir",
                    securityHash = SecurityHelper.generateIntegrityHash("Sound System", "2", "BAIK")
                ),
                InventoryItem(
                    itemName = "Microphone Wireless Dual Handheld",
                    category = "Sound System",
                    quantity = 2,
                    unit = "Set",
                    condition = ItemCondition.BAIK,
                    acquisitionDate = now - 45 * dayMs,
                    source = ItemSource.HIBAH,
                    estimatedValue = 1500000L,
                    location = "Mihrab",
                    notes = "Merk Shure wireless UHF untuk Imam dan Khotib",
                    securityHash = SecurityHelper.generateIntegrityHash("Mic Wireless", "2", "BAIK")
                ),
                InventoryItem(
                    itemName = "AC Split Daikin Inverter 2 PK",
                    category = "Elektronik",
                    quantity = 4,
                    unit = "Unit",
                    condition = ItemCondition.BAIK,
                    acquisitionDate = now - 120 * dayMs,
                    source = ItemSource.WAKAF,
                    estimatedValue = 32000000L,
                    location = "Ruang Sholat Utama",
                    notes = "Servis cuci AC rutin setiap 3 bulan sekali",
                    securityHash = SecurityHelper.generateIntegrityHash("AC Split", "4", "BAIK")
                ),
                InventoryItem(
                    itemName = "Lemari Kaca Al-Qur'an Jati",
                    category = "Perlengkapan Ibadah",
                    quantity = 2,
                    unit = "Unit",
                    condition = ItemCondition.BAIK,
                    acquisitionDate = now - 150 * dayMs,
                    source = ItemSource.HIBAH,
                    estimatedValue = 4000000L,
                    location = "Sayap Kanan & Kiri",
                    notes = "Menyimpan 60 mushaf Al-Qur'an dan buku doa",
                    securityHash = SecurityHelper.generateIntegrityHash("Lemari Kaca", "2", "BAIK")
                ),
                InventoryItem(
                    itemName = "Kipas Angin Dinding Tornado",
                    category = "Elektronik",
                    quantity = 4,
                    unit = "Unit",
                    condition = ItemCondition.RUSAK_RINGAN,
                    acquisitionDate = now - 200 * dayMs,
                    source = ItemSource.BELI_KAS,
                    estimatedValue = 1800000L,
                    location = "Serambi Luar",
                    notes = "1 unit saklar putar macet, perlu ganti kapasitor",
                    securityHash = SecurityHelper.generateIntegrityHash("Kipas Angin", "4", "RUSAK_RINGAN")
                ),
                InventoryItem(
                    itemName = "Mesin Pembersih Vakum Karpet",
                    category = "Kebersihan",
                    quantity = 1,
                    unit = "Unit",
                    condition = ItemCondition.BAIK,
                    acquisitionDate = now - 30 * dayMs,
                    source = ItemSource.BELI_KAS,
                    estimatedValue = 2200000L,
                    location = "Gudang Marbot",
                    notes = "Digunakan setiap hari Jumat dan Selasa",
                    securityHash = SecurityHelper.generateIntegrityHash("Vakum Karpet", "1", "BAIK")
                )
            )
            inventory.forEach { database.inventoryDao().insertItem(it) }
        }
    }
}
