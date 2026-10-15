package com.sophia.nativeapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ThesisDraft7(val claim:String="",val premise1:String="",val premise2:String="",val objection:String="",val reply:String="",val evidence:String=""){
    fun asJson()=JSONObject().put("claim",claim).put("p1",premise1).put("p2",premise2).put("objection",objection).put("reply",reply).put("evidence",evidence)
    companion object{fun from(json:JSONObject)=ThesisDraft7(json.optString("claim"),json.optString("p1"),json.optString("p2"),json.optString("objection"),json.optString("reply"),json.optString("evidence"))}
}
@Composable fun ResearchLab7(books:List<PrimaryBook>){
    val ctx=androidx.compose.ui.platform.LocalContext.current
    val prefs=remember{ctx.getSharedPreferences("sophia_research7",Context.MODE_PRIVATE)}
    var claim by remember{mutableStateOf(prefs.getString("claim","")?:"")}
    var premise1 by remember{mutableStateOf(prefs.getString("p1","")?:"")}
    var premise2 by remember{mutableStateOf(prefs.getString("p2","")?:"")}
    var objection by remember{mutableStateOf(prefs.getString("objection","")?:"")}
    var reply by remember{mutableStateOf(prefs.getString("reply","")?:"")}
    var evidence by remember{mutableStateOf(prefs.getString("evidence","")?:"")}
    var revisions by remember{mutableStateOf(prefs.getString("versions","[]")?:"[]")}
    var showVersion by remember{mutableIntStateOf(-1)}
    val editor=prefs.edit()
    fun save(){editor.putString("claim",claim).putString("p1",premise1).putString("p2",premise2).putString("objection",objection).putString("reply",reply).putString("evidence",evidence).apply()}
    fun draft()=ThesisDraft7(claim,premise1,premise2,objection,reply,evidence)
    val fields=listOf("Thesis" to claim,"Premise 1" to premise1,"Premise 2" to premise2,"Objection" to objection,"Reply" to reply)
    val completion=fields.count{it.second.trim().length>=12}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(17.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
        Text("🧪 THE RESEARCH INSTITUTE",color=Color(0xFFBA8C45),fontSize=12.sp,fontWeight=FontWeight.Bold)
        Text("Build a defensible theory",color=Color(0xFF145D43),fontSize=25.sp,fontWeight=FontWeight.ExtraBold)
        Text("Each field has a different logical role. A diagram of them is not a proof; it is an invitation to identify missing inferences.",color=Color.Gray,fontSize=12.sp)
        LinearProgressIndicator(progress=completion/5f,modifier=Modifier.fillMaxWidth().height(9.dp))
        @Composable fun LabeledEditor(title:String,value:String,onChange:(String)->Unit,color:Color){
            ElevatedCard(shape=RoundedCornerShape(17.dp),colors=CardDefaults.elevatedCardColors(containerColor=color)){
                Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
                    Text(title.uppercase(),fontWeight=FontWeight.Bold,fontSize=12.sp,color=Color(0xFF1B5B43))
                    OutlinedTextField(value=value,onValueChange={onChange(it);save()},modifier=Modifier.fillMaxWidth(),minLines=2,maxLines=5)
                }
            }
        }
        LabeledEditor("① Thesis",claim,{claim=it},Color(0xFFF0F8E9))
        Text("↑ Supported by the following premises",color=Color(0xFF77937A),fontSize=12.sp)
        LabeledEditor("② Premise 1",premise1,{premise1=it},Color.White)
        LabeledEditor("③ Premise 2",premise2,{premise2=it},Color.White)
        Text("↓ Subject your weakest step to a serious objection",color=Color(0xFF77937A),fontSize=12.sp)
        LabeledEditor("④ Strongest objection",objection,{objection=it},Color(0xFFFFF4E7))
        LabeledEditor("⑤ Revised response",reply,{reply=it},Color(0xFFF0F8E9))
        LabeledEditor("⑥ Primary-text evidence and bibliographic source",evidence,{evidence=it},Color.White)
        val checks=listOf(
            "Thesis is specified" to claim.trim().length>=12,
            "Two distinct supporting reasons are supplied" to (premise1.trim().length>=12&&premise2.trim().length>=12&&premise1.trim()!=premise2.trim()),
            "A counterposition is articulated" to objection.trim().length>=12,
            "A response attempts a revision" to reply.trim().length>=12,
            "Textual source is identified" to evidence.trim().length>=12
        )
        Text("ARGUMENT AUDIT",fontWeight=FontWeight.ExtraBold,color=Color(0xFF155D42))
        checks.forEach{(statement,ok)->Text((if(ok)"✓ "else "○ ")+statement,color=Color(0xFF314B3A),fontSize=14.sp)}
        Text("A checkmark signals the presence of a response, not a verified inference or scholarly citation.",color=Color.Gray,fontSize=12.sp)
        Button(onClick={
            val versions=try{JSONArray(revisions)}catch(_:Exception){JSONArray()}
            val record=draft().asJson().put("savedAt",System.currentTimeMillis())
            versions.put(record)
            while(versions.length()>20)versions.remove(0)
            revisions=versions.toString()
            prefs.edit().putString("versions",revisions).apply()
        },modifier=Modifier.fillMaxWidth()){Text("✦ Save this revision to the research timeline")}
        Text("REVISION TIMELINE",color=Color(0xFF155D42),fontWeight=FontWeight.Bold)
        val versions=remember(revisions){try{JSONArray(revisions)}catch(_:Exception){JSONArray()}}
        if(versions.length()==0)Text("Save a revision to begin comparing the development of your argument.",color=Color.Gray)
        for(i in versions.length()-1 downTo 0){
            val obj=versions.optJSONObject(i)?:continue
            val stamp=SimpleDateFormat("MMM dd · HH:mm",Locale.US).format(Date(obj.optLong("savedAt")))
            OutlinedCard(onClick={showVersion=i},modifier=Modifier.fillMaxWidth()){
                Column(Modifier.padding(12.dp)){
                    Text("Revision ${i+1} · $stamp",color=Color(0xFF1C684F),fontWeight=FontWeight.Bold)
                    Text(obj.optString("claim").take(110),color=Color(0xFF4D6657),fontSize=13.sp)
                }
            }
        }
        if(showVersion in 0 until versions.length()){
            val old=versions.optJSONObject(showVersion)?:JSONObject()
            Text("EARLIER CLAIM: ${old.optString("claim")}",color=Color(0xFF1B6448))
            Text("CURRENT CLAIM: $claim",color=Color(0xFF1B6448))
            Text(if(old.optString("claim")==claim)"The core thesis has not changed." else "Your thesis wording changed; assess whether the argument's inferential obligations changed too.",color=Color.Gray)
            OutlinedButton(onClick={
                val restored=ThesisDraft7.from(old)
                claim=restored.claim;premise1=restored.premise1;premise2=restored.premise2
                objection=restored.objection;reply=restored.reply;evidence=restored.evidence
                prefs.edit().putString("claim",claim).putString("p1",premise1).putString("p2",premise2)
                    .putString("objection",objection).putString("reply",reply).putString("evidence",evidence).apply()
                showVersion=-1
            }){Text("Restore this revision")}
        }
        Text("Source catalog: ${books.size} primary works. Cite the original edition and location of each passage in your textual-evidence field.",fontSize=12.sp,color=Color.Gray)
    }
}