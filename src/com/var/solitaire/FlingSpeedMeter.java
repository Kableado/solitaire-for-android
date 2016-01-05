package com.var.solitaire;

class FlingSpeedMeter{
  private static final int SPEED_COUNT = 4;
  private static final float SPEED_THRESHOLD = 10*10;
  private float[] mSpeed;
  private int mIdx;

  public
  FlingSpeedMeter() {
    mSpeed = new float[SPEED_COUNT];
    Reset();
  }
  public void Reset() {
    mIdx = 0;
    for (int i = 0; i < SPEED_COUNT; i++) {
      mSpeed[i] = 0;
    }
  }
  public void AddSpeed(float dx, float dy) {
    mSpeed[mIdx] = dx*dx + dy*dy;
    mIdx = (mIdx + 1) % SPEED_COUNT;
  }
  public boolean IsFast() {
    for (int i = 0; i < SPEED_COUNT; i++) {
      if (mSpeed[i] > SPEED_THRESHOLD) {
        return true;
      }
    }
    return false;
  }
}
