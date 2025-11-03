package players.groupAJ;

import core.AbstractGameState;
import core.interfaces.IStateHeuristic;
import core.components.Deck;
import games.sushigo.SGGameState;
import games.sushigo.cards.SGCard;
import core.components.Counter;
/**

 * SushiGoHeuristic via a weighted combiner
 * author: Josephine
 */

public class SushiGoHeuristic implements IStateHeuristic {

    // weights
    private double wImmediate   = 1.0;
    private double wCombo       = 1.0;
    private double wPudding     = 1.0;
    private double wChopsticks  = 1.0;
    private double wBlocking    = 1.0;
    private double wRisk        = 1.0;

    public enum HeuristicVariant { SIMPLE, BALANCED, AGGRESSIVE, STRATEGIC }
    private HeuristicVariant variant = HeuristicVariant.BALANCED;

    public SushiGoHeuristic() { setVariant(HeuristicVariant.BALANCED); }
    public SushiGoHeuristic(HeuristicVariant v) { setVariant(v); }

    public void setVariant(HeuristicVariant v) {
        variant = v;
        switch (v) {
            case SIMPLE:    wImmediate=1.0; wCombo=0;   wPudding=0; wChopsticks=0; wBlocking=0; wRisk=0;   break;
            case BALANCED:  wImmediate=1.0; wCombo=1.0; wPudding=1.0; wChopsticks=1.0; wBlocking=1.0; wRisk=1.0; break;
            case AGGRESSIVE:wImmediate=1.2; wCombo=0.8; wPudding=0.7; wChopsticks=1.0; wBlocking=0.8; wRisk=1.0; break;
            case STRATEGIC: wImmediate=0.9; wCombo=1.2; wPudding=1.1; wChopsticks=1.0; wBlocking=1.0; wRisk=1.0; break;
        }
    }

    @Override
    public double evaluateState(AbstractGameState gs, int playerId) {
        if (!(gs instanceof SGGameState)) return gs.getHeuristicScore(playerId);
        SGGameState s = (SGGameState) gs;

        double immediatePoints = s.getGameScore(playerId);
        double comboValue      = comboPotential(s, playerId);
        double puddingValue    = puddingHeuristic(s, playerId);
        double chopsticksValue = chopsticksHeuristic(s, playerId);
        double blocking        = blockingHeuristic(s, playerId);
        double riskPenalty     = riskHeuristic(s, playerId);


        return  wImmediate  * immediatePoints
                + wCombo      * comboValue
                + wPudding    * puddingValue
                + wChopsticks * chopsticksValue
                + wBlocking   * blocking
                - wRisk       * riskPenalty;
    }

    // helpers

    private int count(SGGameState s, int pid, SGCard.SGCardType t) {
        Counter c = s.getPlayedCardTypes(t, pid);  // return core.components.Counter
        return c == null ? 0 : c.getValue();
    }

    private double comboPotential(SGGameState s, int pid) {
        int temp = count(s, pid, SGCard.SGCardType.Tempura);
        int sash = count(s, pid, SGCard.SGCardType.Sashimi);
        int dump = count(s, pid, SGCard.SGCardType.Dumpling);
        int was  = count(s, pid, SGCard.SGCardType.Wasabi);
        int usedWas = 0;

        double v = 0;

        // Tempura: 2 => 5 points
        if (temp % 2 == 1) v += 2.5;

        // Sashimi: 3 => 10 points
        int r = sash % 3;
        if (r == 1) v += 3.3;
        else if (r == 2) v += 6.6;

        // Dumpling increasing marginal
        int[] marg = {1,2,3,4,5};
        if (dump >= 0 && dump < marg.length) v += marg[dump];

        // Wasabi waits for Nigiri: expected ~4 each
        int unused = Math.max(0, was - usedWas);
        v += 4.0 * unused;

        return v;
    }

    private double puddingHeuristic(SGGameState s, int pid) {
        int me = count(s, pid, SGCard.SGCardType.Pudding);
        int n = s.getNPlayers();
        int maxO = Integer.MIN_VALUE, minO = Integer.MAX_VALUE;
        for (int i=0;i<n;i++) if (i!=pid) {
            int p = count(s, i, SGCard.SGCardType.Pudding);
            maxO = Math.max(maxO, p); minO = Math.min(minO, p);
        }
        int round = s.getRoundCounter()+1;
        double w = (round==1?0.5:round==2?1.0:2.0);        // later rounds matter more
        double val = 0;
        if (me > maxO) val += (me-maxO)*2.0*w;
        if (me <= minO) val -= (minO-me+1)*2.0*w;          // avoid last place
        return val;
    }

    private double chopsticksHeuristic(SGGameState s, int pid) {
        int csticks = count(s, pid, SGCard.SGCardType.Chopsticks);
        if (csticks == 0) return 0;
        return 1.5 * csticks;
    }

    private double blockingHeuristic(SGGameState s, int pid) {
        double v = 0;
        for (int i=0;i<s.getNPlayers();i++) if (i!=pid) {
            int t = count(s, i, SGCard.SGCardType.Tempura);
            if (t % 2 == 1) v += 1.0;
            int sh = count(s, i, SGCard.SGCardType.Sashimi);
            int r = sh % 3;
            if (r == 1 || r == 2) v += 1.5;
            int w = count(s, i, SGCard.SGCardType.Wasabi);
            if (w > 0) v += 1.0;
        }
        return v;
    }

    private double riskHeuristic(SGGameState s, int pid) {
        int hand = s.getPlayerHands().get(pid) == null ? 0 : s.getPlayerHands().get(pid).getSize();
        int temp = count(s, pid, SGCard.SGCardType.Tempura);
        int sash = count(s, pid, SGCard.SGCardType.Sashimi);
        int was  = count(s, pid, SGCard.SGCardType.Wasabi);

        double r = 0;
        if (temp % 2 == 1 && hand < 2) r += 2.0;
        int rem = 3 - (sash % 3);
        if (rem != 3 && hand < rem) r += 3.0;
        if (was > 0 && hand < 2) r += 1.5; // if wasabi unused
        return r;
    }

}
