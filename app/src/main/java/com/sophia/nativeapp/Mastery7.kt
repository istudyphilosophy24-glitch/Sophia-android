package com.sophia.nativeapp

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/** A transparent estimate, not psychometrically validated. Evidence is separated by skill.
 * Old v6 completion data is retained in the same SharedPreferences store.
 */
class Mastery7(context:Context){
    private val db:SharedPreferences=context.getSharedPreferences("sophia_native_v4",Context.MODE_PRIVATE)
    val skills=listOf("recognition","comprehension","reconstruction","criticism","synthesis","retention")
    private fun safe(value:String)=value.replace("|","/")
    private fun prefix(concept:String,skill:String)="m7:${safe(skill)}:${safe(concept)}"
    fun trials(concept:String,skill:String)=db.getInt(prefix(concept,skill)+":n",0)
    fun estimate(concept:String,skill:String):Int=db.getInt(prefix(concept,skill)+":score",0)
    fun record(concept:String,skill:String,correct:Boolean,confidence:Int=2){
        val base=prefix(concept,skill)
        val n=trials(concept,skill)
        val prior=if(n==0)50.0 else estimate(concept,skill).toDouble()
        val response=if(correct) (if(confidence<=1)75.0 else if(confidence>=3)100.0 else 90.0) else (if(confidence>=3)0.0 else 25.0)
        val weight=if(n<3)0.32 else 0.18
        val updated=((1-weight)*prior+weight*response).roundToInt().coerceIn(0,100)
        // Increasing intervals for correct recall; errors reset the interval.
        val oldInterval=db.getInt(base+":interval",0)
        val days=if(correct)when {oldInterval==0->1;oldInterval==1->3;else->(oldInterval*2).coerceAtMost(45)} else 1
        val due=System.currentTimeMillis()+days*86_400_000L
        db.edit().putInt(base+":n",n+1).putInt(base+":score",updated)
            .putInt(base+":interval",days).putLong(base+":due",due)
            .putInt(base+":lastConfidence",confidence.coerceIn(1,3))
            .putLong(base+":last",System.currentTimeMillis()).apply()
    }
    fun summary(skill:String):Pair<Int,Int>{
        val values=db.all.filterKeys{it.startsWith("m7:$skill:")&&it.endsWith(":score")}
            .values.mapNotNull{it as? Int}
        return Pair(if(values.isEmpty())0 else values.average().roundToInt(),values.size)
    }
    fun dueConcepts():List<String>{
        val now=System.currentTimeMillis()
        return db.all.filter{(k,v)->k.startsWith("m7:")&&k.endsWith(":due")&&v is Long&&v<=now}
            .keys.map{it.removePrefix("m7:").substringAfter(':').removeSuffix(":due")}.distinct().take(60)
    }
    fun difficultConcepts(limit:Int=10):List<String>{
        return db.all.filterKeys{it.startsWith("m7:")&&it.endsWith(":score")}
            .mapNotNull{(k,v)->(v as? Int)?.let{Pair(k.removePrefix("m7:").substringAfter(':').removeSuffix(":score"),it)}}
            .groupBy{it.first}.map{(name,values)->Pair(name,values.map{it.second}.average())}
            .sortedBy{it.second}.take(limit).map{it.first}
    }
    fun evidenceCount():Int=db.all.keys.count{it.startsWith("m7:")&&it.endsWith(":n")}
    fun totalAttempts():Int=db.all.filterKeys{it.startsWith("m7:")&&it.endsWith(":n")}.values.sumOf{(it as? Int)?:0}
    fun recommend():String=skills.minByOrNull{summary(it).first.takeIf{n->summary(it).second>0}?:100}?:skills[0]
}

@Composable fun MasteryDashboard7(mastery:Mastery7,completed:Int){
    val green=Color(0xFF1B634B);val subtitle=Color(0xFF67776B)
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(13.dp)){
        Text("THE DIALECTICAL MASTERY ENGINE",color=Color(0xFFBA8B41),fontWeight=FontWeight.ExtraBold,fontSize=11.sp)
        Text("What can you actually do?",fontSize=23.sp,fontWeight=FontWeight.Bold,color=green)
        Text("Completion: $completed lessons  ·  ${mastery.totalAttempts()} recorded mastery observations",color=subtitle,fontSize=13.sp)
        mastery.skills.forEach{skill->
            val (score,n)=mastery.summary(skill)
            Column(verticalArrangement=Arrangement.spacedBy(5.dp)){
                Row(Modifier.fillMaxWidth()){
                    Text(skill.replaceFirstChar{it.uppercase()},color=green,fontWeight=FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Text(if(n==0)"Not assessed" else "$score% · $n concepts",color=subtitle,fontSize=12.sp)
                }
                LinearProgressIndicator(progress=if(n==0)0f else score/100f,modifier=Modifier.fillMaxWidth().height(10.dp),color=Color(0xFF47A978),trackColor=Color(0xFFE8ECE8))
            }
        }
        Text("Suggested focus: ${mastery.recommend().replaceFirstChar{it.uppercase()}}",color=green,fontWeight=FontWeight.Bold)
        val difficult=mastery.difficultConcepts(5)
        if(difficult.isNotEmpty()) Text("Concepts to revisit: ${difficult.joinToString(" • ")}",color=subtitle,fontSize=13.sp)
        Text("Scores are transparent heuristics based on responses and confidence, not verified measures of philosophical competence. Written self-ratings do not prove correctness.",color=subtitle,fontSize=12.sp)
    }
}

/** Opt-in per-course diagnostic does not force experienced autodidacts to repeat all lessons. */
@Composable fun PlacementTrial7(course:Course,mastery:Mastery7,onDone:()->Unit){
    val entries=remember(course.id){course.units.take(6).mapIndexed{idx,u->Triple(u.concepts[idx%u.concepts.size],u.concepts[(idx+1)%u.concepts.size],idx)}}
    var step by remember(course.id){mutableIntStateOf(0)}
    var answer by remember(course.id){mutableStateOf<String?>(null)}
    var right by remember(course.id){mutableIntStateOf(0)}
    val current=entries[step]
    val opts=remember(step,course.id){listOf(current.first.term,current.second.term,course.units[(step+2)%course.units.size].concepts[0].term).distinct().shuffled()}
    Column(Modifier.fillMaxWidth().padding(17.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("DIAGNOSTIC · ${step+1}/${entries.size}",color=Color(0xFF936B2D),fontWeight=FontWeight.Bold)
        Text("Which concept best matches this description?",fontSize=22.sp,fontWeight=FontWeight.Bold)
        Text(current.first.definition,lineHeight=24.sp)
        opts.forEach{opt->OutlinedButton(onClick={answer=opt},enabled=answer==null,modifier=Modifier.fillMaxWidth()) {Text(opt)}}
        if(answer!=null){
            Text(if(answer==current.first.term)"✓ Correct" else "Study: ${current.first.term} — ${current.first.definition}")
            Button(onClick={
                val correct=answer==current.first.term
                if(correct)right++
                mastery.record(current.first.term,"recognition",correct,2)
                if(step==entries.lastIndex)onDone() else{step++;answer=null}
            },modifier=Modifier.fillMaxWidth()){Text(if(step==entries.lastIndex)"Finish diagnostic ($right correct so far)" else "Next")}
        }
        Text("This diagnostic recommends practice rather than automatically marking unattempted lessons complete.",color=Color.Gray,fontSize=12.sp)
    }
}