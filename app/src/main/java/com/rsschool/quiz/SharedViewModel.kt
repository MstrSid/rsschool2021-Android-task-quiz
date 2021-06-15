package com.rsschool.quiz

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

class SharedViewModel: ViewModel() {
    private var questionNumber: Int = 1
    private var userId: Long = Random.nextLong(1, Long.MAX_VALUE)
    private var userAnswers = HashMap<Int, Int>()
    private var userAnswersText = HashMap<Int, String>()

    fun saveUserAnswers(uAnswers: HashMap<Int, Int>){
        userAnswers = uAnswers
    }

    fun saveUserAnswersText(uAnswersText: HashMap<Int, String>){
        userAnswersText = uAnswersText
    }

    fun saveQuestionNumber(qNumber: Int){
        questionNumber = qNumber
    }

    fun saveUserId(uId: Long){
        userId = uId
    }

    fun getUserAnswers():HashMap<Int, Int> = userAnswers

    fun getUserAnswersText():HashMap<Int, String> = userAnswersText

    fun getQuestionNumber():Int = questionNumber

    fun getUserId():Long = userId
}