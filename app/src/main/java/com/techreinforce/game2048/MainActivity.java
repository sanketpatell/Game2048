package com.techreinforce.game2048;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
/*<h1> MainActivity</h1>
* <P> This is the main activity class for the launching the application ,
* buy  using this class we can load the Game
</p>
* */
public class MainActivity extends AppCompatActivity {
  public static int mRewardDeletes = 2;

  public static int mRewardDeletingSelectionAmounts = 3;

  private static final String REWARD_DELETES = "reward chances";
  private static final String WIDTH = "width";
  private static final String HEIGHT = "height";
  private static final String SCORE = "score";
  private static final String HIGH_SCORE = "high score temp";
  private static final String UNDO_SCORE = "undo score";
  private static final String CAN_UNDO = "can undo";
  private static final String UNDO_GRID = "undo";
  private static final String GAME_STATE = "game state";
  private static final String UNDO_GAME_STATE = "undo game state";
  private static final String REWARD_DELETE_SELECTION = "reward delete selection amounts";

  private View view;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    FrameLayout frameLayout = findViewById(R.id.flMain);
    view = new View(this, this);

    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    view.hasSaveState = settings.getBoolean("save_state", false);

    if (savedInstanceState != null)
      if (savedInstanceState.getBoolean("hasState"))
        load();

    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    view.setLayoutParams(params);

    frameLayout.addView(view);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_MENU)
      return true;
    else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
      view.game.move(2);
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
      view.game.move(0);
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
      view.game.move(3);
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
      view.game.move(1);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putBoolean("hasState", true);
    save();
    super.onSaveInstanceState(savedInstanceState);
  }

  protected void onPause() {
    super.onPause();
    save();
  }

  protected void onResume() {
    super.onResume();
    load();
  }
/*This method use for save the game on pause  */
  private void save() {
    final int rows = 4;

    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = settings.edit();
    Tile[][] field = view.game.grid.field;
    Tile[][] undoField = view.game.grid.undoField;
    editor.putInt(WIDTH + rows, field.length);
    editor.putInt(HEIGHT + rows, field.length);

    for (int xx = 0; xx < field.length; xx++) {
      for (int yy = 0; yy < field[0].length; yy++) {
        if (field[xx][yy] != null)
          editor.putInt(rows + " " + xx + " " + yy, field[xx][yy].getValue());
        else
          editor.putInt(rows + " " + xx + " " + yy, 0);

        if (undoField[xx][yy] != null)
          editor.putInt(UNDO_GRID + rows + " " + xx + " " + yy, undoField[xx][yy].getValue());
        else
          editor.putInt(UNDO_GRID + rows + " " + xx + " " + yy, 0);
      }
    }

    // reward deletions:
    editor.putInt(REWARD_DELETES + rows, mRewardDeletes);
    editor.putInt(REWARD_DELETE_SELECTION + rows, mRewardDeletingSelectionAmounts);

    // game values:
    editor.putLong(SCORE + rows, view.game.score);
    editor.putLong(HIGH_SCORE + rows, view.game.highScore);
    editor.putLong(UNDO_SCORE + rows, view.game.lastScore);
    editor.putBoolean(CAN_UNDO + rows, view.game.canUndo);
    editor.putInt(GAME_STATE + rows, view.game.gameState);
    editor.putInt(UNDO_GAME_STATE + rows, view.game.lastGameState);
    editor.apply();
  }
/*This method use for load the saved game*/
  private void load() {
    final int rows = 4;


    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

    for (int xx = 0; xx < view.game.grid.field.length; xx++) {
      for (int yy = 0; yy < view.game.grid.field[0].length; yy++) {
        int value = settings.getInt(rows + " " + xx + " " + yy, -1);
        if (value > 0)
          view.game.grid.field[xx][yy] = new Tile(xx, yy, value);
        else if (value == 0)
          view.game.grid.field[xx][yy] = null;

        int undoValue = settings.getInt(UNDO_GRID + rows + " " + xx + " " + yy, -1);
        if (undoValue > 0)
          view.game.grid.undoField[xx][yy] = new Tile(xx, yy, undoValue);
        else if (value == 0)
          view.game.grid.undoField[xx][yy] = null;
      }
    }

    mRewardDeletes = settings.getInt(REWARD_DELETES + rows, 2);
    mRewardDeletingSelectionAmounts = settings.getInt(REWARD_DELETE_SELECTION + rows, 3);

    view.game.score = settings.getLong(SCORE + rows, view.game.score);
    view.game.highScore = settings.getLong(HIGH_SCORE + rows, view.game.highScore);
    view.game.lastScore = settings.getLong(UNDO_SCORE + rows, view.game.lastScore);
    view.game.canUndo = settings.getBoolean(CAN_UNDO + rows, view.game.canUndo);
    view.game.gameState = settings.getInt(GAME_STATE + rows, view.game.gameState);
    view.game.lastGameState = settings.getInt(UNDO_GAME_STATE + rows, view.game.lastGameState);
  }
}