package com.ex.realcv.MemoMain;

import android.content.Context;
import android.util.Log;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ex.realcv.R;

public class MemoDialog extends DialogFragment {

    public interface Listener { void onSave(String text); }

    private Listener listener;
    private EditText et;

    public static MemoDialog newInstance(@Nullable String initialText) {
        MemoDialog d = new MemoDialog();
        Bundle b = new Bundle();
        b.putString("initial", initialText);
        d.setArguments(b);



        return d;
    }
    public void setListener(Listener l) { this.listener = l; }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        Dialog dlg = getDialog();
        if (dlg != null) {
            dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dlg.setCanceledOnTouchOutside(true);
            dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        View v = inf.inflate(R.layout.memo_dialog, c, false);
        et = v.findViewById(R.id.etMemo);

        String initial = (getArguments()!=null)? getArguments().getString("initial") : null;
        if (initial != null) {
            et.setText(initial);
            et.setSelection(initial.length());
        }

        et.setFocusable(true);
        et.setFocusableInTouchMode(true);
        et.requestFocus();

        dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);


        return v;
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        // 뷰가 붙고 난 뒤 프레임에서 포커스 + 키보드
        View scrim = v.findViewById(R.id.scrim);
        setCancelable(true);
        scrim.setOnClickListener(view -> dismiss());

        et.post(() -> {
            et.requestFocus();
            showKeyboard();
        });
    }

    @Override public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null && d.getWindow() != null) {

           // d.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
            d.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            /*DisplayMetrics dm = new DisplayMetrics();
            int w = (int) (dm.widthPixels * 0.86f);       // 가로 86%
            int h = ViewGroup.LayoutParams.WRAP_CONTENT;  // 세로는 내용만큼
            d.getWindow().setLayout(w, h);
            d.getWindow().setGravity(Gravity.CENTER);*/
            DisplayMetrics dm = new DisplayMetrics();
            d.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
            int cardWidth = (int)(dm.widthPixels * 0.86f);

            View root = requireView();
            View card = root.findViewById(R.id.memoCard); // XML에서 카드 id 지정 필요
            if (card != null) {
                ViewGroup.LayoutParams lp = card.getLayoutParams();
                lp.width = cardWidth;
                card.setLayoutParams(lp);
            }

            //안드로이드 ui 제거 현재 sdk 11이상만 되게
            WindowInsetsController controller = d.getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
            
        }
    }

    // 바깥 터치/뒤로가기 등으로 닫힐 때 여기로 옴 → 자동 저장 처리
    @Override public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {

            String text = (et != null) ? et.getText().toString().trim() : "";
            listener.onSave(text); // 비어있으면 무시하도록 호출 측에서 판단

        }
    }

    public void showKeyboard(){
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            // 두 번째 인자는 0 또는 SHOW_IMPLICIT 사용
            imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        }


    }

    public void closeKeyboard(){
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && et != null) {
            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        }
    }
}
