package com.var.solitaire;

class RuleEventHandler{
  private int       mEvent;
  private CardStack mCardAnchor;
  private Card      mCard;
  private Rules     mRules;

  public
  RuleEventHandler(Rules rules) {
    mRules = rules;
    mEvent = -1;
    mCardAnchor = null;
    mCard = null;
  }

  public void PostEvent(int event) {
    PostEvent(event, null, null);
  }

  public void PostEvent(int event, CardStack anchor) {
    PostEvent(event, anchor, null);
  }

  public void PostEvent(int event, CardStack anchor, Card card) {
    mEvent = event;
    mCardAnchor = anchor;
    mCard = card;
  }


  public void ClearEvent() {
    mEvent = Rules.EVENT_INVALID;
    mCardAnchor = null;
    mCard = null;
  }

  public boolean HasEvent() {
    return mEvent != Rules.EVENT_INVALID;
  }

  public void HandleEvent() {
    if (HasEvent()) {
      int event = mEvent;
      CardStack cardAnchor = mCardAnchor;
      Card card = mCard;
      ClearEvent();
      if (cardAnchor != null && card != null) {
        mRules.EventProcess(event, cardAnchor, card);
      } else if (cardAnchor != null) {
        mRules.EventProcess(event, cardAnchor);
      } else {
        mRules.EventProcess(event);
      }
    }
  }
}
