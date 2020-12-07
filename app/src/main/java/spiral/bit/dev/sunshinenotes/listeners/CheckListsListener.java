package spiral.bit.dev.sunshinenotes.listeners;

import spiral.bit.dev.sunshinenotes.models.CheckList;

public interface CheckListsListener {
    void onCheckListClicked(CheckList checkList, int position);
    void onLongCheckListClicked(CheckList checkList, int position);
}
