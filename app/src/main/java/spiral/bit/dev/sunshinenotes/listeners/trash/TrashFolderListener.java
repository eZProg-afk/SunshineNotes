package spiral.bit.dev.sunshinenotes.listeners.trash;

import spiral.bit.dev.sunshinenotes.models.trash.TrashFolder;

public interface TrashFolderListener {
    void onTrashFolderClicked(TrashFolder trashFolder, int position);
    void onTrashFolderLongClicked(TrashFolder trashFolder, int position);
}
