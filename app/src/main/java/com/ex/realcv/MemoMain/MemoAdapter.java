package com.ex.realcv.MemoMain;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


import com.ex.realcv.R;
import com.google.android.material.card.MaterialCardView;

//public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.VH> {
public class MemoAdapter extends ListAdapter<Memo, MemoAdapter.VH> {

    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView card;
        CheckBox cb; TextView tv;
        VH(View v){
            super(v);
            card = v.findViewById(R.id.memoCard);
            cb = v.findViewById(R.id.cbDone);
            tv = v.findViewById(R.id.tvText);
        }
    }
    private static final DiffUtil.ItemCallback<Memo> DIFF = new DiffUtil.ItemCallback<Memo>() {
        @Override public boolean areItemsTheSame(@NonNull Memo a, @NonNull Memo b) {
            return a.getId().equals(b.getId());
        }
        @Override public boolean areContentsTheSame(@NonNull Memo a, @NonNull Memo b) {
            // 변경 판단 기준(텍스트/체크/업데이트시간 등)
            return a.isDone() == b.isDone()
                    && Objects.equals(a.text, b.text)
                    && a.getUpdatedAt() == b.getUpdatedAt()
                    && Objects.equals(a.getDeletedAt(), b.getDeletedAt());
        }
    };

    public interface Listener {
        void onToggleDone(Memo memo, boolean checked);
        void onItemClick(Memo memo);

        void onStartDrag(RecyclerView.ViewHolder holder);
    }
    private final Listener  listener;

    public MemoAdapter(Listener  Lis ){
        super(DIFF);
        this.listener = Lis ;
        setHasStableIds(true);
    }

    private boolean trashMode =false;
    public void setTrashMode(boolean on){
        trashMode = on;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.memo_item, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        //Memo m = items.get(pos);
        Memo m = getItem(pos);

        //block
        String preview = buildPreviewText(m.getText(), 2);
        //

       // h.tv.setText(m.getUpdatedAtByString() + "\n" + m.getText());
        h.tv.setText(m.getUpdatedAtByString() + "\n" + preview);

        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(m.done);
        applyDoneStyle(h.tv, m.done);

        int color = ContextCompat.getColor(
                h.itemView.getContext(),
                trashMode ? R.color.memo_bg_trash : R.color.memo_bg_normal
        );
        Log.d("trashMOde " , "color : " + trashMode);
        h.card.setCardBackgroundColor(color);
        h.cb.setOnCheckedChangeListener((button, checked) -> {
            if (listener != null) listener.onToggleDone(m, checked);
        });

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(m);
        });

        h.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onStartDrag(h);
            return true;
        });

        /*h.itemView.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p != RecyclerView.NO_POSITION && onItemClick != null) {
                onItemClick.onItemClick(p, items.get(p));
            }

        });
        h.cb.setOnCheckedChangeListener((b, checked) -> {
            m.done = checked;
            applyDoneStyle(h.tv, checked);
            if (CallBack != null) CallBack.onCallBackFunc(items);
        });*/
    }

    @Override public long getItemId(int position) {
        // stableIds: RecyclerView 성능 및 깜빡임 감소
        return getItem(position).getId().hashCode();
    }

    //@Override public int getItemCount() { return items.size(); }


    public Memo itemAt(int position) {   // public wrapper
        return getItem(position);        // protected 메서드를 내부에서 호출
    }


    private void applyDoneStyle(TextView tv, boolean done){
        if(done){
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tv.setAlpha(0.6f);
        }else{
            tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            tv.setAlpha(1f);
        }
    }


    // ===== 블록 텍스트 프리뷰 생성 =====
    private static final String BLOCKS_PREFIX = "BLOCKS_JSON:";

    private String buildPreviewText(String raw, int maxLines) {
        if (raw == null || raw.isEmpty()) return "";

        if (!raw.startsWith(BLOCKS_PREFIX)) {
            // 과거 평문 메모
            return limitLines(raw, maxLines);
        }

        // 블록 JSON → 평문으로 변환
        String json = raw.substring(BLOCKS_PREFIX.length());
        try {
            java.lang.reflect.Type t = new com.google.gson.reflect.TypeToken<java.util.ArrayList<BlockMemo>>(){}.getType();
            java.util.ArrayList<BlockMemo> blocks = new com.google.gson.Gson().fromJson(json, t);
            if (blocks == null || blocks.isEmpty()) return "";

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < blocks.size(); i++) {
                BlockMemo b = blocks.get(i);
                if (b == null) continue;
                String line;
                if (b.type == BlockMemo.Type.TODO) {
                    // 보기 좋게 토글 기호를 붙여줌
                    line = (b.checked ? "☑ " : "☐ ") + nullToEmpty(b.text);
                } else {
                    line = nullToEmpty(b.text);
                }
                sb.append(line);
                if (i < blocks.size() - 1) sb.append('\n');
            }
            return limitLines(sb.toString(), maxLines);
        } catch (Exception e) {
            // JSON 파싱 실패 시 원문 fallback
            return limitLines(raw, maxLines);
        }
    }

    private String limitLines(String s, int maxLines) {
        if (maxLines <= 0) return s;
        int count = 0, idx = 0, lastBreak = -1;
        while (idx < s.length()) {
            if (s.charAt(idx) == '\n') {
                count++;
                if (count >= maxLines) { lastBreak = idx; break; }
            }
            idx++;
        }
        if (lastBreak >= 0) {
            return s.substring(0, lastBreak);
        }
        return s;
    }
    private String nullToEmpty(String s){ return s == null ? "" : s; }
}