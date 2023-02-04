package dev.gaborbiro.notes.features.nav

import android.content.Context
import android.content.Intent
import dev.gaborbiro.notes.features.host.HostActivity

interface NotesDestination {
    val route: String
}

interface NotesIntentDestination : NotesDestination {
    val intent: (Context) -> Intent
}

object NoteList : NotesDestination {
    override val route = "notes"
}

object AddNoteViaCamera : NotesIntentDestination {
    override val route = "camera"
    override val intent = HostActivity.Companion::getCameraIntent
}

object AddNoteViaImage : NotesIntentDestination {
    override val route = "pick_image"
    override val intent = HostActivity.Companion::getImagePickerIntent
}

object AddNoteViaText : NotesIntentDestination {
    override val route = "text"
    override val intent = HostActivity.Companion::getTextOnlyIntent
}