package com.sophia.nativeapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipFile

data class SeminarChapter8(val name:String,val kind:String,val body:String)
data class SeminarProgram8(val id:String,val type:String,val title:String,val authors:String,val question:String,
    val tags:List<String>,val sources:String,val chapters:List<SeminarChapter8>,val related:String,
    val wordCount:Int=0,val chapterCount:Int=0,val bundle:Int=0){
    val isSeminar get()=type=="seminar"
    val isLecture get()=type=="lecture"
    val isTextSeminar get()=type=="text-seminar"
    val words get()=if(wordCount>0)wordCount else chapters.sumOf{it.body.split(Regex("\\s+")).size}
    val estimatedMinutes get()=(words+124)/125
    val chaptersTotal get()=if(chapterCount>0)chapterCount else chapters.size
}
object AudioCatalog8 {
    @Volatile private var cache:List<SeminarProgram8>?=null
    private fun unpack(o:JSONObject,cc:List<SeminarChapter8>):SeminarProgram8{
        val t=o.optJSONArray("tags")?:JSONArray()
        return SeminarProgram8(o.optString("id"),o.optString("type"),o.optString("title"),o.optString("authors"),
            o.optString("question"),(0 until t.length()).map{t.optString(it)},o.optString("sources"),
            cc,o.optString("related"),o.optInt("wordCount",0),o.optInt("chapterCount",cc.size),o.optInt("bundle",0))
    }
    fun load(context:Context):List<SeminarProgram8>{
        cache?.let{return it}
        return synchronized(this){
            cache?.let{return@synchronized it}
            try{
                val r=JSONArray(context.assets.open("audio_index_v9.json").bufferedReader().use{it.readText()})
                val c=(0 until r.length()).map{unpack(r.getJSONObject(it),emptyList())}
                cache=c;c
            }catch(_:Exception){emptyList()}
        }
    }
    private fun localBundle(context:Context,bundle:Int):File{
        require(bundle in 0..30)
        val name="bundle_"+bundle.toString().padStart(2,'0')+".zip"
        val dir=File(context.cacheDir,"sophia_audio9").apply{mkdirs()}
        val f=File(dir,name)
        if(!f.exists()||f.length()<1024L){
            val partial=File(dir,name+".partial")
            context.assets.open("audio9/"+name).use{input->partial.outputStream().use{input.copyTo(it)}}
            if(!partial.renameTo(f)){partial.copyTo(f,true);partial.delete()}
        }
        return f
    }
    fun loadProgram(context:Context,id:String):SeminarProgram8?{
        if(!Regex("[A-Za-z0-9_-]{1,90}").matches(id))return null
        val meta=load(context).firstOrNull{it.id==id}?:return null
        return try{
            ZipFile(localBundle(context,meta.bundle)).use{zip->
                val entry=zip.getEntry(id+".json")?:return null
                val r=JSONObject(zip.getInputStream(entry).bufferedReader().use{it.readText()})
                val a=r.getJSONArray("chapters")
                val cc=(0 until a.length()).map{n->val c=a.getJSONObject(n)
                    SeminarChapter8(c.optString("name"),c.optString("kind"),c.getString("body"))}
                unpack(r,cc)
            }
        }catch(_:Exception){null}
    }
}
