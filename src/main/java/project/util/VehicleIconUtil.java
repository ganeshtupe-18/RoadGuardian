package project.util;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public final class VehicleIconUtil {

    private VehicleIconUtil() {
    }

    public static SVGPath createCarIcon(
            double scale,
            String color
    ) {

        SVGPath car = new SVGPath();

        car.setContent(
                "M3 11 L5.5 5.5 "
                + "C5.8 4.6 6.7 4 7.7 4 "
                + "H16.3 "
                + "C17.3 4 18.2 4.6 18.5 5.5 "
                + "L21 11 V17 "
                + "C21 17.6 20.6 18 20 18 "
                + "H19 C18.4 18 18 17.6 18 17 "
                + "V16 H6 V17 "
                + "C6 17.6 5.6 18 5 18 "
                + "H4 C3.4 18 3 17.6 3 17 Z "
                + "M6.5 6 L5 10 H19 L17.5 6 Z "
                + "M6 12 C5.4 12 5 12.4 5 13 "
                + "C5 13.6 5.4 14 6 14 "
                + "C6.6 14 7 13.6 7 13 "
                + "C7 12.4 6.6 12 6 12 Z "
                + "M18 12 C17.4 12 17 12.4 17 13 "
                + "C17 13.6 17.4 14 18 14 "
                + "C18.6 14 19 13.6 19 13 "
                + "C19 12.4 18.6 12 18 12 Z"
        );

        car.setFill(
                Color.web(color)
        );

        car.setScaleX(scale);
        car.setScaleY(scale);

        return car;
    }
}