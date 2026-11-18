#version 120
varying vec2 coordinate;
void main() {
    gl_Position = gl_Vertex;
    coordinate = gl_MultiTexCoord0.xy;
}
