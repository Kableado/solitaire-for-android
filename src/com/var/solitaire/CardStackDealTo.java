package com.var.solitaire;

import android.graphics.Canvas;

// Straight up default
class CardStackDealTo extends CardStack{
  private int mShowing;
  public
  CardStackDealTo() {
    super();
    mShowing = 1;
  }

  @Override protected
  void SetCardPosition(int idx){
    int idxShowing = (mCardCount - mShowing);
    if(idx < idxShowing){
      mCard[idx].SetPosition(mX, mY);
      return;
    }
    int pos;
    if(mCardCount < mShowing){
      pos = idx;
    }else{
      pos = idx - idxShowing;
    }
    double x = mX + ((Card.WIDTH * 0.75) * (1 - Math.cos((pos / (double)(mShowing - 1)) * (Math.PI / 2))));
    mCard[idx].SetPosition((int)x, mY);
  }


  @Override
  public void SetShowing(int showing) { mShowing = showing; }

  @Override
  public void AddCard(Card card) {
    super.AddCard(card);
    SetPosition(mX, mY);
  }

  @Override
  public boolean UnhideTopCard() {
    SetPosition(mX, mY);
    return false;
  }

  @Override
  public Card PopCard() {
    Card ret = super.PopCard();
    SetPosition(mX, mY);
    return ret;
  }

  @Override
  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mCardCount == 0) {
      drawMaster.DrawEmptyAnchor(canvas, mX, mY, mDone);
    } else {
      for (int i = mCardCount - mShowing; i < mCardCount; i++) {
        if (i >= 0) {
          drawMaster.DrawCard(canvas, mCard[i]);
        }
      }
    }
  }
}
