package com.sophia.nativeapp

import android.os.Bundle
import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

private val green = Color(0xFF176647)
private val parchment = Color(0xFFFAF9F2)
private val ink = Color(0xFF263D32)
private val sub = Color(0xFF64736B)

data class Concept(val term:String, val definition:String)
data class Course(val title:String, val symbol:String, val concepts:List<Concept>)

// Deliberately small, hand-authored NATIVE PREVIEW bank. The complete SOPHIA 4
// curriculum is in the source archive, not yet migrated to this GitHub preview.
private val seed = listOf(
"Pre-Socratic Philosophy|🌊|Archê~First explanatory principle of nature;Physis~The domain and order of nature;Logos~An account open to rational discussion",
"Socratic Philosophy|🏛️|Elenchus~Testing beliefs through questions and counterexamples;Aporia~A state of productive philosophical puzzlement;Virtue~Excellence of character considered in relation to knowledge",
"Platonic Philosophy|☀️|Forms~Intelligible realities central to Plato's accounts of knowledge;Recollection~The soul's recovery of intelligible knowledge;Dialectic~Reasoned inquiry toward first principles",
"Aristotelian Philosophy|📜|Four causes~Material formal efficient and final explanatory factors;Substance~A primary subject of predication and change;Syllogism~An inference governed by the structure of categorical claims",
"Medieval Philosophy|⛪|Universals~The status of properties shared by many particulars;Analogy~Predication across different orders without identical meaning;Faith and reason~The relation between revealed belief and rational inquiry",
"The Scientific Revolution|🔭|Experiment~A controlled investigation of natural phenomena;Mechanism~An explanation through matter motion and interactions;Induction~Reasoning from observations to broader claims",
"Phenomenology|🌿|Intentionality~Consciousness is consciousness of something;Epoché~Suspension of natural-attitude commitments for reflection;Lifeworld~The pretheoretical world of lived experience",
"Existentialism|🕯️|Authenticity~Taking responsibility for one's own possibilities;Anxiety~A mood disclosing possibility and freedom;Existence precedes essence~Sartre's claim about human self-making",
"Hermeneutics|📖|Hermeneutic circle~Understanding parts through wholes and wholes through parts;Fusion of horizons~Transformation of understanding through encounter;Tradition~Historically transmitted meanings that inform interpretation",
"Vedantic Philosophy|🪷|Brahman~Ultimate reality understood differently among Vedānta schools;Ātman~The self whose relation to Brahman is disputed;Māyā~A concept used differently to explain appearance and ignorance",
"General Indian Philosophy|🕉️|Pramāṇa~A recognized means of acquiring knowledge;Dharma~Normative order or duty with context-dependent meaning;Mokṣa~Liberation from binding conditions of existence",
"Kantian Transcendental Philosophy|🧭|Synthetic a priori~Knowledge informative yet not grounded in particular experience;Categories~Pure concepts structuring possible experience;Transcendental deduction~Kant's argument for the objective validity of categories",
"Logic|🔗|Validity~Impossible for true premises to yield a false conclusion;Soundness~A valid argument with true premises;Quantifier~An expression specifying how many objects satisfy a predicate",
"Aesthetics|🎨|Sublime~An experience involving magnitude or power exceeding ordinary representation;Disinterestedness~Kant's account of aesthetic judgment independent of possession;Mimesis~Artistic imitation or representation",
"Psychoanalysis|🪞|Repression~Defensive exclusion of psychical contents from awareness;Unconscious~Mental processes not directly available to awareness;Transference~Displacement of relational patterns onto the analyst",
"Hegelian Philosophy|🦉|Determinate negation~A negation yielding a specific transformed content;Recognition~A relation essential to the development of self-consciousness;Aufhebung~A transformation that both cancels and preserves",
"Post-Structuralism|🧩|Difference~Relational production of meaning;Discourse~Historically structured conditions of what can be said;Power knowledge~The entanglement of practices of power and knowledge",
"Postmodern Philosophy|🌐|Metanarrative~A broad legitimating story about history or knowledge;Simulacrum~An image or representation whose relation to an original is problematic;Performativity~Production or validation through enacted procedures",
"Deconstruction & Derrida|✒️|Différance~Differing and deferring in the production of meaning;Trace~The marks of absent others within apparent presence;Supplement~What adds to yet exposes an apparent lack in an origin",
"Buddhist Metaphysics|☸️|Dependent origination~Phenomena arise in dependence on conditions;Emptiness~Lack of intrinsic independent existence in Madhyamaka;No-self~Denial of a permanent independent personal essence",
"Analytic Philosophy|🧠|Logical form~The inferential structure of a proposition;Definite descriptions~Russell's analysis of descriptions through quantification;Analysis~Clarifying problems by examining concepts and logical relations",
"Philosophy of Language|💬|Sense and reference~Frege's distinction between mode of presentation and referent;Speech act~An action performed through an utterance;Meaning as use~Wittgenstein's emphasis on language practices",
"Epistemology|🔍|Justification~Support for accepting a belief;Gettier problem~Challenge to the sufficiency of justified true belief;Reliabilism~An account of justification or knowledge involving reliable belief formation",
"Philosophy of Mind|🧬|Qualia~Subjective phenomenal aspects of experience;Functionalism~Mental states characterized by functional roles;Intentionality~Aboutness or directedness of mental states",
"Critical Theory|⚖️|Ideology critique~Analysis of ideas that sustain domination;Instrumental reason~Reason oriented toward efficient control of means;Communicative rationality~Rationality oriented toward mutual understanding",
"Philosophy of Science|🧪|Falsifiability~Capacity of a claim to conflict with possible observation;Paradigm~A disciplinary framework organizing normal science;Inference to best explanation~Reasoning from evidence to an explanatory hypothesis",
"Chinese Philosophy|🐉|Dào~Way or path with different uses across traditions;Rén~Confucian humaneness or benevolence;Wúwéi~Action not forced by willful interference",
"Islamic Philosophy|🌙|Necessary existent~A being whose existence does not depend on another;Occasionalism~Doctrine assigning causal efficacy ultimately to God;Intellect~A capacity or principle central to theories of knowing"
)
private val courses by lazy {
    seed.map { row ->
        val p = row.split("|")
        Course(p[0],p[1],p[2].split(";").map { entry ->
            val pieces=entry.split("~",limit=2)
            Concept(pieces[0],pieces[1])
        })
    }
}

class MainActivity : ComponentActivity() {
    private var voice: TextToSpeech? = null
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        voice = TextToSpeech(this) { result ->
            if (result == TextToSpeech.SUCCESS) voice?.language=Locale.UK
        }
        setContent {
            MaterialTheme(colorScheme=lightColorScheme(primary=green,background=parchment,surface=Color.White)) {
                SophiaUi(onRead={ text -> voice?.setSpeechRate(0.88f); voice?.speak(text,TextToSpeech.QUEUE_FLUSH,null,"sophia") })
            }
        }
    }
    override fun onDestroy() { voice?.stop();voice?.shutdown();super.onDestroy() }
}

@Composable
private fun SophiaUi(onRead:(String)->Unit) {
    val context=LocalContext.current
    val prefs=remember { context.getSharedPreferences("sophia_native_preview",Context.MODE_PRIVATE) }
    var page by remember { mutableStateOf("home") }
    var chosen by remember { mutableIntStateOf(0) }
    var lesson by remember { mutableIntStateOf(0) }
    var question by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var xp by remember { mutableIntStateOf(prefs.getInt("xp",0)) }
    var streak by remember { mutableIntStateOf(prefs.getInt("streak",0)) }
    var coins by remember { mutableIntStateOf(prefs.getInt("coins",0)) }
    var completions by remember { mutableStateOf(prefs.getStringSet("done", emptySet())?.toSet() ?: emptySet()) }
    var journal by remember { mutableStateOf(prefs.getString("journal","") ?: "") }
    val course=courses[chosen]
    val target=course.concepts[(question+lesson)%course.concepts.size]
    val options=remember(chosen,lesson,question) {
        val other=(courses.flatMap { it.concepts }.map { it.term }).distinct()
            .filter { it != target.term }
            .shuffled(Random(chosen*999+lesson*29+question)).take(3)
        (other+target.term).shuffled(Random(chosen*31+lesson*17+question))
    }
    val currentKey="${chosen}:${lesson}"
    BackHandler(page != "home") {
        page=when(page) {
            "quiz" -> "reading"
            "reading" -> "course"
            "course" -> "courses"
            else -> "home"
        }
    }
    Column(Modifier.fillMaxSize().background(parchment).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
            TextButton(onClick={page=if(page=="quiz")"reading" else if(page=="reading")"course" else "home"}) { Text(if(page=="home")"🦉" else "‹ Back") }
            Spacer(Modifier.weight(1f))
            Text("SOPHIA",color=green,fontSize=24.sp,fontWeight=FontWeight.ExtraBold)
            Spacer(Modifier.weight(1f))
            Text("🔥 $streak",color=ink)
        }
        HorizontalDivider(color=Color(0xFFE4E9E2))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=18.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            when(page) {
                "home" -> {
                    Header("The Academy of Reason","A native Android philosophy game · early preview")
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        Stat("⚡","$xp","XP",Modifier.weight(1f))
                        Stat("🪙","$coins","Coins",Modifier.weight(1f))
                        Stat("📖","${completions.size}","Lessons",Modifier.weight(1f))
                    }
                    Tile("🗺️","The Odyssey","Explore 28 philosophical disciplines") { page="courses" }
                    Tile("📚","Academy","Read concepts and practice retrieval") { page="courses" }
                    Tile("🧪","Research Institute","Keep a thesis and criticism journal") { page="research" }
                    Text("Preview note: full SOPHIA 4.0 content is not migrated into this repository yet.",color=sub,fontSize=12.sp)
                }
                "courses" -> {
                    Header("Philosophical worlds","Select a tradition to begin")
                    courses.forEachIndexed { index,c ->
                        Tile(c.symbol,c.title,"12 practice checkpoints · ${c.concepts.size} preview concepts") {
                            chosen=index;page="course"
                        }
                    }
                }
                "course" -> {
                    Header("${course.symbol} ${course.title}","12 checkpoints · read before assessment")
                    course.concepts.forEach { ConceptCard(it) }
                    for (i in 0 until 12) {
                        val done=completions.contains("${chosen}:${i}")
                        Tile(if(done)"✅" else "📜","Lesson ${i+1}","Guided reading · recall · correction") {
                            lesson=i;page="reading"
                        }
                    }
                }
                "reading" -> {
                    Header("📖 Lesson ${lesson+1}","Read, reflect, then practice")
                    Text("What is at stake?",fontSize=20.sp,color=ink,fontWeight=FontWeight.Bold)
                    Text("In ${course.title}, the vocabulary below addresses substantive philosophical problems. Understand the distinctions before selecting answers. Explain each idea in your own words, then ask whether its definition amounts to an argument.",color=ink,lineHeight=23.sp)
                    course.concepts.forEach { ConceptCard(it) }
                    Text("🔁 Spaced return",fontWeight=FontWeight.Bold,color=green)
                    Text("Compare ${course.concepts[lesson%3].term} with ${course.concepts[(lesson+1)%3].term}. What problem does each concept help explain?",color=ink)
                    OutlinedButton(onClick={onRead(course.concepts.joinToString(". ") { it.term+". "+it.definition })},modifier=Modifier.fillMaxWidth()) { Text("🔊 Read aloud using Android voice") }
                    Button(onClick={question=0;score=0;selected=null;page="quiz"},modifier=Modifier.fillMaxWidth()) { Text("Begin 8-question practice") }
                }
                "quiz" -> {
                    Header("Recognition practice","Question ${question+1} of 8")
                    LinearProgressIndicator(progress={ (question+1)/8f },modifier=Modifier.fillMaxWidth())
                    Text("Which concept fits this explanation?",fontSize=21.sp,fontWeight=FontWeight.Bold,color=ink)
                    Card(colors=CardDefaults.cardColors(containerColor=Color.White)) {
                        Text(target.definition,modifier=Modifier.padding(18.dp),lineHeight=25.sp,color=ink)
                    }
                    options.forEach { option ->
                        OutlinedCard(onClick={
                            if(selected==null) {
                                selected=option
                                if(option==target.term) score++
                            }
                        },modifier=Modifier.fillMaxWidth(),border=BorderStroke(if(selected==option)2.dp else 1.dp,if(selected==option)green else Color(0xFFDAE3DD))) {
                            Text(option,modifier=Modifier.padding(17.dp),color=ink)
                        }
                    }
                    if(selected!=null) {
                        Text(if(selected==target.term)"✅ Correct!" else "📘 Review this concept",color=green,fontWeight=FontWeight.Bold)
                        Text("${target.term}: ${target.definition}",color=ink)
                    }
                    Button(enabled=selected!=null,onClick={
                        if(question==7) {
                            val now=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date())
                            val last=prefs.getString("lastDate","") ?: ""
                            if(last!=now) {
                                val yesterday=java.util.Calendar.getInstance().apply { add(java.util.Calendar.DATE,-1) }
                                val y=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(yesterday.time)
                                streak=if(last==y)streak+1 else 1
                            }
                            val first=currentKey !in completions
                            completions=completions+currentKey
                            xp+=if(first)score*10+20 else score*2
                            coins+=if(first)score*2+5 else 0
                            prefs.edit().putStringSet("done",completions).putInt("xp",xp).putInt("coins",coins).putInt("streak",streak).putString("lastDate",now).apply()
                            page="finish"
                        } else { question++;selected=null }
                    },modifier=Modifier.fillMaxWidth()) { Text(if(question==7)"Complete lesson" else "Continue") }
                }
                "finish" -> {
                    Spacer(Modifier.height(50.dp))
                    Text("🏆",fontSize=76.sp,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth())
                    Header("Lesson completed!","$score / 8 correct · keep reviewing")
                    Button(onClick={page="course"},modifier=Modifier.fillMaxWidth()) { Text("Return to course") }
                    OutlinedButton(onClick={page="reading"},modifier=Modifier.fillMaxWidth()) { Text("Repeat lesson") }
                }
                "research" -> {
                    Header("🧪 Research Institute","Write a thesis and examine its limits")
                    OutlinedTextField(value=journal,onValueChange={journal=it;prefs.edit().putString("journal",it).apply()},modifier=Modifier.fillMaxWidth().height(260.dp),label={Text("Research journal")},placeholder={Text("Thesis:\nPremises:\nObjections:\nReplies:\nTextual evidence:")})
                    Text("Reasoning checklist",color=green,fontWeight=FontWeight.Bold,fontSize=20.sp)
                    listOf("Define the thesis.","Separate claims from evidence.","Check inferential validity.","State a charitable objection.","Distinguish textual fidelity from your interpretation.").forEach { Text("□ $it",color=ink) }
                    Text("SOPHIA does not automatically certify truth or originality.",fontSize=12.sp,color=sub)
                }
                "profile" -> {
                    Header("Your philosophical progress","Stored locally on this Android device")
                    Stat("⚡","$xp","Experience")
                    Stat("🔥","$streak","Daily streak")
                    Stat("✅","${completions.size}","Completed lessons")
                }
            }
        }
        if(page in listOf("home","courses","research","profile")) {
            HorizontalDivider()
            NavigationBar(containerColor=Color.White) {
                listOf("home" to "Home","courses" to "Courses","research" to "Research","profile" to "Profile").forEach { (key,label) ->
                    NavigationBarItem(selected=page==key,onClick={page=key},icon={Text(if(key=="home")"🏠" else if(key=="courses")"🗺️" else if(key=="research")"🧪" else "👤")},label={Text(label)})
                }
            }
        }
    }
}

@Composable private fun Header(title:String,subtitle:String) {
    Text(title,color=green,fontSize=26.sp,fontWeight=FontWeight.ExtraBold,lineHeight=32.sp)
    Text(subtitle,color=sub,fontSize=14.sp)
}
@Composable private fun Stat(icon:String,value:String,title:String,modifier:Modifier=Modifier) {
    Card(modifier=modifier,colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.fillMaxWidth().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            Text(icon,fontSize=20.sp)
            Text(value,color=green,fontSize=21.sp,fontWeight=FontWeight.Bold)
            Text(title,color=sub,fontSize=11.sp)
        }
    }
}
@Composable private fun Tile(icon:String,title:String,description:String,onClick:()->Unit) {
    ElevatedCard(onClick=onClick,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.elevatedCardColors(containerColor=Color.White)) {
        Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
            Text(icon,fontSize=27.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title,fontSize=17.sp,fontWeight=FontWeight.Bold,color=ink)
                Text(description,fontSize=12.sp,color=sub)
            }
        }
    }
}
@Composable private fun ConceptCard(c:Concept) {
    Card(colors=CardDefaults.cardColors(containerColor=Color.White),modifier=Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(c.term,color=green,fontWeight=FontWeight.Bold,fontSize=17.sp)
            Spacer(Modifier.height(4.dp))
            Text(c.definition,color=ink,lineHeight=22.sp)
        }
    }
}
