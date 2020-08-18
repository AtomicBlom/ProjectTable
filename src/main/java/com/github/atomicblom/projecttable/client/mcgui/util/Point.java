package com.github.atomicblom.projecttable.client.mcgui.util;

public class Point implements IReadablePoint, IWritablePoint {
    private int x;
    private int y;

    public Point() { }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(IReadablePoint point) {
        this.x = point.getX();
        this.y = point.getY();
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
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setLocation(IReadablePoint point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    public void untranslate(IReadablePoint point) {
        this.x -= point.getX();
        this.y -= point.getY();
    }

    public void translate(IReadablePoint point) {
        this.x += point.getX();
        this.y += point.getY();
    }

    @Override
    public String toString() {
        return "{x:" + this.x + ",y:" + this.y + "}";
    }
}
