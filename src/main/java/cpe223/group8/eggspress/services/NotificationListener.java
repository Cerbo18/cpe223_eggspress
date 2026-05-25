package cpe223.group8.eggspress.services;

import cpe223.group8.eggspress.models.Notification;

public interface NotificationListener {
    void onNotificationReceived(Notification notification);
}
