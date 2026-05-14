package com.nisanurguven.wordleloop

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "WordleLoop.db"
        // Kategori ve hata takibi şeması değiştiği için versiyonu yükselttik
        private const val DATABASE_VERSION = 5
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tablo Oluşturma İşlemleri
        db.execSQL("CREATE TABLE Users (userID INTEGER PRIMARY KEY AUTOINCREMENT, userName TEXT NOT NULL, password TEXT NOT NULL, email TEXT, createdAt TEXT)")
        db.execSQL("CREATE TABLE Categories (categoryID INTEGER PRIMARY KEY AUTOINCREMENT, categoryName TEXT NOT NULL)")
        db.execSQL("CREATE TABLE Difficulties (difficultyID INTEGER PRIMARY KEY AUTOINCREMENT, levelName TEXT NOT NULL)")
        db.execSQL("CREATE TABLE Words (wordID INTEGER PRIMARY KEY AUTOINCREMENT, engWordName TEXT NOT NULL, turWordName TEXT NOT NULL, phonetic TEXT, turkish_reading TEXT, categoryID INTEGER, difficultyID INTEGER, imageUri TEXT, userID INTEGER, FOREIGN KEY (categoryID) REFERENCES Categories(categoryID), FOREIGN KEY (difficultyID) REFERENCES Difficulties(difficultyID), FOREIGN KEY (userID) REFERENCES Users(userID))")
        db.execSQL("CREATE TABLE WordSamples (sampleID INTEGER PRIMARY KEY AUTOINCREMENT, wordID INTEGER, sampleSentence TEXT NOT NULL, FOREIGN KEY (wordID) REFERENCES Words(wordID))")
        db.execSQL("CREATE TABLE UserProgress (progressID INTEGER PRIMARY KEY AUTOINCREMENT, userID INTEGER, wordID INTEGER, correctStreak INTEGER DEFAULT 0, currentStage INTEGER DEFAULT 0, nextReviewDate INTEGER, isLearned INTEGER DEFAULT 0, lastAnsweredDate INTEGER, stageErrors TEXT DEFAULT '0,0,0,0,0,0', FOREIGN KEY (userID) REFERENCES Users(userID), FOREIGN KEY (wordID) REFERENCES Words(wordID))")

        // 8 Ana Kategoriyi Otomatik Ekleme
        val categories = listOf(
            "General (Daily Life)", "Business & Work", "Academic",
            "Travel & Tourism", "Health & Body", "Science & Tech",
            "Social & Feelings", "Phrasal Verbs"
        )
        categories.forEach { name ->
            db.execSQL("INSERT INTO Categories (categoryName) VALUES ('$name')")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS UserProgress")
        db.execSQL("DROP TABLE IF EXISTS WordSamples")
        db.execSQL("DROP TABLE IF EXISTS Words")
        db.execSQL("DROP TABLE IF EXISTS Difficulties")
        db.execSQL("DROP TABLE IF EXISTS Categories")
        db.execSQL("DROP TABLE IF EXISTS Users")
        onCreate(db)
    }

    // --- KULLANICI İŞLEMLERİ ---

    fun checkUser(userName: String, password: String): Boolean {
        val db = this.readableDatabase
        var exists = false
        val cursor = db.rawQuery("SELECT userID FROM Users WHERE userName = ? AND password = ? LIMIT 1", arrayOf(userName, password))
        cursor.use { if (it.moveToFirst()) exists = it.count > 0 }
        return exists
    }

    fun addUser(userName: String, password: String, email: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("userName", userName)
            put("password", password)
            put("email", email)
            put("createdAt", System.currentTimeMillis().toString())
        }
        val result = db.insert("Users", null, values)
        return result != -1L
    }

    // --- KELİME VE ALGORİTMA ---

    fun getFilteredWords(context: Context): List<Word> {
        val sharedPref = context.getSharedPreferences("LoopWordsSettings", Context.MODE_PRIVATE)
        val dailyGoal = sharedPref.getInt("daily_goal", 10)
        val difficulty = sharedPref.getInt("difficulty", 2)
        val categoriesString = sharedPref.getString("categories", "1") ?: "1"

        val currentTime = System.currentTimeMillis()
        val wordList = mutableListOf<Word>()
        val db = this.readableDatabase

        val query = """
            SELECT w.*, p.correctStreak, p.currentStage, p.isLearned, p.nextReviewDate 
            FROM Words w
            LEFT JOIN UserProgress p ON w.wordID = p.wordID
            WHERE w.categoryID IN ($categoriesString) 
            AND w.difficultyID = $difficulty 
            AND (p.isLearned IS NULL OR p.isLearned = 0)
            AND (p.nextReviewDate IS NULL OR p.nextReviewDate <= $currentTime)
            ORDER BY RANDOM()
            LIMIT $dailyGoal
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    wordList.add(Word(
                        id = it.getInt(it.getColumnIndexOrThrow("wordID")),
                        english = it.getString(it.getColumnIndexOrThrow("engWordName")),
                        turkish = it.getString(it.getColumnIndexOrThrow("turWordName")),
                        phonetic = it.getString(it.getColumnIndexOrThrow("phonetic")),
                        turkishReading = it.getString(it.getColumnIndexOrThrow("turkish_reading")),
                        categoryId = it.getInt(it.getColumnIndexOrThrow("categoryID")),
                        difficulty = it.getInt(it.getColumnIndexOrThrow("difficultyID")),
                        correctCount = it.getInt(it.getColumnIndexOrThrow("correctStreak")),
                        repetitionLevel = it.getInt(it.getColumnIndexOrThrow("currentStage")),
                        isLearned = it.getInt(it.getColumnIndexOrThrow("isLearned"))
                    ))
                } while (it.moveToNext())
            }
        }
        return wordList
    }

    fun updateWordProgress(word: Word, isCorrect: Boolean) {
        val db = this.writableDatabase
        val currentTime = System.currentTimeMillis()
        val intervals = listOf(0L, 86400000L, 604800000L, 2592000000L, 7776000000L, 15552000000L, 31536000000L)

        var stageErrors = "0,0,0,0,0,0"
        val errorCursor = db.rawQuery("SELECT stageErrors FROM UserProgress WHERE wordID = ?", arrayOf(word.id.toString()))
        errorCursor.use { if (it.moveToFirst()) stageErrors = it.getString(0) ?: "0,0,0,0,0,0" }

        val errorList = stageErrors.split(",").map { it.toInt() }.toMutableList()

        if (isCorrect) {
            word.correctCount++
            if (word.correctCount >= 6) {
                word.repetitionLevel++
                word.correctCount = 0
                if (word.repetitionLevel >= 6) word.isLearned = 1
            }
        } else {
            if (word.repetitionLevel < errorList.size) {
                errorList[word.repetitionLevel]++
            }
            word.correctCount = 0
        }

        val nextDate = if (word.repetitionLevel < intervals.size) currentTime + intervals[word.repetitionLevel] else currentTime

        val values = ContentValues().apply {
            put("wordID", word.id)
            put("correctStreak", word.correctCount)
            put("currentStage", word.repetitionLevel)
            put("nextReviewDate", nextDate)
            put("isLearned", word.isLearned)
            put("lastAnsweredDate", currentTime)
            put("stageErrors", errorList.joinToString(","))
        }
        db.insertWithOnConflict("UserProgress", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // --- RAPORLAMA ---

    fun getUserStreak(): Int {
        val db = this.readableDatabase
        val currentTime = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val query = "SELECT COUNT(*) FROM UserProgress WHERE lastAnsweredDate > ?"
        var hasActivityToday = false
        try {
            val cursor = db.rawQuery(query, arrayOf((currentTime - oneDayMs).toString()))
            cursor.use { if (it.moveToFirst()) hasActivityToday = it.getInt(0) > 0 }
        } catch (e: Exception) { e.printStackTrace() }
        return if (hasActivityToday) 1 else 0
    }

    fun getFullReportData(): Map<String, Any> {
        val db = this.readableDatabase
        val data = mutableMapOf<String, Any>()

        // 1. Genel Başarı Yüzdesi
        val totalWords = db.rawQuery("SELECT COUNT(*) FROM Words", null).use { if (it.moveToFirst()) it.getInt(0) else 0 }
        val learnedWords = db.rawQuery("SELECT COUNT(*) FROM UserProgress WHERE isLearned = 1", null).use { if (it.moveToFirst()) it.getInt(0) else 0 }
        data["overallPercentage"] = if (totalWords > 0) (learnedWords * 100) / totalWords else 0

        // 2. Kategori Bazlı Detaylı Gelişim (Öğrenilen/Toplam Formatı)
        val categoryList = mutableListOf<String>()
        val catQuery = """
            SELECT c.categoryName, 
                   (SELECT COUNT(*) FROM Words w WHERE w.categoryID = c.categoryID) as total,
                   (SELECT COUNT(*) FROM UserProgress p JOIN Words w ON p.wordID = w.wordID 
                    WHERE w.categoryID = c.categoryID AND p.isLearned = 1) as learned
            FROM Categories c
        """.trimIndent()

        db.rawQuery(catQuery, null).use {
            while (it.moveToNext()) {
                val name = it.getString(0)
                val total = it.getInt(1)
                val learned = it.getInt(2)
                val percent = if (total > 0) (learned * 100) / total else 0
                categoryList.add("• $name: $learned/$total Kelime (%$percent)")
            }
        }
        data["categoryProgress"] = categoryList

        // 3. En Çok Hata Yapılan Aşama Analizi
        val stageNames = listOf("Yeni Kelime", "1 Günlük", "1 Haftalık", "1 Aylık", "3 Aylık", "6 Aylık")
        val errorTotals = mutableListOf(0, 0, 0, 0, 0, 0)
        db.rawQuery("SELECT stageErrors FROM UserProgress", null).use {
            while (it.moveToNext()) {
                val errors = it.getString(0)?.split(",")?.map { it.toInt() }
                errors?.forEachIndexed { index, value -> if (index < 6) errorTotals[index] += value }
            }
        }
        val maxIdx = errorTotals.indexOf(errorTotals.maxOrNull() ?: 0)
        data["mostFailedStage"] = if (errorTotals.maxOrNull() ?: 0 > 0) stageNames[maxIdx] else "Veri Yok"

        return data
    }
}