package com.var.solitaire;

import android.os.Bundle;

public
class RulesSolitaire extends Rules {

  private boolean mDealThree;
  private int mDealsLeft;
  private String mScoreString;
  private int mLastScore;
  private int mCarryOverScore;

  @Override
  public void Init(Bundle map) {
    mIgnoreEvents = true;
    mDealThree = mView.GetSettings().getBoolean("SolitaireDealThree", true);

    // Thirteen total anchors for regular solitaire
    mCardCount = 52;
    mCardAnchorCount = 13;
    mCardAnchor = new CardStack[mCardAnchorCount];

    // Top dealt from anchors
    mCardAnchor[0] = CardStack.CreateAnchor(CardStack.DEAL_FROM, 0, this);
    mCardAnchor[1] = CardStack.CreateAnchor(CardStack.DEAL_TO, 1, this);
    mCardAnchor[1].SetShowing(4);

    // Top anchors for placing cards
    for (int i = 2; i < 6; i++) {
      mCardAnchor[i] = CardStack.CreateAnchor(CardStack.SEQ_SINK, i, this);
    }

    // Middle anchor stacks
    for (int i = 6; i < 13; i++) {
      mCardAnchor[i] = CardStack.CreateAnchor(CardStack.GENERIC_ANCHOR, i, this);
      mCardAnchor[i].SetStartSeq(CardStackGeneric.START_KING);
      mCardAnchor[i].SetBuildSeq(CardStackGeneric.SEQ_DSC);
      mCardAnchor[i].SetMoveSeq(CardStackGeneric.SEQ_ASC);
      mCardAnchor[i].SetSuit(CardStackGeneric.SUIT_RB);
      mCardAnchor[i].SetWrap(false);
      mCardAnchor[i].SetBehavior(CardStackGeneric.PACK_MULTI);
    }

    if (map != null) {
      // Do some assertions, default to a new game if we find an invalid state
      if (map.getInt("cardAnchorCount") == 13 &&
          map.getInt("cardCount") == 52) {
        int[] cardCount = map.getIntArray("anchorCardCount");
        int[] hiddenCount = map.getIntArray("anchorHiddenCount");
        int[] value = map.getIntArray("value");
        int[] suit = map.getIntArray("suit");
        int cardIdx = 0;
        mDealsLeft = map.getInt("rulesExtra");

        for (int i = 0; i < 13; i++) {
          for (int j = 0; j < cardCount[i]; j++, cardIdx++) {
            Card card = new Card(value[cardIdx], suit[cardIdx]);
            mCardAnchor[i].AddCard(card);
          }
          mCardAnchor[i].SetHiddenCount(hiddenCount[i]);
        }
        if (mDealsLeft != -1) {
          // Reset to zero as GetScore() uses it in its calculation.
          mCarryOverScore = 0;
          mCarryOverScore = map.getInt("score") - GetScore();
        }

        mIgnoreEvents = false;
        // Return here so an invalid save state will result in a new game
        return;
      }
    }

    mDeck = new Deck(1);
    for (int i = 0; i < 7; i++) {
      for (int j = 0; j <= i; j++) {
        mCardAnchor[i+6].AddCard(mDeck.PopCard());
      }
      mCardAnchor[i+6].SetHiddenCount(i);
    }

    while (!mDeck.Empty()) {
      mCardAnchor[0].AddCard(mDeck.PopCard());
    }

    if (mView.GetSettings().getBoolean("SolitaireStyleNormal", true)) {
      mDealsLeft = -1;
    } else {
      mDealsLeft = mDealThree ? 2 : 0;
      mLastScore = -52;
      mScoreString = "-$52";
      mCarryOverScore = 0;
    }
    mIgnoreEvents = false;
  }

  @Override
  public void SetCarryOverScore(int score) {
    mCarryOverScore = score;
  }

  @Override
  public void Resize(int width, int height) {
    int rem = width - Card.WIDTH*7;
    int maxHeight = height - (20 + Card.HEIGHT);
    rem /= 8;
    for (int i = 0; i < 7; i++) {
      mCardAnchor[i+6].SetPosition(rem + i * (rem+Card.WIDTH), 20 + Card.HEIGHT);
      mCardAnchor[i+6].SetMaxHeight(maxHeight);
    }

    for (int i = 3; i >= 0; i--) {
      mCardAnchor[i+2].SetPosition(rem + (6-i) * (rem + Card.WIDTH), 10);
    }

    for (int i = 0; i < 2; i++) {
      mCardAnchor[i].SetPosition(rem + i * (rem + Card.WIDTH), 10);
    }

    // Setup edge cards (Touch sensor loses sensitivity towards the edge).
    mCardAnchor[0].SetLeftEdge(0);
    mCardAnchor[2].SetRightEdge(width);
    mCardAnchor[6].SetLeftEdge(0);
    mCardAnchor[12].SetRightEdge(width);
    for (int i = 0; i < 7; i++) {
      mCardAnchor[i+6].SetBottom(height);
    }
  }

  @Override
  public void EventProcess(int event, CardStack anchor) {
    if (mIgnoreEvents) {
      return;
    }
    if (event == EVENT_DEAL) {
      if (mCardAnchor[0].GetCount() == 0) {
        boolean addDealCount = false;
        if (mDealsLeft == 0) {
          mCardAnchor[0].SetDone(true);
          return;
        } else if (mDealsLeft > 0) {
          mDealsLeft--;
          addDealCount = true;
        }
        int count = 0;
        while (mCardAnchor[1].GetCount() > 0) {
          mCardAnchor[0].AddCard(mCardAnchor[1].PopCard());
          count++;
        }
        mMoveHistory.push(new Move(1, 0, count, true, false, addDealCount));
        mView.Refresh();
      } else {
        int count = 0;
        int maxCount = mDealThree ? 3 : 1;
        for (int i = 0; i < maxCount && mCardAnchor[0].GetCount() > 0; i++) {
          mCardAnchor[1].AddCard(mCardAnchor[0].PopCard());
          count++;
        }
        if (mDealsLeft == 0 && mCardAnchor[0].GetCount() == 0) {
          mCardAnchor[0].SetDone(true);
        }
        mMoveHistory.push(new Move(0, 1, count, true, false));
      }
    } else if (event == EVENT_STACK_ADD) {
      if (mCardAnchor[2].GetCount() == 13 && mCardAnchor[3].GetCount() == 13 &&
          mCardAnchor[4].GetCount() == 13 && mCardAnchor[5].GetCount() == 13) {
        SignalWin();
      } else {
        if (mAutoMoveLevel == AUTO_MOVE_ALWAYS ||
            (mAutoMoveLevel == AUTO_MOVE_FLING_ONLY && mWasFling)) {
          EventAlert(EVENT_SMART_MOVE);
        } else {
          mView.StopAnimating();
          mWasFling = false;
        }
      }
    }
  }

  @Override
  public void EventProcess(int event, CardStack anchor, Card card) {
    if (mIgnoreEvents) {
      anchor.AddCard(card);
      return;
    }
    if (event == EVENT_FLING) {
      mWasFling = true;
      if (!TryToSinkCard(anchor, card)) {
        anchor.AddCard(card);
        mWasFling = false;
      }
    } else {
      anchor.AddCard(card);
    }
  }

  @Override
  public void EventProcess(int event) {
    if (mIgnoreEvents) {
      return;
    }
    if (event == EVENT_SMART_MOVE) {
      int i;
      for (i = 0; i < 7; i++) {
        if (mCardAnchor[i+6].GetCount() > 0 &&
            TryToSink(mCardAnchor[i+6])) {
          break;
        }
      }
      if (i == 7) {
        mWasFling = false;
        mView.StopAnimating();
      }
    }
  }

  @Override
  public boolean Fling(MoveCard moveCard) {
    if (moveCard.GetCount() == 1) {
      CardStack anchor = moveCard.GetAnchor();
      Card card = moveCard.DumpCards(false)[0];
      for (int i = 0; i < 4; i++) {
        if (mCardAnchor[i+2].DropSingleCard(card)) {
          EventAlert(EVENT_FLING, anchor, card);
          return true;
        }
      }
      anchor.AddCard(card);
    } else {
      moveCard.Release();
    }
    return false;
  }

  private boolean TryToSink(CardStack anchor) {
    Card card = anchor.PopCard();
    boolean ret = TryToSinkCard(anchor, card);
    if (!ret) {
      anchor.AddCard(card);
    }
    return ret;
  }

  private boolean TryToSinkCard(CardStack anchor, Card card) {
    for (int i = 0; i < 4; i++) {
      if (mCardAnchor[i+2].DropSingleCard(card)) {
        mMoveHistory.push(new Move(anchor.GetNumber(), i+2, 1, false, anchor.UnhideTopCard()));
        mAnimateCard.MoveCard(card, mCardAnchor[i+2]);
        return true;
      }
    }

    return false;
  }

  @Override
  public int GetRulesExtra() {
    return mDealsLeft;
  }

  @Override
  public String GetGameTypeString() {
    if (mDealsLeft == -1) {
      if (mDealThree) {
        return "SolitaireNormalDeal3";
      } else {
        return "SolitaireNormalDeal1";
      }
    } else {
      if (mDealThree) {
        return "SolitaireVegasDeal3";
      } else {
        return "SolitaireVegasDeal1";
      }
    }
  }
  @Override
  public String GetPrettyGameTypeString() {
    if (mDealsLeft == -1) {
      if (mDealThree) {
        return "Solitaire Dealing Three Cards";
      } else {
        return "Solitaire Dealing One Card";
      }
    } else {
      if (mDealThree) {
        return "Vegas Solitaire Dealing Three Cards";
      } else {
        return "Vegas Solitaire Dealing One Card";
      }
    }
  }

  @Override
  public boolean HasScore() {
    if (mDealsLeft != -1) {
      return true;
    }
    return false;
  }

  @Override
  public boolean HasString() {
    return HasScore();
  }

  @Override
  public String GetString() {
    if (mDealsLeft != -1) {
      int score = mCarryOverScore - 52;
      for (int i = 0; i < 4; i++) {
        score += 5 * mCardAnchor[i+2].GetCount();
      }
      if (score != mLastScore) {
        if (score < 0) {
          mScoreString = "-$" + (score * -1);
        } else {
          mScoreString = "$" + score;
        }
      }
      return mScoreString;
    }
    return "";
  }

  @Override
  public int GetScore() {
    if (mDealsLeft != -1) {
      int score = mCarryOverScore - 52;
      for (int i = 0; i < 4; i++) {
        score += 5 * mCardAnchor[i+2].GetCount();
      }
      return score;
    }
    return 0;
  }

  @Override
  public void AddDealCount() {
    if (mDealsLeft != -1) {
      mDealsLeft++;
      mCardAnchor[0].SetDone(false);
    }
  }
}
