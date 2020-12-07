package spiral.bit.dev.sunshinenotes.listeners;

import spiral.bit.dev.sunshinenotes.models.SimpleNote;

public interface NotesListener {
    void onNoteClicked(SimpleNote simpleNote, int position);
    void onLongNoteClicked(SimpleNote simpleNote, int position);
}
