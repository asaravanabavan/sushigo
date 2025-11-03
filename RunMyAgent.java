package players.groupAJ;



public class RunMyAgent {
    public static void main(String[] args) {
        String[] gameArgs = {
                "-g", "SushiGo",
                "-p", "groupAJ.MyAgent,Random,Random",
                "-n", "5"
        };

        try {
            core.Game.main(gameArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}