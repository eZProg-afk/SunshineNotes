package spiral.bit.dev.sunshinenotes.listeners;

import spiral.bit.dev.sunshinenotes.models.CheckItem;

public interface CheckListener {
    void onCheckClicked(CheckItem checkItem, int position);
}
