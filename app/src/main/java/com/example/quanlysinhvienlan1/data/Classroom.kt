package com.example.quanlysinhvienlan1.data

class Classroom(
    var idClassroom: String,
    var nameClass: String,
    var teacher: String,
    var membersQuantity: Int,
    var members: List<User> = emptyList()
)