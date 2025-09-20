package com.ex.realcv.TodoMain;// imports
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomGLSurfaceView extends GLSurfaceView {

    private BaseRenderer renderer;

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            // OpenGL 렌더러에 전달
            queueEvent(() -> renderer.handleTouch(x, y, getWidth(), getHeight()));
        }
        return true;
    }
}