package com.github.atomicblom.projecttable.client.mcgui.util;

public interface IWriteableRectangle extends IWritablePoint, IWritableDimension {


    void setBounds(IReadableRectangle bounds);
}
