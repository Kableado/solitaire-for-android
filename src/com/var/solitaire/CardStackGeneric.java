package com.var.solitaire;

import android.graphics.Canvas;

// New Abstract
class CardStackGeneric extends CardStack{

  //Sequence start values
  public static final int START_ANY  = 1; // An empty stack can take any card.
  public static final int START_KING = 2; // An empty stack can take only a king.

  //Value Sequences
  public static final int SEQ_ANY = 1; //You can build as you like
  public static final int SEQ_SEQ = 2;  //Building only allows sequential
  public static final int SEQ_ASC = 3;  //Ascending only
  public static final int SEQ_DSC = 4;  //Descending only

  //Suit Sequences that limits how adding cards to the stack works
  public static final int SUIT_ANY   = 1;  //Build doesn't care about suite
  public static final int SUIT_RB    = 2;  //Must alternate Red & Black
  public static final int SUIT_OTHER = 3;//As long as different
  public static final int SUIT_COLOR = 4;//As long as same color
  public static final int SUIT_SAME  = 5; //As long as same suit

  //Pickup & Dropoff Behavior
  public static final int PACK_NONE          = 1;  // Interaction in this mode not allowed
  public static final int PACK_ONE           = 2;  //Can only accept 1 card
  public static final int PACK_MULTI         = 3;  //Can accept multiple cards
  public static final int PACK_FIXED         = 4;  //Don't think this will ever be used
  public static final int PACK_LIMIT_BY_FREE = 5; //For freecell style movement

  protected int mMaxHeight;

  public
  CardStackGeneric(){
    super();
    SetStartSeq(CardStackGeneric.SEQ_ANY);
    SetBuildSeq(CardStackGeneric.SEQ_ANY);
    SetBuildWrap(false);
    SetBuildSuit(CardStackGeneric.SUIT_ANY);
    SetDropoff(CardStackGeneric.PACK_NONE);
    SetPickup(CardStackGeneric.PACK_NONE);
    mMaxHeight = Card.HEIGHT;
  }

  @Override public
  void SetMaxHeight(int maxHeight){
    mMaxHeight = maxHeight;
    SetPosition(mX, mY);
  }

  @Override protected
  void SetCardPosition(int idx){
    if(idx < mHiddenCount){
      // Position a hidden card
      mCard[idx].SetPosition(mX, mY + Card.HIDDEN_SPACING * idx);
      return;
    }

    // Position any other card
    boolean readjust = false;
    int startY = mHiddenCount * Card.HIDDEN_SPACING;
    int spaceLeft = mMaxHeight - (startY + Card.HEIGHT);
    int count = (mCardCount - mHiddenCount);
    int spacing = Card.SMALL_SPACING;
    if(count > 1){
      spacing = spaceLeft / (count - 1);
      if(spacing > Card.SMALL_SPACING){
        spacing = Card.SMALL_SPACING;
      }
    }
    for(int idx2 = mHiddenCount; idx2 <= idx; idx2++){
      int y = (int)mY + startY + (idx2 - mHiddenCount) * spacing;
      mCard[idx2].SetPosition(mX, y);
    }
  }

  @Override public
  void SetHiddenCount(int count){
    super.SetHiddenCount(count);
    SetPosition(mX, mY);
  }

  @Override public
  boolean CanDropCard(MoveCard moveCard, int close){
    if(mDROPOFF == CardStackGeneric.PACK_NONE){
      return false;
    }

    Card card = moveCard.GetTopCard();
    float x = card.GetX() + Card.WIDTH / 2;
    float y = card.GetY() + Card.HEIGHT / 2;
    if(IsOverCard(x, y, close)){
      return CanBuildCard(card);
    }
    return false;
  }

  public
  boolean CanBuildCard(Card card){
    // SEQ_ANY will allow all
    if(mBUILDSEQ == CardStackGeneric.SEQ_ANY){
      return true;
    }
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    // Rules for empty stacks
    if(topCard == null){
      switch(mSTARTSEQ){
        case CardStackGeneric.START_KING:
          return card.GetValue() == Card.KING;
        case CardStackGeneric.START_ANY:
        default:
          return true;
      }
    }
    int value = card.GetValue();
    int suit = card.GetSuit();
    int tvalue = topCard.GetValue();
    int tsuit = topCard.GetSuit();
    // Fail if sequence is wrong
    switch(mBUILDSEQ){
      //WRAP_NOWRAP=1; //Building stacks do not wrap
      //WRAP_WRAP=2;   //Building stacks wraps around
      case CardStackGeneric.SEQ_ASC:
        if(value - tvalue != 1){
          return false;
        }
        break;
      case CardStackGeneric.SEQ_DSC:
        if(tvalue - value != 1){
          return false;
        }
        break;
      case CardStackGeneric.SEQ_SEQ:
        if(Math.abs(tvalue - value) != 1){
          return false;
        }
        break;
    }
    // Fail if suit is wrong
    switch(mBUILDSUIT){
      case CardStackGeneric.SUIT_RB:
        if(Math.abs(tsuit - suit) % 2 == 0){
          return false;
        }
        break;
      case CardStackGeneric.SUIT_OTHER:
        if(tsuit == suit){
          return false;
        }
        break;
      case CardStackGeneric.SUIT_COLOR:
        if(Math.abs(tsuit - suit) != 2){
          return false;
        }
        break;
      case CardStackGeneric.SUIT_SAME:
        if(tsuit != suit){
          return false;
        }
        break;
    }
    // Passes all rules
    return true;
  }

  @Override public
  void Draw(DrawMaster drawMaster, Canvas canvas){
    if(mCardCount == 0){
      drawMaster.DrawEmptyAnchor(canvas, mX, mY, mDone);
      return;
    }
    for(int i = 0; i < mCardCount; i++){
      if(i < mHiddenCount){
        drawMaster.DrawHiddenCard(canvas, mCard[i]);
      }else{
        drawMaster.DrawCard(canvas, mCard[i]);
      }
    }
  }

  @Override
  public boolean ExpandStack(float x, float y) {
    if (IsOverDeck(x, y)) {
      return (GetMovableCount() > 0);
    }
    return false;
  }

  @Override
  public boolean CanMoveStack(float x, float y) { return ExpandStack(x, y); }

  @Override
  public Card[] GetCardStack() {
    int movableCount = GetMovableCount();
    Card[] ret = new Card[movableCount];
    for (int i = movableCount-1; i >= 0; i--) {
      ret[i] = PopCard();
    }
    return ret;
  }

  @Override
  public int GetMovableCount() {
    int visibleCount = GetVisibleCount();
    if (visibleCount == 0 || mPICKUP == CardStackGeneric.PACK_NONE){
      return 0;
    }
    int seq_allowed = 1;
    if (visibleCount > 1){
      int i = mCardCount-1;
      boolean g;
      boolean h;
      do {
        g = true;
        h = true;
        switch (mMOVESEQ){
          case CardStackGeneric.SEQ_ANY:
            h = true;
            break;
          case CardStackGeneric.SEQ_ASC:
            h = this.is_seq_asc(i-1, i, mMOVEWRAP);
            break;
          case CardStackGeneric.SEQ_DSC:
            h = this.is_seq_asc(i, i-1, mMOVEWRAP);
            break;
          case CardStackGeneric.SEQ_SEQ:
            h = (this.is_seq_asc(i, i-1, mMOVEWRAP) ||
                this.is_seq_asc(i-1, i, mMOVEWRAP));
            break;
        }
        if (h == false){
          g = false;
        }
        switch (mMOVESUIT){
          case CardStackGeneric.SUIT_ANY:
            h = true;
            break;
          case CardStackGeneric.SUIT_COLOR:
            h = !this.is_suit_rb(i-1,i);
            break;
          case CardStackGeneric.SUIT_OTHER:
            h = this.is_suit_other(i-1, i);
            break;
          case CardStackGeneric.SUIT_RB:
            h = this.is_suit_rb(i-1, i);
            break;
          case CardStackGeneric.SUIT_SAME:
            h = this.is_suit_same(i-1, i);
            break;
        }
        if (h == false){
          g = false;
        }
        if (g){  seq_allowed++;  }
        i--;
      }while(g && (mCardCount - i) < visibleCount);
    }

    switch (mPICKUP){
      case CardStackGeneric.PACK_NONE:
        return 0;
      case CardStackGeneric.PACK_ONE:
        seq_allowed = Math.min(1, seq_allowed);
        break;
      case CardStackGeneric.PACK_MULTI:
        break;
      case CardStackGeneric.PACK_FIXED:
        //seq_allowed = Math.min( xmin, seq_allowed);
        break;
      case CardStackGeneric.PACK_LIMIT_BY_FREE:
        seq_allowed = Math.min(mRules.CountFreeSpaces()+1, seq_allowed);
        break;
    }
    return seq_allowed;
  }

  public boolean is_seq_asc(int p1, int p2, boolean wrap){
    Card c1 = mCard[p1];
    Card c2 = mCard[p2];
    int v1 = c1.GetValue();
    int v2 = c2.GetValue();

    if (v2 + 1 == v1){
      return true;
    }
    if (wrap){
      if (v2 == Card.KING && v1 == Card.ACE){
        return true;
      }
    }
    return false;
  }
  public boolean is_suit_rb(int p1, int p2){
    Card c1 = mCard[p1];
    Card c2 = mCard[p2];
    int s1 = c1.GetSuit();
    int s2 = c2.GetSuit();
    if (  (s1 == Card.CLUBS || s1 == Card.SPADES) &&
          (s2 == Card.HEARTS || s2 == Card.DIAMONDS)  ){
      return true;
    }
    if (  (s1 == Card.HEARTS || s1 == Card.DIAMONDS) &&
          (s2 == Card.CLUBS || s2 == Card.SPADES)  ){
      return true;
    }
    return false;
  }
  public boolean is_suit_same(int p1, int p2){
    return (mCard[p1].GetSuit() == mCard[p2].GetSuit());
  }
  public boolean is_suit_other(int p1, int p2){
    return (mCard[p1].GetSuit() != mCard[p2].GetSuit());
  }

  public float GetNewY() {
    if (mCardCount == 0) {
      return mY;
    }
    return mCard[mCardCount-1].GetY();
  }
}
