package com.ex.realcv.TodoMain;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import com.ex.realcv.R;

public class SkyboxRenderer {
    private final SkyboxCube cube;
    private final int program;

    private final int aPositionLocation;
    private final int uMatrixLocation;
    private final int uSkyboxLocation;

    private final int[] textureId;

    public SkyboxRenderer(Context context) {
        cube = new SkyboxCube();

        // 셰이더 로드
        String vertexShaderCode = ShaderUtils.readTextFileFromRawResource(context, R.raw.skybox_vertex);
        String fragmentShaderCode = ShaderUtils.readTextFileFromRawResource(context, R.raw.skybox_fragment);

        int vertexShader = ShaderUtils.compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ShaderUtils.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        aPositionLocation = GLES20.glGetAttribLocation(program, "a_Position");
        uMatrixLocation = GLES20.glGetUniformLocation(program, "u_Matrix");
        uSkyboxLocation = GLES20.glGetUniformLocation(program, "u_Skybox");

        // CubeMap 텍스처 로드 (예: 6개 면 이미지를 로딩)
        textureId = SkyboxUtilis.loadCubeMap(context, new int[]{
                R.drawable.right,  // +X
                R.drawable.left,   // -X
                R.drawable.top,    // +Y
                R.drawable.bottom, // -Y
                R.drawable.front,  // +Z
                R.drawable.back    // -Z
        });
    }

    public void draw(float[] viewMatrix, float[] projMatrix) {
        GLES20.glUseProgram(program);

        // 카메라의 위치 이동 제거 (회전만 반영)
        float[] view = new float[16];
        System.arraycopy(viewMatrix, 0, view, 0, viewMatrix.length);
        view[12] = view[13] = view[14] = 0f;

        float[] matrix = new float[16];
        Matrix.multiplyMM(matrix, 0, projMatrix, 0, view, 0);

        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        // CubeMap 텍스처 활성화
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId[0]);
        GLES20.glUniform1i(uSkyboxLocation, 0);

        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        // 정점 버퍼 설정
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, cube.getVertexBuffer());

        // 큐브 그리기
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, cube.getIndexCount(),
                GLES20.GL_UNSIGNED_BYTE, cube.getIndexBuffer());

        GLES20.glDisableVertexAttribArray(aPositionLocation);

        GLES20.glDepthFunc(GLES20.GL_LESS);
    }
}