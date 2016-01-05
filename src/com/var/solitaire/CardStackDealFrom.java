package com.var.solitaire;

import android.graphics.Canvas;

// Anchor where cards to deal come from
class CardStackDealFrom extends CardStack{

  @Override
  public Card GrabCard(float x, float y) { return null; }

  @Override
  public boolean TapCard(float x, float y) {
    if (IsOverCard(x, y)) {
      mRules.EventAlert(Rules.EVENT_DEAL, this);
      return true;
    }
    return false;
  }

  @Override
  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mCardCount == 0) {
      drawMaster.DrawEmptyAnchor(canvas, mX, mY, mDone);
    } else {
      drawMaster.DrawHiddenCard(canvas, mCard[mCardCount-1]);
    }
  }
}
