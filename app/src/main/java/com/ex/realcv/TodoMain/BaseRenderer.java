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
import android.util.Log;

//EGL
import com.ex.realcv.MemoMain.FileMemoRepository;
import com.ex.realcv.MemoMain.Memo;

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
    private final List<Planet> planets = new ArrayList<>();
    private final List<Memo> memos = new ArrayList<>();

    private SkyboxRenderer skyboxRenderer;

    private OnPlanetClickListener listener;

    private PlanetShader planetShader;
    private DrawSphere DrawSphere;
    private SpaceManager spaceManager;

    private float yaw = 0f;   // 좌우 회전
    private float pitch = 0f; // 상하 회전

    public BaseRenderer(Context context) {
        this.context = context; // ✅ context 저장
    }

    // 카메라 위치

    // 카메라 현재 위치
    private float[] Cam_eye = {0f, 0f, 5f};
    // 카메라 현재 바라보는 지점
    private float[] Cam_center = {0f, 0f, 0f};
    private float[] Cam_up = {0f, 1f, 0f};

    // 카메라 애니메이션용
    private float[] Cam_targetEye = {0f, 0f, 5f};
    private float[] Cam_targetCenter = {0f, 0f, 0f};
    private float Cam_cameraLerpSpeed = 0.05f; // 값이 작을수록 느리게 이동
    enum CameraMode {
        FREE_LOOK,    // 사용자가 터치로 yaw/pitch 조작
        FOCUS_PLANET  // 특정 행성 바라보기
    }

    private CameraMode cameraMode = CameraMode.FREE_LOOK;
    // 오차 허용값
    private static final float EPSILON = 0.05f;

    public void setMemos(List<Memo> newMemos) {
        memos.clear();
        memos.addAll(newMemos);


    }

    public void rebuildPlanetsFromMemos() {
        planets.clear();
        int n = memos.size();
        for (int i = 0; i < n; i++) {
            float angle = (float) (i * (360.0 /  n));
            float x = (float) Math.cos(Math.toRadians(angle)) * 3f;
            float z = (float) Math.sin(Math.toRadians(angle)) * 3f;
            planets.add(new Planet(0f, 0f, -5f, 1f));
        }
        Log.d("planets " , " planets : " + planets.size());
        // ... 메모→행성 배치 생성 로직 ...
    }


    public void handleCameraRotation(float dx, float dy) {

        //if (cameraMode != CameraMode.FREE_LOOK) return;
        if (cameraMode == CameraMode.FOCUS_PLANET) {
            // 전환 직전 방향 동기화
            syncYawPitchFromCurrentLook();
            cameraMode = CameraMode.FREE_LOOK;
        }

        yaw += dx;
        pitch += dy;

       // Log.d("test","yaw : ${yaw}");

        // pitch 제한 (-90 ~ +90)
        pitch = Math.max(-89f, Math.min(89f, pitch));
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 예시: 행성 2개 추가
     //   planets.add(new Planet(new float[]{0f, 0f, -5f}, 1.0f)); // (x,y,z), 반지름
   //     planets.add(new Planet(new float[]{2f, 1f, -6f}, 0.5f));
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        skyboxRenderer = new SkyboxRenderer(context);


        planetShader = new PlanetShader(context);
        DrawSphere = new DrawSphere(20, 20, 1.0f); // stacks, slices, radius

        // 카메라 초기 위치
        Matrix.setLookAtM(viewMatrix, 0,
                Cam_eye[0], Cam_eye[1], Cam_eye[2],
                0f, 0f, 0f,
                0f, 1f, 0f);

        // 우주 초기화
        spaceManager = new SpaceManager();
        spaceManager.initLayers(5, 8f);   // 5개의 레이어, 반경 8 단위 간격
        //spaceManager.generatePlanets(10); // 각 레이어당 10개씩


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1, 1, 1, 100);
       // Matrix.setLookAtM(viewMatrix, 0,
       //         0, 0, 0,   // 카메라 위치
        //        0, 0, -1,  // 카메라가 보는 방향
       //         0, 1, 0);  // Up 벡터
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 배경/행성 그리기
        // 1. 화면 초기화 (배경색 = 진한 남색)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0.05f, 0.05f, 0.2f, 1.0f); // R,G,B,A

        // 카메라 방향 계산
        if (cameraMode == CameraMode.FREE_LOOK) {
            // yaw/pitch 기반 방향
            // 라디안 미리 계산

            float[] fv = calcForwardVector();
            // center = eye + forward  (절대 좌표!)
            Cam_center[0] = Cam_eye[0] + fv[0];
            Cam_center[1] = Cam_eye[1] + fv[1];
            Cam_center[2] = Cam_eye[2] + fv[2];
        } else if (cameraMode == CameraMode.FOCUS_PLANET) {
            // Lerp로 부드럽게 목표로 이동
            // 부드럽게 카메라 이동
            boolean arrived = true;
            for (int i = 0; i < 3; i++) {
                Cam_eye[i] += (Cam_targetEye[i] - Cam_eye[i]) * Cam_cameraLerpSpeed;
                Cam_center[i] += (Cam_targetCenter[i] - Cam_center[i]) * Cam_cameraLerpSpeed;

                // 아직 목표와 차이가 크면 도착 X
                if (Math.abs(Cam_targetEye[i] - Cam_eye[i]) > EPSILON ||
                        Math.abs(Cam_targetCenter[i] - Cam_center[i]) > EPSILON) {
                    arrived = false;
                }
            }

            // 목표 위치 도착 → 자유 회전 모드로 전환
            if (arrived) {
                syncYawPitchFromCurrentLook();
                cameraMode = CameraMode.FREE_LOOK;
            }
        }
        Matrix.setLookAtM(viewMatrix, 0,
                Cam_eye[0], Cam_eye[1], Cam_eye[2],
                Cam_center[0], Cam_center[1], Cam_center[2],
                Cam_up[0], Cam_up[1], Cam_up[2]);

        // ✅ 1. Skybox 먼저 그리기
        skyboxRenderer.draw(viewMatrix, projMatrix);

        // ✅ 2. 행성(메모) 그리기
        planetShader.useProgram();

        if(!planets.isEmpty()){
            for (Planet planet : planets) {
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix, 0, planet.x, planet.y, planet.z);

                float[] temp = new float[16];
                Matrix.multiplyMM(temp, 0, viewMatrix, 0, modelMatrix, 0);   // V*M
                Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, temp, 0);     // P*(V*M)

                planetShader.setUniforms(mvpMatrix, planet.color);
                DrawSphere.bindData(planetShader.getPositionAttributeLocation());
                DrawSphere.draw();
            }
        }

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
        // 행성 중심 좌표
        float[] center = {planet.x, planet.y, planet.z};

        // 벡터 L = center - rayOrigin
        float[] L = {
                center[0] - rayOrigin[0],
                center[1] - rayOrigin[1],
                center[2] - rayOrigin[2]
        };

        // 광선과 구의 중심 간의 투영 길이
        float tca = dot(L, rayDir);
        if (tca < 0) return false; // 뒤쪽이면 교차 없음

        // 중심에서 광선까지의 최소 거리 제곱
        float d2 = dot(L, L) - tca * tca;

        // 반지름과 비교
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


    // ✅ 카메라 이동용 (터치 이벤트 등에서 호출 가능)
    public void moveCamera(float dx, float dy, float dz) {
        Cam_eye[0] += dx;
        Cam_eye[1] += dy;
        Cam_eye[2] += dz;

        Matrix.setLookAtM(viewMatrix, 0,
                Cam_eye[0], Cam_eye[1], Cam_eye[2],
                0f, 0f, 0f,
                0f, 1f, 0f);
    }

    // ✅ 새로운 영역으로 이동 + 행성 생성
    public void moveToNewArea() {
        // 카메라를 랜덤한 방향으로 전진
        float x = (float)(Math.random() * 6f - 3f);
        float y = (float)(Math.random() * 6f - 3f);
        float z = (float)(Math.random() * -10f - 5f);
        float radius = 0.5f + (float)Math.random() * 1.5f;

        Planet newPlanet = new Planet(x, y, z, radius);
        planets.add(newPlanet);

        // 새 카메라 목표 위치 (행성 뒤쪽)
        float offset = -5f;
        Cam_targetEye[0] = x;
        Cam_targetEye[1] = y;
        Cam_targetEye[2] = z + offset;

        // 바라볼 지점 = 행성 중심
        Cam_targetCenter[0] = x;
        Cam_targetCenter[1] = y;
        Cam_targetCenter[2] = z;

        cameraMode = CameraMode.FOCUS_PLANET;
    }


    private float[] calcForwardVector() {
        float ry = (float) Math.toRadians(yaw);
        float rp = (float) Math.toRadians(pitch);
        float cy = (float) Math.cos(ry), sy = (float) Math.sin(ry);
        float cp = (float) Math.cos(rp), sp = (float) Math.sin(rp);

        // 전방(forward) 벡터
        float fx = sy * cp;
        float fy = sp;
        float fz = -cy * cp;

        return new float[]{fx, fy, fz};
    }

    private void syncYawPitchFromCurrentLook() {
        float fx = Cam_center[0] - Cam_eye[0];
        float fy = Cam_center[1] - Cam_eye[1];
        float fz = Cam_center[2] - Cam_eye[2];

        // forward 정규화
        float len = (float)Math.sqrt(fx*fx + fy*fy + fz*fz);
        if (len < 1e-6f) return;
        fx /= len; fy /= len; fz /= len;

        // calcForwardVector()와의 축 정의를 그대로 역이용
        // 거기선: fx =  sy*cp, fy = sp, fz = -cy*cp
        // ⇒ pitch = asin(fy)
        float rp = (float)Math.asin(fy);
        float cp = (float)Math.cos(rp);

        // cp가 0에 가까우면 yaw 계산이 불안정 → 가드
        float ry;
        if (Math.abs(cp) < 1e-6f) {
            // 수직에 가까움: yaw는 이전 값 유지
            ry = (float)Math.toRadians(yaw);
        } else {
            // fx = sy*cp,  -fz = cy*cp  ⇒ tan(yaw) = fx / (-fz)
            ry = (float)Math.atan2(fx, -fz);
        }

        // 도(deg)로 저장
        pitch = (float)Math.toDegrees(rp);
        yaw   = (float)Math.toDegrees(ry);

        // 기존 클램프 적용
        pitch = Math.max(-89f, Math.min(89f, pitch));
    }
}


