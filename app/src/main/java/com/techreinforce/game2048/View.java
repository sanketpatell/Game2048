package com.techreinforce.game2048;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import java.util.ArrayList;
/*This is view class for the activity , in this class we create the view programmatically*/
public class View extends android.view.View
{
    //Internal Constants
    static final int BASE_ANIMATION_TIME = 100000000;
    private static final String TAG = View.class.getSimpleName();
    private static final float MERGING_ACCELERATION = (float) -0.5;
    private static final float INITIAL_VELOCITY = (1 - MERGING_ACCELERATION) / 4;
    public final int numCellTypes = 12;
    private final BitmapDrawable[] bitmapCell = new BitmapDrawable[numCellTypes];
    public final Game game;
    //Internal variables
    private final Paint paint = new Paint();
    public boolean hasSaveState = false;
    public boolean continueButtonEnabled = false;
    public int startingX;
    public int startingY;
    public int endingX;
    public int endingY;
    //Icons
    public int sYIcons;
    public int sXNewGame;
    public int sXUndo;
   // load previous saved board
    public int iconSize;
    //Misc
    boolean refreshLastTime = true;
    //Timing
    private long lastFPSTime = System.nanoTime();
    //Text
    private float titleTextSize;
    private float bodyTextSize;
    private float headerTextSize;
    private float gameOverTextSize;
    //Layout variables
    private int cellSize = 0;
    private float textSize = 0;
    private float cellTextSize = 0;
    private int gridWidth = 0;
    private int textPaddingSize;
    private int iconPaddingSize;
    //Assets
    private Drawable backgroundRectangle;
    private Drawable lightUpRectangle;
    private Drawable fadeRectangle;
    private Bitmap background = null;
    private BitmapDrawable loseGameOverlay;
    private BitmapDrawable winGameContinueOverlay;
    private BitmapDrawable winGameFinalOverlay;
    //Text variables
    private int sYAll;
    private int titleStartYAll;
    private int bodyStartYAll;
    private int eYAll;
    private int titleWidthHighScore;
    private int titleWidthScore;

    public MainActivity mActivity;
    public Context mContext;

    public View(Context context, MainActivity activity)
    {
        super(context);

        mActivity = activity;
        mContext = context;

        //Loading resources
        game = new Game(context, this);
        try
        {
            //Getting assets
            backgroundRectangle = getDrawable(R.drawable.background_rectangle);
            lightUpRectangle = getDrawable(R.drawable.light_up_rectangle);
            fadeRectangle = getDrawable(R.drawable.fade_rectangle);
            this.setBackgroundColor(0xFFFAF8EF);

            Typeface font = Typeface.createFromAsset(getResources().getAssets(), "ClearSans-Bold.ttf");

            paint.setTypeface(font);
            paint.setAntiAlias(true);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error getting assets?", e);
        }
        setOnTouchListener(new InputListener(this));
        game.newGame();
    }

    public View(Context context)
    {
        super(context);
        mContext = context;

        //Loading resources
        game = new Game(context, this);
        try
        {
            //Getting assets
            backgroundRectangle = getDrawable(R.drawable.background_rectangle);
            lightUpRectangle = getDrawable(R.drawable.light_up_rectangle);
            fadeRectangle = getDrawable(R.drawable.fade_rectangle);
            this.setBackgroundColor(0xFFFAF8EF);

            Typeface font = Typeface.createFromAsset(getResources().getAssets(), "ClearSans-Bold.ttf");

            paint.setTypeface(font);
            paint.setAntiAlias(true);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error getting assets?", e);
        }
        setOnTouchListener(new InputListener(this));
        game.newGame();
    }

    private static int log2(int n)
    {
        if (n <= 0) throw new IllegalArgumentException();
        return 31 - Integer.numberOfLeadingZeros(n);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        //Reset the transparency of the screen
        canvas.drawBitmap(background, 0, 0, paint);
        drawScoreText(canvas);
        drawCells(canvas);

        if (!game.canContinue())
            drawEndlessText(canvas);

        // checking game over here
        if (!game.isActive())
        {
            drawEndGameState(canvas);

            if(!game.aGrid.isAnimationActive())
                drawGameOverButtons(canvas);
        }

        //Refresh the screen if there is still an animation running
        if (game.aGrid.isAnimationActive())
        {
            invalidate(startingX, startingY, endingX, endingY);
            tick();
            //Refresh one last time on game end.
        }
        else if (!game.isActive() && refreshLastTime)
        {
            invalidate();
            refreshLastTime = false;
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldW, int oldH)
    {
        super.onSizeChanged(width, height, oldW, oldH);
        getLayout(width, height);
        createBitmapCells();
        createBackgroundBitmap(width, height);
        createOverlays();
    }

    private Drawable getDrawable(int resId)
    {
        return getResources().getDrawable(resId);
    }

    private void drawDrawable(Canvas canvas, Drawable draw, int startingX, int startingY, int endingX, int endingY)
    {
        draw.setBounds(startingX, startingY, endingX, endingY);
        draw.draw(canvas);
    }

    private void drawCellText(Canvas canvas, int value)
    {
        int textShiftY = centerText();
        if (value >= 8)
            paint.setColor(getResources().getColor(R.color.text_white));
        else
            paint.setColor(getResources().getColor(R.color.text_black));

        canvas.drawText("" + value, cellSize / 2, cellSize / 2 - textShiftY, paint);
    }

    /*This method used for the updating the score text*/
    private void drawScoreText(Canvas canvas)
    {
        //Drawing the score text: Ver 2
        paint.setTextSize(bodyTextSize);
        paint.setTextAlign(Paint.Align.CENTER);

        int bodyWidthHighScore = (int) (paint.measureText("" + game.highScore));
        int bodyWidthScore = (int) (paint.measureText("" + game.score));

        int textWidthHighScore = Math.max(titleWidthHighScore, bodyWidthHighScore) + textPaddingSize * 2;
        int textWidthScore = Math.max(titleWidthScore, bodyWidthScore) + textPaddingSize * 2;

        int textMiddleHighScore = textWidthHighScore / 2;
        int textMiddleScore = textWidthScore / 2;

        int eXHighScore = endingX;
        int sXHighScore = eXHighScore - textWidthHighScore;

        int eXScore = sXHighScore - textPaddingSize;
        int sXScore = eXScore - textWidthScore;

        //Outputting high-scores box
        backgroundRectangle.setBounds(sXHighScore, sYAll, eXHighScore, eYAll);
        backgroundRectangle.draw(canvas);
        paint.setTextSize(titleTextSize);
        paint.setColor(getResources().getColor(R.color.text_brown));
        canvas.drawText(getResources().getString(R.string.high_score), sXHighScore + textMiddleHighScore, titleStartYAll, paint);
        paint.setTextSize(bodyTextSize);
        paint.setColor(getResources().getColor(R.color.text_white));
        canvas.drawText(String.valueOf(game.highScore), sXHighScore + textMiddleHighScore, bodyStartYAll, paint);

        //Outputting scores box
        backgroundRectangle.setBounds(sXScore, sYAll, eXScore, eYAll);
        backgroundRectangle.draw(canvas);
        paint.setTextSize(titleTextSize);
        paint.setColor(getResources().getColor(R.color.text_brown));
        canvas.drawText(getResources().getString(R.string.score), sXScore + textMiddleScore, titleStartYAll, paint);
        paint.setTextSize(bodyTextSize);
        paint.setColor(getResources().getColor(R.color.text_white));
        canvas.drawText(String.valueOf(game.score), sXScore + textMiddleScore, bodyStartYAll, paint);
    }

    /*This method used for the draw thw reset /  new gae button */
    private void drawNewGameButton(Canvas canvas, boolean lightUp)
    {
        if (lightUp)
            drawDrawable(canvas, lightUpRectangle, sXNewGame, sYIcons,
                    sXNewGame + iconSize,
                    sYIcons + iconSize);
        else
            drawDrawable(canvas, backgroundRectangle, sXNewGame, sYIcons,
                    sXNewGame + iconSize,
                    sYIcons + iconSize);

        drawDrawable(canvas, getDrawable(R.drawable.ic_action_refresh),
                sXNewGame + iconPaddingSize,
                sYIcons + iconPaddingSize,
                sXNewGame + iconSize - iconPaddingSize,
                sYIcons + iconSize - iconPaddingSize);
    }

  /*This method used for the undo the moved steps */
    private void drawUndoButton(Canvas canvas, boolean lightUp)
    {
        if (lightUp)
            drawDrawable(canvas, lightUpRectangle, sXUndo, sYIcons,
                    sXUndo + iconSize,
                    sYIcons + iconSize);
        else
        drawDrawable(canvas, backgroundRectangle, sXUndo, sYIcons,
                sXUndo + iconSize,
                sYIcons + iconSize);

        drawDrawable(canvas, getDrawable(R.drawable.ic_action_undo),
                sXUndo + iconPaddingSize,
                sYIcons + iconPaddingSize,
                sXUndo + iconSize - iconPaddingSize,
                sYIcons + iconSize - iconPaddingSize);
    }
  /*This method used for the draw the hader view */
    private void drawHeader(Canvas canvas)
    {
        paint.setTextSize(headerTextSize);
        paint.setColor(getResources().getColor(R.color.text_black));
        paint.setTextAlign(Paint.Align.LEFT);
        int textShiftY = centerText() * 2;
        int headerStartY = sYAll - textShiftY;
        canvas.drawText(getResources().getString(R.string.header), startingX, headerStartY, paint);
    }

    private void drawBackground(Canvas canvas)
    {
        drawDrawable(canvas, backgroundRectangle, startingX, startingY, endingX, endingY);
    }

    //Renders the set of 16 background squares.
    private void drawBackgroundGrid(Canvas canvas)
    {
        final int ROWS = 4;

        Drawable backgroundCell = getDrawable(R.drawable.cell_rectangle);
        // Outputting the game grid
        for (int xx = 0; xx < ROWS; xx++)
            for (int yy = 0; yy < ROWS; yy++)
            {
                int sX = startingX + gridWidth + (cellSize + gridWidth) * xx;
                int eX = sX + cellSize;
                int sY = startingY + gridWidth + (cellSize + gridWidth) * yy;
                int eY = sY + cellSize;

                drawDrawable(canvas, backgroundCell, sX, sY, eX, eY);
            }
    }
  /*This method used for the draw the cells */
    private void drawCells(Canvas canvas)
    {
        final int ROWS = 4;

        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        // Outputting the individual cells
        for (int xx = 0; xx < ROWS; xx++) {
            for (int yy = 0; yy < ROWS; yy++) {
                int sX = startingX + gridWidth + (cellSize + gridWidth) * xx;
                int eX = sX + cellSize;
                int sY = startingY + gridWidth + (cellSize + gridWidth) * yy;
                int eY = sY + cellSize;

                Tile currentTile = game.grid.getCellContent(xx, yy);
                if (currentTile != null) {
                    //Get and represent the value of the tile
                    int value = currentTile.getValue();
                    int index = log2(value);

                    //Check for any active animations
                    ArrayList<AnimationCell> aArray = game.aGrid.getAnimationCell(xx, yy);
                    boolean animated = false;
                    for (int i = aArray.size() - 1; i >= 0; i--)
                    {
                        AnimationCell aCell = aArray.get(i);
                        //If this animation is not active, skip it
                        if (aCell.getAnimationType() == Game.SPAWN_ANIMATION)
                            animated = true;
                        if (!aCell.isActive())
                            continue;

                        if (aCell.getAnimationType() == Game.SPAWN_ANIMATION)   // Spawning animation
                        {
                            double percentDone = aCell.getPercentageDone();
                            float textScaleSize = (float) (percentDone);
                            paint.setTextSize(textSize * textScaleSize);

                            float cellScaleSize = cellSize / 2 * (1 - textScaleSize);
                            bitmapCell[index].setBounds((int) (sX + cellScaleSize), (int) (sY + cellScaleSize), (int) (eX - cellScaleSize), (int) (eY - cellScaleSize));
                            bitmapCell[index].draw(canvas);
                        }
                        else if (aCell.getAnimationType() == Game.MERGE_ANIMATION)  // Merging Animation
                        {
                            double percentDone = aCell.getPercentageDone();
                            float textScaleSize = (float) (1 + INITIAL_VELOCITY * percentDone
                                    + MERGING_ACCELERATION * percentDone * percentDone / 2);
                            paint.setTextSize(textSize * textScaleSize);

                            float cellScaleSize = cellSize / 2 * (1 - textScaleSize);
                            bitmapCell[index].setBounds((int) (sX + cellScaleSize), (int) (sY + cellScaleSize), (int) (eX - cellScaleSize), (int) (eY - cellScaleSize));
                            bitmapCell[index].draw(canvas);
                        }
                        else if (aCell.getAnimationType() == Game.MOVE_ANIMATION)   // Moving animation
                        {
                            double percentDone = aCell.getPercentageDone();
                            int tempIndex = index;
                            if (aArray.size() >= 2)
                                tempIndex = tempIndex - 1;

                            int previousX = aCell.extras[0];
                            int previousY = aCell.extras[1];
                            int currentX = currentTile.getX();
                            int currentY = currentTile.getY();
                            int dX = (int) ((currentX - previousX) * (cellSize + gridWidth) * (percentDone - 1) * 1.0);
                            int dY = (int) ((currentY - previousY) * (cellSize + gridWidth) * (percentDone - 1) * 1.0);
                            bitmapCell[tempIndex].setBounds(sX + dX, sY + dY, eX + dX, eY + dY);
                            bitmapCell[tempIndex].draw(canvas);
                        }
                        animated = true;
                    }

                    //No active animations? Just draw the cell
                    if (!animated)
                    {
                        bitmapCell[index].setBounds(sX, sY, eX, eY);
                        bitmapCell[index].draw(canvas);
                    }
                }
            }
        }
    }
  /*This method used for the end view*/
    private void drawEndGameState(Canvas canvas)
    {
        double alphaChange = 1;
        continueButtonEnabled = false;
        for (AnimationCell animation : game.aGrid.globalAnimation)
            if (animation.getAnimationType() == Game.FADE_GLOBAL_ANIMATION)
                alphaChange = animation.getPercentageDone();

        BitmapDrawable displayOverlay = null;
        if (game.gameWon())
        {
            if (game.canContinue())
            {
                continueButtonEnabled = true;
                displayOverlay = winGameContinueOverlay;
            }
            else
                displayOverlay = winGameFinalOverlay;
        }
        else if (game.gameLost())
            displayOverlay = loseGameOverlay;

        if (displayOverlay != null)
        {
            displayOverlay.setBounds(startingX, startingY, endingX, endingY);
            displayOverlay.setAlpha((int) (255 * alphaChange));
            displayOverlay.draw(canvas);
        }
    }
  /*This method used for the show the no more move */
    private void drawEndlessText(Canvas canvas)
    {
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(bodyTextSize);
        paint.setColor(getResources().getColor(R.color.text_black));
        canvas.drawText(getResources().getString(R.string.endless), startingX, sYIcons - centerText() * 2, paint);
    }

    private void drawGameOverButtons(Canvas canvas)
    {
        drawNewGameButton(canvas, true);
        drawUndoButton(canvas, true);
    }
  /*This method used for the show the end game view */
    private void createEndGameStates(Canvas canvas, boolean win, boolean showButton)
    {
        int width = endingX - startingX;
        int length = endingY - startingY;
        int middleX = width / 2;
        int middleY = length / 2;
        if (win)
        {
            lightUpRectangle.setAlpha(127);
            drawDrawable(canvas, lightUpRectangle, 0, 0, width, length);
            lightUpRectangle.setAlpha(255);
            paint.setColor(getResources().getColor(R.color.text_white));
            paint.setAlpha(255);
            paint.setTextSize(gameOverTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            int textBottom = middleY - centerText();
            canvas.drawText(getResources().getString(R.string.you_win), middleX, textBottom, paint);
            paint.setTextSize(bodyTextSize);
            String text = showButton ? getResources().getString(R.string.go_on) :
                    getResources().getString(R.string.for_now);
            canvas.drawText(text, middleX, textBottom + textPaddingSize * 2 - centerText() * 2, paint);
        }
        else
        {
            fadeRectangle.setAlpha(127);
            drawDrawable(canvas, fadeRectangle, 0, 0, width, length);
            fadeRectangle.setAlpha(255);
            paint.setColor(getResources().getColor(R.color.text_black));
            paint.setAlpha(255);
            paint.setTextSize(gameOverTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(getResources().getString(R.string.game_over), middleX, middleY - centerText(), paint);
        }
    }
  /*This method used for the change the view of the cell */
    private void createBackgroundBitmap(int width, int height)
    {
        background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        drawHeader(canvas);
        drawNewGameButton(canvas, false);
        drawUndoButton(canvas, false);

        drawBackground(canvas);
        drawBackgroundGrid(canvas);
    }
  /*This method used for the create the cell */
    private void createBitmapCells()
    {
        Resources resources = getResources();
        int[] cellRectangleIds = getCellRectangleIds();
        paint.setTextAlign(Paint.Align.CENTER);
        for (int xx = 1; xx < bitmapCell.length; xx++)
        {
            int value = (int) Math.pow(2, xx);
            paint.setTextSize(cellTextSize);
            float tempTextSize = cellTextSize * cellSize * 0.9f / Math.max(cellSize * 0.9f, paint.measureText(String.valueOf(value)));
            paint.setTextSize(tempTextSize);
            Bitmap bitmap = Bitmap.createBitmap(cellSize, cellSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawDrawable(canvas, getDrawable(cellRectangleIds[xx]), 0, 0, cellSize, cellSize);
            drawCellText(canvas, value);
            bitmapCell[xx] = new BitmapDrawable(resources, bitmap);
        }
    }
  /*This method used for the get the cell resources */
    private int[] getCellRectangleIds()
    {
        int[] cellRectangleIds = new int[numCellTypes];
        cellRectangleIds[0] = R.drawable.cell_rectangle;
        cellRectangleIds[1] = R.drawable.cell_2;
        cellRectangleIds[2] = R.drawable.cell_4;
        cellRectangleIds[3] = R.drawable.cell_8;
        cellRectangleIds[4] = R.drawable.cell_16;
        cellRectangleIds[5] = R.drawable.cell_32;
        cellRectangleIds[6] = R.drawable.cell_64;
        cellRectangleIds[7] = R.drawable.cell_128;
        cellRectangleIds[8] = R.drawable.cell_256;
        cellRectangleIds[9] = R.drawable.cell_512;
        cellRectangleIds[10] = R.drawable.cell_1024;
        cellRectangleIds[11] = R.drawable.cell_2048;

        return cellRectangleIds;
    }

    private void createOverlays()
    {
        Resources resources = getResources();
        //Initialize overlays
        Bitmap bitmap = Bitmap.createBitmap(endingX - startingX, endingY - startingY, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        createEndGameStates(canvas, true, true);
        winGameContinueOverlay = new BitmapDrawable(resources, bitmap);
        bitmap = Bitmap.createBitmap(endingX - startingX, endingY - startingY, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        createEndGameStates(canvas, true, false);
        winGameFinalOverlay = new BitmapDrawable(resources, bitmap);
        bitmap = Bitmap.createBitmap(endingX - startingX, endingY - startingY, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        createEndGameStates(canvas, false, false);
        loseGameOverlay = new BitmapDrawable(resources, bitmap);
    }
  /*This method used for the make the move perfect */
    private void tick()
    {
        long currentTime = System.nanoTime();
        game.aGrid.tickAll(currentTime - lastFPSTime);
        lastFPSTime = currentTime;
    }

    public void resyncTime()
    {
        lastFPSTime = System.nanoTime();
    }
  /*This method used for the get the layout  */
    private void getLayout(int width, int height)
    {
        final int ROWS = 4;

        cellSize = Math.min(width / (ROWS + 1), height / (ROWS + 3));
        gridWidth = cellSize / (ROWS + 3);  // (ROWS + 3) was 7
        int screenMiddleX = width / 2;
        int screenMiddleY = height / 2;
        int boardMiddleY = screenMiddleY + cellSize / 2;
        iconSize = cellSize / 2;

        //Grid Dimensions
        double halfNumSquaresX = ROWS / 2d;
        double halfNumSquaresY = ROWS / 2d;
        startingX = (int) (screenMiddleX - (cellSize + gridWidth) * halfNumSquaresX - gridWidth / 2);
        endingX = (int) (screenMiddleX + (cellSize + gridWidth) * halfNumSquaresX + gridWidth / 2);
        startingY = (int) (boardMiddleY - (cellSize + gridWidth) * halfNumSquaresY - gridWidth / 2);
        endingY = (int) (boardMiddleY + (cellSize + gridWidth) * halfNumSquaresY + gridWidth / 2);

        float widthWithPadding = endingX - startingX;

        // Text Dimensions
        paint.setTextSize(cellSize);
        textSize = cellSize * cellSize / Math.max(cellSize, paint.measureText("0000"));

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(1000);

        gameOverTextSize = Math.min(
            Math.min(
                1000f * ((widthWithPadding - gridWidth * 2) / (paint.measureText(getResources().getString(R.string.game_over)))),
                textSize * 2
            ),
            1000f * ((widthWithPadding - gridWidth * 2) / (paint.measureText(getResources().getString(R.string.you_win))))
        );

        paint.setTextSize(cellSize);
        cellTextSize = textSize;
        titleTextSize = textSize / 3;
        bodyTextSize = (int) (textSize / 1.5);
        headerTextSize = textSize * 2;
        textPaddingSize = (int) (textSize / 3);
        iconPaddingSize = (int) (textSize / 5);

        paint.setTextSize(titleTextSize);

        int textShiftYAll = centerText();
        //static variables
        sYAll = (int) (startingY - cellSize * 1.5);
        titleStartYAll = (int) (sYAll + textPaddingSize + titleTextSize / 2 - textShiftYAll);
        bodyStartYAll = (int) (titleStartYAll + textPaddingSize + titleTextSize / 2 + bodyTextSize / 2);

        titleWidthHighScore = (int) (paint.measureText(getResources().getString(R.string.high_score)));
        titleWidthScore = (int) (paint.measureText(getResources().getString(R.string.score)));
        paint.setTextSize(bodyTextSize);
        textShiftYAll = centerText();
        eYAll = (int) (bodyStartYAll + textShiftYAll + bodyTextSize / 2 + textPaddingSize);

        sYIcons = (startingY + eYAll) / 2 - iconSize / 2;
        sXNewGame = (endingX - iconSize);
        sXUndo = sXNewGame - iconSize - iconPaddingSize;
        resyncTime();
    }
  /*This method used for the create the text view*/
    private int centerText()
    {
        return (int) ((paint.descent() + paint.ascent()) / 2);
    }
}