package spiral.bit.dev.sunshinenotes.listeners;

import spiral.bit.dev.sunshinenotes.models.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
    void onLongNoteClicked(Note note, int position);
}
