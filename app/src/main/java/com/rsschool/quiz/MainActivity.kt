package com.rsschool.quiz

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner


class MainActivity : AppCompatActivity(), ViewModelStoreOwner, Communicator {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        openQuizFragment()
        Log.i("shared", "create")

    }

    private fun openQuizFragment() {
        val firstFragment: Fragment = QuizFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, firstFragment)
        transaction.commit()
    }

    private fun openResultFragment(userId: Long, uAnswers: HashMap<Int, Int>, uAnswersText: HashMap<Int, String>) {
        val resultFragment: Fragment = ResultFragment.newInstance(userId, uAnswers, uAnswersText)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, resultFragment)
        transaction.commit()
    }

    override fun passToResult(userId: Long, uAnswers: HashMap<Int, Int>, uAnswersText: HashMap<Int, String>) {
        openResultFragment(userId, uAnswers, uAnswersText)
    }

    override fun startQuizFragment() {
        openQuizFragment()
    }

}