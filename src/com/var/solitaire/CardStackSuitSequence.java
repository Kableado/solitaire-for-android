package com.var.solitaire;

// Anchor that holds increasing same suited cards
public
class CardStackSuitSequence extends CardStack{

  @Override
  public void AddCard(Card card) {
    super.AddCard(card);
    mRules.EventAlert(Rules.EVENT_STACK_ADD, this);
  }

  @Override
  public boolean CanDropCard(MoveCard moveCard, int close) {
    Card card = moveCard.GetTopCard();
    float x = card.GetX() + Card.WIDTH/2;
    float y = card.GetY() + Card.HEIGHT/2;
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    float my = mCardCount > 0 ? topCard.GetY() : mY;

    if (IsOverCard(x, y, close)) {
      if (moveCard.GetCount() == 1) {
        if ((topCard == null && card.GetValue() == 1) ||
            (topCard != null && card.GetSuit() == topCard.GetSuit() &&
             card.GetValue() == topCard.GetValue() + 1)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean DropSingleCard(Card card) {
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    if ((topCard == null && card.GetValue() == 1) ||
        (topCard != null && card.GetSuit() == topCard.GetSuit() &&
         card.GetValue() == topCard.GetValue() + 1)) {
      //AddCard(card);
      return true;
    }
    return false;
  }
}
