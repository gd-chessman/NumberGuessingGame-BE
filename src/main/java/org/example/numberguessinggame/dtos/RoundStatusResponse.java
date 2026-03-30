package org.example.numberguessinggame.dtos;

/**
 * @param roundActive {@code true} if a round is in progress (secret set); {@code false} if no round started
 *     yet or last round ended after a win.
 */
public record RoundStatusResponse(boolean roundActive, int turnsRemaining, int score) {}
