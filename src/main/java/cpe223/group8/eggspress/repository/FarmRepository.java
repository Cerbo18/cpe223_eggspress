package cpe223.group8.eggspress.repository;

import cpe223.group8.eggspress.models.InventoryItem;
import cpe223.group8.eggspress.models.FeedingSchedule;
import cpe223.group8.eggspress.models.Automation;
import java.util.ArrayList;
import java.util.List;

public class FarmRepository {
    private static final List<InventoryItem> inventory = new ArrayList<>();
    private static final List<FeedingSchedule> feedingSchedules = new ArrayList<>();
    private static final List<Automation> automations = new ArrayList<>();

    static {
        // Pre-populate with typical farm starter stock to show versatility
        inventory.add(new InventoryItem("INV-001", "Main Water Tank", "Water", 1000.0, "Liters"));
        inventory.add(new InventoryItem("INV-002", "Coarse Corn Silo", "Grains", 500.0, "kg"));
        inventory.add(new InventoryItem("INV-003", "Layer Feed Store", "Feed", 200.0, "kg"));
        inventory.add(new InventoryItem("INV-004", "Organic Wheat Bin", "Grains", 350.0, "kg"));

        // Pre-populate with typical starter feeding schedules
        feedingSchedules.add(new FeedingSchedule("Feed", "08:00 AM", "Layer Feed", "Scheduled"));
        feedingSchedules.add(new FeedingSchedule("Water", "12:00 PM", "Fresh Water", "Completed"));
        feedingSchedules.add(new FeedingSchedule("Grains", "04:30 PM", "Coarse Corn", "Active"));
    }

    public static List<InventoryItem> getStaticInventory() {
        return inventory;
    }

    public static void addStaticItem(InventoryItem item) {
        inventory.add(item);
    }

    public static void removeStaticItem(InventoryItem item) {
        inventory.remove(item);
    }

    public static List<FeedingSchedule> getStaticSchedules() {
        return feedingSchedules;
    }

    public static void addStaticSchedule(FeedingSchedule schedule) {
        feedingSchedules.add(schedule);
    }

    public static List<Automation> getStaticAutomations() {
        return automations;
    }

    public static void addStaticAutomation(Automation automation) {
        automations.add(automation);
    }
}
