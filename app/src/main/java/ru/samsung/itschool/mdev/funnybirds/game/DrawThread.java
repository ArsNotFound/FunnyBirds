package ru.samsung.itschool.mdev.funnybirds.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import ru.samsung.itschool.mdev.funnybirds.R;

public class DrawThread extends Thread {
    private volatile boolean running = true;

    private SurfaceHolder holder;

    private final Context context;

    private Sprite playerBirdSprite;

    private Sprite enemyBirdSprite;

    private int viewHeight;

    private int viewWidth;

    private int points = 0;

    private static final Paint pointsPaint = new Paint();

    static {
        pointsPaint.setAntiAlias(true);
        pointsPaint.setTextSize(55.0f);
        pointsPaint.setColor(Color.WHITE);
    }

    public DrawThread(Context context, SurfaceHolder holder) {
        this.holder = holder;
        this.context = context;

        initSprites();
    }

    private void initSprites() {
        Bitmap playerBirdBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player);
        int playerBirdW = playerBirdBitmap.getWidth() / 5;
        int playerBirdH = playerBirdBitmap.getHeight() / 3;
        Rect firstFrame = new Rect(0, 0, playerBirdW, playerBirdH);
        playerBirdSprite = new Sprite(10, 0, 0, 100, firstFrame, playerBirdBitmap);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                if (i == 2 && j == 3) {
                    continue;
                }
                playerBirdSprite.addFrame(new Rect(
                        j * playerBirdW,
                        i * playerBirdH,
                        j * playerBirdW + playerBirdW,
                        i * playerBirdW + playerBirdW
                ));
            }
        }

        Bitmap enemyBirdBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemy);
        int enemyBirdW = enemyBirdBitmap.getWidth() / 5;
        int enemyBirdH = enemyBirdBitmap.getHeight() / 3;
        firstFrame = new Rect(4 * enemyBirdW, 0, 5 * enemyBirdW, enemyBirdH);

        enemyBirdSprite = new Sprite(2000, 250, -300, 0, firstFrame, enemyBirdBitmap);

        for (int i = 0; i < 3; i++) {
            for (int j = 4; j >= 0; j--) {

                if (i == 0 && j == 4) {
                    continue;
                }

                if (i == 2 && j == 0) {
                    continue;
                }

                enemyBirdSprite.addFrame(new Rect(
                        j * enemyBirdW,
                        i * enemyBirdH,
                        j * enemyBirdW + enemyBirdW,
                        i * enemyBirdW + enemyBirdW
                ));
            }
        }
    }

    public void onSurfaceChange(SurfaceHolder holder, int viewHeight, int viewWidth) {
        this.holder = holder;
        this.viewHeight = viewHeight;
        this.viewWidth = viewWidth;
    }

    public boolean onTouch(MotionEvent event) {
        int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_DOWN) {

            if (event.getY() < playerBirdSprite.getBoundingBoxRect().top) {
                playerBirdSprite.setVy(-100);
                points--;
            } else if (event.getY() > (playerBirdSprite.getBoundingBoxRect().bottom)) {
                playerBirdSprite.setVy(100);
                points--;
            }
        }

        return true;
    }

    private void draw(Canvas canvas) {
        canvas.drawARGB(250, 127, 199, 255);
        playerBirdSprite.draw(canvas);
        enemyBirdSprite.draw(canvas);
        canvas.drawText(String.valueOf(points), viewWidth - 200, 70, pointsPaint);
    }

    private void update(long timeDelta) {
        playerBirdSprite.update(timeDelta);
        enemyBirdSprite.update(timeDelta);

        if (playerBirdSprite.getY() + playerBirdSprite.getFrameHeight() > viewHeight) {
            playerBirdSprite.setY(viewHeight - playerBirdSprite.getFrameHeight());
            playerBirdSprite.setVy(-playerBirdSprite.getVy());
            points--;
        } else if (playerBirdSprite.getY() < 0) {
            playerBirdSprite.setY(0);
            playerBirdSprite.setVy(-playerBirdSprite.getVy());
            points--;
        }

        if (enemyBirdSprite.getX() < -enemyBirdSprite.getFrameWidth()) {
            teleportEnemy();
            points += 10;
        }

        if (enemyBirdSprite.intersect(playerBirdSprite)) {
            teleportEnemy();
            points -= 40;
        }
    }

    private void teleportEnemy() {
        enemyBirdSprite.setX(viewWidth + Math.random() * 500);
        enemyBirdSprite.setY(Math.random() * (viewHeight - enemyBirdSprite.getFrameHeight()));
    }

    public void requestStop() {
        this.running = false;
    }

    public void run() {
        long prevFrameTime = System.currentTimeMillis();
        long timeDelta = 1;
        while (running) {
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                try {
                    update(timeDelta);
                    draw(canvas);
                    long frameTime = System.currentTimeMillis();
                    timeDelta = prevFrameTime - frameTime;
                    prevFrameTime = frameTime;
                } finally {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}

