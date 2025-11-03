package players.groupAJ;

import core.AbstractGameState;
import core.interfaces.IStateHeuristic;
import core.components.Deck;
import games.sushigo.SGGameState;
import games.sushigo.cards.SGCard;

/**
 * Heuristics - Domain-Specific State Evaluation for Sushi Go!
 * Uses correct SGCard.SGCardType enum values.
 *
 * @author Group AJ
 */
public class Heuristics implements IStateHeuristic {

    private double currentScoreWeight = 1.0;
    private double setProgressWeight = 0.8;
    private double synergyWeight = 0.6;
    private double competitiveWeight = 0.7;
    private double potentialWeight = 0.5;

    public enum HeuristicVariant {
        SIMPLE, BALANCED, AGGRESSIVE, STRATEGIC
    }

    private HeuristicVariant variant = HeuristicVariant.BALANCED;

    public Heuristics() {
        setWeightsForVariant(HeuristicVariant.BALANCED);
    }

    public Heuristics(HeuristicVariant variant) {
        setWeightsForVariant(variant);
    }

    private void setWeightsForVariant(HeuristicVariant variant) {
        this.variant = variant;
        switch (variant) {
            case SIMPLE:
                currentScoreWeight = 1.0; setProgressWeight = 0.0; synergyWeight = 0.0;
                competitiveWeight = 0.0; potentialWeight = 0.0;
                break;
            case BALANCED:
                currentScoreWeight = 1.0; setProgressWeight = 0.8; synergyWeight = 0.6;
                competitiveWeight = 0.7; potentialWeight = 0.5;
                break;
            case AGGRESSIVE:
                currentScoreWeight = 1.2; setProgressWeight = 0.5; synergyWeight = 0.4;
                competitiveWeight = 0.9; potentialWeight = 0.3;
                break;
            case STRATEGIC:
                currentScoreWeight = 0.8; setProgressWeight = 1.2; synergyWeight = 1.0;
                competitiveWeight = 0.6; potentialWeight = 0.9;
                break;
        }
    }

    @Override
    public double evaluateState(AbstractGameState gs, int playerId) {
        if (!(gs instanceof SGGameState)) {
            return gs.getHeuristicScore(playerId);
        }

        SGGameState state = (SGGameState) gs;

        try {
            double score = currentScoreWeight * state.getGameScore(playerId);
            score += setProgressWeight * evaluateSetProgress(state, playerId);
            score += synergyWeight * evaluateSynergyPotential(state, playerId);
            score += competitiveWeight * evaluateCompetitivePosition(state, playerId);
            score += potentialWeight * evaluateFuturePotential(state, playerId);
            return score;
        } catch (Exception e) {
            return state.getGameScore(playerId);
        }
    }

    private int count(SGGameState state, int playerId, SGCard.SGCardType type) {
        try {
            return state.getPlayedCardTypes(type, playerId).getValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private double evaluateSetProgress(SGGameState state, int playerId) {
        try {
            double progress = 0.0;

            int tempura = count(state, playerId, SGCard.SGCardType.Tempura);
            if (tempura % 2 == 1) progress += 2.5;

            int sashimi = count(state, playerId, SGCard.SGCardType.Sashimi);
            if (sashimi % 3 == 1) progress += 3.0;
            else if (sashimi % 3 == 2) progress += 7.0;

            int dumpling = count(state, playerId, SGCard.SGCardType.Dumpling);
            if (dumpling > 0 && dumpling < 5) {
                int[] values = {0, 1, 3, 6, 10, 15};
                progress += (values[dumpling + 1] - values[dumpling]) * 0.5;
            }

            return progress;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double evaluateSynergyPotential(SGGameState state, int playerId) {
        try {
            double synergy = 0.0;

            int wasabi = count(state, playerId, SGCard.SGCardType.Wasabi);
            int nigiri = count(state, playerId, SGCard.SGCardType.SquidNigiri) +
                    count(state, playerId, SGCard.SGCardType.SalmonNigiri) +
                    count(state, playerId, SGCard.SGCardType.EggNigiri);

            if (wasabi > nigiri) synergy += (wasabi - nigiri) * 4.0;
            if (nigiri > wasabi) synergy -= (nigiri - wasabi) * 0.5;

            int chopsticks = count(state, playerId, SGCard.SGCardType.Chopsticks);
            synergy += chopsticks * 2.0;

            return synergy;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double evaluateCompetitivePosition(SGGameState state, int playerId) {
        return evaluateMakiPosition(state, playerId) + evaluatePuddingPosition(state, playerId);
    }

    private double evaluateMakiPosition(SGGameState state, int playerId) {
        try {
            // Maki cards have a count field (1, 2, or 3)
            int ourMaki = count(state, playerId, SGCard.SGCardType.Maki);

            int maxOpp = 0, secondMaxOpp = 0;

            for (int i = 0; i < state.getNPlayers(); i++) {
                if (i == playerId) continue;
                int oppMaki = count(state, i, SGCard.SGCardType.Maki);
                if (oppMaki > maxOpp) {
                    secondMaxOpp = maxOpp;
                    maxOpp = oppMaki;
                } else if (oppMaki > secondMaxOpp) {
                    secondMaxOpp = oppMaki;
                }
            }

            if (ourMaki > maxOpp) return 6.0;
            if (ourMaki == maxOpp && maxOpp > 0) return 4.0;
            if (ourMaki > secondMaxOpp) return 2.0;
            if (ourMaki == secondMaxOpp && secondMaxOpp > 0) return 1.5;
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double evaluatePuddingPosition(SGGameState state, int playerId) {
        try {
            int round = state.getRoundCounter();
            int ourPudding = count(state, playerId, SGCard.SGCardType.Pudding);

            int minOpp = Integer.MAX_VALUE, maxOpp = 0;
            for (int i = 0; i < state.getNPlayers(); i++) {
                if (i == playerId) continue;
                int p = count(state, i, SGCard.SGCardType.Pudding);
                minOpp = Math.min(minOpp, p);
                maxOpp = Math.max(maxOpp, p);
            }

            double weight = round / 3.0;
            double score = 0.0;

            if (ourPudding > maxOpp) score += 6.0 * weight;
            else if (ourPudding == maxOpp && maxOpp > 0) score += 3.0 * weight;
            if (ourPudding < minOpp) score -= 6.0 * weight;

            return score;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double evaluateFuturePotential(SGGameState state, int playerId) {
        try {
            double potential = 0.0;
            Deck<SGCard> hand = state.getPlayerHands().get(playerId);
            if (hand != null) potential += hand.getSize() * 0.5;

            int round = state.getRoundCounter();
            if (round == 0) potential += 3.0;
            else if (round == 1) potential += 1.5;

            return potential;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public void setVariant(HeuristicVariant variant) {
        setWeightsForVariant(variant);
    }

    public HeuristicVariant getVariant() {
        return variant;
    }
}