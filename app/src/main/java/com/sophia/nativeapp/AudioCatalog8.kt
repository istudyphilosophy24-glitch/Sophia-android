package com.sophia.nativeapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipFile

/** Indexed audio-ready listening library. Big episode bodies are loaded only on selection,
 * keeping hundreds of 30-minute primary-text seminars practical on Android devices.
 * Word-count estimates are based on the actual spoken text, not arbitrary duration labels.
 */
data class SeminarChapter8(val name:String,val kind:String,val body:String)
data class SeminarProgram8(
    val id:String,val type:String,val title:String,val authors:String,val question:String,
    val tags:List<String>,val sources:String,val chapters:List<SeminarChapter8>,val related:String,
    val wordCount:Int=0,val chapterCount:Int=0,val bundle:Int=0
){
    val isSeminar get()=type=="seminar"
    val isLecture get()=type=="lecture"
    val isTextSeminar get()=type=="text-seminar"
    val words get()=if(wordCount>0)wordCount else chapters.sumOf{c->c.body.split(Regex("\\s+")).size}
    val estimatedMinutes get()=(words+124)/125
    val chaptersTotal get()=if(chapterCount>0)chapterCount else chapters.size
}

object AudioCatalog8 {
    @Volatile private var cache:List<SeminarProgram8>?=null
    private fun tags(o:JSONObject):List<String>{
        val t=o.optJSONArray("tags")?:JSONArray()
        return (0 until t.length()).map{t.optString(it)}
    }
    private fun metadata(o:JSONObject,chapters:List<SeminarChapter8>):SeminarProgram8 = SeminarProgram8(
        o.optString("id"),o.optString("type"),o.optString("title"),o.optString("authors"),
        o.optString("question"),tags(o),o.optString("sources"),chapters,o.optString("related"),
        o.optInt("wordCount",0),o.optInt("chapterCount",chapters.size),o.optInt("bundle",0)
    )
    fun load(context:Context):List<SeminarProgram8>{
        cache?.let{return it}
        return synchronized(this){
            cache?.let{return@synchronized it}
            try{
                val raw=context.assets.open("audio_index_v9.json").bufferedReader().use{it.readText()}
                val items=JSONArray(raw)
                val loaded=(0 until items.length()).map{i->metadata(items.getJSONObject(i),emptyList())}
                cache=loaded;loaded
            }catch(_:Exception){emptyList()}
        }
    }
    private fun bundleFile(context:Context,bundle:Int):File{
        require(bundle in 0..30)
        val name="bundle_${bundle.toString().padStart(2,'0')}.zip"
        val folder=File(context.cacheDir,"sophia_audio9").apply{mkdirs()}
        val file=File(folder,name)
        if(!file.exists() || file.length()<1024L){
            val temp=File(folder,name+".partial")
            context.assets.open("audio9/$name").use{input->temp.outputStream().use{out->input.copyTo(out)}}
            if(!temp.renameTo(file)){
                temp.copyTo(file,overwrite=true);temp.delete()
            }
        }
        return file
    }
    fun loadProgram(context:Context,id:String):SeminarProgram8?{
        if(!Regex("[A-Za-z0-9_-]{1,90}").matches(id))return null
        val info=load(context).firstOrNull{it.id==id}?:return null
        return try{
            ZipFile(bundleFile(context,info.bundle)).use{zip->
                val entry=zip.getEntry("$id.json")?:return null
                val raw=zip.getInputStream(entry).bufferedReader(Charsets.UTF_8).use{it.readText()}
                val o=JSONObject(raw);val a=o.getJSONArray("chapters")
                val chapters=(0 until a.length()).map{i->val c=a.getJSONObject(i)
                    SeminarChapter8(c.getString("name"),c.optString("kind"),c.getString("body"))}
                metadata(o,chapters)
            }
        }catch(_:Exception){null}
    }
}