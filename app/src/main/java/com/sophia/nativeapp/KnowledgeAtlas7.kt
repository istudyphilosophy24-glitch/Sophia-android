package com.sophia.nativeapp

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Curated conceptual links with explicit relation types, not spurious entailments. */
data class AtlasEdge7(val from:String,val to:String,val relation:String,val note:String)
val curatedEdges7=listOf(
    AtlasEdge7("Phenomenology","Existentialism","historical dialogue","Husserl's analysis of intentionality helps frame important later existential inquiries, although the methods differ."),
    AtlasEdge7("Phenomenology","Hermeneutics","methodological debate","Interpretation and historical situatedness complicate accounts of pure description."),
    AtlasEdge7("Hegelian Philosophy","Critical Theory","historical reception","Hegelian dialectic informs diverse strands of later social critique."),
    AtlasEdge7("Hegelian Philosophy","Post-Structuralism","critical encounter","Later theorists challenge teleological and totalizing readings of dialectical development."),
    AtlasEdge7("Kantian Transcendental Philosophy","Schopenhauerian Philosophy","critical appropriation","Schopenhauer inherits and transforms the appearance/thing-in-itself distinction."),
    AtlasEdge7("Schopenhauerian Philosophy","Buddhist Metaphysics","comparative interpretation","Claims about suffering, will and liberation invite comparison; the traditions are not equivalent."),
    AtlasEdge7("Schopenhauerian Philosophy","Existentialism","thematic comparison","Pessimism, agency and meaning are differently articulated across these traditions."),
    AtlasEdge7("Socratic Philosophy","Platonic Philosophy","historical transmission","Plato's dialogues represent Socrates while advancing Plato's distinctive philosophical projects."),
    AtlasEdge7("Platonic Philosophy","Aristotelian Philosophy","explicit criticism","Aristotle critically develops and contests aspects of Platonic metaphysics."),
    AtlasEdge7("The Scientific Revolution","Positivism","historical development","Classical positivism generalizes scientific methods into a broader philosophy of knowledge."),
    AtlasEdge7("Positivism","Philosophy of Science","methodological reassessment","Later philosophy of science reconsiders observation, explanation and theories of progress."),
    AtlasEdge7("Logic","Analytic Philosophy","methodological influence","Formal logic is central to many twentieth-century analytic projects."),
    AtlasEdge7("Analytic Philosophy","Philosophy of Language","shared problems","Reference, logical form and meaning occupy both traditions."),
    AtlasEdge7("Philosophy of Language","Deconstruction & Derrida","interpretive contrast","Deconstruction questions assumptions about presence, writing and determinate meaning."),
    AtlasEdge7("Post-Structuralism","Deconstruction & Derrida","overlapping tradition","Deconstruction is related to but cannot be reduced to the entire post-structuralist movement."),
    AtlasEdge7("Psychoanalysis","Post-Structuralism","critical reception","Psychoanalytic concepts are reworked in several post-structuralist approaches."),
    AtlasEdge7("General Indian Philosophy","Vedantic Philosophy","historical relation","Vedānta is one family within the diverse Indian philosophical landscape."),
    AtlasEdge7("General Indian Philosophy","Buddhist Metaphysics","philosophical disagreement","Distinct positions on self, dependence and liberation invite careful comparative study."),
    AtlasEdge7("Chinese Philosophy","Buddhist Metaphysics","historical encounter","Buddhist schools influenced Chinese philosophical and religious thought in complex ways."),
    AtlasEdge7("Medieval Philosophy","Islamic Philosophy","historical transmission","Latin scholastic thinkers engaged significantly with Arabic philosophical traditions."),
    AtlasEdge7("Epistemology","Philosophy of Mind","overlapping question","Consciousness, mental representation and justification raise connected but different problems."),
    AtlasEdge7("Aesthetics","Schopenhauerian Philosophy","thematic connection","Art and aesthetic contemplation acquire a distinctive metaphysical function in Schopenhauer."),
    AtlasEdge7("Postmodern Philosophy","Critical Theory","critical disagreement","Accounts of emancipation and metanarratives are contested within these traditions.")
)
@Composable fun KnowledgeAtlas7(courses:List<Course>,onCourse:(Int)->Unit){
    var selected by remember{mutableStateOf("Phenomenology")}
    var filter by remember{mutableStateOf("All")}
    val names=courses.map{it.name}
    val edges=curatedEdges7.filter{it.from==selected||it.to==selected}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("✦ THE LIVING ATLAS",color=Color(0xFFBC9046),fontWeight=FontWeight.Bold,fontSize=12.sp)
        Text("30 traditions, connected",fontSize=26.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF145F42))
        Text("Tap a tradition to inspect historically grounded links and disagreements. Connections indicate questions for study, not logical entailment.",color=Color(0xFF516A59),lineHeight=21.sp)
        OutlinedTextField(value=filter,onValueChange={filter=it},label={Text("Filter traditions (clear 'All' to search)")},modifier=Modifier.fillMaxWidth())
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)){
            courses.filter{filter=="All"||it.name.contains(filter,true)}.forEach{c->
                FilterChip(selected=selected==c.name,onClick={selected=c.name},label={Text(c.icon+" "+c.name,fontSize=12.sp)})
            }
        }
        ElevatedCard(colors=CardDefaults.elevatedCardColors(containerColor=Color(0xFFE7F2E7)),shape=RoundedCornerShape(23.dp)){
            Column(Modifier.fillMaxWidth().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){
                Text("◉",fontSize=45.sp,color=Color(0xFF1E805D))
                Text(selected,fontSize=22.sp,fontWeight=FontWeight.Bold,color=Color(0xFF145F42))
                Text("${edges.size} curated relationships",fontSize=13.sp,color=Color.Gray)
            }
        }
        if(edges.isEmpty())Text("No vetted cross-course link is currently recorded. This does not imply intellectual isolation.")
        edges.forEach{edge->
            val other=if(edge.from==selected)edge.to else edge.from
            ElevatedCard(onClick={selected=other},modifier=Modifier.fillMaxWidth()){
                Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
                    Text("◉ ─── ◉   $other",color=Color(0xFF155D42),fontWeight=FontWeight.Bold,fontSize=17.sp)
                    Text(edge.relation.uppercase(),color=Color(0xFFB0833B),fontSize=10.sp,fontWeight=FontWeight.ExtraBold)
                    Text(edge.note,color=Color(0xFF374F41),lineHeight=20.sp,fontSize=13.sp)
                }
            }
        }
        val cidx=names.indexOf(selected)
        if(cidx>=0)Button(onClick={onCourse(cidx)},modifier=Modifier.fillMaxWidth()){Text("Open ${selected} course →")}
        Text("The links above were explicitly authored for SOPHIA. Additional within-course similarities are presented as study prompts rather than verified historical influences.",color=Color.Gray,fontSize=12.sp)
    }
}