package com.sophia.nativeapp
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PrimaryBook(val id:String,val title:String,val author:String,val translator:String,val tradition:String,val description:String,val offline:Boolean) {
    val url:String get()="https://www.gutenberg.org/ebooks/"+id
    val asset:String get()="primary/"+id+".txt"
}
fun loadPrimaryBooks(context:Context):List<PrimaryBook>{
    val available=context.assets.list("primary")?.toSet() ?: emptySet()
    val rows=listOf(
        "1656|Apology|Plato|Benjamin Jowett|Socratic Philosophy|Socrates' trial and examined life",
        "1497|The Republic|Plato|Benjamin Jowett|Platonic Philosophy|Justice, forms and the ideal city",
        "8438|Nicomachean Ethics|Aristotle|Historical English translation|Aristotelian Philosophy|Virtue, practical wisdom and happiness",
        "59|Discourse on the Method|René Descartes|John Veitch|Scientific Revolution|Methodic doubt and rational inquiry",
        "9662|An Enquiry Concerning Human Understanding|David Hume|Original English; L. A. Selby-Bigge edition|Epistemology|Experience, causality and skepticism",
        "4280|Critique of Pure Reason|Immanuel Kant|J. M. D. Meiklejohn|Kantian Philosophy|The limits of reason and conditions of knowledge",
        "4363|Beyond Good and Evil|Friedrich Nietzsche|Helen Zimmern|Existentialism|Critique of inherited philosophical values",
        "11224|Utilitarianism|John Stuart Mill|Original English|Ethics|The greatest happiness principle",
        "5827|The Problems of Philosophy|Bertrand Russell|Original English|Analytic Philosophy|Appearance, reality and the value of philosophy",
        "3330|The Analects|Confucius|James Legge|Chinese Philosophy|Virtue, ritual and moral cultivation",
        "216|Tao Te Ching|Laozi|James Legge|Chinese Philosophy|The Way, non-forcing and government",
        "2017|Dhammapada|Ancient Buddhist tradition|F. Max Müller|Buddhist Metaphysics|Discipline of mind and freedom from suffering",
        "2680|Meditations|Marcus Aurelius|Historical public-domain translation|Stoicism|Character, nature and ethical practice"
    )
    return rows.map {row->
        val p=row.split("|")
        PrimaryBook(p[0],p[1],p[2],p[3],p[4],p[5],(p[0]+".txt") in available)
    }
}
fun loadPrimaryText(context:Context,book:PrimaryBook):String=try{
    val raw=context.assets.open(book.asset).bufferedReader(Charsets.UTF_8).use{it.readText()}
    val st=Regex("\\*\\*\\*\\s*START OF (?:THE |THIS )?PROJECT GUTENBERG EBOOK[^\\r\\n]*\\*\\*\\*",RegexOption.IGNORE_CASE).find(raw)
    val end=Regex("\\*\\*\\*\\s*END OF (?:THE |THIS )?PROJECT GUTENBERG EBOOK",RegexOption.IGNORE_CASE).find(raw)
    raw.substring(st?.range?.last?.plus(1)?:0,end?.range?.first?:raw.length).trim()
}catch(_:Exception){""}
@Composable fun PrimaryLibraryIndex(books:List<PrimaryBook>,onOpen:(Int)->Unit,onSource:(String)->Unit){
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(17.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Surface(shape=RoundedCornerShape(22.dp),color=Color(0xFF155540)){
            Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
                Text("✦ THE GREAT LIBRARY",color=Color(0xFFFFD685),fontWeight=FontWeight.Bold,fontSize=11.sp)
                Text("Read the philosophers.",color=Color.White,fontSize=27.sp,fontWeight=FontWeight.ExtraBold)
                Text(books.count{it.offline}.toString()+" original complete texts available offline",color=Color.White,fontSize=14.sp)
                Text("These are genuine historical editions, separate from SOPHIA's commentaries.",color=Color(0xFFD9F1DE),fontSize=12.sp)
            }
        }
        books.forEachIndexed{idx,b->
            ElevatedCard(onClick={onOpen(idx)},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(17.dp),colors=CardDefaults.elevatedCardColors(containerColor=Color.White)){
                Row(Modifier.padding(13.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically){
                    Surface(color=Color(0xFFF3E4CB),shape=RoundedCornerShape(13.dp)){
                        Text("📖",fontSize=28.sp,modifier=Modifier.padding(11.dp))
                    }
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
                        Text(b.title,color=Color(0xFF184A38),fontWeight=FontWeight.ExtraBold,fontSize=16.sp)
                        Text(b.author+" · "+b.tradition,color=Color(0xFF6C786C),fontSize=11.sp)
                        Text(if(b.offline)"✓ COMPLETE WORK OFFLINE" else "↗ OFFICIAL SOURCE ONLINE",color=if(b.offline)Color(0xFF27865A) else Color(0xFFA37A3C),fontWeight=FontWeight.Bold,fontSize=10.sp)
                    }
                }
            }
        }
        Text("Project Gutenberg editions are identified as public domain in the USA. Rights in other jurisdictions may differ.",color=Color(0xFF647669),fontSize=11.sp)
    }
}
@Composable fun PrimaryReader(book:PrimaryBook,onSpeak:(String)->Unit,onStop:()->Unit,onShare:(String,String)->Unit,onSource:(String)->Unit){
    val context=androidx.compose.ui.platform.LocalContext.current
    val prefs=remember{context.getSharedPreferences("sophia_primary_v5",Context.MODE_PRIVATE)}
    var body by remember(book.id){mutableStateOf(loadPrimaryText(context,book))}
    val pages=remember(body){if(body.isBlank())emptyList() else body.chunked(3200)}
    var page by remember(book.id,body){mutableIntStateOf(prefs.getInt("page_"+book.id,0).coerceIn(0,(pages.size-1).coerceAtLeast(0)))}
    var font by remember{mutableFloatStateOf(17f)}
    var search by remember{mutableStateOf("")}
    var status by remember{mutableStateOf("")}
    val importer=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri:Uri?->
        if(uri!=null)try{
            val text=context.contentResolver.openInputStream(uri)?.bufferedReader()?.use{it.readText()}?:""
            if(text.isNotBlank()){body=text;page=0;status="Imported user-owned text in this reading session."}
        }catch(_:Exception){status="Cannot open selected text file."}
    }
    val exporter=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")){uri:Uri?->
        if(uri!=null)try{
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use{it.write(body)}
            status="Saved .txt. Import it into ElevenReader using Upload a file."
        }catch(_:Exception){status="Cannot save the document."}
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(17.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("📖 PRIMARY SOURCE",fontSize=11.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFFB88D3A))
        Text(book.title,color=Color(0xFF164E39),fontSize=25.sp,fontWeight=FontWeight.ExtraBold,lineHeight=28.sp)
        Text(book.author+" · "+book.translator,color=Color(0xFF657B6B),fontSize=13.sp)
        Surface(color=Color(0xFFFFF1D6),shape=RoundedCornerShape(13.dp)){
            Column(Modifier.padding(13.dp)){
                Text("Project Gutenberg #"+book.id,color=Color(0xFF705527),fontWeight=FontWeight.Bold,fontSize=12.sp)
                Text("Historical original / public-domain edition in the USA. Check copyright rules for your country.",color=Color(0xFF6E634E),fontSize=11.sp)
            }
        }
        if(body.isBlank()){
            Text("Full text is not bundled in this build. Open the original edition online or import a .txt file you've obtained lawfully.",color=Color(0xFF243E31),lineHeight=23.sp)
        }else{
            Row(verticalAlignment=Alignment.CenterVertically){
                Text("Page "+(page+1)+" of "+pages.size,color=Color(0xFF164E39),fontWeight=FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("A",fontSize=13.sp)
                Slider(value=font,onValueChange={font=it},valueRange=14f..25f,modifier=Modifier.width(115.dp))
                Text("A",fontSize=20.sp)
            }
            LinearProgressIndicator(progress=(page+1f)/pages.size,modifier=Modifier.fillMaxWidth().height(8.dp),color=Color(0xFF31A16E))
            ElevatedCard(colors=CardDefaults.elevatedCardColors(containerColor=Color.White)){
                Text(pages[page],modifier=Modifier.padding(18.dp),color=Color(0xFF20362A),fontSize=font.sp,lineHeight=(font*1.55f).sp)
            }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                OutlinedButton(enabled=page>0,onClick={page--;prefs.edit().putInt("page_"+book.id,page).apply()},modifier=Modifier.weight(1f)){Text("‹ Previous")}
                Button(enabled=page<pages.size-1,onClick={page++;prefs.edit().putInt("page_"+book.id,page).apply()},modifier=Modifier.weight(1f)){Text("Next ›")}
            }
            OutlinedTextField(value=search,onValueChange={search=it},label={Text("Search within full text")},modifier=Modifier.fillMaxWidth())
            OutlinedButton(enabled=search.length>=3,onClick={
                val at=body.indexOf(search,(page+1)*3200,ignoreCase=true).let{if(it<0)body.indexOf(search,ignoreCase=true)else it}
                if(at>=0){page=(at/3200).coerceAtMost(pages.size-1);prefs.edit().putInt("page_"+book.id,page).apply();status="Found on page "+(page+1)}
                else status="Not found"
            }){Text("Find next")}
            OutlinedButton(onClick={onSpeak(pages[page])},modifier=Modifier.fillMaxWidth()){Text("🔊 Android narration of this page")}
            OutlinedButton(onClick=onStop,modifier=Modifier.fillMaxWidth()){Text("■ Stop Android narration")}
        }
        Button(onClick={onShare(book.title,body.ifBlank{book.url})},modifier=Modifier.fillMaxWidth()){Text("🎧 Choose narrator app / ElevenReader")}
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            OutlinedButton(onClick={
                val clipboard=context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(book.title,body.ifBlank{book.url}))
                status="Copied. In ElevenReader select Write text and paste."
            },modifier=Modifier.weight(1f)){Text("Copy text")}
            OutlinedButton(enabled=body.isNotBlank(),onClick={exporter.launch(book.title.replace(Regex("[^A-Za-z0-9_]"),"_")+".txt")},modifier=Modifier.weight(1f)){Text("Save .txt")}
        }
        OutlinedButton(onClick={importer.launch(arrayOf("text/plain"))},modifier=Modifier.fillMaxWidth()){Text("Import my .txt file")}
        OutlinedButton(onClick={onSource(book.url)},modifier=Modifier.fillMaxWidth()){Text("↗ Read official source online")}
        if(status.isNotEmpty())Text(status,color=Color(0xFF167B56),fontSize=12.sp)
        Text("External apps narrate in their own player. If ElevenReader is absent from Android's chooser, use Save .txt or Copy text and import inside ElevenReader.",color=Color(0xFF65776A),fontSize=12.sp)
    }
}