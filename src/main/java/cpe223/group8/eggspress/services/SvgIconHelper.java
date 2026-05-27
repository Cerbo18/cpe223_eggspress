package cpe223.group8.eggspress.services;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/**
 * Technical utility service responsible for uniform, DPI-independent SVG icon rendering.
 * Centralizes layout properties, geometric dimensions, and raw coordinates to prevent
 * duplicate code and resource redundancy across the application UI controllers.
 */
public class SvgIconHelper {

    // Centralized constant path arrays for standard vector icons
    public static final String[] EYE_PATHS = {
        "M10 12a2 2 0 1 0 4 0a2 2 0 0 0 -4 0 M21 12c-2.4 4 -5.4 6 -9 6c-3.6 0 -6.6 -2 -9 -6c2.4 -4 5.4 -6 9 -6c3.6 0 6.6 2 9 6"
    };

    public static final String[] EYE_OFF_PATHS = {
        "M10.585 10.587a2 2 0 0 0 2.829 2.828",
        "M16.681 16.673a8.717 8.717 0 0 1 -4.681 1.327c-3.6 0 -6.6 -2 -9 -6c1.272 -2.12 2.712 -3.678 4.32 -4.674m2.86 -1.146a9.055 9.055 0 0 1 1.82 -.18c3.6 0 6.6 2 9 6c-.666 1.11 -1.379 2.067 -2.138 2.87",
        "M3 3l18 18"
    };

    public static final String[] EDIT_PATHS = {
        "M7 7h-1a2 2 0 0 0 -2 2v9a2 2 0 0 0 2 2h9a2 2 0 0 0 2 -2v-1",
        "M20.385 6.585a2.1 2.1 0 0 0 -2.97 -2.97l-8.415 8.385v3h3l8.385 -8.415",
        "M16 5l3 3"
    };

    public static final String[] DELETE_PATHS = {
        "M4 7h16",
        "M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2 -2l1 -12",
        "M9 7v-3a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v3",
        "M10 12l4 4m0 -4l-4 4"
    };

    public static final String[] CHECK_ALL_PATHS = {
        "M10 12a2 2 0 1 0 4 0a2 2 0 0 0 -4 0",
        "M21 12c-2.4 4 -5.4 6 -9 6c-3.6 0 -6.6 -2 -9 -6c2.4 -4 5.4 -6 9 -6c3.6 0 6.6 2 9 6"
    };

    public static final String[] CLEAR_ALL_PATHS = {
        "M8 6h12",
        "M6 12h12",
        "M4 18h12"
    };

    public static final String[] WARNING_PATHS = {
        "M12 9v4",
        "M10.363 3.591l-8.106 13.534a1.914 1.914 0 0 0 1.636 2.871h16.214a1.914 1.914 0 0 0 1.636 -2.87l-8.106 -13.536a1.914 1.914 0 0 0 -3.274 0",
        "M12 16h.01"
    };

    public static final String[] INFO_PATHS = {
        "M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0",
        "M12 8v4",
        "M12 16h.01"
    };

    public static final String[] CLOSE_PATHS = {
        "M18 6l-12 12",
        "M6 6l12 12"
    };

    public static final String[] MARK_READ_PATHS = {
        "M10 12a2 2 0 1 0 4 0a2 2 0 0 0 -4 0",
        "M11.102 17.957c-3.204 -.307 -5.904 -2.294 -8.102 -5.957c2.4 -4 5.4 -6 9 -6c3.6 0 6.6 2 9 6a19.5 19.5 0 0 1 -.663 1.032",
        "M15 19l2 2l4 -4"
    };

    // Constant paths for active/inactive visual theme toggle graphics
    public static final String THEME_SUN_PATH = "M8 12a4 4 0 1 0 8 0a4 4 0 1 0 -8 0 M3 12h1m8 -9v1m8 8h1m-9 8v1m-6.4 -15.4l.7 .7m12.1 -.7l-.7 .7m0 11.4l.7 .7m-12.1 -.7l-.7 .7";
    public static final String THEME_MOON_PATH = "M12 3c.132 0 .263 0 .393 0a7.5 7.5 0 0 0 7.92 12.446a9 9 0 1 1 -8.313 -12.454l0 .008";

    /**
     * Enumeration indexing supported vector icon designs and their path coordinate matrices.
     */
    public enum IconType {
        EYE(EYE_PATHS),
        EYE_OFF(EYE_OFF_PATHS),
        EDIT(EDIT_PATHS),
        DELETE(DELETE_PATHS),
        CHECK_ALL(CHECK_ALL_PATHS),
        CLEAR_ALL(CLEAR_ALL_PATHS),
        WARNING(WARNING_PATHS),
        INFO(INFO_PATHS),
        CLOSE(CLOSE_PATHS),
        MARK_READ(MARK_READ_PATHS);

        private final String[] paths;

        IconType(String[] paths) {
            this.paths = paths;
        }

        public String[] getPaths() {
            return paths;
        }

        public String getCombinedPath() {
            return String.join(" ", paths);
        }
    }

    /**
     * Creates a JavaFX Group containing scaled SVGPaths representing the vector graphic.
     *
     * @param type       the target icon design representation
     * @param scale      the scaling multiplier for proportional alignment
     * @param styleClass the CSS stylesheet identifier to bind
     * @return a structured Group node for render injection
     */
    public static Group createGroupIcon(IconType type, double scale, String styleClass) {
        Group group = new Group();
        group.setScaleX(scale);
        group.setScaleY(scale);
        for (String pathData : type.getPaths()) {
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(pathData);
            if (styleClass != null && !styleClass.trim().isEmpty()) {
                svgPath.getStyleClass().add(styleClass);
            }
            group.getChildren().add(svgPath);
        }
        return group;
    }

    /**
     * Standard convenient overload for creating scaled Group-based SVG icons.
     */
    public static Group createGroupIcon(IconType type, String styleClass) {
        return createGroupIcon(type, 0.7, styleClass);
    }

    /**
     * Creates a layout-bounded StackPane enclosing a Group of SVGPaths to enforce strict
     * dimensions. Prevents spatial drift inside table layout grids.
     *
     * @param type          the target icon design representation
     * @param scale         the scaling multiplier for internal Group scaling
     * @param styleClass    the CSS style class applied to each SVG path node
     * @param containerSize the strict geometric dimension boundary (width and height)
     * @return a StackPane containing the aligned SVG graphic
     */
    public static StackPane createWrapperIcon(IconType type, double scale, String styleClass, double containerSize) {
        Group group = createGroupIcon(type, scale, styleClass);
        StackPane wrapper = new StackPane(group);
        wrapper.setMinWidth(containerSize);
        wrapper.setPrefWidth(containerSize);
        wrapper.setMaxWidth(containerSize);
        wrapper.setMinHeight(containerSize);
        wrapper.setPrefHeight(containerSize);
        wrapper.setMaxHeight(containerSize);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    /**
     * Standard convenient overload for creating a StackPane-wrapped table cell action icon.
     */
    public static StackPane createTableActionIcon(IconType type, String styleClass) {
        return createWrapperIcon(type, 0.65, styleClass, 14.0);
    }
}
