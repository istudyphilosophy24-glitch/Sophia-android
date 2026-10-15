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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import kotlin.math.sin

private val forest=Color(0xFF164D3A)
private val amber=Color(0xFFF7DA91)
private val hues=listOf(0xFF217B7A,0xFF8166AC,0xFF527FAC,0xFFAD844B,0xFF648E6B,0xFF9E6173,0xFF468DA3)
@Composable private fun Scene(variant:Int,modifier:Modifier) {
    Canvas(modifier){
        val w=size.width;val h=size.height
        drawRect(brush=Brush.verticalGradient(listOf(Color(hues[variant%hues.size]),Color(0xFFB8DEC2),Color(0xFFE8D9AF))),size=size)
        drawCircle(Color(0xFFFCE3A6),radius=w*.13f,center=Offset(w*.83f,h*.18f))
        val hill=Path().apply{moveTo(0f,h*.70f);lineTo(w*.25f,h*.23f);lineTo(w*.55f,h*.71f);lineTo(w*.78f,h*.35f);lineTo(w,h*.74f);lineTo(w,h);lineTo(0f,h);close()}
        drawPath(hill,Color(0xFF7EAA8D))
        val foreground=Path().apply{moveTo(0f,h*.85f);quadraticTo(w*.50f,h*.53f,w,h*.81f);lineTo(w,h);lineTo(0f,h);close()}
        drawPath(foreground,Color(0xFF456F53))
        val x=w*.43f;val y=h*.44f;val sw=w*.37f
        drawRect(Color(0xFFEAD5A8),Offset(x,y),androidx.compose.ui.geometry.Size(sw,h*.30f))
        repeat(5){i->drawRect(Color(0xFFF9ECCB),Offset(x+(i+.5f)*sw/5f,y+h*.03f),androidx.compose.ui.geometry.Size(sw/11f,h*.25f))}
        val roof=Path().apply{moveTo(x-sw*.08f,y);lineTo(x+sw*.5f,y-h*.14f);lineTo(x+sw*1.08f,y);close()}
        drawPath(roof,Color(0xFFE0B875))
        drawRect(Color(0xFFF4E6C9),Offset(x-sw*.10f,y+h*.28f),androidx.compose.ui.geometry.Size(sw*1.20f,h*.045f))
        repeat(4){i->val xx=(i*.28f+.09f)*w;drawOval(Color(0xFF1D5141),Offset(xx,h*.48f),androidx.compose.ui.geometry.Size(w*.045f,h*.39f))}
        repeat(7){i->drawCircle(amber,radius=(2+i%3).dp.toPx(),center=Offset(w*((i*17%89)/100f),h*((i*31%43)/100f)))}
    }
}
@Composable fun IllustratedHomeBanner(onExplore:()->Unit) {
    Box(Modifier.fillMaxWidth().height(238.dp).clip(RoundedCornerShape(24.dp))){
        Scene(0,Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent,Color(0xD00C3B2A)))))
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
            Text("✦ THE PHILOSOPHICAL ODYSSEY",color=amber,fontSize=11.sp,fontWeight=FontWeight.Bold)
            Text("Explore the Academy",color=Color.White,fontSize=26.sp,fontWeight=FontWeight.ExtraBold)
            Row(verticalAlignment=Alignment.CenterVertically){
                Text("28 worlds · 280 chapters",color=Color.White,fontSize=13.sp)
                Spacer(Modifier.weight(1f))
                Surface(onClick=onExplore,color=amber,shape=CircleShape){
                    Text("Explore →",Modifier.padding(11.dp),color=forest,fontWeight=FontWeight.Bold)
                }
            }
        }
    }
}
@Composable fun IllustratedCourseTile(course:Course,index:Int,done:Int,onClick:()->Unit){
    ElevatedCard(onClick=onClick,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)){
        Box(Modifier.fillMaxWidth().height(140.dp)){
            Scene(index,Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xE5123B2D),Color(0x88123B2D),Color.Transparent))))
            Row(Modifier.fillMaxSize().padding(14.dp),verticalAlignment=Alignment.CenterVertically){
                Text(course.icon,fontSize=35.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)){
                    Text("WORLD "+(index+1),fontSize=10.sp,color=amber,fontWeight=FontWeight.ExtraBold)
                    Text(course.name,fontSize=18.sp,color=Color.White,fontWeight=FontWeight.ExtraBold,lineHeight=21.sp)
                    Text("$done / 120 lessons",color=Color.White,fontSize=12.sp)
                    LinearProgressIndicator(progress=done/120f,modifier=Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),color=amber,trackColor=Color(0x7788AB90))
                }
            }
        }
    }
}
@Composable fun CourseWorldHeader(course:Course,index:Int,done:Int){
    Box(Modifier.fillMaxWidth().height(197.dp).clip(RoundedCornerShape(20.dp))){
        Scene(index,Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent,Color(0xDA123C2F)))))
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
            Text("✦ WORLD "+(index+1),color=amber,fontSize=11.sp,fontWeight=FontWeight.Bold)
            Text(course.name,color=Color.White,fontSize=25.sp,fontWeight=FontWeight.ExtraBold,lineHeight=28.sp)
            Text("$done / 120 quests finished",color=Color.White,fontSize=12.sp)
            LinearProgressIndicator(progress=done/120f,modifier=Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),color=amber,trackColor=Color(0x7788AB90))
        }
    }
}
@Composable fun IllustratedUnitPath(unit:UnitLesson,course:Course,unitNumber:Int,isDone:(Int)->Boolean,onEnter:(Int)->Unit){
    ElevatedCard(shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth(),colors=CardDefaults.elevatedCardColors(containerColor=Color.White)){
        Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Text("CHAPTER "+(unitNumber+1),color=Color(0xFFB68B43),fontSize=11.sp,fontWeight=FontWeight.Bold)
            Text(unit.name,color=forest,fontWeight=FontWeight.ExtraBold,fontSize=19.sp)
            Text(unit.concepts.take(4).joinToString(" · "){it.term},fontSize=11.sp,color=Color(0xFF687E71))
            (0..2).forEach{r->
                Row(Modifier.fillMaxWidth().padding(start=if(r%2==0)0.dp else 14.dp,end=if(r%2==0)14.dp else 0.dp),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){
                    (0..3).forEach{j->
                        val n=r*4+j;val done=isDone(n);val boss=n==11
                        val fill=if(done)Color(0xFF43A978)else if(boss)Color(0xFFF0BB60)else Color(0xFFE1F2E4)
                        Box(Modifier.size(if(boss)56.dp else 48.dp).shadow(3.dp,CircleShape).clip(CircleShape).background(fill).border(3.dp,Color.White,CircleShape).clickable{onEnter(n)},contentAlignment=Alignment.Center){
                            Text(if(done)"✓"else if(boss)"⚔"else ""+(n+1),color=if(done)Color.White else forest,fontWeight=FontWeight.ExtraBold,fontSize=if(boss)21.sp else 16.sp)
                        }
                        if(j<3)Text("·",fontSize=23.sp,color=Color(0xFF83AE90))
                    }
                }
                if(r<2)Text("﹀",modifier=Modifier.fillMaxWidth(),textAlign=if(r==0)TextAlign.End else TextAlign.Start,color=Color(0xFF7AB690))
            }
            Text("⚔ Chapter boss examination",color=Color(0xFF708276),fontSize=11.sp)
        }
    }
}
@Composable fun RewardCelebration(passed:Boolean,score:Int){
    val t=rememberInfiniteTransition(label="reward")
    val drift by t.animateFloat(initialValue=0f,targetValue=1f,animationSpec=infiniteRepeatable(tween(1600),RepeatMode.Reverse),label="drift")
    Box(Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(20.dp)).background(Brush.verticalGradient(listOf(Color(0xFFFFF5D7),Color(0xFFE6F4EA))))){
        Canvas(Modifier.fillMaxSize()){
            val colors=listOf(Color(0xFFE1B74D),Color(0xFF47AF79),Color(0xFF9471C6))
            repeat(38){i->drawCircle(colors[i%3],radius=(3+i%3).dp.toPx(),center=Offset((i*137%997)/997f*size.width,((i*311%991)/991f+drift*.04f)*size.height))}
        }
        Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally){
            Text(if(passed)"🏆" else "📚",fontSize=75.sp)
            Text(if(passed)"QUEST COMPLETE!" else "KEEP LEARNING",fontSize=22.sp,fontWeight=FontWeight.ExtraBold,color=forest)
            Text("$score / 8 correct",fontSize=14.sp,color=Color(0xFF896B38))
        }
    }
}