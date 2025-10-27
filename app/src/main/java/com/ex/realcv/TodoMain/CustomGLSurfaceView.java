package com.ex.realcv.TodoMain;// imports
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.ex.realcv.MemoMain.Memo;

import java.util.ArrayList;
import java.util.List;

public class CustomGLSurfaceView extends GLSurfaceView {

    private BaseRenderer renderer;

    private List<Memo> cachedMemos = new ArrayList<>();

    private float previousX, previousY;
    private final float TOUCH_SCALE_FACTOR = 0.5f; // 회전 민감도 조절

    // XML에서 사용할 수도 있으니 함께 제공 (선택이지만 권장)
    public CustomGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        renderer = new BaseRenderer(context);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    /** UI/IO 스레드에서 호출 OK — 내부에서 GLThread로 전달 */
    public void setMemos(List<Memo> memos) {
        cachedMemos.clear();
        cachedMemos.addAll(memos);

        queueEvent(() -> {
            //Log.d("test custom ","memos size : " + cachedMemos.size());
            renderer.setMemos(cachedMemos);
            renderer.rebuildPlanetsFromMemos();
        });
        requestRender();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - previousX;
                float dy = y - previousY;

                // OpenGL 스레드에 전달
                queueEvent(() -> {
                    renderer.handleCameraRotation(
                            dx * TOUCH_SCALE_FACTOR,
                            dy * TOUCH_SCALE_FACTOR
                    );
                });
                break;
        }

        previousX = x;
        previousY = y;
        return true;
    }

    public void MoveToNewArea() {
        renderer.moveToNewArea();
    }
}