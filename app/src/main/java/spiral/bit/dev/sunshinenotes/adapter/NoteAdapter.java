package spiral.bit.dev.sunshinenotes.adapter;

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
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.listeners.NotesListener;
import spiral.bit.dev.sunshinenotes.models.NoteInFolder;
import spiral.bit.dev.sunshinenotes.models.SimpleNote;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<SimpleNote> noteInFolders;
    private final NotesListener listener;
    private Timer timer;
    private final List<SimpleNote> notesSource;

    public NoteAdapter(List<SimpleNote> noteInFolders, NotesListener listener) {
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
    public void onBindViewHolder(@NonNull NoteViewHolder holder, final int position) {
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

        TextView textTitle, textSubTitle, textDateTime;
        LinearLayout layoutNote;
        RoundedImageView imageNote;
        RoundedImageView imageDraw;
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
        }

        void setNote(SimpleNote simpleNote) {
            textTitle.setText(simpleNote.getTitle());
            String type = simpleNote.getFontStyle();
            String typeTextSize = simpleNote.getTextSize();
            String typeTextColor = simpleNote.getNoteColor();
            if (simpleNote.getSubTitle().trim().isEmpty()) {
                textSubTitle.setVisibility(View.GONE);
            } else {
                textSubTitle.setText(simpleNote.getSubTitle());
            }
            textDateTime.setText(simpleNote.getDateTime());
            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (simpleNote.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(simpleNote.getColor()));
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

            if (simpleNote.getImgTag() != null && !simpleNote.getImgTag().isEmpty()) {
                imageNote.setTag(simpleNote.getImgTag());
            }

            if (simpleNote.getImagePath() != null && !simpleNote.getImagePath().isEmpty()) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(simpleNote.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
                imageDraw.setVisibility(View.GONE);
            } else {
                imageNote.setVisibility(View.GONE);
                if (simpleNote.getDrawPath() != null && !simpleNote.getDrawPath().isEmpty()) {
                    InputStream is = null;
                    try {
                        is = context.getContentResolver().openInputStream(Uri.parse(simpleNote.getDrawPath()));
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
                    ArrayList<SimpleNote> tempList = new ArrayList<>();
                    for (SimpleNote simpleNote : notesSource) {
                        if (simpleNote.getImgTag() != null && !simpleNote.getImgTag().isEmpty()) {
                            if (simpleNote.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    simpleNote.getSubTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    simpleNote.getNoteText().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    simpleNote.getImgTag().toLowerCase().contains(searchKeyWord.toLowerCase())) {
                                tempList.add(simpleNote);
                            }
                        } else {
                            if (simpleNote.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    simpleNote.getSubTitle().toLowerCase().contains(searchKeyWord.toLowerCase()) ||
                                    simpleNote.getNoteText().toLowerCase().contains(searchKeyWord.toLowerCase())) {
                                tempList.add(simpleNote);
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
