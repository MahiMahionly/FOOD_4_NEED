package com.codetuners.food4need.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fav_posts")
data class postEntity(
    @PrimaryKey val pid: String,
    @ColumnInfo(name = "P_Description") val pdesc: String,
    @ColumnInfo(name = "P_Image") val pImage: String,
    @ColumnInfo(name = "P_Location") val pLoc: String,
    @ColumnInfo(name = "P_Mobile") val pMob: String,
    @ColumnInfo(name = "P_Title") val pTitle: String,
    @ColumnInfo(name = "P_Time") val postedtime: String,
    @ColumnInfo(name = "User_DP") val userdap: String,
    @ColumnInfo(name = "User_Email") val useremail: String,
    @ColumnInfo(name = "User_ID") val userid: String,
    @ColumnInfo(name = "User_Name") val username: String
)
