package com.sophia.nativeapp

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min
import kotlin.random.Random

/** A faithful migration of the 28-course, 280-unit SOPHIA 4 curriculum. */
data class Concept(val term:String, val definition:String)
data class Reading(val foundation:String, val intermediate:String, val advanced:String, val thesis:String, val objection:String, val source:String)
data class UnitLesson(val name:String, val lecture:String, val concepts:List<Concept>, val reading:Reading)
data class Course(val id:String, val name:String, val icon:String, val color:String, val description:String, val units:List<UnitLesson>)

enum class Tier(val label:String) {
    RECOGNITION("Recognition"), COMPREHENSION("Comprehension"), RECONSTRUCTION("Reconstruction"),
    CRITICISM("Criticism"), SYNTHESIS("Synthesis"), BOSS("Mastery Trial")
}
fun tierFor(lessonIndex:Int): Tier = when (lessonIndex) {
    in 0..2 -> Tier.RECOGNITION
    in 3..4 -> Tier.COMPREHENSION
    in 5..6 -> Tier.RECONSTRUCTION
    in 7..8 -> Tier.CRITICISM
    in 9..10 -> Tier.SYNTHESIS
    else -> Tier.BOSS
}
fun unitKey(courseId:String,ui:Int,li:Int)="$courseId:$ui:$li"
fun loadCourses(context:Context):List<Course> {
    val root=JSONArray(context.assets.open("curriculum.json").bufferedReader().use { it.readText() })
    return (0 until root.length()).map { ci ->
        val c=root.getJSONObject(ci)
        val units=c.getJSONArray("units")
        Course(c.getString("id"),c.getString("name"),c.optString("icon","📜"),c.optString("color","#26734E"),c.optString("desc"),
            (0 until units.length()).map { ui ->
                val u=units.getJSONObject(ui)
                val cc=u.getJSONArray("concepts")
                val concepts=(0 until cc.length()).map { i ->
                    val v=cc.getJSONObject(i);Concept(v.getString("term"),v.getString("definition"))
                }
                val r=u.optJSONObject("reading") ?: JSONObject()
                val primary=concepts.joinToString(" ") { "${it.term}: ${it.definition}." }
                val foundation=r.optString("foundation").ifBlank { "This unit investigates ${u.getString("name")}. $primary" }
                val intermediate=r.optString("intermediate").ifBlank { "Distinguish ${concepts[0].term} from ${concepts[1].term}. Explain what philosophical problem the distinction addresses and whether the ideas are compatible." }
                val advanced=r.optString("advanced").ifBlank { "Reconstruct the unit's position as explicit premises and a conclusion. Challenge its weakest premise with a relevant counterexample." }
                val lecture=u.optString("lecture").ifBlank { "$foundation $intermediate $advanced" }
                val reading=Reading(foundation,intermediate,advanced,r.optString("thesis"),r.optString("objection"),r.optString("source"))
                UnitLesson(u.getString("name"),lecture,concepts,reading)
            })
    }
}

/** Question types deliberately differ, rather than presenting eight synonyms of one idea. */
enum class QuestionKind { IDENTIFY, EXPLAIN, TRANSFER, LOGIC, CRITIQUE, RECALL }
data class Exercise(
    val kind:QuestionKind,val prompt:String,val choices:List<String>,val answer:String,
    val feedback:String,val concept:String,val skill:String
)
fun createExercises(course:Course,unitIndex:Int,lessonIndex:Int):List<Exercise> {
    val u=course.units[unitIndex]
    val concepts=u.concepts
    val previous=if(unitIndex>0)course.units[unitIndex-1].concepts else emptyList()
    val distant=if(unitIndex>2)course.units[unitIndex-3].concepts else emptyList()
    val all=course.units.flatMap { it.concepts }.distinctBy { it.term }
    val rng=Random(1009*unitIndex+131*lessonIndex+course.id.hashCode())
    fun opts(answer:String,others:List<String>):List<String> = (others.filter { it!=answer }.distinct().shuffled(rng).take(3)+answer).distinct().shuffled(rng)
    fun target(i:Int): Concept=concepts[(lessonIndex+i)%concepts.size]
    val questions=ArrayList<Exercise>()
    for (n in 0..2) {
        val c=target(n)
        questions+=Exercise(QuestionKind.IDENTIFY,"Which concept corresponds to this explanation?\n\n${c.definition}",
            opts(c.term,all.map { it.term }),c.term,"${c.term} — ${c.definition}",c.term,"recognition")
    }
    for (n in 0..1) {
        val c=target(n+3)
        questions+=Exercise(QuestionKind.EXPLAIN,"Which explanation is accurate for ${c.term}?",
            opts(c.definition,all.map { it.definition }),c.definition,"${c.term}: ${c.definition}",c.term,"comprehension")
    }
    val other=target(1);val main=target(0)
    questions+=Exercise(QuestionKind.TRANSFER,"Which pair of ideas should you distinguish when interpreting ${u.name}?",
        opts("${main.term} / ${other.term}",listOf("${all.last().term} / ${all.first().term}","Cause / popularity","Author / publication year")),
        "${main.term} / ${other.term}","Distinguish ${main.term}: ${main.definition} from ${other.term}: ${other.definition}. Their mere association does not prove equivalence.",main.term,"synthesis")
    val valid=lessonIndex%2==0
    val inference=if(valid)"If P then Q. P. Therefore Q." else "If P then Q. Q. Therefore P."
    questions+=Exercise(QuestionKind.LOGIC,"Evaluate this inference without judging whether P or Q is true:\n\n$inference",
        listOf("Valid deductive form","Invalid deductive form").shuffled(rng),
        if(valid)"Valid deductive form" else "Invalid deductive form",
        if(valid)"This is modus ponens, a truth-preserving inferential form." else "This affirms the consequent: Q could obtain for some reason other than P.",main.term,"reconstruction")
    val critiques=listOf(
        "Identify an unsupported premise and supply a relevant counterexample",
        "Attack the author's character without addressing the premises",
        "Repeat the conclusion in different words",
        "Assume a rival view is false without giving reasons"
    )
    questions+=Exercise(QuestionKind.CRITIQUE,"Which approach constitutes a serious philosophical criticism of the unit's thesis?",
        critiques.shuffled(rng),critiques[0],"A strong objection identifies a specific inferential gap or premise rather than attacking a person or repeating a claim.",other.term,"criticism")
    // Recall is integrated into one of the initial identification items on later units.
    if(previous.isNotEmpty()) {
        val remembered=(previous+distant).getOrNull(lessonIndex%(previous.size+distant.size)) ?: previous.first()
        questions[2]=Exercise(QuestionKind.RECALL,"SPACED REVIEW · From an earlier unit:\n\n${remembered.definition}",
            opts(remembered.term,all.map { it.term }),remembered.term,
            "Earlier concept: ${remembered.term} — ${remembered.definition}",remembered.term,"retention")
    }
    return questions
}

class SophiaProgress(context:Context) {
    private val prefs:SharedPreferences=context.getSharedPreferences("sophia_native_v4",Context.MODE_PRIVATE)
    val xp get()=prefs.getInt("xp",0)
    val coins get()=prefs.getInt("coins",0)
    val streak get()=prefs.getInt("streak",0)
    val completed:Set<String> get()=prefs.getStringSet("completed", emptySet())?.toSet()?:emptySet()
    val badges:Set<String> get()=prefs.getStringSet("badges", emptySet())?.toSet()?:emptySet()
    fun isCompleted(key:String)=key in completed
    fun research():String=prefs.getString("research","")?:""
    fun saveResearch(value:String)=prefs.edit().putString("research",value).apply()
    fun seminarResponse(course:String,unit:Int,stage:Int)=prefs.getString("seminar:$course:$unit:$stage","")?:""
    fun saveSeminar(course:String,unit:Int,stage:Int,value:String)=prefs.edit().putString("seminar:$course:$unit:$stage",value).apply()
    fun annotation(course:String,unit:Int)=prefs.getString("annotation:$course:$unit","")?:""
    fun saveAnnotation(course:String,unit:Int,value:String)=prefs.edit().putString("annotation:$course:$unit",value).apply()
    fun attempts(concept:String,skill:String)=prefs.getInt("try:$skill:$concept",0)
    fun correct(concept:String,skill:String)=prefs.getInt("right:$skill:$concept",0)
    fun skillPercent(skill:String):Int {
        val total=prefs.all.filterKeys { it.startsWith("try:$skill:") }.values.sumOf { (it as? Int)?:0 }
        val right=prefs.all.filterKeys { it.startsWith("right:$skill:") }.values.sumOf { (it as? Int)?:0 }
        return if(total==0)0 else (right*100/total).coerceIn(0,100)
    }
    fun dueConcepts(limit:Int=30):List<String> {
        val now=System.currentTimeMillis()
        return prefs.all.keys.filter { it.startsWith("due:") && (prefs.getLong(it,Long.MAX_VALUE)<=now) }.map { it.removePrefix("due:") }.take(limit)
    }
    fun recordAnswer(ex:Exercise,ok:Boolean) {
        val a=attempts(ex.concept,ex.skill)
        val right=correct(ex.concept,ex.skill)
        val next=if(ok)min(30, if(a==0)1 else 1+right*2) else 1
        val now=System.currentTimeMillis()
        prefs.edit().putInt("try:${ex.skill}:${ex.concept}",a+1)
            .putInt("right:${ex.skill}:${ex.concept}",right+if(ok)1 else 0)
            .putLong("due:${ex.concept}",now+next*86_400_000L).apply()
    }
    fun recordReflection(concept:String) {
        val a=attempts(concept,"synthesis")
        prefs.edit().putInt("try:synthesis:$concept",a+1).apply()
    }
    fun finishLesson(courseId:String,unit:Int,lesson:Int,score:Int,passed:Boolean):Boolean {
        if(!passed)return false
        val key=unitKey(courseId,unit,lesson)
        val already=isCompleted(key)
        val fmt=SimpleDateFormat("yyyy-MM-dd",Locale.US)
        val today=fmt.format(Date())
        val last=prefs.getString("lastDay","")?:""
        val yesterday=Calendar.getInstance().apply {add(Calendar.DATE,-1)}
        val nextStreak=if(last==today)streak else if(last==fmt.format(yesterday.time))streak+1 else 1
        val finished=completed+key
        val earned=if(already)score*2 else score*10+30
        val rewards=if(already)0 else 5+score*2
        val badgeSet=badges.toMutableSet()
        if(finished.size>=1)badgeSet.add("First Steps")
        if(finished.size>=10)badgeSet.add("Apprentice")
        if(finished.size>=100)badgeSet.add("Scholar")
        if(finished.size>=500)badgeSet.add("Philosopher")
        prefs.edit().putStringSet("completed",finished).putStringSet("badges",badgeSet)
            .putInt("xp",xp+earned).putInt("coins",coins+rewards)
            .putInt("streak",nextStreak).putString("lastDay",today).apply()
        return true
    }
    fun exportBackup():String {
        val objectData=JSONObject()
        prefs.all.forEach { (k,v) ->
            when(v) {
                is String -> objectData.put(k,v)
                is Int -> objectData.put(k,v)
                is Long -> objectData.put(k,v)
                is Boolean -> objectData.put(k,v)
                is Set<*> -> objectData.put(k,JSONArray(v.toList()))
            }
        }
        return JSONObject().put("format","SOPHIA-NATIVE-4").put("created",System.currentTimeMillis()).put("records",objectData).toString()
    }
    fun importBackup(json:String):Boolean = try {
        val root=JSONObject(json)
        if(root.optString("format")!="SOPHIA-NATIVE-4")false else {
            val data=root.getJSONObject("records")
            val edit=prefs.edit()
            for(k in data.keys()){
                when(val value=data.get(k)) {
                    is Int->edit.putInt(k,value)
                    is Long->edit.putLong(k,value)
                    is String->edit.putString(k,value)
                    is Boolean->edit.putBoolean(k,value)
                    is JSONArray->edit.putStringSet(k,(0 until value.length()).map { value.getString(it) }.toSet())
                }
            }
            edit.apply();true
        }
    }catch (_:Exception){false}
}