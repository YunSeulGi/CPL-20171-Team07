package com.thierry.cube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

class Cube {

	public Cube() {
		int one = 0x10000;
		int vertices[] = {

				// FRONT
				-one, -one * 2, one, one, -one * 2, one, -one, one * 2, one, one, one * 2, one,
				// BACK
				-one, -one * 2, -one, -one, one * 2, -one, one, -one * 2, -one, one, one * 2, -one,
				// LEFT
				-one, -one * 2, one, -one, one * 2, one, -one, -one * 2, -one, -one, one * 2, -one,
				// RIGHT
				one, -one * 2, -one, one, one * 2, -one, one, -one * 2, one, one, one * 2, one,
				// TOP
				-one, one, one, one, one, one, -one, one, -one, one, one, -one,
				// BOTTOM
				-one, -one, one, -one, -one, -one, one, -one, one, one, -one, -one,

		};

		float spriteTexcoords[] = {

				// FRONT
				0, 1, 1, 1, 0, 0, 1, 0,
				// BACK
				1, 1, 1, 0, 0, 1, 0, 0,
				// LEFT
				1, 1, 1, 0, 0, 1, 0, 0,
				// RIGHT
				1, 1, 1, 0, 0, 1, 0, 0,
				// TOP
				1, 0, 0, 0, 1, 1, 0, 1,
				// BOTTOM
				0, 0, 0, 1, 1, 0, 1, 1,

		};

		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asIntBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);

		ByteBuffer tbb = ByteBuffer.allocateDirect(spriteTexcoords.length * 4);
		tbb.order(ByteOrder.nativeOrder());
		mTexBuffer = tbb.asFloatBuffer();
		mTexBuffer.position(0);

		for (int i = 0; i < 48; i++) {
			mTexBuffer.put(spriteTexcoords[i]);
		}

		mTexBuffer.position(0);

	}

	public void draw(GL10 gl) {
		gl.glFrontFace(GL10.GL_CCW);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnableClientState(gl.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, gl.GL_FIXED, 0, mVertexBuffer);
		gl.glScalef(1.0f, 1.0f, 1.0f);// gl.glScalef(0.65f, 0.65f, 0.65f);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);

		gl.glBindTexture(GL10.GL_TEXTURE_2D, Cube.mTextureID_front);
		gl.glDrawArrays(gl.GL_TRIANGLE_STRIP, 0, 4);

		gl.glBindTexture(GL10.GL_TEXTURE_2D, Cube.mTextureID_back);
		gl.glDrawArrays(gl.GL_TRIANGLE_STRIP, 4, 4);

		// gl.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, Cube.mTextureID_left);
		gl.glDrawArrays(gl.GL_TRIANGLE_STRIP, 8, 4);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, Cube.mTextureID_right);
		gl.glDrawArrays(gl.GL_TRIANGLE_STRIP, 12, 4);

		// gl.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
		// gl.glBindTexture(GL10.GL_TEXTURE_2D, Cube.mTextureID_top);
		// gl.glDrawArrays(gl.GL_TRIANGLE_STRIP, 16, 4);
		//
		// gl.glBindTexture(GL10.GL_TEXTURE_2D, Cube.mTextureID_bottom);
		// gl.glDrawArrays(gl.GL_TRIANGLE_STRIP, 20, 4);

	}

	private IntBuffer mVertexBuffer;
	private FloatBuffer mTexBuffer;
	public static int mTextureID_front;
	public static int mTextureID_back;
	public static int mTextureID_left;
	public static int mTextureID_right;
	public static int mTextureID_top;
	public static int mTextureID_bottom;

}