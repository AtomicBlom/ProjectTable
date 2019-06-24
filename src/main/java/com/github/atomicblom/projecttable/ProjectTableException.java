package com.github.atomicblom.projecttable;

/**
 * Created by steblo on 11/05/2015.
 */
public class ProjectTableException extends RuntimeException {
    public ProjectTableException() {
    }

    public ProjectTableException(String s) {
        super(s);
    }

    public ProjectTableException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ProjectTableException(Throwable throwable) {
        super(throwable);
    }

    public ProjectTableException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}