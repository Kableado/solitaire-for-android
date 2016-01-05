package com.var.solitaire;

class ViewRefreshHandler implements Runnable {
  public static final int NO_REFRESH = 1;
  public static final int SINGLE_REFRESH = 2;
  public static final int LOCK_REFRESH = 3;

  private static final int FPS = 30;

  private boolean mRun;
  private int mRefresh;
  private SolitaireView mView;

  public
  ViewRefreshHandler(SolitaireView solitaireView) {
    mView = solitaireView;
    mRun = true;
    mRefresh = NO_REFRESH;
  }

  public void SetRefresh(int refresh) {
    synchronized (this) {
      mRefresh = refresh;
    }
  }

  public void SingleRefresh() {
    synchronized (this) {
      if (mRefresh == NO_REFRESH) {
        mRefresh = SINGLE_REFRESH;
      }
    }
  }

  public void SetRunning(boolean run) {
    mRun = run;
  }

  public void run() {
    while (mRun) {
      try {
        Thread.sleep(1000 / FPS);
      } catch (InterruptedException e) {
      }
      mView.UpdateTime();
      if (mRefresh != NO_REFRESH) {
        mView.postInvalidate();
        if (mRefresh == SINGLE_REFRESH) {
          SetRefresh(NO_REFRESH);
        }
      }
    }
  }
}
