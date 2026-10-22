package cn.minimal.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "tasks")
data class Task(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val status: Int = 0)
@Dao
interface Tasks {
 @Query("SELECT * FROM tasks ORDER BY id DESC") fun observe(): Flow<List<Task>>
 @Insert suspend fun add(task: Task)
 @Update suspend fun update(task: Task)
 @Delete suspend fun delete(task: Task)
}
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class Store : RoomDatabase() { abstract fun tasks(): Tasks }
class MainActivity : ComponentActivity() {
 private val store by lazy { Room.databaseBuilder(applicationContext, Store::class.java, "todo.db").build() }
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  window.statusBarColor = android.graphics.Color.BLACK
  window.navigationBarColor = android.graphics.Color.BLACK
  setContent {
   MaterialTheme(colorScheme = darkColorScheme(background = Color.Black, surface = Color(0xFF202020), primary = Color(0xFFFFD65A), onPrimary = Color.Black)) { Todo(store.tasks()) }
  }
 }
}
@Composable
fun Todo(dao: Tasks) {
 val tasks by dao.observe().collectAsStateWithLifecycle(initialValue = emptyList())
 val scope = rememberCoroutineScope()
 var editing by remember { mutableStateOf<Task?>(null) }
 var dialog by rememberSaveable { mutableStateOf(false) }
 var name by rememberSaveable { mutableStateOf("") }
 var deleting by remember { mutableStateOf<Task?>(null) }
 var successOpen by rememberSaveable { mutableStateOf(false) }
 var failedOpen by rememberSaveable { mutableStateOf(false) }
 val success = tasks.filter { it.status == 1 }
 val failed = tasks.filter { it.status == 2 }
 val pending = tasks.filter { it.status == 0 }
 fun edit(t: Task?) { editing = t; name = t?.name ?: ""; dialog = true }
 Scaffold(containerColor = Color.Black, floatingActionButton = {
  FloatingActionButton(onClick = { edit(null) }, containerColor = Color(0xFFFFD65A), shape = androidx.compose.foundation.shape.CircleShape) { Text("+", fontSize = 32.sp, color = Color.Black) }
 }) { padding ->
  LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 28.dp, bottom = 100.dp)) {
   item { Text("待办", fontSize = 40.sp, color = Color.White, modifier = Modifier.padding(bottom = 20.dp)) }
   if (pending.isEmpty()) item { Text("暂无待办，点击 + 添加", color = Color.Gray, modifier = Modifier.padding(vertical = 30.dp)) }
   items(pending, key = { it.id }) { t -> TaskRow(t, { state -> scope.launch { dao.update(t.copy(status = state)) } }, { edit(t) }, { deleting = t }) }
   item { Spacer(Modifier.height(36.dp)); Text("历史统计", color = Color.Gray, fontSize = 16.sp) }
   item {
    Row(Modifier.fillMaxWidth().background(Color(0xFF191919), RoundedCornerShape(20.dp)).padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
     Column(Modifier.clickable { successOpen = !successOpen }) { Text("✓ 成功", color = Color.Gray); Text("${success.size}", fontSize = 28.sp); Text(if(successOpen) "收起 ▴" else "展开 ▾", fontSize = 12.sp, color = Color.Gray) }
     Column(Modifier.clickable { failedOpen = !failedOpen }) { Text("× 失败", color = Color.Gray); Text("${failed.size}", fontSize = 28.sp); Text(if(failedOpen) "收起 ▴" else "展开 ▾", fontSize = 12.sp, color = Color.Gray) }
     Column { Text("成功率", color = Color.Gray); Text(if(success.size + failed.size == 0) "0%" else "${success.size * 100 / (success.size + failed.size)}%", fontSize = 28.sp) }
    }
   }
   if(successOpen) { item { Text("成功记录", color = Color.Gray) }; items(success, key = { it.id }) { t -> TaskRow(t, { s -> scope.launch { dao.update(t.copy(status=s)) } }, { edit(t) }, { deleting=t }) } }
   if(failedOpen) { item { Text("失败记录", color = Color.Gray) }; items(failed, key = { it.id }) { t -> TaskRow(t, { s -> scope.launch { dao.update(t.copy(status=s)) } }, { edit(t) }, { deleting=t }) } }
  }
 }
 if(dialog) AlertDialog(onDismissRequest = { dialog=false }, title = { Text(if(editing == null) "添加任务" else "编辑任务") }, text = { OutlinedTextField(value=name, onValueChange={name=it}, label={Text("任务名称")}, singleLine=true) }, confirmButton={ TextButton(enabled=name.isNotBlank(), onClick={ val old=editing; val value=name.trim(); scope.launch { if(old == null) dao.add(Task(name=value)) else dao.update(old.copy(name=value)) }; dialog=false }) { Text("保存") } }, dismissButton={TextButton(onClick={dialog=false}){Text("取消")}})
 deleting?.let { t -> AlertDialog(onDismissRequest={deleting=null}, title={Text("删除任务？")}, text={Text("将永久删除“${t.name}”，并更新统计。")}, confirmButton={TextButton(onClick={scope.launch {dao.delete(t)}; deleting=null}){Text("删除")}}, dismissButton={TextButton(onClick={deleting=null}){Text("取消")}}) }
}
@Composable
fun TaskRow(task: Task, settle: (Int)->Unit, edit: ()->Unit, delete: ()->Unit) {
 var statusMenu by remember { mutableStateOf(false) }
 var actions by remember { mutableStateOf(false) }
 Row(Modifier.fillMaxWidth().background(Color(0xFF202020), RoundedCornerShape(18.dp)).padding(8.dp), verticalAlignment=Alignment.CenterVertically) {
  Box {
   TextButton(onClick={statusMenu=true}) { Text(when(task.status){1->"✓";2->"×";else->"□"}, fontSize=27.sp, color=Color.White) }
   DropdownMenu(expanded=statusMenu, onDismissRequest={statusMenu=false}) {
    DropdownMenuItem(text={Text("✓ 完成")}, onClick={statusMenu=false;settle(1)})
    DropdownMenuItem(text={Text("× 失败")}, onClick={statusMenu=false;settle(2)})
    if(task.status != 0) DropdownMenuItem(text={Text("□ 恢复待办")}, onClick={statusMenu=false;settle(0)})
   }
  }
  Text(task.name, color=Color.White, fontSize=17.sp, modifier=Modifier.weight(1f).padding(vertical=16.dp))
  Box { TextButton(onClick={actions=true}){Text("⋮",color=Color.Gray,fontSize=24.sp)}; DropdownMenu(expanded=actions,onDismissRequest={actions=false}) { DropdownMenuItem(text={Text("编辑")},onClick={actions=false;edit()}); DropdownMenuItem(text={Text("删除")},onClick={actions=false;delete()}) } }
 }
}

