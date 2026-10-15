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
import kotlin.random.Random

/** Native, illustrated four-phase challenge; no generic quiz screen is reused. */
@Composable fun BossArena(unit:UnitLesson,course:Course,courseIndex:Int,
    mastery:Mastery7,onFinish:(Int,Boolean)->Unit,onExit:()->Unit) {
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
    val attacks=listOf("DISCERNMENT","REASONING EXAMINATION","CRITICAL SHIELD","DIALECTICAL TURN")
    val titles=listOf("Find the strongest thesis","Assemble the premises","Block the weak attacks","Revise without retreating")
    val philosopherChallenge=when {
        course.name.contains("Kantian",true)->"Can a condition of experience be confused with a property of things as they are in themselves?"
        course.name.contains("Hegelian",true)->"Does your negation transform a specific content, or merely reject it?"
        course.name.contains("Socratic",true)->"Can your definition survive a counterexample offered in dialogue?"
        course.name.contains("Platonic",true)->"Do your premises explain why knowledge differs from opinion?"
        course.name.contains("Schopenhauer",true)->"Can you defend the relation between representation, Will, and suffering?"
        course.name.contains("Buddhist",true)->"Does dependent origination require an independent self?"
        course.name.contains("Phenomenology",true)->"Can you distinguish description from a psychological hypothesis?"
        course.name.contains("Positivism",true)->"Can observation alone secure the methodological claims of positivism?"
        course.name.contains("Aristotelian",true)->"Have you distinguished explanation by cause from mere succession?"
        else->"Which assumption is indispensable to your thesis, and how could an opponent test it?"
    }
    val thesis=unit.reading.thesis.ifBlank{"The chapter investigates how ${cc[0].term} and ${cc[1].term} are related."}
    val challenge=unit.reading.objection.ifBlank{"Could a counterexample disprove this interpretation?"}
    val claims=listOf(
        thesis to true,
        "${cc[0].term} and ${cc[1].term} must have exactly the same explanatory role." to false,
        "The existence of ${cc[0].term} alone logically entails every claim in this chapter." to false
    )
    val premises=listOf(
        "P1 · ${cc[1].term}: ${cc[1].definition}" to true,
        "P2 · ${cc[2].term}: ${cc[2].definition}" to true,
        "P3 · ${cc[2].term}: ${cc[1].definition}" to false,
        "P4 · ${cc[1].term} is true merely because the thinker is famous." to false
    )
    val objections=listOf(
        "Test the specific challenge: $challenge" to true,
        "Reject the thesis because the thinker had historical opponents." to false,
        "Assume that every alternative explanation is equally well supported." to false
    )
    val revisions=listOf(
        "Clarify the premise about ${cc[0].term}, qualify the thesis where needed, and answer the objection." to true,
        "Keep the conclusion unchanged even if its decisive premise fails." to false,
        "Reject all argumentation because disagreement is possible." to false
    )
    val choices=remember(phase,course.id,unit.name){
        when(phase){0->claims;1->premises;2->objections;else->revisions}
            .shuffled(Random(31001+course.id.hashCode()+unit.name.hashCode()+phase))
    }
    var chosenSingle by remember(course.id,unit.name){mutableIntStateOf(-1)}
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
                    Text("“${when(phase){0->philosopherChallenge;1->"A claim is not yet an argument.";2->"Can your reasons survive criticism?";else->"Now refine your understanding."}}”",color=Color.White,modifier=Modifier.fillMaxWidth().padding(10.dp),textAlign=TextAlign.Center,fontSize=13.sp)
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
        choices.forEachIndexed { idx,entry ->
            val choice=entry.first
            val selected=if(phase==1)idx in picked else locked && chosenSingle==idx
            val color=when { selected && entry.second->Color(0xFFD8F5DF);selected->Color(0xFFFCE4DB);else->Color.White }
            OutlinedCard(
                onClick={
                    if(!locked){
                        if(phase==1){
                            picked=picked+idx
                            if(picked.size==2){
                                good=picked.count{choices[it].second}==2;locked=true
                                mastery.record(cc[phase].term,"reconstruction",good)
                                if(good){score+=2;opponentHp=(opponentHp-25).coerceAtLeast(0)}
                                else heroHp=(heroHp-25).coerceAtLeast(0)
                            }
                        }else{
                            chosenSingle=idx;good=entry.second;locked=true
                            mastery.record(cc[phase].term,when(phase){0->"comprehension";2->"criticism";else->"synthesis"},good)
                            if(good){score+=2;opponentHp=(opponentHp-25).coerceAtLeast(0)}
                            else heroHp=(heroHp-25).coerceAtLeast(0)
                        }
                    }
                },modifier=Modifier.fillMaxWidth(),colors=CardDefaults.outlinedCardColors(containerColor=color),
                shape=RoundedCornerShape(16.dp),border=BorderStroke(2.dp,if((idx in picked||chosenSingle==idx)&&entry.second) Color(0xFF68B98B) else Color(0xFFE1E7DF))
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
                else {phase++;locked=false;good=false;picked=emptySet();chosenSingle=-1}
            },modifier=Modifier.fillMaxWidth()) {Text(if(phase==3 || heroHp<=0)"Complete encounter" else "⚔ Continue to ${phaseNames[phase+1]}")}
        }
        Text("Gameplay tests philosophical fidelity and inferential care; it does not decide which disputed theory is universally true.",fontSize=11.sp,color=Color.Gray)
        OutlinedButton(onClick=onExit,modifier=Modifier.fillMaxWidth()) {Text("Leave the arena")}
    }
}