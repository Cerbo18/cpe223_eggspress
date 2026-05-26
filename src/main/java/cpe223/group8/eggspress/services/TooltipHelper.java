package cpe223.group8.eggspress.services;

import javafx.scene.control.Tooltip;
import javafx.scene.control.Control;
import javafx.util.Duration;

/**
 * Service utility to build and install customized tooltip elements
 * with accelerated show/hide response times.
 */
public class TooltipHelper {

    /**
     * Creates a custom tooltip and attaches it to the specified UI control.
     * Configure response delay constraints to ensure fast layout transitions.
     *
     * @param control the target UI node to attach the tooltip to
     * @param text    the descriptive text content for the tooltip
     */
    public static void installTooltip(Control control, String text) {
        if (control == null || text == null || text.trim().isEmpty()) {
            return;
        }

        Tooltip tooltip = new Tooltip(text);
        
        // Fast response times (150ms show delay, 200ms hide delay)
        tooltip.setShowDelay(Duration.millis(150));
        tooltip.setHideDelay(Duration.millis(200));
        tooltip.setShowDuration(Duration.seconds(10));
        
        Tooltip.install(control, tooltip);
    }
}
