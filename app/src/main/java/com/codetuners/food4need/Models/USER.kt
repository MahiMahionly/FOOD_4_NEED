package com.codetuners.food4need.Models

data class USER(
    val Image: String,
    val Name: String,
    val role:String,
    val hotel_organ_name:String,
    val phone: String,
    val email: String,
    val UserID: String,
) {
    constructor() : this("", "", "", "", "","","")
}