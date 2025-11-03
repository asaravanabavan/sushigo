package players.groupAJ;

import games.sushigo.SGGameState;

/**
 * Determinizer - Samples opponent hands for IS-MCTS
 *
 * Uses the built-in copy(playerId) method from SGGameState
 * which re-randomizes hidden information for that player's perspective.
 *
 * @author Group AJ
 */
public class Determinizer {

    public Determinizer() {
        // No parameters needed - using built-in TAG framework methods
    }

    /**
     * Determinizes the Sushi Go! state for IS-MCTS.
     * Creates a copy of the state with re-randomized opponent hands.
     *
     * @param currentState The current game state
     * @param playerId The player whose perspective to determinize from
     * @return A new state with sampled opponent hands
     */
    public SGGameState determinize(SGGameState currentState, int playerId) {
        // Deep copy with re-randomized hidden info using TAG's built-in method
        // This automatically samples new opponent hands from the deck
        SGGameState copy = (SGGameState) currentState.copy(playerId);

        return copy;
    }
}