package cpe223.group8.eggspress.simulation;

import cpe223.group8.eggspress.models.ChickenHouse;
import cpe223.group8.eggspress.models.FeedingSchedule;
import cpe223.group8.eggspress.models.InventoryItem;
import cpe223.group8.eggspress.models.Automation;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.services.NotificationService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutomationService {
    private static AutomationService instance;
    private final ScheduledExecutorService scheduler;
    private final Map<String, CoopTelemetry> telemetryMap;
    private final Random random;
    private final Set<String> activeAlerts;

    private AutomationService() {
        this.telemetryMap = new ConcurrentHashMap<>();
        this.random = new Random();
        this.activeAlerts = Collections.synchronizedSet(new HashSet<>());
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "Automation-Service-Thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    public static synchronized AutomationService getInstance() {
        if (instance == null) {
            instance = new AutomationService();
        }
        return instance;
    }

    public void start() {
        // Run every 5 seconds
        scheduler.scheduleAtFixedRate(this::tick, 2, 5, TimeUnit.SECONDS);
        System.out.println("Automation & Telemetry background service successfully started.");
    }

    public void stop() {
        scheduler.shutdown();
        System.out.println("Automation & Telemetry background service stopped.");
    }

    public Collection<CoopTelemetry> getAllTelemetry() {
        ensureTelemetryInitialized();
        return telemetryMap.values();
    }

    public CoopTelemetry getTelemetryForCoop(String coopId) {
        ensureTelemetryInitialized();
        return telemetryMap.get(coopId);
    }

    private void ensureTelemetryInitialized() {
        List<ChickenHouse> coops = FarmRepository.getAllCoops();
        for (ChickenHouse coop : coops) {
            if (!telemetryMap.containsKey(coop.getId())) {
                // Initialize default values based on coop status
                double temp = 24.0;
                double humid = 60.0;
                if ("Monitoring".equalsIgnoreCase(coop.getStatus())) {
                    temp = 28.5;
                    humid = 72.0;
                } else if ("Inactive".equalsIgnoreCase(coop.getStatus())) {
                    temp = 18.0;
                    humid = 45.0;
                }
                telemetryMap.put(coop.getId(), new CoopTelemetry(
                    coop.getId(),
                    coop.getName(),
                    temp,
                    humid,
                    "ACTIVE (Low)",
                    "STANDBY",
                    "STANDBY"
                ));
            }
        }
    }

    private void tick() {
        try {
            // 1. Simulate environmental telemetry drift for all coops
            simulateTelemetryDrift();

            // 2. Evaluate environmental alerts
            evaluateAlerts();

            // 3. Process automated feeding and watering schedules
            processScheduledEvents();
        } catch (Exception e) {
            System.err.println("Error in AutomationService tick execution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void simulateTelemetryDrift() {
        List<ChickenHouse> coops = FarmRepository.getAllCoops();
        for (ChickenHouse coop : coops) {
            CoopTelemetry telemetry = telemetryMap.get(coop.getId());
            if (telemetry == null) {
                // Initialize if missing
                ensureTelemetryInitialized();
                telemetry = telemetryMap.get(coop.getId());
                if (telemetry == null) continue;
            }

            double currentTemp = telemetry.getTemperature();
            double currentHumid = telemetry.getHumidity();

            double targetTemp = 24.2;
            double targetHumid = 60.0;

            if ("Monitoring".equalsIgnoreCase(coop.getStatus())) {
                targetTemp = 30.5; // elevated, triggers warnings/criticals
                targetHumid = 76.0;
            } else if ("Inactive".equalsIgnoreCase(coop.getStatus())) {
                targetTemp = 17.5; // cooler
                targetHumid = 45.0;
            }

            // Drift slowly towards the target status value + small random jitter
            double tempDrift = (targetTemp - currentTemp) * 0.1 + (random.nextDouble() - 0.5) * 0.8;
            double humidDrift = (targetHumid - currentHumid) * 0.1 + (random.nextDouble() - 0.5) * 2.0;

            double newTemp = currentTemp + tempDrift;
            double newHumid = currentHumid + humidDrift;

            // Clamp values to realistic ranges
            newTemp = Math.max(10.0, Math.min(45.0, newTemp));
            newHumid = Math.max(10.0, Math.min(100.0, newHumid));

            telemetry.setTemperature(newTemp);
            telemetry.setHumidity(newHumid);

            // 2. Update equipment states dynamically based on new metrics
            // Fans Speed
            if ("Inactive".equalsIgnoreCase(coop.getStatus()) || newTemp < 18.0) {
                telemetry.setFanSpeed("STANDBY");
            } else if (newTemp >= 18.0 && newTemp < 24.0) {
                telemetry.setFanSpeed("ACTIVE (Low)");
            } else if (newTemp >= 24.0 && newTemp <= 28.0) {
                telemetry.setFanSpeed("ACTIVE (Med)");
            } else {
                // Temp > 28°C or Humidity > 75.0%
                telemetry.setFanSpeed("MAX SPEED");
            }

            // Misting status
            if (newTemp > 29.0) {
                telemetry.setMisterStatus("ACTIVE");
            } else {
                telemetry.setMisterStatus("STANDBY");
            }

            // Default feeder status is STANDBY, will be toggled ACTIVE when schedule executes
            telemetry.setFeederStatus("STANDBY");
        }
    }

    private void evaluateAlerts() {
        List<ChickenHouse> coops = FarmRepository.getAllCoops();
        for (ChickenHouse coop : coops) {
            CoopTelemetry telemetry = telemetryMap.get(coop.getId());
            if (telemetry == null) continue;

            double temp = telemetry.getTemperature();
            double humid = telemetry.getHumidity();

            // Temperature Warning
            String tempWarningKey = coop.getId() + "_temp_warn";
            String tempCriticalKey = coop.getId() + "_temp_crit";

            if (temp > 32.0) {
                if (!activeAlerts.contains(tempCriticalKey)) {
                    activeAlerts.add(tempCriticalKey);
                    NotificationService.notificationCritical("Emergency: High temperature hazard in " + coop.getName() + "! Currently " + String.format("%.1f", temp) + "°C, exceeding safe limit (29.0°C)!", true);
                }
            } else if (temp > 29.0 || temp < 18.0) {
                if (!activeAlerts.contains(tempWarningKey)) {
                    activeAlerts.add(tempWarningKey);
                    String cause = temp > 29.0 ? "exceeds upper safe limit of 29°C" : "drops below lower safe limit of 18°C";
                    NotificationService.notificationWarning("Warning: Out-of-bounds temperature in " + coop.getName() + " (" + String.format("%.1f", temp) + "°C) which " + cause + ".", true);
                }
                activeAlerts.remove(tempCriticalKey); // clear critical if downgraded
            } else {
                // Back to normal
                if (activeAlerts.contains(tempWarningKey) || activeAlerts.contains(tempCriticalKey)) {
                    activeAlerts.remove(tempWarningKey);
                    activeAlerts.remove(tempCriticalKey);
                    NotificationService.notificationInfo("Resolved: Temperature in " + coop.getName() + " has returned to the safe optimal range (" + String.format("%.1f", temp) + "°C).", true);
                }
            }

            // Humidity Warning
            String humidWarningKey = coop.getId() + "_humid_warn";
            String humidCriticalKey = coop.getId() + "_humid_crit";

            if (humid > 82.0) {
                if (!activeAlerts.contains(humidCriticalKey)) {
                    activeAlerts.add(humidCriticalKey);
                    NotificationService.notificationCritical("Emergency: High humidity hazard in " + coop.getName() + "! Currently " + String.format("%.1f", humid) + "%, exceeding safe limit (75.0%)!", true);
                }
            } else if (humid > 75.0 || humid < 40.0) {
                if (!activeAlerts.contains(humidWarningKey)) {
                    activeAlerts.add(humidWarningKey);
                    String cause = humid > 75.0 ? "exceeds upper safe limit of 75%" : "drops below lower safe limit of 40%";
                    NotificationService.notificationWarning("Warning: Out-of-bounds humidity in " + coop.getName() + " (" + String.format("%.1f", humid) + "%) which " + cause + ".", true);
                }
                activeAlerts.remove(humidCriticalKey);
            } else {
                // Back to normal
                if (activeAlerts.contains(humidWarningKey) || activeAlerts.contains(humidCriticalKey)) {
                    activeAlerts.remove(humidWarningKey);
                    activeAlerts.remove(humidCriticalKey);
                    NotificationService.notificationInfo("Resolved: Humidity in " + coop.getName() + " has returned to the safe optimal range (" + String.format("%.1f", humid) + "%).", true);
                }
            }
        }
    }

    private static LocalTime parseScheduleTime(String timeStr) {
        if (timeStr == null) return null;
        timeStr = timeStr.trim().toUpperCase();
        
        // Try HH:mm (24 hours format, e.g. 14:35)
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e1) {
            // Try H:mm
            try {
                return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("H:mm"));
            } catch (Exception e2) {
                // Try hh:mm a (12 hours format, e.g. 08:30 AM)
                try {
                    return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("hh:mm a"));
                } catch (Exception e3) {
                    try {
                        return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("h:mm a"));
                    } catch (Exception e4) {
                        System.err.println("Could not parse schedule time: " + timeStr);
                        return null;
                    }
                }
            }
        }
    }

    private void processScheduledEvents() {
        List<FeedingSchedule> schedules = FarmRepository.getAllSchedules();
        List<ChickenHouse> coops = FarmRepository.getAllCoops();
        if (coops.isEmpty()) return;

        LocalTime now = LocalTime.now();

        for (FeedingSchedule schedule : schedules) {
            String status = schedule.getStatus();
            boolean isPending = "Pending".equalsIgnoreCase(status);
            boolean isScheduled = "Scheduled".equalsIgnoreCase(status);

            if (!isPending && !isScheduled) {
                continue;
            }

            boolean shouldExecute = false;

            // Rule 1: "Pending" schedules execute instantly in the simulator (within next 5s tick) for rapid verification
            if (isPending) {
                shouldExecute = true;
            }
            // Rule 2: "Scheduled" items execute if the current system clock time matches or has passed the scheduled time
            else if (isScheduled) {
                LocalTime schedTime = parseScheduleTime(schedule.getTime());
                if (schedTime != null) {
                    if (!now.isBefore(schedTime)) {
                        shouldExecute = true;
                    }
                } else {
                    // Fallback to exact match logic if parsing somehow fails
                    String timeStr12 = now.format(DateTimeFormatter.ofPattern("hh:mm a"));
                    String timeStr24 = now.format(DateTimeFormatter.ofPattern("HH:mm"));
                    String schedTimeStr = schedule.getTime().trim().toUpperCase();
                    if (schedTimeStr.equalsIgnoreCase(timeStr12) || schedTimeStr.equalsIgnoreCase(timeStr24)) {
                        shouldExecute = true;
                    }
                }
            }

            if (shouldExecute) {
                executeSchedule(schedule, coops);
            }
        }
    }

    private void executeSchedule(FeedingSchedule schedule, List<ChickenHouse> coops) {
        // Pick the first coop as the main automated dispatch target for simplicity
        ChickenHouse targetCoop = coops.get(0);
        String targetCoopName = targetCoop.getName();

        // Calculate dynamic amount based on target coop flock population
        int birds = targetCoop.getFlockCount();
        String category = schedule.getCategory();

        double factor = "Water".equalsIgnoreCase(category) ? 0.15 : 0.10;
        double amount = birds * factor;
        if (amount <= 0) {
            amount = "Water".equalsIgnoreCase(category) ? 50.0 : 30.0; // sensible fallback
        }

        // Get appropriate inventory item
        InventoryItem targetItem = null;
        if ("Water".equalsIgnoreCase(category)) {
            // Find Water stock
            targetItem = FarmRepository.getInventoryItemById("INV002");
        } else {
            // Find Grains/Layers Feed stock
            targetItem = FarmRepository.getInventoryItemById("INV001");
            if (targetItem == null || targetItem.getQuantity() < amount) {
                InventoryItem altItem = FarmRepository.getInventoryItemById("INV003");
                if (altItem != null && altItem.getQuantity() >= amount) {
                    targetItem = altItem;
                }
            }
        }

        if (targetItem == null) {
            // Insufficient stock / item not found
            System.err.println("Scheduler Error: No suitable inventory found for " + category);
            NotificationService.notificationCritical("Automation Core Error: Scheduled run failed for " + schedule.getFeedingType() + " - No inventory resource found!", true);
            FarmRepository.updateScheduleStatus(schedule.getTime(), schedule.getFeedingType(), schedule.getCategory(), "Failed");
            return;
        }

        if (targetItem.getQuantity() < amount) {
            // Insufficient stock warning
            System.err.println("Scheduler Error: Insufficient stock for " + targetItem.getName() + " (Required: " + amount + ", Available: " + targetItem.getQuantity() + ")");
            NotificationService.notificationCritical("Automation Core Error: Scheduled run failed for " + schedule.getFeedingType() + " - Insufficient stock of " + targetItem.getName() + " (Required " + String.format("%.1f", amount) + " " + targetItem.getUnit() + ")!", true);
            FarmRepository.updateScheduleStatus(schedule.getTime(), schedule.getFeedingType(), schedule.getCategory(), "Failed");
            return;
        }

        // Deduct inventory
        double updatedQty = targetItem.getQuantity() - amount;
        boolean success = FarmRepository.updateInventoryQuantity(targetItem.getId(), updatedQty);

        if (success) {
            targetItem.setQuantity(updatedQty);
            // Check inventory threshold triggers
            NotificationService.getInstance().checkInventoryThresholds(targetItem);

            // Log automation run in history
            String autoId = "AUTO-" + (FarmRepository.getAutomationCount() + 1);
            Automation log = new Automation(autoId, targetItem.getName(), targetCoopName, amount, "Success");
            FarmRepository.addAutomationLog(log);

            // Update schedule status to Completed
            FarmRepository.updateScheduleStatus(schedule.getTime(), schedule.getFeedingType(), schedule.getCategory(), "Completed");

            // Dispatch global info notification
            NotificationService.notificationInfo("Automation System: Successfully executed scheduled " + category.toLowerCase() + " dispatch. Dispatched " + String.format("%.1f", amount) + " " + targetItem.getUnit() + " of " + targetItem.getName() + " to " + targetCoopName + ".", true);

            // Set telemetry feeder status to ACTIVE for this tick
            CoopTelemetry telemetry = telemetryMap.get(targetCoop.getId());
            if (telemetry != null) {
                if ("Water".equalsIgnoreCase(category)) {
                    telemetry.setMisterStatus("ACTIVE");
                } else {
                    telemetry.setFeederStatus("ACTIVE");
                }
            }
        } else {
            NotificationService.notificationCritical("Automation Core Error: Failed to update database quantity during scheduled run.", true);
            FarmRepository.updateScheduleStatus(schedule.getTime(), schedule.getFeedingType(), schedule.getCategory(), "Failed");
        }
    }
}
