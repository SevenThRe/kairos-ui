#version 120
uniform sampler2D uTexture;
uniform vec2 uTexel;
uniform float uOffset;
varying vec2 vUv;
void main() {
    vec2 d = uTexel * uOffset;
    vec4 color = texture2D(uTexture, vUv + vec2(-d.x, -d.y));
    color += texture2D(uTexture, vUv + vec2(d.x, -d.y));
    color += texture2D(uTexture, vUv + vec2(-d.x, d.y));
    color += texture2D(uTexture, vUv + vec2(d.x, d.y));
    gl_FragColor = color * 0.25;
}
