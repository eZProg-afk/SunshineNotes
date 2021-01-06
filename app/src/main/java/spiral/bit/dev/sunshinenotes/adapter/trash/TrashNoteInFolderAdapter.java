package spiral.bit.dev.sunshinenotes.adapter.trash;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.listeners.NotesInFolderListener;
import spiral.bit.dev.sunshinenotes.listeners.trash.TrashNotesInFolderListener;
import spiral.bit.dev.sunshinenotes.models.NoteInFolder;
import spiral.bit.dev.sunshinenotes.models.trash.TrashNoteInFolder;

public class TrashNoteInFolderAdapter extends RecyclerView.Adapter<TrashNoteInFolderAdapter.NoteViewHolder> {

    private List<TrashNoteInFolder> noteInFolders;
    private final TrashNotesInFolderListener listener;
    private Timer timer;
    private final List<TrashNoteInFolder> notesSource;
    private boolean isDeleteModeEnabled = false;

    public TrashNoteInFolderAdapter(List<TrashNoteInFolder> noteInFolders, TrashNotesInFolderListener listener) {
        this.noteInFolders = noteInFolders;
        this.listener = listener;
        notesSource = noteInFolders;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.note_item, viewGroup, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NoteViewHolder holder, final int position) {
        holder.setNote(noteInFolders.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onNoteClicked(noteInFolders.get(position), position);
            }
        });
        holder.layoutNote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongNoteClicked(noteInFolders.get(position), position);
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

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle, textSubTitle, textDateTime, selectedForDeleteText;
        LinearLayout layoutNote;
        ConstraintLayout backDelete;
        RoundedImageView imageNote;
        RoundedImageView imageDraw, selectForDelete;
        private final Context context;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            textTitle = itemView.findViewById(R.id.text_title);
            textSubTitle = itemView.findViewById(R.id.text_sub_title);
            textDateTime = itemView.findViewById(R.id.text_date_time);
            layoutNote = itemView.findViewById(R.id.layout_note);
            imageNote = itemView.findViewById(R.id.image_note);
            imageDraw = itemView.findViewById(R.id.image_draw_note);
            selectForDelete = itemView.findViewById(R.id.image_select_delete);
            selectedForDeleteText = itemView.findViewById(R.id.text_selected_delete);
            backDelete = itemView.findViewById(R.id.back_delete);
        }

        void setNote(TrashNoteInFolder trashNoteInFolder) {
            textTitle.setText(trashNoteInFolder.getTitle());
            String type = trashNoteInFolder.getFontStyle();
            String typeTextSize = trashNoteInFolder.getTextSize();
            String typeTextColor = trashNoteInFolder.getNoteColor();
            if (trashNoteInFolder.getSubTitle().trim().isEmpty()) {
                textSubTitle.setVisibility(View.GONE);
            } else {
                textSubTitle.setText(trashNoteInFolder.getSubTitle());
            }
            textDateTime.setText(trashNoteInFolder.getDateTime());
            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (trashNoteInFolder.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(trashNoteInFolder.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if (type != null) {
                switch (type) {
                    case "def": {
                        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/ubuntu_bold.ttf");
                        Typeface typeface2 = Typeface.createFromAsset(context.getAssets(), "fonts/ubuntu_medium.ttf");
                        textTitle.setTypeface(typeface);
                        textSubTitle.setTypeface(typeface2);
                        break;
                    }
                    case "comissioner": {
                        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/comm_black.ttf");
                        Typeface typeface2 = Typeface.createFromAsset(context.getAssets(), "fonts/comm_medium.ttf");
                        textTitle.setTypeface(typeface);
                        textSubTitle.setTypeface(typeface2);
                        break;
                    }
                    case "roboto": {
                        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/robotoslab_black.ttf");
                        Typeface typeface2 = Typeface.createFromAsset(context.getAssets(), "fonts/robotoslab_regular.ttf");
                        textTitle.setTypeface(typeface);
                        textSubTitle.setTypeface(typeface2);
                        break;
                    }
                    case "sourcecode": {
                        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/sourcecode_black.ttf");
                        Typeface typeface2 = Typeface.createFromAsset(context.getAssets(), "fonts/source_regular.ttf");
                        textTitle.setTypeface(typeface);
                        textSubTitle.setTypeface(typeface2);
                        break;
                    }
                }
            }

            if (typeTextSize != null) {
                switch (typeTextSize) {
                    case "def":
                        textTitle.setTextSize(16);
                        textSubTitle.setTextSize(13);
                        break;
                    case "small":
                        textTitle.setTextSize(12);
                        textSubTitle.setTextSize(10);
                        break;
                    case "medium":
                        textTitle.setTextSize(18);
                        textSubTitle.setTextSize(16);
                        break;
                    case "big":
                        textTitle.setTextSize(22);
                        textSubTitle.setTextSize(17);
                        break;
                }
            }

            if (typeTextColor != null) {
                switch (typeTextColor) {
                    case "def":
                        textTitle.setTextColor(Color.WHITE);
                        textSubTitle.setTextColor(Color.WHITE);
                        break;
                    case "yellow":
                        textTitle.setTextColor(Color.YELLOW);
                        textSubTitle.setTextColor(Color.YELLOW);
                        break;
                    case "green":
                        textTitle.setTextColor(Color.GREEN);
                        textSubTitle.setTextColor(Color.GREEN);
                        break;
                    case "red":
                        textTitle.setTextColor(Color.RED);
                        textSubTitle.setTextColor(Color.RED);
                        break;
                }
            }

            if (trashNoteInFolder.getImgTag() != null && !trashNoteInFolder.getImgTag().isEmpty()) {
                imageNote.setTag(trashNoteInFolder.getImgTag());
            }

            if (trashNoteInFolder.getImagePath() != null && !trashNoteInFolder.getImagePath().isEmpty()) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(trashNoteInFolder.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
                imageDraw.setVisibility(View.GONE);
            } else {
                imageNote.setVisibility(View.GONE);
                if (trashNoteInFolder.getDrawPath() != null && !trashNoteInFolder.getDrawPath().isEmpty()) {
                    InputStream is = null;
                    try {
                        is = context.getContentResolver().openInputStream(Uri.parse(trashNoteInFolder.getDrawPath()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    imageDraw.setImageBitmap(BitmapFactory.decodeStream(is));
                    imageDraw.setVisibility(View.VISIBLE);
                } else {
                    imageDraw.setVisibility(View.GONE);
                }
            }
        }
    }

    public void searchNote(final String searchKeyWord) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyWord.trim().isEmpty()) {
                    noteInFolders = notesSource;
                } else {
                    ArrayList<TrashNoteInFolder> tempList = new ArrayList<>();
                    for (TrashNoteInFolder trashNoteInFolder : notesSource) {
                        if (trashNoteInFolder.getImgTag() != null && !trashNoteInFolder.getImgTag().isEmpty()) {
                            if (trashNoteInFolder.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    trashNoteInFolder.getSubTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    trashNoteInFolder.getNoteText().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    trashNoteInFolder.getImgTag().toLowerCase().contains(searchKeyWord.toLowerCase())|
                                            trashNoteInFolder.getDateTime().toLowerCase().contains(searchKeyWord.toLowerCase())) {
                                tempList.add(trashNoteInFolder);
                            }
                        } else {
                            if (trashNoteInFolder.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    trashNoteInFolder.getSubTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    trashNoteInFolder.getNoteText().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    trashNoteInFolder.getDateTime().toLowerCase().contains(searchKeyWord.toLowerCase())) {
                                tempList.add(trashNoteInFolder);
                            }
                        }
                    }
                    noteInFolders = tempList;
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
