package com.codetuners.food4need.Models

data class POST(
    var PID: String,
    var PDescription: String,
    var PImage: String,
    var PLocation: String,
    var PMobileNumber: String,
    var PTitle: String,
    var PostedTime: String,
    var UserDP: String,
    var UserEmail: String,
    var UserID: String,
    var UserName: String
) {
    constructor():this(
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "")
}
