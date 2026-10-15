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
fun tierFor(lessonIndex:Int):Tier = when(lessonIndex){
    in 0..2->Tier.RECOGNITION
    in 3..4->Tier.COMPREHENSION
    in 5..6->Tier.RECONSTRUCTION
    in 7..8->Tier.CRITICISM
    in 9..10->Tier.SYNTHESIS
    else->Tier.BOSS
}
fun unitKey(courseId:String,ui:Int,li:Int)="$courseId:$ui:$li"
fun loadCourses(context:Context):List<Course> {
    val root=JSONArray(context.assets.open("curriculum.json").bufferedReader().use { it.readText() })
    return (0 until root.length()).map { ci ->
        val c=root.getJSONObject(ci);val units=c.getJSONArray("units")
        Course(c.getString("id"),c.getString("name"),c.optString("icon","📜"),c.optString("color","#26734E"),c.optString("desc"),
            (0 until units.length()).map { ui ->
                val u=units.getJSONObject(ui);val cc=u.getJSONArray("concepts")
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

/** Distinct pedagogical goals for the twelve encounters in every chapter. */
data class LessonPlan(val title:String,val objective:String,val focus:List<Concept>,val introduction:String,val task:String,val mode:String)
private val modes=listOf("Discover","Discover","Discover","Discover","Discover","Discover",
    "Connect","Reconstruct","Criticize","Apply","Recall","Boss battle")
fun lessonPlan(course:Course,unitIndex:Int,lessonIndex:Int):LessonPlan {
    val u=course.units[unitIndex];val cc=u.concepts
    val previous=if(unitIndex>0) course.units[unitIndex-1].concepts else emptyList()
    val earlier=if(unitIndex>2) course.units[unitIndex-3].concepts else emptyList()
    val i=lessonIndex.coerceIn(0,11);val a=cc[i%cc.size];val b=cc[(i+1)%cc.size]
    val focus=when(i){
        in 0..5->listOf(a,b)
        6->listOf(cc[0],cc[3],cc[5])
        7->listOf(cc[1],cc[2],cc[4])
        8->listOf(cc[0],cc[4],cc[5])
        9->listOf(cc[2],cc[3],cc[5])
        10->(listOf(cc[0],cc[2],cc[4])+previous.take(1)+earlier.take(1)).distinctBy { it.term }
        else->cc
    }
    val title=when(i){
        in 0..5->"Discover ${a.term}"
        6->"Compare and connect"
        7->"Build the argument"
        8->"Challenge the thesis"
        9->"Transfer laboratory"
        10->"Spiral memory workshop"
        else->"The ${u.name} Trial"
    }
    val objective=when(i){
        in 0..5->"Explain ${a.term} precisely and distinguish it from ${b.term}."
        6->"Compare three ideas and identify differences that change the philosophical explanation."
        7->"Separate premises from conclusions and test the structure of reasoning."
        8->"Identify vulnerable premises and construct a charitable objection."
        9->"Apply philosophical ideas to cases outside their original historical setting."
        10->"Retrieve earlier ideas and show their relevance to the current argument."
        else->"Defend a position across four interactive philosophical combat phases."
    }
    val intro=when(i){
        in 0..5->"NEW IDEA: ${a.term}. ${a.definition}\n\nRELATED IDEA: ${b.term}. ${b.definition}\n\nDistinguishing these matters: ask what each explains and under what conditions it applies."
        6->"COMPARATIVE PROBLEM\n${cc[0].term}: ${cc[0].definition}\n\n${cc[3].term}: ${cc[3].definition}\n\n${cc[5].term}: ${cc[5].definition}\n\nAre these ideas rival explanations, or do they work at different levels?"
        7->"ARGUMENT WORKSHOP\n${u.reading.thesis.ifBlank { "This chapter presents an explanatory position about ${cc[1].term}." }}\n\nDo not assume that plausible premises make a conclusion valid. Reconstruct the reasoning, then determine whether the inferential step follows."
        8->"OBJECTION WORKSHOP\nThesis: ${u.reading.thesis.ifBlank { "The unit provides an explanation worth evaluating." }}\n\nA serious challenge: ${u.reading.objection.ifBlank { "Could a rival account explain the same phenomenon?" }}\n\nA good philosopher critiques premises, not people."
        9->"TRANSFER LAB\nSomeone encounters a case outside the original context of ${u.name}. Which notion explains it better?\n\n${cc[2].term}: ${cc[2].definition}\n${cc[3].term}: ${cc[3].definition}\n\nIdentify the assumptions you would need to apply either idea."
        10->"RETRIEVAL & SYNTHESIS\nWithout consulting the glossary, recover ${cc[0].term}, ${cc[2].term} and ${cc[4].term}.\n\n${previous.firstOrNull()?.let { "From a previous unit, reconsider ${it.term}: ${it.definition}" } ?: "Connect your earlier discoveries."}\n\n${earlier.firstOrNull()?.let { "An older challenge: ${it.term} — ${it.definition}" } ?: ""}"
        else->"BOSS PREPARATION\nYour opponent tests identification, valid inference, a principled criticism and the defensibility of revision. Knowing definitions alone is not enough."
    }
    val task=when(i){
        in 0..5->"Explain ${a.term} without using the word itself. Give a counterexample to confusing it with ${b.term}."
        6->"Create a comparison between the three major concepts."
        7->"Write P1, P2, and Conclusion. Evaluate their inferential connection."
        8->"Construct the strongest objection, not a straw man."
        9->"Apply two concepts to a novel case and defend your preference."
        10->"Explain an older concept using terms from this chapter."
        else->"Select defensible moves through four visual boss phases."
    }
    return LessonPlan(title,objective,focus,intro,task,modes[i])
}

enum class QuestionKind { IDENTIFY, EXPLAIN, TRANSFER, LOGIC, CRITIQUE, RECALL, CONTRAST, PREMISE, APPLICATION }
data class Exercise(val kind:QuestionKind,val prompt:String,val choices:List<String>,val answer:String,val feedback:String,val concept:String,val skill:String)

fun createExercises(course:Course,unitIndex:Int,lessonIndex:Int):List<Exercise>{
    val u=course.units[unitIndex];val plan=lessonPlan(course,unitIndex,lessonIndex)
    val all=course.units.flatMap { it.concepts }.distinctBy { it.term }
    val previous=(if(unitIndex>0) course.units[unitIndex-1].concepts else emptyList())+
        (if(unitIndex>2) course.units[unitIndex-3].concepts else emptyList())
    val rng=Random(course.id.hashCode()*31+unitIndex*601+lessonIndex*137)
    fun choices(answer:String,others:List<String>):List<String> {
        val pool=others.filter { it!=answer && it.isNotBlank() }.distinct().shuffled(rng).take(3)
        return (pool+answer).shuffled(rng)
    }
    fun concept(i:Int)=plan.focus[i%plan.focus.size]
    val prior=if(previous.isNotEmpty())previous[(unitIndex*5+lessonIndex)%previous.size] else u.concepts[(lessonIndex+4)%u.concepts.size]
    val a=concept(0);val b=concept(1)
    val out=mutableListOf<Exercise>()
    out+=Exercise(QuestionKind.IDENTIFY,"DISCOVERY • Which concept is described here?\n\n${a.definition}",
        choices(a.term,all.map { it.term }),a.term,"${a.term}: ${a.definition}",a.term,"recognition")
    out+=Exercise(QuestionKind.EXPLAIN,"INTERPRET • Which explanation correctly describes ${b.term}?",
        choices(b.definition,all.map { it.definition }),b.definition,"${b.term}: ${b.definition}",b.term,"comprehension")
    val correctPair="${a.term}: ${a.definition}\n${b.term}: ${b.definition}"
    out+=Exercise(QuestionKind.CONTRAST,"CONCEPT MAP • Which interpretation keeps the two notions distinct?",
        choices(correctPair,listOf("${a.term}: ${b.definition}\n${b.term}: ${a.definition}",
            "These terms must mean the same thing in all contexts.","Neither term has any explanatory role.")),correctPair,
        "A valid comparison respects the definition and explanatory role of each concept.",a.term,"comprehension")
    val applied=concept(2)
    out+=Exercise(QuestionKind.APPLICATION,"TRANSFER • A scholar offers this account:\n\n${applied.definition}\n\nWhat concept does it invoke?",
        choices(applied.term,all.map { it.term }),applied.term,"${applied.term} is the fitting term, but applications still require reasons.",applied.term,"synthesis")
    val forms=listOf(
        Triple("If P then Q. P. Therefore Q.","Valid","Modus ponens."),
        Triple("If P then Q. Not Q. Therefore not P.","Valid","Modus tollens."),
        Triple("If P then Q. If Q then R. Therefore if P then R.","Valid","Hypothetical syllogism."),
        Triple("P or Q. Not P. Therefore Q.","Valid","Disjunctive syllogism."),
        Triple("If P then Q. Q. Therefore P.","Invalid","Affirming the consequent."),
        Triple("If P then Q. Not P. Therefore not Q.","Invalid","Denying the antecedent."),
        Triple("All A are B. All B are C. Therefore all A are C.","Valid","Categorical transitivity."),
        Triple("Some A are B. Some B are C. Therefore some A are C.","Invalid","The witnesses need not overlap.")
    )
    val form=forms[(lessonIndex+unitIndex*3)%forms.size]
    out+=Exercise(QuestionKind.LOGIC,"ARGUMENT FORGE • Does the conclusion necessarily follow?\n\n${form.first}",
        listOf("Valid","Invalid").shuffled(rng),form.second,form.third,a.term,"reconstruction")
    val thesis=u.reading.thesis.ifBlank { "The account of ${u.name} is defensible." }
    val objection=u.reading.objection.ifBlank { "Could a rival account explain the same phenomenon?" }
    val moves=listOf("Test this specific objection: $objection","Reject the view solely because of the author's identity.",
        "Restate the conclusion without new support.","Assume the rival view false before examining it.")
    out+=Exercise(QuestionKind.CRITIQUE,"CRITICAL DUEL • Against this claim, which move offers a serious philosophical challenge?\n\n$thesis",
        moves.shuffled(rng),moves[0],"A strong challenge addresses an inference or premise: $objection",b.term,"criticism")
    out+=Exercise(QuestionKind.RECALL,"SPIRAL REVIEW • You encountered this idea previously:\n\n${prior.definition}\n\nIdentify it without reopening its lesson.",
        choices(prior.term,all.map { it.term }),prior.term,"Retrieval strengthens memory of ${prior.term}: ${prior.definition}",prior.term,"retention")
    val x=concept(2)
    val premises=listOf("${x.term}: ${x.definition}","An idea is true just because its author was influential.",
        "A conclusion needs no supporting reasons if it sounds persuasive.","All positions are true merely because they are possible.")
    out+=Exercise(QuestionKind.PREMISE,"RECONSTRUCTION • Which statement correctly describes ${x.term} and can be investigated as a premise?",
        premises.shuffled(rng),premises[0],"A correctly associated proposition can serve as a premise; valid inference and truth must then be assessed separately.",x.term,"reconstruction")
    return if(lessonIndex<6)out else out.shuffled(rng)
}

class SophiaProgress(context:Context) {
    private val prefs:SharedPreferences=context.getSharedPreferences("sophia_native_v4",Context.MODE_PRIVATE)
    val xp get()=prefs.getInt("xp",0)
    val coins get()=prefs.getInt("coins",0)
    val streak get()=prefs.getInt("streak",0)
    val completed:Set<String> get()=prefs.getStringSet("completed", emptySet())?.toSet()?:emptySet()
    val todayLessons:Int get()=if(prefs.getString("dailyDate","")==SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date()))prefs.getInt("dailyLessons",0) else 0
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
        val priorDaily=if(prefs.getString("dailyDate","")==today)prefs.getInt("dailyLessons",0) else 0
        val daily=priorDaily+(if(already)0 else 1)
        val earned=if(already)score*2 else score*10+30
        val rewards=if(already)0 else 5+score*2
        val badgeSet=badges.toMutableSet()
        if(finished.size>=1)badgeSet.add("First Steps")
        if(finished.size>=10)badgeSet.add("Apprentice")
        if(finished.size>=100)badgeSet.add("Scholar")
        if(finished.size>=500)badgeSet.add("Philosopher")
        prefs.edit().putStringSet("completed",finished).putStringSet("badges",badgeSet)
            .putInt("xp",xp+earned).putInt("coins",coins+rewards)
            .putInt("streak",nextStreak).putString("lastDay",today)
            .putString("dailyDate",today).putInt("dailyLessons",daily).apply()
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