package com.ex.realcv.TodoMain;

import android.content.Context;
import android.opengl.GLES20;
import com.ex.realcv.R;
public class PlanetShader {
    private final int program;

    // Attribute / Uniform locations
    private final int aPositionLocation;
    private final int uMVPMatrixLocation;
    private final int uColorLocation;

    public PlanetShader(Context context) {
        // GLSL 파일을 res/raw/에서 읽기
        String vertexShaderCode = ShaderUtils.readTextFileFromRawResource(context, R.raw.vertex_shader);
        String fragmentShaderCode = ShaderUtils.readTextFileFromRawResource(context, R.raw.fragment_shader);

        // 셰이더 컴파일
        int vertexShader = ShaderUtils.compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ShaderUtils.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // 프로그램 생성
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        // Attribute / Uniform 변수 위치 가져오기
        aPositionLocation = GLES20.glGetAttribLocation(program, "a_Position");
        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
        uColorLocation = GLES20.glGetUniformLocation(program, "u_Color");
    }

    public void useProgram() {
        GLES20.glUseProgram(program);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public void setUniforms(float[] mvpMatrix, float[] color) {
        // MVP 행렬 전달
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
        // 색상 전달 (RGBA)
        GLES20.glUniform4fv(uColorLocation, 1, color, 0);
    }
}