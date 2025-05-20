package com.example.snake;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

public class AnimatedSprite {
    private int x;
    private int y;
    private int xStep;
    private int yStep;
    private Bitmap myPic;
    private int width;
    private int height;
    private int imgH;
    private int imgW;
    private Rect rect;

    AnimatedSprite(int xStart, int yStart, Bitmap pic, int scrW, int scrH){ //מאתחל אובייקט חדש עם מיקום התחלתי, תמונה (Bitmap), וממדי המסך לצורך חישוב גבולות.
        x=xStart;
        y=yStart;
        xStep=0;
        yStep=0;
        myPic=pic;
        width=scrW;
        height=scrH;
        imgH = pic.getHeight();
        imgW = pic.getWidth();
        Log.d("APV",x+" "+y+" "+scrW+" "+scrH);
        rect = new Rect();
        updateRect();
    }
    public void draw(Canvas canvas){ //מצייר את התמונה של ה-Sprite על ה-"Canvas" הנתון, במיקום (x,y) הנוכחי של ה-Sprite.
        canvas.drawBitmap(myPic,x,y,null);
    }

    // Getter for the x coordinate
    public int getX() {
        return x;
    }

    // Getter for the y coordinate
    public int getY() {
        return y;
    }

    // Setter for the x coordinate
    public void setX(int newX) {
        this.x = newX;
        updateRect();
    }

    // Setter for the y coordinate
    public void setY(int newY) {
        this.y = newY;
        updateRect();
    }

    public int getSpriteWidth() {
        return imgW; // Return the width of the image
    }

    public int getSpriteHeight() {
        return imgH; // Return the height of the image
    }

    public void setxStep(int xStep){
        this.xStep = xStep;
    }

    public void setyStep(int yStep){
        this.yStep=yStep;
    }
    public void move() { //מעדכi את מיקום ה-Sprite על פי ערכי הצעד שלו (xStep, yStep). כאשר ה-Sprite יוצא מצד אחד של המסך, הוא מופיע מחדש בצד הנגדי.

        x = x - xStep;
        if (x<=-500) x=width+500;
        y = y + yStep;
        if (y>height) y=0;
        updateRect();
    }
    public void setPic (Bitmap updPic){
        myPic = updPic;
    }
    // Getter for the myPic Bitmap
    public Bitmap getMyPic() {
        return myPic;
    }

    private void updateRect(){
        rect.set(x,y,x+imgW,y+imgH);
    }

    public boolean inRect(int x, int y){
        return  rect.contains(x,y);
    }

    public Rect getRect() {
        return rect;
    }

    public boolean intersect(AnimatedSprite other){//בודקת האם המלבן התוחם של ה-Sprite הנוכחי חותך מלבן התוחם של "AnimatedSprite" אחר.

        return rect.intersect(other.getRect());
    }
}