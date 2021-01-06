package spiral.bit.dev.sunshinenotes.listeners;

import spiral.bit.dev.sunshinenotes.models.other.BackgroundItem;

public interface BackListener {
    void onNoteClicked(BackgroundItem backgroundItem, int position);
    void onLongNoteClicked(BackgroundItem backgroundItem, int position);
}
