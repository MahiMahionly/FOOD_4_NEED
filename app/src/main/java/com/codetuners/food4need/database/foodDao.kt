package com.codetuners.food4need.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface foodDao {

    @Insert
    fun insertpost(postEntity: postEntity)

    @Delete
    fun deletepost(postEntity: postEntity)

    @Query("SELECT * FROM fav_posts")
    fun getallfavposts(): List<postEntity>

    @Query("SELECT * FROM fav_posts WHERE pid =:PID")
    fun getpostbyId(PID: String):postEntity

}