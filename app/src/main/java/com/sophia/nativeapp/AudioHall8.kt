package com.sophia.nativeapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

private val midnight8=Color(0xFF0E2032)
private val navy8=Color(0xFF172D42)
private val gold8=Color(0xFFE5BF79)
private val ivory8=Color(0xFFFAF0DA)

@Composable fun AudioHall8(onOpenLibrary:()->Unit){
    val context=LocalContext.current
    val programs=remember { AudioCatalog8.load(context) }
    val settings=remember { context.getSharedPreferences("sophia_radio_v6",Context.MODE_PRIVATE) }
    var filter by remember { mutableStateOf("Seminars") }
    var selected by remember { mutableStateOf(programs.firstOrNull { it.isSeminar }?.id ?: "") }
    var transcript by remember { mutableStateOf(false) }
    var followTheme by remember { mutableStateOf(true) }
    var sleep by remember { mutableIntStateOf(0) }
    var voiceSpeed by remember {mutableFloatStateOf(.9f)}
    var state by remember {mutableStateOf("stopped")}
    var title by remember {mutableStateOf("No program playing")}
    var chapter by remember {mutableIntStateOf(0)}
    var subtitle by remember {mutableStateOf("")}
    var activeId by remember { mutableStateOf("") }
    val prefsSaved=remember {context.getSharedPreferences("sophia_audio_bookmarks_v8",Context.MODE_PRIVATE)}
    var saved by remember {mutableStateOf(prefsSaved.getStringSet("programs",emptySet())?.toSet()?:emptySet())}
    var search by remember {mutableStateOf("")}
    var updated by remember {mutableIntStateOf(0)}
    val selectedProgram=programs.firstOrNull{it.id==selected}
    val notificationPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){}
    LaunchedEffect(Unit){while(true){
        state=settings.getString("state","stopped")?:"stopped"
        title=settings.getString("title","No program playing")?:"No program playing"
        subtitle=settings.getString("detail","")?:""
        chapter=settings.getInt("chapter",0)
        activeId=settings.getString("program","")?:""
        delay(750)
    }}
    fun action(type:String,programId:String=selected,index:Int=-1){
        val intent=Intent(context,PhilosophyRadioService::class.java).setAction(type)
        if(type==PhilosophyRadioService.START){
            intent.putExtra("mode",if(programId.isNotBlank())"episode" else "alternating")
            intent.putExtra("programId",programId).putExtra("follow",followTheme)
            intent.putExtra("scope","all").putExtra("minutes",sleep)
            intent.putExtra("rate",voiceSpeed).putExtra("pitch",1f)
            if(Build.VERSION.SDK_INT>=33&&ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            ContextCompat.startForegroundService(context,intent)
        } else {
            if(index>=0)intent.putExtra("chapter",index)
            context.startService(intent)
        }
    }
    Column(Modifier.fillMaxSize().background(midnight8)){
        LazyColumn(modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(bottom=26.dp)){
            item {
                Box(Modifier.fillMaxWidth().height(290.dp)){
                    Image(painterResource(R.drawable.grand_audio),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x22091525),midnight8))))
                    Column(Modifier.align(Alignment.BottomStart).padding(start=24.dp,end=24.dp,bottom=25.dp)){
                        Text("THE GRAND ACADEMY",color=gold8,fontSize=11.sp,letterSpacing=3.sp,fontWeight=FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text("The Audio Hall",fontSize=36.sp,lineHeight=38.sp,color=ivory8,fontFamily=FontFamily.Serif)
                        Spacer(Modifier.height(7.dp))
                        Text("A philosophy to hear, question, and remember.",color=Color(0xFFD3E2DE),fontSize=14.sp)
                    }
                }
            }
            item {
                Row(Modifier.padding(horizontal=18.dp),horizontalArrangement=Arrangement.spacedBy(7.dp)){
                    listOf("Seminars","Lectures","Saved","Radio").forEach{tab->
                        FilterChip(selected=filter==tab,onClick={filter=tab},label={Text(tab,fontSize=12.sp)},colors=FilterChipDefaults.filterChipColors(selectedContainerColor=gold8,selectedLabelColor=midnight8,labelColor=ivory8))
                    }
                }
                if(filter=="Radio"){
                    Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
                        Text("Continuous Philosophical Radio",fontSize=24.sp,color=ivory8,fontFamily=FontFamily.Serif)
                        Text("Curated source excerpts and commentary, shuffled with recent-play avoidance. It keeps playing with the screen locked.",color=Color(0xFFB6C8CD),fontSize=14.sp)
                        Button(onClick={action(PhilosophyRadioService.START,"")},colors=ButtonDefaults.buttonColors(containerColor=gold8,contentColor=midnight8),modifier=Modifier.fillMaxWidth()){Icon(Icons.Rounded.PlayArrow,null);Text(" Start continuous radio")}
                        OutlinedButton(onClick={onOpenLibrary},modifier=Modifier.fillMaxWidth()){Text("Explore the original texts",color=ivory8)}
                    }
                } else {
                    OutlinedTextField(value=search,onValueChange={search=it},label={Text("Search programs")},singleLine=true,
                        leadingIcon={Icon(Icons.Rounded.Search,null)},modifier=Modifier.fillMaxWidth().padding(horizontal=18.dp),
                        colors=OutlinedTextFieldDefaults.colors(focusedTextColor=ivory8,unfocusedTextColor=ivory8,focusedLabelColor=gold8,unfocusedLabelColor=ivory8,focusedBorderColor=gold8,unfocusedBorderColor=Color(0xFF71818E)))
                }
            }
            if(filter!="Radio"){
                val shown=programs.filter{p->(when(filter){"Lectures"->!p.isSeminar;"Saved"->p.id in saved;else->p.isSeminar}) &&
                    (search.isBlank()||p.title.contains(search,true)||p.authors.contains(search,true)||p.tags.any{it.contains(search,true)})}
                items(shown,key={it.id}){program->
                    val index=programs.indexOf(program)
                    Surface(onClick={selected=program.id;transcript=false},modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=5.dp),
                        color=if(selected==program.id)navy8 else Color(0xFF142638),shape=RoundedCornerShape(18.dp),
                        border=BorderStroke(1.dp,if(selected==program.id)gold8.copy(alpha=.55f) else Color(0xFF314456))){
                        Row(Modifier.padding(9.dp),verticalAlignment=Alignment.CenterVertically){
                            Box(Modifier.size(88.dp).clip(RoundedCornerShape(13.dp))){
                                val img=when(index%6){0->R.drawable.grand_cover_0;1->R.drawable.grand_cover_1;2->R.drawable.grand_cover_2;3->R.drawable.grand_cover_3;4->R.drawable.grand_cover_4;else->R.drawable.grand_cover_5}
                                Image(painterResource(img),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                                Box(Modifier.fillMaxSize().background(Color(0x330F1723)))
                                Icon(Icons.Rounded.Headphones,null,Modifier.align(Alignment.Center),tint=Color.White)
                            }
                            Column(Modifier.weight(1f).padding(start=13.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
                                Text(if(program.isSeminar)"THEMATIC SEMINAR" else "COURSE LECTURE",fontSize=9.sp,letterSpacing=1.3.sp,color=gold8)
                                Text(program.title,fontSize=18.sp,fontFamily=FontFamily.Serif,color=ivory8,lineHeight=20.sp)
                                Text(program.authors,fontSize=11.sp,color=Color(0xFFB2C5C7),maxLines=2)
                                Text("${program.chapters.size} chapters · ~${program.estimatedMinutes} min narrated script",fontSize=10.sp,color=Color(0xFFA3B2BF))
                            }
                            Icon(Icons.Rounded.KeyboardArrowRight,null,tint=gold8)
                        }
                    }
                }
            }
            if(selectedProgram!=null){
                item{
                    Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                        HorizontalDivider(color=Color(0xFF405368))
                        Text("FEATURED PROGRAM",color=gold8,fontSize=10.sp,letterSpacing=2.sp)
                        Text(selectedProgram.title,fontSize=29.sp,color=ivory8,fontFamily=FontFamily.Serif,lineHeight=32.sp)
                        Text(selectedProgram.question,color=Color(0xFFCCD9DA),fontSize=15.sp,lineHeight=23.sp)
                        Row(horizontalArrangement=Arrangement.spacedBy(9.dp)){
                            Button(onClick={action(PhilosophyRadioService.START,selectedProgram.id)},modifier=Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=gold8,contentColor=midnight8)){
                                Icon(Icons.Rounded.PlayArrow,null);Text(if(settings.getString("program","")==selectedProgram.id)" Resume / Restart" else " Listen")
                            }
                            OutlinedButton(onClick={
                                saved=if(selectedProgram.id in saved)saved-selectedProgram.id else saved+selectedProgram.id
                                prefsSaved.edit().putStringSet("programs",saved).apply()
                            }){Icon(if(selectedProgram.id in saved)Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,null,tint=gold8)}
                        }
                        Text("CHAPTERS",color=gold8,fontSize=10.sp,letterSpacing=2.sp)
                        selectedProgram.chapters.forEachIndexed{idx,c->
                            Row(Modifier.fillMaxWidth().clickable{
                                if(activeId!=selectedProgram.id)action(PhilosophyRadioService.START,selectedProgram.id)
                                settings.edit().putInt("chapter_${selectedProgram.id}",idx).apply()
                                action(PhilosophyRadioService.SEEK,selectedProgram.id,idx)
                            }.padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
                                Text("${idx+1}".padStart(2,'0'),color=gold8,fontSize=14.sp)
                                Spacer(Modifier.width(15.dp))
                                Text(c.name,color=ivory8,fontSize=14.sp,modifier=Modifier.weight(1f))
                                Icon(Icons.Rounded.PlayCircleOutline,null,tint=Color(0xFF8DA9B2),modifier=Modifier.size(19.dp))
                            }
                            HorizontalDivider(color=Color(0xFF283B4D))
                        }
                        Text("Sources: ${selectedProgram.sources}",color=Color(0xFF9BB2B7),fontSize=11.sp,lineHeight=17.sp)
                        Row(verticalAlignment=Alignment.CenterVertically){
                            Checkbox(checked=followTheme,onCheckedChange={followTheme=it},colors=CheckboxDefaults.colors(checkedColor=gold8))
                            Text("Continue into related seminars",color=ivory8,fontSize=12.sp)
                        }
                        Row(verticalAlignment=Alignment.CenterVertically){
                            Text("Sleep",color=ivory8,fontSize=12.sp);Spacer(Modifier.width(12.dp))
                            listOf(0,15,30,60).forEach{m->FilterChip(selected=sleep==m,onClick={sleep=m},label={Text(if(m==0)"∞" else "$m m",fontSize=10.sp)})}
                        }
                        Text("Narration speed: ${"%.2f".format(voiceSpeed)}×",color=ivory8,fontSize=12.sp)
                        Slider(value=voiceSpeed,onValueChange={voiceSpeed=it},valueRange=.7f..1.3f,colors=SliderDefaults.colors(thumbColor=gold8,activeTrackColor=gold8))
                        OutlinedButton(onClick={transcript=!transcript},modifier=Modifier.fillMaxWidth()){
                            Icon(Icons.Rounded.Subject,null);Text(if(transcript)" Hide full transcript" else " Read full transcript",color=ivory8)
                        }
                        if(transcript)selectedProgram.chapters.forEach{c->
                            Text(c.name,fontSize=20.sp,fontFamily=FontFamily.Serif,color=gold8)
                            Text(c.body,fontSize=15.sp,lineHeight=24.sp,color=ivory8)
                        }
                    }
                }
            }
            item {
                Surface(Modifier.fillMaxWidth().padding(12.dp),color=navy8,shape=RoundedCornerShape(24.dp),border=BorderStroke(1.dp,Color(0xFF466075))){
                    Column(Modifier.padding(17.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                        Text("NOW PLAYING",color=gold8,fontWeight=FontWeight.SemiBold,fontSize=10.sp,letterSpacing=2.sp)
                        Text(title,color=ivory8,fontFamily=FontFamily.Serif,fontSize=21.sp)
                        Text(subtitle,color=Color(0xFFB2C5C7),fontSize=12.sp)
                        LinearProgressIndicator(progress={if(activeId.isNotBlank())(chapter+1f)/(programs.firstOrNull{it.id==activeId}?.chapters?.size?:1).coerceAtLeast(1) else 0f},modifier=Modifier.fillMaxWidth(),color=gold8,trackColor=Color(0xFF304C61))
                        Text(if(state=="playing")"Playing · chapter ${chapter+1}" else state.replaceFirstChar{it.uppercase()},color=gold8,fontSize=12.sp)
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceEvenly){
                            IconButton(onClick={action(PhilosophyRadioService.PREVIOUS)}){Icon(Icons.Rounded.SkipPrevious,null,tint=ivory8)}
                            IconButton(onClick={action(if(state=="paused")PhilosophyRadioService.RESUME else PhilosophyRadioService.PAUSE)}){
                                Icon(if(state=="paused")Icons.Rounded.PlayArrow else Icons.Rounded.Pause,null,tint=gold8,modifier=Modifier.size(35.dp))
                            }
                            IconButton(onClick={action(PhilosophyRadioService.NEXT)}){Icon(Icons.Rounded.SkipNext,null,tint=ivory8)}
                            IconButton(onClick={action(PhilosophyRadioService.STOP)}){Icon(Icons.Rounded.Stop,null,tint=ivory8)}
                        }
                    }
                }
                Text("Playback uses Android's installed TTS engine. Voice choice is available in Scholar Settings. Pausing resumes at the current speech segment, not an exact millisecond. Program durations estimate the authored script only; longer full seminars will arrive in content updates.",color=Color(0xFFA7B8BF),fontSize=11.sp,lineHeight=17.sp,modifier=Modifier.padding(horizontal=20.dp))
            }
        }
    }
}