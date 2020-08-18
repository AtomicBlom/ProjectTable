package com.github.atomicblom.projecttable.client.mcgui.util;

public class Rectangle implements IReadableRectangle, IWriteableRectangle {
    private int x;
    private int y;
    private int width;
    private int height;

    public Rectangle() { }

    public Rectangle(int x, int y, int width, int height) {

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void setSize(IReadableDimension dimensions) {
        this.width = dimensions.getWidth();
        this.height = dimensions.getHeight();
    }

    @Override
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setLocation(IReadablePoint point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    @Override
    public void setBounds(IReadableRectangle bounds) {
        this.x = bounds.getX();
        this.y = bounds.getY();
        this.width = bounds.getWidth();
        this.height = bounds.getHeight();
    }

    public boolean contains(int xB, int yB, int widthB, int heightB) {
        int xA = this.x;
        int yA = this.y;
        int widthA = this.width;
        int heightA = this.height;

        if (xB < xA || yB < yA) {
            return false;
        }

        if ((widthA | heightA | widthB | heightB) < 0) {
            return false;
        }

        widthA += xA;
        widthB += xB;
        heightA += yA;
        heightB += yB;

        if (widthB <= xB) {
            if (widthA >= xA || widthB > widthA) return false;
        } else {
            if (widthA >= xA && widthB > widthA) return false;
        }

        if (heightB <= yB) {
            return heightA < yA && heightB <= heightA;
        } else {
            return heightA < yA || heightB <= heightA;
        }
    }

    public boolean contains(Point point) {
        return contains(point.getX(), point.getY());
    }

    private boolean contains(int xB, int yB) {
        int xA = this.x;
        int yA = this.y;
        int widthA = this.width;
        int heightA = this.height;

        if ((widthA | heightA) < 0) return false;
        if (xB < xA || yB < yA) return false;

        widthA += xA;
        heightA += yA;

        return ((widthA < xA || widthA > xB) && (heightA < yA || heightA > yB));
    }

    @Override
    public String toString() {
        return "{x:" + this.x + ",y:" + this.y + ",width:" + this.width + ",height:" + this.height + "}";
    }
}
