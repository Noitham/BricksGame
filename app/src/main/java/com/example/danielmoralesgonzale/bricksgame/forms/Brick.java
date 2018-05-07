package com.example.danielmoralesgonzale.bricksgame.forms;

public class Brick extends Shape {

	public enum Type {
		NORMAL
	}

	private static final float SCALE = 0.1f;
	private static final float[] VERTICES = {
		-0.5f, -0.2f, // bottom left
		-0.5f,  0.2f, // top left
		0.5f, -0.2f, // bottom right
		0.5f,  0.2f, // top right
	};
	
	private int mLives;
	private Type mType;

	public Brick(float[] colors, float posX, float posY, Type type) {
		super(VERTICES, SCALE, colors, posX, posY);
		mType = type;
		switch (type) {
		case NORMAL:
			mLives = 0;
			break;
		}
				
	}

	
	public int getLives() {
		return mLives;
	}
	
	public Type getType() {
		return mType;
	}

}
