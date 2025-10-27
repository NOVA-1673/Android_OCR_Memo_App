package com.ex.realcv.TodoMain;

import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLES20;
        import android.opengl.GLUtils;

public class SkyboxUtilis {
    public static int[] loadCubeMap(Context context, int[] resourceIds) {
        final int[] textureId = new int[1];
        GLES20.glGenTextures(1, textureId, 0);

        if (textureId[0] == 0) {
            throw new RuntimeException("CubeMap texture load failed");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId[0]);

        // 각 면에 텍스처 바인딩
        for (int i = 0; i < 6; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceIds[i]);
            Matrix flip = new Matrix();
            if (i == 2 ) { // top, bottom
                flip.postRotate(90); // 필요하면 90, -90으로 조정
                flip.preScale(1f,-1f);
            } else if(i == 3){
                flip.postRotate(-90); // 필요하면 90, -90으로 조정
                flip.preScale(1f,-1f);
            } else {

                flip.preScale(-1f, -1f);

            }

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), flip, true);

            // ✅ Y축 flip


            GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, bitmap, 0);

            bitmap.recycle();
        }

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, 0);

        return textureId;
    }
}
