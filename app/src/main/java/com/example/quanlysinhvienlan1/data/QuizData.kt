package com.example.quanlysinhvienlan1.data

data class QuestionSet(
    val setId: String,
    val classroomID: String,
    val setName: String,
    val questions: List<Question> = emptyList(),
    val status: Int = 0,
    val countDownTime: Int = 0
)

data class Question(
    val questionID: String,
    val questionSetID: String,
    val questionName: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)