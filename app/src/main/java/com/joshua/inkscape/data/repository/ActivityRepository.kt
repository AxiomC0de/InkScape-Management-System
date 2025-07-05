package com.joshua.inkscape.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.joshua.inkscape.data.model.Activity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ActivityRepository {
    private val database = FirebaseDatabase.getInstance().getReference("activities")

    fun addActivity(activity: Activity) {
        val key = database.push().key
        if (key != null) {
            database.child(key).setValue(activity.copy(id = key))
        }
    }

    fun getRecentActivities(): Flow<List<Activity>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activities = snapshot.children.mapNotNull { it.getValue(Activity::class.java) }
                trySend(activities.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }
} 