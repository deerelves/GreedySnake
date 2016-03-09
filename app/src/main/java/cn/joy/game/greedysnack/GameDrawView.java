package cn.joy.game.greedysnack;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import rx.Observable;
import rx.schedulers.Schedulers;


/**
 * **********************
 * Author: yu
 * Date:   2015/12/19
 * Time:   12:00
 * **********************
 */
public class GameDrawView extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "GameDrawView";

	/**
	 * 方向
	 */
	public enum Toward {
		Left(-1, 0), Up(0, -1), Right(1, 0), Down(0, 1);

		int x, y;

		Toward(int x, int y) {
			this.x = x;
			this.y = y;
		}

		Point next(Point point) {
			point.x += x;
			point.y += y;
			return point;
		}

		/** 判断两个方向是否处在同一条直线上面 */
		boolean singleLine(Toward toward) {
			return toward.x == x || toward.y == y;
		}
	}

	private static final int INDEX_SIDE_COUNT = 30;

	private static final int SNACK_BODY_PADDING = 2;

	private static final Point[] SNACK_INITIAL = {new Point(14, 25), new Point(14, 26), new Point(14, 27), new Point(14, 28), new Point(14, 29)};

	private int indexSide;
	private int background = Color.parseColor("#aaaaaa");
	private Paint snackPaint = new Paint();
	private Paint snackHeaderPaint;
	private Paint sweetPaint;
	private Rect snackRect = new Rect();

	private Point sweet = new Point();

	/** snack身体位置列表 */
	private LinkedList<Point> snackBodys = new LinkedList<>(Arrays.asList(SNACK_INITIAL));

	private boolean isRunning = false;
	private boolean canOperate = false;

	private int level = 1;
	private int delay = 1000;
	private Toward toward = Toward.Up;
	private SnackRunning snackRunning = new SnackRunning();

	CallBack callBack;

	public GameDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		snackPaint.setColor(Color.WHITE);
		snackPaint.setAntiAlias(true);
		snackHeaderPaint = new Paint(snackPaint);
		snackHeaderPaint.setColor(Color.GRAY);
		sweetPaint = new Paint(snackPaint);
		sweetPaint.setColor(Color.YELLOW);
		getHolder().addCallback(this);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		if (width > 0) {
			indexSide = width / INDEX_SIDE_COUNT;
			snackRect.set(0, 0, indexSide - 2 * SNACK_BODY_PADDING, indexSide - 2 * SNACK_BODY_PADDING);
			setMeasuredDimension(width, width);
		}
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		reset();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		isRunning = false;
	}

	/**
	 * 重置
	 */
	public void reset() {
		canOperate = true;
		isRunning = false;
		snackBodys.clear();
		snackBodys.addAll(Arrays.asList(SNACK_INITIAL));
		toward = Toward.Up;
		resetSweet();
		snackRunning.start();
	}

	public void start() {
		isRunning = true;
		snackRunning.start();
	}

	public void pause() {
		isRunning = false;
	}

	public void toggle(boolean start) {
		if (start) {
			start();
		} else {
			pause();
		}
	}

	public void setToward(Toward toward) {
		if (!isRunning & canOperate) {
			start();
		}
		if (toward.singleLine(this.toward))
			return;
		this.toward = toward;
	}

	public void setLevel(int level) {
		this.level = level;
		delay = 1000 / this.level;
	}

	/**
	 * 重新生成一个sweet位置
	 */
	private void resetSweet() {
		sweet.set(new Random().nextInt(INDEX_SIDE_COUNT - 1), new Random().nextInt(INDEX_SIDE_COUNT - 1));
		if (snackBodys.contains(sweet)) {
			resetSweet();
		}
	}

	private void gameOver() {
		canOperate = false;
		isRunning = false;
		if (callBack != null) {
			callBack.onGameOver();
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * 绘制蛇身
	 */
	private void drawSnack(Canvas canvas) {
		Point header = snackBodys.get(0);
		snackRect.offsetTo(header.x * indexSide + SNACK_BODY_PADDING, header.y * indexSide + SNACK_BODY_PADDING);
		canvas.drawRect(snackRect, snackHeaderPaint);
		for (int i = 1; i < snackBodys.size(); i++) {
			Point body = snackBodys.get(i);
			snackRect.offsetTo(body.x * indexSide + SNACK_BODY_PADDING, body.y * indexSide + SNACK_BODY_PADDING);
			Log.w(TAG, snackRect.toString());
			canvas.drawRect(snackRect, snackPaint);
		}
	}

	/**
	 * 绘制糖果
	 */
	private void drawSweet(Canvas canvas) {
		snackRect.offsetTo(sweet.x * indexSide + SNACK_BODY_PADDING, sweet.y * indexSide + SNACK_BODY_PADDING);
		Log.w(TAG, snackRect.toString());
		canvas.drawRect(snackRect, sweetPaint);
	}

	private void clearDraw(Canvas canvas) {
		canvas.drawColor(background);
	}

	private void draw() {
		Canvas canvas = getHolder().lockCanvas();
		if (canvas == null)
			return;
		clearDraw(canvas);
		drawSweet(canvas);
		drawSnack(canvas);
		getHolder().unlockCanvasAndPost(canvas);
	}

	private class SnackRunning implements Runnable {
		@Override
		public void run() {
			if (isRunning) {
				Observable.<Boolean>create(sb -> {
					sb.onNext(move());
					sb.onCompleted();
				}).subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread()).subscribe(flag -> {
					draw();
					if (flag) {
						postDelayed(this, delay);
					} else {
						gameOver();
					}
				});
			} else {
				draw();
			}
		}

		boolean move() {
			Point header = snackBodys.get(0);
			Point newHeader = toward.next(new Point(header));
			snackBodys.addFirst(newHeader);
			if (!newHeader.equals(sweet)) {
				snackBodys.removeLast();
			} else {
				resetSweet();
			}
			return newHeader.x > 0 & newHeader.x < INDEX_SIDE_COUNT - 1 & newHeader.y > 0 & newHeader.y <= INDEX_SIDE_COUNT;
		}

		void start() {
			postDelayed(this, delay);
		}
	}

	public void setCallBack(CallBack callBack) {
		this.callBack = callBack;
	}

	public interface CallBack {
		void onGameOver();
	}
}
