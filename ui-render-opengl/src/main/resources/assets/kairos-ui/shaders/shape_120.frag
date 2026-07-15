#version 120
varying vec2 vUv;
varying vec2 vSize;
varying float vRadius;
varying vec4 vColor;
void main() {
    vec2 p = (vUv - 0.5) * vSize;
    vec2 halfSize = vSize * 0.5;
    vec2 q = abs(p) - halfSize + vec2(vRadius);
    float distance = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - vRadius;
    float alpha = 1.0 - smoothstep(-1.0, 1.0, distance);
    gl_FragColor = vec4(vColor.rgb, vColor.a * alpha);
}
