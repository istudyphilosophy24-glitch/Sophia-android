package com.sophia.nativeapp

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
import kotlin.random.Random

private val emerald7=Color(0xFF155E45)
private val ink7=Color(0xFF294035)
private val gold7=Color(0xFFBC8F42)
private val practiceTypes=listOf("Concept Forge","Argument Architect","Dialectical Tribunal","Interpretive Detective")

/** Four distinct interaction mechanics. Outcomes enter the same concept-skill ledger as other lessons. */
@Composable fun DialecticalMiniGame7(course:Course,unit:UnitLesson,unitIndex:Int,lessonIndex:Int,
    excerpts:List<PrimaryBook>,mastery:Mastery7,onComplete:(Int)->Unit,onExit:()->Unit){
    val mode=when(lessonIndex){5,6->0;7->1;8->2;else->3}
    val label=practiceTypes[mode]
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
        Text("✦ INTERACTIVE PHILOSOPHY",color=gold7,fontSize=12.sp,fontWeight=FontWeight.ExtraBold)
        Text(label,color=emerald7,fontSize=28.sp,fontWeight=FontWeight.ExtraBold)
        Text(course.name+" · "+unit.name,color=ink7,fontSize=13.sp)
        LinearProgressIndicator(progress=(mode+1)/4f,modifier=Modifier.fillMaxWidth().height(7.dp),color=Color(0xFF47A676))
        when(mode){
            0->ConceptForge7(unit,lessonIndex,mastery,onComplete)
            1->ArgumentArchitect7(unit,mastery,onComplete)
            2->DialecticalTribunal7(unit,course,mastery,onComplete)
            else->InterpretiveDetective7(unit,course,lessonIndex,excerpts,mastery,onComplete)
        }
        OutlinedButton(onClick=onExit,modifier=Modifier.fillMaxWidth()){Text("Return to the reading")}
    }
}

@Composable private fun ConceptForge7(unit:UnitLesson,lessonIndex:Int,mastery:Mastery7,onComplete:(Int)->Unit){
    val cc=unit.concepts
    val candidates=remember(unit.name,lessonIndex){(0..3).map{cc[(it+lessonIndex)%cc.size]}.shuffled(Random(unit.name.hashCode()+lessonIndex))}
    val definitions=remember(unit.name){candidates.shuffled(Random(13+unit.name.hashCode()))}
    var chosenTerm by remember(unit.name){mutableStateOf<String?>(null)}
    var solved by remember(unit.name){mutableStateOf(setOf<String>())}
    var mistakes by remember(unit.name){mutableIntStateOf(0)}
    var message by remember(unit.name){mutableStateOf("Select a term, then tap the explanation that belongs to it.")}
    Text("Match each concept to its philosophical role. Unlike a multiple-choice quiz, you must build the correct connections.",color=ink7,lineHeight=22.sp)
    Text("TERMS",color=gold7,fontWeight=FontWeight.Bold)
    candidates.forEach { concept->
        val complete=concept.term in solved
        OutlinedCard(onClick={if(!complete)chosenTerm=concept.term},modifier=Modifier.fillMaxWidth(),
            colors=CardDefaults.outlinedCardColors(containerColor=if(complete)Color(0xFFDEF5E6) else if(chosenTerm==concept.term)Color(0xFFFFF0CB) else Color.White)){
            Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                Text(if(complete)"✓" else "◇",color=emerald7,fontSize=24.sp)
                Spacer(Modifier.width(12.dp));Text(concept.term,color=ink7,fontWeight=FontWeight.Bold)
            }
        }
    }
    Text("EXPLANATIONS",color=gold7,fontWeight=FontWeight.Bold)
    definitions.filter{it.term !in solved}.forEach { item->
        ElevatedCard(onClick={
            val target=chosenTerm
            if(target==null){message="Choose a concept first."}
            else{
                val correct=target==item.term
                mastery.record(target,"comprehension",correct)
                if(correct){solved=solved+target;chosenTerm=null;message="✓ Connection established: ${item.term}"}
                else{mistakes++;message="Not quite. The explanation belongs to ${item.term}; try again."}
            }
        },modifier=Modifier.fillMaxWidth()){
            Text(item.definition,Modifier.padding(13.dp),color=ink7,lineHeight=21.sp)
        }
    }
    Text(message,color=emerald7,fontWeight=FontWeight.SemiBold)
    Text("${solved.size} / ${candidates.size} links · $mistakes errors",color=Color.Gray,fontSize=12.sp)
    if(solved.size==candidates.size)Button(onClick={onComplete((8-mistakes).coerceIn(0,8))},modifier=Modifier.fillMaxWidth()){Text("Complete the Concept Forge")}
}

private data class ArgumentPuzzle7(val title:String,val steps:List<String>,val explanation:String)
private val argumentPuzzles7=listOf(
    ArgumentPuzzle7("Conditional reasoning",listOf("P1. If a doctrine entails Q, then Q follows when it holds.","P2. This doctrine holds.","C. Therefore Q follows."),"Modus ponens is valid regardless of whether the antecedent is actually true."),
    ArgumentPuzzle7("Refutation",listOf("P1. If the proposed principle were correct, consequence Q would follow.","P2. Consequence Q is demonstrably false.","C. Therefore the proposed principle is false."),"Modus tollens; falsifying a necessary consequence refutes the antecedent."),
    ArgumentPuzzle7("Categorical reasoning",listOf("P1. Every A belongs to B.","P2. Every B belongs to C.","C. Every A belongs to C."),"A valid categorical syllogism. Truth of the premises must be assessed independently."),
    ArgumentPuzzle7("Disjunctive elimination",listOf("P1. Either P or Q is true.","P2. P is false.","C. Therefore Q is true."),"Disjunctive syllogism, provided the stated alternatives are exhaustive."),
    ArgumentPuzzle7("Multi-step inference",listOf("P1. If P then Q.","P2. If Q then R.","P3. P is true.","C. Therefore R is true."),"Two applications of modus ponens; the form is deductively valid."),
    ArgumentPuzzle7("Counterexample method",listOf("P1. The proposed universal claim says every A is B.","P2. This particular A has been shown not to be B.","C. The universal claim is false."),"One genuine counterexample can refute a universal generalization.")
)
@Composable private fun ArgumentArchitect7(unit:UnitLesson,mastery:Mastery7,onComplete:(Int)->Unit){
    val scenario=remember(unit.name){argumentPuzzles7[(unit.name.hashCode().ushr(1))%argumentPuzzles7.size]}
    var order by remember(unit.name){mutableStateOf(scenario.steps.indices.shuffled(Random(unit.name.hashCode())).toList())}
    var evaluated by remember(unit.name){mutableStateOf(false)}
    val correct=order==scenario.steps.indices.toList()
    var attempts by remember(unit.name){mutableIntStateOf(0)}
    Text("Rearrange the claims until the premises justify the conclusion. The claims below are FORMAL EXAMPLES, not quotations from ${unit.name}.",color=ink7,lineHeight=22.sp)
    Text(scenario.title,color=emerald7,fontWeight=FontWeight.Bold,fontSize=18.sp)
    order.forEachIndexed { position,index->
        OutlinedCard(modifier=Modifier.fillMaxWidth()){
            Row(Modifier.fillMaxWidth().padding(9.dp),verticalAlignment=Alignment.CenterVertically){
                Surface(color=Color(0xFFE5F2E9),shape=RoundedCornerShape(8.dp)){
                    Text("${position+1}",Modifier.padding(10.dp),color=emerald7,fontWeight=FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Text(scenario.steps[index],Modifier.weight(1f),color=ink7,fontSize=14.sp)
                Column {
                    TextButton(enabled=position>0,onClick={val next=order.toMutableList();val t=next[position-1];next[position-1]=next[position];next[position]=t;order=next;evaluated=false}){Text("▲")}
                    TextButton(enabled=position<order.lastIndex,onClick={val next=order.toMutableList();val t=next[position+1];next[position+1]=next[position];next[position]=t;order=next;evaluated=false}){Text("▼")}
                }
            }
        }
    }
    Button(onClick={evaluated=true;attempts++;mastery.record(unit.concepts.first().term,"reconstruction",correct)},modifier=Modifier.fillMaxWidth()){Text("Check argumentative structure")}
    if(evaluated){
        Text(if(correct)"✓ You reconstructed a valid form." else "An inference is not complete until the conclusion is grounded in its premises. Try rearranging.",color=emerald7)
        Text(scenario.explanation,color=ink7,lineHeight=21.sp)
        if(correct)Button(onClick={onComplete((9-attempts).coerceIn(3,8))},modifier=Modifier.fillMaxWidth()){Text("Claim argumentative mastery")}
    }
    Text("Now ask: is the thesis of ${unit.name} supported by a sound argument? Formal validity does not establish its premises.",color=Color.Gray,fontSize=12.sp)
}

@Composable private fun DialecticalTribunal7(unit:UnitLesson,course:Course,mastery:Mastery7,onComplete:(Int)->Unit){
    val objection=unit.reading.objection.ifBlank{"What evidence would show the account inadequate?"}
    val thesis=unit.reading.thesis.ifBlank{"The chapter proposes a position involving ${unit.concepts.first().term}."}
    val stages=listOf("Interpret the claim","Confront a serious objection","Respond without evasion")
    val correctMoves=listOf(
        "Clarify the thesis and identify which terms require definition.",
        "Target a particular premise or consequence: $objection",
        "Concede what the objection establishes, qualify the claim, and supply further reasons."
    )
    val distractors=listOf(
        listOf("A prestigious author's claim requires no clarification.","If people disagree, no interpretation can be justified."),
        listOf("Attack the author's personality instead of the thesis.","Assume every competing explanation is false by definition."),
        listOf("Repeat the thesis more loudly without addressing criticism.","Give up every claim merely because a counterargument exists.")
    )
    var phase by remember(unit.name){mutableIntStateOf(0)}
    var correctCount by remember(unit.name){mutableIntStateOf(0)}
    var selected by remember(unit.name){mutableStateOf<String?>(null)}
    val opts=remember(phase,unit.name){(distractors[phase]+correctMoves[phase]).shuffled(Random(phase*17+unit.name.hashCode()))}
    LinearProgressIndicator(progress=(phase+1)/3f,modifier=Modifier.fillMaxWidth().height(9.dp))
    Text("PHASE ${phase+1}/3 · ${stages[phase]}",fontWeight=FontWeight.Bold,color=gold7)
    ElevatedCard(colors=CardDefaults.elevatedCardColors(containerColor=Color(0xFFF0F7F0))){Text(thesis,Modifier.padding(14.dp),color=ink7,lineHeight=22.sp)}
    if(phase>0)Text("EXAMINER'S CHALLENGE: $objection",color=ink7,fontWeight=FontWeight.SemiBold)
    Text("Which move genuinely advances the dialectic?",fontWeight=FontWeight.Bold,color=emerald7)
    opts.forEach{choice->OutlinedCard(onClick={if(selected==null)selected=choice},colors=CardDefaults.outlinedCardColors(containerColor=if(selected==choice)Color(0xFFEBF4E1)else Color.White),modifier=Modifier.fillMaxWidth()){
        Text(choice,Modifier.padding(13.dp),color=ink7,lineHeight=21.sp)
    }}
    if(selected!=null){
        val ok=selected==correctMoves[phase]
        Text(if(ok)"✓ This responds to the relevant philosophical problem." else "This move fails to answer the strongest objection.",color=emerald7)
        Text("Strong move: ${correctMoves[phase]}",color=ink7,fontSize=13.sp)
        Button(onClick={
            mastery.record(unit.concepts[phase].term,"criticism",ok)
            if(ok)correctCount++
            if(phase==2)onComplete((correctCount*8/3).coerceIn(0,8)) else{phase++;selected=null}
        },modifier=Modifier.fillMaxWidth()){Text(if(phase==2)"Conclude the Tribunal" else "Next dialectical phase")}
    }
    Text("A successful response is assessed as fidelity to the displayed argumentative task—not the ultimate truth of ${course.name}.",color=Color.Gray,fontSize=12.sp)
}

@Composable private fun InterpretiveDetective7(unit:UnitLesson,course:Course,lessonIndex:Int,books:List<PrimaryBook>,mastery:Mastery7,onComplete:(Int)->Unit){
    val all=remember(books){books.flatMap{b->b.excerpts.map{Pair(b,it)}}}
    val source=remember(course.id,unit.name,lessonIndex,all.size){
        all.filter{(b,_)->b.tradition.contains(course.name.substringBefore(" "),ignoreCase=true)}.getOrNull(lessonIndex%all.filter{(b,_)->b.tradition.contains(course.name.substringBefore(" "),ignoreCase=true)}.size.coerceAtLeast(1))
            ?:all.getOrNull((course.id.hashCode().ushr(1))%all.size.coerceAtLeast(1))
    }
    if(source==null){Text("No verified source is bundled for this detective exercise.");Button(onClick={onComplete(0)}){Text("Return")};return}
    val (book,excerpt)=source
    var step by remember(unit.name){mutableIntStateOf(0)}
    var chosen by remember(unit.name){mutableStateOf<String?>(null)}
    var score by remember(unit.name){mutableIntStateOf(0)}
    val claims=listOf(
        "The words in the excerpt are PRIMARY TEXT; the accompanying explanation is editorial." to true,
        "A historical translation proves that every rival translation must use identical terminology." to false,
        "A philosophical passage must be examined in its argumentative context before inferring the author's whole system." to true
    )
    Text("SOURCE INVESTIGATION · ${book.title}",color=gold7,fontWeight=FontWeight.Bold)
    Text(book.author+" · "+book.translator,color=Color.Gray,fontSize=12.sp)
    ElevatedCard{Text(excerpt.passage.take(900),Modifier.padding(14.dp),color=ink7,lineHeight=24.sp)}
    Text("EVIDENCE CHECK ${step+1}/3",color=emerald7,fontWeight=FontWeight.Bold)
    Text(claims[step].first,color=ink7,fontSize=18.sp)
    listOf("Warranted reading practice","Unwarranted interpretive leap").forEach{label->
        OutlinedButton(onClick={if(chosen==null)chosen=label},modifier=Modifier.fillMaxWidth()){Text(label)}
    }
    if(chosen!=null){
        val correct=(chosen=="Warranted reading practice")==claims[step].second
        Text(if(correct)"✓ Careful textual reasoning." else "This choice confuses source fidelity with unsupported inference.",color=emerald7)
        Button(onClick={
            mastery.record(unit.concepts[step].term,"comprehension",correct)
            if(correct)score++
            if(step==2)onComplete(score*8/3) else{step++;chosen=null}
        },modifier=Modifier.fillMaxWidth()){Text(if(step==2)"Finish textual investigation" else "Next evidence test")}
    }
    Text("A useful detective never confuses the original passage with SOPHIA's commentary, and never infers a complete doctrine from a decontextualized selection.",color=Color.Gray,fontSize=12.sp)
}