package chalkinshmeal.lockin.artifacts.tasks.specific;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;

import chalkinshmeal.lockin.artifacts.tasks.LockinTask;

public class EnterBoatWithPassengerTask extends LockinTask {
    //---------------------------------------------------------------------------------------------
    // Constructor, which takes lockintaskhandler
    //---------------------------------------------------------------------------------------------
    public EnterBoatWithPassengerTask() {
        super();
        this.name = "Enter boat with a passenger in it";
        this.item = new ItemStack(Material.OAK_BOAT);
    }

    //---------------------------------------------------------------------------------------------
    // Abstract methods
    //---------------------------------------------------------------------------------------------
    public void validateConfig() {}

    public void addListeners() {
		this.listeners.add(new EnterBoatWithPassengerTaskVehicleEnterEventListener(this));
    }

    //---------------------------------------------------------------------------------------------
    // Task getter
    //---------------------------------------------------------------------------------------------
    public static List<EnterBoatWithPassengerTask> getTasks(int tier) {
        if (tier != 1) return new ArrayList<>();
        List<EnterBoatWithPassengerTask> tasks = new ArrayList<>();
        tasks.add(new EnterBoatWithPassengerTask());
        return tasks;
    }

    //---------------------------------------------------------------------------------------------
    // Any listeners. Upon completion, lockinTaskHandler.CompleteTask(player);
    //---------------------------------------------------------------------------------------------
    public void onVehicleEnterEvent(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player)) return;
        Player player = (Player) event.getEntered();
        Vehicle vehicle = event.getVehicle();
        if (!(vehicle instanceof Boat)) return;
        
        // Check if the boat already has a passenger
        if (vehicle.getPassengers().size() < 1) return;

        this.complete(player);
    }
}

//---------------------------------------------------------------------------------------------
// Private classes - any listeners that this task requires
//---------------------------------------------------------------------------------------------
class EnterBoatWithPassengerTaskVehicleEnterEventListener implements Listener {
    private final EnterBoatWithPassengerTask task;

    public EnterBoatWithPassengerTaskVehicleEnterEventListener(EnterBoatWithPassengerTask task) {
        this.task = task;
    }

    /** Event Handler */
    @EventHandler
    public void onVehicleEnterEvent(VehicleEnterEvent event) {
        if (this.task.haveAllTeamsCompleted()) return;
        this.task.onVehicleEnterEvent(event);
    }
}
