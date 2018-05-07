package com.example.danielmoralesgonzale.bricksgame.forms;

public class Paddle extends Shape {

	private static final float SCALE = 0.1f;
	private static final float[] VERTICES = {
		-1.0f, -0.3f, // bottom left
		-1.0f,  0.3f, // top left
		1.0f, -0.3f, // bottom right
		1.0f,  0.3f, // top right
	};

	public Paddle(float[] colors, float posX, float posY) {
		super(VERTICES, SCALE, colors, posX , posY);
	}
	
}
