import java.util.*;
import core.GameState;
import games.sushigo.Card;
import games.sushigo.CardType;

public class Determinizer {

    private ProbabilityTracker probabilityTracker;
    private Random random;

    public Determinizer(ProbabilityTracker probabilityTracker) {
        this.probabilityTracker = probabilityTracker;
        this.random = new Random();
    }

    public Determinizer(ProbabilityTracker probabilityTracker, long seed) {
        this.probabilityTracker = probabilityTracker;
        this.random = new Random(seed);
    }

    public GameState determinize(GameState currentState) {
        GameState deterministicState = currentState.copy();
        
        int numPlayers = deterministicState.getNPlayers();
        int currentPlayer = deterministicState.getCurrentPlayer();
        
        List<Card> unknownCards = getUnknownCards();
        
        Collections.shuffle(unknownCards, random);
        
        int cardIndex = 0;
        for (int i = 0; i < numPlayers; i++) {
            if (i == currentPlayer) {
                continue;
            }
            
            int handSize = deterministicState.getPlayerHandSize(i);
            List<Card> assignedHand = new ArrayList<>();
            
            for (int j = 0; j < handSize && cardIndex < unknownCards.size(); j++) {
                assignedHand.add(unknownCards.get(cardIndex));
                cardIndex++;
            }
            
            setPlayerHand(deterministicState, i, assignedHand);
        }
        
        return deterministicState;
    }

    public List<GameState> generateMultipleDeterminizations(GameState currentState, int count) {
        List<GameState> determinizations = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            determinizations.add(determinize(currentState));
        }
        
        return determinizations;
    }

    private List<Card> getUnknownCards() {
        Map<CardType, Integer> totalCardCounts = initializeTotalCardCounts();
        Map<CardType, Integer> seenCardCounts = new HashMap<>();
        
        for (CardType type : totalCardCounts.keySet()) {
            seenCardCounts.put(type, 0);
        }
        
        Map<CardType, Double> probabilities = probabilityTracker.getProbabilities();
        
        for (CardType type : totalCardCounts.keySet()) {
            int total = totalCardCounts.get(type);
            double prob = probabilities.getOrDefault(type, 0.0);
            
            int totalUnknown = getTotalUnknownCardCount();
            int remaining = (int) Math.round(prob * totalUnknown);
            
            int seen = total - remaining;
            seenCardCounts.put(type, seen);
        }
        
        List<Card> unknownCards = new ArrayList<>();
        
        for (CardType type : totalCardCounts.keySet()) {
            int total = totalCardCounts.get(type);
            int seen = seenCardCounts.get(type);
            int unknown = total - seen;
            
            for (int i = 0; i < unknown; i++) {
                unknownCards.add(createCard(type));
            }
        }
        
        return unknownCards;
    }

    private Map<CardType, Integer> initializeTotalCardCounts() {
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

    private int getTotalUnknownCardCount() {
        Map<CardType, Integer> totalCardCounts = initializeTotalCardCounts();
        int total = 0;
        for (int count : totalCardCounts.values()) {
            total += count;
        }
        
        Map<CardType, Double> probabilities = probabilityTracker.getProbabilities();
        int seen = 0;
        for (CardType type : totalCardCounts.keySet()) {
            double prob = probabilities.getOrDefault(type, 0.0);
            if (prob == 0.0) {
                seen += totalCardCounts.get(type);
            }
        }
        
        return total - seen;
    }

    private Card createCard(CardType type) {
        return new Card(type);
    }

    private void setPlayerHand(GameState state, int playerId, List<Card> hand) {
        state.setPlayerHand(playerId, hand);
    }

    public GameState determinizeWithBias(GameState currentState, Map<CardType, Double> biasWeights) {
        GameState deterministicState = currentState.copy();
        
        int numPlayers = deterministicState.getNPlayers();
        int currentPlayer = deterministicState.getCurrentPlayer();
        
        List<Card> unknownCards = getUnknownCards();
        
        List<Card> biasedCards = applyBias(unknownCards, biasWeights);
        
        Collections.shuffle(biasedCards, random);
        
        int cardIndex = 0;
        for (int i = 0; i < numPlayers; i++) {
            if (i == currentPlayer) {
                continue;
            }
            
            int handSize = deterministicState.getPlayerHandSize(i);
            List<Card> assignedHand = new ArrayList<>();
            
            for (int j = 0; j < handSize && cardIndex < biasedCards.size(); j++) {
                assignedHand.add(biasedCards.get(cardIndex));
                cardIndex++;
            }
            
            setPlayerHand(deterministicState, i, assignedHand);
        }
        
        return deterministicState;
    }

    private List<Card> applyBias(List<Card> cards, Map<CardType, Double> biasWeights) {
        List<Card> biasedList = new ArrayList<>();
        
        for (Card card : cards) {
            double weight = biasWeights.getOrDefault(card.type, 1.0);
            int copies = (int) Math.max(1, Math.round(weight));
            
            for (int i = 0; i < copies; i++) {
                biasedList.add(card);
            }
        }
        
        return biasedList;
    }

    public void setSeed(long seed) {
        this.random = new Random(seed);
    }
}
