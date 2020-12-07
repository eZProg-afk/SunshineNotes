package spiral.bit.dev.sunshinenotes.listeners;

import spiral.bit.dev.sunshinenotes.models.Folder;

public interface FoldersListener {
    void onFolderClicked(Folder folder, int position);
    void onLongFolderClicked(Folder folder, int position);
}
