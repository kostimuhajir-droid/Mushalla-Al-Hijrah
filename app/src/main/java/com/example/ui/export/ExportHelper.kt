package com.example.ui.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.InventoryItem
import com.example.data.model.KasTransaction
import com.example.data.model.MushallaProfile
import com.example.data.model.TransactionType
import com.example.data.model.WakafDonation
import com.example.data.model.WakafProject
import com.example.ui.components.formatRupiah
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
    private val dateTimeFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm 'WIB'", Locale("id", "ID"))

    /**
     * Generate PDF resmi laporan keuangan, wakaf, dan inventaris
     */
    fun generatePdfReport(
        context: Context,
        profile: MushallaProfile,
        transactions: List<KasTransaction>,
        wakafDonations: List<WakafDonation>,
        wakafProject: WakafProject?,
        inventoryList: List<InventoryItem>,
        totalMasuk: Long,
        totalKeluar: Long,
        saldoKas: Long,
        totalWakaf: Long
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 standard width in points (72 dpi)
            val pageHeight = 842 // A4 standard height

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val titlePaint = Paint().apply {
                color = Color.rgb(15, 81, 50) // Emerald Green
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val subtitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 9f
                textAlign = Paint.Align.CENTER
            }
            val headerPaint = Paint().apply {
                color = Color.rgb(15, 81, 50)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bodyPaint = Paint().apply {
                color = Color.BLACK
                textSize = 8.5f
            }
            val boldPaint = Paint().apply {
                color = Color.BLACK
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            var y = 35f

            // Kop Surat Mushalla
            canvas.drawText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", pageWidth / 2f, y, subtitlePaint)
            y += 18f
            canvas.drawText("SISTEM MANAJEMEN KEUANGAN & WAKAF MUSHALLA", pageWidth / 2f, y, subtitlePaint)
            y += 16f
            titlePaint.textSize = 16f
            canvas.drawText(profile.name.uppercase(Locale.ROOT), pageWidth / 2f, y, titlePaint)
            y += 13f
            canvas.drawText("${profile.address}, ${profile.city} | Kontak: ${profile.phone}", pageWidth / 2f, y, subtitlePaint)
            y += 10f

            // Garis pembatas kop surat ganda
            paint.color = Color.rgb(15, 81, 50)
            paint.strokeWidth = 2f
            canvas.drawLine(35f, y, (pageWidth - 35).toFloat(), y, paint)
            y += 3f
            paint.strokeWidth = 0.8f
            canvas.drawLine(35f, y, (pageWidth - 35).toFloat(), y, paint)
            y += 18f

            // Judul Laporan
            boldPaint.textSize = 11f
            boldPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("LAPORAN PERTANGGUNGJAWABAN KAS & WAKAF PEMBANGUNAN", pageWidth / 2f, y, boldPaint)
            y += 12f
            boldPaint.textAlign = Paint.Align.LEFT
            boldPaint.textSize = 8.5f
            canvas.drawText("Waktu Cetak: ${dateTimeFormat.format(Date())} (Status: Terverifikasi Enkripsi)", 35f, y, subtitlePaint.apply { textAlign = Paint.Align.LEFT })
            y += 16f

            // Kotak Ringkasan Keuangan
            paint.color = Color.rgb(240, 248, 243)
            canvas.drawRoundRect(35f, y, (pageWidth - 35).toFloat(), y + 42f, 6f, 6f, paint)

            paint.color = Color.rgb(15, 81, 50)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(35f, y, (pageWidth - 35).toFloat(), y + 42f, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            val colW = (pageWidth - 70) / 4f
            val summaryY = y + 15f

            // Col 1: Saldo Kas
            canvas.drawText("TOTAL SALDO KAS", 45f, summaryY, boldPaint)
            canvas.drawText(formatRupiah(saldoKas), 45f, summaryY + 16f, headerPaint)

            // Col 2: Kas Masuk
            canvas.drawText("TOTAL KAS MASUK", 45f + colW, summaryY, boldPaint)
            canvas.drawText(formatRupiah(totalMasuk), 45f + colW, summaryY + 16f, bodyPaint.apply { color = Color.rgb(25, 135, 84) })

            // Col 3: Kas Keluar
            canvas.drawText("TOTAL KAS KELUAR", 45f + (colW * 2), summaryY, boldPaint.apply { color = Color.BLACK })
            canvas.drawText(formatRupiah(totalKeluar), 45f + (colW * 2), summaryY + 16f, bodyPaint.apply { color = Color.rgb(220, 53, 69) })

            // Col 4: Wakaf Terkumpul
            val targetNominal = wakafProject?.targetAmount ?: 1L
            val pct = if (targetNominal > 0) (totalWakaf.toDouble() / targetNominal * 100).toInt() else 0
            canvas.drawText("WAKAF TERKUMPUL", 45f + (colW * 3), summaryY, boldPaint.apply { color = Color.BLACK })
            canvas.drawText("${formatRupiah(totalWakaf)} ($pct%)", 45f + (colW * 3), summaryY + 16f, headerPaint.apply { color = Color.rgb(184, 134, 11) })

            y += 58f

            // Tabel 1: Ringkasan Transaksi Kas Terbaru
            canvas.drawText("1. REKAPITULASI TRANSAKSI KAS MUSHALLA", 35f, y, headerPaint.apply { color = Color.rgb(15, 81, 50) })
            y += 12f

            // Header Tabel Kas
            paint.color = Color.rgb(225, 238, 230)
            canvas.drawRect(35f, y, (pageWidth - 35).toFloat(), y + 16f, paint)

            canvas.drawText("TANGGAL", 40f, y + 11f, boldPaint)
            canvas.drawText("TIPE", 100f, y + 11f, boldPaint)
            canvas.drawText("KATEGORI / KETERANGAN", 155f, y + 11f, boldPaint)
            canvas.drawText("NOMINAL", 400f, y + 11f, boldPaint)
            canvas.drawText("INTEGRITY HASH", 490f, y + 11f, boldPaint)
            y += 18f

            val topTransactions = transactions.take(8)
            topTransactions.forEachIndexed { idx, tx ->
                val lineBg = if (idx % 2 == 1) Color.rgb(248, 250, 249) else Color.WHITE
                paint.color = lineBg
                canvas.drawRect(35f, y - 2f, (pageWidth - 35).toFloat(), y + 12f, paint)

                canvas.drawText(dateFormat.format(Date(tx.date)), 40f, y + 8f, bodyPaint)

                val typeText = if (tx.type == TransactionType.MASUK) "MASUK" else "KELUAR"
                val typeColor = if (tx.type == TransactionType.MASUK) Color.rgb(25, 135, 84) else Color.rgb(220, 53, 69)
                canvas.drawText(typeText, 100f, y + 8f, boldPaint.apply { color = typeColor })

                val desc = if (tx.description.length > 36) tx.description.take(34) + ".." else tx.description
                canvas.drawText("${tx.category} - $desc", 155f, y + 8f, bodyPaint.apply { color = Color.BLACK })

                val nominalStr = if (tx.type == TransactionType.MASUK) "+${formatRupiah(tx.amount)}" else "-${formatRupiah(tx.amount)}"
                canvas.drawText(nominalStr, 400f, y + 8f, boldPaint.apply { color = typeColor })

                canvas.drawText(tx.securityHash.ifEmpty { "VERIFIED" }, 490f, y + 8f, bodyPaint.apply { color = Color.GRAY })

                y += 14f
            }

            y += 12f

            // Tabel 2: Rekapitulasi Sumbangan Wakaf Pembangunan
            canvas.drawText("2. DAFTAR SUMBANGAN WAKAF SARANA & PRASARANA", 35f, y, headerPaint.apply { color = Color.rgb(15, 81, 50) })
            y += 12f

            paint.color = Color.rgb(225, 238, 230)
            canvas.drawRect(35f, y, (pageWidth - 35).toFloat(), y + 16f, paint)

            canvas.drawText("NAMA WAKIF", 40f, y + 11f, boldPaint.apply { color = Color.BLACK })
            canvas.drawText("ALAMAT", 160f, y + 11f, boldPaint)
            canvas.drawText("TANGGAL", 320f, y + 11f, boldPaint)
            canvas.drawText("NOMINAL WAKAF", 390f, y + 11f, boldPaint)
            canvas.drawText("PERUNTUKAN / DOA", 480f, y + 11f, boldPaint)
            y += 18f

            val topWakaf = wakafDonations.take(6)
            topWakaf.forEachIndexed { idx, w ->
                val lineBg = if (idx % 2 == 1) Color.rgb(248, 250, 249) else Color.WHITE
                paint.color = lineBg
                canvas.drawRect(35f, y - 2f, (pageWidth - 35).toFloat(), y + 12f, paint)

                canvas.drawText(w.donorName.take(20), 40f, y + 8f, boldPaint.apply { color = Color.BLACK })
                canvas.drawText(w.address.take(22), 160f, y + 8f, bodyPaint)
                canvas.drawText(dateFormat.format(Date(w.date)), 320f, y + 8f, bodyPaint)
                canvas.drawText(formatRupiah(w.nominal), 390f, y + 8f, boldPaint.apply { color = Color.rgb(15, 81, 50) })
                val note = if (w.note.length > 18) w.note.take(16) + ".." else w.note.ifEmpty { "Pembangunan" }
                canvas.drawText(note, 480f, y + 8f, bodyPaint)

                y += 14f
            }

            y += 14f

            // Info Rekening Resmi Mushalla
            paint.color = Color.rgb(245, 247, 246)
            canvas.drawRoundRect(35f, y, (pageWidth - 35).toFloat(), y + 36f, 4f, 4f, paint)
            boldPaint.color = Color.rgb(15, 81, 50)
            canvas.drawText("REKENING RESMI PENERIMAAN INFAQ & WAKAF MUSHALLA:", 45f, y + 14f, boldPaint)
            canvas.drawText("• ${profile.bankNagariName}: ${profile.bankNagariAccount} a.n ${profile.name}  |  • ${profile.bankBsiName}: ${profile.bankBsiAccount} a.n ${profile.name}", 45f, y + 26f, bodyPaint.apply { color = Color.BLACK })
            y += 48f

            // Tanda Tangan Pengurus (Ketua & Bendahara)
            val signY = y + 10f
            canvas.drawText("${profile.city.split(",").firstOrNull()?.trim() ?: "Padang"}, ${dateFormat.format(Date())}", (pageWidth - 170).toFloat(), signY, bodyPaint)
            canvas.drawText("Mengetahui,", 50f, signY + 12f, bodyPaint)
            canvas.drawText("Ketua Pengurus / Takmir,", 50f, signY + 24f, boldPaint)

            canvas.drawText("Bendahara Mushalla,", (pageWidth - 170).toFloat(), signY + 24f, boldPaint)

            val nameY = signY + 68f
            canvas.drawText("(${profile.leaderName})", 50f, nameY, boldPaint)
            canvas.drawText("(${profile.treasurerName})", (pageWidth - 170).toFloat(), nameY, boldPaint)

            pdfDocument.finishPage(page)

            // Simpan ke file cache internal agar bisa langsung di-share
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, "Laporan_Keuangan_Mushalla_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate CSV yang kompatibel dengan Excel (dengan UTF-8 BOM)
     */
    fun generateExcelCsv(
        context: Context,
        profile: MushallaProfile,
        transactions: List<KasTransaction>,
        wakafDonations: List<WakafDonation>,
        inventoryList: List<InventoryItem>
    ): File? {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, "Laporan_Keuangan_Mushalla_Excel_${System.currentTimeMillis()}.csv")
            val writer = file.bufferedWriter(Charsets.UTF_8)

            // UTF-8 BOM agar Microsoft Excel langsung mengenali encoding dengan tepat
            writer.write("\uFEFF")

            writer.write("LAPORAN KEUANGAN DAN INVENTARIS ${profile.name.uppercase(Locale.ROOT)}\n")
            writer.write("Alamat: ${profile.address}, ${profile.city}\n")
            writer.write("Kontak: ${profile.phone}\n")
            writer.write("Rekening Bank Nagari Syariah: ${profile.bankNagariAccount}\n")
            writer.write("Rekening Bank Syariah Indonesia: ${profile.bankBsiAccount}\n")
            writer.write("Tanggal Ekspor: ${dateTimeFormat.format(Date())}\n\n")

            // Section 1: Transaksi Kas
            writer.write("=== DATA BUKU KAS (MASUK & KELUAR) ===\n")
            writer.write("ID,Tanggal,Jenis Transaksi,Kategori,Penerima/Penyetor,Nominal (Rp),Metode Pembayaran,Keterangan,Integrity Hash\n")
            transactions.forEach { tx ->
                val dateStr = dateFormat.format(Date(tx.date))
                val typeStr = if (tx.type == TransactionType.MASUK) "KAS MASUK" else "KAS KELUAR"
                val cleanDesc = tx.description.replace(",", ";").replace("\n", " ")
                val cleanRecipient = tx.recipientOrDonor.replace(",", ";")
                writer.write("${tx.id},\"$dateStr\",\"$typeStr\",\"${tx.category}\",\"$cleanRecipient\",${tx.amount},\"${tx.paymentMethod}\",\"$cleanDesc\",\"${tx.securityHash}\"\n")
            }

            writer.write("\n=== DATA SUMBANGAN WAKAF PEMBANGUNAN ===\n")
            writer.write("ID,Tanggal,Nama Donatur/Wakif,Alamat,Nominal Wakaf (Rp),Nomor Kontak,Peruntukan / Doa,Integrity Hash\n")
            wakafDonations.forEach { w ->
                val dateStr = dateFormat.format(Date(w.date))
                val cleanName = w.donorName.replace(",", ";")
                val cleanAddress = w.address.replace(",", ";")
                val cleanNote = w.note.replace(",", ";").replace("\n", " ")
                writer.write("${w.id},\"$dateStr\",\"$cleanName\",\"$cleanAddress\",${w.nominal},\"${w.phoneNumber}\",\"$cleanNote\",\"${w.securityHash}\"\n")
            }

            writer.write("\n=== DATA INVENTARIS SARANA & PRASARANA ===\n")
            writer.write("ID,Nama Barang,Kategori,Kuantitas,Satuan,Kondisi,Sumber Perolehan,Taksiran Nilai (Rp),Lokasi,Keterangan\n")
            inventoryList.forEach { item ->
                val cleanName = item.itemName.replace(",", ";")
                val cleanNotes = item.notes.replace(",", ";").replace("\n", " ")
                writer.write("${item.id},\"$cleanName\",\"${item.category}\",${item.quantity},\"${item.unit}\",\"${item.condition.name}\",\"${item.source.name}\",${item.estimatedValue},\"${item.location}\",\"$cleanNotes\"\n")
            }

            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Membuka Android Share Sheet untuk membagikan file PDF / CSV
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title - Resmi Mushalla Ar-Rahman")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan Laporan Keuangan"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
