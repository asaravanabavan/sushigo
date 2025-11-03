package players.groupAJ;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IStateHeuristic;
import games.sushigo.SGGameState;
import players.basicMCTS.BasicMCTSParams;
import players.basicMCTS.BasicMCTSPlayer;

import java.util.*;

/**
 * MyAgent - BasicMCTS with determinization voting + blended heuristics.
 * Uses BOTH Heuristics and SushiGoHeuristic via a weighted combiner.
 *
 * COMPLIES WITH ASSIGNMENT RULES:
 * - Extends BasicMCTSPlayer (allowed)
 * - Implements determinization (IS-MCTS)
 * - Uses custom heuristics
 *
 * @author Josephine (Group AJ)
 */
public class MyAgent extends BasicMCTSPlayer {

    // Configuration
    private int numDeterminizations = 5;

    private final Random rnd;
    private final Determinizer determinizer;

    // Individual heuristics
    private final Heuristics classicHeu;
    private final SushiGoHeuristic sushiHeu;

    // Blended heuristic given to MCTS
    private final IStateHeuristic blendedHeu;

    public MyAgent() {
        this(System.currentTimeMillis());
    }

    public MyAgent(long seed) {
        super(makeParams());
        this.rnd = new Random(seed);

        // Determinization helper
        this.determinizer = new Determinizer();

        // Initialize both heuristics
        this.classicHeu = new Heuristics();
        this.sushiHeu   = new SushiGoHeuristic();

        // Blend them: 0.5 = equal weight
        this.blendedHeu = new BlendedHeuristic(classicHeu, sushiHeu, 0.5);

        // Set heuristic for MCTS
        this.setStateHeuristic(blendedHeu);
        this.setName("MyAgent");
    }

    private static BasicMCTSParams makeParams() {
        BasicMCTSParams p = new BasicMCTSParams();
        try { p.K = Math.sqrt(2); } catch (Throwable ignored) {}
        try { p.rolloutLength = 12; } catch (Throwable ignored) {}
        try { p.maxTreeDepth  = 10; } catch (Throwable ignored) {}
        try { p.epsilon = 1e-6; } catch (Throwable ignored) {}
        return p;
    }

    /**
     * Main decision method with determinization voting
     * Implements IS-MCTS (Cowling et al., 2012)
     */
    @Override
    public AbstractAction _getAction(AbstractGameState gs, List<AbstractAction> actions) {
        if (actions == null || actions.isEmpty()) {
            throw new IllegalStateException("MyAgent: no actions available");
        }
        if (actions.size() == 1) {
            return actions.get(0);
        }

        // Vote tracking
        Map<AbstractAction, Integer> votes = new HashMap<>();

        // Run multiple determinizations
        for (int i = 0; i < numDeterminizations; i++) {
            // Get determinized state (with sampled opponent hands)
            SGGameState det = determinizer.determinize((SGGameState) gs, getPlayerID());

            // Ask BasicMCTS with the blended heuristic
            AbstractAction choice = super._getAction(det, actions);

            // Record vote - Explicitly return Integer to satisfy strict type checking
            votes.merge(choice, Integer.valueOf(1), (oldVal, newVal) -> Integer.valueOf(oldVal + newVal));
        }

        // Return action with most votes
        return votes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new IllegalStateException("MyAgent: voting produced no action"))
                .getKey();
    }

    @Override
    public MyAgent copy() {
        MyAgent clone = new MyAgent(rnd.nextLong());
        clone.numDeterminizations = this.numDeterminizations;
        clone.setStateHeuristic(this.blendedHeu);
        clone.setForwardModel(getForwardModel());
        return clone;
    }

    public void setNumDeterminizations(int n) {
        this.numDeterminizations = Math.max(1, n);
    }

    @Override
    public String toString() {
        return "MyAgent";
    }

    /**
     * BlendedHeuristic - Combines two heuristics with weighted average
     */
    private static final class BlendedHeuristic implements IStateHeuristic {
        private final IStateHeuristic h1;
        private final IStateHeuristic h2;
        private final double alpha; // weight for h1 (0..1)

        BlendedHeuristic(IStateHeuristic h1, IStateHeuristic h2, double alpha) {
            this.h1 = h1;
            this.h2 = h2;
            this.alpha = Math.max(0.0, Math.min(1.0, alpha));
        }

        @Override
        public double evaluateState(AbstractGameState gs, int playerId) {
            double s1 = safeEval(h1, gs, playerId);
            double s2 = safeEval(h2, gs, playerId);
            return alpha * s1 + (1.0 - alpha) * s2;
        }

        private double safeEval(IStateHeuristic h, AbstractGameState gs, int playerId) {
            try {
                return h.evaluateState(gs, playerId);
            } catch (Throwable t) {
                return gs.getHeuristicScore(playerId);
            }
        }
    }
}


