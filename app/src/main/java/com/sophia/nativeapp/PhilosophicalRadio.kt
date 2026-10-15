package com.sophia.nativeapp

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.*
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val CHANNEL="sophia_radio_channel_v6"
private const val NOTIFICATION_ID=9021
private data class SpokenItem(val id:String,val title:String,val subtitle:String,val text:String)

/** TTS-based foreground playback, independent of the Activity. */
class PhilosophyRadioService:Service() {
    companion object {
        const val START="sophia.radio.START";const val PAUSE="sophia.radio.PAUSE"
        const val RESUME="sophia.radio.RESUME";const val NEXT="sophia.radio.NEXT"
        const val STOP="sophia.radio.STOP";const val PREVIOUS="sophia.radio.PREVIOUS";const val SEEK="sophia.radio.SEEK"
    }
    private val handler=Handler(Looper.getMainLooper())
    private lateinit var session:MediaSession
    private val pref by lazy{getSharedPreferences("sophia_radio_v6",MODE_PRIVATE)}
    private var tts:TextToSpeech?=null
    private var ready=false;private var playing=false;private var paused=false
    private var token=0;private var idx=0;private var errors=0
    private var rate=.9f;private var pitch=1f;private var deadline=Long.MAX_VALUE
    private var sleeper:Runnable?=null
    private var current:SpokenItem?=null
    private val queue=mutableListOf<SpokenItem>()
    private var mode="alternating";private var scope="all"
    private var programId="";private var followTheme=false;private var segments=emptyList<String>();private var segmentIndex=0
    private var restoreSavedSegment=false

    override fun onCreate(){super.onCreate()
        if(Build.VERSION.SDK_INT>=26){
            val c=NotificationChannel(CHANNEL,"SOPHIA Radio",NotificationManager.IMPORTANCE_LOW)
            c.description="Continuous offline philosophical readings"
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(c)
        }
        session=MediaSession(this,"SOPHIA Philosophy Radio")
        session.setCallback(object:MediaSession.Callback(){
            override fun onPlay(){resumeRadio()};override fun onPause(){pauseRadio()}
            override fun onSkipToNext(){skip()};override fun onSkipToPrevious(){restoreSavedSegment=false;idx=(idx-1).coerceAtLeast(0);speakItem()}
            override fun onStop(){stopRadio()}
        })
        session.isActive=true;updateSession()
    }
    override fun onBind(intent:Intent?):IBinder?=null
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int{
        when(intent?.action){
            START->{
                mode=intent.getStringExtra("mode")?:"alternating";scope=intent.getStringExtra("scope")?:"all"
                programId=intent.getStringExtra("programId")?:"";followTheme=intent.getBooleanExtra("follow",false)
                rate=intent.getFloatExtra("rate",.9f);pitch=intent.getFloatExtra("pitch",1f)
                val duration=intent.getIntExtra("minutes",30)
                deadline=if(duration==0)Long.MAX_VALUE else SystemClock.elapsedRealtime()+duration*60_000L
                sleeper?.let{handler.removeCallbacks(it)};sleeper=null
                if(duration>0){val task=Runnable{stopRadio()};sleeper=task;handler.postDelayed(task,duration*60_000L)}
                playing=true;paused=false;ready=false;errors=0;current=null
                restoreSavedSegment=true
                foreground(note());createQueue();initializeSpeech()
            }
            NEXT->skip();PREVIOUS->{if(playing){restoreSavedSegment=false;idx=(idx-1).coerceAtLeast(0);speakItem()}}
            SEEK->{if(playing){restoreSavedSegment=false;idx=intent.getIntExtra("chapter",0).coerceIn(0,(queue.size-1).coerceAtLeast(0));speakItem()}}
            PAUSE->pauseRadio();RESUME->resumeRadio();STOP->stopRadio()
        }
        return START_NOT_STICKY
    }
    private fun foreground(n:Notification){
        if(Build.VERSION.SDK_INT>=29)startForeground(NOTIFICATION_ID,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        else startForeground(NOTIFICATION_ID,n)
    }
    private fun updateSession(){
        val st=if(!playing)PlaybackState.STATE_STOPPED else if(paused)PlaybackState.STATE_PAUSED else PlaybackState.STATE_PLAYING
        val actions=PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_STOP
        session.setPlaybackState(PlaybackState.Builder().setActions(actions or PlaybackState.ACTION_SKIP_TO_PREVIOUS)
            .setState(st,0L,1f).build())
        session.setMetadata(MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE,current?.title?:"SOPHIA 9 · Definitive Audio Academy")
            .putString(MediaMetadata.METADATA_KEY_ARTIST,current?.subtitle?:"The Grand Academy")
            .build())
    }
    private fun command(action:String,id:Int):PendingIntent{
        val flags=PendingIntent.FLAG_UPDATE_CURRENT or if(Build.VERSION.SDK_INT>=23)PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getService(this,id,Intent(this,PhilosophyRadioService::class.java).setAction(action),flags)
    }
    private fun note():Notification{
        val b=if(Build.VERSION.SDK_INT>=26)Notification.Builder(this,CHANNEL)else Notification.Builder(this)
        val flags=PendingIntent.FLAG_UPDATE_CURRENT or if(Build.VERSION.SDK_INT>=23)PendingIntent.FLAG_IMMUTABLE else 0
        val open=PendingIntent.getActivity(this,8,Intent(this,MainActivity::class.java),flags)
        b.setSmallIcon(android.R.drawable.ic_media_play).setContentTitle(current?.title?:"SOPHIA Philosophical Radio")
            .setContentText(current?.subtitle?:"Preparing philosophical readings")
            .setContentIntent(open).setOnlyAlertOnce(true).setOngoing(true).setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(if(paused)android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause,
                if(paused)"Play" else "Pause",command(if(paused)RESUME else PAUSE,11))
            .addAction(android.R.drawable.ic_media_previous,"Previous",command(PREVIOUS,14))
            .addAction(android.R.drawable.ic_media_next,"Next",command(NEXT,12))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel,"Stop",command(STOP,13))
        b.setStyle(Notification.MediaStyle().setMediaSession(session.sessionToken))
        return b.build()
    }
    private fun report(state:String,extra:String=""){
        pref.edit().putString("state",state).putString("title",current?.title?:"SOPHIA Radio")
            .putString("program",programId).putInt("chapter",idx)
            .putInt("segment",segmentIndex).putInt("segments",segments.size)
            .putString("detail",extra.ifBlank{current?.subtitle?:""}).apply()
        updateSession()
        if(playing)(getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_ID,note())
    }
    private fun createQueue(){
        if(mode=="episode" && programId.isNotBlank()){
            val selected=AudioCatalog8.loadProgram(this,programId)
            if(selected!=null){
                queue.clear()
                selected.chapters.forEachIndexed{n,c->
                    queue.add(SpokenItem("${selected.id}:$n",selected.title,"${n+1}/${selected.chapters.size} · ${c.name}",c.body))
                }
                idx=pref.getInt("chapter_${selected.id}",0).coerceIn(0,(queue.size-1).coerceAtLeast(0))
                pref.edit().putString("mode",mode).putString("program",programId).apply()
                return
            }
        }
        val books=loadPrimaryBooks(this);val all=books.flatMap{b->b.excerpts.map{b to it}}
        val fav=getSharedPreferences("sophia_anthology_v6",MODE_PRIVATE).getStringSet("favorites",emptySet())?:emptySet()
        val progress=getSharedPreferences("sophia_native_v4",MODE_PRIVATE)
        val studied=(progress.getStringSet("completed",emptySet())?:emptySet()).map{it.substringBefore(':')}.toSet()
        val due=progress.all.filter{(k,v)->k.startsWith("due:")&&v is Long&&v<=System.currentTimeMillis()}
            .keys.map{it.removePrefix("due:").lowercase()}
        val filtered=all.filter{(b,e)->when(scope){
            "favorites"->e.id in fav
            "studied"->studied.isEmpty()||studied.any{key->key in b.tradition.lowercase()||key in b.title.lowercase()}
            "weak"->due.isEmpty()||due.any{it in e.passage.lowercase()||it in e.commentary.lowercase()}
            else->true
        }}
        val chosen=(if(filtered.isEmpty())all else filtered).shuffled(Random(System.currentTimeMillis()))
        val recent=(pref.getString("recent","")?:"").split('|').takeLast(20).toSet()
        val ordered=chosen.filter{it.second.id !in recent}+chosen.filter{it.second.id in recent}
        queue.clear()
        if(mode=="program"){
            val topics=listOf(
                Triple("The Question of Freedom",listOf("free","will","necessity","cause"),"What would distinguish genuine choice from the appearance of choice?"),
                Triple("Knowing and Doubting",listOf("knowledge","truth","doubt","perception"),"Can certainty be established without presupposing what is to be proved?"),
                Triple("Virtue and the Good Life",listOf("virtue","good","moral","happiness"),"Does knowing the good suffice for acting well?"),
                Triple("Reality and Appearance",listOf("being","appearance","reality","representation"),"What justifies our distinction between how things appear and what they are?"),
                Triple("Mind and Selfhood",listOf("mind","self","conscious","thought"),"What conditions must hold for an enduring self to be intelligible?")
            )
            val chosenTopic=topics[Random(System.currentTimeMillis()).nextInt(topics.size)]
            val related=ordered.filter{(b,e)->
                val text=(b.title+" "+e.passage+" "+e.title).lowercase()
                chosenTopic.second.any{it in text}
            }
            val first=related.firstOrNull()?:ordered.firstOrNull()
            val second=related.firstOrNull{it.first.id!=first?.first?.id}?:related.drop(1).firstOrNull()
            queue.add(SpokenItem("program:intro",chosenTopic.first,"Opening orientation",
                "Welcome to SOPHIA's thematic audio seminar. Our problem is: ${chosenTopic.third} You will hear historically sourced passages and commentary, then be invited to think through the question independently."))
            if(first!=null){
                val (book,ex)=first
                queue.add(SpokenItem(ex.id+":q",book.title+" — "+ex.title,"Primary text · "+book.author,ex.passage))
                queue.add(SpokenItem(ex.id+":c",book.title+" — commentary","SOPHIA original interpretive guide",ex.commentary))
            }
            if(second!=null){
                val (book,ex)=second
                queue.add(SpokenItem(ex.id+":q",book.title+" — "+ex.title,"Counterpoint · "+book.author,
                    "Now consider a second text. Compare its assumptions carefully with the first author. "+ex.passage))
                queue.add(SpokenItem(ex.id+":c",book.title+" — comparison","SOPHIA interpretive questions",
                    "The two excerpts address an overlapping theme, not necessarily a shared doctrine. "+ex.commentary))
            }
            queue.add(SpokenItem("program:reflection",chosenTopic.first,"Reflective conclusion",
                "This seminar closes with a question. ${chosenTopic.third} Pause your listening if you wish to formulate two premises, an objection, and a revised thesis."))
        }else if(mode=="seminar"){
            queue.addAll(loadCourses(this).flatMap{course->course.units.map{u->
                SpokenItem("seminar:${course.id}:${u.name}","${course.name}: ${u.name}","Guided seminar",
                    u.reading.foundation+". "+u.reading.intermediate+". Reflection: "+u.reading.objection)
            }}.shuffled(Random(System.currentTimeMillis())))
        }else for((book,ex) in ordered){
            val q=SpokenItem(ex.id+":q",book.title+" — "+ex.title,"Primary source · "+book.author,ex.passage)
            val c=SpokenItem(ex.id+":c",book.title+" — Commentary","SOPHIA guided interpretation",ex.commentary)
            when(mode){"quotes"->queue.add(q);"commentaries"->queue.add(c);else->{queue.add(q);queue.add(c)}}
        }
        idx=0;pref.edit().putString("mode",mode).putString("scope",scope).apply()
    }
    private fun initializeSpeech(){
        tts?.stop();tts?.shutdown()
        val settings=getSharedPreferences("sophia_narrator_v5",MODE_PRIVATE)
        val engine=settings.getString("engine","")?:""
        val init=TextToSpeech.OnInitListener{r->
            if(r!=TextToSpeech.SUCCESS){current=SpokenItem("err","TTS unavailable","Choose another Android voice","");report("error")}
            else{
                tts?.voices?.firstOrNull{it.name==settings.getString("voice","")}?.let{tts?.voice=it}
                tts?.setSpeechRate(rate);tts?.setPitch(pitch)
                tts?.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                tts?.setOnUtteranceProgressListener(object:UtteranceProgressListener(){
                    override fun onStart(utteranceId:String?){}
                    override fun onDone(utteranceId:String?){handler.post{if(utteranceId=="sophia_$token"&&playing&&!paused){
                        if(segmentIndex+1<segments.size){segmentIndex++;speakSegment()}else skip()
                    }}}
                    @Deprecated("Platform API")
                    override fun onError(utteranceId:String?){handler.post{errors++;if(errors<3)skip() else pauseRadio()}}
                })
                ready=true;speakItem()
            }
        }
        tts=if(engine.isBlank())TextToSpeech(this,init)else TextToSpeech(this,init,engine)
    }
    private fun speakItem(){
        if(!playing||paused||!ready)return
        if(SystemClock.elapsedRealtime()>=deadline){stopRadio();return}
        if(queue.isEmpty()){stopRadio();return}
        if(idx>=queue.size){
            if(mode=="episode" && programId.isNotBlank()){
                val next=AudioCatalog8.load(this).firstOrNull{it.id==programId}?.related
                if(followTheme && !next.isNullOrBlank() && next!=programId){
                    programId=next;pref.edit().putInt("chapter_$next",0).apply();createQueue()
                }else{stopRadio();return}
            }else createQueue()
        }
        current=queue.getOrNull(idx)?:run{stopRadio();return}
        if(mode=="episode")pref.edit().putInt("chapter_$programId",idx).apply()
        val entry=current?:return
        if(entry.id.endsWith(":q")){
            val recent=((pref.getString("recent","")?:"").split('|').filter{it.isNotBlank()}+entry.id.removeSuffix(":q")).takeLast(20)
            pref.edit().putString("recent",recent.joinToString("|")).apply()
        }
        segments=speechChunks(entry.text)
        if(segments.isEmpty())segments=listOf("There is no text in this chapter.")
        segmentIndex=if(mode=="episode" && restoreSavedSegment)
            pref.getInt("segment_${programId}_$idx",0).coerceIn(0,segments.lastIndex) else 0
        restoreSavedSegment=false
        speakSegment()
    }
    /** Avoid splitting a word or sentence arbitrarily at the Android TTS length limit. */
    private fun speechChunks(text:String):List<String>{
        val result=mutableListOf<String>()
        var i=0
        while(i<text.length){
            var end=(i+2200).coerceAtMost(text.length)
            if(end<text.length){
                var cut=end
                while(cut>i+950 && text[cut-1] !in charArrayOf('.', '!', '?', '\n', ' '))cut--
                if(cut>i+950)end=cut
            }
            val chunk=text.substring(i,end).trim()
            if(chunk.isNotEmpty())result.add(chunk)
            i=end
        }
        return result
    }
    private fun speakSegment(){
        if(!playing||paused||!ready)return
        if(mode=="episode" && programId.isNotBlank())
            pref.edit().putInt("segment_${programId}_$idx",segmentIndex).apply()
        token++
        if(tts?.speak(segments.getOrElse(segmentIndex){""},TextToSpeech.QUEUE_FLUSH,null,"sophia_$token")==TextToSpeech.ERROR){
            report("error","The selected Android TTS engine could not read this passage");pauseRadio()
        }else report("playing")
    }
    private fun skip(){
        if(!playing)return
        if(mode=="episode" && programId.isNotBlank())
            pref.edit().remove("segment_${programId}_$idx").apply()
        tts?.stop();token++;paused=false;restoreSavedSegment=false;idx++;speakItem()
    }
    private fun pauseRadio(){if(!playing)return;tts?.stop();token++;paused=true;report("paused")}
    private fun resumeRadio(){if(!playing)return;paused=false;if(segments.isNotEmpty())speakSegment() else speakItem()}
    private fun stopRadio(){
        playing=false;paused=false;token++;tts?.stop();sleeper?.let{handler.removeCallbacks(it)}
        report("stopped")
        if(Build.VERSION.SDK_INT>=24)stopForeground(STOP_FOREGROUND_REMOVE)else stopForeground(true)
        stopSelf()
    }
    override fun onDestroy(){playing=false;tts?.stop();tts?.shutdown();sleeper?.let{handler.removeCallbacks(it)}
        session.isActive=false;session.release();super.onDestroy()}
}

@Composable fun PhilosophicalRadioScreen(onBack:()->Unit){
    val context=androidx.compose.ui.platform.LocalContext.current
    val prefs=remember{context.getSharedPreferences("sophia_radio_v6",Context.MODE_PRIVATE)}
    var mode by remember{mutableStateOf(prefs.getString("mode","alternating")?:"alternating")}
    var scope by remember{mutableStateOf(prefs.getString("scope","all")?:"all")}
    var minutes by remember{mutableIntStateOf(30)}
    var title by remember{mutableStateOf("SOPHIA Philosophical Radio")}
    var detail by remember{mutableStateOf("")}
    var status by remember{mutableStateOf("stopped")}
    val notificationPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ /* Playback still works if notification permission is denied. */ }
    LaunchedEffect(Unit){while(true){
        status=prefs.getString("state","stopped")?:"stopped"
        title=prefs.getString("title","SOPHIA Philosophical Radio")?:"SOPHIA Philosophical Radio"
        detail=prefs.getString("detail","")?:"";delay(900)
    }}
    fun send(action:String){
        val i=Intent(context,PhilosophyRadioService::class.java).setAction(action)
        if(action==PhilosophyRadioService.START){
            i.putExtra("mode",mode).putExtra("scope",scope).putExtra("minutes",minutes)
                .putExtra("rate",.9f).putExtra("pitch",1f)
            if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            ContextCompat.startForegroundService(context,i)
        }else context.startService(i)
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
        RadioBanner()
        Text("The Philosophical Radio 2.0",fontSize=26.sp,color=Color(0xFF16533E),fontWeight=FontWeight.ExtraBold)
        Text("Hands-free philosophical passages, original interpretations, and reflective seminars. Your Android voice continues while the phone is locked.",color=Color(0xFF506C60),fontSize=14.sp)
        Text("LISTENING FORMAT",fontSize=11.sp,color=Color(0xFFB18E43),fontWeight=FontWeight.Bold)
        listOf("quotes" to "Random primary passages","commentaries" to "Interpretive commentaries","alternating" to "Passage followed by commentary","seminar" to "Scholarly seminar readings","program" to "Thematic Audio Seminar · reading / counterpoint / reflection").forEach { (value,label)->
            Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){RadioButton(selected=mode==value,onClick={mode=value});Text(label,color=Color(0xFF234636))}
        }
        Text("SELECT FROM",fontSize=11.sp,color=Color(0xFFB18E43),fontWeight=FontWeight.Bold)
        listOf("all" to "All traditions","studied" to "Previously studied courses","weak" to "Due for review","favorites" to "Saved anthology").forEach{(value,label)->
            Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){RadioButton(selected=scope==value,onClick={scope=value});Text(label,color=Color(0xFF234636))}
        }
        Text("SLEEP TIMER",fontSize=11.sp,color=Color(0xFFB18E43),fontWeight=FontWeight.Bold)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
            listOf(15,30,60,0).forEach {duration-> FilterChip(selected=minutes==duration,onClick={minutes=duration},label={Text(if(duration==0)"∞" else "$duration min",fontSize=12.sp)}) }
        }
        ElevatedCard(shape=RoundedCornerShape(20.dp),colors=CardDefaults.elevatedCardColors(containerColor=Color(0xFFF0F5EB))){
            Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                Text("NOW PLAYING",fontSize=11.sp,color=Color(0xFFB18E43),fontWeight=FontWeight.Bold)
                Text(title,fontSize=17.sp,color=Color(0xFF15513B),fontWeight=FontWeight.ExtraBold)
                Text(detail,fontSize=12.sp,color=Color(0xFF52675B))
                Text("Playback: $status",color=Color(0xFF19633E),fontSize=12.sp)
                Button(onClick={send(PhilosophyRadioService.START)},modifier=Modifier.fillMaxWidth()){Text("▶ Start shuffled session")}
                Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){
                    OutlinedButton(onClick={send(if(status=="paused")PhilosophyRadioService.RESUME else PhilosophyRadioService.PAUSE)},modifier=Modifier.weight(1f))
                        {Text(if(status=="paused")"Resume" else "Pause",fontSize=12.sp)}
                    OutlinedButton(onClick={send(PhilosophyRadioService.NEXT)},modifier=Modifier.weight(1f)){Text("Next",fontSize=12.sp)}
                    OutlinedButton(onClick={send(PhilosophyRadioService.STOP)},modifier=Modifier.weight(1f)){Text("Stop",fontSize=12.sp)}
                }
            }
        }
        Text("Thematic programs include an introduction, two source readings when available, commentary and reflective questions. Other queues are randomized, and recently heard passages are avoided until needed again. If no studied, weak, or favorite selections are found, playback falls back to all excerpts. The sleep timer counts wall-clock time, including pauses.",fontSize=12.sp,color=Color.Gray)
        Text("ElevenReader can receive exported passages but does not become SOPHIA’s internal voice unless installed as an Android speech engine.",fontSize=12.sp,color=Color.Gray)
    }
}