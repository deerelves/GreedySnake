package cn.joy.game.greedysnack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * **********************
 * Author: yu
 * Date:   2015/12/19
 * Time:   11:18
 * **********************
 */
public class MainActivity extends Activity {

	private static final int LEVEL_MAX = 10;
	private static final int LEVEL_MIN = 1;

	private int level = 1;

	@Bind(R.id.main_level_text)
	protected TextView txtLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		txtLevel.setText(String.valueOf(level));
	}

	@OnClick(R.id.main_level_left)
	protected void downLevel() {
		level = --level < LEVEL_MIN ? LEVEL_MIN : level;
		txtLevel.setText(String.valueOf(level));
	}

	@OnClick(R.id.main_level_right)
	protected void upLevel() {
		level = ++level > LEVEL_MAX ? LEVEL_MAX : level;
		txtLevel.setText(String.valueOf(level));
	}

	@OnClick(R.id.main_start_btn)
	protected void play() {
		startActivity(new Intent(MainActivity.this, GameActivity.class).putExtra(GameActivity.EXTRA_LEVEL, level));
	}
}
