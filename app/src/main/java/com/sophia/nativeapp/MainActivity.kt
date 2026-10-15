package com.sophia.nativeapp

import android.content.ClipData
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File

private val ink8=Color(0xFF1A3341)
private val forest8=Color(0xFF194E42)
private val gold8=Color(0xFFC49A55)
private val parchment8=Color(0xFFFAF5E9)
private val ivory8=Color(0xFFFFFCF5)
private val navy8=Color(0xFF0E2032)
private val sage8=Color(0xFFDDE6D7)
private val smoky8=Color(0xFF5D706D)

class MainActivity:ComponentActivity(){
    private lateinit var narrator:NarratorController
    private var sounds:ToneGenerator?=null
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        narrator=NarratorController(this)
        sounds=try{ToneGenerator(AudioManager.STREAM_MUSIC,28)}catch(_:Exception){null}
        val courses=loadCourses(this)
        val books=loadPrimaryBooks(this)
        val progress=SophiaProgress(this)
        setContent{
            MaterialTheme(colorScheme=lightColorScheme(primary=forest8,onPrimary=Color.White,background=parchment8,surface=ivory8,onSurface=ink8)){
                GrandApp8(courses,books,progress,narrator,
                    speak={text,rate,pitch->narrator.speak(text,rate,pitch)},stop={narrator.stop()},
                    buzz={yes->sounds?.startTone(if(yes)ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_NACK,95)},
                    share={title,body->shareReading(title,body)},openSource={url->try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(_:Exception){}})
            }
        }
    }
    private fun shareReading(title:String,body:String){
        try{
            val folder=File(cacheDir,"reader_shares").apply{mkdirs()}
            val filename=title.replace(Regex("[^A-Za-z0-9_-]"),"_").take(50)+".txt"
            val f=File(folder,filename);f.writeText(body)
            val uri=FileProvider.getUriForFile(this,packageName+".files",f)
            val intent=Intent(Intent.ACTION_SEND).apply{
                type="text/plain";putExtra(Intent.EXTRA_SUBJECT,title);putExtra(Intent.EXTRA_TEXT,body)
                putExtra(Intent.EXTRA_STREAM,uri);clipData=ClipData.newUri(contentResolver,title,uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent,"Share with a reading app"))
        }catch(_:Exception){
            try{startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,body)},"Share reading"))}catch(_:Exception){}
        }
    }
    override fun onDestroy(){narrator.release();sounds?.release();super.onDestroy()}
}

internal enum class GrandPage8{CAMPUS,AUDIO,LIBRARY,ODYSSEY,COURSE,LESSON,QUIZ,BOSS,FINISHED,READER,COMMENTARY,SCHOLAR,SOCRATIC,DOCTORAL,RESEARCH,ATLAS,MASTERY,COLLECTION,SETTINGS}
private val mainTabs8=setOf(GrandPage8.CAMPUS,GrandPage8.AUDIO,GrandPage8.LIBRARY,GrandPage8.ODYSSEY)

@Composable
private fun GrandApp8(courses:List<Course>,books:List<PrimaryBook>,progress:SophiaProgress,narrator:NarratorController,
    speak:(String,Float,Float)->Unit,stop:()->Unit,buzz:(Boolean)->Unit,share:(String,String)->Unit,openSource:(String)->Unit){
    var page by remember {mutableStateOf(GrandPage8.CAMPUS)}
    var courseId by remember{mutableIntStateOf(0)}
    var unitId by remember{mutableIntStateOf(0)}
    var lessonId by remember{mutableIntStateOf(0)}
    var bookId by remember{mutableIntStateOf(0)}
    var quizIndex by remember{mutableIntStateOf(0)}
    var picked by remember{mutableStateOf<String?>(null)}
    var score by remember{mutableIntStateOf(0)}
    var finished by remember{mutableStateOf(false)}
    var rate by remember{mutableFloatStateOf(.90f)}
    var pitch by remember{mutableFloatStateOf(.98f)}
    var passed by remember{mutableStateOf(false)}
    var totalTick by remember{mutableIntStateOf(0)}
    var lastTab by remember{mutableStateOf(GrandPage8.CAMPUS)}
    val context=androidx.compose.ui.platform.LocalContext.current
    val mastery=remember { Mastery7(context) }
    val haptic=LocalHapticFeedback.current
    val course=courses[courseId]
    val unit=course.units[unitId]
    val exercises=remember(courseId,unitId,lessonId){createExercises(course,unitId,lessonId)}
    val plan=lessonPlan(course,unitId,lessonId)
    fun to(destination:GrandPage8){if(destination in mainTabs8)lastTab=destination;page=destination}
    fun back(){page=when(page){
        GrandPage8.COURSE->GrandPage8.ODYSSEY
        GrandPage8.LESSON->GrandPage8.COURSE
        GrandPage8.QUIZ,GrandPage8.BOSS->GrandPage8.LESSON
        GrandPage8.FINISHED->GrandPage8.COURSE
        GrandPage8.READER->GrandPage8.LIBRARY
        GrandPage8.COMMENTARY,GrandPage8.SOCRATIC,GrandPage8.DOCTORAL->GrandPage8.COURSE
        GrandPage8.SCHOLAR,GrandPage8.MASTERY,GrandPage8.SETTINGS,GrandPage8.COLLECTION,GrandPage8.RESEARCH,GrandPage8.ATLAS->lastTab
        else->GrandPage8.CAMPUS
    }}
    BackHandler(page!=GrandPage8.CAMPUS){back()}
    fun complete(value:Int,isPass:Boolean){
        score=value;passed=isPass;finished=isPass
        if(isPass){progress.finishLesson(course.id,unitId,lessonId,value,true);totalTick++}
        page=GrandPage8.FINISHED
    }
    Column(Modifier.fillMaxSize().background(parchment8).statusBarsPadding().navigationBarsPadding()){
        if(page !in mainTabs8){
            Row(Modifier.fillMaxWidth().background(ivory8).padding(horizontal=13.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically){
                IconButton(onClick={back()}){Icon(Icons.Rounded.ArrowBack,null,tint=forest8)}
                Column(Modifier.weight(1f)){
                    Text("SOPHIA",fontSize=11.sp,color=gold8,letterSpacing=3.sp,fontWeight=FontWeight.ExtraBold)
                    Text(when(page){GrandPage8.COURSE->course.name;GrandPage8.LESSON->unit.name;GrandPage8.READER->books.getOrNull(bookId)?.title?:"Great Library";GrandPage8.RESEARCH->"Research Institute";else->"The Grand Academy"},fontSize=15.sp,color=ink8,maxLines=1)
                }
                IconButton(onClick={to(GrandPage8.SCHOLAR)}){Icon(Icons.Rounded.AccountCircle,null,tint=forest8)}
            }
        }
        Box(Modifier.weight(1f)){
            when(page){
                GrandPage8.CAMPUS->GrandCampus8(progress,books,onAudio={to(GrandPage8.AUDIO)},onLibrary={to(GrandPage8.LIBRARY)},onOdyssey={to(GrandPage8.ODYSSEY)},onScholar={to(GrandPage8.SCHOLAR)},onSocratic={to(GrandPage8.SOCRATIC)})
                GrandPage8.AUDIO->AudioHall8(onOpenLibrary={to(GrandPage8.LIBRARY)})
                GrandPage8.LIBRARY->GrandLibrary8(books,onOpen={bookId=it;to(GrandPage8.READER)},onScholar={to(GrandPage8.SCHOLAR)})
                GrandPage8.ODYSSEY->GrandOdyssey8(courses,progress,onCourse={courseId=it;to(GrandPage8.COURSE)})
                GrandPage8.COURSE->GrandCourse8(course,courseId,progress,onLesson={ui,li->unitId=ui;lessonId=li;to(GrandPage8.LESSON)},onCommentary={unitId=it;to(GrandPage8.COMMENTARY)},onSocratic={to(GrandPage8.SOCRATIC)})
                GrandPage8.LESSON->GrandLesson8(course,unit,plan,lessonId,
                    onPractice={quizIndex=0;picked=null;score=0;to(if(lessonId==11)GrandPage8.BOSS else GrandPage8.QUIZ)},
                    onCommentary={to(GrandPage8.COMMENTARY)},onSpeak={speak(plan.introduction+". "+plan.focus.joinToString(". "){it.term+". "+it.definition},rate,pitch)},
                    onLibrary={to(GrandPage8.LIBRARY)})
                GrandPage8.QUIZ->{
                    val ex=exercises[quizIndex]
                    Column(Modifier.fillMaxSize().padding(19.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                        Text("REFLECT · DISCOVER · UNDERSTAND",fontSize=10.sp,color=gold8,letterSpacing=1.7.sp,fontWeight=FontWeight.Bold)
                        LinearProgressIndicator(progress={(quizIndex+1f)/exercises.size},modifier=Modifier.fillMaxWidth().height(7.dp),color=forest8)
                        Text("${quizIndex+1} / ${exercises.size} · ${score} correct",color=smoky8,fontSize=13.sp)
                        Text(ex.prompt,fontFamily=FontFamily.Serif,fontSize=23.sp,lineHeight=32.sp,color=ink8)
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
                            ex.choices.forEach { option->
                                val optionColor=when{
                                    picked!=null&&option==ex.answer->sage8
                                    picked==option->Color(0xFFFFE1DB)
                                    else->ivory8
                                }
                                Surface(onClick={
                                    if(picked==null){
                                        picked=option;val okay=option==ex.answer
                                        if(okay)score++
                                        progress.recordAnswer(ex,okay);mastery.record(ex.concept,ex.skill,okay)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress);buzz(okay)
                                    }
                                },shape=RoundedCornerShape(17.dp),color=optionColor,border=BorderStroke(1.dp,Color(0xFFD9DECE)),modifier=Modifier.fillMaxWidth()){
                                    Text(option,Modifier.padding(15.dp),fontSize=15.sp,color=ink8,lineHeight=21.sp)
                                }
                            }
                            if(picked!=null){
                                Text(if(picked==ex.answer)"✓ Accurate interpretation" else "Examine the distinction",fontWeight=FontWeight.Bold,color=forest8)
                                Text(ex.feedback,fontSize=14.sp,color=ink8,lineHeight=23.sp)
                            }
                        }
                        Button(enabled=picked!=null,onClick={
                            if(quizIndex<exercises.lastIndex){quizIndex++;picked=null}
                            else complete(score,score>=if(lessonId>=9)6 else 4)
                        },modifier=Modifier.fillMaxWidth().height(51.dp),shape=RoundedCornerShape(15.dp)){
                            Text(if(quizIndex==exercises.lastIndex)"Finish review" else "Continue")
                        }
                    }
                }
                GrandPage8.BOSS->BossArena(unit,course,courseId,mastery,onFinish={result,success->complete(result,success)},onExit={back()})
                GrandPage8.FINISHED->Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(25.dp),verticalArrangement=Arrangement.spacedBy(16.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    Spacer(Modifier.height(28.dp));Icon(if(passed)Icons.Rounded.WorkspacePremium else Icons.Rounded.AutoStories,null,modifier=Modifier.size(80.dp),tint=gold8)
                    Text(if(passed)"A chapter of your journey" else "A question worth revisiting",fontFamily=FontFamily.Serif,fontSize=29.sp,color=ink8,textAlign=TextAlign.Center)
                    Text(if(passed)"Progress saved. Revisit the argument later to strengthen understanding." else "Study the passage and its commentary again before proceeding.",textAlign=TextAlign.Center,color=smoky8)
                    Text("$score correct moves",fontSize=19.sp,color=forest8)
                    Button(onClick={to(GrandPage8.COURSE)},modifier=Modifier.fillMaxWidth()){Text("Return to the Odyssey")}
                    OutlinedButton(onClick={to(GrandPage8.LESSON)},modifier=Modifier.fillMaxWidth()){Text("Revisit the reading")}
                }
                GrandPage8.READER->PrimaryReader(books[bookId],onSpeak={speak(it,rate,pitch)},onStop=stop,onShare=share,onSource=openSource)
                GrandPage8.COMMENTARY->AcademicCommentaryScreen(course,unit,progress,onSpeak={speak(it,rate,pitch)},onLibrary={to(GrandPage8.LIBRARY)},onRadio={to(GrandPage8.AUDIO)})
                GrandPage8.SOCRATIC->SocraticChamber7(course,unit,progress,onExit={back()})
                GrandPage8.DOCTORAL->DoctoralExamination7(course,unit,progress,onExit={back()})
                GrandPage8.RESEARCH->ResearchLab7(books)
                GrandPage8.ATLAS->KnowledgeAtlas7(courses,onCourse={courseId=it;to(GrandPage8.COURSE)})
                GrandPage8.MASTERY->MasteryDashboard7(mastery,progress.completed.size)
                GrandPage8.COLLECTION->ScholarCollection7(courses,progress)
                GrandPage8.SCHOLAR->GrandScholar8(progress,onRadio={to(GrandPage8.AUDIO)},onResearch={to(GrandPage8.RESEARCH)},onSocratic={to(GrandPage8.SOCRATIC)},onDoctoral={to(GrandPage8.DOCTORAL)},onAtlas={to(GrandPage8.ATLAS)},onMastery={to(GrandPage8.MASTERY)},onCollection={to(GrandPage8.COLLECTION)},onSettings={to(GrandPage8.SETTINGS)})
                GrandPage8.SETTINGS->Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                    Text("Narration studio",fontFamily=FontFamily.Serif,fontSize=29.sp,color=forest8)
                    NarratorSettings(narrator,rate,{rate=it},pitch,{pitch=it})
                    Button(onClick={stop()},modifier=Modifier.fillMaxWidth()){Text("Stop voice")}
                    Text("SOPHIA 8 uses native Android TTS. ElevenReader can receive exported excerpts, but is not assumed to expose a selectable Android speech engine.",fontSize=12.sp,color=smoky8)
                }
            }
        }
        if(page in mainTabs8){
            GrandDock8(page,onNavigate={to(it)},onProfile={to(GrandPage8.SCHOLAR)})
        }
    }
}
