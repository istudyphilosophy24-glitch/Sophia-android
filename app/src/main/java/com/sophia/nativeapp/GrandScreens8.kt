package com.sophia.nativeapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val night=Color(0xFF0E2032)
private val evergreen=Color(0xFF174C3D)
private val oldGold=Color(0xFFE9C788)
private val paper=Color(0xFFFAF4E8)
private val cream=Color(0xFFFFFCF5)
private val body=Color(0xFF263D40)
private val faint=Color(0xFF698079)
private val goldDark=Color(0xFFB78A4A)

@Composable private fun StoryImage8(drawable:Int,modifier:Modifier=Modifier,height:Int=240,shade:Color=night,content:@Composable BoxScope.()->Unit){
    Box(modifier.fillMaxWidth().height(height.dp).clip(RoundedCornerShape(bottomStart=24.dp,bottomEnd=24.dp))){
        Image(painterResource(drawable),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent,shade.copy(alpha=.17f),shade.copy(alpha=.93f)))))
        content()
    }
}
@Composable private fun SmallCaps8(label:String){Text(label.uppercase(),color=goldDark,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)}
@Composable private fun Serif8(text:String,size:Int=27,color:Color=body){Text(text,fontFamily=FontFamily.Serif,fontSize=size.sp,lineHeight=(size+3).sp,color=color)}

@Composable internal fun GrandDock8(page:GrandPage8,onNavigate:(GrandPage8)->Unit,onProfile:()->Unit){
    Surface(shadowElevation=20.dp,color=night){
        Row(Modifier.fillMaxWidth().height(68.dp).padding(horizontal=6.dp),verticalAlignment=Alignment.CenterVertically){
            val tabs=listOf(Triple(GrandPage8.CAMPUS,"Campus",Icons.Rounded.AccountBalance),Triple(GrandPage8.AUDIO,"Listen",Icons.Rounded.Headphones),Triple(GrandPage8.LIBRARY,"Library",Icons.Rounded.AutoStories),Triple(GrandPage8.ODYSSEY,"Odyssey",Icons.Rounded.Explore))
            tabs.forEach{(destination,name,icon)->
                val active=page==destination
                Column(Modifier.weight(1f).fillMaxHeight().clickable{onNavigate(destination)}.padding(top=8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)){
                    Icon(icon,null,modifier=Modifier.size(24.dp),tint=if(active)oldGold else Color(0xFFA0B5BE))
                    Text(name,fontSize=11.sp,color=if(active)oldGold else Color(0xFFB3C2C5),fontWeight=if(active)FontWeight.Bold else FontWeight.Normal)
                    if(active)Box(Modifier.size(width=23.dp,height=3.dp).clip(CircleShape).background(oldGold))
                }
            }
        }
    }
}
@Composable private fun FrontFeature8(icon:Int,heading:String,caption:String,onClick:()->Unit){
    Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(22.dp)).clickable{onClick()}){
        Image(painterResource(icon),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(night.copy(alpha=.93f),night.copy(alpha=.56f),Color.Transparent))))
        Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(.72f).padding(19.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
            Text(heading,color=Color.White,fontFamily=FontFamily.Serif,fontSize=24.sp,lineHeight=27.sp)
            Text(caption,color=Color(0xFFE1E4DE),fontSize=12.sp,lineHeight=17.sp)
            Row(verticalAlignment=Alignment.CenterVertically){Text("ENTER",fontSize=10.sp,color=oldGold,letterSpacing=2.sp,fontWeight=FontWeight.Bold);Icon(Icons.Rounded.ArrowForward,null,tint=oldGold,modifier=Modifier.size(18.dp))}
        }
    }
}
@Composable fun GrandCampus8(progress:SophiaProgress,books:List<PrimaryBook>,onAudio:()->Unit,onLibrary:()->Unit,onOdyssey:()->Unit,onScholar:()->Unit,onSocratic:()->Unit){
    Column(Modifier.fillMaxSize().background(paper).verticalScroll(rememberScrollState())){
        StoryImage8(R.drawable.grand_campus,height=355){
            Row(Modifier.align(Alignment.TopEnd).padding(13.dp),verticalAlignment=Alignment.CenterVertically){
                Surface(onClick=onScholar,color=night.copy(alpha=.78f),shape=CircleShape){Icon(Icons.Rounded.AccountCircle,"Profile",Modifier.padding(8.dp).size(25.dp),tint=oldGold)}
            }
            Column(Modifier.align(Alignment.BottomStart).padding(start=22.dp,end=18.dp,bottom=32.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                Text("✦ THE GRAND ACADEMY",color=oldGold,fontWeight=FontWeight.Bold,fontSize=11.sp,letterSpacing=2.5.sp)
                Text("SOPHIA",color=Color.White,fontSize=49.sp,fontFamily=FontFamily.Serif,letterSpacing=2.sp)
                Text("A world of philosophy to explore, read, and hear.",fontSize=15.sp,color=Color(0xFFFAF0D8),lineHeight=22.sp)
            }
        }
        Column(Modifier.padding(horizontal=18.dp,vertical=18.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
            SmallCaps8("Your intellectual journey")
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                listOf(Triple("${progress.completed.size}","LESSONS",Icons.Rounded.LocalLibrary),Triple("${progress.streak}","DAY STREAK",Icons.Rounded.LocalFireDepartment),Triple("${progress.xp}","XP EARNED",Icons.Rounded.WorkspacePremium)).forEach{(value,label,icon)->
                    Surface(Modifier.weight(1f),color=cream,shape=RoundedCornerShape(17.dp),shadowElevation=1.dp){
                        Column(Modifier.padding(vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)){
                            Icon(icon,null,modifier=Modifier.size(20.dp),tint=goldDark)
                            Text(value,color=evergreen,fontSize=20.sp,fontWeight=FontWeight.SemiBold)
                            Text(label,color=faint,fontSize=9.sp,fontWeight=FontWeight.Bold)
                        }
                    }
                }
            }
            SmallCaps8("The academy is alive")
            FrontFeature8(R.drawable.grand_audio,"Continue listening","Explore curated thematic seminars and hands-free philosophical radio.",onAudio)
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
                Box(Modifier.weight(1f)){FrontFeature8(R.drawable.grand_library,"The Library","${books.sumOf{it.excerpts.size}} source excerpts",onLibrary)}
                Box(Modifier.weight(1f)){FrontFeature8(R.drawable.grand_odyssey,"The Odyssey","30 philosophical worlds",onOdyssey)}
            }
            Surface(color=Color(0xFFE9E5D7),shape=RoundedCornerShape(21.dp),modifier=Modifier.fillMaxWidth()){
                Row(Modifier.clickable{onSocratic()}.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
                    Icon(Icons.Rounded.RecordVoiceOver,null,tint=evergreen,modifier=Modifier.size(32.dp))
                    Column(Modifier.weight(1f).padding(start=12.dp)){
                        Text("The Socratic Chamber",fontFamily=FontFamily.Serif,fontSize=21.sp,color=evergreen)
                        Text("Practice open-ended inquiry without a mini-game.",fontSize=12.sp,color=faint)
                    }
                    Icon(Icons.Rounded.ChevronRight,null,tint=evergreen)
                }
            }
            Text("30 disciplines · 300 chapters · 420 historical excerpts · 20 thematic seminars · 30 course lectures",fontSize=11.sp,color=faint,lineHeight=19.sp)
            Spacer(Modifier.height(12.dp))
        }
    }
}
@Composable fun GrandOdyssey8(courses:List<Course>,progress:SophiaProgress,onCourse:(Int)->Unit){
    Column(Modifier.fillMaxSize().background(paper)){
        LazyColumn(contentPadding=PaddingValues(bottom=20.dp)){
            item{StoryImage8(R.drawable.grand_odyssey,height=255){
                Column(Modifier.align(Alignment.BottomStart).padding(22.dp)){
                    Text("THIRTY WORLDS",color=oldGold,letterSpacing=3.sp,fontWeight=FontWeight.Bold,fontSize=11.sp)
                    Text("The Odyssey",color=Color.White,fontSize=39.sp,fontFamily=FontFamily.Serif)
                    Text("From the Presocratics to Schopenhauer",color=Color(0xFFE3EDE6),fontSize=13.sp)
                }
            }}
            items(courses.size){i->
                val course=courses[i]
                val done=progress.completed.count{it.startsWith("${course.id}:")}
                val img=when(i%6){0->R.drawable.grand_cover_0;1->R.drawable.grand_cover_1;2->R.drawable.grand_cover_2;3->R.drawable.grand_cover_3;4->R.drawable.grand_cover_4;else->R.drawable.grand_cover_5}
                Surface(onClick={onCourse(i)},color=cream,shape=RoundedCornerShape(20.dp),modifier=Modifier.padding(horizontal=15.dp,vertical=6.dp).fillMaxWidth(),shadowElevation=1.dp){
                    Row(Modifier.height(122.dp),verticalAlignment=Alignment.CenterVertically){
                        Box(Modifier.width(124.dp).fillMaxHeight()){
                            Image(painterResource(img),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                            Box(Modifier.fillMaxSize().background(night.copy(alpha=.10f)))
                            Text("${i+1}".padStart(2,'0'),modifier=Modifier.align(Alignment.BottomStart).padding(9.dp),color=Color.White,fontSize=28.sp,fontFamily=FontFamily.Serif)
                        }
                        Column(Modifier.weight(1f).padding(horizontal=13.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
                            Text("PHILOSOPHICAL WORLD ${i+1}",color=goldDark,fontSize=9.sp,letterSpacing=1.sp)
                            Text(course.name,color=body,fontSize=19.sp,fontFamily=FontFamily.Serif,maxLines=2,lineHeight=21.sp)
                            LinearProgressIndicator(progress={done/120f},color=evergreen,trackColor=Color(0xFFE1E8D8),modifier=Modifier.fillMaxWidth().height(5.dp))
                            Text("$done / 120 lessons completed",color=faint,fontSize=11.sp)
                        }
                        Icon(Icons.Rounded.KeyboardArrowRight,null,tint=evergreen,modifier=Modifier.padding(end=7.dp))
                    }
                }
            }
        }
    }
}
@Composable fun GrandCourse8(course:Course,courseIndex:Int,progress:SophiaProgress,onLesson:(Int,Int)->Unit,onCommentary:(Int)->Unit,onSocratic:()->Unit){
    val img=when(courseIndex%6){0->R.drawable.grand_cover_0;1->R.drawable.grand_cover_1;2->R.drawable.grand_cover_2;3->R.drawable.grand_cover_3;4->R.drawable.grand_cover_4;else->R.drawable.grand_cover_5}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(paper)){
        StoryImage8(img,height=240){
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)){
                Text("WORLD ${courseIndex+1}",fontSize=12.sp,color=oldGold,letterSpacing=2.sp)
                Text(course.name,fontSize=33.sp,color=Color.White,fontFamily=FontFamily.Serif,lineHeight=37.sp)
            }
        }
        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
            Text(course.description,fontSize=15.sp,color=faint,lineHeight=23.sp)
            SmallCaps8("The study path")
            Text("Ten chapters. Twelve guided encounters each. No mini-games—read, consider, question, and progress.",fontSize=13.sp,color=body,lineHeight=21.sp)
            course.units.forEachIndexed{ui,u->
                val done=(0..11).count{progress.isCompleted(unitKey(course.id,ui,it))}
                Surface(color=cream,shape=RoundedCornerShape(20.dp),shadowElevation=2.dp,modifier=Modifier.fillMaxWidth()){
                    Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                        Row(verticalAlignment=Alignment.CenterVertically){
                            Text("${ui+1}".padStart(2,'0'),fontFamily=FontFamily.Serif,fontSize=28.sp,color=goldDark)
                            Column(Modifier.weight(1f).padding(start=14.dp)){
                                Text(u.name,color=evergreen,fontFamily=FontFamily.Serif,fontSize=21.sp)
                                Text("$done / 12 readings and examinations",color=faint,fontSize=11.sp)
                            }
                            IconButton(onClick={onCommentary(ui)}){Icon(Icons.Rounded.MenuBook,"Chapter commentary",tint=evergreen)}
                        }
                        LinearProgressIndicator(progress={done/12f},modifier=Modifier.fillMaxWidth().height(5.dp),color=evergreen)
                        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                            (0..11).forEach{li->
                                val complete=progress.isCompleted(unitKey(course.id,ui,li))
                                Surface(onClick={onLesson(ui,li)},shape=RoundedCornerShape(12.dp),color=if(complete)evergreen else if(li==11)Color(0xFFF3E3C0) else Color(0xFFEBF0E7)){
                                    Box(Modifier.size(width=46.dp,height=45.dp),contentAlignment=Alignment.Center){
                                        if(li==11)Icon(Icons.Rounded.WorkspacePremium,null,tint=if(complete)Color.White else goldDark,modifier=Modifier.size(22.dp))
                                        else Text(if(complete)"✓" else "${li+1}",fontWeight=FontWeight.Bold,color=if(complete)Color.White else evergreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            OutlinedButton(onClick=onSocratic,modifier=Modifier.fillMaxWidth()) {Icon(Icons.Rounded.RecordVoiceOver,null);Text(" Socratic examination")}
        }
    }
}
@Composable fun GrandLesson8(course:Course,unit:UnitLesson,plan:LessonPlan,lesson:Int,onPractice:()->Unit,onCommentary:()->Unit,onSpeak:()->Unit,onLibrary:()->Unit){
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(paper)){
        StoryImage8(R.drawable.grand_library,height=202){
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)){
                Text("STUDY ENCOUNTER ${lesson+1} / 12",color=oldGold,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.7.sp)
                Text(plan.title,fontFamily=FontFamily.Serif,fontSize=27.sp,lineHeight=31.sp,color=Color.White)
            }
        }
        Column(Modifier.padding(horizontal=20.dp,vertical=22.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
            SmallCaps8(plan.mode)
            Text(plan.objective,fontFamily=FontFamily.Serif,fontSize=22.sp,lineHeight=30.sp,color=evergreen)
            HorizontalDivider(color=Color(0xFFD7DACD))
            Text(plan.introduction,fontSize=17.sp,lineHeight=29.sp,color=body)
            if(lesson in 6..10){
                Text("A deeper examination",fontFamily=FontFamily.Serif,fontSize=23.sp,color=evergreen)
                Text(when(lesson){6->unit.reading.intermediate;7->unit.reading.advanced;8->unit.reading.objection;9->unit.reading.thesis;else->unit.reading.foundation},fontSize=16.sp,lineHeight=26.sp,color=body)
            }
            Surface(color=Color(0xFFE7EAD9),shape=RoundedCornerShape(18.dp)){
                Column(Modifier.padding(17.dp)){
                    SmallCaps8("Your question")
                    Text(plan.task,fontFamily=FontFamily.Serif,fontSize=19.sp,lineHeight=26.sp,color=evergreen)
                }
            }
            SmallCaps8("A few concepts to carry forward")
            plan.focus.take(4).forEach{c->
                Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
                    Text(c.term,color=evergreen,fontWeight=FontWeight.Bold,fontSize=16.sp)
                    Text(c.definition,color=body,fontSize=14.sp,lineHeight=21.sp)
                };HorizontalDivider(color=Color(0xFFE5E7D8))
            }
            Button(onClick=onSpeak,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)){
                Icon(Icons.Rounded.VolumeUp,null);Text(" Listen to this encounter")
            }
            Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){
                OutlinedButton(onClick=onCommentary,modifier=Modifier.weight(1f)){Text("Commentary",fontSize=12.sp)}
                OutlinedButton(onClick=onLibrary,modifier=Modifier.weight(1f)){Text("Primary texts",fontSize=12.sp)}
            }
            Button(onClick=onPractice,modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(16.dp)){
                Text(if(lesson==11)"Enter philosophical examination" else "Check your understanding")
                Icon(Icons.Rounded.ArrowForward,null)
            }
        }
    }
}
@Composable fun GrandLibrary8(books:List<PrimaryBook>,onOpen:(Int)->Unit,onScholar:()->Unit){
    var search by remember{mutableStateOf("")}
    val filtered=books.withIndex().filter{(_,b)->search.isBlank()||b.title.contains(search,true)||b.author.contains(search,true)||b.tradition.contains(search,true)}
    Column(Modifier.fillMaxSize().background(paper)){
        LazyColumn(contentPadding=PaddingValues(bottom=24.dp)){
            item{StoryImage8(R.drawable.grand_library,height=261){
                Column(Modifier.align(Alignment.BottomStart).padding(22.dp)){
                    Text("GREAT READER 4.0",color=oldGold,letterSpacing=3.sp,fontSize=11.sp,fontWeight=FontWeight.Bold)
                    Text("The Great Library",fontFamily=FontFamily.Serif,fontSize=34.sp,color=Color.White)
                    Text("${books.sumOf{it.excerpts.size}} original excerpts · annotated reading",color=Color(0xFFDBE3DC),fontSize=13.sp)
                }
            }}
            item{
                OutlinedTextField(value=search,onValueChange={search=it},singleLine=true,label={Text("Search by author, tradition or work")},
                    leadingIcon={Icon(Icons.Rounded.Search,null)},modifier=Modifier.fillMaxWidth().padding(16.dp),shape=RoundedCornerShape(16.dp))
                SmallCaps8("Curated primary reading")
                Spacer(Modifier.height(8.dp))
            }
            items(filtered.size){idx->
                val (i,b)=filtered[idx]
                Surface(onClick={onOpen(i)},modifier=Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=5.dp),color=cream,shape=RoundedCornerShape(17.dp),shadowElevation=1.dp){
                    Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){
                        Box(Modifier.size(85.dp).clip(RoundedCornerShape(12.dp))){
                            val img=when(i%9){0->R.drawable.grand_scholar_0;1->R.drawable.grand_scholar_1;2->R.drawable.grand_scholar_2;3->R.drawable.grand_scholar_3;4->R.drawable.grand_scholar_4;5->R.drawable.grand_scholar_5;6->R.drawable.grand_scholar_6;7->R.drawable.grand_scholar_7;else->R.drawable.grand_scholar_8}
                            Image(painterResource(img),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                        }
                        Column(Modifier.weight(1f).padding(start=13.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
                            Text(b.author.uppercase(),color=goldDark,fontSize=10.sp,letterSpacing=1.sp)
                            Text(b.title,color=evergreen,fontSize=19.sp,lineHeight=22.sp,fontFamily=FontFamily.Serif)
                            Text("${b.excerpts.size} excerpts · ${b.tradition}",color=faint,fontSize=11.sp)
                        }
                        Icon(Icons.Rounded.KeyboardArrowRight,null,tint=evergreen)
                    }
                }
            }
            item{Text("Historical translations require careful source checks. A second translation is not bundled unless rights are verified; users may import their own lawful text.",modifier=Modifier.padding(19.dp),fontSize=11.sp,lineHeight=18.sp,color=faint)}
        }
    }
}
@Composable fun GrandScholar8(progress:SophiaProgress,onRadio:()->Unit,onResearch:()->Unit,onSocratic:()->Unit,onDoctoral:()->Unit,onAtlas:()->Unit,onMastery:()->Unit,onCollection:()->Unit,onSettings:()->Unit){
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(paper)){
        StoryImage8(R.drawable.grand_campus,height=229){
            Column(Modifier.align(Alignment.BottomStart).padding(19.dp)){
                Text("THE SCHOLAR'S QUARTERS",color=oldGold,fontSize=11.sp,letterSpacing=2.sp)
                Text("Your Academy",color=Color.White,fontSize=36.sp,fontFamily=FontFamily.Serif)
            }
        }
        Column(Modifier.padding(17.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Text("${progress.completed.size} lessons completed · ${progress.streak}-day streak · ${progress.xp} experience points",color=faint,fontSize=13.sp)
            listOf(
                Triple("The Audio Hall","Your listening library and thematic seminars",onRadio),
                Triple("Socratic Chamber","A serious dialogue with branching objections",onSocratic),
                Triple("Doctoral Examination","Extended philosophical questions and self-assessment",onDoctoral),
                Triple("Research Institute","Draft, annotate, revise and compare philosophical positions",onResearch),
                Triple("Knowledge Atlas","Connections between philosophical traditions",onAtlas),
                Triple("Mastery Dashboard","Review how learning and retention develop",onMastery),
                Triple("The Philosopher Collection","Illustrated achievements from completed study",onCollection),
                Triple("Voice & Accessibility","Select Android TTS engine and voice settings",onSettings)
            ).forEach{(name,desc,action)->
                Surface(onClick=action,modifier=Modifier.fillMaxWidth(),color=cream,shape=RoundedCornerShape(18.dp)){
                    Row(Modifier.padding(15.dp),verticalAlignment=Alignment.CenterVertically){
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){
                            Text(name,color=evergreen,fontFamily=FontFamily.Serif,fontSize=21.sp)
                            Text(desc,color=faint,fontSize=12.sp)
                        }
                        Icon(Icons.Rounded.ArrowForward,null,tint=goldDark)
                    }
                }
            }
        }
    }
}