package com.rsschool.quiz

interface Communicator {

    fun passToResult(userId: Long, uAnswers: HashMap<Int, Int>, uAnswersText: HashMap<Int, String>)

    fun startQuizFragment()

}