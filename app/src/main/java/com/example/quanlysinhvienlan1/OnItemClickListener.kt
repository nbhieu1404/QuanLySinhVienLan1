package com.example.quanlysinhvienlan1

interface OnItemClickListener {
    fun onItemClick(position: Int)
    abstract fun onUpdateButtonClick(position: Int)
    abstract fun onDeleteButtonClick(position: Int)
    abstract fun onStartQuestionSetClick(position: Int)
    abstract fun onStopQuestionSetClick(position: Int)
}