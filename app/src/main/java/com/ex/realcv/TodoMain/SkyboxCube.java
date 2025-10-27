package com.ex.realcv.TodoMain;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class SkyboxCube {
    private static final float[] VERTICES = {
            // X, Y, Z (큐브 8개 꼭짓점)
            -1f,  1f,  1f,   // 0
            -1f, -1f,  1f,   // 1
            1f, -1f,  1f,   // 2
            1f,  1f,  1f,   // 3
            -1f,  1f, -1f,   // 4
            -1f, -1f, -1f,   // 5
            1f, -1f, -1f,   // 6
            1f,  1f, -1f    // 7
    };

    private static final byte[] INDICES = {
            // 앞, 뒤, 좌, 우, 위, 아래
            0,1,2, 0,2,3,    // Front
            7,6,5, 7,5,4,    // Back
            4,5,1, 4,1,0,    // Left
            3,2,6, 3,6,7,    // Right
            4,0,3, 4,3,7,    // Top
            1,5,6, 1,6,2     // Bottom
    };

    private final FloatBuffer vertexBuffer;
    private final ByteBuffer indexBuffer;

    public SkyboxCube() {
        vertexBuffer = ByteBuffer.allocateDirect(VERTICES.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(VERTICES).position(0);

        indexBuffer = ByteBuffer.allocateDirect(INDICES.length);
        indexBuffer.put(INDICES).position(0);
    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public ByteBuffer getIndexBuffer() {
        return indexBuffer;
    }

    public int getIndexCount() {
        return INDICES.length;
    }
}
