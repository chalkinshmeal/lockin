package chalkinshmeal.lockin.artifacts.team;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class LockinTeamHandler {
    private JavaPlugin plugin;
    private LinkedHashMap<String, HashSet<UUID>> teams = new LinkedHashMap<>();
    private LinkedHashMap<String, Material> teamMaterials = new LinkedHashMap<>();
    private boolean debug = true;

    public LockinTeamHandler(JavaPlugin plugin) {
        this.plugin = plugin;

        // Initialize default teams
        this.teams.put("Team 1", new HashSet<>());
        this.teams.put("Team 2", new HashSet<>());
        this.teams.put("Team 3", new HashSet<>());
        this.teams.put("Team 4", new HashSet<>());

        this.teamMaterials.put("Team 1", Material.BLUE_WOOL);
        this.teamMaterials.put("Team 2", Material.GREEN_WOOL);
        this.teamMaterials.put("Team 3", Material.RED_WOOL);
        this.teamMaterials.put("Team 4", Material.MAGENTA_WOOL);
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

    public int getNumTeams() { return this.teams.size(); }
    public HashSet<UUID> getTeamPlayers(String teamName) { return this.teams.get(teamName); }
    public HashSet<UUID> getTeamPlayers(int teamIndex) { return new ArrayList<>(this.teams.values()).get(teamIndex); }
    public List<String> getTeamNames() { return new ArrayList<>(this.teams.keySet()); }
    public String getTeamName(Player player) {
        for (String teamName : this.teams.keySet()) {
            if (this.teams.get(teamName).contains(player.getUniqueId())) {
                return teamName;
            }
        }
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
        for (UUID uuid : teamPlayers) playerNames.add(this.plugin.getServer().getPlayer(uuid).getName());
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

}
