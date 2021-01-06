package spiral.bit.dev.sunshinenotes.listeners.trash;

import spiral.bit.dev.sunshinenotes.models.trash.TrashCheckList;

public interface TrashCheckListListener {
    void onTrashCheckListClicked(TrashCheckList trashCheckList, int position);
    void onTrashCheckListLongClicked(TrashCheckList trashCheckList, int position);
}
