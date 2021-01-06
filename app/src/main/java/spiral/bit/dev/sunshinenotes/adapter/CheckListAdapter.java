package spiral.bit.dev.sunshinenotes.adapter;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.listeners.CheckListsListener;
import spiral.bit.dev.sunshinenotes.models.CheckList;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.CheckListViewHolder> {

    private List<CheckList> checkLists;
    private final CheckListsListener listener;
    private Timer timer;
    private final List<CheckList> checkListsSource;
    private boolean isDeleteModeEnabled = false;

    public CheckListAdapter(List<CheckList> checkLists, CheckListsListener listener) {
        this.checkLists = checkLists;
        this.listener = listener;
        checkListsSource = checkLists;
    }

    @NonNull
    @Override
    public CheckListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.check_list_item, viewGroup, false);
        return new CheckListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CheckListViewHolder holder, final int position) {
        holder.setNote(checkLists.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onCheckListClicked(checkLists.get(position), position);
                if (isDeleteModeEnabled) {
                    holder.selectForDelete.setVisibility(View.VISIBLE);
                    holder.selectedForDeleteText.setVisibility(View.VISIBLE);
                } else {
                    holder.selectForDelete.setVisibility(View.GONE);
                    holder.selectedForDeleteText.setVisibility(View.GONE);
                }
            }
        });
        holder.layoutNote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongCheckListClicked(checkLists.get(position), position);
                holder.selectForDelete.setVisibility(View.VISIBLE);
                holder.selectedForDeleteText.setVisibility(View.VISIBLE);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return checkLists.size();
    }

    public void setOneClickDeleteMode() {
        isDeleteModeEnabled = true;
    }

    public void disableOneClickDeleteMode() {
        isDeleteModeEnabled = false;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class CheckListViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle, textDateTime, selectedForDeleteText;
        ConstraintLayout layoutNote, backDelete;
        RoundedImageView imageNote, selectForDelete;

        public CheckListViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.check_list_title);
            textDateTime = itemView.findViewById(R.id.check_list_date_time);
            layoutNote = itemView.findViewById(R.id.layout_check_list);
            imageNote = itemView.findViewById(R.id.check_list_image);
            selectForDelete = itemView.findViewById(R.id.image_select_delete);
            selectedForDeleteText = itemView.findViewById(R.id.text_selected_delete);
            backDelete = itemView.findViewById(R.id.back_delete);
        }

        void setNote(CheckList checkList) {
            textTitle.setText(checkList.getTitle());
            textDateTime.setText(checkList.getDateTime());
            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (checkList.getCheckListColor() != null) {
                gradientDrawable.setColor(Color.parseColor(checkList.getCheckListColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if (checkList.getImagePath() != null && !checkList.getImagePath().isEmpty()) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(checkList.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            } else {
                imageNote.setVisibility(View.GONE);
            }
        }
    }

    public void searchCheckList(final String searchKeyWord) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyWord.trim().isEmpty()) {
                    checkLists = checkListsSource;
                } else {
                    ArrayList<CheckList> tempList = new ArrayList<>();
                    for (CheckList checkList : checkListsSource) {
                        if (checkList.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                checkList.getDateTime().toLowerCase().contains(searchKeyWord.toLowerCase())) {
                            tempList.add(checkList);
                        }
                    }
                    checkLists = tempList;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
