package com.ex.realcv.TodoMain;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import android.os.Handler;
import android.os.Looper;

//EGL
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// 자바/유틸
import java.util.ArrayList;
import java.util.List;


public class BaseRenderer implements GLSurfaceView.Renderer{

    private final Context context;
    // 행렬용 배열 (4x4 행렬 = 16 크기)
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix  = new float[16];
    private final float[] projMatrix  = new float[16];
    private final float[] mvpMatrix   = new float[16]; // 최종 변환 행렬
    private List<Planet> planets = new ArrayList<>();

    private OnPlanetClickListener listener;

    private PlanetShader planetShader;
    private DrawSphere DrawSphere;

    public BaseRenderer(Context context) {
        this.context = context; // ✅ context 저장
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 예시: 행성 2개 추가
     //   planets.add(new Planet(new float[]{0f, 0f, -5f}, 1.0f)); // (x,y,z), 반지름
   //     planets.add(new Planet(new float[]{2f, 1f, -6f}, 0.5f));

        planetShader = new PlanetShader(context);
        DrawSphere = new DrawSphere(20, 20, 1.0f); // stacks, slices, radius
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 배경/행성 그리기
        // 1. 화면 초기화 (배경색 = 진한 남색)
        GLES20.glClearColor(0.05f, 0.05f, 0.2f, 1.0f); // R,G,B,A
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // 2. 깊이 테스트 활성화 (3D용)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // 3. 구(행성) 렌더링
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0f, 0f, -5f); // z축 -5 위치에 배치
        Matrix.scaleM(modelMatrix, 0, 1f, 1f, 1f);      // 구 크기 (반지름 1)

        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0);

        planetShader.useProgram();
        planetShader.setUniforms(mvpMatrix, new float[]{0.7f, 0.7f, 0.7f, 1f}); // 회색 구
        DrawSphere.bindData(planetShader.getPositionAttributeLocation());
        DrawSphere.draw();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1, 1, 1, 100);
        Matrix.setLookAtM(viewMatrix, 0,
                0, 0, 0,   // 카메라 위치
                0, 0, -1,  // 카메라가 보는 방향
                0, 1, 0);  // Up 벡터
    }

    public void handleTouch(float x, float y, int screenWidth, int screenHeight) {
        // 1) NDC 좌표계 (-1~1)
        float nx = (2.0f * x) / screenWidth - 1.0f;
        float ny = 1.0f - (2.0f * y) / screenHeight;

        // 2) Ray 생성 (Clip → Eye → World 변환)
        float[] nearPointNDC = {nx, ny, -1f, 1f};
        float[] farPointNDC  = {nx, ny,  1f, 1f};

        float[] invVP = new float[16];
        float[] vpMatrix = new float[16];
        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0);
        Matrix.invertM(invVP, 0, vpMatrix, 0);

        float[] nearPointWorld = new float[4];
        float[] farPointWorld  = new float[4];
        Matrix.multiplyMV(nearPointWorld, 0, invVP, 0, nearPointNDC, 0);
        Matrix.multiplyMV(farPointWorld,  0, invVP, 0, farPointNDC, 0);

        for (int i = 0; i < 3; i++) {
            nearPointWorld[i] /= nearPointWorld[3];
            farPointWorld[i]  /= farPointWorld[3];
        }

        // Ray 시작점 & 방향
        float[] rayOrigin = {nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]};
        float[] rayDir = {
                farPointWorld[0] - nearPointWorld[0],
                farPointWorld[1] - nearPointWorld[1],
                farPointWorld[2] - nearPointWorld[2]
        };
        normalize(rayDir);

        // 3) 각 행성과 충돌 판정
        for (int i = 0; i < planets.size(); i++) {
            if (rayIntersectsSphere(rayOrigin, rayDir, planets.get(i))) {
                if (listener != null) {
                    int planetId = i;
                    new Handler(Looper.getMainLooper()).post(() ->
                            listener.onPlanetClicked(planetId)
                    );
                }
            }
        }
    }

    private boolean rayIntersectsSphere(float[] rayOrigin, float[] rayDir, Planet planet) {
        float[] center = planet.position;
        float[] L = {center[0] - rayOrigin[0], center[1] - rayOrigin[1], center[2] - rayOrigin[2]};
        float tca = dot(L, rayDir);
        if (tca < 0) return false;
        float d2 = dot(L, L) - tca * tca;
        return d2 <= planet.radius * planet.radius;
    }

    private float dot(float[] a, float[] b) {
        return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
    }

    private void normalize(float[] v) {
        float len = (float)Math.sqrt(dot(v, v));
        v[0] /= len; v[1] /= len; v[2] /= len;
    }

    public void setOnPlanetClickListener(OnPlanetClickListener l) {
        this.listener = l;
    }


}


