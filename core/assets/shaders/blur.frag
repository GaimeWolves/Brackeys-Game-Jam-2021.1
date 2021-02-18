#ifdef GL_ES
#define LOWP lowp
	precision mediump float;
#else
	#define LOWP
#endif

//"in" attributes from our vertex shader
varying LOWP vec4 v_color;
varying vec2 v_texCoords;

//declare uniforms
uniform sampler2D u_texture;
uniform vec2 resolution;
uniform float radius;
uniform vec2 dir;

const float pi = 3.14159265;

void main() {
	if (radius == 0.0)
	{
		gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
		return;
	}

	//the amount to blur, i.e. how far off center to sample from
	//1.0 -> blur by one pixel
	//2.0 -> blur by two pixels, etc.
	vec2 blur = radius / resolution;

	vec3 incrementalGaussian;
	incrementalGaussian.x = 1.0 / (sqrt(2.0 * pi) * radius);
	incrementalGaussian.y = exp(-0.5 / (radius * radius));
	incrementalGaussian.z = incrementalGaussian.y * incrementalGaussian.y;

	vec4 avgValue = vec4(0.0);
	float coefficientSum = 0.0;

	// Take the central sample first...
	avgValue += texture2D(u_texture, v_texCoords) * incrementalGaussian.x;
	coefficientSum += incrementalGaussian.x;
	incrementalGaussian.xy *= incrementalGaussian.yz;

	// Go through the remaining 8 vertical samples (4 on each side of the center)
	for (float i = 1.0; i <= 4.0; i++) {
		avgValue += texture2D(u_texture, v_texCoords - i * blur * dir) * incrementalGaussian.x;
		avgValue += texture2D(u_texture, v_texCoords + i * blur * dir) * incrementalGaussian.x;
		coefficientSum += 2.0 * incrementalGaussian.x;
		incrementalGaussian.xy *= incrementalGaussian.yz;
	}

	gl_FragColor = v_color * (avgValue / coefficientSum);
}