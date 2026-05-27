package com.nisanurguven.wordleloop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class FlashcardAdapter(private val words: List<Word>) : RecyclerView.Adapter<FlashcardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardFront: View = view.findViewById(R.id.cardFront)
        val tvEnglishWord: TextView = view.findViewById(R.id.tvEnglishWord)

        val cardBack: View = view.findViewById(R.id.cardBack)
        val ivWordImage: ImageView = view.findViewById(R.id.ivWordImage)
        val tvPhonetic: TextView = view.findViewById(R.id.tvPhonetic)
        val tvTurkishReading: TextView = view.findViewById(R.id.tvTurkishReading)
        val tvSampleSentence: TextView = view.findViewById(R.id.tvSampleSentence)
        val tvSampleTurkish: TextView = view.findViewById(R.id.tvSampleTurkish)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_flashcard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = words[position]

        holder.tvEnglishWord.text = word.english.uppercase(Locale.getDefault())

        holder.tvPhonetic.text = word.phonetic
        holder.tvTurkishReading.text = "(${word.turkishReading})"

        val sentenceParts = word.sampleSentence.split("|")
        if (sentenceParts.size >= 2) {
            holder.tvSampleSentence.text = sentenceParts[0].trim()
            holder.tvSampleTurkish.text = "(${sentenceParts[1].trim()})"
        } else {
            holder.tvSampleSentence.text = word.sampleSentence
            holder.tvSampleTurkish.text = ""
        }

        // Başlangıçta daima ön yüz gösterilir
        holder.cardFront.visibility = View.VISIBLE
        holder.cardBack.visibility = View.GONE

        // Çevirme animasyonu olmadan anında dönüşüm
        holder.itemView.setOnClickListener {
            if (holder.cardFront.visibility == View.VISIBLE) {
                holder.cardFront.visibility = View.GONE
                holder.cardBack.visibility = View.VISIBLE
            } else {
                holder.cardFront.visibility = View.VISIBLE
                holder.cardBack.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = words.size
}