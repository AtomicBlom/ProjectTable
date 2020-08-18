package com.github.atomicblom.projecttable.client.mcgui.util;

public interface IWritablePoint {
    void setLocation(int x, int y);

    void setLocation(IReadablePoint point);
}
