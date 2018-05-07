package com.example.danielmoralesgonzale.bricksgame.game;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import com.example.danielmoralesgonzale.bricksgame.forms.Ball;
import com.example.danielmoralesgonzale.bricksgame.forms.Brick;
import com.example.danielmoralesgonzale.bricksgame.forms.Brick.Type;
import com.example.danielmoralesgonzale.bricksgame.forms.Paddle;
import com.example.danielmoralesgonzale.bricksgame.game.Constants.Collision;
import com.example.danielmoralesgonzale.bricksgame.game.Constants.Color;
import com.example.danielmoralesgonzale.bricksgame.game.Constants.Config;
import com.example.danielmoralesgonzale.bricksgame.game.Constants.Difficult;
import com.example.danielmoralesgonzale.bricksgame.game.Constants.Hit;
import com.example.danielmoralesgonzale.bricksgame.game.Constants.Lives;
import com.example.danielmoralesgonzale.bricksgame.game.Constants.Score;
import com.example.danielmoralesgonzale.bricksgame.game.Constants.ScoreMultiplier;

public class Game {

	// Constants
	private static final int SCREEN_INITIAL_X = 0;
	private static final int SCREEN_INITIAL_Y = 0;

	//Game objects
	private Paddle mPaddle;
	private Ball mBall;
	private Brick[][] mBricks;
	private HashMap<Collision, Integer> mConsecutiveCollision;

	// Game State preferences

	public Game(Context context) {
		// Create level elements
		resetElements();
	}

	public void resetElements() {

		State.setScreenMeasures(2.0f, 2.0f);

		// Initialize game state
		State.setGamePaused(true);
		State.setGameOver(false);
		State.setLives(Lives.RESTART_LEVEL);
		State.setScore(Score.RESTART_LEVEL);
		State.setScoreMultiplier(ScoreMultiplier.RESTART_LEVEL);
		mConsecutiveCollision = new HashMap<Collision, Integer>();
		for (Collision type : Config.CONSECUTIVE_COLLISION_DETECTION) {
			mConsecutiveCollision.put(type, 0);
		}

		// Initialize graphics
		mPaddle = new Paddle(Color.WHITE, Config.PADDLE_INITIAL_POS_X, Config.PADDLE_INITIAL_POS_Y);


		mBall = new Ball(Color.WHITE, Config.BALL_INITIAL_PREVIOUS_POS_X, Config.BALL_INITIAL_PREVIOUS_POS_Y,
				Config.BALL_INITIAL_POS_X, Config.BALL_INITIAL_POS_Y, Difficult.BALL_SPEED[State.getDifficult()]);


		createLevel(Config.NUMBER_OF_LINES_OF_BRICKS, Config.NUMBER_OF_COLUMNS_OF_BRICKS,
				Config.BRICKS_INITIAL_POS_X, Config.BRICKS_INITIAL_POS_Y);

	}

	private void createLevel (int blocksX, int blocksY, float initialX, float initialY) {
		mBricks = new Brick[blocksX][blocksY];

		// The initial position of the brick should be the one passed from the call of this function
		float newPosX = initialX;
		float newPosY = initialY;

		for (int i = 0; i < blocksX; i++) {
			for (int j = 0; j < blocksY; j++) {

					mBricks[i][j] = new Brick(Color.WHITE, newPosX, newPosY, Type.NORMAL);

				// The position of the next brick on the same line should be on the right side of the last brick
				newPosX += mBricks[i][j].getSizeX() + Config.SPACE_BETWEEN_BRICKS;
			}
			// Finished filling a line of bricks, resetting to initial X position to fill the next line
			newPosX = initialX;
			// Same as the X position, put the next line of bricks on bottom of the last one
			newPosY += mBricks[i][0].getSizeY() + Config.SPACE_BETWEEN_BRICKS;
		}

	}

	public void drawElements(GL10 gl) {
		// Draw ball and paddle elements on surface
		mPaddle.draw(gl);
		mBall.draw(gl);

		// Need to draw each block on surface
		for (int i=0; i<mBricks.length; i++) {
			for (int j=0; j<mBricks[i].length; j++) {
				// Checking if the brick is not destroyed
				if (mBricks[i][j] != null) {
					mBricks[i][j].draw(gl);
				}
			}
		}
	}


	public void updatePaddlePosX(float x) {
		/* We need to update Paddle position from touch, but we can't access touch updates
		 * directly from TouchSurfaceView, so create a wrapper and call it. */
		mPaddle.setPosX(x);
	}

	private float calcReflectedAngle(float x2, float x1) {
		return Constants.ANGLE_OF_REFLECTION_BOUND * (x2 - x1)/(mPaddle.getWidth()/2);
	}

	private Collision detectConsecutiveCollision(Collision collisionType) {

		for(Map.Entry<Collision, Integer> entry : mConsecutiveCollision.entrySet()) {

			Collision currentType = entry.getKey();
			int currentValue = entry.getValue();

			if(currentValue > 100) {

				mBall = new Ball(Color.WHITE,
						Config.BALL_INITIAL_PREVIOUS_POS_X, Config.BALL_INITIAL_PREVIOUS_POS_Y,
						Config.BALL_INITIAL_POS_X, Config.BALL_INITIAL_POS_Y,
						Difficult.BALL_SPEED[State.getDifficult()]);
				entry.setValue(0);
				State.setGamePaused(true);
				return Collision.NOT_AVAILABLE;
			} else if(currentType == collisionType && currentValue > 0) {

				return Collision.NOT_AVAILABLE;
			} else if(currentType == collisionType) {

				entry.setValue(++currentValue);
			} else if(currentValue > 0) {

				entry.setValue(--currentValue);
			}
		}
		return collisionType;

	}

	public void updateState() {
		float reflectedAngle = 0.0f, angleOfBallSlope = 0.0f;

		Collision collisionType = detectConsecutiveCollision(detectCollision());

		switch(collisionType) {
			case WALL_RIGHT_LEFT_SIDE:
				mBall.turnToPerpendicularDirection(Hit.RIGHT_LEFT);
				break;
			case WALL_TOP_BOTTOM_SIDE:
				mBall.turnToPerpendicularDirection(Hit.TOP_BOTTOM);
				break;
			case BRICK_BALL:
				State.setScore(Score.BRICK_HIT);
				State.setScoreMultiplier(ScoreMultiplier.BRICK_HIT);
				mBall.turnToPerpendicularDirection(Hit.TOP_BOTTOM);
				break;
			case EX_BRICK_BALL:
				State.setScore(Score.EX_BRICK_HIT);
				State.setScoreMultiplier(ScoreMultiplier.BRICK_HIT);
				mBall.turnToPerpendicularDirection(Hit.TOP_BOTTOM);
				break;
			case PADDLE_BALL:

				State.setScoreMultiplier(ScoreMultiplier.PADDLE_HIT);
				if(mPaddle.getPosX() >= mBall.getPosX()) {
					reflectedAngle = calcReflectedAngle(mBall.getPosX(), mPaddle.getPosX());
					angleOfBallSlope = (Constants.RIGHT_ANGLE - reflectedAngle);
				} else {
					reflectedAngle = calcReflectedAngle(mPaddle.getPosX(), mBall.getPosX());
					angleOfBallSlope = -1 * (Constants.RIGHT_ANGLE - reflectedAngle);
				}
				mBall.turnByAngle(angleOfBallSlope);
				break;
			case LIFE_LOST:
				State.setLives(Lives.LOST_LIFE);
				// If the user still has lives left, create a new ball and reset score multiplier
				if(!State.getGameOver()) {

					mBall = new Ball(Color.WHITE,
							Config.BALL_INITIAL_PREVIOUS_POS_X, Config.BALL_INITIAL_PREVIOUS_POS_Y,
							Config.BALL_INITIAL_POS_X, Config.BALL_INITIAL_POS_Y,
							Difficult.BALL_SPEED[State.getDifficult()]);
					State.setScoreMultiplier(ScoreMultiplier.LOST_LIFE);
					State.setGamePaused(true);
				} else {

				}
				break;
			case NOT_AVAILABLE:
				break;
			default:
				break;
		}
		mBall.move();
	}

	private Collision detectCollision() {

		// Detecting collision between ball and wall
		if (mBall.getBottomY() <= State.getScreenLowerY()			//if invincibility is off and the ball
				&& !Difficult.INVINCIBILITY[State.getDifficult()])		//collided with bottom wall, user loses a life
		{
			return Collision.LIFE_LOST;
		} else if ((mBall.getTopY() >= State.getScreenHigherY())	//collided in the top wall
				|| (mBall.getBottomY() <= State.getScreenLowerY()	//collided in the bottom wall...
				&& Difficult.INVINCIBILITY[State.getDifficult()]))	//...with invincibility mode on
		{
			return Collision.WALL_TOP_BOTTOM_SIDE;
		} else if ((mBall.getRightX() >= State.getScreenHigherX())	//collided in the right wall
				|| (mBall.getLeftX() <= State.getScreenLowerX()))	//collided in the left wall
		{
			return Collision.WALL_RIGHT_LEFT_SIDE;
		}

		//detecting collision between the ball and the paddle
		if (mBall.getTopY() >= mPaddle.getBottomY() && mBall.getBottomY() <= mPaddle.getTopY() &&
				mBall.getRightX() >= mPaddle.getLeftX() && mBall.getLeftX() <= mPaddle.getRightX())
		{
			return Collision.PADDLE_BALL;
		}

		// If the game is finished, there should be no bricks left
		boolean gameFinish = true;

		for (int i = 0; i<mBricks.length; i++) {
			boolean checkedLine =  false;
			for (int j=0; j<mBricks[i].length; j++) {
				// Check if the brick is not destroyed yet
				if(mBricks[i][j] != null) {
					// If there are still bricks, the game is not over yet
					gameFinish = false;

					// Check if the ball is in the same Y position as the brick
					if (checkedLine
							|| (mBall.getTopY() >= mBricks[i][j].getBottomY()
							&& mBall.getBottomY() <= mBricks[i][j].getTopY()))
					{
						checkedLine = true;
						// Check if the collision actually happened
						if (mBall.getRightX() >= mBricks[i][j].getLeftX()
								&& mBall.getLeftX() <= mBricks[i][j].getRightX())
						{

							/* Since the update happens so fast (on each draw frame) we can update the brick
							 * state on the next frame. */
							if (mBricks[i][j].getLives() == 0) {
								mBricks[i][j] = null; // Deleting brick
							}
							return Collision.BRICK_BALL;
						}
					} else {
						break;
					}
				}
			}
		}
		// If there is no more blocks, the game is over
		State.setGameOver(gameFinish);

		return Collision.NOT_AVAILABLE;
	}


	/**
	 * Represents the game state, like the actual game score and multiplier, number of lives and
	 * if the game is over or not.
	 */
	public static class State {
		private static long sScore;
		private static int sScoreMultiplier;
		private static int sLives;
		private static boolean sGameOver;
		private static float sScreenHigherY;
		private static float sScreenLowerY;
		private static float sScreenHigherX;
		private static float sScreenLowerX;
		private static boolean sGamePaused;
		private static int sDifficult;
		private static float sVolume;

		public static void setScore (Score event) {
			switch(event) {
				case BRICK_HIT:
					sScore += Difficult.HIT_SCORE[sDifficult] * getScoreMultiplier();
					break;
				case RESTART_LEVEL:
					sScore = 0;
					break;
				case EX_BRICK_HIT:
					sScore += Difficult.HIT_SCORE[sDifficult] * 2 * getScoreMultiplier();
					break;
			}
		}

		public static void setScoreMultiplier(ScoreMultiplier event) {
			switch(event) {
				case RESTART_LEVEL:
				case LOST_LIFE:
					sScoreMultiplier = 1;
					break;
				case BRICK_HIT:
					if (sScoreMultiplier < Difficult.MAX_SCORE_MULTIPLIER[sDifficult]) {
						sScoreMultiplier *= 2;
					}
					break;
				case PADDLE_HIT:
					if (sScoreMultiplier > 1) {
						sScoreMultiplier /= 2;
					}
					break;
			}
		}

		public static void setLives(Lives event) {
			switch(event) {
				case RESTART_LEVEL:
					sGameOver = false;
					sLives = Difficult.LIFE_STOCK[sDifficult];
					break;
				case LOST_LIFE:
					if (sLives > 0) {
						sLives--;
					} else {
						sGameOver = true;
					}
					break;
			}
		}

		public static void setDifficult(int difficult) {
			if (difficult < 0) {

				// If there is some problem on difficult setting, set it to debug ("Can't die")
				sDifficult = 0;
			} else {
				sDifficult = difficult;
			}
		}

		public static void setGameOver(boolean gameIsOver) {
			// Add bonus points for each extra life the user has
			if (gameIsOver) {
				sScore += sLives * Difficult.LIFE_SCORE_BONUS[sDifficult];
			}
			sGameOver = gameIsOver;
		}

		public static void setGamePaused(boolean gamePaused) {
			sGamePaused = gamePaused;
		}


		public static boolean getGameOver() {
			return sGameOver;
		}

		public static boolean getGamePaused() {
			return sGamePaused;
		}

		public static long getScore() {
			return sScore;
		}

		public static int getScoreMultiplier() {
			return sScoreMultiplier;
		}

		public static int getLives() {
			return sLives;
		}

		public static float getScreenLowerX() {
			return sScreenLowerX;
		}

		public static float getScreenHigherX() {
			return sScreenHigherX;
		}

		public static float getScreenLowerY() {
			return sScreenLowerY;
		}

		public static float getScreenHigherY() {
			return sScreenHigherY;
		}

		public static void setScreenMeasures(float screenWidth, float screenHeight) {
			/* Calculate the new screen measure. This is important since we need to delimit a wall
			 * to the ball. */
			sScreenLowerX = SCREEN_INITIAL_X - screenWidth/2;
			sScreenHigherX = SCREEN_INITIAL_X + screenWidth/2;
			sScreenLowerY = SCREEN_INITIAL_Y - screenHeight/2;
			sScreenHigherY = SCREEN_INITIAL_Y + screenHeight/2;
		}

		public static int getDifficult() {
			return sDifficult;
		}

	}
}
