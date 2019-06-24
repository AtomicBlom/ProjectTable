package com.github.atomicblom.projecttable.gui;

public enum ModGUIs
{
    PROJECT_TABLE;

    private static final ModGUIs[] cache = values();

    public int getID()
    {
        // Not used for persistent data, so ordinal is perfect here!
        return ordinal();
    }

    public static ModGUIs fromId(int id)
    {
        return cache[id];
    }
}
