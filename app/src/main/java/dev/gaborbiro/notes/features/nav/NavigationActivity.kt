package dev.gaborbiro.notes.features.nav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.RecordsUIMapper
import dev.gaborbiro.notes.features.notes.NotesListNavigatorImpl
import dev.gaborbiro.notes.features.notes.NotesListScreen
import dev.gaborbiro.notes.store.bitmap.BitmapStore
import dev.gaborbiro.notes.design.NotesTheme
import dev.gaborbiro.notes.store.file.FileStoreFactoryImpl

class NavigationActivity : ComponentActivity() {

    private val fileStore = FileStoreFactoryImpl(this).getStore("public", keepFiles = true)
    private val repository by lazy { RecordsRepository.get(fileStore) }
    private val uiMapper: RecordsUIMapper by lazy {
        RecordsUIMapper(BitmapStore(fileStore))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesTheme {
                val navController = rememberNavController()
                NotesNavHost(navController, repository, uiMapper)
            }
        }
    }
}

@Composable
fun NotesNavHost(
    navController: NavHostController,
    repository: RecordsRepository,
    uiMapper: RecordsUIMapper,
) {
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = NoteList.route,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(route = NoteList.route) {
            NotesListScreen(
                repository = repository,
                uiMapper = uiMapper,
                navigator = NotesListNavigatorImpl(context),
            )
        }
    }
}
