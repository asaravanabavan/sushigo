package players.groupAJ;

import core.AbstractGameState;
import core.interfaces.IStateHeuristic;
import games.sushigo.SGGameState;
import games.sushigo.cards.SGCard;

/**
 * SushiGoHeuristic - Counter-based evaluation with correct card types
 *
 * @author Group AJ
 */
public class SushiGoHeuristic implements IStateHeuristic {

    private double wImmediate = 1.0;
    private double wCombo = 1.0;
    private double wPudding = 1.0;
    private double wChopsticks = 1.0;

    public enum HeuristicVariant {
        SIMPLE,
        BALANCED,
        AGGRESSIVE,
        STRATEGIC
    }

    private HeuristicVariant variant = HeuristicVariant.BALANCED;

    public SushiGoHeuristic() {
        setVariant(HeuristicVariant.BALANCED);
    }

    public SushiGoHeuristic(HeuristicVariant v) {
        setVariant(v);
    }

    public void setVariant(HeuristicVariant v) {
        variant = v;
        switch (v) {
            case SIMPLE:
                wImmediate = 1.0; wCombo = 0; wPudding = 0; wChopsticks = 0;
                break;
            case BALANCED:
                wImmediate = 1.0; wCombo = 1.0; wPudding = 1.0; wChopsticks = 1.0;
                break;
            case AGGRESSIVE:
                wImmediate = 1.2; wCombo = 0.8; wPudding = 0.7; wChopsticks = 1.0;
                break;
            case STRATEGIC:
                wImmediate = 0.9; wCombo = 1.2; wPudding = 1.1; wChopsticks = 1.0;
                break;
        }
    }

    @Override
    public double evaluateState(AbstractGameState gs, int playerId) {
        if (!(gs instanceof SGGameState)) {
            return gs.getHeuristicScore(playerId);
        }

        SGGameState s = (SGGameState) gs;

        double immediate = s.getGameScore(playerId);
        double combo = comboPotential(s, playerId);
        double pudding = puddingHeuristic(s, playerId);
        double chopsticks = chopsticksHeuristic(s, playerId);

        return wImmediate * immediate
                + wCombo * combo
                + wPudding * pudding
                + wChopsticks * chopsticks;
    }

    private int count(SGGameState s, int pid, SGCard.SGCardType t) {
        try {
            return s.getPlayedCardTypes(t, pid).getValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private double comboPotential(SGGameState s, int pid) {
        double v = 0;

        int temp = count(s, pid, SGCard.SGCardType.Tempura);
        if (temp % 2 == 1) v += 2.5;

        int sash = count(s, pid, SGCard.SGCardType.Sashimi);
        int sashRem = sash % 3;
        if (sashRem == 1) v += 3.3;
        else if (sashRem == 2) v += 6.6;

        int dump = count(s, pid, SGCard.SGCardType.Dumpling);
        if (dump > 0 && dump <= 5) {
            int[] marginal = {1, 2, 3, 4, 5};
            v += marginal[Math.min(dump - 1, 4)];
        }

        int was = count(s, pid, SGCard.SGCardType.Wasabi);
        v += 3.0 * was;

        return v;
    }

    private double puddingHeuristic(SGGameState s, int pid) {
        int me = count(s, pid, SGCard.SGCardType.Pudding);

        int maxOpp = Integer.MIN_VALUE;
        int minOpp = Integer.MAX_VALUE;

        for (int i = 0; i < s.getNPlayers(); i++) {
            if (i != pid) {
                int p = count(s, i, SGCard.SGCardType.Pudding);
                maxOpp = Math.max(maxOpp, p);
                minOpp = Math.min(minOpp, p);
            }
        }

        int round = s.getRoundCounter() + 1;
        double weight = (round == 1 ? 0.5 : round == 2 ? 1.0 : 2.0);

        double val = 0;
        if (me > maxOpp) val += (me - maxOpp) * 2.0 * weight;
        if (me <= minOpp) val -= (minOpp - me + 1) * 2.0 * weight;

        return val;
    }

    private double chopsticksHeuristic(SGGameState s, int pid) {
        int chopsticks = count(s, pid, SGCard.SGCardType.Chopsticks);
        return chopsticks * 1.5;
    }
}