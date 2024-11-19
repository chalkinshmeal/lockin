package chalkinshmeal.lockin.artifacts.scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import chalkinshmeal.lockin.artifacts.team.LockinTeamHandler;
import chalkinshmeal.lockin.data.ConfigHandler;
import chalkinshmeal.lockin.utils.LoggerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LockinScoreboard {
    private final JavaPlugin plugin;
    private final ConfigHandler configHandler;
    private final Scoreboard scoreboard;
    private final Objective objective;
    private final Map<String, Score> teamScores;
    private LockinTeamHandler lockinTeamHandler;
    private int maxLives;
    private final boolean debug = false;

    @SuppressWarnings("deprecation")
    public LockinScoreboard(JavaPlugin plugin, ConfigHandler configHandler) {
        this.plugin = plugin;
        this.configHandler = configHandler;

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        this.scoreboard = manager.getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("scores", "dummy", Component.text("Lives", NamedTextColor.GOLD));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.teamScores = new HashMap<>();
        this.maxLives = this.configHandler.getInt("maxLives", 1);
    }

    //-------------------------------------------------------------------------
    // Accessor/Mutator methods
    //-------------------------------------------------------------------------
    public boolean atMostOneTeamHasPositiveLives() {
        int positiveTeams = 0;
        for (String teamName : this.getTeamNames()) {
            if (this.getScore(teamName) > 0) positiveTeams += 1;
        }
        return positiveTeams <= 1;
    }

    // Get the names of the winning teams
    // This should be the actual team names, not the display names
    public List<String> getWinningTeams() {
        List<String> winningTeams = new ArrayList<>();
        int maxScore = this.getMaxScore();
        for (String teamName : this.lockinTeamHandler.getTeamNames()) {
            String displayTeamName = this.lockinTeamHandler.getDisplayTeamName(teamName);
            if (this.getScore(displayTeamName) == maxScore) winningTeams.add(teamName);
        }
        return winningTeams;
    }

    public int getMaxScore() {
        if (debug) LoggerUtils.info("Getting max score");
        int maxValue = Integer.MIN_VALUE;
        for (String teamName : this.getTeamNames()) {
            if (debug) LoggerUtils.info("Checking team " + teamName + ". Score: " + this.getScore(teamName));
            if (this.getScore(teamName) > maxValue) { maxValue = this.getScore(teamName); }
        }
        return maxValue;
    }

    public void init(LockinTeamHandler lockinTeamHandler) {
        this.lockinTeamHandler = lockinTeamHandler;

        for (String teamName : this.lockinTeamHandler.getTeamNames()) {
            for (UUID uuid : this.lockinTeamHandler.getTeamPlayers(teamName)) {
                Player player = this.plugin.getServer().getPlayer(uuid);
                this.addPlayerToTeam(player, teamName);
                this.showToPlayer(player);
            }
            this.setScore(teamName, this.maxLives);
        }
    }

    public int getNumTeams() {
        return this.scoreboard.getTeams().size();
    }

    public List<String> getTeamNames() {
        if (debug) LoggerUtils.info("Getting team names (Size: " + this.teamScores.size() + ")");
        List<String> teamNames = new ArrayList<>();
        for (String teamName : this.teamScores.keySet()) {
            if (debug) LoggerUtils.info("  " + teamName);
            teamNames.add(teamName);
        }
        return teamNames;
    }

    public void addPlayerToTeam(Player player, String teamName) {
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        team.addEntry(player.getName());
    }

    public void addScore(Player player, int scoreValue) {
        String teamName = this.lockinTeamHandler.getTeamName(player);
        if (teamName == null) return;

        Team team = scoreboard.getTeam(teamName);
        if (team == null || team.getEntries().size() == 0) {
            return;
        }

        String displayName = team.getEntries().size() == 1 ? team.getEntries().iterator().next() : teamName;
        Score score = teamScores.computeIfAbsent(displayName, k -> objective.getScore(displayName));
        score.setScore(score.getScore() + scoreValue);
    }

    public void addScore(String teamName, int scoreValue) {
        Team team = scoreboard.getTeam(teamName);
        if (team == null || team.getEntries().size() == 0) {
            return;
        }

        String displayName = team.getEntries().size() == 1 ? team.getEntries().iterator().next() : teamName;
        Score score = teamScores.computeIfAbsent(displayName, k -> objective.getScore(displayName));
        score.setScore(score.getScore() + scoreValue);
    }

    public void subtractScore(String teamName, int scoreValue) {
        Team team = scoreboard.getTeam(teamName);
        if (team == null || team.getEntries().size() == 0) {
            return;
        }

        String displayName = team.getEntries().size() == 1 ? team.getEntries().iterator().next() : teamName;
        Score score = teamScores.computeIfAbsent(displayName, k -> objective.getScore(displayName));
        score.setScore(score.getScore() - scoreValue);
    }

    public void setScore(String teamName, int scoreValue) {
        Team team = scoreboard.getTeam(teamName);
        if (team == null || team.getEntries().size() == 0) {
            return;
        }

        String displayName = team.getEntries().size() == 1 ? team.getEntries().iterator().next() : teamName;
        Score score = teamScores.computeIfAbsent(displayName, k -> objective.getScore(displayName));
        score.setScore(scoreValue);
    }

    public int getScore(String teamName) {
        Team team = scoreboard.getTeam(teamName);
        if (team == null || team.getEntries().size() == 0) {
            return -1;
        }

        String displayName = team.getEntries().size() == 1 ? team.getEntries().iterator().next() : teamName;
        Score score = teamScores.computeIfAbsent(displayName, k -> objective.getScore(displayName));
        return score.getScore();
    }

    public void clearScore(String teamName) {
        Team team = scoreboard.getTeam(teamName);
        if (team == null || team.getEntries().size() == 0) {
            return;
        }

        String displayName = team.getEntries().size() == 1 ? team.getEntries().iterator().next() : teamName;
        Score score = teamScores.remove(displayName);
        if (score != null) {
            objective.getScoreboard().resetScores(displayName);
        }
    }

    public void showToPlayer(Player player) {
        player.setScoreboard(scoreboard);
    }

    public void hideFromPlayer(Player player) {
        if (player.getScoreboard().equals(scoreboard)) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }
}