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
*/ 
package com.var.solitaire;

import android.graphics.Canvas;


class CardStack{

  public static final int MAX_CARDS = 104;
  public static final int SEQ_SINK = 1;
  public static final int DEAL_FROM = 3;
  public static final int DEAL_TO = 4;
  public static final int GENERIC_ANCHOR = 8;

  private int mNumber;
  protected Rules mRules;
  protected float mX;
  protected float mY;
  protected Card[] mCard;
  protected int mCardCount;
  protected int mHiddenCount;
  protected float mLeftEdge;
  protected float mRightEdge;
  protected float mBottom;
  protected boolean mDone;

  //Variables for GenericAnchor
  protected int mSTARTSEQ;
  protected int mBUILDSEQ;
  protected int mMOVESEQ;
  protected int mBUILDSUIT;
  protected int mMOVESUIT;
  protected boolean mBUILDWRAP;
  protected boolean mMOVEWRAP;
  protected int mDROPOFF;
  protected int mPICKUP;
  protected int mDISPLAY; 
  protected int mHACK;
  
  // ==========================================================================
  // Create a CardAnchor
  // -------------------
  public static
  CardStack CreateAnchor(int type, int number, Rules rules) {
    CardStack ret = null;
    switch (type) {
      case SEQ_SINK:
        ret = new CardStackSuitSequence();
        break;
      case DEAL_FROM:
        ret = new CardStackDealFrom();
        break;
      case DEAL_TO:
        ret = new CardStackDealTo();
        break;
      case GENERIC_ANCHOR:
        ret = new GenericAnchor();
        break;
    }
    ret.SetRules(rules);
    ret.SetNumber(number);
    return ret;
  }

  public
  CardStack() {
    mX = 1;
    mY = 1;
    mCard = new Card[MAX_CARDS];
    mCardCount = 0;
    mHiddenCount = 0;
    mLeftEdge = -1;
    mRightEdge = -1;
    mBottom = -1;
    mNumber = -1;
    mDone = false;
  }

  // ==========================================================================
  // Getters and Setters
  // -------------------
  public Card[] GetCards() { return mCard; }
  public int GetCount() { return mCardCount; }
  public int GetHiddenCount() { return mHiddenCount; }
  public float GetLeftEdge() { return mLeftEdge; }
  public int GetNumber() { return mNumber; }
  public float GetRightEdge() { return mRightEdge; }
  public int GetVisibleCount() { return mCardCount - mHiddenCount; }
  public int GetMovableCount() { return mCardCount > 0 ? 1 : 0; }
  public float GetX() { return mX; }
  public float GetNewY() { return mY; }
  public boolean IsDone() { return mDone; }

  public void SetBottom(float edge) { mBottom = edge; }
  public void SetHiddenCount(int count) { mHiddenCount = count; }
  public void SetLeftEdge(float edge) { mLeftEdge = edge; }
  public void SetMaxHeight(int maxHeight) { }
  public void SetNumber(int number) { mNumber = number; }
  public void SetRightEdge(float edge) { mRightEdge = edge; }
  public void SetRules(Rules rules) { mRules = rules; }
  public void SetShowing(int showing) {  }
  protected void SetCardPosition(int idx) { mCard[idx].SetPosition(mX, mY); }
  public void SetDone(boolean done) { mDone = done; }

  //Methods for GenericAnchor
  public void SetStartSeq(int seq){ mSTARTSEQ = seq; }
  public void SetSeq(int seq){ mBUILDSEQ = seq; mMOVESEQ = seq; }
  public void SetBuildSeq(int buildseq){ mBUILDSEQ = buildseq;  }
  public void SetMoveSeq(int moveseq){ mMOVESEQ = moveseq;  }
  
  public void SetWrap(boolean wrap){ mBUILDWRAP = wrap; mMOVEWRAP = wrap; }
  public void SetMoveWrap(boolean movewrap){ mMOVEWRAP = movewrap;  }
  public void SetBuildWrap(boolean buildwrap){ mBUILDWRAP = buildwrap;  }
  
  public void SetSuit(int suit){ mBUILDSUIT = suit; mMOVESUIT = suit; }
  public void SetBuildSuit(int buildsuit){ mBUILDSUIT = buildsuit;  }
  public void SetMoveSuit(int movesuit){ mMOVESUIT = movesuit;  }
  
  public void SetBehavior(int beh){ mDROPOFF = beh; mPICKUP = beh; }
  public void SetDropoff(int dropoff){ mDROPOFF = dropoff;  }
  public void SetPickup(int pickup){ mPICKUP = pickup;  }
  //End Methods for Generic Anchor  
  
  public void SetPosition(float x, float y) {
    mX = x;
    mY = y;
    for (int i = 0; i < mCardCount; i++) {
      SetCardPosition(i);
    }
  }

  // ==========================================================================
  // Functions to add cards
  // ----------------------
  public void AddCard(Card card) {
    mCard[mCardCount++] = card;
    SetCardPosition(mCardCount - 1);
  }

  public void AddMoveCard(MoveCard moveCard) {
    int count = moveCard.GetCount();
    Card[] cards = moveCard.DumpCards();

    for (int i = 0; i < count; i++) {
      AddCard(cards[i]);
    }
  }

  public boolean DropSingleCard(Card card) { return false; }
  public boolean CanDropCard(MoveCard moveCard, int close) { return false; }

  // ==========================================================================
  // Functions to take cards
  // -----------------------
  public Card[] GetCardStack() { return null; }

  public Card GrabCard(float x, float y) {
    Card ret = null;
    if (mCardCount > 0 && IsOverCard(x, y)) {
      ret = PopCard();
    }
    return ret;
  }

  public Card PopCard() {
    Card ret = mCard[--mCardCount];
    mCard[mCardCount] = null;
    return ret;
  }

  // ==========================================================================
  // Functions to interact with cards
  // --------------------------------
  public boolean TapCard(float x, float y) { return false; }

  public boolean UnhideTopCard() {
    if (mCardCount  > 0 && mHiddenCount > 0 && mHiddenCount == mCardCount) {
      mHiddenCount--;
      return true;
    }
    return false;
  }
  public boolean ExpandStack(float x, float y) { return false; }
  public boolean CanMoveStack(float x, float y) { return false; }


  // ==========================================================================
  // Functions to check locations
  // ----------------------------
  private boolean IsOver(float x, float y, boolean deck, int close) {
    float clx = mCardCount == 0 ? mX : mCard[mCardCount - 1].GetX();
    float leftX = mLeftEdge == -1 ? clx : mLeftEdge;
    float rightX = mRightEdge == -1 ? clx + Card.WIDTH : mRightEdge;
    float topY = (mCardCount == 0 || deck) ? mY : mCard[mCardCount-1].GetY();
    float botY = mCardCount > 0 ? mCard[mCardCount - 1].GetY() : mY;
    botY += Card.HEIGHT;

    leftX -= close*Card.WIDTH/2;
    rightX += close*Card.WIDTH/2;
    topY -= close*Card.HEIGHT/2;
    botY += close*Card.HEIGHT/2;
    if (mBottom != -1 && botY + 10 >= mBottom)
      botY = mBottom;

    if (x >= leftX && x <= rightX && y >= topY && y <= botY) {
      return true;
    }
    return false;
  }

  protected boolean IsOverCard(float x, float y) {
    return IsOver(x, y, false, 0);
  }
  protected boolean IsOverCard(float x, float y, int close) {
    return IsOver(x, y, false, close);
  }

  protected boolean IsOverDeck(float x, float y) {
    return IsOver(x, y, true, 0);
  }

  // ==========================================================================
  // Functions to Draw
  // ----------------------------
  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mCardCount == 0) {
      drawMaster.DrawEmptyAnchor(canvas, mX, mY, mDone);
    } else {
      drawMaster.DrawCard(canvas, mCard[mCardCount-1]);
    }
  }
}

