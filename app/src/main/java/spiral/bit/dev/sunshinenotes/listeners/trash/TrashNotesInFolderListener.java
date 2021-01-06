package spiral.bit.dev.sunshinenotes.listeners.trash;

import spiral.bit.dev.sunshinenotes.models.trash.TrashNoteInFolder;

public interface TrashNotesInFolderListener {
    void onNoteClicked(TrashNoteInFolder trashNoteInFolder, int position);
    void onLongNoteClicked(TrashNoteInFolder trashNoteInFolder, int position);
}
