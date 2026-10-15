package com.sophia.nativeapp

import android.content.Context
import org.json.JSONArray

data class SeminarChapter8(val name:String,val kind:String,val body:String)
data class SeminarProgram8(val id:String,val type:String,val title:String,val authors:String,val question:String,
    val tags:List<String>,val sources:String,val chapters:List<SeminarChapter8>,val related:String){
    val isSeminar get()=type=="seminar"
    val words get()=chapters.sumOf{it.body.split(Regex("\\s+")).size}
    val estimatedMinutes get()=(words/125).coerceAtLeast(1)
}
object AudioCatalog8 {
    fun load(context:Context):List<SeminarProgram8> = try {
        val root=JSONArray(context.assets.open("audio_programs.json").bufferedReader().use{it.readText()})
        (0 until root.length()).map {i->
            val o=root.getJSONObject(i);val cc=o.getJSONArray("chapters");val tags=o.getJSONArray("tags")
            SeminarProgram8(o.getString("id"),o.getString("type"),o.getString("title"),o.getString("authors"),
                o.getString("question"),(0 until tags.length()).map{tags.getString(it)},o.getString("sources"),
                (0 until cc.length()).map{ j-> val c=cc.getJSONObject(j);SeminarChapter8(c.getString("name"),c.getString("kind"),c.getString("body"))},o.optString("related"))
        }
    }catch(_:Exception){emptyList()}
}