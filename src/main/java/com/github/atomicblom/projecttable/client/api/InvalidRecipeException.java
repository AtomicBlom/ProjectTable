package com.github.atomicblom.projecttable.client.api;

import com.github.atomicblom.projecttable.api.ingredient.IngredientProblem;

import java.util.Collection;
import java.util.List;

public class InvalidRecipeException extends RuntimeException {
    private final List<IngredientProblem> problems;

    public InvalidRecipeException(String message, List<IngredientProblem> problems) {
        super(message);
        this.problems = problems;
    }

    public Collection<IngredientProblem> getProblems() {
        return problems;
    }
}
