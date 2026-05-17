package com.nisanurguven.wordleloop

data class Word(
    val id: Int,                        // Veritabanındaki wordID
    val english: String,
    val turkish: String,
    val phonetic: String? = null,
    val turkishReading: String? = null, // Veritabanındaki turkish_reading
    val categoryId: Int,                // Veritabanındaki categoryID
    val difficulty: Int,                // Veritabanındaki difficultyID
    val sampleSentence: String = "",    // Örnek cümle alanı
    val imagePath: String? = null,      // BU SATIR EKSİKTİ: Resim yolu için
    var correctCount: Int = 0,          // correctStreak
    var repetitionLevel: Int = 0,       // currentStage
    var isLearned: Int = 0              // 0 veya 1
)