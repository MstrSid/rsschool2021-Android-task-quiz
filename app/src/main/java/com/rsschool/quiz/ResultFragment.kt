package com.rsschool.quiz

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rsschool.quiz.databinding.FragmentResultBinding
import kotlin.system.exitProcess


class ResultFragment : Fragment() {
    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var communicator: Communicator
    private val dbQuestions = Firebase.firestore
    private val questionsRef = dbQuestions.collection("quiz").document("questions")
    private val source = Source.DEFAULT
    private var userAnswers = mapOf<Int, Int>()
    private var userAnswersText = mapOf<Int, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentResultBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        communicator = activity as Communicator

        userAnswers = arguments?.getSerializable(USER_ANSWERS_ID_KEY) as HashMap<Int, Int>
        userAnswersText = arguments?.getSerializable(USER_ANSWERS_TEXT_KEY) as HashMap<Int, String>
        val userId = arguments?.getLong(USER_ID_KEY) as Long

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    binding.restartButton.callOnClick()
                }
            })

        binding.userId.text = "id: $userId"

        readRightAnswers {
            var counter = 0
            it.forEach { (t, u) ->
                if (userAnswers[t] == u) {
                    ++counter
                }
            }
            binding.rightAnswers.text =
                "Ваш результат: ${(counter.toDouble() / userAnswers.size * 100).toInt()}%"
        }

        binding.closeButton.setOnClickListener {
            activity?.finish()
            exitProcess(0)
        }

        binding.restartButton.setOnClickListener {
            startActivity(Intent(context, MainActivity::class.java))
            activity?.finish()
        }

        binding.shareButton.setOnClickListener {
            readAnswers {
                val intent = Intent(Intent.ACTION_SEND)
                intent.data = Uri.parse("mailto:")
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "Ваши результаты квиза")
                val sb = StringBuilder()
                sb.append("id: $userId\n\n")
                sb.append("${binding.rightAnswers.text}\n\n")
                it.forEach { (t, u) ->
                    sb.append("$t) $u\n")
                    sb.append("Ваш ответ: ${userAnswersText[t]}\n\n")
                    intent.putExtra(Intent.EXTRA_TEXT, sb.toString())
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()

                }
            }
        }
    }

    private fun readRightAnswers(myCallback: (Map<Int, Int>) -> Unit) { //request for right answers in Map
        questionsRef.get(source).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val map = mutableMapOf<Int, Int>()
                val questionsCol = task.result?.get("count").toString().toInt()
                for (i in 1..questionsCol) {
                    val document = task.result?.get(i.toString()) as Map<*, *>
                    map[i] = document["rightAnswer"].toString().toInt()
                }
                myCallback(map.toMap())
            }
        }
    }

    private fun readAnswers(myCallback: (Map<Int, String>) -> Unit) { //request for all answers in Map
        questionsRef.get(source).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val map = mutableMapOf<Int, String>()
                val questionsCol = task.result?.get("count").toString().toInt()
                for (i in 1..questionsCol) {
                    val document = task.result?.get(i.toString()) as Map<*, *>
                    map[i] = document["text"].toString()
                }
                myCallback(map.toMap())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance(
            userId: Long,
            userAnswers: HashMap<Int, Int>,
            userAnswersText: HashMap<Int, String>
        ): ResultFragment {
            val fragment = ResultFragment()
            val args = Bundle()
            args.putLong(USER_ID_KEY, userId)
            args.putSerializable(USER_ANSWERS_ID_KEY, userAnswers)
            args.putSerializable(USER_ANSWERS_TEXT_KEY, userAnswersText)
            fragment.arguments = args
            return fragment
        }

        private const val USER_ID_KEY = "USER_ID"
        private const val USER_ANSWERS_ID_KEY = "USER_ANSWERS_ID"
        private const val USER_ANSWERS_TEXT_KEY = "USER_ANSWERS_TEXT"

    }
}