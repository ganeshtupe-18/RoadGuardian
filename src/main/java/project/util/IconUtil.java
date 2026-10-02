package project.util;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public final class IconUtil {

    private IconUtil() {
    }

    // =========================
    // USER / CUSTOMER ICON
    // =========================
    public static SVGPath createUserIcon(double scale, String color) {

        SVGPath icon = new SVGPath();

        icon.setContent(
                "M12 12 "
                + "C14.76 12 17 9.76 17 7 "
                + "C17 4.24 14.76 2 12 2 "
                + "C9.24 2 7 4.24 7 7 "
                + "C7 9.76 9.24 12 12 12 Z "
                + "M12 14 "
                + "C7.58 14 4 16.24 4 19 "
                + "V21 H20 V19 "
                + "C20 16.24 16.42 14 12 14 Z"
        );

        icon.setFill(Color.web(color));
        icon.setScaleX(scale);
        icon.setScaleY(scale);

        return icon;
    }


    // =========================
    // MECHANIC / WRENCH ICON
    // =========================
    public static SVGPath createMechanicIcon(double scale, String color) {

        SVGPath icon = new SVGPath();

        icon.setContent(
                "M21.7 19.3 "
                + "L14.7 12.3 "
                + "C15.5 10.9 15.7 9.2 15.2 7.6 "
                + "C14.4 5.1 12 3.3 9.3 3.2 "
                + "C8.1 3.2 7 3.5 6 4.1 "
                + "L10.1 8.2 "
                + "L8.2 10.1 "
                + "L4.1 6 "
                + "C3.5 7 3.2 8.1 3.2 9.3 "
                + "C3.3 12 5.1 14.4 7.6 15.2 "
                + "C9.2 15.7 10.9 15.5 12.3 14.7 "
                + "L19.3 21.7 "
                + "C20 22.4 21 22.4 21.7 21.7 "
                + "C22.4 21 22.4 20 21.7 19.3 Z"
        );

        icon.setFill(Color.web(color));
        icon.setScaleX(scale);
        icon.setScaleY(scale);

        return icon;
    }


    // =========================
    // CAR / VEHICLE ICON
    // =========================
    public static SVGPath createVehicleIcon(double scale, String color) {

        SVGPath icon = new SVGPath();

        icon.setContent(
                "M3 11 "
                + "L5.5 5.5 "
                + "C5.8 4.6 6.7 4 7.7 4 "
                + "H16.3 "
                + "C17.3 4 18.2 4.6 18.5 5.5 "
                + "L21 11 "
                + "V17 "
                + "C21 17.6 20.6 18 20 18 "
                + "H19 "
                + "C18.4 18 18 17.6 18 17 "
                + "V16 "
                + "H6 "
                + "V17 "
                + "C6 17.6 5.6 18 5 18 "
                + "H4 "
                + "C3.4 18 3 17.6 3 17 Z "
                + "M6.5 6 "
                + "L5 10 "
                + "H19 "
                + "L17.5 6 "
                + "Z "
                + "M6 12 "
                + "C5.4 12 5 12.4 5 13 "
                + "C5 13.6 5.4 14 6 14 "
                + "C6.6 14 7 13.6 7 13 "
                + "C7 12.4 6.6 12 6 12 Z "
                + "M18 12 "
                + "C17.4 12 17 12.4 17 13 "
                + "C17 13.6 17.4 14 18 14 "
                + "C18.6 14 19 13.6 19 13 "
                + "C19 12.4 18.6 12 18 12 Z"
        );

        icon.setFill(Color.web(color));
        icon.setScaleX(scale);
        icon.setScaleY(scale);

        return icon;
    }
}