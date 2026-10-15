package com.sophia.nativeapp

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

/** An offline, branching authored dialogue, NOT an LLM. Free prose is saved, not semantically graded. */
@Composable fun SocraticChamber7(course:Course,unit:UnitLesson,progress:SophiaProgress,onExit:()->Unit){
    val stages=listOf("Define","Give reasons","Face objection","Revise")
    var stage by remember(course.id,unit.name){mutableIntStateOf(0)}
    var answer by remember(course.id,unit.name){mutableStateOf(progress.seminarResponse(course.id,course.units.indexOf(unit).coerceAtLeast(0),0))}
    var challenge by remember(course.id,unit.name){mutableStateOf("Not specified")}
    var accepted by remember(course.id,unit.name){mutableStateOf(false)}
    val uidx=course.units.indexOf(unit).coerceAtLeast(0)
    val thesis=unit.reading.thesis.ifBlank { "This chapter investigates the relations among ${unit.concepts[0].term} and ${unit.concepts[1].term}." }
    val objection=unit.reading.objection.ifBlank { "A rival account might explain the same phenomenon without these assumptions." }
    val prompts=listOf(
        "State the claim as charitably as possible. Define ${unit.concepts[0].term} and distinguish it from ${unit.concepts[1].term}.",
        "What two reasons support this claim? Write them as P1 and P2, and explain how the conclusion follows.",
        "Consider the strongest challenge: $objection. What premise does it target?",
        "Revise the thesis without pretending the objection never existed. What changed, and why?"
    )
    val nudges=listOf(
        "A definition should delimit a concept, not simply repeat its name.",
        "A reason must support a claim; the name of a famous philosopher is not a premise.",
        "The strongest criticism targets a definite assumption or inference, not its proponent.",
        "A defensible revision may narrow scope, add a distinction, or admit a counterexample."
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
        Text("🎓 THE SOCRATIC CHAMBER",fontSize=13.sp,color=Color(0xFFB78A3D),fontWeight=FontWeight.Bold)
        Text("An examiner who asks for reasons",fontSize=25.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF155D44))
        Text("${course.name} · ${unit.name}",fontSize=12.sp,color=Color.Gray)
        LinearProgressIndicator(progress=(stage+1)/4f,modifier=Modifier.fillMaxWidth().height(9.dp))
        Text("Round ${stage+1}/4 — ${stages[stage]}",fontWeight=FontWeight.Bold,color=Color(0xFF17674A))
        ElevatedCard(colors=CardDefaults.elevatedCardColors(containerColor=Color(0xFFF1F5E9)),shape=RoundedCornerShape(18.dp)){
            Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
                PhilosopherAvatar(4,Modifier.size(92.dp))
                Text("EXAMINER",fontWeight=FontWeight.Bold,color=Color(0xFF9B7133),fontSize=11.sp)
                Text(if(stage==0)"First, let us identify the claim at issue: $thesis" else when(challenge){
                    "Undefined term"->"Your terms may still shift in meaning. Can you defend the distinction?"
                    "Unsupported inference"->"Your conclusion does not follow merely from your assertion. Supply a missing reason."
                    "Counterexample"->"Suppose a counterexample exists. Which part of your position must be qualified?"
                    else->"Continue the argument, but do not overlook its strongest opposition."
                },lineHeight=23.sp,color=Color(0xFF244537))
                Text(prompts[stage],color=Color(0xFF214233),fontWeight=FontWeight.SemiBold,lineHeight=24.sp)
            }
        }
        OutlinedTextField(value=answer,onValueChange={answer=it;accepted=false;progress.saveSeminar(course.id,uidx,stage,it)},label={Text("Your philosophical response")},modifier=Modifier.fillMaxWidth().height(185.dp),placeholder={Text("Make the distinction explicit and explain why it matters...")})
        if(stage<3){
            Text("Choose what the examiner should challenge next:",fontWeight=FontWeight.Bold,color=Color(0xFF155D44))
            listOf("Undefined term","Unsupported inference","Counterexample").forEach{v->
                Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){RadioButton(selected=challenge==v,onClick={challenge=v});Text(v,color=Color(0xFF344A3E))}
            }
        }
        if(accepted){
            Surface(color=Color(0xFFE6F2E5),shape=RoundedCornerShape(14.dp)){
                Text("EXAMINER'S GUIDANCE: ${nudges[stage]}",Modifier.fillMaxWidth().padding(12.dp),color=Color(0xFF225A3F),lineHeight=21.sp)
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            OutlinedButton(enabled=stage>0,onClick={stage--;answer=progress.seminarResponse(course.id,uidx,stage);accepted=false},modifier=Modifier.weight(1f)){Text("Previous")}
            Button(enabled=answer.trim().length>=35,onClick={
                if(!accepted){accepted=true}else if(stage==3){onExit()}else{stage++;answer=progress.seminarResponse(course.id,uidx,stage);accepted=false}
            },modifier=Modifier.weight(1f)){Text(if(!accepted)"See examiner guidance" else if(stage==3)"Finish examination" else "Proceed")}
        }
        Text("This is authored, branching offline coaching. SOPHIA stores your answer but does not claim that a model has understood or independently graded it.",color=Color.Gray,fontSize=12.sp)
    }
}

@Composable fun DoctoralExamination7(course:Course,unit:UnitLesson,progress:SophiaProgress,onExit:()->Unit){
    val topics=listOf(
        "Reconstruct the author's central thesis and two supporting premises.",
        "Analyze a difficult passage or conceptual distinction and explain where it occurs in the argument.",
        "Give a serious counterargument that a defender must answer.",
        "Identify a rival interpretation and explain what source evidence could distinguish it.",
        "Revise and defend your position while explicitly acknowledging an unresolved problem."
    )
    var stage by remember(unit.name){mutableIntStateOf(0)}
    val uidx=course.units.indexOf(unit).coerceAtLeast(0)
    var response by remember(stage,unit.name){mutableStateOf(progress.seminarResponse(course.id,uidx,stage+10))}
    var checks by remember(stage){mutableStateOf(setOf<Int>())}
    val criteria=listOf("A definite, intelligible claim","Reasons or textual evidence","Attention to counterexamples","No unsupported claim of scholarly originality")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
        Text("🎓 DOCTORAL TRIAL",color=Color(0xFFAE7D37),fontWeight=FontWeight.ExtraBold,fontSize=12.sp)
        Text("Comprehensive oral examination",fontSize=25.sp,fontWeight=FontWeight.Bold,color=Color(0xFF165F45))
        Text("${course.name} · ${unit.name}",color=Color.Gray)
        LinearProgressIndicator(progress=(stage+1)/5f,modifier=Modifier.fillMaxWidth())
        Text("Examiner question ${stage+1}/5",fontWeight=FontWeight.Bold,color=Color(0xFF165F45))
        Text(topics[stage],fontSize=18.sp,color=Color(0xFF1A4633),lineHeight=25.sp)
        if(unit.reading.thesis.isNotBlank())Text("Relevant thesis: ${unit.reading.thesis}",color=Color.Gray,fontSize=13.sp)
        OutlinedTextField(value=response,onValueChange={response=it;progress.saveSeminar(course.id,uidx,stage+10,it)},modifier=Modifier.fillMaxWidth().height(235.dp),label={Text("Your examination response")})
        Text("Reflect on the answer using this rubric",fontWeight=FontWeight.SemiBold,color=Color(0xFF1E624B))
        criteria.forEachIndexed{i,label->
            Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Checkbox(checked=i in checks,onCheckedChange={checks=if(it)checks+i else checks-i});Text(label,fontSize=13.sp)}
        }
        Button(enabled=response.trim().length>=100&&checks.size>=3,onClick={if(stage<4)stage++ else onExit()},modifier=Modifier.fillMaxWidth()){
            Text(if(stage==4)"Complete doctoral reflection" else "Continue to harder examination")
        }
        Text("A completed doctoral reflection is practice, not a certified oral examination. Accurate grading of original philosophical arguments requires scholarly review or a reliable external evaluator.",color=Color.Gray,fontSize=12.sp)
    }
}