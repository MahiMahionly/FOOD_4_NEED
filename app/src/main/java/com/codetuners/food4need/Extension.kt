package com.codetuners.food4need

import android.content.Context
import android.widget.Toast

fun Context.MahiShortToast(message:String){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.MahiLongToast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}