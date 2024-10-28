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

import chalkinshmeal.lockin.artifacts.team.lockinTeamHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class lockinScoreboard {
    private final JavaPlugin plugin;
    private final Scoreboard scoreboard;
    private final Objective objective;
    private final Map<String, Score> teamScores;
    private lockinTeamHandler lockinTeamHandler;

    @SuppressWarnings("deprecation")
    public lockinScoreboard(JavaPlugin plugin) {
        this.plugin = plugin;

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            throw new IllegalStateException("ScoreboardManager is not available");
        }

        this.scoreboard = manager.getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("scores", "dummy", Component.text("lockin", NamedTextColor.GOLD));
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.teamScores = new HashMap<>();
    }

    public void init(lockinTeamHandler lockinTeamHandler) {
        this.lockinTeamHandler = lockinTeamHandler;

        for (String teamName : this.lockinTeamHandler.getTeamNames()) {
            for (UUID uuid : this.lockinTeamHandler.getTeamPlayers(teamName)) {
                this.addPlayerToTeam(this.plugin.getServer().getPlayer(uuid), teamName);
            }
        }
    }

    public int getNumTeams() {
        return this.scoreboard.getTeams().size();
    }

    public List<String> getTeamNames() {
        List<String> teamNames = new ArrayList<>();
        for (String teamName : this.teamScores.keySet()) {
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
        System.out.println("[lockinScoreboard::addScore] Player: " + player + " Point: " + scoreValue);
        String teamName = this.lockinTeamHandler.getTeamName(player);
        System.out.println("[lockinScoreboard::addScore] Fetching team name: " + teamName);
        System.out.println("[lockinScoreboard::addScore] HERE1");
        if (teamName == null) return;
        System.out.println("[lockinScoreboard::addScore] HERE2");

        Team team = scoreboard.getTeam(teamName);
        System.out.println("[lockinScoreboard::addScore] HERE3");
        if (team == null || team.getEntries().size() == 0) {
            System.out.println("[lockinScoreboard::addScore] HERE3a");
            return;
        }
        System.out.println("[lockinScoreboard::addScore] HERE4");

        String displayName = team.getEntries().size() == 1 ? team.getEntries().iterator().next() : teamName;
        System.out.println("[lockinScoreboard::addScore] HERE5");
        Score score = teamScores.computeIfAbsent(displayName, k -> objective.getScore(displayName));
        System.out.println("[lockinScoreboard::addScore] HERE6");
        score.setScore(score.getScore() + scoreValue);
        System.out.println("[lockinScoreboard::addScore] HERE7");
    }

    public void addScore(String teamName, int scoreValue) {
        System.out.println("[lockinScoreboard::addScore] Team: " + teamName + " Point: " + scoreValue);
        Team team = scoreboard.getTeam(teamName);
        System.out.println("[lockinScoreboard::addScore] HERE1");
        if (team == null || team.getEntries().size() == 0) {
            System.out.println("[lockinScoreboard::addScore] HERE1a");
            return;
        }
        System.out.println("[lockinScoreboard::addScore] HERE2");

        String displayName = team.getEntries().size() == 1 ? team.getEntries().iterator().next() : teamName;
        System.out.println("[lockinScoreboard::addScore] HERE3");
        Score score = teamScores.computeIfAbsent(displayName, k -> objective.getScore(displayName));
        System.out.println("[lockinScoreboard::addScore] HERE4");
        score.setScore(score.getScore() + scoreValue);
        System.out.println("[lockinScoreboard::addScore] HERE5");
    }

    public void setScore(String teamName, int scoreValue) {
        System.out.println("[lockinScoreboard::setScore] Team: " + teamName + " Point: " + scoreValue);
        Team team = scoreboard.getTeam(teamName);
        System.out.println("[lockinScoreboard::addScore] HERE1");
        if (team == null || team.getEntries().size() == 0) {
            System.out.println("[lockinScoreboard::addScore] HERE1a");
            return;
        }
        System.out.println("[lockinScoreboard::addScore] HERE2");

        String displayName = team.getEntries().size() == 1 ? team.getEntries().iterator().next() : teamName;
        System.out.println("[lockinScoreboard::addScore] HERE3");
        Score score = teamScores.computeIfAbsent(displayName, k -> objective.getScore(displayName));
        System.out.println("[lockinScoreboard::addScore] HERE4");
        score.setScore(scoreValue);
        System.out.println("[lockinScoreboard::addScore] HERE5");
    }

    public int getScore(String teamName) {
        Team team = scoreboard.getTeam(teamName);
        if (team == null || team.getEntries().size() == 0) {
            return -1;
        }

        String displayName = team.getEntries().size() == 1 ? team.getEntries().iterator().next() : teamName;
        Score score = teamScores.computeIfAbsent(displayName, k -> objective.getScore(displayName));
        System.out.println("[lockinScoreboard::getScore] Team: " + teamName + " Point: " + score.getScore());
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

