package com.example.codeforcealarmer

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import kotlin.IllegalArgumentException

class JsonContestParser(private val input: String) {
    enum class Status{
        OK, FAILED, NOT_PARSED
    }

    var status : Status = Status.NOT_PARSED
        private set
    private var comment : String? = null
    var contests : MutableList<Contest>? = null
        private set


    suspend fun parse(): Unit = withContext(Dispatchers.Default){
        try {
            val rootJson = JSONObject(input)
            status = when(rootJson.getString("status")){
                "OK" -> Status.OK
                "FAILED" -> Status.FAILED
                else -> throw JSONException("Invalid status returned")
            }

            if (status == Status.FAILED){
                comment = rootJson.getString("comment")
            }else{
                contests = mutableListOf()
                val result = rootJson.getJSONArray("result")
                for (i in 0 until result.length()){
                    val entry = result.getJSONObject(i)
                    val id = entry.getInt("id")
                    val name = entry.getString("name")
                    val phase = Phase.fromStr(entry.getString("phase"))
                    val durationSecs = entry.getLong("durationSeconds")
                    val startTimeSecs = getLongOrNull("startTimeSeconds", entry)

                    contests?.add(Contest.makeContest(id, name, phase, durationSecs, startTimeSecs))
                }
            }
        }catch (e : JSONException){
            Log.e(this::class.java.simpleName, e.toString())
            throw IllegalArgumentException()
        }
    }

    private fun getLongOrNull(name: String, jsonObject: JSONObject) =
        try{
            jsonObject.getLong(name)
        }catch (e : JSONException){
            null
        }
}