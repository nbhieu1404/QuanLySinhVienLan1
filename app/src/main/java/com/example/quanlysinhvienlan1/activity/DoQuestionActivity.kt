package com.example.quanlysinhvienlan1.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.quanlysinhvienlan1.MainActivity
import com.example.quanlysinhvienlan1.R
import com.example.quanlysinhvienlan1.data.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class DoQuestionActivity : AppCompatActivity() {
    private lateinit var txtQuestion: TextView
    private lateinit var rdgAnswer: RadioGroup
    private var questionIndex: Int = 0
    private var questionList: MutableList<Question> = mutableListOf()
    private lateinit var fireStore: FirebaseFirestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var imvCancelDoQuestion: ImageView
    private lateinit var txtQuantityQuestion: TextView
    private lateinit var btnConfirmNext: Button
    private var correctAnswersCount = 0
    private var totalQuestions = 0
    private lateinit var txtCountdown: TextView
    private var countdownTimer: CountDownTimer? = null
    private var countdownTime: Long = 0
    private var cloneQuestionSetID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_do_question)
        fireStore = FirebaseFirestore.getInstance()
        val bundle = intent.extras
        var questionSetID: String = ""
        if (bundle != null) {
            questionSetID = bundle.getString("QuestionSetID").toString()
            cloneQuestionSetID = questionSetID
            fireStore.collection("QuestionSet").document(questionSetID).get()
                .addOnSuccessListener { document ->
                    val inputCountdownTime = document.getLong("countDownTime")
                    if (inputCountdownTime != null) {
                        countdownTime = inputCountdownTime
                        startCountdownTimer((countdownTime * 1000) * 60)
                    }
                }
        }
        mappingViews()
        clickEvents()
        getQuestionList(questionSetID)
    }

    private fun mappingViews() {
        txtQuestion = findViewById(R.id.txt_Question)
        rdgAnswer = findViewById(R.id.rdg_Answer)
        imvCancelDoQuestion = findViewById(R.id.imv_CancelDoQuestion)
        btnConfirmNext = findViewById(R.id.btn_ConfirmNext)
        txtQuantityQuestion = findViewById(R.id.txt_QuantityQuestion)
        txtCountdown = findViewById(R.id.txt_Countdown)
    }

    private fun clickEvents() {
        imvCancelDoQuestion.setOnClickListener {
            showDialogCancel()
        }
        btnConfirmNext.setOnClickListener {
            // Kiểm tra đáp án được chọn trước khi chuyển sang câu hỏi tiếp theo
            val selectedAnswerIndex = rdgAnswer.checkedRadioButtonId
            if (selectedAnswerIndex != -1) {
                handleAnswerSelection(selectedAnswerIndex, questionList[questionIndex])
                btnConfirmNext.isEnabled = false
            } else {
                // Hiển thị thông báo nếu người dùng chưa chọn đáp án
                Toast.makeText(this, "Vui lòng chọn đáp án!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showDialogCancel()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showDialogCancel() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Kết thúc bài làm")
        dialog.setMessage("Bạn muốn kết thúc bài làm?\nNếu kết thúc, kết quả bài làm sẽ không được lưu lại!")
        dialog.setPositiveButton("Kết thúc") { _, _ ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        dialog.setNegativeButton("Hủy") { _, _ -> }
        dialog.show()
    }

    private fun getQuestionList(questionSetID: String) {
        fireStore.collection("QuestionSet").document(questionSetID).get().addOnSuccessListener {
            val questions = it.get("questions") as List<String>
            for (questionID in questions) {
                fireStore.collection("Questions").document(questionID).get()
                    .addOnSuccessListener { questionDocument ->
                        val questionName = questionDocument.getString("questionName") ?: ""
                        val correctAnswerIndex =
                            questionDocument.getLong("correctAnswerIndex")?.toInt() ?: 0
                        val options =
                            questionDocument.get("options") as? List<String> ?: emptyList()

                        // Tạo đối tượng Question và thêm vào danh sách
                        val question = Question(
                            questionID,
                            questionSetID,
                            questionName,
                            options,
                            correctAnswerIndex
                        )

                        questionList.add(question)

                        if (questionList.size == questions.size) {
                            displayQuestion(questionList[questionIndex])
                        }
                    }
            }
        }
    }

    private fun displayQuestion(question: Question) {
        // Hiển thị câu hỏi lên giao diện
        txtQuestion.text = question.questionName

        // Hiển thị số thứ tự của câu hỏi
        val currentQuestionNumber = questionIndex + 1
        totalQuestions = questionList.size
        txtQuantityQuestion.text = "$currentQuestionNumber/$totalQuestions"

        // Xóa tất cả các lựa chọn trước đó
        rdgAnswer.removeAllViews()

        // Thêm các lựa chọn vào RadioGroup
        for (i in question.options.indices) {
            val radioButton = RadioButton(this)
            radioButton.text = question.options[i]
            radioButton.id = i

            val marginParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            )
            marginParams.setMargins(
                0,
                0,
                0,
                resources.getDimensionPixelSize(R.dimen.radio_button_margin_bottom)
            )
            radioButton.layoutParams = marginParams
            radioButton.gravity = Gravity.CENTER
            radioButton.setTextColor(ContextCompat.getColor(this, R.color.black))

            // Áp dụng background và textSize
            radioButton.background = ContextCompat.getDrawable(this, R.drawable.custom_answer)
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)

            rdgAnswer.addView(radioButton)
        }
    }

    private fun handleAnswerSelection(selectedAnswerIndex: Int, question: Question) {
        // Kiểm tra xem đáp án được chọn có đúng không
        val isCorrect = selectedAnswerIndex == question.correctAnswerIndex

        // Lấy RadioButton được chọn từ RadioGroup
        val selectedRadioButton = rdgAnswer.getChildAt(selectedAnswerIndex) as? RadioButton

        if (isCorrect) {
            selectedRadioButton?.background =
                ContextCompat.getDrawable(this, R.drawable.custom_answer_correct)
            correctAnswersCount++
        } else {
            selectedRadioButton?.background =
                ContextCompat.getDrawable(this, R.drawable.custom_answer_incorrect)
            // Nếu trả lời sai, hiển thị cả câu trả lời đúng bằng cách tìm RadioButton có chỉ số là correctAnswerIndex
            val correctRadioButton =
                rdgAnswer.getChildAt(question.correctAnswerIndex) as? RadioButton
            correctRadioButton?.background =
                ContextCompat.getDrawable(this, R.drawable.custom_answer_correct)
        }

        // Sử dụng Handler để chờ 0.5 giây trước khi load câu hỏi tiếp theo
        Handler().postDelayed({
            loadNextQuestion()
        }, 200) // 500 milliseconds = 0.5 giây
    }

    private fun loadNextQuestion() {
        // Kiểm tra xem còn câu hỏi tiếp theo không
        if (questionIndex < questionList.size - 1) {
            // Nếu còn, tăng chỉ số câu hỏi lên và hiển thị câu hỏi tiếp theo
            questionIndex++
            displayQuestion(questionList[questionIndex])
            btnConfirmNext.isEnabled = true
        } else {
            showDialogResult()
        }
    }

    private fun showDialogResult() {
        val score = (correctAnswersCount.toDouble() / totalQuestions.toDouble()) * 10
        val showNumberCorrect = "$correctAnswersCount / $totalQuestions"
        val formattedScore = String.format("%.2f", score)
        val scoreWithoutComma = formattedScore.replace(",", ".")
        val showScore = "$formattedScore / 10"
        val finalScore: Double = scoreWithoutComma.toDouble()
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_show_result)

        val window = dialog.window ?: return
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        val txtNumberCorrect = dialog.findViewById<TextView>(R.id.txt_NumberCorrect)
        val txtScore = dialog.findViewById<TextView>(R.id.txt_Score)
        val btnConfirmSetTimeCountdown =
            dialog.findViewById<Button>(R.id.btn_ConfirmSetTimeCountdown)

        txtNumberCorrect.text = showNumberCorrect
        txtScore.text = showScore

        btnConfirmSetTimeCountdown.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                fireStore.collection("QuestionSet").document(cloneQuestionSetID).get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            val cloneClassroomID = it.getString("classroomID").toString()
                            val dcm = "$cloneClassroomID${generateDays()}"
                            Log.d("idAttend", dcm)
                            fireStore.collection("Attendance")
                                .document(dcm).get()
                                .addOnSuccessListener { documentSnapshot ->
                                    if (documentSnapshot.exists()) {
                                        val userScores =
                                            documentSnapshot.get("getScoreToDay") as? ArrayList<HashMap<String, Any>>

                                        if (userScores != null) {
                                            for (scoreMap in userScores) {
                                                val getScoreToDay =
                                                    scoreMap["getScoreToDay"] as? Double
                                                val memberID = scoreMap["memberID"] as? String

                                                if (getScoreToDay != null && memberID != null) {
                                                    if (memberID == currentUser.uid) {
                                                        val newScore: Double = finalScore
                                                        scoreMap["getScoreToDay"] = newScore
                                                        val update = hashMapOf<String, Any>(
                                                            "getScoreToDay" to userScores
                                                        )
                                                        fireStore.collection("Attendance")
                                                            .document(dcm)
                                                            .update(update)
                                                            .addOnSuccessListener {
                                                                Toast.makeText(
                                                                    this,
                                                                    "Đã điểm danh thành công!",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                val intent =
                                                                    Intent(
                                                                        this,
                                                                        MainActivity::class.java
                                                                    )
                                                                startActivity(intent)
                                                                finish()
                                                            }.addOnFailureListener {
                                                                Toast.makeText(
                                                                    this,
                                                                    "Điểm danh thất bại",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                    }
                                                }
                                            }
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Không tìm thấy userScore của bạn",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Không tìm thấy ",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }.addOnFailureListener {
                                    Toast.makeText(this, "Truy cập thất bại", Toast.LENGTH_SHORT)
                                        .show()
                                }
                        } else {
                            Toast.makeText(this, "Truy cập thất bại 1", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                    }

            }

        }
        dialog.show()
    }

    // Khai báo hàm để bắt đầu đếm ngược
    private fun startCountdownTimer(timeInMillis: Long) {
        countdownTimer?.cancel() // Hủy đồng hồ đếm ngược trước nếu đã tồn tại

        countdownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Cập nhật TextView để hiển thị thời gian đếm ngược
                updateCountdownText(millisUntilFinished)
            }

            override fun onFinish() {
                // Thực hiện hành động khi đếm ngược kết thúc (ví dụ: hiển thị kết quả)
                showDialogResult()
            }
        }

        // Bắt đầu đếm ngược
        countdownTimer?.start()
    }

    // Hàm để cập nhật TextView với thời gian đếm ngược mới
    private fun updateCountdownText(millisUntilFinished: Long) {
        val seconds = (millisUntilFinished / 1000).toInt() % 60
        val minutes = ((millisUntilFinished / (1000 * 60)) % 60).toInt()
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        txtCountdown.text = timeFormatted
    }

    fun generateDays(): String {
        val currentTime = Calendar.getInstance()

        val currentDay = currentTime.get(Calendar.DAY_OF_MONTH)
        val currentMonth = currentTime.get(Calendar.MONTH) + 1
        val currentYear = currentTime.get(Calendar.YEAR)

        return "$currentDay-$currentMonth-$currentYear"
    }
}
