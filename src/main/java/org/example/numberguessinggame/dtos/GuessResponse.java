package org.example.numberguessinggame.dtos;

public record GuessResponse(
        boolean correct, int turnsRemaining, int score, String message) {}
