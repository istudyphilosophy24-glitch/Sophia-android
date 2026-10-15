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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val forest=Color(0xFF164D3A)
private val amber=Color(0xFFF7DA91)
private val courseColors=listOf(0xFF217B7A,0xFF8166AC,0xFF527FAC,0xFFAD844B,0xFF648E6B,0xFF9E6173,0xFF468DA3)

@Composable fun PhilosopherAvatar(index:Int,modifier:Modifier=Modifier.size(100.dp)){
    val image=ImageBitmap.imageResource(R.drawable.philosopher_gallery)
    Canvas(modifier.clip(RoundedCornerShape(18.dp))){
        val cell=image.width/3;val r=((index%9)+9)%9
        drawImage(image,srcOffset=IntOffset((r%3)*cell,(r/3)*cell),srcSize=IntSize(cell,cell),
            dstOffset=IntOffset.Zero,dstSize=IntSize(size.width.toInt(),size.height.toInt()))
    }
}
@Composable fun IllustratedHomeBanner(onExplore:()->Unit){
    Box(Modifier.fillMaxWidth().height(272.dp).clip(RoundedCornerShape(26.dp))){
        Image(painterResource(R.drawable.academy_world),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent,Color(0xD70C382D)))))
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
            Text("✦ THE PHILOSOPHICAL ODYSSEY",color=amber,fontSize=11.sp,fontWeight=FontWeight.Bold)
            Text("Enter the Academy",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.ExtraBold)
            Row(verticalAlignment=Alignment.CenterVertically){
                Text("28 worlds · 280 chapters",color=Color.White,fontSize=13.sp)
                Spacer(Modifier.weight(1f))
                Surface(onClick=onExplore,color=amber,shape=CircleShape){Text("Explore →",Modifier.padding(11.dp),color=forest,fontWeight=FontWeight.Bold)}
            }
        }
    }
}
@Composable fun IllustratedCourseTile(course:Course,index:Int,done:Int,onClick:()->Unit){
    ElevatedCard(onClick=onClick,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)){
        Box(Modifier.fillMaxWidth().height(148.dp)){
            Image(painterResource(R.drawable.academy_world),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(courseColors[index%courseColors.size]).copy(alpha=.95f),Color(0xAA14442F),Color.Transparent))))
            Row(Modifier.fillMaxSize().padding(14.dp),verticalAlignment=Alignment.CenterVertically){
                PhilosopherAvatar(index,Modifier.size(83.dp));Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)){
                    Text("WORLD ${index+1}",fontSize=10.sp,color=amber,fontWeight=FontWeight.ExtraBold)
                    Text(course.name,fontSize=18.sp,color=Color.White,fontWeight=FontWeight.ExtraBold,lineHeight=22.sp)
                    Text("$done / 120 lessons",color=Color.White,fontSize=12.sp)
                    LinearProgressIndicator(progress=done/120f,modifier=Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),color=amber,trackColor=Color(0x99709F83))
                }
            }
        }
    }
}
@Composable fun CourseWorldHeader(course:Course,index:Int,done:Int){
    Box(Modifier.fillMaxWidth().height(206.dp).clip(RoundedCornerShape(22.dp))){
        Image(painterResource(R.drawable.academy_world),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x440A3B32),Color(0xE3124635)))))
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
            Text("✦ WORLD ${index+1}",color=amber,fontWeight=FontWeight.Bold,fontSize=11.sp)
            Text(course.name,color=Color.White,fontSize=25.sp,fontWeight=FontWeight.ExtraBold,lineHeight=29.sp)
            Text("$done / 120 quests finished",color=Color.White,fontSize=12.sp)
            LinearProgressIndicator(progress=done/120f,modifier=Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),color=amber,trackColor=Color(0x887DA892))
        }
    }
}
@Composable fun IllustratedUnitPath(unit:UnitLesson,course:Course,unitNumber:Int,isDone:(Int)->Boolean,onEnter:(Int)->Unit){
    val grove=Color(0xFF1B6447);val gold=Color(0xFFB98B38)
    ElevatedCard(shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth(),colors=CardDefaults.elevatedCardColors(containerColor=Color.White)){
        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("✦ CHAPTER ${unitNumber+1}",fontSize=11.sp,color=gold,fontWeight=FontWeight.ExtraBold)
            Text(unit.name,color=grove,fontWeight=FontWeight.ExtraBold,fontSize=20.sp)
            Text("Six new discoveries · four advanced challenges · spiral memory · illustrated boss",color=Color(0xFF6E8175),fontSize=11.sp)
            Box(Modifier.fillMaxWidth().height(485.dp)){
                Canvas(Modifier.fillMaxSize()){
                    val w=size.width;val h=size.height
                    val ys=(0..3).map { h*(it+0.5f)/4f }
                    val xs=listOf(.18f*w,.5f*w,.82f*w)
                    val track=Path().apply{
                        moveTo(xs[0],ys[0]);lineTo(xs[1],ys[0]);lineTo(xs[2],ys[0]);
                        quadraticTo(w*.98f,(ys[0]+ys[1])/2,xs[2],ys[1]);
                        lineTo(xs[1],ys[1]);lineTo(xs[0],ys[1]);
                        quadraticTo(w*.02f,(ys[1]+ys[2])/2,xs[0],ys[2]);
                        lineTo(xs[1],ys[2]);lineTo(xs[2],ys[2]);
                        quadraticTo(w*.98f,(ys[2]+ys[3])/2,xs[2],ys[3]);
                        lineTo(xs[1],ys[3]);lineTo(xs[0],ys[3]);
                    }
                    drawPath(track,Color(0xFFB3D1B6),style=androidx.compose.ui.graphics.drawscope.Stroke(width=8.dp.toPx(),cap=androidx.compose.ui.graphics.StrokeCap.Round))
                }
                Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.SpaceAround){
                    repeat(4){r->
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceAround){
                            repeat(3){col->
                                val j=if(r%2==0)col else 2-col
                                val n=r*3+j;val done=isDone(n);val boss=n==11
                                val mode=when(n){in 0..5->"Explore";6->"Connect";7->"Argue";8->"Critique";9->"Apply";10->"Recall";else->"BOSS"}
                                Column(horizontalAlignment=Alignment.CenterHorizontally){
                                    Box(Modifier.size(if(boss)72.dp else 65.dp)
                                        .shadow(if(boss)8.dp else 4.dp,CircleShape)
                                        .clip(CircleShape)
                                        .background(Brush.verticalGradient(if(done)listOf(Color(0xFF62C995),Color(0xFF218459))
                                            else if(boss)listOf(Color(0xFFFFE19A),Color(0xFFF2B34B))
                                            else listOf(Color(0xFFE7F8E7),Color(0xFFBDE4CA))))
                                        .border(3.dp,Color.White,CircleShape)
                                        .clickable{onEnter(n)},contentAlignment=Alignment.Center){
                                        Text(if(done)"✓" else if(boss)"⚔" else when(n){6->"⌁";7->"⚙";8->"🛡";9->"✧";10->"♻";else->"${n+1}"},
                                            fontSize=if(boss)30.sp else 22.sp,color=if(done)Color.White else grove,fontWeight=FontWeight.ExtraBold)
                                    }
                                    Text(mode,fontSize=10.sp,color=if(boss)gold else grove,fontWeight=FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            Text("⚔ Defeat the chapter guardian to claim your philosopher's crest",color=gold,fontSize=12.sp,fontWeight=FontWeight.Bold)
        }
    }
}
@Composable fun RewardCelebration(passed:Boolean,score:Int){
    val t=rememberInfiniteTransition(label="reward")
    val drift by t.animateFloat(0f,1f,infiniteRepeatable(tween(1500),RepeatMode.Reverse),label="confetti")
    Box(Modifier.fillMaxWidth().height(245.dp).clip(RoundedCornerShape(24.dp))){
        Image(painterResource(R.drawable.academy_world),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x99FFF5D7),Color(0xC9E5F1DE)))))
        Canvas(Modifier.fillMaxSize()){
            val palette=listOf(Color(0xFFD9A82E),Color(0xFF4BBA7C),Color(0xFF9675C8))
            repeat(55){i->drawCircle(palette[i%3],radius=(3+i%3).dp.toPx(),center=Offset((i*137%997)/997f*size.width,((i*311%991)/991f+drift*.05f)*size.height))}
        }
        Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally){
            Text(if(passed)"🏆" else "📚",fontSize=83.sp)
            Text(if(passed)"QUEST COMPLETE!" else "TRY AGAIN",fontSize=24.sp,fontWeight=FontWeight.ExtraBold,color=forest)
            Text("$score / 8 points",fontSize=15.sp,color=Color(0xFF895E26))
        }
    }
}
@Composable fun DailyQuestBoard(completed:Int,streak:Int,xp:Int){
    ElevatedCard(shape=RoundedCornerShape(22.dp),colors=CardDefaults.elevatedCardColors(containerColor=Color.White)){
        Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
            Text("⚡ YOUR DAILY ADVENTURES",color=forest,fontSize=15.sp,fontWeight=FontWeight.ExtraBold)
            listOf(Triple("📖 Discover one new idea",completed.coerceAtMost(1),1),
                Triple("🏅 Finish five lessons",completed.coerceAtMost(5),5),
                Triple("🔥 Maintain a study streak",streak.coerceAtMost(1),1)).forEach{(label,n,total)->
                Row(verticalAlignment=Alignment.CenterVertically){
                    Text(label,color=forest,modifier=Modifier.weight(1f),fontSize=12.sp)
                    LinearProgressIndicator(progress=(n.toFloat()/total).coerceIn(0f,1f),modifier=Modifier.width(90.dp).height(7.dp).clip(CircleShape),color=Color(0xFF43B67C),trackColor=Color(0xFFE1F0E4))
                }
            }
            Text("Level ${xp/600+1} · ${xp%600} / 600 XP to next rank",color=Color(0xFF8A682D),fontWeight=FontWeight.SemiBold,fontSize=12.sp)
            LinearProgressIndicator(progress=(xp%600)/600f,modifier=Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),color=amber,trackColor=Color(0xFFF4E6C9))
        }
    }
}

/** Visual control surface for the hands-free philosophical station. */
@Composable fun RadioBanner() {
    Box(Modifier.fillMaxWidth().height(195.dp).clip(RoundedCornerShape(23.dp))){
        androidx.compose.foundation.Image(
            painter=painterResource(R.drawable.academy_world),contentDescription="Illustrated Academy",
            modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xEF102D28),Color(0x99192B40),Color(0x330D2B24)))))
        Column(Modifier.align(Alignment.CenterStart).padding(17.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
            Text("✦ SOPHIA RADIO",color=Color(0xFFF0D290),fontWeight=FontWeight.Bold,fontSize=11.sp)
            Text("Wisdom on the move",color=Color.White,fontSize=23.sp,fontWeight=FontWeight.ExtraBold)
            Text("Lectures · Excerpts · Commentary",color=Color.White,fontSize=13.sp)
            Text("🎧  Shuffle endlessly  •  hands free",color=Color(0xFFE4ECD9),fontSize=12.sp)
        }
    }
}

/** Concept constellation visualizes conceptual neighbors, and exposes an explanatory selected node. */
@Composable fun ConceptConstellation(course:Course,unitIndex:Int) {
    val unit=course.units[unitIndex]
    var selected by remember(course.id,unitIndex){mutableIntStateOf(0)}
    val selectedConcept=unit.concepts[selected]
    ElevatedCard(shape=RoundedCornerShape(23.dp),colors=CardDefaults.elevatedCardColors(containerColor=Color(0xFF132C36))){
        Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
            Text("✧ CONCEPT CONSTELLATION",color=Color(0xFFF9DEA3),fontSize=11.sp,fontWeight=FontWeight.Bold)
            Text(unit.name,color=Color.White,fontSize=20.sp,fontWeight=FontWeight.ExtraBold)
            Canvas(Modifier.fillMaxWidth().height(205.dp)){
                val cx=size.width*.50f;val cy=size.height*.5f
                val coords=unit.concepts.indices.map { i ->
                    val a=(i.toFloat()/6f)*Math.PI*2.0-Math.PI/2
                    Offset(cx+size.width*.35f*kotlin.math.cos(a).toFloat(),cy+size.height*.33f*kotlin.math.sin(a).toFloat())
                }
                for(i in coords.indices){
                    drawLine(Color(0xFF507F81),Offset(cx,cy),coords[i],strokeWidth=2.dp.toPx())
                    drawLine(Color(0xFF426C79),coords[i],coords[(i+1)%6],strokeWidth=1.dp.toPx())
                }
                drawCircle(Color(0xFFECCF7D),radius=24.dp.toPx(),center=Offset(cx,cy))
                for(i in coords.indices){
                    drawCircle(if(i==selected)Color(0xFFFFC75F) else Color(0xFF70C6B4),radius=(if(i==selected)17 else 12).dp.toPx(),center=coords[i])
                    drawCircle(Color(0xFF172F38),radius=4.dp.toPx(),center=coords[i])
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                (0..5).forEach{i->
                    FilterChip(selected=selected==i,onClick={selected=i},label={Text("${i+1}",fontSize=11.sp)})
                }
            }
            Text(selectedConcept.term,color=Color(0xFFF4E0AA),fontSize=17.sp,fontWeight=FontWeight.Bold)
            Text(selectedConcept.definition,color=Color(0xFFE1F0E7),fontSize=13.sp,lineHeight=20.sp)
            Text("Connect this idea with the other five. A line means a study relationship, not automatically logical entailment.",color=Color(0xFFB0C9C1),fontSize=11.sp)
        }
    }
}
