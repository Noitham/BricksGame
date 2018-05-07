package com.example.danielmoralesgonzale.bricksgame;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import com.example.danielmoralesgonzale.bricksgame.game.Constants.Config;
import com.example.danielmoralesgonzale.bricksgame.game.Game.State;
import com.example.danielmoralesgonzale.bricksgame.game.TouchSurfaceView;

public class GameActivity extends Activity {

	private TouchSurfaceView mTouchSurfaceView;
	private Handler mHandler;
	private TextView mScoreTextView;
	private TextView mScoreMultiplierTextView;
	private TextView mLivesTextView;
	private TextView mHighScoreTextView;
	private TextView mReadyTextView;
	private SharedPreferences mSharedPrefs;
	private SharedPreferences.Editor mSharedPrefsEditor;
	private long mHighScore;
	private boolean mNewHighScore;
	private boolean mFinish;
	private View mDecorView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_game);

		mHandler = new Handler();
		mNewHighScore = false;
		mFinish = false;
		mDecorView = getWindow().getDecorView();

		mTouchSurfaceView = (TouchSurfaceView) findViewById(R.id.opengl);
		// Initialize SharedPreferences, so we can save the user High Score
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		mSharedPrefsEditor = mSharedPrefs.edit();
		mHighScore = mSharedPrefs.getLong("high_score", 0);

		/* Initialize TextViews to show user game state (both high and current
		 * score, current score multiplier and number of lives remaining) and change
		 * color of them to give that retro style ;). */
		mScoreTextView = (TextView) findViewById(R.id.score);
		mScoreTextView.setTextColor(Color.WHITE);
		mScoreMultiplierTextView = (TextView) findViewById(R.id.scoreMultiplier);
		mScoreMultiplierTextView.setTextColor(Color.WHITE);
		mLivesTextView = (TextView) findViewById(R.id.lives);
		mLivesTextView.setTextColor(Color.WHITE);
		mHighScoreTextView = (TextView) findViewById(R.id.highScore);
		mHighScoreTextView.setTextColor(Color.GRAY);
		mReadyTextView = (TextView) findViewById(R.id.ready);
		mReadyTextView.setTextColor(Color.RED);

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(!mFinish){
					updateUI();
				} else {
					return;
				}
			}
		}, 0, Config.MS_PER_UPDATE * 10);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mTouchSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(mNewHighScore) {
			mSharedPrefsEditor.putLong("high_score", mHighScore);
			mSharedPrefsEditor.commit();
		}
		// Pause the game if the user exits the app
		State.setGamePaused(true);
		mTouchSurfaceView.onPause();
	}

	/* Change to immersive mode. Since this is only supported on API 19 (KitKat),
	 * build this code only on newer versions of Android. */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (hasFocus) {
				mDecorView.setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE
								| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
								| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_FULLSCREEN
								| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void showGameOverDialog(long finalScore, boolean newHighScore) {
		AlertDialog.Builder builder = null;
		builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.game_over);
		// Show a different message if the player beats the high score or not
		if(newHighScore){
			builder.setMessage(getString(R.string.new_high_score) + finalScore + "\n" +
					getString(R.string.do_you_want_to_restart_the_game));
		} else {
			builder.setMessage(getString(R.string.final_score) + finalScore + "\n" +
					getString(R.string.do_you_want_to_restart_the_game));
		}

		// If the user click Yes, restart this Activity so the user can play again
		builder.setPositiveButton(R.string.yes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				restartGame();
			}
		});

		// If the user click No, go back to the MainActivity
		builder.setNegativeButton(R.string.no, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});

		if(!isFinishing()) builder.show().setCanceledOnTouchOutside(false);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void restartGame() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			recreate();
		} else {
			Intent intent = getIntent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			finish();
			overridePendingTransition(0, 0);

			startActivity(intent);
			overridePendingTransition(0, 0);
		}
	}

	private void updateUI() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {

				/* Show a "Ready?" text in red for the user to know when the game is
				 * paused and ready waiting for the user input */
				if(State.getGamePaused()) {
					mReadyTextView.setVisibility(View.VISIBLE);
				} else {
					mReadyTextView.setVisibility(View.INVISIBLE);
				}

				mScoreTextView.setText(getString(R.string.score) + String.format("%08d", State.getScore()));
				mScoreMultiplierTextView.setText(getString(R.string.multiplier) + State.getScoreMultiplier() + "x");


				if(State.getScore() > mHighScore) {
					mHighScore = State.getScore();
					mNewHighScore = true;
					mHighScoreTextView.setTextColor(Color.GREEN);
				}

				mHighScoreTextView.setText(getString(R.string.high_score) + String.format("%08d", mHighScore));
				mLivesTextView.setText(getString(R.string.lives) + State.getLives());

				if (State.getGameOver()) {
					/* Show user score and ask if he wants to play again */
					showGameOverDialog(State.getScore(), mNewHighScore);
					/* If the user beats his High Score, save his new high score on SharedPreferences */
					if(mNewHighScore) {
						mSharedPrefsEditor.putLong("high_score", mHighScore);
						mSharedPrefsEditor.commit();
					}
					mFinish = true;
				}
			}
		});
	}
	
}