package com.ex.realcv.TodoMain;

import java.util.Random;
public class Planet {
    float x, y, z;
    float[] color;

    float sid_num;
    float mother_planet;

    private static Random random = new Random();
    float radius;

    Planet(float x, float y, float z, float size) {
        this.x = x;
        this.y = y;
        this.z = z;
        // 랜덤 색상 (0.5~1.0 사이 밝은 계열로)
        this.color = new float[]{
                0.5f + random.nextFloat() * 0.5f,  // R
                0.5f + random.nextFloat() * 0.5f,  // G
                0.5f + random.nextFloat() * 0.5f,  // B
                1.0f                               // A
        };
        this.radius = size;
    }
}
