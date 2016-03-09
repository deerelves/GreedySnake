package cn.joy.game.greedysnack;

import android.animation.Animator;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * **********************
 * Author: yu
 * Date:   2015/12/19
 * Time:   11:54
 * **********************
 */
public class GameActivity extends Activity implements GameDrawView.CallBack {

	public static final String EXTRA_LEVEL = "extra_level";

	@Bind(R.id.game_view)
	protected GameDrawView gameView;

	@Bind(R.id.game_start)
	protected ImageView imgStart;

	@Bind(R.id.game_dialog)
	protected View dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		ButterKnife.bind(this);
		gameView.setLevel(getIntent().getIntExtra(EXTRA_LEVEL, 1));
		gameView.setCallBack(this);
		dialog.setOnClickListener((v) -> {
		});
	}

	@OnClick(R.id.game_left)
	protected void turnLeft() {
		gameView.setToward(GameDrawView.Toward.Left);
	}

	@OnClick(R.id.game_right)
	protected void turnRight() {
		gameView.setToward(GameDrawView.Toward.Right);
	}

	@OnClick(R.id.game_up)
	protected void turnUp() {
		gameView.setToward(GameDrawView.Toward.Up);
	}

	@OnClick(R.id.game_down)
	protected void turnDown() {
		gameView.setToward(GameDrawView.Toward.Down);
	}

	@OnClick(R.id.game_start)
	protected void toggle() {
		toggle(!gameView.isRunning());
	}

	@OnClick(R.id.game_restart)
	protected void reset() {
		new AlertDialog.Builder(GameActivity.this).setTitle(R.string.text_restart).setPositiveButton(R.string.yes, (d, w) -> {
			gameView.reset();
			toggle(false);
		}).setNegativeButton(R.string.no, null).create().show();
	}

	@OnClick(R.id.game_dialog_restart)
	protected void dialogReset() {
		dialog.animate().alpha(0f).setDuration(500).setListener(new SimpleAnimListener() {
			@Override
			public void onAnimationEnd(Animator animation) {
				dialog.setVisibility(View.GONE);
			}
		});
		gameView.reset();
		toggle(false);
	}

	protected void dialogExit() {
		finish();
	}

	private void toggle(boolean start) {
		imgStart.setImageResource(start ? R.drawable.selector_game_pause : R.drawable.selector_game_start);
		gameView.toggle(start);
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(GameActivity.this).setTitle(R.string.text_exit)
				.setPositiveButton(R.string.yes, (dialog, which) -> finish())
				.setNegativeButton(R.string.no, null)
				.create()
				.show();
	}

	@Override
	public void onGameOver() {
		runOnUiThread(() -> {
			dialog.setVisibility(View.VISIBLE);
			dialog.animate().alpha(1f).setDuration(500).start();
		});
	}

	class SimpleAnimListener implements Animator.AnimatorListener {

		@Override
		public void onAnimationStart(Animator animation) {

		}

		@Override
		public void onAnimationEnd(Animator animation) {

		}

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	}
}
