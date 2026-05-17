package com.nisanurguven.wordleloop

import android.content.Context
import android.content.SharedPreferences

class XpManager(context: Context) {

    // Sabit değişkenleri companion object içinde tanımlamak best practice'dir.
    // Bu sayede "current_xp" yazarken hata yapma ihtimalin ortadan kalkar.
    companion object {
        private const val PREFS_NAME = "UserXP"
        private const val KEY_XP = "current_xp"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Mevcut XP değerini döndürür.
     */
    fun getXP(): Int {
        return sharedPreferences.getInt(KEY_XP, 0)
    }

    /**
     * Mevcut XP'ye miktar ekler veya çıkarır.
     * @param amount Eklenecek miktar (Çıkarmak için negatif değer girin)
     */
    fun addXP(amount: Int) {
        val currentXP = getXP()
        val newXP = currentXP + amount

        // XP'nin asla 0'ın altına düşmemesini sağlıyoruz.
        val finalXP = if (newXP < 0) 0 else newXP

        saveXP(finalXP)
    }

    /**
     * XP değerini doğrudan set eder.
     */
    fun setXP(value: Int) {
        val finalValue = if (value < 0) 0 else value
        saveXP(finalValue)
    }

    /**
     * SharedPreferences kayıt işlemini yapan private yardımcı fonksiyon.
     */
    private fun saveXP(value: Int) {
        sharedPreferences.edit().putInt(KEY_XP, value).apply()
    }
}