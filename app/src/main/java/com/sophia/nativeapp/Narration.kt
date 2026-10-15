package com.sophia.nativeapp

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/** Select the actual Android TTS engine and its voice from inside SOPHIA.
 * Apps that do not implement Android TextToSpeechService cannot be shown as an engine.
 */
data class AndroidEngine(val id:String,val name:String)
data class AndroidVoice(val id:String,val name:String)
class NarratorController(private val activity:ComponentActivity){
    private val prefs=activity.getSharedPreferences("sophia_narrator_v5",0)
    private var tts:TextToSpeech?=null
    val engines=mutableStateListOf<AndroidEngine>()
    val voices=mutableStateListOf<AndroidVoice>()
    var engineId by mutableStateOf(prefs.getString("engine","")?:"")
        private set
    var voiceId by mutableStateOf(prefs.getString("voice","")?:"")
        private set
    var status by mutableStateOf("Discovering installed Android speech engines…")
        private set
    init { discover();chooseEngine(engineId) }
    private fun discover(){
        val intent=Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE)
        val services=activity.packageManager.queryIntentServices(intent,0)
        val found=services.map { AndroidEngine(it.serviceInfo.packageName,
            it.loadLabel(activity.packageManager)?.toString()?:it.serviceInfo.packageName) }.distinctBy { it.id }
        engines.clear();engines.add(AndroidEngine("","System default"));engines.addAll(found)
    }
    fun chooseEngine(id:String){
        tts?.stop();tts?.shutdown();voices.clear()
        engineId=id;prefs.edit().putString("engine",id).apply()
        status="Connecting to Android speech service…"
        val listener=TextToSpeech.OnInitListener { result ->
            if(result==TextToSpeech.SUCCESS){
                val speaker=tts
                speaker?.language=Locale.UK
                val available=speaker?.voices.orEmpty().sortedWith(compareBy({it.locale.toLanguageTag()},{it.name}))
                voices.clear();voices.addAll(available.map { AndroidVoice(it.name,"${it.locale.displayName} · ${it.name.substringAfterLast('.')}" ) })
                val choice=available.firstOrNull { it.name==voiceId }
                if(choice!=null) speaker?.voice=choice
                status="Ready: ${if(id.isBlank())"system default" else id} · ${voices.size} voices"
            } else status="Unable to initialize that speech engine. Try the system default."
        }
        tts=if(id.isBlank())TextToSpeech(activity,listener) else TextToSpeech(activity,listener,id)
    }
    fun chooseVoice(id:String){
        val chosen=tts?.voices?.firstOrNull{it.name==id}
        if(chosen!=null){ tts?.voice=chosen;voiceId=id;prefs.edit().putString("voice",id).apply();status="Voice: ${chosen.locale.displayName} · ${chosen.name}" }
    }
    fun speak(text:String,rate:Float,pitch:Float){
        val speaker=tts?:run { status="Speech engine not ready";return }
        speaker.setSpeechRate(rate);speaker.setPitch(pitch);speaker.stop()
        if(text.isBlank())return
        text.chunked(2200).forEachIndexed {i,part->speaker.speak(part,if(i==0)TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,null,"sophia_${i}")}
    }
    fun stop(){tts?.stop()}
    fun release(){tts?.stop();tts?.shutdown()}
}

@Composable fun NarratorSettings(controller:NarratorController,
    rate:Float,onRate:(Float)->Unit,pitch:Float,onPitch:(Float)->Unit){
    var openEngine by remember{mutableStateOf(false)}
    var openVoice by remember{mutableStateOf(false)}
    Text("🎙 IN-APP NARRATOR",fontSize=19.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF16563E))
    Text("Choose a TTS service already installed on this phone. The selection applies to lesson and excerpt playback.",fontSize=12.sp,color=Color.Gray)
    Box {
        OutlinedButton(onClick={openEngine=true},modifier=Modifier.fillMaxWidth()){
            Text("Speech engine: "+(controller.engines.firstOrNull{it.id==controller.engineId}?.name?:"System default")+" ▾")
        }
        DropdownMenu(expanded=openEngine,onDismissRequest={openEngine=false}){
            controller.engines.forEach{e->DropdownMenuItem(text={Text(e.name)},onClick={openEngine=false;controller.chooseEngine(e.id)})}
        }
    }
    Box {
        OutlinedButton(enabled=controller.voices.isNotEmpty(),onClick={openVoice=true},modifier=Modifier.fillMaxWidth()){
            Text("Voice: "+(controller.voices.firstOrNull{it.id==controller.voiceId}?.name?:"Engine default")+" ▾",maxLines=2)
        }
        DropdownMenu(expanded=openVoice,onDismissRequest={openVoice=false}){
            Column(Modifier.heightIn(max=380.dp)){
                controller.voices.forEach{v->DropdownMenuItem(text={Text(v.name,fontSize=12.sp)},onClick={openVoice=false;controller.chooseVoice(v.id)})}
            }
        }
    }
    Text("Speed ${"%.2f".format(rate)}×",fontSize=13.sp)
    Slider(value=rate,onValueChange=onRate,valueRange=.65f..1.35f)
    Text("Pitch ${"%.2f".format(pitch)}",fontSize=13.sp)
    Slider(value=pitch,onValueChange=onPitch,valueRange=.7f..1.25f)
    Button(onClick={controller.speak("Welcome to SOPHIA. Philosophy begins in wonder, and grows through careful questioning.",rate,pitch)},modifier=Modifier.fillMaxWidth()){Text("▶ Preview my selected voice")}
    Text(controller.status,color=Color(0xFF32654B),fontSize=11.sp)
    Text("ElevenReader is primarily a document-reading app, not necessarily an Android TTS engine. If it is not listed above, use the separate Share Reading option to import a selected excerpt into ElevenReader.",color=Color.Gray,fontSize=12.sp,lineHeight=18.sp)
}