package chalkinshmeal.lockin.artifacts.team;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import chalkinshmeal.lockin.utils.LoggerUtils;
import chalkinshmeal.lockin.artifacts.scoreboard.LockinScoreboard;
import chalkinshmeal.lockin.utils.EntityUtils;

public class LockinTeamHandler {
    private JavaPlugin plugin;
    private LockinScoreboard lockinScoreboard;
    private LinkedHashMap<String, HashSet<UUID>> teams = new LinkedHashMap<>();
    private Set<String> catchUpTeams = new HashSet<>();
    private LinkedHashMap<String, Material> teamMaterials = new LinkedHashMap<>();
    private boolean debug = false;

    public LockinTeamHandler(JavaPlugin plugin, LockinScoreboard lockinScoreboard) {
        this.plugin = plugin;
        this.lockinScoreboard = lockinScoreboard;

        // Initialize default teams
        this.teams.put("Blue Team", new HashSet<>());
        this.teams.put("Green Team", new HashSet<>());
        this.teams.put("Red Team", new HashSet<>());
        this.teams.put("Magenta Team", new HashSet<>());

        this.teamMaterials.put("Blue Team", Material.BLUE_WOOL);
        this.teamMaterials.put("Green Team", Material.GREEN_WOOL);
        this.teamMaterials.put("Red Team", Material.RED_WOOL);
        this.teamMaterials.put("Magenta Team", Material.MAGENTA_WOOL);
    }

    //---------------------------------------------------------------------------------------------
    // Init methods
    //---------------------------------------------------------------------------------------------
    // Initialization
    public void init() {
        if (debug) this.plugin.getLogger().info("[LockinTeamHandler::init] Removing all empty teams");
        List<String> teamNamesToRemove = new ArrayList<>();
        // Remove empty teams
        for (String teamName : this.teams.keySet()) {
            HashSet<UUID> teamPlayers = this.teams.get(teamName);
            if (teamPlayers.size() == 0) teamNamesToRemove.add(teamName);
        }
        for (String teamNameToRemove : teamNamesToRemove) {
            this.teams.remove(teamNameToRemove);
        }
    }
    
    //---------------------------------------------------------------------------------------------
    // Accessor/Mutator methods
    //---------------------------------------------------------------------------------------------
    // Materials
    public List<Material> getTeamMaterials() { return new ArrayList<>(this.teamMaterials.values()); }

    // Teams
    public void addTeam(String teamName, Material material) {
        this.teams.put(teamName, new HashSet<>());
        this.teamMaterials.put(teamName, material);
    }

    // Display
    public List<String> getDisplayTeamNames() {
        List<String> displayTeamNames = new ArrayList<>();
        for (int i = 0; i < this.getNumTeams(); i++) {
            String displayTeamName = this.getDisplayTeamName(i);
            if (displayTeamName != null) displayTeamNames.add(displayTeamName);
        }
        return displayTeamNames;
    }
    public String getDisplayTeamName(int teamIndex) {
        if (this.getPlayerNames(teamIndex).size() == 0) return null;

        boolean oneManTeam = this.getPlayerNames(teamIndex).size() == 1;
        String firstPlayerName = this.getPlayerNames(teamIndex).get(0);
        String teamName = this.getTeamName(teamIndex);
        return oneManTeam ? firstPlayerName : teamName;
    }
    public String getDisplayTeamName(Player player) {
        return this.getDisplayTeamName(this.getTeamIndex(player));
    }
    public String getDisplayTeamName(String teamName) {
        return this.getDisplayTeamName(this.getTeamIndex(teamName));
    }

    public int getNumTeams() { return this.teams.size(); }
    public HashSet<UUID> getTeamPlayers(String teamName) { return this.teams.get(teamName); }
    public HashSet<UUID> getTeamPlayers(int teamIndex) { return new ArrayList<>(this.teams.values()).get(teamIndex); }
    public List<String> getTeamNames() {
        if (debug) LoggerUtils.info("Getting team names: (Size: " + this.teams.size() + ")");
        for (String teamName : this.teams.keySet()) {
            if (debug) LoggerUtils.info("  " + teamName);
        }
        return new ArrayList<>(this.teams.keySet());
    }
    public String getTeamName(Player player) {
        if (debug) this.plugin.getLogger().info("[LockinTeamHandler::getTeamName] Getting team name for player: " + player.getName());
        if (debug) this.plugin.getLogger().info("[LockinTeamHandler::getTeamName]   Teams:");
        for (String teamName : this.teams.keySet()) {
            if (debug) this.plugin.getLogger().info("[LockinTeamHandler::getTeamName]     " + teamName + " (Size: " + this.teams.get(teamName).size() + ")");
        }
        for (String teamName : this.teams.keySet()) {
            if (this.teams.get(teamName).contains(player.getUniqueId())) {
                if (debug) this.plugin.getLogger().info("[LockinTeamHandler::getTeamName]   Found team: " + teamName);
                return teamName;
            }
        }
        if (debug) this.plugin.getLogger().info("[LockinTeamHandler::getTeamName]   Could not find team. Returning null");
        return null;
    }

    public String getTeamName(int teamIndex) {
        return new ArrayList<>(this.teams.keySet()).get(teamIndex);
    }

    public Integer getTeamIndex(Player player) {
        int i = 0;
        for (String teamName : this.teams.keySet()) {
            if (this.teams.get(teamName).contains(player.getUniqueId())) return i;
            i += 1;
        }
        return null;
    }

    public Integer getTeamIndex(String teamName) {
        int i = 0;
        for (String _teamName : this.teams.keySet()) {
            if (teamName.equals(_teamName)) return i;
            i += 1;
        }
        return null;
    }

    public int GetPlayerCount(int teamIndex) { return this.getTeamPlayers(teamIndex).size(); }
    public List<String> getPlayerNames(int teamIndex) {
        List<HashSet<UUID>> teamPlayerList = new ArrayList<>(this.teams.values());
        HashSet<UUID> teamPlayers = teamPlayerList.get(teamIndex);
        List<String> playerNames = new ArrayList<>();
        for (UUID uuid : teamPlayers) playerNames.add(EntityUtils.getPlayerName(uuid));
        return playerNames; 
    }
    public void addPlayer(Player player, String teamName) {
        if (!this.teams.containsKey(teamName)) return;

        this.teams.get(teamName).add(player.getUniqueId());
    }
    public void addPlayer(Player player, int teamIndex) {
        if (teamIndex < 0) return;
        if (teamIndex >= this.teams.size()) return;

        String teamName = this.getTeamName(teamIndex);
        this.teams.get(teamName).add(player.getUniqueId());
    }
    public void removePlayer(Player player) {
        String teamName = this.getTeamName(player);
        if (teamName == null) return;

        this.teams.get(teamName).remove(player.getUniqueId());
    }

    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        for (String teamName : this.teams.keySet()) {
            for (UUID uuid : this.teams.get(teamName)) {
                players.add(this.plugin.getServer().getPlayer(uuid));
            }
        }
        return players;
    }

    public List<UUID> getAllPlayerUUIDs() {
        List<UUID> uuids = new ArrayList<>();
        for (String teamName : this.teams.keySet()) {
            for (UUID uuid : this.teams.get(teamName)) {
                uuids.add(uuid);
            }
        }
        return uuids;
    }

    public List<Player> getAllOnlinePlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : this.getAllPlayerUUIDs()) {
            Player player = this.plugin.getServer().getPlayer(uuid);
            if (player != null) players.add(player);
        }
        return players;
    }

    public List<UUID> getAllOnlinePlayerUUIDs() {
        List<UUID> uuids = new ArrayList<>();
        for (Player player : this.getAllOnlinePlayers()) {
            uuids.add(player.getUniqueId());
        }
        return uuids;
    }

    //-------------------------------------------------------------------------
    // Scoreboard-related accessors/mutators
    //-------------------------------------------------------------------------
    public List<Player> getLeadingTeamPlayers() {
        List<Player> leadingTeamPlayers = new ArrayList<>();
        if (debug) LoggerUtils.info("Getting leading team players");
        for (String teamName : this.lockinScoreboard.getWinningTeams()) {
            if (debug) LoggerUtils.info("  Checking winning team: " + teamName);
            for (UUID uuid : this.getTeamPlayers(teamName)) {
                Player player = EntityUtils.getPlayer(uuid);
                if (player == null) continue;

                leadingTeamPlayers.add(player);
                if (debug) LoggerUtils.info("    Adding player: " + player.getName());
            }
        }

        return leadingTeamPlayers;
    }

    public List<Player> getNonLeadingTeamPlayers() {
        List<Player> nonLeadingTeamPlayers = new ArrayList<>();
        if (debug) LoggerUtils.info("Getting non leading team players");
        for (String teamName : this.lockinScoreboard.getNonWinningTeams()) {
            if (debug) LoggerUtils.info("  Checking non-winning team: " + teamName);
            for (UUID uuid : this.getTeamPlayers(teamName)) {
                Player player = EntityUtils.getPlayer(uuid);
                if (player == null) continue;

                nonLeadingTeamPlayers.add(player);
                if (debug) LoggerUtils.info("    Adding player: " + player.getName());
            }
        }

        return nonLeadingTeamPlayers;
    }

    public List<Player> getTeamPlayersWithNoLives() {
        List<Player> noLifePlayers = new ArrayList<>();
        for (String teamName : this.getTeamNames()) {
            if (this.lockinScoreboard.getScore(teamName) > 0) continue;
            for (UUID uuid : this.getTeamPlayers(teamName)) {
                Player player = EntityUtils.getPlayer(uuid);
                if (player == null) continue;

                noLifePlayers.add(player);
            }
        }

        return noLifePlayers;
    }

    public List<String> getCatchUpTeamNames() {
        List<String> catchUpTeamNames = new ArrayList<>();
        for (String teamName : this.getTeamNames()) {
            if (this.isCatchUpTeam(teamName)) catchUpTeamNames.add(teamName);
        }
        return catchUpTeamNames;
    }

    public List<String> getNonCatchUpTeamNames() {
        List<String> nonCatchUpTeamNames = new ArrayList<>();
        for (String teamName : this.getTeamNames()) {
            if (!this.isCatchUpTeam(teamName)) nonCatchUpTeamNames.add(teamName);
        }
        return nonCatchUpTeamNames;
    }

    public void addCatchUpTeam(String teamName) {
        this.catchUpTeams.add(teamName);
    }

    public void removeCatchUpTeam(String teamName) {
        this.catchUpTeams.remove(teamName);
    }

    public boolean isCatchUpTeam(String teamName) {
        return this.catchUpTeams.contains(teamName);
    }
}