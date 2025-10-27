package com.ex.realcv.MemoMain;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;



import com.ex.realcv.R;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.VH> {

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cb; TextView tv;
        VH(View v){ super(v); cb = v.findViewById(R.id.cbDone); tv = v.findViewById(R.id.tvText); }
    }
    public interface OnCallBackFunc { void onCallBackFunc(List<Memo> current); }
    public interface OnItemClick { void onItemClick(int position, Memo memo);}
    private final List<Memo> items = new ArrayList<>();
    private final OnCallBackFunc CallBack;
    private OnItemClick onItemClick;

    public MemoAdapter(OnCallBackFunc CallBack) { this.CallBack = CallBack; }
    public void setOnItemClick(OnItemClick l) { this.onItemClick = l; }
    public List<Memo> getItems(){ return items; }

    public void setItems(List<Memo> list) {
        items.clear(); items.addAll(list);
        notifyDataSetChanged();
    }

    public Memo getItem(int position) {
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }
    public void add(Memo m) {
        items.add(0, m);
        notifyItemInserted(0);
        CallBack.onCallBackFunc(items);
    }
    public void remove(int pos) {
        items.remove(pos);
        notifyItemRemoved(pos);
        CallBack.onCallBackFunc(items);
    }

    // ★ 텍스트 수정 반영
    public void update(int position, String newText){
        if (position < 0 || position >= items.size()) return;
        items.get(position).text = newText;
        notifyItemChanged(position);
        if (CallBack!=null) CallBack.onCallBackFunc(items);
    }


    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.memo_item, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Memo m = items.get(pos);
        h.tv.setText(m.getFormattedTime() + '\n' +m.text);
        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(m.done);
        applyDoneStyle(h.tv, m.done);

        h.itemView.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p != RecyclerView.NO_POSITION && onItemClick != null) {
                onItemClick.onItemClick(p, items.get(p));
            }

        });
        h.cb.setOnCheckedChangeListener((b, checked) -> {
            m.done = checked;
            applyDoneStyle(h.tv, checked);
            if (CallBack != null) CallBack.onCallBackFunc(items);
        });
    }

    @Override public int getItemCount() { return items.size(); }



    private void applyDoneStyle(TextView tv, boolean done){
        if(done){
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tv.setAlpha(0.6f);
        }else{
            tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            tv.setAlpha(1f);
        }
    }
}