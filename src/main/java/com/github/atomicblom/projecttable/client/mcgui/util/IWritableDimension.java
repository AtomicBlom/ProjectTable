package com.github.atomicblom.projecttable.client.mcgui.util;

public interface IWritableDimension {
    void setSize(int width, int height);

    void setSize(IReadableDimension dimensions);
}
