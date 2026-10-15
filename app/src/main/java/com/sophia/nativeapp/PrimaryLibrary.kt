package com.sophia.nativeapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray
import java.util.Locale

/** Source-specific quotations, not generated summaries or full book dumps. */
data class PrimaryExcerpt(val id:String,val title:String,val location:String,val passage:String,
    val question:String,val glossary:List<String>,val commentary:String)
data class PrimaryBook(val id:String,val title:String,val author:String,val translator:String,
    val tradition:String,val source:String,val url:String,val excerpts:List<PrimaryExcerpt>) {
    val offline:Boolean get()=excerpts.isNotEmpty()
}
fun loadPrimaryBooks(context:Context):List<PrimaryBook> = try {
    val raw=context.assets.open("excerpts.json").bufferedReader().use { it.readText() }
    val arr=JSONArray(raw)
    (0 until arr.length()).map { n ->
        val obj=arr.getJSONObject(n); val ee=obj.getJSONArray("excerpts")
        val es=(0 until ee.length()).map { i ->
            val a=ee.getJSONObject(i);val glossary=a.getJSONArray("glossary")
            PrimaryExcerpt(a.getString("id"),a.getString("title"),a.getString("location"),
                a.getString("passage"),a.getString("question"),
                (0 until glossary.length()).map { glossary.getString(it) },a.getString("commentary"))
        }
        PrimaryBook(obj.getString("id"),obj.getString("title"),obj.getString("author"),
            obj.getString("translator"),obj.getString("tradition"),obj.getString("source"),obj.getString("url"),es)
    }
} catch(_:Exception){ emptyList() }

private val emerald=Color(0xFF155540)
private val amber=Color(0xFFBA8E40)
private val bodyInk=Color(0xFF21382C)

@Composable fun PrimaryLibraryIndex(books:List<PrimaryBook>,onOpen:(Int)->Unit,onSource:(String)->Unit){
    val context=LocalContext.current
    val anthology=context.getSharedPreferences("sophia_anthology_v6",Context.MODE_PRIVATE)
    var savedOnly by remember{mutableStateOf(false)}
    var term by remember{mutableStateOf("")}
    val favorites=anthology.getStringSet("favorites",emptySet())?:emptySet()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
        Surface(shape=RoundedCornerShape(23.dp),color=emerald){
            Column(Modifier.padding(19.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text("✦ THE GREAT LIBRARY",color=Color(0xFFFFD98A),fontSize=11.sp,fontWeight=FontWeight.Bold)
                Text("Read the philosophers",fontSize=26.sp,color=Color.White,fontWeight=FontWeight.ExtraBold)
                Text("${books.sumOf{it.excerpts.size}} curated primary-source excerpts across ${books.size} works",color=Color.White)
                Text("Each includes its translation credit, reading questions, and scholarly context. No complete books are bundled.",fontSize=12.sp,color=Color(0xFFD5EBDD))
            }
        }
        if(books.isEmpty()) Text("The excerpt library is not bundled correctly in this build.")
        OutlinedTextField(term,onValueChange={term=it},label={Text("Search authors, books and excerpt titles")},modifier=Modifier.fillMaxWidth())
        Row(verticalAlignment=Alignment.CenterVertically){
            FilterChip(selected=savedOnly,onClick={savedOnly=!savedOnly},label={Text("★ My anthology")})
            Spacer(Modifier.width(9.dp))
            Text("${favorites.size} saved passages",fontSize=12.sp,color=emerald)
        }
        books.forEachIndexed{ idx,b->
            if((savedOnly && b.excerpts.none{it.id in favorites}) || (term.isNotBlank() &&
                !b.title.contains(term,true) && !b.author.contains(term,true) && b.excerpts.none{it.title.contains(term,true)||it.passage.contains(term,true)}))return@forEachIndexed
            ElevatedCard(onClick={onOpen(idx)},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),colors=CardDefaults.elevatedCardColors(containerColor=Color.White)){
                Row(Modifier.padding(13.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically){
                    PhilosopherAvatar(idx,Modifier.size(79.dp))
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){
                        Text(b.title,fontSize=17.sp,color=emerald,fontWeight=FontWeight.ExtraBold)
                        Text(b.author+" · "+b.tradition,fontSize=12.sp,color=bodyInk)
                        Text("📜 ${b.excerpts.size} primary excerpts · offline",fontSize=11.sp,color=amber,fontWeight=FontWeight.Bold)
                    }
                }
            }
        }
        Text("Passages are drawn from historical editions previously imported from Project Gutenberg; individual selections and accompanying guided analyses are not all independently peer-reviewed. Confirm precise section locations in the edition linked by each work. Rights in your country may differ from U.S. public-domain status.",color=Color.Gray,fontSize=11.sp)
    }
}
@Composable fun PrimaryReader(book:PrimaryBook,onSpeak:(String)->Unit,onStop:()->Unit,onShare:(String,String)->Unit,onSource:(String)->Unit){
    val context=LocalContext.current
    val prefs=remember { context.getSharedPreferences("sophia_primary_v5_excerpts",Context.MODE_PRIVATE) }
    var index by remember(book.id){mutableIntStateOf(prefs.getInt("excerpt_"+book.id,0).coerceIn(0,(book.excerpts.size-1).coerceAtLeast(0)))}
    var fontSize by remember { mutableFloatStateOf(17f) }
    val scroll=rememberScrollState()
    val favoritesPrefs=remember{context.getSharedPreferences("sophia_anthology_v6",Context.MODE_PRIVATE)}
    var favorites by remember{mutableStateOf(favoritesPrefs.getStringSet("favorites",emptySet())?.toSet()?:emptySet())}
    var showCommentary by remember{mutableStateOf(false)}
    var showNotes by remember { mutableStateOf(false) }
    var reflection by remember(book.id,index){mutableStateOf(prefs.getString("note_"+book.id+"_"+index,"")?:"")}
    var status by remember { mutableStateOf("") }
    if(book.excerpts.isEmpty()){
        Column(Modifier.padding(18.dp)){Text("No source excerpts are available.");OutlinedButton(onClick={onSource(book.url)}){Text("Source catalog")}}
        return
    }
    val excerpt=book.excerpts[index]
    LaunchedEffect(index){scroll.scrollTo(0);showCommentary=false}
    Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(17.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("📜 PRIMARY TEXT · CURATED EXCERPT",color=amber,fontSize=11.sp,fontWeight=FontWeight.ExtraBold)
        Text(book.title,color=emerald,fontSize=26.sp,fontWeight=FontWeight.ExtraBold)
        Text(book.author+" · "+book.translator,color=bodyInk,fontSize=12.sp)
        LinearProgressIndicator(progress=(index+1f)/book.excerpts.size,modifier=Modifier.fillMaxWidth().height(8.dp),color=emerald)
        Row(verticalAlignment=Alignment.CenterVertically){
            Text("Excerpt ${index+1} / ${book.excerpts.size}",color=emerald,fontWeight=FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("A",fontSize=12.sp);Slider(value=fontSize,onValueChange={fontSize=it},valueRange=14f..24f,modifier=Modifier.width(110.dp));Text("A",fontSize=20.sp)
        }
        Text(excerpt.title,fontSize=22.sp,fontWeight=FontWeight.Bold,color=emerald)
        Text(excerpt.location,fontSize=12.sp,color=amber,lineHeight=19.sp)
        OutlinedButton(onClick={
            favorites=if(excerpt.id in favorites)favorites-excerpt.id else favorites+excerpt.id
            favoritesPrefs.edit().putStringSet("favorites",favorites).apply()
        },modifier=Modifier.fillMaxWidth()){
            Text(if(excerpt.id in favorites)"★ Saved in My Anthology" else "☆ Save to My Anthology")
        }
        ElevatedCard(shape=RoundedCornerShape(19.dp),colors=CardDefaults.elevatedCardColors(containerColor=Color.White)){
            Column(Modifier.padding(17.dp)){
                Text("ORIGINAL WORDING · HISTORICAL TRANSLATION",fontSize=10.sp,color=amber,fontWeight=FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text(excerpt.passage,color=bodyInk,fontSize=fontSize.sp,lineHeight=(fontSize*1.56f).sp)
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            OutlinedButton(enabled=index>0,onClick={index--;prefs.edit().putInt("excerpt_"+book.id,index).apply()},modifier=Modifier.weight(1f)){Text("‹ Previous")}
            Button(enabled=index<book.excerpts.size-1,onClick={index++;prefs.edit().putInt("excerpt_"+book.id,index).apply()},modifier=Modifier.weight(1f)){Text("Next ›")}
        }
        Text("THINK ABOUT THE TEXT",fontSize=11.sp,color=amber,fontWeight=FontWeight.ExtraBold)
        Text(excerpt.question,fontSize=17.sp,color=emerald,fontWeight=FontWeight.SemiBold,lineHeight=24.sp)
        OutlinedButton(onClick={showCommentary=!showCommentary},modifier=Modifier.fillMaxWidth()){
            Text(if(showCommentary)"Hide scholarly interpretation ⌃" else "Open interpretive commentary ⌄")
        }
        if(showCommentary){
            ElevatedCard(colors=CardDefaults.elevatedCardColors(containerColor=Color(0xFFF2F6EB))){
                Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
                    Text("PASSAGE-SPECIFIC INTERPRETIVE GUIDE",fontSize=11.sp,color=amber,fontWeight=FontWeight.Bold)
                    Text(excerpt.commentary,color=bodyInk,fontSize=14.sp,lineHeight=23.sp)
                    Text("Interpretation is editorial; the passage above alone is primary text. These automated guided notes have not all received scholarly editorial review.",fontSize=11.sp,color=Color.Gray)
                }
            }
        }
        Text("Glossary: "+excerpt.glossary.joinToString(" • "),fontSize=12.sp,color=Color.Gray)
        OutlinedButton(onClick={showNotes=!showNotes},modifier=Modifier.fillMaxWidth()){Text(if(showNotes)"Hide reading notes" else "✍ Add reading annotations")}
        if(showNotes) OutlinedTextField(value=reflection,onValueChange={reflection=it;prefs.edit().putString("note_"+book.id+"_"+index,it).apply()},modifier=Modifier.fillMaxWidth().height(150.dp),label={Text("Interpretation / objection")})
        Button(onClick={onSpeak(excerpt.passage)},modifier=Modifier.fillMaxWidth()){Text("🔊 Read this excerpt with my selected Android voice")}
        OutlinedButton(onClick=onStop,modifier=Modifier.fillMaxWidth()){Text("■ Stop narration")}
        OutlinedButton(onClick={onShare(book.title+" — "+excerpt.title,excerpt.passage)},modifier=Modifier.fillMaxWidth()){Text("🎧 Send this excerpt to a compatible reader")}
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            OutlinedButton(onClick={
                (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(excerpt.title,excerpt.passage));status="Excerpt copied. You can paste it into ElevenReader."
            },modifier=Modifier.weight(1f)){Text("Copy text")}
            OutlinedButton(onClick={onSource(book.url)},modifier=Modifier.weight(1f)){Text("Source ↗")}
        }
        if(status.isNotBlank()) Text(status,color=emerald,fontSize=12.sp)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            OutlinedButton(enabled=index>0,onClick={index--;prefs.edit().putInt("excerpt_"+book.id,index).apply()},modifier=Modifier.weight(1f)){Text("‹ Previous")}
            Button(enabled=index<book.excerpts.size-1,onClick={index++;prefs.edit().putInt("excerpt_"+book.id,index).apply()},modifier=Modifier.weight(1f)){Text("Next ›")}
        }
        Text("Bibliographic source: ${book.source}. Edition: ${book.translator}. Selections only; full works are not bundled in this edition.",fontSize=11.sp,color=Color.Gray)
    }
}
