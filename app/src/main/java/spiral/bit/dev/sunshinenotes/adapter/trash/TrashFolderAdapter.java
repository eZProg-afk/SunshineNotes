package spiral.bit.dev.sunshinenotes.adapter.trash;

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
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.listeners.trash.TrashEditListener;
import spiral.bit.dev.sunshinenotes.listeners.trash.TrashFolderListener;
import spiral.bit.dev.sunshinenotes.models.trash.TrashFolder;

public class TrashFolderAdapter extends RecyclerView.Adapter<TrashFolderAdapter.FolderViewHolder> {

    private List<TrashFolder> folders;
    private final TrashFolderListener listener;
    private TrashEditListener editListener;
    private Timer timer;
    private final List<TrashFolder> foldersSource;

    public TrashFolderAdapter(List<TrashFolder> folders, TrashFolderListener listener, TrashEditListener editListener) {
        this.folders = folders;
        this.listener = listener;
        this.editListener = editListener;
        foldersSource = folders;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.folder_item, viewGroup, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, final int position) {
        holder.setFolder(folders.get(position));
        holder.imageLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onTrashFolderClicked(folders.get(position), position);
            }
        });
        holder.textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editListener.onEdit(folders.get(position), position);
            }
        });
        holder.layoutFolder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onTrashFolderLongClicked(folders.get(position), position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle, textSubTitle, textDateTime;
        LinearLayout layoutFolder;
        RoundedImageView imageFolder, imageLabel;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.folder_title);
            textDateTime = itemView.findViewById(R.id.folder_date_time);
            textSubTitle = itemView.findViewById(R.id.folder_sub_title);
            imageFolder = itemView.findViewById(R.id.image_folder);
            imageLabel = itemView.findViewById(R.id.folder_label);
            layoutFolder = itemView.findViewById(R.id.layout_folder);
        }

        void setFolder(TrashFolder trashFolder) {
            textTitle.setText(trashFolder.getName());
            textDateTime.setText(trashFolder.getDateTime());
            textSubTitle.setText(trashFolder.getSubTitle());
            GradientDrawable gradientDrawable = (GradientDrawable) layoutFolder.getBackground();
            if (trashFolder.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(trashFolder.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }
            if (trashFolder.getImagePath() != null && !trashFolder.getImagePath().isEmpty()) {
                imageFolder.setImageBitmap(BitmapFactory.decodeFile(trashFolder.getImagePath()));
                imageFolder.setVisibility(View.VISIBLE);
            }
        }
    }

    public void searchFolder(final String searchKeyWord) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyWord.trim().isEmpty()) {
                    folders = foldersSource;
                } else {
                    ArrayList<TrashFolder> tempList = new ArrayList<>();
                    for (TrashFolder trashFolder : foldersSource) {
                            if (trashFolder.getName().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    trashFolder.getSubTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    trashFolder.getDateTime().toLowerCase().contains(searchKeyWord.toLowerCase())) {
                                tempList.add(trashFolder);
                            }
                    }
                    folders = tempList;
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
