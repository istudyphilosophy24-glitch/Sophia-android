package com.sophia.nativeapp

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

/** Six interpretive dimensions. This text is explicitly editorial, not a quoted primary source.
 * Existing/older curriculum commentary has structured pedagogical reconstruction; not all modules
 * have verified author-specific section references or an individual peer-review.
 */
@Composable fun AcademicCommentaryScreen(
    course:Course,unit:UnitLesson,progress:SophiaProgress,
    onSpeak:(String)->Unit,onLibrary:()->Unit,onRadio:()->Unit
){
    val editorial=Color(0xFF1D593F)
    val notes=unit.reading.notes
    var open by remember(course.id,unit.name){ mutableStateOf(setOf(0,1,2)) }
    var dual by remember(course.id,unit.name){ mutableIntStateOf(0) }
    var annotation by remember(course.id,unit.name){mutableStateOf(progress.annotation(course.id,course.units.indexOf(unit)))}
    val paragraphs=listOf(
        "Context & interpretive problem" to notes.orientation,
        "Close reading: distinctions" to notes.closeReading,
        "Reconstructing the argument" to notes.argument,
        "Philology & source orientation" to notes.philology,
        "Rival interpretations" to notes.controversy,
        "Critical assessment" to notes.assessment
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
        Surface(color=Color(0xFF174F3C),shape=RoundedCornerShape(23.dp)){
            Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
                Text("THE SCHOLAR'S COMMENTARY",color=Color(0xFFF9D99D),fontSize=11.sp,fontWeight=FontWeight.Bold)
                Text(unit.name,color=Color.White,fontSize=25.sp,fontWeight=FontWeight.ExtraBold)
                Text(course.name,color=Color(0xFFDBE8DD),fontSize=13.sp)
                Text("Text-oriented study · not a verbatim primary quotation",color=Color(0xFFDBE8DD),fontSize=11.sp)
            }
        }
        Text("INTERPRETIVE THESIS",fontSize=11.sp,fontWeight=FontWeight.Bold,color=Color(0xFFBA8A3A))
        ElevatedCard(colors=CardDefaults.elevatedCardColors(containerColor=Color(0xFFEAF4EA))){
            Text(unit.reading.thesis.ifBlank{"What justifies the chapter's central explanatory distinction?"},
                modifier=Modifier.fillMaxWidth().padding(15.dp),color=editorial,lineHeight=23.sp)
        }
        paragraphs.forEachIndexed{i,(heading,body)->
            ElevatedCard(onClick={open=if(i in open)open-i else open+i},colors=CardDefaults.elevatedCardColors(containerColor=Color.White)){
                Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
                    Text("${i+1}. $heading ${if(i in open)"⌃" else "⌄"}",fontWeight=FontWeight.Bold,color=editorial,fontSize=16.sp)
                    if(i in open)Text(body,color=Color(0xFF273B31),fontSize=15.sp,lineHeight=24.sp)
                }
            }
        }
        Text("COMPETING INTERPRETATIONS",fontSize=11.sp,fontWeight=FontWeight.Bold,color=Color(0xFFBA8A3A))
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
            FilterChip(selected=dual==0,onClick={dual=0},label={Text("Charitable reconstruction")})
            FilterChip(selected=dual==1,onClick={dual=1},label={Text("Critical counter-reading")})
        }
        ElevatedCard(colors=CardDefaults.elevatedCardColors(containerColor=Color(0xFFF7F4EC))){
            Text(if(dual==0)"A charitable reading begins from the scope of the author's problem. ${unit.reading.thesis}. It distinguishes the doctrines before assuming they entail one another. The task is to reconstruct the best reasons available, not to treat the school label as evidence."
                else "A critical interpretation asks what would have to be shown for this position to fail: ${unit.reading.objection}. Any disagreement should target a textual claim, premise, or inference—not simply substitute another vocabulary.",
                Modifier.padding(15.dp),color=Color(0xFF283D31),lineHeight=23.sp)
        }
        Text("EDITORIAL NOTE",fontSize=11.sp,color=Color(0xFFBA8A3A),fontWeight=FontWeight.Bold)
        Text(notes.editorialNote, fontSize=12.sp,color=Color.Gray)
        Text("Source orientation: ${unit.reading.source.ifBlank{"Primary section not yet verified"}}",fontSize=12.sp,color=Color.Gray)
        OutlinedTextField(value=annotation,onValueChange={
            annotation=it
            progress.saveAnnotation(course.id,course.units.indexOf(unit),it)
        },label={Text("Your interpretation and objections")},modifier=Modifier.fillMaxWidth().height(160.dp))
        Button(onClick={onSpeak(paragraphs.joinToString(". "){it.first+". "+it.second})},modifier=Modifier.fillMaxWidth()) {Text("🔊 Narrate scholarly commentary")}
        OutlinedButton(onClick=onLibrary,modifier=Modifier.fillMaxWidth()){Text("📖 Compare with original passages")}
        OutlinedButton(onClick=onRadio,modifier=Modifier.fillMaxWidth()){Text("🎧 Start Philosophical Radio")}
    }
}
