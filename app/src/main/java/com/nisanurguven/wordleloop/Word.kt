package com.nisanurguven.wordleloop

data class Word(
    val id: Int,
    val english: String,
    val turkish: String,
    val phonetic: String? = null,
    val turkishReading: String? = null,
    val categoryId: Int,
    val difficulty: Int,
    val imagePath: String? = null,
    var correctCount: Int = 0,
    var lastCorrectDate: Long = 0,
    var repetitionLevel: Int = 0,
    var isLearned: Int = 0
)