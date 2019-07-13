package com.github.atomicblom.projecttable.api.ingredient;

public class IngredientProblem {
    private final String id;
    private final String source;
    private final String message;

    public IngredientProblem(String id, String source, String message) {
        this.id = id;
        this.source = source;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }
}
