package com.sophia.nativeapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Native, illustrated four-phase challenge; no generic quiz screen is reused. */
@Composable fun BossArena(unit:UnitLesson,course:Course,courseIndex:Int,
    onFinish:(Int,Boolean)->Unit,onExit:()->Unit) {
    val gold=Color(0xFFFFD482);val forest=Color(0xFF134F3B);val ink=Color(0xFF253629)
    val cc=unit.concepts
    var phase by remember(course.id,unit.name){mutableIntStateOf(0)}
    var score by remember(course.id,unit.name){mutableIntStateOf(0)}
    var heroHp by remember(course.id,unit.name){mutableIntStateOf(100)}
    var opponentHp by remember(course.id,unit.name){mutableIntStateOf(100)}
    var locked by remember(course.id,unit.name){mutableStateOf(false)}
    var good by remember(course.id,unit.name){mutableStateOf(false)}
    var picked by remember(course.id,unit.name){mutableStateOf(setOf<Int>())}
    val phaseNames=listOf("The Claim","The Argument","The Objection","The Revision")
    val attacks=listOf("DISCERNMENT","ARGUMENT FORGE","CRITICAL SHIELD","DIALECTICAL TURN")
    val titles=listOf("Find the strongest thesis","Assemble the premises","Block the weak attacks","Revise without retreating")
    val claims=listOf(
        "The chapter studies ${cc[0].term} and how it relates to ${cc[1].term}.",
        "These concepts have the same meaning and require no distinction.",
        "This question is unanswerable because it involves philosophy."
    )
    val premises=listOf(
        "P1 · ${cc[1].term}: ${cc[1].definition}",
        "P2 · ${cc[2].term}: ${cc[2].definition}",
        "P3 · Popularity alone makes an argument valid.",
        "P4 · We may assume our conclusion without reasons."
    )
    val objections=listOf(
        "Test this objection: ${unit.reading.objection.ifBlank { "Another explanation might fit the evidence." }}",
        "Criticize the thinker personally rather than the premises.",
        "Ignore the thesis and repeat a different conclusion."
    )
    val revisions=listOf(
        "Retain the thesis provisionally, clarify its premises, and address the strongest objection.",
        "Insist the original conclusion must be true, no matter the evidence.",
        "Abandon all reasoning because more than one interpretation is possible."
    )
    val choices=when(phase){0->claims;1->premises;2->objections;else->revisions}
    val transition=rememberInfiniteTransition(label="boss")
    val float by transition.animateFloat(0f,1f,infiniteRepeatable(tween(1450),RepeatMode.Reverse),label="opponent")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(13.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Box(Modifier.fillMaxWidth().height(292.dp).clip(RoundedCornerShape(25.dp))){
            Image(painterResource(R.drawable.academy_world),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xB10B3127),Color.Transparent,Color(0xC8133F33)))))
            Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                    Text("⚔ CHAPTER BOSS",color=gold,fontWeight=FontWeight.ExtraBold,fontSize=11.sp)
                    Text("PHASE ${phase+1}/4",color=Color.White,fontSize=12.sp,fontWeight=FontWeight.Bold)
                }
                Text("${course.name} · ${unit.name}",color=Color.White,fontWeight=FontWeight.Bold,fontSize=17.sp,maxLines=2)
                Row(verticalAlignment=Alignment.CenterVertically){
                    Text("BOSS",color=Color.White,fontSize=11.sp)
                    Spacer(Modifier.width(8.dp))
                    LinearProgressIndicator(progress=opponentHp/100f,modifier=Modifier.weight(1f).height(10.dp).clip(CircleShape),color=Color(0xFFF2C45D),trackColor=Color(0x777D342A))
                    Spacer(Modifier.width(6.dp));Text("$opponentHp",color=gold,fontSize=13.sp)
                }
            }
            PhilosopherAvatar(courseIndex,Modifier.size(151.dp).align(Alignment.Center).offset(y=((float-.5f)*13).dp))
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(13.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                Surface(color=Color(0xDE123F31),shape=RoundedCornerShape(13.dp)){
                    Text("“${when(phase){0->"Define what you are defending.";1->"A claim is not yet an argument.";2->"Can your reasons survive criticism?";else->"Now refine your understanding."}}”",color=Color.White,modifier=Modifier.fillMaxWidth().padding(10.dp),textAlign=TextAlign.Center,fontSize=13.sp)
                }
                Row(verticalAlignment=Alignment.CenterVertically){
                    Text("🛡 YOU",color=Color.White,fontSize=11.sp);Spacer(Modifier.width(8.dp))
                    LinearProgressIndicator(progress=heroHp/100f,modifier=Modifier.weight(1f).height(10.dp).clip(CircleShape),color=Color(0xFF62E39B),trackColor=Color(0x99633232))
                    Spacer(Modifier.width(7.dp));Text("$heroHp",color=Color.White,fontSize=12.sp)
                }
            }
        }
        Text(attacks[phase],color=Color(0xFFBA8B39),fontSize=11.sp,fontWeight=FontWeight.ExtraBold)
        Text(titles[phase],color=forest,fontSize=24.sp,fontWeight=FontWeight.Bold,lineHeight=28.sp)
        Text(when(phase){
            0->"Strike the false thesis by selecting the philosophically accurate claim."
            1->"Collect TWO sound candidate premises. Avoid the red herrings!"
            2->"Raise your shield with an objection that addresses reasons, not the person."
            else->"Choose a rational revision. Avoid both dogmatism and total surrender."
        },color=ink,fontSize=14.sp,lineHeight=21.sp)
        if(phase==1){
            Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){
                repeat(2){i->Surface(color=if(picked.size>i)Color(0xFF5DBC85) else Color(0xFFDDE7DF),shape=CircleShape){Text(if(picked.size>i)"✓" else "◇",modifier=Modifier.padding(horizontal=17.dp,vertical=6.dp),fontWeight=FontWeight.Bold)}}
            }
        }
        choices.forEachIndexed { idx,choice ->
            val selected=if(phase==1)idx in picked else locked && (idx==0 || (good && idx==0))
            val color=when { selected && idx==0->Color(0xFFD8F5DF);selected->Color(0xFFFCE4DB);else->Color.White }
            OutlinedCard(
                onClick={
                    if(!locked){
                        if(phase==1){
                            picked=picked+idx
                            if(picked.size==2){
                                good=picked==setOf(0,1);locked=true
                                if(good){score+=2;opponentHp=(opponentHp-25).coerceAtLeast(0)}
                                else heroHp=(heroHp-25).coerceAtLeast(0)
                            }
                        }else{
                            good=idx==0;locked=true
                            if(good){score+=2;opponentHp=(opponentHp-25).coerceAtLeast(0)}
                            else heroHp=(heroHp-25).coerceAtLeast(0)
                        }
                    }
                },modifier=Modifier.fillMaxWidth(),colors=CardDefaults.outlinedCardColors(containerColor=color),
                shape=RoundedCornerShape(16.dp),border=BorderStroke(2.dp,if(idx in picked||locked&&idx==0) Color(0xFF68B98B) else Color(0xFFE1E7DF))
            ){
                Row(Modifier.padding(15.dp),verticalAlignment=Alignment.CenterVertically){
                    Text(if(idx in picked)"✦" else "◇",color=forest,fontSize=24.sp);Spacer(Modifier.width(10.dp))
                    Text(choice,color=ink,fontSize=14.sp,lineHeight=20.sp,modifier=Modifier.weight(1f))
                }
            }
        }
        if(locked){
            Surface(color=if(good)Color(0xFFE0F6E6) else Color(0xFFFFE9DE),shape=RoundedCornerShape(15.dp)){
                Text(if(good)"⚡ DEFENSE BROKEN! Argument strength +2" else "🛡 Your defense was breached. Review the distinction before advancing.",modifier=Modifier.fillMaxWidth().padding(14.dp),color=forest,fontWeight=FontWeight.Bold)
            }
            Button(onClick={
                if(phase==3 || heroHp<=0){onFinish(score,score>=6 && heroHp>0)}
                else {phase++;locked=false;good=false;picked=emptySet()}
            },modifier=Modifier.fillMaxWidth()) {Text(if(phase==3 || heroHp<=0)"Complete encounter" else "⚔ Continue to ${phaseNames[phase+1]}")}
        }
        Text("Gameplay tests philosophical fidelity and inferential care; it does not decide which disputed theory is universally true.",fontSize=11.sp,color=Color.Gray)
        OutlinedButton(onClick=onExit,modifier=Modifier.fillMaxWidth()) {Text("Leave the arena")}
    }
}