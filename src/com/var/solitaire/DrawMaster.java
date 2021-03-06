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
*/
package com.var.solitaire;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;


public class DrawMaster {

  private Context mContext;

  // Background
  private int mScreenWidth;
  private int mScreenHeight;
  private int mDpi;
  private Paint mBGPaint;

  // Card stuff
  private final Paint mSuitPaint = new Paint();
  private Bitmap[] mCardBitmap;
  private Bitmap mCardHidden;

  private Paint mEmptyAnchorPaint;
  private Paint mDoneEmptyAnchorPaint;
  private Paint mShadePaint;
  private Paint mLightShadePaint;

  private int roundEdge;    // Round curve on each card corner
  private int cardOutline;  // Card outline border thickness in pixels
  private int offset;       // Whitespace between card border and font
  private Paint mGrnTxtPaint;
  private Paint mWhitePaint;
  private Paint mTimePaint;
  private Paint mMenuPaint;
  private int mLastSeconds;
  private String mTimeString;
  
  private Bitmap mBoardBitmap;
  private Canvas mBoardCanvas;

  private final Rect textBounds = new Rect(); //don't new this up in a draw method


  public DrawMaster(Context context, int width, int height, int dpi) {

    mContext = context;
    mScreenWidth = width;
    mScreenHeight = height;
    mDpi = dpi;

    // Background
    mBGPaint = new Paint();
    mBGPaint.setARGB(255, 0, 128, 0);

    mShadePaint = new Paint();
    mShadePaint.setARGB(200, 0, 0, 0);

    mLightShadePaint = new Paint();
    mLightShadePaint.setARGB(100, 0, 0, 0);

    // Card related stuff
    mEmptyAnchorPaint = new Paint();
    mEmptyAnchorPaint.setARGB(255, 0, 64, 0);
    mEmptyAnchorPaint.setAntiAlias(true);
    mDoneEmptyAnchorPaint = new Paint();
    mDoneEmptyAnchorPaint.setARGB(128, 255, 0, 0);
    mEmptyAnchorPaint.setAntiAlias(true);
    roundEdge = 4 * mDpi/160;
    cardOutline = 1 + (mDpi-40)/160;
    offset = mDpi/160;

    mGrnTxtPaint = new Paint();
    mGrnTxtPaint.setARGB(255, 0, 128, 0);
    mGrnTxtPaint.setTextSize(18 * mDpi/160);
    mGrnTxtPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
    mGrnTxtPaint.setTextAlign(Paint.Align.CENTER);
    mGrnTxtPaint.setAntiAlias(true);
    mWhitePaint = new Paint();
    mWhitePaint.setARGB(255, 255, 255, 255);
    mTimePaint = new Paint();
    mTimePaint.setTextSize(18 * mDpi/160);
    mTimePaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
    mTimePaint.setTextAlign(Paint.Align.RIGHT);
    mTimePaint.setAntiAlias(true);
    mMenuPaint = new Paint();
    mMenuPaint.setTextSize(18 * mDpi/160);
    mMenuPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
    mMenuPaint.setTextAlign(Paint.Align.CENTER);
    mMenuPaint.setAntiAlias(true);
    mLastSeconds = -1;

    mCardBitmap = new Bitmap[52];
    mBoardBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565);
    mBoardCanvas = new Canvas(mBoardBitmap);
  }

  public int GetWidth() { return mScreenWidth; }
  public int GetHeight() { return mScreenHeight; }
  public Canvas GetBoardCanvas() { return mBoardCanvas; }
  public int GetDpi() { return mDpi; }

  public void DrawCard(Canvas canvas, Card card) {
    float x = card.GetX();
    float y = card.GetY();
    int idx = card.GetSuit()*13+(card.GetValue()-1);
    canvas.drawBitmap(mCardBitmap[idx], x, y, mSuitPaint);
  }

  public void DrawHiddenCard(Canvas canvas, Card card) {
    float x = card.GetX();
    float y = card.GetY();
    canvas.drawBitmap(mCardHidden, x, y, mSuitPaint);
  }

  public void DrawEmptyAnchor(Canvas canvas, float x, float y, boolean done) {
    RectF pos = new RectF(x, y, x + Card.WIDTH, y + Card.HEIGHT);
    if (!done) {
      canvas.drawRoundRect(pos, roundEdge, roundEdge, mEmptyAnchorPaint);
    } else {
      canvas.drawRoundRect(pos, roundEdge, roundEdge, mDoneEmptyAnchorPaint);
    }
  }

  public void DrawBackground(Canvas canvas) {
    canvas.drawRect(0, 0, mScreenWidth, mScreenHeight, mBGPaint);
  }

  public void DrawShade(Canvas canvas) {
    canvas.drawRect(0, 0, mScreenWidth, mScreenHeight, mShadePaint);
  }

  public void DrawLightShade(Canvas canvas) {
    canvas.drawRect(0, 0, mScreenWidth, mScreenHeight, mLightShadePaint);
  }

  public void DrawLastBoard(Canvas canvas) {
    canvas.drawBitmap(mBoardBitmap, 0, 0, mSuitPaint);
  }

  public void SetScreenSize(int width, int height) {
    mScreenWidth = width;
    mScreenHeight = height;
    mBoardBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    mBoardCanvas = new Canvas(mBoardBitmap);
  }

  public void DrawCards(boolean bigCards) {
    if (bigCards) {
      DrawBigCards(mContext.getResources());
    } else {
      DrawCards(mContext.getResources());
    }
  }

  private void DrawBigCards(Resources r) {

    Paint cardFrontPaint = new Paint();
    Paint cardBorderPaint = new Paint();
    Bitmap[] bigSuit = new Bitmap[4];
    Bitmap[] suit = new Bitmap[4];
    Bitmap[] blackFont = new Bitmap[13];
    Bitmap[] redFont = new Bitmap[13];
    Canvas canvas;
    int width = Card.WIDTH;
    int height = Card.HEIGHT;
    int w = 0;   // Width  of actual bitmap image
    int h = 0;   // Height of actual bitmap image

    Drawable drawable = r.getDrawable(com.var.solitaire.R.drawable.cardback);

    mCardHidden = Bitmap.createBitmap(Card.WIDTH, Card.HEIGHT,
                                      Bitmap.Config.ARGB_4444);
    canvas = new Canvas(mCardHidden);
    drawable.setBounds(0, 0, Card.WIDTH, Card.HEIGHT);
    drawable.draw(canvas);

    // Load suit bitmaps
    drawable = r.getDrawable(com.var.solitaire.R.drawable.suits);
    w = drawable.getIntrinsicWidth();
    h = drawable.getIntrinsicHeight();
    int suitWidth = w/4;
    for (int i = 0; i < 4; i++) {
      suit[i] = Bitmap.createBitmap(w/4, h, Bitmap.Config.ARGB_4444);
      canvas = new Canvas(suit[i]);
      drawable.setBounds(-i*w/4, 0, -i*w/4+w, h);
      drawable.draw(canvas);
    }

    // Load big suit bitmaps
    drawable = r.getDrawable(com.var.solitaire.R.drawable.bigsuits);
    w = drawable.getIntrinsicWidth();
    h = drawable.getIntrinsicHeight();
    int bigSuitWidth = w/4;
    int bigSuitHeight = h;
    for (int i = 0; i < 4; i++) {
      bigSuit[i] = Bitmap.createBitmap(w/4, h, Bitmap.Config.ARGB_4444);
      canvas = new Canvas(bigSuit[i]);
      drawable.setBounds(-i*w/4, 0, -i*w/4+w, h);
      drawable.draw(canvas);
    }

    // Load big black font bitmaps
    drawable = r.getDrawable(com.var.solitaire.R.drawable.bigblackfont);
    w = drawable.getIntrinsicWidth();
    h = drawable.getIntrinsicHeight();
    for (int i = 0; i < 13; i++) {
      blackFont[i] = Bitmap.createBitmap(w/13, h, Bitmap.Config.ARGB_4444);
      canvas = new Canvas(blackFont[i]);
      drawable.setBounds(-i*w/13, 0, -i*w/13+w, h);
      drawable.draw(canvas);
    }

    // Load big red font bitmaps
    drawable = r.getDrawable(com.var.solitaire.R.drawable.bigredfont);
    for (int i = 0; i < 13; i++) {
      redFont[i] = Bitmap.createBitmap(w/13, h, Bitmap.Config.ARGB_4444);
      canvas = new Canvas(redFont[i]);
      drawable.setBounds(-i*w/13, 0, -i*w/13+w, h);
      drawable.draw(canvas);
    }

    // Create deck of cards
    cardBorderPaint.setARGB(255, 0, 0, 0);
    cardFrontPaint.setARGB(255, 255, 255, 255);
    RectF pos = new RectF();
    for (int suitIdx = 0; suitIdx < 4; suitIdx++) {
      for (int valueIdx = 0; valueIdx < 13; valueIdx++) {
        mCardBitmap[suitIdx*13+valueIdx] = Bitmap.createBitmap(
            width, height, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(mCardBitmap[suitIdx*13+valueIdx]);
        // Draw card outline
        for (int k = 0; k < cardOutline; k++) {
          pos.set(k, k, width-k, height-k);
          canvas.drawRoundRect(pos, roundEdge, roundEdge, cardBorderPaint);
        }
        pos.set(cardOutline, cardOutline, width-cardOutline, height-cardOutline);
        canvas.drawRoundRect(pos, roundEdge, roundEdge, cardFrontPaint);

	// Draw font in upper-left
        if ((suitIdx & 1) == 1) {
          canvas.drawBitmap(redFont[valueIdx], roundEdge, roundEdge,
                            mSuitPaint);
        } else {
          canvas.drawBitmap(blackFont[valueIdx], roundEdge, roundEdge,
                            mSuitPaint);
        }

	// Draw suit in upper-right
        canvas.drawBitmap(suit[suitIdx], width-suitWidth-roundEdge,
                          roundEdge, mSuitPaint);
	// Draw big suit in center of card
        canvas.drawBitmap(bigSuit[suitIdx], (width-bigSuitWidth)/2+1,
                          (height-bigSuitHeight)/2+3, mSuitPaint);
      }
    }
  }

  private void DrawCards(Resources r) {

    Paint cardFrontPaint = new Paint();
    cardFrontPaint.setAntiAlias(true);
    Paint cardBorderPaint = new Paint();
    cardBorderPaint.setAntiAlias(true);
    Bitmap[] suit = new Bitmap[4];
    Bitmap[] revSuit = new Bitmap[4];
    Bitmap[] smallSuit = new Bitmap[4];
    Bitmap[] revSmallSuit = new Bitmap[4];
    Bitmap[] blackFont = new Bitmap[13];
    Bitmap[] revBlackFont = new Bitmap[13];
    Bitmap[] redFont = new Bitmap[13];
    Bitmap[] revRedFont = new Bitmap[13];
    Bitmap redJack;
    Bitmap redRevJack;
    Bitmap redQueen;
    Bitmap redRevQueen;
    Bitmap redKing;
    Bitmap redRevKing;
    Bitmap blackJack;
    Bitmap blackRevJack;
    Bitmap blackQueen;
    Bitmap blackRevQueen;
    Bitmap blackKing;
    Bitmap blackRevKing;
    Canvas canvas;
    int width = Card.WIDTH;
    int height = Card.HEIGHT;
    int w = 0;   // Width  of actual bitmap image
    int h = 0;   // Height of actual bitmap image

    Drawable drawable = r.getDrawable(com.var.solitaire.R.drawable.cardback);

    mCardHidden = Bitmap.createBitmap(Card.WIDTH, Card.HEIGHT,
                                      Bitmap.Config.ARGB_4444);
    canvas = new Canvas(mCardHidden);
    drawable.setBounds(0, 0, Card.WIDTH, Card.HEIGHT);
    drawable.draw(canvas);

    // Load suit bitmaps
    drawable = r.getDrawable(com.var.solitaire.R.drawable.suits);
    w = drawable.getIntrinsicWidth();
    h = drawable.getIntrinsicHeight();
    for (int i = 0; i < 4; i++) {
      suit[i] = Bitmap.createBitmap(w/4, h, Bitmap.Config.ARGB_4444);
      revSuit[i] = Bitmap.createBitmap(w/4, h, Bitmap.Config.ARGB_4444);
      canvas = new Canvas(suit[i]);
      drawable.setBounds(-i*w/4, 0, -i*w/4+w, h);
      drawable.draw(canvas);
      canvas = new Canvas(revSuit[i]);
      canvas.rotate(180);
      drawable.setBounds(-i*w/4-w/4, -h, -i*w/4+(w-w/4), 0);
      drawable.draw(canvas);
    }

    // Define faceBox line pairs now that we have suits (w)idth and (h)eight.
    // Allow 1 pixel for line.
    //                     x0,y0,x1,y1
    int suitWidth = w/4;
    int suitHeight = h;
    int borderWidth = width/7;
    int borderHeight = height/7;
    if(borderWidth<suitWidth){
      borderWidth=suitWidth;
    }
    if(borderHeight<suitHeight){
      borderHeight=suitHeight;
    }
    float[] faceBox = {
            borderWidth-1,     borderHeight-2,        width-borderWidth, borderHeight-2,        //  Top edge
            width-borderWidth, borderHeight-2,        width-borderWidth, height-borderHeight+1, //  Right edge
            width-borderWidth, height-borderHeight+1, borderWidth-1,     height-borderHeight+1, //  Bottom edge
            borderWidth-1,     height-borderHeight+1, borderWidth-1,     borderHeight-2         //  Left edge
    };

    // Load small suit bitmaps
    drawable = r.getDrawable(com.var.solitaire.R.drawable.smallsuits);
    w = drawable.getIntrinsicWidth();
    h = drawable.getIntrinsicHeight();
    int smallSuitWidth = w/4;
    int smallSuitHeight = h;
    for (int i = 0; i < 4; i++) {
      smallSuit[i] = Bitmap.createBitmap(w/4, h, Bitmap.Config.ARGB_4444);
      revSmallSuit[i] = Bitmap.createBitmap(w/4, h, Bitmap.Config.ARGB_4444);
      canvas = new Canvas(smallSuit[i]);
      drawable.setBounds(-i*w/4, 0, -i*w/4+w, h);
      drawable.draw(canvas);
      canvas = new Canvas(revSmallSuit[i]);
      canvas.rotate(180);
      drawable.setBounds(-i*w/4-w/4, -h, -i*w/4+(w-w/4), 0);
      drawable.draw(canvas);
    }

    // Load medium black font bitmaps
    drawable = r.getDrawable(com.var.solitaire.R.drawable.medblackfont);
    w = drawable.getIntrinsicWidth();
    h = drawable.getIntrinsicHeight();
    int fontWidth = w/13;   // Same as for red below
    int fontHeight = h;     // Same as for red below
    for (int i = 0; i < 13; i++) {
      blackFont[i] = Bitmap.createBitmap(fontWidth, fontHeight, Bitmap.Config.ARGB_4444);
      revBlackFont[i] = Bitmap.createBitmap(fontWidth, fontHeight, Bitmap.Config.ARGB_4444);
      canvas = new Canvas(blackFont[i]);
      drawable.setBounds(-i*fontWidth, 0, -i*fontWidth+13*fontWidth, fontHeight);
      drawable.draw(canvas);
      canvas = new Canvas(revBlackFont[i]);
      canvas.rotate(180);
      drawable.setBounds(-i*fontWidth-fontWidth, -fontHeight, -i*fontWidth+(12*fontWidth), 0);
      drawable.draw(canvas);
    }

    // Load medium red font bitmaps
    drawable = r.getDrawable(com.var.solitaire.R.drawable.medredfont);
    for (int i = 0; i < 13; i++) {
      redFont[i] = Bitmap.createBitmap(fontWidth, fontHeight, Bitmap.Config.ARGB_4444);
      revRedFont[i] = Bitmap.createBitmap(fontWidth, fontHeight, Bitmap.Config.ARGB_4444);
      canvas = new Canvas(redFont[i]);
      drawable.setBounds(-i*fontWidth, 0, -i*fontWidth+13*fontWidth, fontHeight);
      drawable.draw(canvas);
      canvas = new Canvas(revRedFont[i]);
      canvas.rotate(180);
      drawable.setBounds(-i*fontWidth-fontWidth, -fontHeight, -i*fontWidth+(12*fontWidth), 0);
      drawable.draw(canvas);
    }

    int faceWidth = width - borderWidth*2;
    if(faceWidth<1){faceWidth=1;}
    int faceHeight = height/2 - borderHeight+1;
    if(faceHeight<1){faceHeight=1;}

    // Load red jack bitmap
    drawable = r.getDrawable(com.var.solitaire.R.drawable.redjack);
    redJack = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
    redRevJack = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
    canvas = new Canvas(redJack);
    drawable.setBounds(0, 0, faceWidth, faceHeight);
    drawable.draw(canvas);
    canvas = new Canvas(redRevJack);
    canvas.rotate(180);
    drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
    drawable.draw(canvas);

    // Load red queen bitmap
    drawable = r.getDrawable(com.var.solitaire.R.drawable.redqueen);
    redQueen = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
    redRevQueen = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
    canvas = new Canvas(redQueen);
    drawable.setBounds(0, 0, faceWidth, faceHeight);
    drawable.draw(canvas);
    canvas = new Canvas(redRevQueen);
    canvas.rotate(180);
    drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
    drawable.draw(canvas);

    // Load red king bitmap
    drawable = r.getDrawable(com.var.solitaire.R.drawable.redking);
    redKing = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
    redRevKing = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
    canvas = new Canvas(redKing);
    drawable.setBounds(0, 0, faceWidth, faceHeight);
    drawable.draw(canvas);
    canvas = new Canvas(redRevKing);
    canvas.rotate(180);
    drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
    drawable.draw(canvas);

    // Load black jack bitmap
    drawable = r.getDrawable(com.var.solitaire.R.drawable.blackjack);
    blackJack = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
    blackRevJack = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
    canvas = new Canvas(blackJack);
    drawable.setBounds(0, 0, faceWidth, faceHeight);
    drawable.draw(canvas);
    canvas = new Canvas(blackRevJack);
    canvas.rotate(180);
    drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
    drawable.draw(canvas);

    // Load black queen bitmap
    drawable = r.getDrawable(com.var.solitaire.R.drawable.blackqueen);
    blackQueen = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
    blackRevQueen = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
    canvas = new Canvas(blackQueen);
    drawable.setBounds(0, 0, faceWidth, faceHeight);
    drawable.draw(canvas);
    canvas = new Canvas(blackRevQueen);
    canvas.rotate(180);
    drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
    drawable.draw(canvas);

    // Load black king bitmap
    drawable = r.getDrawable(com.var.solitaire.R.drawable.blackking);
    blackKing = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
    blackRevKing = Bitmap.createBitmap(faceWidth, faceHeight, Bitmap.Config.ARGB_4444);
    canvas = new Canvas(blackKing);
    drawable.setBounds(0, 0, faceWidth, faceHeight);
    drawable.draw(canvas);
    canvas = new Canvas(blackRevKing);
    canvas.rotate(180);
    drawable.setBounds(-faceWidth, -faceHeight, 0, 0);
    drawable.draw(canvas);

    // Create deck of cards
    cardBorderPaint.setARGB(255, 0, 0, 0);
    cardFrontPaint.setARGB(255, 255, 255, 255);
    RectF pos = new RectF();
    for (int suitIdx = 0; suitIdx < 4; suitIdx++) {
      for (int valueIdx = 0; valueIdx < 13; valueIdx++) {
        mCardBitmap[suitIdx*13+valueIdx] = Bitmap.createBitmap(
            width, height, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(mCardBitmap[suitIdx*13+valueIdx]);
        // Draw card outline
        for (int k = 0; k < cardOutline; k++) {
          pos.set(k, k, width-k, height-k);
          canvas.drawRoundRect(pos, roundEdge, roundEdge, cardBorderPaint);
        }
        pos.set(cardOutline, cardOutline, width-cardOutline, height-cardOutline);
        canvas.drawRoundRect(pos, roundEdge, roundEdge, cardFrontPaint);

        if ((suitIdx & 1) == 1) {
          // Draw upper-left red card number
          canvas.drawBitmap(redFont[valueIdx],
            cardOutline+offset, roundEdge,
            mSuitPaint);
          // Draw lower-right red card number
          canvas.drawBitmap(revRedFont[valueIdx],
            width-fontWidth-cardOutline-offset, height-fontHeight-roundEdge,
            mSuitPaint);
        } else {
          // Draw upper-left black card number
          canvas.drawBitmap(blackFont[valueIdx],
            cardOutline+offset, roundEdge,
            mSuitPaint);
          // Draw lower-right black card number
          canvas.drawBitmap(revBlackFont[valueIdx],
            width-fontWidth-cardOutline-offset, height-fontHeight-roundEdge,
            mSuitPaint);
        }

	// Draw small suit centered below upper-left card number
        canvas.drawBitmap(smallSuit[suitIdx],
          cardOutline+offset+(fontWidth-smallSuitWidth)/2,
          roundEdge+fontHeight+offset*2,
          mSuitPaint);
	// Draw small suit centered above lower-right card number
        canvas.drawBitmap(revSmallSuit[suitIdx],
           width-cardOutline-offset-fontWidth+(fontWidth-smallSuitWidth)/2,
           height-roundEdge-fontHeight-offset*2-smallSuitHeight,
           mSuitPaint);

        // Add multiple suits positioned geometrically on card
        int[] suitX = {borderWidth, (width-suitWidth)/2, width-(suitWidth+borderWidth)};
        int[] suitY = {borderHeight,
                       borderHeight+((height-(borderHeight*2 + suitHeight))/3)*1,
                       borderHeight+((height-(borderHeight*2 + suitHeight))/3)*2,
                       height-(suitHeight+borderHeight)};
        int suitMidY = (height-suitHeight)/2;
        switch (valueIdx+1) {
          case 1:
            canvas.drawBitmap(suit[suitIdx], suitX[1], suitMidY, mSuitPaint);
            break;
          case 2:
            canvas.drawBitmap(suit[suitIdx], suitX[1], suitY[0], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[1], suitY[3], mSuitPaint);
            break;
          case 3:
            canvas.drawBitmap(suit[suitIdx], suitX[1], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[1], suitMidY, mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[1], suitY[3], mSuitPaint);
            break;
          case 4:
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], mSuitPaint);
            break;
          case 5:
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[1], suitMidY, mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], mSuitPaint);
            break;
          case 6:
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitMidY, mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitMidY, mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], mSuitPaint);
            break;
          case 7:
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitMidY, mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitMidY, mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[1], (suitMidY+suitY[0])/2, mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], mSuitPaint);
            break;
          case 8:
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitY[0], mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[0], suitMidY, mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[2], suitMidY, mSuitPaint);
            canvas.drawBitmap(suit[suitIdx], suitX[1], (suitMidY+suitY[0])/2, mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[0], suitY[3], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[2], suitY[3], mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[1], (suitY[3]+suitMidY)/2, mSuitPaint);
            break;
          case 9:
            for (int i = 0; i < 4; i++) {
              canvas.drawBitmap(suit[suitIdx], suitX[(i%2)*2], suitY[i/2], mSuitPaint);
              canvas.drawBitmap(revSuit[suitIdx], suitX[(i%2)*2], suitY[i/2+2], mSuitPaint);
            }
            canvas.drawBitmap(suit[suitIdx], suitX[1], suitMidY, mSuitPaint);
            break;
          case 10:
            for (int i = 0; i < 4; i++) {
              canvas.drawBitmap(suit[suitIdx], suitX[(i%2)*2], suitY[i/2], mSuitPaint);
              canvas.drawBitmap(revSuit[suitIdx], suitX[(i%2)*2], suitY[i/2+2], mSuitPaint);
            }
            canvas.drawBitmap(suit[suitIdx], suitX[1], (suitY[1]+suitY[0])/2, mSuitPaint);
            canvas.drawBitmap(revSuit[suitIdx], suitX[1], (suitY[3]+suitY[2])/2, mSuitPaint);
            break;

          case Card.JACK:
            canvas.drawLines(faceBox, cardBorderPaint);
            if ((suitIdx & 1) == 1) {
              canvas.drawBitmap(redJack, borderWidth, borderHeight-1, mSuitPaint);
              canvas.drawBitmap(redRevJack, borderWidth, height-faceHeight-borderHeight+1, mSuitPaint);
            } else {
              canvas.drawBitmap(blackJack, borderWidth, borderHeight-1, mSuitPaint);
              canvas.drawBitmap(blackRevJack, borderWidth, height-faceHeight-borderHeight+1, mSuitPaint);
            }
            break;
          case Card.QUEEN:
            canvas.drawLines(faceBox, cardBorderPaint);
            if ((suitIdx & 1) == 1) {
              canvas.drawBitmap(redQueen, borderWidth, borderHeight-1, mSuitPaint);
              canvas.drawBitmap(redRevQueen, borderWidth, height-faceHeight-borderHeight+1, mSuitPaint);
            } else {
              canvas.drawBitmap(blackQueen, borderWidth, borderHeight-1, mSuitPaint);
              canvas.drawBitmap(blackRevQueen, borderWidth, height-faceHeight-borderHeight+1, mSuitPaint);
            }
            break;
          case Card.KING:
            canvas.drawLines(faceBox, cardBorderPaint);
            if ((suitIdx & 1) == 1) {
              canvas.drawBitmap(redKing, borderWidth, borderHeight-1, mSuitPaint);
              canvas.drawBitmap(redRevKing, borderWidth, height-faceHeight-borderHeight+1, mSuitPaint);
            } else {
              canvas.drawBitmap(blackKing, borderWidth, borderHeight-1, mSuitPaint);
              canvas.drawBitmap(blackRevKing, borderWidth, height-faceHeight-borderHeight+1, mSuitPaint);
            }
            break;
        }

        // Add suit to face cards (see faceBox boundaries above)
        if (valueIdx >= 10) {
          // Draw suit in upper-left
          canvas.drawBitmap(suit[suitIdx], borderWidth, borderHeight, mSuitPaint);
          // Draw suit in lower-right
          canvas.drawBitmap(revSuit[suitIdx], width-(borderWidth+suitWidth), height-(borderHeight+suitHeight), mSuitPaint);
        }
      }
    }
  }

  public void DrawTime(Canvas canvas, int millis) {
    int seconds = (millis / 1000) % 60;
    int minutes = millis / 60000;
    if (seconds != mLastSeconds) {
      mLastSeconds = seconds;
      // String.format is insanely slow (~15ms)
      if (seconds < 10) {
        mTimeString = minutes + ":0" + seconds;
      } else {
        mTimeString = minutes + ":" + seconds;
      }
    }
    mTimePaint.setARGB(128, 0, 0, 0);
    canvas.drawText(mTimeString, mScreenWidth-9, mScreenHeight-9, mTimePaint);
    mTimePaint.setARGB(255, 0, 0, 0);
    canvas.drawText(mTimeString, mScreenWidth-10, mScreenHeight-10, mTimePaint);
  }

  public void DrawRulesString(Canvas canvas, String score) {
    mTimePaint.setARGB(255, 20, 20, 20);
    canvas.drawText(score, mScreenWidth-9, mScreenHeight-(18*mDpi/160)-9, mTimePaint);
    if (score.charAt(0) == '-') {
      mTimePaint.setARGB(255, 255, 0, 0);
    } else {
      mTimePaint.setARGB(255, 0, 0, 0);
    }
    canvas.drawText(score, mScreenWidth-10, mScreenHeight-(18*mDpi/160)-10, mTimePaint);

  }
}
