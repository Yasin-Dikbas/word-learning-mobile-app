package com.nisanurguven.wordleloop

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Locale

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "WordleLoop.db"
        private const val DATABASE_VERSION = 49 // LLM ve Quiz mantığı için güncelledik
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE Categories (categoryID INTEGER PRIMARY KEY AUTOINCREMENT, categoryName TEXT NOT NULL)")
        db.execSQL("CREATE TABLE Difficulties (difficultyID INTEGER PRIMARY KEY AUTOINCREMENT, levelName TEXT NOT NULL)")
        db.execSQL("CREATE TABLE Users (userID INTEGER PRIMARY KEY AUTOINCREMENT, userName TEXT NOT NULL, password TEXT NOT NULL, email TEXT, securityQuestion TEXT, securityAnswer TEXT, securityHint TEXT, createdAt TEXT)")

        db.execSQL("CREATE TABLE Words (wordID INTEGER PRIMARY KEY AUTOINCREMENT, english TEXT NOT NULL, turkish TEXT NOT NULL, phonetic TEXT, turkish_reading TEXT, categoryID INTEGER, difficultyID INTEGER, imageUri TEXT, userID INTEGER, FOREIGN KEY (categoryID) REFERENCES Categories(categoryID), FOREIGN KEY (difficultyID) REFERENCES Difficulties(difficultyID), FOREIGN KEY (userID) REFERENCES Users(userID))")

        db.execSQL("CREATE TABLE WordSamples (sampleID INTEGER PRIMARY KEY AUTOINCREMENT, wordID INTEGER, sampleSentence TEXT NOT NULL, FOREIGN KEY (wordID) REFERENCES Words(wordID))")

        db.execSQL("CREATE TABLE UserProgress (progressID INTEGER PRIMARY KEY AUTOINCREMENT, userID INTEGER, wordID INTEGER UNIQUE, correctStreak INTEGER DEFAULT 0, currentStage INTEGER DEFAULT 0, nextReviewDate INTEGER, isLearned INTEGER DEFAULT 0, lastAnsweredDate INTEGER, stageErrors TEXT DEFAULT '0,0,0,0,0,0', FOREIGN KEY (userID) REFERENCES Users(userID), FOREIGN KEY (wordID) REFERENCES Words(wordID))")

        val categories = listOf("General", "Business", "Academic", "Travel", "Health", "Science", "Social", "Phrasal Verbs")
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
    fun addUser(userName: String, password: String, email: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("userName", userName)
            put("password", password)
            put("email", email)
            put("createdAt", System.currentTimeMillis().toString())
        }
        return db.insert("Users", null, values) != -1L
    }

    fun checkUser(userName: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT userID FROM Users WHERE userName = ? AND password = ? LIMIT 1", arrayOf(userName, password))
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun getSecurityData(email: String): Triple<String, String, String>? {
        val db = this.readableDatabase
        // SORGUDAN userName = ? YERİNE email = ? YAPTIK
        val cursor = db.rawQuery("SELECT securityQuestion, securityAnswer, securityHint FROM Users WHERE email = ?", arrayOf(email))
        return if (cursor.moveToFirst()) {
            val data = Triple(cursor.getString(0) ?: "", cursor.getString(1) ?: "", cursor.getString(2) ?: "")
            cursor.close()
            data
        } else { cursor.close(); null }
    }

    fun updatePassword(email: String, newPass: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply { put("password", newPass) }
        // SORGUDAN userName = ? YERİNE email = ? YAPTIK
        return db.update("Users", values, "email = ?", arrayOf(email)) > 0
    }
    fun getUserStreak(): Int {
        val db = this.readableDatabase
        val currentTime = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        var streak = 0
        try {
            val cursor = db.rawQuery("SELECT COUNT(*) FROM UserProgress WHERE lastAnsweredDate > ?", arrayOf((currentTime - oneDayMs).toString()))
            if (cursor.moveToFirst()) streak = if (cursor.getInt(0) > 0) 1 else 0
            cursor.close()
        } catch (e: Exception) { e.printStackTrace() }
        return streak
    }

    // --- KELİME VE İLERLEME İŞLEMLERİ ---
    fun updateWordProgress(word: Word, isCorrect: Boolean) {
        val db = this.writableDatabase
        val currentTime = System.currentTimeMillis()

        val intervals = listOf(
            0L,              // Stage 0: Hemen
            0L,              // Stage 1: Hemen (Test için 0 bıraktık)
            604800000L,      // Stage 2: 1 Hafta
            2592000000L,     // Stage 3: 1 Ay
            7776000000L,     // Stage 4: 3 Ay
            15552000000L     // Stage 5: 6 Ay
        )

        if (isCorrect) {
            if (word.repetitionLevel < intervals.size - 1) {
                word.repetitionLevel++
            } else {
                word.isLearned = 1
            }
        } else {
            word.repetitionLevel = 0
            word.correctCount = 0
            word.isLearned = 0
        }

        val nextDate = if (word.repetitionLevel < intervals.size) {
            currentTime + intervals[word.repetitionLevel]
        } else {
            currentTime + intervals.last()
        }

        val values = ContentValues().apply {
            put("wordID", word.id)
            put("correctStreak", word.correctCount)
            put("currentStage", word.repetitionLevel)
            put("nextReviewDate", nextDate)
            put("isLearned", word.isLearned)
            put("lastAnsweredDate", currentTime)
        }
        db.insertWithOnConflict("UserProgress", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun resetWordProgress(wordID: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("currentStage", 0)
            put("correctStreak", 0)
            put("isLearned", 0)
            put("nextReviewDate", System.currentTimeMillis())
        }
        db.update("UserProgress", values, "wordID = ?", arrayOf(wordID.toString()))
    }

    // --- QUIZ VE FİLTRELEME ---
    fun getQuizWords(limit: Int = 10): List<Word> {
        val db = this.readableDatabase
        val wordList = mutableListOf<Word>()
        val currentTime = System.currentTimeMillis()

        val query = """
            SELECT w.*, s.sampleSentence, p.correctStreak, p.currentStage, p.isLearned 
            FROM Words w 
            INNER JOIN UserProgress p ON w.wordID = p.wordID 
            LEFT JOIN WordSamples s ON w.wordID = s.wordID
            WHERE p.lastAnsweredDate IS NOT NULL 
            AND (p.nextReviewDate <= $currentTime OR p.nextReviewDate IS NULL)
            ORDER BY RANDOM() LIMIT $limit
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                wordList.add(cursorToWord(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return wordList
    }

    fun getFilteredWords(context: Context): List<Word> {
        val sharedPref = context.getSharedPreferences("LoopWordsSettings", Context.MODE_PRIVATE)
        val dailyGoal = sharedPref.getInt("daily_goal", 10)
        val categoriesString = sharedPref.getString("categories", "1,2,3,4,5,6,7,8") ?: "1,2,3,4,5,6,7,8"
        val currentTime = System.currentTimeMillis()
        val wordList = mutableListOf<Word>()
        val db = this.readableDatabase

        val query = """
            SELECT w.*, s.sampleSentence, p.correctStreak, p.currentStage, p.isLearned, p.nextReviewDate 
            FROM Words w
            LEFT JOIN WordSamples s ON w.wordID = s.wordID
            LEFT JOIN UserProgress p ON w.wordID = p.wordID
            WHERE w.categoryID IN ($categoriesString) 
            AND (p.isLearned IS NULL OR p.isLearned = 0)
            AND (p.nextReviewDate IS NULL OR p.nextReviewDate <= $currentTime)
            ORDER BY RANDOM() LIMIT $dailyGoal
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do { wordList.add(cursorToWord(cursor)) } while (cursor.moveToNext())
        }
        cursor.close()
        return wordList
    }

    private fun cursorToWord(cursor: android.database.Cursor): Word {
        return Word(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("wordID")),
            english = cursor.getString(cursor.getColumnIndexOrThrow("english")),
            turkish = cursor.getString(cursor.getColumnIndexOrThrow("turkish")),
            phonetic = cursor.getString(cursor.getColumnIndexOrThrow("phonetic")) ?: "",
            turkishReading = cursor.getString(cursor.getColumnIndexOrThrow("turkish_reading")) ?: "",
            categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("categoryID")),
            difficulty = cursor.getInt(cursor.getColumnIndexOrThrow("difficultyID")),
            sampleSentence = cursor.getString(cursor.getColumnIndexOrThrow("sampleSentence")) ?: "",
            imagePath = cursor.getString(cursor.getColumnIndexOrThrow("imageUri")),
            correctCount = if (cursor.getColumnIndex("correctStreak") != -1) cursor.getInt(cursor.getColumnIndexOrThrow("correctStreak")) else 0,
            repetitionLevel = if (cursor.getColumnIndex("currentStage") != -1) cursor.getInt(cursor.getColumnIndexOrThrow("currentStage")) else 0,
            isLearned = if (cursor.getColumnIndex("isLearned") != -1) cursor.getInt(cursor.getColumnIndexOrThrow("isLearned")) else 0
        )
    }

    // --- DİĞER FONKSİYONLAR ---
    fun updateWordStatusInDb(wordText: String) {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT wordID FROM Words WHERE english = ? LIMIT 1", arrayOf(wordText.uppercase(Locale.ROOT)))
        if (cursor.moveToFirst()) {
            val wordId = cursor.getInt(0)
            val values = ContentValues().apply {
                put("wordID", wordId)
                put("isLearned", 1)
                put("lastAnsweredDate", System.currentTimeMillis())
            }
            db.insertWithOnConflict("UserProgress", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
        cursor.close()
    }

    fun getWordleWord(): Word? {
        val db = this.readableDatabase
        val query = "SELECT w.*, s.sampleSentence FROM Words w LEFT JOIN WordSamples s ON w.wordID = s.wordID WHERE length(trim(w.english)) = 5 ORDER BY RANDOM() LIMIT 1"
        val cursor = db.rawQuery(query, null)
        return if (cursor.moveToFirst()) {
            val word = cursorToWord(cursor)
            cursor.close()
            word
        } else { cursor.close(); null }
    }

    fun getFullReportData(): Map<String, Any> {
        val db = this.readableDatabase
        val data = mutableMapOf<String, Any>()
        val totalWords = db.rawQuery("SELECT COUNT(*) FROM Words", null).use { if (it.moveToFirst()) it.getInt(0) else 0 }
        val learnedWords = db.rawQuery("SELECT COUNT(*) FROM UserProgress WHERE isLearned = 1", null).use { if (it.moveToFirst()) it.getInt(0) else 0 }
        data["overallPercentage"] = if (totalWords > 0) (learnedWords * 100) / totalWords else 0

        val categoryList = mutableListOf<String>()
        val catQuery = "SELECT c.categoryName, (SELECT COUNT(*) FROM Words w WHERE w.categoryID = c.categoryID) as total, (SELECT COUNT(*) FROM UserProgress p JOIN Words w ON p.wordID = w.wordID WHERE w.categoryID = c.categoryID AND p.isLearned = 1) as learned FROM Categories c"
        db.rawQuery(catQuery, null).use {
            while (it.moveToNext()) {
                val name = it.getString(0) ?: "Bilinmeyen"
                val total = it.getInt(1)
                val learned = it.getInt(2)
                val percent = if (total > 0) (learned * 100) / total else 0
                categoryList.add("• $name: $learned/$total (%$percent)")
            }
        }
        data["categoryProgress"] = categoryList
        data["mostFailedStage"] = if (learnedWords > 0) "Gelişmekte" else "Yeni Başlangıç"
        return data
    }

    fun importCSV(context: Context) {
        val db = this.writableDatabase
        try {
            val inputStream = context.assets.open("words.csv")
            val reader = inputStream.bufferedReader()
            db.beginTransaction()
            reader.readLine()
            reader.forEachLine { line ->
                if (line.isNotBlank()) {
                    val columns = line.split(";")
                    if (columns.size >= 7) {
                        val cv = ContentValues().apply {
                            put("english", columns[0].trim().uppercase(Locale.ROOT))
                            put("turkish", columns[1].trim())
                            put("phonetic", columns[2].trim())
                            put("turkish_reading", columns[3].trim())
                            put("categoryID", columns[6].trim().toIntOrNull() ?: 1)
                            put("difficultyID", 1)
                        }
                        val wordId = db.insert("Words", null, cv)
                        if (wordId != -1L) {
                            val sampleCv = ContentValues().apply {
                                put("wordID", wordId)
                                val engS = columns[4].trim()
                                val trS = columns[5].trim()
                                put("sampleSentence", "$engS | $trS")
                            }
                            db.insert("WordSamples", null, sampleCv)
                        }
                    }
                }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) { e.printStackTrace() } finally { if (db.inTransaction()) db.endTransaction() }
    }

    fun getWordByEnglish(englishWord: String): Word? {
        val db = this.readableDatabase
        val query = "SELECT w.*, s.sampleSentence FROM Words w LEFT JOIN WordSamples s ON w.wordID = s.wordID WHERE w.english = ? LIMIT 1"
        val cursor = db.rawQuery(query, arrayOf(englishWord.uppercase(Locale.ROOT)))
        return if (cursor.moveToFirst()) {
            val word = cursorToWord(cursor)
            cursor.close()
            word
        } else { cursor.close(); null }
    }

    fun getLearnedWordsToday(): List<String> {
        val wordList = mutableListOf<String>()
        val db = this.readableDatabase

        // Bugünün başlangıç zamanı (00:00:00)
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // Tablo isimlerini netleştirerek 'english' kolonunu çekiyoruz
        val query = """
            SELECT w.english 
            FROM Words w 
            JOIN UserProgress p ON w.wordID = p.wordID 
            WHERE p.lastAnsweredDate >= ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(startOfDay.toString()))
        if (cursor.moveToFirst()) {
            do {
                wordList.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return wordList
    }
}