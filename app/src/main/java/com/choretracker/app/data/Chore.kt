package com.choretracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chores")
data class Chore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String
)

@Entity(
    tableName = "completions",
    indices = [androidx.room.Index(value = ["choreName", "completedDate"])]
)
data class ChoreCompletion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val choreName: String,
    val category: String,
    val completedDate: Long
)
