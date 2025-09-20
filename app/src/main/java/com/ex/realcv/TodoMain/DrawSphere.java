package com.ex.realcv.TodoMain;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class DrawSphere {
    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private int numIndices;

    public DrawSphere(int stacks, int slices, float radius) {
        List<Float> vertices = new ArrayList<>();
        List<Short> indices = new ArrayList<>();

        // ✅ 정점 좌표 생성
        for (int stack = 0; stack <= stacks; stack++) {
            float phi = (float) Math.PI * stack / stacks;
            float y = (float) Math.cos(phi);
            float r = (float) Math.sin(phi);

            for (int slice = 0; slice <= slices; slice++) {
                float theta = (float) (2 * Math.PI * slice / slices);
                float x = r * (float) Math.cos(theta);
                float z = r * (float) Math.sin(theta);

                vertices.add(x * radius);
                vertices.add(y * radius);
                vertices.add(z * radius);
            }
        }

        // ✅ 인덱스 생성
        for (int stack = 0; stack < stacks; stack++) {
            for (int slice = 0; slice < slices; slice++) {
                short first = (short) ((stack * (slices + 1)) + slice);
                short second = (short) (first + slices + 1);

                indices.add(first);
                indices.add(second);
                indices.add((short) (first + 1));

                indices.add(second);
                indices.add((short) (second + 1));
                indices.add((short) (first + 1));
            }
        }

        // ✅ 버퍼로 변환
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size() * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (float v : vertices) vertexBuffer.put(v);
        vertexBuffer.position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.size() * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        for (short i : indices) indexBuffer.put(i);
        indexBuffer.position(0);

        numIndices = indices.size();
    }

    // ✅ 셰이더와 연결
    public void bindData(int positionAttributeLocation) {
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(
                positionAttributeLocation,
                3,                          // 좌표(x,y,z)
                GLES20.GL_FLOAT,
                false,
                0,
                vertexBuffer
        );
        GLES20.glEnableVertexAttribArray(positionAttributeLocation);
    }

    // ✅ 그리기
    public void draw() {
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                numIndices,
                GLES20.GL_UNSIGNED_SHORT,
                indexBuffer
        );
    }
}
