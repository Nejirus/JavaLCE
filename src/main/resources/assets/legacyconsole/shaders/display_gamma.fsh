#version 120
uniform sampler2D image;
uniform float exponent;
varying vec2 coordinate;
void main() {
    vec4 color = texture2D(image, coordinate);
    gl_FragColor = vec4(pow(clamp(color.rgb, 0.0, 1.0), vec3(exponent)), color.a);
}
