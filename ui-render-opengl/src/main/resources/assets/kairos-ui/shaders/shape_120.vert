#version 120
attribute vec2 aPosition;
attribute vec2 aUv;
attribute vec2 aSize;
attribute float aRadius;
attribute vec4 aColor;
uniform vec2 uViewport;
varying vec2 vUv;
varying vec2 vSize;
varying float vRadius;
varying vec4 vColor;
void main() {
    vec2 clip = (aPosition / uViewport) * 2.0 - 1.0;
    gl_Position = vec4(clip.x, -clip.y, 0.0, 1.0);
    vUv = aUv;
    vSize = aSize;
    vRadius = aRadius;
    vColor = aColor;
}
