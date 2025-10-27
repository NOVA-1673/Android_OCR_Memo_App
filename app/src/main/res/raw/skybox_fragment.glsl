// skybox_fragment.glsl
precision mediump float;

uniform samplerCube u_Skybox;
varying vec3 v_Position;

void main() {
    gl_FragColor = textureCube(u_Skybox, v_Position);
}