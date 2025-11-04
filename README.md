# Group AJ — Sushi Go! AI Agent

Sushi Go! agent for the TabletopGames.AI framework.  
It uses **IS-MCTS** with **determinization voting** and a **blended heuristic** (two heuristics combined).

Place these in your project at:  
`src/main/java/players/groupAJ/`

- MyAgent.java — main agent (BasicMCTS + determinization + blended heuristics)
- Determinizer.java — samples hidden hands (IS-MCTS)
- Heuristics.java — feature-based heuristic
- SushiGoHeuristic.java — counter-based heuristic
- (Optional) config.json — notes for parameters

> Heuristic Score:
> `score = immediate + combos + pudding + chopsticks + blocking - risk`


RUN:

 `evaluation.RunGames`  


```bash
# from the project root
gradlew clean build
gradlew run -PmainClass=evaluation.RunGames --args="--game SushiGo --nPlayers 3 --mode roundrobin --focusPlayer players.groupAJ.MyAgent --destDir results --addTimeStamp true"





