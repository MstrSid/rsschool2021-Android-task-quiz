package com.rsschool.quiz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.RadioButton
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rsschool.quiz.databinding.FragmentQuizBinding
import com.rsschool.quiz.databinding.FragmentResultBinding


class QuizFragment : Fragment() {
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private val dbQuestions = Firebase.firestore
    private var userNumber: Long = 0
    private var numberOfQuestion: Int = 1
    private val source = Source.DEFAULT
    private lateinit var userAnswerDoc: DocumentReference
    private lateinit var communicator: Communicator
    private var userAnswers = HashMap<Int, Int>()
    private var userAnswersText = HashMap<Int, String>()
    private val questionsRef = dbQuestions.collection("quiz").document("questions")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("lcl", "onCreateView")
        val sharedViewModel = obtainViewModel() //change theme and color status bar
        val theme = sharedViewModel.getQuestionNumber()
        var contextWrapper = ContextThemeWrapper(this.context, R.style.First)
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        when (theme) {
            1 -> {
                contextWrapper = ContextThemeWrapper(this.context, R.style.First)
                window?.statusBarColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.deep_orange_100
                )
            }
            2 -> {
                contextWrapper = ContextThemeWrapper(this.context, R.style.Second)
                window?.statusBarColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.yellow_100
                )
            }
            3 -> {
                contextWrapper = ContextThemeWrapper(this.context, R.style.Third)
                window?.statusBarColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.deep_purple_100_dark
                )
            }
            4 -> {
                contextWrapper = ContextThemeWrapper(this.context, R.style.Four)
                window?.statusBarColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.cyan_100
                )
            }
            5 -> {
                contextWrapper = ContextThemeWrapper(this.context, R.style.Five)
                window?.statusBarColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.light_green_100
                )
            }
            6 -> {
                contextWrapper = ContextThemeWrapper(this.context, R.style.Six)
                window?.statusBarColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.light_blue_600
                )
            }
        }

            _binding =
                FragmentQuizBinding.inflate(
                    inflater.cloneInContext(contextWrapper),
                    container,
                    false
                )
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i("lcl", "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        val sharedViewModel = obtainViewModel()
        communicator = activity as Communicator
        userNumber = sharedViewModel.getUserId()
        numberOfQuestion = sharedViewModel.getQuestionNumber()
        userAnswers = sharedViewModel.getUserAnswers()
        userAnswersText = sharedViewModel.getUserAnswersText()
        userAnswerDoc = dbQuestions.collection("quiz").document("userAnswers$userNumber")
        Utils.onActivityCreateSetTheme(requireActivity())

        _binding?.toString()?.let { Log.i("lcl", it) }

        binding.previousButton.isEnabled = numberOfQuestion > 1
        if (numberOfQuestion == 1) binding.toolbar.navigationIcon = null

        binding.toolbar.setNavigationOnClickListener {
            binding.previousButton.callOnClick()
        }

        activity?.onBackPressedDispatcher?.addCallback( //system back button
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    binding.previousButton.callOnClick()
                }
            })


        getQuestion()

        binding.nextButton.setOnClickListener { //next question on button click
            splash()
            numberOfQuestion++
            queryForNextButton(sharedViewModel)
        }

        binding.previousButton.setOnClickListener { //prev question on button click
            splash()
            if (numberOfQuestion == 1) {
                it.isEnabled = false
            }
            --numberOfQuestion
            sharedViewModel.saveUserId(userNumber)
            sharedViewModel.saveQuestionNumber(numberOfQuestion)
            activity?.recreate()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, QuizFragment()).commit()
            getQuestion()
        }

    }

    private fun getQuestion() {
        binding.toolbar.title = "Вопрос $numberOfQuestion"
        binding.radioGroup.setOnCheckedChangeListener(null)
        binding.radioGroup.clearCheck()
        restoreCheck()
        processAnswer()
        queryForGetQuestion()
        binding.nextButton.isEnabled = false
    }

    private fun obtainViewModel(): SharedViewModel {
        return ViewModelProvider(requireActivity() as ViewModelStoreOwner).get(SharedViewModel::class.java)
    }

    private fun processAnswer() {
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId -> //on changed radiobutton save choose in firestore
            if (activity != null) {
                if (isInet(requireActivity())) { //check internet status
                    binding.nextButton.isEnabled = true
                    val numberAnswer =
                        group.indexOfChild(binding.radioGroup.findViewById(checkedId) as View)
                    userAnswers[numberOfQuestion] = numberAnswer
                    userAnswersText[numberOfQuestion] =
                        (view?.findViewById<RadioButton>(checkedId))?.text.toString()
                    val answer = mapOf( //create map for firestore
                        numberOfQuestion.toString() to mapOf(
                            "numberAnswer" to numberAnswer,
                            "textAnswer" to (view?.findViewById<RadioButton>(checkedId))?.text.toString()
                        )
                    )
                    userAnswerDoc.get().addOnSuccessListener {
                        if (it.exists()) {
                            userAnswerDoc.update(
                                answer //update answers in firestore if exist document
                            )
                        } else {
                            userAnswerDoc.set(
                                answer //create answers in firestore if exist document
                            )
                        }
                    }
                } else { //if no internet
                    Snackbar.make(
                        binding.root,
                        getString(R.string.txt_inet_status),
                        Snackbar.LENGTH_LONG
                    ).show()
                    getQuestion()
                }
            }
        }
    }

    private fun queryForNextButton(sharedViewModel: SharedViewModel) {
        questionsRef.get(source).addOnCompleteListener { task -> //request firestore document
            if (task.isSuccessful) {
                val questionsCol = task.result?.get("count").toString().toInt()
                if (activity != null) {
                    if (numberOfQuestion <= questionsCol) {
                        binding.previousButton.isEnabled = true
                        sharedViewModel.saveUserId(userNumber)
                        sharedViewModel.saveQuestionNumber(numberOfQuestion)
                        sharedViewModel.saveUserAnswers(userAnswers)
                        sharedViewModel.saveUserAnswersText(userAnswersText)
                        activity?.recreate()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerView, QuizFragment()).commit()
                        getQuestion()
                    } else {
                        communicator.passToResult(userNumber, userAnswers, userAnswersText)
                        _binding = null
                    }
                }
            }
        }
    }

    private fun queryForGetQuestion() {
        questionsRef.get(source).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val questionsCol = task.result?.get("count").toString().toInt()
                val document = task.result?.get(numberOfQuestion.toString()) as Map<*, *>
                binding.question.text = document["text"].toString()
                binding.optionOne.text = document["answer0"].toString()
                binding.optionTwo.text = document["answer1"].toString()
                binding.optionThree.text = document["answer2"].toString()
                binding.optionFour.text = document["answer3"].toString()
                binding.optionFive.text = document["answer4"].toString()
                if (activity != null) {
                    if (numberOfQuestion == questionsCol) {
                        binding.nextButton.text = getString(R.string.txt_submit)
                        binding.nextButton.isEnabled = true
                    }
                    if (numberOfQuestion > questionsCol) {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainerView, ResultFragment()).commit()
                    }
                }
            }
        }
    }

    private fun restoreCheck() { //restore checked in previous questions
        userAnswerDoc.get(source).addOnSuccessListener {
            if (it.exists()) {
                userAnswerDoc.get(source).addOnCompleteListener { task -> //get answers document for current user
                    if (task.isSuccessful) {
                        if (task.result?.get(numberOfQuestion.toString()) != null) {
                            val document =
                                task.result?.get(numberOfQuestion.toString()) as Map<*, *>
                            if (document["numberAnswer"] != null) {
                                val index = document["numberAnswer"].toString().toInt()
                                (binding.radioGroup.getChildAt(index) as RadioButton).isChecked = true //set checked answer from document
                            }
                        }
                    }
                }
            }
        }
    }

    private fun splash() { //for hide black screen on recreate activity
        val intent = Intent(activity, SplashActivity::class.java)
        startActivity(intent)
    }

    fun isInet(activity: Activity): Boolean { //che inet function
        val cm = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    override fun onDestroy() { //retrieve old sharedViewModel on destroy activity
        if (!requireActivity().isFinishing) {
            val oldViewModel = obtainViewModel()
            numberOfQuestion = oldViewModel.getQuestionNumber()
            userNumber = oldViewModel.getUserId()
            super.onDestroy()
            val newViewModel = obtainViewModel()
            if (newViewModel != oldViewModel) {
                newViewModel.saveQuestionNumber(numberOfQuestion)
                newViewModel.saveUserId(userNumber)
            }
        } else {
            super.onDestroy()
        }
    }
}