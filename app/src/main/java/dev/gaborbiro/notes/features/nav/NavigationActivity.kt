package dev.gaborbiro.notes.features.nav

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.RecordsUIMapper
import dev.gaborbiro.notes.features.notes.NotesListNavigatorImpl
import dev.gaborbiro.notes.features.notes.NotesListScreen
import dev.gaborbiro.notes.store.bitmap.BitmapStore
import dev.gaborbiro.notes.ui.theme.NotesTheme

class NavigationActivity : ComponentActivity() {

    private val repository by lazy { RecordsRepository.get() }
    private val uiMapper: RecordsUIMapper by lazy {
        RecordsUIMapper(BitmapStore(context = this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesTheme {
                val navController = rememberNavController()
                NotesNavHost(this, navController, repository, uiMapper)
            }
        }
    }
}

@Composable
fun NotesNavHost(
    context: Context,
    navController: NavHostController,
    repository: RecordsRepository,
    uiMapper: RecordsUIMapper,
) {
    NavHost(
        navController = navController,
        startDestination = NoteList.route,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(route = NoteList.route) {
            NotesListScreen(
                context = context,
                repository = repository,
                uiMapper = uiMapper,
                navigator = NotesListNavigatorImpl(context),
            )
        }
//        intent(context, AddNoteViaCamera)
//        intent(context, AddNoteViaImage)
//        intent(context, AddNoteViaText)
    }
}

//private fun NavGraphBuilder.intent(
//    context: Context,
//    destination: NotesIntentDestination
//) {
//    addDestination(
//        ActivityNavigator(context)
//            .createDestination()
//            .setIntent(destination.intent(context))
//            .also { it.route = destination.route }
//    )
//}