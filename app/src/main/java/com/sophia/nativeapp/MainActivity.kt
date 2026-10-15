package com.sophia.nativeapp

import android.content.Intent
import android.content.ClipData
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import android.os.Bundle
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val Pine=Color(0xFF136645)
private val Leaf=Color(0xFF2C925C)
private val Cream=Color(0xFFFBFAF5)
private val Ink=Color(0xFF27372E)
private val Muted=Color(0xFF617064)
private val Sky=Color(0xFFE7F3E9)
private val Gold=Color(0xFFB98B38)
private val Border=Color(0xFFE2EAE4)

class MainActivity:ComponentActivity(){
    private lateinit var narration:NarratorController
    private var tones:ToneGenerator?=null
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        narration=NarratorController(this)
        tones=try{ToneGenerator(AudioManager.STREAM_MUSIC,36)}catch(_:Exception){null}
        val courses=loadCourses(this)
        val books=loadPrimaryBooks(this)
        val progress=SophiaProgress(this)
        setContent{
            MaterialTheme(colorScheme=lightColorScheme(primary=Pine,onPrimary=Color.White,background=Cream,surface=Color.White,onSurface=Ink)){
                SophiaApp(courses,books,progress,narration,
                    onSpeak={s,rate,pitch->narration.speak(s,rate,pitch)},
                    onStop={narration.stop()},
                    onTone={correct->tones?.startTone(if(correct)ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_NACK,120)},
                    onShare={shareBackup(it)},
                    onExternalReader={title,body->shareToReader(title,body)},
                    onOpenSource={link->openSource(link)})
            }
        }
    }
    private fun shareBackup(data:String){
        val intent=Intent(Intent.ACTION_SEND).apply{
            type="text/plain";putExtra(Intent.EXTRA_SUBJECT,"SOPHIA Progress Backup");putExtra(Intent.EXTRA_TEXT,data)
        }
        startActivity(Intent.createChooser(intent,"Share progress backup"))
    }
    private fun shareToReader(title:String,body:String){
        try{
            val folder=File(cacheDir,"reader_shares").apply{mkdirs()}
            val f=File(folder,title.replace(Regex("[^A-Za-z0-9_-]"),"_").take(65)+".txt")
            f.writeText(body,Charsets.UTF_8)
            val uri=FileProvider.getUriForFile(this,packageName+".files",f)
            val intent=Intent(Intent.ACTION_SEND).apply{
                type="text/plain"
                putExtra(Intent.EXTRA_STREAM,uri)
                putExtra(Intent.EXTRA_TEXT,body)
                putExtra(Intent.EXTRA_SUBJECT,title)
                clipData=ClipData.newUri(contentResolver,title,uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent,"Send reading to compatible app"))
        }catch(e:Exception){
            val fallback=Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,body)}
            try{startActivity(Intent.createChooser(fallback,"Share reading text"))}catch(_:Exception){}
        }
    }
    private fun openSource(url:String){try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(_:Exception){}}
    override fun onDestroy(){narration.release();tones?.release();super.onDestroy()}
}

private enum class Page { HOME, COURSES, COURSE, READ, QUIZ, BOSS, REFLECTION, FINISH, ACADEMY, LIBRARY, PRIMARY_READER, READING_DESK, SEMINAR, ARGUMENT, REVIEW, RESEARCH, MASTERY, PROFILE, BACKUP }

@Composable
private fun SophiaApp(courses:List<Course>,primaryBooks:List<PrimaryBook>,progress:SophiaProgress,narrator:NarratorController,onSpeak:(String,Float,Float)->Unit,onStop:()->Unit,onTone:(Boolean)->Unit,onShare:(String)->Unit,onExternalReader:(String,String)->Unit,onOpenSource:(String)->Unit) {
    var page by remember { mutableStateOf(Page.HOME) }
    var courseIndex by remember { mutableIntStateOf(0) }
    var unitIndex by remember { mutableIntStateOf(0) }
    var lessonIndex by remember { mutableIntStateOf(0) }
    var questionIndex by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var writing by remember { mutableStateOf("") }
    var reviewText by remember { mutableStateOf("") }
    var journal by remember { mutableStateOf(progress.research()) }
    var researchRubric by remember { mutableStateOf(setOf<Int>()) }
    var seminarStep by remember { mutableIntStateOf(0) }
    var seminarText by remember { mutableStateOf("") }
    var depth by remember { mutableIntStateOf(0) }
    var speakingSpeed by remember { mutableFloatStateOf(0.90f) }
    var speakingPitch by remember { mutableFloatStateOf(0.95f) }
    var soundOn by remember { mutableStateOf(true) }
    var tick by remember { mutableIntStateOf(0) }
    var backupText by remember { mutableStateOf("") }
    var importStatus by remember { mutableStateOf("") }
    var primaryIndex by remember { mutableIntStateOf(0) }
    val haptic=LocalHapticFeedback.current
    val course=courses[courseIndex]
    val unit=course.units[unitIndex]
    val tier=tierFor(lessonIndex)
    val plan=lessonPlan(course,unitIndex,lessonIndex)
    val exercises=remember(courseIndex,unitIndex,lessonIndex) { createExercises(course,unitIndex,lessonIndex) }
    val isBoss=tier==Tier.BOSS
    val lessonKey=unitKey(course.id,unitIndex,lessonIndex)
    val due by remember(tick) { mutableStateOf(progress.dueConcepts()) }
    fun goBack() {
        page=when(page) {
            Page.COURSE->Page.COURSES
            Page.READ->Page.COURSE
            Page.QUIZ,Page.BOSS,Page.REFLECTION->Page.READ
            Page.FINISH->Page.COURSE
            Page.PRIMARY_READER->Page.LIBRARY
            Page.LIBRARY->Page.ACADEMY
            Page.READING_DESK,Page.SEMINAR,Page.ARGUMENT,Page.REVIEW,Page.MASTERY->Page.ACADEMY
            Page.BACKUP->Page.PROFILE
            else->Page.HOME
        }
    }
    BackHandler(page!=Page.HOME){goBack()}
    fun endLesson() {
        val pass = !isBoss && tier!=Tier.SYNTHESIS || score>=6
        if(!pass) {page=Page.FINISH;tick++}
        else {progress.finishLesson(course.id,unitIndex,lessonIndex,score,true);tick++;page=Page.FINISH}
    }
    Column(Modifier.fillMaxSize().background(Cream).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically) {
            if(page==Page.HOME) Text("🦉",fontSize=25.sp)
            else TextButton(onClick={goBack()}) { Text("‹ Back",color=Pine) }
            Spacer(Modifier.weight(1f))
            Text("SOPHIA",fontWeight=FontWeight.ExtraBold,color=Pine,fontSize=22.sp)
            Spacer(Modifier.weight(1f))
            Text("🔥 ${progress.streak}",fontSize=14.sp,color=Ink)
        }
        HorizontalDivider(color=Border)
        Box(Modifier.weight(1f)) {
            when(page) {
                Page.HOME->ScrollFrame {
                    IllustratedHomeBanner(onExplore={page=Page.COURSES})
                    Headline("SOPHIA — Illustrated Odyssey","Adventure through the world of philosophy")
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        SmallStat("⚡","${progress.xp}","XP",Modifier.weight(1f))
                        SmallStat("🪙","${progress.coins}","Coins",Modifier.weight(1f))
                        SmallStat("🏅","${progress.completed.size}","Lessons",Modifier.weight(1f))
                    }
                    DailyQuestBoard(progress.completed.size,progress.streak,progress.xp)
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        Pill("📚 28 courses")
                        Pill("🧠 1,680 concepts")
                    }
                    MenuCard("🗺️","The Odyssey","Worlds, course paths and mastery trials","Explore") {page=Page.COURSES}
                    MenuCard("📚","The Academy","Reading library, seminars and logic arena","Study") {page=Page.ACADEMY}
                    MenuCard("🧪","Research Institute","Defend and revise your own philosophical theories","Investigate") {page=Page.RESEARCH}
                    if(due.isNotEmpty()) {
                        TitleLine("🔁 Your review queue")
                        Text("${due.size} concepts need another encounter. Review past ideas before they fade.",color=Muted)
                        OutlinedButton(onClick={page=Page.REVIEW}) { Text("Review now") }
                    }
                    Text("Native Compose edition · offline by design · no advertisements",fontSize=12.sp,color=Muted)
                }
                Page.COURSES->ScrollFrame {
                    Headline("🗺️ Philosophical worlds","Twenty-eight adventures await")
                    courses.forEachIndexed {i,c->IllustratedCourseTile(c,i,progress.completed.count{it.startsWith("${c.id}:")}){courseIndex=i;page=Page.COURSE}}
                }
                Page.COURSE->ScrollFrame {
                    CourseWorldHeader(course,courseIndex,progress.completed.count{it.startsWith("${course.id}:")})
                    Text(course.description,color=Muted,lineHeight=22.sp)
                    course.units.forEachIndexed{ui,u->
                        IllustratedUnitPath(u,course,ui,
                            isDone={li->progress.isCompleted(unitKey(course.id,ui,li))},
                            onEnter={li->unitIndex=ui;lessonIndex=li;page=Page.READ})
                    }
                }
                Page.READ->ScrollFrame {
                    CourseWorldHeader(course,courseIndex,progress.completed.count{it.startsWith("${course.id}:")})
                    Headline("📖 ${plan.title}","${course.name} · Lesson ${lessonIndex+1}/12 · ${plan.mode}")
                    Pill(if(isBoss)"⚔ Illustrated chapter boss" else "✦ ${tier.label}")
                    Card(colors=CardDefaults.cardColors(containerColor=Sky),shape=RoundedCornerShape(19.dp)){
                        Column(Modifier.fillMaxWidth().padding(17.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
                            Text("YOUR MISSION",color=Pine,fontWeight=FontWeight.ExtraBold,fontSize=11.sp)
                            Text(plan.objective,color=Ink,fontSize=16.sp,lineHeight=23.sp,fontWeight=FontWeight.SemiBold)
                        }
                    }
                    TitleLine("📚 Study before playing")
                    Text(plan.introduction,color=Ink,lineHeight=24.sp,fontSize=15.sp)
                    if(lessonIndex in listOf(0,3,6,9)) {
                        TitleLine(when(lessonIndex){0->"Foundation";3->"Interpretation";6->"Connection";else->"Advanced study"})
                        Text(when(lessonIndex){0->unit.reading.foundation;3->unit.reading.intermediate;else->unit.reading.advanced},color=Ink,lineHeight=24.sp)
                    }
                    TitleLine("🧩 Ideas in this encounter")
                    plan.focus.forEach{ConceptTile(it)}
                    if(unitIndex>0){
                        TitleLine("🔁 Spiral recall")
                        val previous=course.units[unitIndex-1].concepts[(unitIndex+lessonIndex)%course.units[unitIndex-1].concepts.size]
                        ConceptTile(previous)
                        Text("Explain how this earlier concept relates to ${plan.focus.first().term}.",fontSize=12.sp,color=Muted)
                    }
                    Text("✍️ ${plan.task}",color=Pine,fontSize=15.sp,fontWeight=FontWeight.Bold)
                    OutlinedButton(onClick={onSpeak(plan.introduction+". "+plan.focus.joinToString(". "){it.term+". "+it.definition},speakingSpeed,speakingPitch)},modifier=Modifier.fillMaxWidth()){
                        Text("🔊 Read using my selected TTS voice")
                    }
                    OutlinedButton(onClick={onExternalReader(course.name+" - "+plan.title,plan.introduction+"\n\n"+plan.focus.joinToString("\n"){it.term+": "+it.definition})},modifier=Modifier.fillMaxWidth()){
                        Text("🎧 Send reading to another app / ElevenReader")
                    }
                    OutlinedButton(onClick=onStop,modifier=Modifier.fillMaxWidth()){Text("■ Stop narration")}
                    Button(onClick={
                        questionIndex=0;selected=null;score=0;writing="";
                        page=if(isBoss)Page.BOSS else Page.QUIZ
                    },modifier=Modifier.fillMaxWidth().height(54.dp)){
                        Text(if(isBoss)"⚔ ENTER ILLUSTRATED BOSS ARENA" else "✦ START ${plan.mode.uppercase()} CHALLENGE")
                    }
                }
                Page.BOSS-> androidx.compose.runtime.key(courseIndex,unitIndex){
                    BossArena(unit,course,courseIndex,
                        onFinish={points,passed->
                            score=points
                            if(passed)progress.finishLesson(course.id,unitIndex,lessonIndex,points,true)
                            tick++;page=Page.FINISH
                        },onExit={page=Page.READ})
                }
                Page.QUIZ->{
                    val exercise=exercises[questionIndex]
                    Column(Modifier.fillMaxSize().padding(17.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment=Alignment.CenterVertically) {
                            Text(tier.label.uppercase()+" · "+plan.mode.uppercase(),color=Pine,fontWeight=FontWeight.ExtraBold)
                            Spacer(Modifier.weight(1f));Text("${questionIndex+1} / 8 · $score correct",color=Muted)
                        }
                        LinearProgressIndicator(progress=(questionIndex+1)/8f,modifier=Modifier.fillMaxWidth(),color=Leaf)
                        Text(exercise.prompt,color=Ink,fontSize=20.sp,lineHeight=28.sp,fontWeight=FontWeight.SemiBold)
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            exercise.choices.forEach { option ->
                                val hasSelection=selected!=null
                                val state=when {hasSelection&&option==exercise.answer->Color(0xFFE3F8E9);selected==option->Color(0xFFFFE9E5);else->Color.White}
                                OutlinedCard(onClick={
                                    if(selected==null){
                                        selected=option
                                        val ok=option==exercise.answer
                                        if(ok)score++
                                        progress.recordAnswer(exercise,ok)
                                        haptic.performHapticFeedback(if(ok)HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove)
                                        if(soundOn)onTone(ok)
                                    }
                                },colors=CardDefaults.outlinedCardColors(containerColor=state),modifier=Modifier.fillMaxWidth()) {
                                    Text(option,modifier=Modifier.fillMaxWidth().padding(15.dp),color=Ink)
                                }
                            }
                            if(selected!=null) {
                                Text(if(selected==exercise.answer)"✅ Well reasoned" else "📘 Corrective explanation",color=Pine,fontWeight=FontWeight.Bold)
                                Text(exercise.feedback,color=Ink,lineHeight=22.sp)
                            }
                        }
                        Button(enabled=selected!=null,onClick={
                            if(questionIndex<7) {questionIndex++;selected=null}
                            else if(isBoss||tier==Tier.SYNTHESIS||tier==Tier.CRITICISM) {
                                writing="";page=Page.REFLECTION
                            } else endLesson()
                        },modifier=Modifier.fillMaxWidth()) {Text(if(questionIndex==7)"Finish assessment" else "Continue")}
                    }
                }
                Page.REFLECTION->ScrollFrame {
                    Headline("✍️ Philosophical reflection","An answer cannot be graded merely by matching keywords")
                    Text("Based on ${unit.name}, formulate a thesis, give two supporting premises, and consider the strongest objection. Distinguish your own position from the historical author's.",color=Ink,lineHeight=24.sp)
                    OutlinedTextField(value=writing,onValueChange={writing=it},modifier=Modifier.fillMaxWidth().height(230.dp),label={Text("Your argument")},placeholder={Text("Thesis:\nP1:\nP2:\nObjection:\nResponse:")})
                    Text("Self-assessment rubric",fontWeight=FontWeight.Bold,color=Pine)
                    listOf("Clear thesis and defined concepts","Premises offered as reasons","Relevant objection answered","Historical claims separated from personal interpretation").forEach {Text("□ $it",color=Ink)}
                    Text("Self-assessment is reflective, not external verification of philosophical truth.",fontSize=12.sp,color=Muted)
                    Button(enabled=writing.trim().length>=25,onClick={progress.recordReflection(unit.concepts.first().term);endLesson()},modifier=Modifier.fillMaxWidth()) {Text("Record reflection")}
                }
                Page.FINISH->ScrollFrame {
                    RewardCelebration(if(isBoss||tier==Tier.SYNTHESIS)score>=6 else true,score)
                    val passed= if(isBoss||tier==Tier.SYNTHESIS)score>=6 else true
                    Headline(if(passed)"Victory! Intellectual progress earned." else "Your opponent prevailed", "$score / 8 mastery points")
                    if(!passed)Text("Mastery trials require at least six correct answers. Review the reading and try again; no XP or unlock is awarded yet.",color=Ink)
                    else Text("Progress recorded. Revisit this material later for lasting understanding.",color=Ink)
                    Button(onClick={page=Page.COURSE},modifier=Modifier.fillMaxWidth()) {Text("Back to course")}
                    OutlinedButton(onClick={page=Page.READ},modifier=Modifier.fillMaxWidth()) {Text("Review and repeat")}
                }
                Page.ACADEMY->ScrollFrame {
                    Headline("📚 The Academy","Read, reconstruct, criticize and remember")
                    IllustratedHomeBanner(onExplore={page=Page.COURSES})
                    MenuCard("📜","Great Library","39 original source excerpts with questions and glossaries","Read excerpts") {page=Page.LIBRARY}
                    MenuCard("📑","Course Commentary","Deep readings across the 280 units","Study") {page=Page.READING_DESK}
                    MenuCard("🎙️","Socratic Seminar","Four-stage reflection with a transparent rubric","Enter") {seminarStep=0;seminarText=progress.seminarResponse(course.id,unitIndex,0);page=Page.SEMINAR}
                    MenuCard("🧩","Argument Architect","Classify inferences and examine counterexamples","Practice") {page=Page.ARGUMENT}
                    MenuCard("🔁","Spaced Review","Revisit concepts scheduled from past performance","Review") {page=Page.REVIEW}
                    MenuCard("📊","Mastery Dashboard","Track six dimensions of your learning","Examine") {page=Page.MASTERY}
                    Text("For source-based study, prefer annotated texts and check primary editions. The app does not certify contested interpretations.",color=Muted,fontSize=12.sp)
                }
                Page.LIBRARY->PrimaryLibraryIndex(primaryBooks,onOpen={primaryIndex=it;page=Page.PRIMARY_READER},onSource=onOpenSource)
                Page.PRIMARY_READER->PrimaryReader(primaryBooks[primaryIndex],onSpeak={onSpeak(it,speakingSpeed,speakingPitch)},onStop=onStop,onShare=onExternalReader,onSource=onOpenSource)
                Page.READING_DESK->ScrollFrame {
                    Headline("📚 ${unit.name}",course.name)
                    val opts=listOf("Foundation","Intermediate","Advanced")
                    SingleChoiceSegmentedButtonRow(modifier=Modifier.fillMaxWidth()) {
                        opts.forEachIndexed { i,txt->
                            SegmentedButton(selected=depth==i,onClick={depth=i},shape=SegmentedButtonDefaults.itemShape(i,opts.size)) {Text(txt,fontSize=12.sp)}
                        }
                    }
                    val body=when(depth){0->unit.reading.foundation;1->unit.reading.intermediate;else->unit.reading.advanced}
                    Text(body,color=Ink,lineHeight=26.sp)
                    if(unit.reading.thesis.isNotBlank())ConceptTile(Concept("Thesis",unit.reading.thesis))
                    if(unit.reading.source.isNotBlank())Text("Source orientation: ${unit.reading.source}",color=Muted,fontSize=12.sp)
                    TitleLine("📌 Reading annotations")
                    var notes by remember(courseIndex,unitIndex){mutableStateOf(progress.annotation(course.id,unitIndex))}
                    OutlinedTextField(value=notes,onValueChange={notes=it;progress.saveAnnotation(course.id,unitIndex,it)},modifier=Modifier.fillMaxWidth().height(155.dp),label={Text("Your notes and objections")})
                    Button(onClick={onSpeak(body,speakingSpeed,speakingPitch)},modifier=Modifier.fillMaxWidth()) {Text("🔊 Read this commentary")}
                    Text("Passages here are instructional expositions, not necessarily verbatim quotations from philosophers.",fontSize=12.sp,color=Muted)
                }
                Page.SEMINAR->ScrollFrame {
                    Headline("🎙️ Socratic Seminar","${course.name} · ${unit.name}")
                    val stages=listOf("Define the thesis","Reconstruct the argument","State a serious objection","Revise and defend")
                    Text("Stage ${seminarStep+1}/4 — ${stages[seminarStep]}",color=Pine,fontWeight=FontWeight.ExtraBold,fontSize=20.sp)
                    val prompts=listOf(
                        "What is the central philosophical problem here? Define your key terms precisely.",
                        "Provide two premises that support the unit's thesis. Explain how the conclusion follows.",
                        "Give the strongest objection to a particular premise. Why might a careful philosopher accept it?",
                        "Revise your thesis to address the objection without merely restating your conclusion."
                    )
                    Text(prompts[seminarStep],color=Ink,lineHeight=25.sp)
                    if(unit.reading.thesis.isNotBlank())Text("Study orientation: ${unit.reading.thesis}",color=Muted,fontSize=13.sp)
                    OutlinedTextField(value=seminarText,onValueChange={seminarText=it;progress.saveSeminar(course.id,unitIndex,seminarStep,it)},modifier=Modifier.fillMaxWidth().height(200.dp),label={Text("Your answer")})
                    Text("Examiner's checklist: conceptual clarity • relevance • supporting reasons • objection sensitivity",fontSize=13.sp,color=Muted)
                    Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(enabled=seminarStep>0,onClick={seminarStep--;seminarText=progress.seminarResponse(course.id,unitIndex,seminarStep)},modifier=Modifier.weight(1f)) {Text("Previous")}
                        Button(enabled=seminarText.trim().length>=20,onClick={if(seminarStep<3){seminarStep++;seminarText=progress.seminarResponse(course.id,unitIndex,seminarStep)}else page=Page.ACADEMY},modifier=Modifier.weight(1f)) {Text(if(seminarStep==3)"Finish" else "Next stage")}
                    }
                    Text("Offline reflective exercise. No AI semantic grading is claimed.",fontSize=12.sp,color=Muted)
                }
                Page.ARGUMENT->ArgumentArchitect()
                Page.REVIEW->ScrollFrame {
                    Headline("🔁 Spaced Review","Retrieval practice based on earlier answers")
                    if(due.isEmpty())Text("Nothing is due at this moment. Explore new lessons or revisit a favorite course.",color=Ink)
                    else due.forEach { term ->
                        val c=courses.asSequence().flatMap { it.units.asSequence() }.flatMap { it.concepts.asSequence() }.firstOrNull {it.term==term}
                        if(c!=null)ConceptTile(c)
                    }
                    Text("The scheduler uses a simple increasing-interval heuristic, not a validated FSRS model.",fontSize=12.sp,color=Muted)
                }
                Page.RESEARCH->ScrollFrame {
                    Headline("🧪 Research Institute","Construct, criticize and revise a philosophical position")
                    OutlinedTextField(value=journal,onValueChange={journal=it;progress.saveResearch(it)},label={Text("Your research notebook")},placeholder={Text("Research problem:\nThesis:\nBasic distinctions:\nPremises:\nCounterexamples:\nRival interpretations:\nRevision:" )},modifier=Modifier.fillMaxWidth().height(260.dp))
                    TitleLine("Methodological audit")
                    val items=listOf("The thesis is stated clearly","Terms are defined","Conclusions follow from the premises","A serious rival position is considered","Textual evidence is distinguished from interpretation","Unresolved difficulties are acknowledged")
                    items.forEachIndexed {index,t-> Row(verticalAlignment=Alignment.CenterVertically) { Checkbox(checked=index in researchRubric,onCheckedChange={researchRubric=if(it)researchRubric+index else researchRubric-index});Text(t,color=Ink,fontSize=14.sp) } }
                    Text("${researchRubric.size} / ${items.size} checks considered — self-reported, not objectively certified",color=Muted)
                    Text("Your notebook saves automatically on this device.",color=Muted,fontSize=12.sp)
                }
                Page.MASTERY->ScrollFrame {
                    Headline("📊 Intellectual mastery","Skills are tracked separately")
                    val dims=listOf("recognition","comprehension","reconstruction","criticism","synthesis","retention")
                    dims.forEach {skill->
                        val n=progress.skillPercent(skill)
                        Column(verticalArrangement=Arrangement.spacedBy(5.dp)) {
                            Row(verticalAlignment=Alignment.CenterVertically) {Text(skill.replaceFirstChar { it.titlecase() },color=Ink,fontWeight=FontWeight.SemiBold);Spacer(Modifier.weight(1f));Text("$n%",color=Muted)}
                            LinearProgressIndicator(progress=n/100f,modifier=Modifier.fillMaxWidth().height(9.dp),color=Leaf,trackColor=Border)
                        }
                    }
                    Text("These percentages summarize recorded responses; they are not validated psychometric estimates. Synthesis requires human self-assessment.",fontSize=12.sp,color=Muted)
                    TitleLine("Recent learning")
                    Text("${progress.completed.size} completed lessons • ${progress.dueConcepts().size} concepts scheduled for review",color=Ink)
                }
                Page.PROFILE->ScrollFrame {
                    Headline("👤 Your philosophical journey","Private, offline, stored on your device")
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        SmallStat("⚡","${progress.xp}","XP",Modifier.weight(1f))
                        SmallStat("🔥","${progress.streak}","Streak",Modifier.weight(1f))
                        SmallStat("📚","${progress.completed.size}","Lessons",Modifier.weight(1f))
                    }
                    TitleLine("Achievements")
                    Text(if(progress.badges.isEmpty())"Complete a lesson to earn your first badge." else progress.badges.joinToString(" · ") {"🏅 $it"},color=Ink)
                    NarratorSettings(narrator,speakingSpeed,{speakingSpeed=it},speakingPitch,{speakingPitch=it})
                    Row(verticalAlignment=Alignment.CenterVertically) {Switch(checked=soundOn,onCheckedChange={soundOn=it});Spacer(Modifier.width(10.dp));Text("Quiz feedback sounds",color=Ink)}
                    Text("ElevenReader appears as an internal voice only if it exposes an Android TTS engine. To use ElevenReader as an independent reader, share individual lessons or excerpts from their reading screens.",fontSize=12.sp,color=Muted)
                    MenuCard("💾","Back up progress","Share or restore all native progress and notes","Manage") {page=Page.BACKUP}
                }
                Page.BACKUP->ScrollFrame {
                    Headline("💾 Progress backup","Export before changing phones or reinstalling")
                    Button(onClick={onShare(progress.exportBackup())},modifier=Modifier.fillMaxWidth()) {Text("Export native backup via Android Share")}
                    Text("To restore, paste the complete native backup JSON below.",color=Muted,fontSize=13.sp)
                    OutlinedTextField(value=backupText,onValueChange={backupText=it},modifier=Modifier.fillMaxWidth().height(170.dp),label={Text("Paste backup")})
                    OutlinedButton(enabled=backupText.isNotBlank(),onClick={importStatus=if(progress.importBackup(backupText)){tick++;"Backup restored. Restart the app to refresh all screens."}else"Invalid or incompatible backup."},modifier=Modifier.fillMaxWidth()) {Text("Restore progress")}
                    if(importStatus.isNotBlank())Text(importStatus,color=Pine)
                    Text("Legacy WebView SOPHIA 4.0 storage is not automatically readable by this separate native application; a dedicated converter would be required for its JSON export.",color=Muted,fontSize=12.sp)
                }
            }
        }
        if(page in listOf(Page.HOME,Page.COURSES,Page.ACADEMY,Page.RESEARCH,Page.PROFILE)) {
            HorizontalDivider(color=Border)
            NavigationBar(containerColor=Color.White) {
                listOf(Triple("🏠","Home",Page.HOME),Triple("🗺️","Odyssey",Page.COURSES),Triple("📚","Academy",Page.ACADEMY),Triple("🧪","Research",Page.RESEARCH),Triple("👤","Profile",Page.PROFILE)).forEach { (icon,label,destination) ->
                    NavigationBarItem(selected=page==destination,onClick={page=destination},icon={Text(icon)},label={Text(label,fontSize=9.sp)})
                }
            }
        }
    }
}

@Composable private fun ArgumentArchitect() {
    data class LogicalForm(val name:String,val premises:String,val valid:Boolean,val explanation:String)
    val forms=listOf(
        LogicalForm("Modus ponens","If P then Q. P. Therefore Q.",true,"If both premises are true, Q must be true."),
        LogicalForm("Affirming the consequent","If P then Q. Q. Therefore P.",false,"Q could be true for reasons other than P."),
        LogicalForm("Modus tollens","If P then Q. Not Q. Therefore not P.",true,"A true conditional together with not-Q rules out P."),
        LogicalForm("Denying the antecedent","If P then Q. Not P. Therefore not Q.",false,"Not-P does not rule out Q arising in another way.")
    )
    var index by remember {mutableIntStateOf(0)}
    var choice by remember {mutableStateOf<Boolean?>(null)}
    val f=forms[index]
    ScrollFrame {
        Headline("🧩 Argument Architect","Formal validity and counterexamples")
        Text("Puzzle ${index+1} / ${forms.size}",color=Pine,fontWeight=FontWeight.Bold)
        ElevatedCard(colors=CardDefaults.elevatedCardColors(containerColor=Color.White)) {Column(Modifier.padding(18.dp)) {
            Text(f.premises,fontWeight=FontWeight.SemiBold,fontSize=22.sp,color=Ink,lineHeight=31.sp)
        }}
        Text("Does the conclusion follow necessarily from the premises?",color=Ink)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick={choice=true},modifier=Modifier.weight(1f)) {Text("Valid")}
            OutlinedButton(onClick={choice=false},modifier=Modifier.weight(1f)) {Text("Invalid")}
        }
        if(choice!=null) {
            Text(if(choice==f.valid)"✅ Correct" else "📘 Examine the counterexample",color=Pine,fontWeight=FontWeight.Bold)
            Text("${f.name}. ${f.explanation}",color=Ink,lineHeight=24.sp)
            Button(onClick={choice=null;index=(index+1)%forms.size},modifier=Modifier.fillMaxWidth()) {Text("Next inference")}
        }
        Text("This is a finite demonstration of four forms, not a general-purpose theorem prover.",color=Muted,fontSize=12.sp)
    }
}

@Composable private fun ScrollFrame(content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=18.dp,vertical=15.dp),verticalArrangement=Arrangement.spacedBy(13.dp),content=content)
}
@Composable private fun Headline(title:String,subtitle:String) {
    Text(title,color=Pine,fontSize=27.sp,lineHeight=33.sp,fontWeight=FontWeight.ExtraBold)
    Text(subtitle,color=Muted,fontSize=14.sp)
}
@Composable private fun TitleLine(value:String) {Text(value,fontWeight=FontWeight.Bold,color=Ink,fontSize=19.sp)}
@Composable private fun Pill(value:String) {Surface(color=Sky,shape=RoundedCornerShape(50.dp)) {Text(value,modifier=Modifier.padding(horizontal=13.dp,vertical=7.dp),color=Pine,fontSize=12.sp,fontWeight=FontWeight.SemiBold)}}
@Composable private fun SmallStat(icon:String,value:String,label:String,modifier:Modifier=Modifier) {
    ElevatedCard(modifier=modifier,colors=CardDefaults.elevatedCardColors(containerColor=Color.White)) {Column(Modifier.fillMaxWidth().padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Text(icon,fontSize=20.sp);Text(value,color=Pine,fontSize=20.sp,fontWeight=FontWeight.Bold);Text(label,fontSize=11.sp,color=Muted)
    }}
}
@Composable private fun MenuCard(icon:String,title:String,desc:String,cta:String,onClick:()->Unit) {
    ElevatedCard(onClick=onClick,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.elevatedCardColors(containerColor=Color.White)) {
        Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
            Text(icon,fontSize=31.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title,fontSize=17.sp,color=Ink,fontWeight=FontWeight.Bold)
                Text(desc,fontSize=12.sp,color=Muted,lineHeight=18.sp)
                Text("$cta →",fontSize=12.sp,color=Leaf,fontWeight=FontWeight.Bold)
            }
        }
    }
}
@Composable private fun ConceptTile(c:Concept) {
    ElevatedCard(colors=CardDefaults.elevatedCardColors(containerColor=Color.White),shape=RoundedCornerShape(14.dp)) {Column(Modifier.fillMaxWidth().padding(15.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
        Text(c.term,color=Pine,fontWeight=FontWeight.Bold,fontSize=17.sp)
        Text(c.definition,color=Ink,lineHeight=21.sp,fontSize=14.sp)
    }}
}