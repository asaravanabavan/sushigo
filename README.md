# sushigo
# Group AJ – Sushi Go! AI Agent  
A SushiGo agent developed in Java 

1. Place the files
Copy all files into your project under:
TabletopGames/
src/main/java/players/groupAJ/
MyAgent.java
Determinizer.java
Heuristics.java
SushiGoHeuristic.java
config.json (optional)

score = immediatePoints
      + comboValue
      + puddingValue
      + chopsticksValue
      + blocking
      - riskPenalty
1) Quick presets
Each heuristic has built-in variants:

java
Options: SIMPLE, BALANCED, AGGRESSIVE, STRATEGIC
public Heuristics() { setVariant(HeuristicVariant.BALANCED); }
public SushiGoHeuristic() { setVariant(HeuristicVariant.BALANCED); }
Change the variant name to try different playstyles.

setWeights(1.0, 1.5, 2.0, 1.2, 0.8, 0.5);
Blending Both Heuristics
Inside MyAgent.java:
private int numDeterminizations = 5; // raise to 7–9 for stronger, lower to 3 for faster

p.rolloutLength = 12; // try 8–12 if timing is tight
p.maxTreeDepth  = 10; // try 8–12
p.K = Math.sqrt(2);


At the top of either heuristic file:
private double wImmediate=1.0, wCombo=1.5, wPudding=2.0, wChopsticks=1.2, wBlocking=0.8, wRisk=0.5;
this.blendedHeu = new BlendedHeuristic(classicHeu, sushiHeu, 0.5);
The 0.5 value sets the blend ratio:
1.0 → only use Heuristics
0.0 → only use SushiGoHeuristic
0.5 → equal mix (default)

Run:
gradlew run -PmainClass=evaluation.RunGames --args="--game SushiGo --nPlayers 3 --mode roundrobin --focusPlayer players.groupAJ.MyAgent --destDir results --addTimeStamp true"
or
--game SushiGo --nPlayers 3 --mode roundrobin --focusPlayer players.groupAJ.MyAgent --destDir results --addTimeStamp true



Description
MyAgent.java	Main agent (BasicMCTS + determinization + heuristic blending)
Heuristics.java	Classic heuristic (explicit card evaluation)
SushiGoHeuristic.java	Counter-based heuristic for stability
Determinizer.java	Samples opponent hands for IS-MCTS
config.json	Optional – records parameter notes




