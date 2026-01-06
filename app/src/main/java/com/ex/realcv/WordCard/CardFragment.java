package com.ex.realcv.WordCard;


import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ex.realcv.DB.AppDatabase;
import com.ex.realcv.DB.WordCard.ProgressHost;
import com.ex.realcv.DB.WordCard.WordCardRepository;
import com.ex.realcv.DB.WordCard.WordEntity;
import com.ex.realcv.R;

import java.util.List;

public class CardFragment extends Fragment {

    private WordCardRepository repo;
    private List<WordEntity> sessionCards;
    private int index = 0;
    private String mode;

    // 📌 View references
    private TextView tvJapanese;
    private TextView tvExplain;
    private View cardContainer;
    private GestureDetector gestureDetector;
    private static final int SWIPE_THRESHOLD = 120;      // px
    private static final int SWIPE_VELOCITY_THRESHOLD = 120; // px/sec
    private Button btnTts;

    private boolean isExplainVisible = false;
    private boolean didSwipe = false;
    private float downX = 0f;
    private float downY = 0f;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mode = getArguments() != null
                ? getArguments().getString("extra_mode", "DAILY")
                : "DAILY";

        AppDatabase db = AppDatabase.getInstance(requireContext());
        repo = new WordCardRepository(db.wordDao());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wordcard_card_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1️⃣ View 바인딩
        tvJapanese = view.findViewById(R.id.tvJapanese);
        tvExplain  = view.findViewById(R.id.tvExplain);
        btnTts     = view.findViewById(R.id.btnTts);
        cardContainer = view.findViewById(R.id.cardContainer);

        // ✅ 카드 탭 시 설명 토글
        cardContainer.setOnClickListener(v -> toggleExplain());

        setupSwipe(cardContainer);
        //테스트 데이터 삽입
        repo.seedIfEmpty();
         // 3️⃣ DB에서 카드 세션 로드
        loadSession();
    }


    private void loadSession() {
        repo.loadSession(mode, 50, new WordCardRepository.Callback<List<WordEntity>>() {
            @Override
            public void onResult(List<WordEntity> cards) {
                requireActivity().runOnUiThread(() -> {
                    sessionCards = cards;

                    index = 0;
                    bindCard(sessionCards.get(index));
                    updateProgress();
                });
            }

            @Override
            public void onError(Throwable e) {
                // error 처리
            }
        });
    }

    // 🔹 1) 카드 UI에 데이터 바인딩
    private void bindCard(WordEntity card) {
        if (card == null) return;

        tvJapanese.setText(card.jpText);

        // 설명 텍스트 세팅
        String ex = (card.explainText == null) ? "" : card.explainText.trim();
        tvExplain.setText(ex);

        // ✅ 새 카드 바인딩될 때는 항상 "숨김"으로 시작
        isExplainVisible = false;
        tvExplain.setVisibility(View.INVISIBLE); // 공간 유지하면서 숨김 (예쁘게 유지)
       // tvExplain.setVisibility(View.GONE);   // 공간까지 줄이려면 이걸로
    }

    // 🔹 2) 진행도 UI 갱신
    private void updateProgress() {
        int total = (sessionCards == null) ? 0 : sessionCards.size();
        int current = (total == 0) ? 0 : (index + 1);

        if (!isAdded()) return;

        if (getActivity() instanceof ProgressHost) {
            ((ProgressHost) getActivity()).onProgressChanged(current, total);
        }
    }

    private void nextCard() {
        if (sessionCards == null || sessionCards.isEmpty()) return;

        if (index < sessionCards.size() - 1) {
            index++;

            // ✅ 다음 카드로 갈 때 설명 자동 숨김
            isExplainVisible = false;
            tvExplain.setVisibility(View.INVISIBLE);
            bindCard(sessionCards.get(index));
            updateProgress();
        } else {
            // 마지막 카드
            // (선택) 완료 UI 표시 / 다시 시작 / 종료
            // Toast.makeText(getContext(), "세션 완료!", Toast.LENGTH_SHORT).show();
        }
    }


    private void prevCard() {
        if (sessionCards == null || sessionCards.isEmpty()) return;

        if (index > 0) {
            index--;

            isExplainVisible = false;
            tvExplain.setVisibility(View.INVISIBLE);
            bindCard(sessionCards.get(index));
            updateProgress();
        }
    }

    private void toggleExplain() {
        // 설명이 비어있으면 아무것도 안 함
        CharSequence ex = tvExplain.getText();
        if (ex == null || ex.toString().trim().isEmpty()) return;

        isExplainVisible = !isExplainVisible;
        tvExplain.setVisibility(isExplainVisible ? View.VISIBLE : View.INVISIBLE);
    }

    private void setupSwipe(View target) {

        // dp → px로 임계치 잡는 게 안정적
        final int SWIPE_DISTANCE = (int) (80 * getResources().getDisplayMetrics().density); // 80dp
        final int SWIPE_VELOCITY = (int) (80 * getResources().getDisplayMetrics().density); // 80dp/s 느낌

        gestureDetector = new GestureDetector(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        didSwipe = false;
                        if (e != null) {
                            downX = e.getX();
                            downY = e.getY();
                        }
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                        if (e1 == null || e2 == null) return false;

                        float diffX = e2.getX() - e1.getX();
                        float diffY = e2.getY() - e1.getY();

                        // 수평 스와이프만 인정 (세로가 더 크면 무시)
                        if (Math.abs(diffX) <= Math.abs(diffY)) return false;

                        if (Math.abs(diffX) < SWIPE_DISTANCE) return false;
                        if (Math.abs(vx) < SWIPE_VELOCITY) return false;

                        didSwipe = true;

                        if (diffX > 0) onSwipeUnderstood(); // 👉 오른쪽
                        else onSwipeHard();                 // 👈 왼쪽

                        return true;
                    }
                });

        // ✅ 핵심: 클릭을 살리기 위해 onTouch에서 "무조건 true"를 리턴하면 안 됨
        target.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);

            // 스와이프가 발생했으면 터치 소비(클릭 방지)
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                if (didSwipe) return true;

                // 스와이프가 아니면 클릭이 정상 동작하도록 false 리턴
                return false;
            }

            // DOWN/MOVE는 클릭을 위해 false
            return false;
        });

        // 안전장치: 컨테이너가 클릭 가능해야 OnClick이 뜸
        target.setClickable(true);
        target.setFocusable(true);
    }

    private void onSwipeUnderstood() {
        // (선택) haptic
        if (cardContainer != null)
            cardContainer.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
        Log.d("SWIPE", "LEFT");
        // TODO: DB에 "understood" 기록(가중치/통계) 저장하고 싶으면 여기서
        prevCard();
    }

    private void onSwipeHard() {
        if (cardContainer != null)
            cardContainer.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);

        // TODO: DB에 "hard" 기록(가중치/통계) 저장하고 싶으면 여기서
        Log.d("SWIPE", "RIGHT");
        nextCard();
    }
}
