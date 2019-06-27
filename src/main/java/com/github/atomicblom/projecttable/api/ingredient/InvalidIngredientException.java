package com.github.atomicblom.projecttable.api.ingredient;

public class InvalidIngredientException extends RuntimeException {
    private final String id;
    private final String source;

    public InvalidIngredientException(String id, String source, String message) {
        super(message + " in " + source + " (" + id + ")");
        this.id = id;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }
}
