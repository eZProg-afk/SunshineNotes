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
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.listeners.FoldersListener;
import spiral.bit.dev.sunshinenotes.models.Folder;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private List<Folder> folders;
    private final FoldersListener listener;
    private Timer timer;
    private final List<Folder> foldersSource;

    public FolderAdapter(List<Folder> folders, FoldersListener listener) {
        this.folders = folders;
        this.listener = listener;
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
        holder.layoutFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onFolderClicked(folders.get(position), position);
            }
        });
        holder.layoutFolder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongFolderClicked(folders.get(position), position);
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
        RoundedImageView imageFolder;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.folder_title);
            textDateTime = itemView.findViewById(R.id.folder_date_time);
            textSubTitle = itemView.findViewById(R.id.folder_sub_title);
            imageFolder = itemView.findViewById(R.id.image_folder);
            layoutFolder = itemView.findViewById(R.id.layout_folder);
        }

        void setFolder(Folder folder) {
            textTitle.setText(folder.getName());
            textDateTime.setText(folder.getDateTime());
            textSubTitle.setText(folder.getSubTitle());
            GradientDrawable gradientDrawable = (GradientDrawable) layoutFolder.getBackground();
            if (folder.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(folder.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }
            if (folder.getImagePath() != null && !folder.getImagePath().isEmpty()) {
                imageFolder.setImageBitmap(BitmapFactory.decodeFile(folder.getImagePath()));
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
                    ArrayList<Folder> tempList = new ArrayList<>();
                    for (Folder folder : foldersSource) {
                            if (folder.getName().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    folder.getSubTitle().toLowerCase().contains(searchKeyWord.toLowerCase())) {
                                tempList.add(folder);
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
