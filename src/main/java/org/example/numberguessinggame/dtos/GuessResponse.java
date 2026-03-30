package org.example.numberguessinggame.dtos;

/**
 * @param roundActive Whether a round is still in progress (secret not yet won).
 */
public record GuessResponse(
        boolean correct, int turnsRemaining, int score, boolean roundActive, String message) {}
