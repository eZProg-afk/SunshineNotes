package spiral.bit.dev.sunshinenotes.listeners;

import spiral.bit.dev.sunshinenotes.models.NoteInFolder;

public interface NotesInFolderListener {
    void onNoteClicked(NoteInFolder noteInFolder, int position);
    void onLongNoteClicked(NoteInFolder noteInFolder, int position);
}
