package spiral.bit.dev.sunshinenotes.listeners.trash;

import spiral.bit.dev.sunshinenotes.models.trash.TrashNote;

public interface TrashListener {
    void onTrashNoteClicked(TrashNote trashNote, int position);
    void onTrashNoteLongClicked(TrashNote trashNote, int position);
}
