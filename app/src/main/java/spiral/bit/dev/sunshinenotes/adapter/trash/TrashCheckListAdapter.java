package spiral.bit.dev.sunshinenotes.adapter.trash;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;
import java.util.Timer;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.listeners.trash.TrashCheckListListener;
import spiral.bit.dev.sunshinenotes.models.trash.TrashCheckList;

public class TrashCheckListAdapter extends RecyclerView.Adapter<TrashCheckListAdapter.TrashCheckListViewHolder> {

    private List<TrashCheckList> noteInFolders;
    private final TrashCheckListListener listener;
    private Timer timer;
    private final List<TrashCheckList> notesSource;

    public TrashCheckListAdapter(List<TrashCheckList> noteInFolders, TrashCheckListListener listener) {
        this.noteInFolders = noteInFolders;
        this.listener = listener;
        notesSource = noteInFolders;
    }

    @NonNull
    @Override
    public TrashCheckListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.check_list_item, viewGroup, false);
        return new TrashCheckListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrashCheckListViewHolder holder, final int position) {
        holder.setNote(noteInFolders.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onTrashCheckListClicked(noteInFolders.get(position), position);
            }
        });
        holder.layoutNote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onTrashCheckListLongClicked(noteInFolders.get(position), position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteInFolders.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class TrashCheckListViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle, textDateTime;
        ConstraintLayout layoutNote;
        RoundedImageView imageNote;

        public TrashCheckListViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.check_list_title);
            textDateTime = itemView.findViewById(R.id.check_list_date_time);
            layoutNote = itemView.findViewById(R.id.layout_check_list);
            imageNote = itemView.findViewById(R.id.check_list_image);
        }

        void setNote(TrashCheckList trashCheckList) {
            textTitle.setText(trashCheckList.getTitle());
            textDateTime.setText(trashCheckList.getDateTime());
            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (trashCheckList.getCheckListColor() != null) {
                gradientDrawable.setColor(Color.parseColor(trashCheckList.getCheckListColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if (trashCheckList.getImagePath() != null && !trashCheckList.getImagePath().isEmpty()) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(trashCheckList.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            }
        }
    }

//    public void searchNote(final String searchKeyWord) {
//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if (searchKeyWord.trim().isEmpty()) {
//                    noteInFolders = notesSource;
//                } else {
//                    ArrayList<TrashCheckList> tempList = new ArrayList<>();
//                    for (TrashNote trashNote : notesSource) {
//                            if (trashNote.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
//                                    trashNote.getSubTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
//                                    trashNote.getNoteText().toLowerCase().contains(searchKeyWord.toLowerCase())) {
//                                tempList.add(trashNote);
//                            }
//                    }
//                    noteInFolders = tempList;
//                }
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        notifyDataSetChanged();
//                    }
//                });
//            }
//        }, 500);
//    }
//
//    public void cancelTimer() {
//        if (timer != null) {
//            timer.cancel();
//        }
//    }
}
