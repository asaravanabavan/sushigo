package players.groupAJ;

import games.sushigo.SGGameState;
import games.sushigo.cards.SGCard;
import core.components.Deck;

/**
 * Heuristics - Domain-Specific State Evaluation for Sushi Go!
 * Evaluates game states to guide MCTS search with multi-factor scoring.
 *
 * @author Group AJ
 */
public class Heuristics {

    private double currentScoreWeight = 1.0;
    private double setProgressWeight = 0.8;
    private double synergyWeight = 0.6;
    private double competitiveWeight = 0.7;
    private double potentialWeight = 0.5;

    public enum HeuristicVariant {
        SIMPLE,      // Current score only
        BALANCED,    // All factors weighted equally
        AGGRESSIVE,  // Favor immediate points
        STRATEGIC    // Favor set completion
    }

    private HeuristicVariant variant = HeuristicVariant.BALANCED;

    public Heuristics() {
        this.variant = HeuristicVariant.BALANCED;
        setWeightsForVariant(variant);
    }

    public Heuristics(HeuristicVariant variant) {
        this.variant = variant;
        setWeightsForVariant(variant);
    }

    private void setWeightsForVariant(HeuristicVariant variant) {
        switch (variant) {
            case SIMPLE:
                currentScoreWeight = 1.0;
                setProgressWeight = 0.0;
                synergyWeight = 0.0;
                competitiveWeight = 0.0;
                potentialWeight = 0.0;
                break;

            case BALANCED:
                currentScoreWeight = 1.0;
                setProgressWeight = 0.8;
                synergyWeight = 0.6;
                competitiveWeight = 0.7;
                potentialWeight = 0.5;
                break;

            case AGGRESSIVE:
                currentScoreWeight = 1.2;
                setProgressWeight = 0.5;
                synergyWeight = 0.4;
                competitiveWeight = 0.9;
                potentialWeight = 0.3;
                break;

            case STRATEGIC:
                currentScoreWeight = 0.8;
                setProgressWeight = 1.2;
                synergyWeight = 1.0;
                competitiveWeight = 0.6;
                potentialWeight = 0.9;
                break;
        }
    }

    /**
     * Evaluate game state from player's perspective
     * Returns higher score for better positions
     */
    public double evaluateState(SGGameState state, int playerId) {
        if (state == null || playerId < 0 || playerId >= state.getNPlayers()) {
            return 0.0;
        }

        double score = 0.0;

        score += currentScoreWeight * state.getGameScore(playerId);
        score += setProgressWeight * evaluateSetProgress(state, playerId);
        score += synergyWeight * evaluateSynergyPotential(state, playerId);
        score += competitiveWeight * evaluateCompetitivePosition(state, playerId);
        score += potentialWeight * evaluateFuturePotential(state, playerId);

        return score;
    }

    // Evaluate partial set completion (Tempura, Sashimi, Dumpling)
    private double evaluateSetProgress(SGGameState state, int playerId) {
        double progress = 0.0;
        Deck<SGCard> ourBoard = state.getPlayerBoards().get(playerId);

        int tempuraCount = countCardType(ourBoard, "Tempura");
        if (tempuraCount == 1) progress += 2.5;

        int sashimiCount = countCardType(ourBoard, "Sashimi");
        if (sashimiCount == 1) progress += 3.0;
        else if (sashimiCount == 2) progress += 7.0;

        int dumplingCount = countCardType(ourBoard, "Dumpling");
        if (dumplingCount > 0) {
            int currentValue = getDumplingValue(dumplingCount);
            int nextValue = getDumplingValue(dumplingCount + 1);
            progress += (nextValue - currentValue) * 0.5;
        }

        return progress;
    }

    private int getDumplingValue(int count) {
        switch (count) {
            case 0: return 0;
            case 1: return 1;
            case 2: return 3;
            case 3: return 6;
            case 4: return 10;
            default: return 15;
        }
    }

    // Evaluate Wasabi-Nigiri synergy potential
    private double evaluateSynergyPotential(SGGameState state, int playerId) {
        double synergy = 0.0;
        Deck<SGCard> ourBoard = state.getPlayerBoards().get(playerId);

        int wasabiCount = countCardType(ourBoard, "Wasabi");
        int nigiriCount = countCardType(ourBoard, "Squid") +
                countCardType(ourBoard, "Salmon") +
                countCardType(ourBoard, "Egg");

        int unusedWasabi = Math.max(0, wasabiCount - nigiriCount);
        if (unusedWasabi > 0) synergy += unusedWasabi * 4.0;

        int unpairedNigiri = Math.max(0, nigiriCount - wasabiCount);
        if (unpairedNigiri > 0) synergy -= unpairedNigiri * 0.5;

        int chopsticksCount = countCardType(ourBoard, "Chopsticks");
        if (chopsticksCount > 0) synergy += chopsticksCount * 2.0;

        return synergy;
    }

    // Evaluate competitive position (Maki race, Pudding standings)
    private double evaluateCompetitivePosition(SGGameState state, int playerId) {
        return evaluateMakiPosition(state, playerId) +
                evaluatePuddingPosition(state, playerId);
    }

    private double evaluateMakiPosition(SGGameState state, int playerId) {
        int ourMaki = countTotalMaki(state.getPlayerBoards().get(playerId));

        int maxOpponentMaki = 0;
        int secondMaxOpponentMaki = 0;

        for (int i = 0; i < state.getNPlayers(); i++) {
            if (i == playerId) continue;

            int opponentMaki = countTotalMaki(state.getPlayerBoards().get(i));
            if (opponentMaki > maxOpponentMaki) {
                secondMaxOpponentMaki = maxOpponentMaki;
                maxOpponentMaki = opponentMaki;
            } else if (opponentMaki > secondMaxOpponentMaki) {
                secondMaxOpponentMaki = opponentMaki;
            }
        }

        if (ourMaki > maxOpponentMaki) return 6.0;
        else if (ourMaki == maxOpponentMaki && maxOpponentMaki > 0) return 4.0;
        else if (ourMaki > secondMaxOpponentMaki) return 2.0;
        else if (ourMaki == secondMaxOpponentMaki && secondMaxOpponentMaki > 0) return 1.5;
        else return 0.0;
    }

    private double evaluatePuddingPosition(SGGameState state, int playerId) {
        int currentRound = state.getRoundCounter();
        int ourPudding = countCardType(state.getPlayerBoards().get(playerId), "Pudding");

        int minOpponentPudding = Integer.MAX_VALUE;
        int maxOpponentPudding = 0;

        for (int i = 0; i < state.getNPlayers(); i++) {
            if (i == playerId) continue;

            int opponentPudding = countCardType(state.getPlayerBoards().get(i), "Pudding");
            minOpponentPudding = Math.min(minOpponentPudding, opponentPudding);
            maxOpponentPudding = Math.max(maxOpponentPudding, opponentPudding);
        }

        double roundWeight = currentRound / 3.0;
        double puddingScore = 0.0;

        if (ourPudding > maxOpponentPudding) {
            puddingScore = 6.0 * roundWeight;
        } else if (ourPudding == maxOpponentPudding && maxOpponentPudding > 0) {
            puddingScore = 3.0 * roundWeight;
        } else if (ourPudding < minOpponentPudding) {
            puddingScore = -6.0 * roundWeight;
        }

        return puddingScore;
    }

    // Evaluate future scoring potential based on hand size and game phase
    private double evaluateFuturePotential(SGGameState state, int playerId) {
        double potential = 0.0;

        Deck<SGCard> hand = state.getPlayerHands().get(playerId);
        if (hand != null && hand.getSize() > 0) {
            potential += hand.getSize() * 0.5;
        }

        int currentRound = state.getRoundCounter();
        if (currentRound == 1) potential += 3.0;
        else if (currentRound == 2) potential += 1.5;

        return potential;
    }

    // Helper methods
    private int countCardType(Deck<SGCard> deck, String cardType) {
        if (deck == null) return 0;
        int count = 0;
        for (int i = 0; i < deck.getSize(); i++) {
            SGCard card = deck.get(i);
            if (card != null && card.cardType.toString().equals(cardType)) {
                count++;
            }
        }
        return count;
    }

    private int countTotalMaki(Deck<SGCard> deck) {
        if (deck == null) return 0;
        int total = 0;
        for (int i = 0; i < deck.getSize(); i++) {
            SGCard card = deck.get(i);
            if (card != null) {
                String type = card.cardType.toString();
                if (type.equals("Maki1")) total += 1;
                else if (type.equals("Maki2")) total += 2;
                else if (type.equals("Maki3")) total += 3;
            }
        }
        return total;
    }

    // Configuration methods
    public void setVariant(HeuristicVariant variant) {
        this.variant = variant;
        setWeightsForVariant(variant);
    }

    public HeuristicVariant getVariant() {
        return variant;
    }

    public void setWeights(double current, double setProgress, double synergy,
                           double competitive, double potential) {
        this.currentScoreWeight = current;
        this.setProgressWeight = setProgress;
        this.synergyWeight = synergy;
        this.competitiveWeight = competitive;
        this.potentialWeight = potential;
    }
}