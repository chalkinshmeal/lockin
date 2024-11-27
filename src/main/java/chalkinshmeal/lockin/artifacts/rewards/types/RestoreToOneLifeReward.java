package chalkinshmeal.lockin.artifacts.rewards.types;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.artifacts.rewards.LockinReward;
import chalkinshmeal.lockin.artifacts.scoreboard.LockinScoreboard;
import chalkinshmeal.lockin.artifacts.team.LockinTeamHandler;
import chalkinshmeal.lockin.utils.LoggerUtils;

public class RestoreToOneLifeReward extends LockinReward {
    private final LockinTeamHandler lockinTeamHandler;
    private final LockinScoreboard lockinScoreboard;

    //---------------------------------------------------------------------------------------------
    // Constructor
    //---------------------------------------------------------------------------------------------
    public RestoreToOneLifeReward(JavaPlugin plugin, LockinTeamHandler lockinTeamHandler, LockinScoreboard lockinScoreboard) {
        super(plugin);
        this.lockinTeamHandler = lockinTeamHandler;
        this.lockinScoreboard = lockinScoreboard;
        this.description = "Have your score restored to 1 life";
    }

    //---------------------------------------------------------------------------------------------
    // Reward methods
    //---------------------------------------------------------------------------------------------
    public void giveReward(Player player) {
        LoggerUtils.info("Giving reward to player----------------------------------");
        this.lockinScoreboard.setScore(this.lockinTeamHandler.getTeamName(player), 1);
    }
}
