package com.sophia.nativeapp

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A small collection of independently interactive mini-games, rather than only Q&A cards. */
@Composable fun ExerciseGame(exercise:Exercise,selected:String?,onChosen:(String)->Unit){
    val pine=Color(0xFF185D43)
    when(exercise.kind){
        QuestionKind.CONTRAST->ConceptMatchGame(exercise,selected,onChosen)
        else->{
            var recallRevealed by remember(exercise.prompt) {mutableStateOf(false)}
            if(exercise.kind==QuestionKind.RECALL&&!recallRevealed){
                ElevatedCard(colors=CardDefaults.elevatedCardColors(containerColor=Color(0xFFFFF2D5)),shape=RoundedCornerShape(22.dp)){
                    Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
                        Text("🧠 RETRIEVAL FIRST",color=pine,fontWeight=FontWeight.Bold)
                        Text("Say your answer aloud or picture it mentally before revealing any options.",fontSize=14.sp,lineHeight=22.sp)
                        Button(onClick={recallRevealed=true},modifier=Modifier.fillMaxWidth()){Text("I have recalled an answer →")}
                    }
                }
            } else {
                val icon=when(exercise.kind){
                    QuestionKind.CRITIQUE->"🛡"
                    QuestionKind.LOGIC->"⚙"
                    QuestionKind.PREMISE->"🧩"
                    QuestionKind.APPLICATION,QuestionKind.TRANSFER->"✦"
                    QuestionKind.RECALL->"🧠"
                    else->"◇"
                }
                exercise.choices.forEachIndexed { i,option ->
                    val hasSelection=selected!=null
                    val state=when{hasSelection&&option==exercise.answer->Color(0xFFD8F5E2);selected==option->Color(0xFFFFE8DF);else->Color.White}
                    OutlinedCard(onClick={if(selected==null)onChosen(option)},colors=CardDefaults.outlinedCardColors(containerColor=state),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp),border=BorderStroke(2.dp,if(selected==option)pine else Color(0xFFE0E8E2))){
                        Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){
                            Text(if(exercise.kind==QuestionKind.LOGIC)if(option=="Valid")"✓" else "×" else icon,fontSize=22.sp,color=pine)
                            Spacer(Modifier.width(12.dp))
                            Text(option,color=Color(0xFF273A2D),modifier=Modifier.weight(1f),fontSize=14.sp,lineHeight=20.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun ConceptMatchGame(exercise:Exercise,selected:String?,onChosen:(String)->Unit){
    val pairs=remember(exercise.answer){
        exercise.answer.split("\n").take(2).map { row ->
            val at=row.indexOf(": ")
            if(at<0)row to row else row.substring(0,at) to row.substring(at+2)
        }
    }
    if(pairs.size!=2){
        exercise.choices.forEach { option ->
            OutlinedButton(onClick={onChosen(option)},modifier=Modifier.fillMaxWidth()){Text(option)}
        }
        return
    }
    val order=remember(exercise.answer){if(exercise.answer.hashCode()%2==0)listOf(1,0) else listOf(0,1)}
    var chosen by remember(exercise.prompt){mutableIntStateOf(-1)}
    var matched by remember(exercise.prompt){mutableStateOf(emptySet<Int>())}
    var mistakes by remember(exercise.prompt){mutableIntStateOf(0)}
    var hint by remember(exercise.prompt){mutableStateOf("Tap a term, then its matching explanation.")}
    Text("🔗 MATCH THE IDEAS",fontSize=12.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF1B6A47))
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
        Column(Modifier.weight(.85f),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("Concepts",fontWeight=FontWeight.Bold,fontSize=13.sp)
            pairs.forEachIndexed{i,p->
                val active=chosen==i
                OutlinedCard(onClick={if(selected==null&&i !in matched){chosen=i;hint="Now choose the corresponding explanation."}},
                    colors=CardDefaults.outlinedCardColors(containerColor=if(i in matched)Color(0xFFDEF5E3) else if(active)Color(0xFFFFE9BC) else Color.White),
                    modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(13.dp)){
                    Text(if(i in matched)"✓ ${p.first}" else p.first,modifier=Modifier.padding(11.dp),fontSize=13.sp,fontWeight=FontWeight.SemiBold)
                }
            }
        }
        Column(Modifier.weight(1.15f),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("Meanings",fontWeight=FontWeight.Bold,fontSize=13.sp)
            order.forEach{actual->
                OutlinedCard(onClick={
                    if(selected==null && actual !in matched){
                        if(chosen==actual){
                            matched=matched+actual;chosen=-1;hint="✓ Linked! ${matched.size}/2 completed"
                            if(matched.size==2)onChosen(if(mistakes==0)exercise.answer else "__MATCH_WITH_ERRORS__")
                        }else{
                            mistakes++;chosen=-1;hint="Not quite. Reconsider the definition and try again."
                        }
                    }
                },modifier=Modifier.fillMaxWidth(),colors=CardDefaults.outlinedCardColors(containerColor=if(actual in matched)Color(0xFFDEF5E3) else Color.White),shape=RoundedCornerShape(13.dp)){
                    Text(if(actual in matched)"✓ ${pairs[actual].second}" else pairs[actual].second,modifier=Modifier.padding(11.dp),fontSize=12.sp,lineHeight=18.sp)
                }
            }
        }
    }
    Text(hint,color=if(mistakes>0)Color(0xFF9E5B34) else Color(0xFF29714D),fontSize=12.sp)
    if(selected!=null)Text("Concept connections complete.",color=Color(0xFF176A48),fontWeight=FontWeight.Bold)
}