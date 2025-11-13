package com.ex.realcv.MemoMain;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import com.ex.realcv.R;
import com.ex.realcv.MemoMain.BlockMemo;
import com.ex.realcv.MemoMain.BlocksAdapter;

public class MemoBlockDialog extends DialogFragment implements BlocksAdapter.Callbacks {

    private static final String ARG_BLOCKS = "blocks";
    public static MemoBlockDialog newInstance(ArrayList<BlockMemo> blocks, String resultKey){
        Bundle b = new Bundle();
        b.putSerializable("blocks", blocks);
        b.putString("resultKey", resultKey);
        MemoBlockDialog f = new MemoBlockDialog();
        f.setArguments(b);
        return f;
    }

    private ArrayList<BlockMemo> data;
    private RecyclerView rv;
    private BlocksAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.memo_dialog, container, false);

        // 스크림 터치 시 닫기 (카드 터치는 통과)
        v.findViewById(R.id.scrim).setOnClickListener(x -> dismiss());

        // 데이터
        Object ser = getArguments()!=null ? getArguments().getSerializable(ARG_BLOCKS) : null;
        if (ser instanceof ArrayList) data = (ArrayList<BlockMemo>) ser;
        if (data == null || data.isEmpty()) {
            data = new ArrayList<>();
            data.add(BlockMemo.para("메모를 시작해보세요"));
        }

        // RecyclerView
        rv = v.findViewById(R.id.rvBlocks);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BlocksAdapter(data, this);
        rv.setAdapter(adapter);

        // 새 TODO 블록 추가
        v.findViewById(R.id.btnTodo).setOnClickListener(b -> {
            int pos = data.size();
            data.add(BlockMemo.todo("", false));
            adapter.notifyItemInserted(pos);
            requestFocusAt(pos, 0);
        });

        return v;
    }

    @Override public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null && d.getWindow() != null) {
            Window w = d.getWindow();
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            w.setGravity(Gravity.CENTER);
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            // ✅ 전체화면 immersive 모드 (상단/하단 바 숨기기)
            View decor = w.getDecorView();
            WindowInsetsController controller = decor.getWindowInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        }

        setCancelable(true);
    }

    // 새 블록 포커스 이동 (adapter 콜백)
    @Override public void requestFocusAt(int position, int sel) {
        rv.post(() -> {
            rv.scrollToPosition(position);
            RecyclerView.ViewHolder vh = rv.findViewHolderForAdapterPosition(position);
            if (vh == null) { rv.post(() -> requestFocusAt(position, sel)); return; }
            View v = vh.itemView.findViewById(R.id.et);
            if (v instanceof EditText) {
                EditText et = (EditText) v;
                et.requestFocus();
                et.setSelection(Math.min(sel, et.getText().length()));
            }
        });
    }

    // 닫을 때 블록 결과를 액티비티로 전달(원하면 저장)
    @Override public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        String resultKey = getArguments()!=null ? getArguments().getString("resultKey","memo_result") : "memo_result";
        Bundle out = new Bundle();
        out.putSerializable("blocks", data); // data: RecyclerView가 편집한 ArrayList<Block>
        getParentFragmentManager().setFragmentResult(resultKey, out);
    }
}
