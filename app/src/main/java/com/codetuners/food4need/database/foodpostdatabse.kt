package com.codetuners.food4need.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [postEntity::class],version = 1,exportSchema = false)
abstract  class foodpostdatabse:RoomDatabase() {
    abstract fun foodDAO():foodDao
}