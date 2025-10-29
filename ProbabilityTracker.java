import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

public class ProbabilityTracker {

    private Set<Card> seenCards;
    private Map<CardType, Integer> totalCardCounts;

    public ProbabilityTracker(){

        this.seenCards = new HashSet<>();
        this.totalCardCounts = initializeCardCounts();
    }
    
    public void update(GameState state){
        

    }

    public Map<CardType, Double> getProbabilities(){

        return null;
    }

    private Map<CardType, Integer> initializeCardCounts(){
        Map<CardType, Integer> cardCounts = new HashMap<>();
        cardCounts.put(CardType.TEMPURA, 14);
        cardCounts.put(CardType.SASHIMI, 14);
        cardCounts.put(CardType.DUMPLING, 14);
        cardCounts.put(CardType.MAKI_ROLL_2, 12);
        cardCounts.put(CardType.MAKI_ROLL_3, 8);
        cardCounts.put(CardType.MAKI_ROLL_1, 6);
        cardCounts.put(CardType.SALMON_NIGIRI, 10);
        cardCounts.put(CardType.SQUID_NIGIRI, 5);
        cardCounts.put(CardType.EGG_NIGIRI, 5);
        cardCounts.put(CardType.PUDDING, 10);
        cardCounts.put(CardType.WASABI, 6);
        cardCounts.put(CardType.CHOPSTICKS, 4);     
        return cardCounts;
    }

    public void update(GameState state){
        
    }

    private int countSeenCards(CardType type){
        return 0;
    }

    private int get TotalUnknownCards(){
        return 1;
    }
    
}