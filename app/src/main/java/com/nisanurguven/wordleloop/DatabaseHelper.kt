package com.nisanurguven.wordleloop

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "WordleLoop.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE Users (
                userID INTEGER PRIMARY KEY AUTOINCREMENT,
                userName TEXT NOT NULL,
                password TEXT NOT NULL,
                email TEXT,
                createdAt TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE Categories (
                categoryID INTEGER PRIMARY KEY AUTOINCREMENT,
                categoryName TEXT NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE Difficulties (
                difficultyID INTEGER PRIMARY KEY AUTOINCREMENT,
                levelName TEXT NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE Words (
                wordID INTEGER PRIMARY KEY AUTOINCREMENT,
                engWordName TEXT NOT NULL,
                turWordName TEXT NOT NULL,
                phonetic TEXT,
                categoryID INTEGER,
                difficultyID INTEGER,
                imageUri TEXT,
                userID INTEGER,
                FOREIGN KEY (categoryID) REFERENCES Categories(categoryID),
                FOREIGN KEY (difficultyID) REFERENCES Difficulties(difficultyID),
                FOREIGN KEY (userID) REFERENCES Users(userID)
            )
        """)

        db.execSQL("""
            CREATE TABLE WordSamples (
                sampleID INTEGER PRIMARY KEY AUTOINCREMENT,
                wordID INTEGER,
                sampleSentence TEXT NOT NULL,
                FOREIGN KEY (wordID) REFERENCES Words(wordID)
            )
        """)

        db.execSQL("""
            CREATE TABLE Settings (
                settingsID INTEGER PRIMARY KEY AUTOINCREMENT,
                dailyNewWordCount INTEGER DEFAULT 10,
                userID INTEGER,
                FOREIGN KEY (userID) REFERENCES Users(userID)
            )
        """)

        db.execSQL("""
            CREATE TABLE UserProgress (
                progressID INTEGER PRIMARY KEY AUTOINCREMENT,
                userID INTEGER,
                wordID INTEGER,
                correctStreak INTEGER DEFAULT 0,
                currentStage INTEGER DEFAULT 0,
                nextReviewDate TEXT,
                isLearned INTEGER DEFAULT 0,
                lastAnsweredDate TEXT,
                FOREIGN KEY (userID) REFERENCES Users(userID),
                FOREIGN KEY (wordID) REFERENCES Words(wordID)
            )
        """)

        db.execSQL("""
            CREATE TABLE QuizResults (
                resultID INTEGER PRIMARY KEY AUTOINCREMENT,
                userID INTEGER,
                wordID INTEGER,
                isCorrect INTEGER NOT NULL,
                answerDate TEXT,
                FOREIGN KEY (userID) REFERENCES Users(userID),
                FOREIGN KEY (wordID) REFERENCES Words(wordID)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS QuizResults")
        db.execSQL("DROP TABLE IF EXISTS UserProgress")
        db.execSQL("DROP TABLE IF EXISTS Settings")
        db.execSQL("DROP TABLE IF EXISTS WordSamples")
        db.execSQL("DROP TABLE IF EXISTS Words")
        db.execSQL("DROP TABLE IF EXISTS Difficulties")
        db.execSQL("DROP TABLE IF EXISTS Categories")
        db.execSQL("DROP TABLE IF EXISTS Users")

        onCreate(db)
    }
}