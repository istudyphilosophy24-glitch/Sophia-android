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

@Composable fun ScholarCollection7(courses:List<Course>,progress:SophiaProgress){
    val completed=progress.completed
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(11.dp)){
        Text("THE PHILOSOPHER COLLECTION",color=Color(0xFF9A732B),fontWeight=FontWeight.ExtraBold,fontSize=12.sp)
        Text("Learn to unlock portraits—not to purchase them.",color=Color.Gray,fontSize=12.sp)
        courses.forEachIndexed{index,course->
            val earned=completed.any{it.startsWith(course.id+":")}
            ElevatedCard(shape=RoundedCornerShape(15.dp),colors=CardDefaults.elevatedCardColors(containerColor=if(earned)Color(0xFFE7F4E6)else Color(0xFFF2F3F0))){
                Row(Modifier.fillMaxWidth().padding(10.dp)){
                    if(earned)PhilosopherAvatar(index,Modifier.size(67.dp))
                    else Surface(Modifier.size(67.dp),shape=RoundedCornerShape(13.dp),color=Color(0xFFD8E0D9)){
                        Box(contentAlignment=androidx.compose.ui.Alignment.Center){Text("?",fontSize=30.sp,color=Color(0xFF87988C))}
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)){
                        Text(course.name,color=Color(0xFF20553C),fontWeight=FontWeight.SemiBold)
                        Text(if(earned)"✦ Discovered through study" else "Complete any lesson to discover this tradition",fontSize=11.sp,color=Color.Gray)
                    }
                }
            }
        }
    }
}