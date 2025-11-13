package com.ex.realcv.MemoMain;

import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ex.realcv.MemoMain.BlockMemo;
import com.ex.realcv.MemoMain.BlockMemo.Type;
import com.ex.realcv.R;

import java.util.List;
import java.util.UUID;

public class BlocksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Callbacks {
        void requestFocusAt(int position, int sel); // 새 블록 생긴 후 포커스 이동
    }

    private final List<BlockMemo> items;
    private final Callbacks cb;

    public BlocksAdapter(List<BlockMemo> items, Callbacks cb) {
        this.items = items;
        this.cb = cb;
        setHasStableIds(true);
    }

    @Override public long getItemId(int position) { return items.get(position).id.hashCode(); }
    @Override public int getItemViewType(int position) { return items.get(position).type == Type.TODO ? 1 : 2; }
    @Override public int getItemCount() { return items.size(); }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int vt) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (vt == 1) {
            View v = inf.inflate(R.layout.memo_item_todo, parent, false);
            return new TodoVH(v);
        } else {
            View v = inf.inflate(R.layout.memo_item_para, parent, false);
            return new ParaVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
        BlockMemo b = items.get(pos);
        if (h instanceof TodoVH) ((TodoVH) h).bind(b, pos);
        else ((ParaVH) h).bind(b, pos);
    }

    // ---- ViewHolders ----

    class TodoVH extends RecyclerView.ViewHolder {
        CheckBox cbx; EditText et;
        TodoVH(@NonNull View itemView) {
            super(itemView);
            cbx = itemView.findViewById(R.id.cb);
            et = itemView.findViewById(R.id.et);
        }
        void bind(BlockMemo b, int pos){
            cbx.setOnCheckedChangeListener(null);
            cbx.setChecked(b.checked);
            et.setText(b.text);
            et.setPaintFlags(b.checked
                    ? et.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                    : et.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

            cbx.setOnCheckedChangeListener((buttonView, isChecked) -> {
                b.checked = isChecked;
                et.setPaintFlags(isChecked
                        ? et.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                        : et.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            });

            // 엔터로 블록 분할
            et.setOnKeyListener((v, keyCode, ev) -> {
                if (ev.getAction()==KeyEvent.ACTION_DOWN && keyCode==KeyEvent.KEYCODE_ENTER) {
                    splitBlock(pos, et);
                    return true;
                }
                //체크박스 바로 앞에서 backspace시 일반 edit을 ㅗ전환
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    int sel = et.getSelectionStart();
                    if (sel == 0) { // 커서가 맨 앞
                        int adapterPos = getBindingAdapterPosition();
                        if (adapterPos == RecyclerView.NO_POSITION) return false;

                        BlockMemo cur = items.get(adapterPos);
                        // 현 텍스트는 그대로 두고, 타입만 PARA로 변경
                        cur.type = BlockMemo.Type.PARA;
                        cur.checked = false;

                        // 뷰타입이 바뀌므로 갱신
                        notifyItemChanged(adapterPos);

                        // 바뀐 뷰로 포커스/커서 이동
                        if (cb != null) cb.requestFocusAt(adapterPos, 0);
                        return true; // 이벤트 소비 (체크박스가 삭제된 효과)
                    }
                }
                return false;
            });

            // 텍스트 변경 모델 반영
            et.addTextChangedListener(simpleWatcher(t -> b.text = t));
        }
    }

    class ParaVH extends RecyclerView.ViewHolder {
        EditText et;
        ParaVH(@NonNull View itemView) {
            super(itemView);
            et = itemView.findViewById(R.id.et);
        }
        void bind(BlockMemo b, int pos){
            et.setText(b.text);
            et.setOnKeyListener((v, keyCode, ev) -> {
                if (ev.getAction()==KeyEvent.ACTION_DOWN && keyCode==KeyEvent.KEYCODE_ENTER) {
                    splitBlock(pos, et);
                    return true;
                }
                // Backspace at line start → 이전 블록과 병합
                if (keyCode==KeyEvent.KEYCODE_DEL) {
                    int sel = et.getSelectionStart();
                    if (sel == 0) {
                        mergeWithPrevious(pos);   // ← 아래 helper
                        return true;              // 이벤트 소비 (위로 당겨짐)
                    }
                }
                return false;
            });
            et.addTextChangedListener(simpleWatcher(t -> b.text = t));
        }
    }

    // ---- helpers ----

    private interface OnText { void apply(String s); }
    private TextWatcher simpleWatcher(OnText f){
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { f.apply(s.toString()); }
        };
    }

    /** 커서 기준으로 블록을 두 개로 쪼개고, 새 블록에 포커스 */
    private void splitBlock(int pos, EditText et) {
        BlockMemo cur = items.get(pos);
        int sel = et.getSelectionStart();
        String all = et.getText().toString();
        String left = all.substring(0, Math.max(0, sel));
        String right = all.substring(Math.max(0, sel));

        cur.text = left;

        BlockMemo nb = new BlockMemo();
        nb.id = UUID.randomUUID().toString();
        nb.type = cur.type;            // 같은 타입으로 새 블록
        nb.text = right;
        if (cur.type == BlockMemo.Type.TODO) nb.checked = false;

        items.add(pos+1, nb);
        notifyItemRangeChanged(pos, 2);

        if (cb != null) cb.requestFocusAt(pos+1, 0);
    }

    private void mergeWithPrevious(int pos) {
        if (pos <= 0) return; // 맨 첫 블록이면 할 게 없음

        BlockMemo cur  = items.get(pos);
        BlockMemo prev = items.get(pos - 1);

        // 병합 지점(커서가 옮겨갈 위치): 이전 블록 텍스트 끝
        int newSel = (prev.text != null ? prev.text.length() : 0);

        // 병합 규칙 (간단 버전):
        // - 같은 타입이면 그냥 이어붙임
        // - 타입이 달라도 텍스트는 이어붙이고, 이전 블록 타입 유지
        String curText  = (cur.text  != null ? cur.text  : "");
        String prevText = (prev.text != null ? prev.text : "");

        prev.text = prevText + curText;

        // TODO와 PARA가 섞일 때 커스텀 규칙이 필요하면 여기서 분기해도 됨
        // ex) prev가 TODO면 취소선/체크 상태는 prev.checked 그대로 유지

        // 현재 블록 삭제
        items.remove(pos);
        notifyItemRemoved(pos);
        notifyItemChanged(pos - 1);

        // 포커스를 이전 블록의 끝으로 이동
        if (cb != null) cb.requestFocusAt(pos - 1, newSel);
    }
}
