// skybox_vertex.glsl
attribute vec3 a_Position;   // 정점 좌표 (x,y,z)
uniform mat4 u_Matrix;       // View × Projection (translation 제거)
varying vec3 v_Position;     // 프래그먼트 셰이더에 넘길 방향 벡터

void main() {
    v_Position = a_Position;
    gl_Position = u_Matrix * vec4(a_Position, 1.0);

    // 원근 왜곡 방지 → 깊이값을 살짝 조정
    gl_Position = gl_Position.xyww;
}