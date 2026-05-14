package com.nisanurguven.wordleloop

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "WordleLoop.db"
        private const val DATABASE_VERSION = 3 // Versiyonu 3 yaptık (Sıfırlama için)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE Users (userID INTEGER PRIMARY KEY AUTOINCREMENT, userName TEXT NOT NULL, password TEXT NOT NULL, email TEXT, createdAt TEXT)")
        db.execSQL("CREATE TABLE Categories (categoryID INTEGER PRIMARY KEY AUTOINCREMENT, categoryName TEXT NOT NULL)")
        db.execSQL("CREATE TABLE Difficulties (difficultyID INTEGER PRIMARY KEY AUTOINCREMENT, levelName TEXT NOT NULL)")
        db.execSQL("CREATE TABLE Words (wordID INTEGER PRIMARY KEY AUTOINCREMENT, engWordName TEXT NOT NULL, turWordName TEXT NOT NULL, phonetic TEXT, turkish_reading TEXT, categoryID INTEGER, difficultyID INTEGER, imageUri TEXT, userID INTEGER, FOREIGN KEY (categoryID) REFERENCES Categories(categoryID), FOREIGN KEY (difficultyID) REFERENCES Difficulties(difficultyID), FOREIGN KEY (userID) REFERENCES Users(userID))")
        db.execSQL("CREATE TABLE WordSamples (sampleID INTEGER PRIMARY KEY AUTOINCREMENT, wordID INTEGER, sampleSentence TEXT NOT NULL, FOREIGN KEY (wordID) REFERENCES Words(wordID))")
        db.execSQL("CREATE TABLE UserProgress (progressID INTEGER PRIMARY KEY AUTOINCREMENT, userID INTEGER, wordID INTEGER, correctStreak INTEGER DEFAULT 0, currentStage INTEGER DEFAULT 0, nextReviewDate INTEGER, isLearned INTEGER DEFAULT 0, lastAnsweredDate INTEGER, FOREIGN KEY (userID) REFERENCES Users(userID), FOREIGN KEY (wordID) REFERENCES Words(wordID))")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Hata almaman için tüm tabloları sırayla siliyoruz
        db.execSQL("DROP TABLE IF EXISTS UserProgress")
        db.execSQL("DROP TABLE IF EXISTS WordSamples")
        db.execSQL("DROP TABLE IF EXISTS Words")
        db.execSQL("DROP TABLE IF EXISTS Difficulties")
        db.execSQL("DROP TABLE IF EXISTS Categories")
        db.execSQL("DROP TABLE IF EXISTS Users")
        onCreate(db)
    }

    fun checkUser(userName: String, password: String): Boolean {
        val db = this.readableDatabase
        var exists = false
        // Cursor.use kullanımı bellek sızıntısını ve kilitlenmeyi önler
        val cursor = db.rawQuery("SELECT userID FROM Users WHERE userName = ? AND password = ? LIMIT 1", arrayOf(userName, password))
        cursor.use {
            if (it.moveToFirst()) {
                exists = it.count > 0
            }
        }
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

        if (isCorrect) {
            word.correctCount++
            if (word.correctCount >= 6) {
                word.repetitionLevel++
                word.correctCount = 0
                if (word.repetitionLevel >= 6) word.isLearned = 1
            }
        } else {
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
        }
        db.insertWithOnConflict("UserProgress", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}