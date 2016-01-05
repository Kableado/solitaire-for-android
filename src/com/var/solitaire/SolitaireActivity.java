/*
  Copyright 2008 Google Inc.
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  Modified by Curtis Gedak 2015
  Modified by Valeriano A.R 2016
*/
package com.var.solitaire;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

// Base activity class.
public class SolitaireActivity extends Activity {
  private static final int MENU_NEW_GAME  = 1;
  private static final int MENU_RESTART   = 2;
  private static final int MENU_OPTIONS   = 3;
  private static final int MENU_SAVE_QUIT = 4;
  private static final int MENU_QUIT      = 5;
  private static final int MENU_STATS     = 10;
  private static final int MENU_HELP      = 11;

  // View extracted from main.xml.
  private View mMainView;
  private SolitaireView mSolitaireView;
  private SharedPreferences mSettings;
  
  // Shared preferences are where the various user settings are stored.
  public SharedPreferences GetSettings() { return mSettings; }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);

    // If the user has never accepted the EULA show it again.
    mSettings = getSharedPreferences("SolitairePreferences", 0);
    setContentView(com.var.solitaire.R.layout.main);
    mMainView = findViewById(com.var.solitaire.R.id.main_view);
    mSolitaireView = (SolitaireView) findViewById(com.var.solitaire.R.id.solitaire);
    mSolitaireView.SetTextView((TextView) findViewById(com.var.solitaire.R.id.text));

    //StartSolitaire(savedInstanceState);
  }

  // Entry point for starting the game.
  //public void StartSolitaire(Bundle savedInstanceState) {
  @Override
  public void onStart() {
    super.onStart();
    mSolitaireView.onStart();

    if (mSettings.getBoolean("SolitaireSaveValid", false)) {
      SharedPreferences.Editor editor = GetSettings().edit();
      editor.putBoolean("SolitaireSaveValid", false);
      editor.commit();
      // If save is corrupt, just start a new game.
      if (mSolitaireView.LoadSave()) {
        HelpSplashScreen();
        return;
      }
    }

    mSolitaireView.InitGame();
    HelpSplashScreen();
  }

  // Force show the help if this is the first time played. Sadly no one reads
  // it anyways.
  private void HelpSplashScreen() {
    if (!mSettings.getBoolean("PlayedBefore", false)) {
      mSolitaireView.DisplayHelp();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(0, MENU_NEW_GAME, 0, com.var.solitaire.R.string.menu_newgame);
    menu.add(0, MENU_RESTART, 0, com.var.solitaire.R.string.menu_restart);
    menu.add(0, MENU_OPTIONS, 0, com.var.solitaire.R.string.menu_options);
    menu.add(0, MENU_STATS, 0, com.var.solitaire.R.string.menu_stats);
    menu.add(0, MENU_HELP, 0, com.var.solitaire.R.string.menu_help);
    menu.add(0, MENU_QUIT, 0, com.var.solitaire.R.string.menu_quit);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case MENU_NEW_GAME:
        mSolitaireView.InitGame();
        break;
      case MENU_RESTART:
        mSolitaireView.RestartGame();
        break;
      case MENU_STATS:
        DisplayStats();
        break;
      case MENU_OPTIONS:
        DisplayOptions();
        break;
      case MENU_HELP:
        mSolitaireView.DisplayHelp();
        break;
      case MENU_QUIT:
        finish();
        break;
    }

    return false;
  }

  @Override
  protected void onPause() {
    super.onPause();
    mSolitaireView.onPause();
  }

  @Override
  protected void onStop(){
    super.onStop();
    mSolitaireView.SaveGame();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSolitaireView.onResume();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  public void DisplayOptions() {
    mSolitaireView.SetTimePassing(false);
    new Options(this, mSolitaireView.GetDrawMaster());
  }

  public void DisplayStats() {
    mSolitaireView.SetTimePassing(false);
    new Stats(this, mSolitaireView);
  }

  public void CancelOptions() {
    setContentView(mMainView);
    mSolitaireView.requestFocus();
    mSolitaireView.SetTimePassing(true);
  }

  public void NewOptions() {
    setContentView(mMainView);
    mSolitaireView.InitGame();
  }

  // This is called for option changes that require a refresh, but not a new game
  public void RefreshOptions() {
    setContentView(mMainView);
    mSolitaireView.RefreshOptions();
  }
}
