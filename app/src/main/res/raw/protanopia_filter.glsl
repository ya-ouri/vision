#version 300 es
precision mediump float;

in vec2 vTexCoord;
uniform sampler2D uTexture;
out vec4 fragColor;

void main() {
    vec4 color = texture(uTexture, vTexCoord);

    // Преобразование для эмуляции протанопии
    float r = 0.567 * color.r + 0.433 * color.g + 0.0 * color.b;
    float g = 0.558 * color.r + 0.442 * color.g + 0.0 * color.b;
    float b = 0.0 * color.r + 0.242 * color.g + 0.758 * color.b;

    fragColor = vec4(r, g, b, color.a);
}