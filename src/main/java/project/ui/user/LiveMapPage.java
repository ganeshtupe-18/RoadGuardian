package project.ui.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.concurrent.Worker;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import project.firebase.FirebaseConfig;

import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import project.controller.user.LiveTrackingController;
import project.model.Mechanic;
import project.model.ServiceRequest;

/**
 * Customer Live Map / Tracking page.
 *
 * Data flow:
 * serviceRequests/{requestId}
 *      ↑ Mechanic NavigationPage writes navigationStatus / arrivalStatus
 *      ↓ Customer LiveMapPage reads the same document
 *
 * This page intentionally does NOT show fake ETA, distance, mechanic,
 * shop or phone values. If GPS/navigation data is not available it
 * clearly says "Not available".
 */
public class LiveMapPage {

    // ============================================================
    // COLORS
    // ============================================================

    private static final String MAIN_BACKGROUND  = "#0F0F0F";
    private static final String CARD_SURFACE      = "#1A1A1A";
    private static final String SECONDARY_SURFACE = "#242424";
    private static final String WHITE             = "#1A1A1A";

    private static final String HEADING       = "#F3F4F6";
    private static final String TEXT          = "#A1A1AA";
    private static final String SECONDARY_TEXT= "#A1A1AA";
    private static final String BORDER        = "#333333";

    private static final String BLUE   = "#F59E0B";
    private static final String ORANGE = "#F59E0B";
    private static final String GREEN  = "#22C55E";
    private static final String PURPLE = "#A78BFA";
    private static final String RED    = "#EF4444";

    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 760;

    // ============================================================
    // DATA
    // ============================================================

    private LiveTrackingController controller;
    private ServiceRequest currentRequest;
    private Mechanic currentMechanic;

    // ============================================================
    // UI
    // ============================================================

    private VBox trackingHost;
    private Label pageSubtitle;
    private Button refreshButton;

    // Legacy external tracking hook labels.
    private Label externalEtaLabel;
    private Label externalDistanceLabel;
    private Label externalRoadLabel;
    private Label externalMechanicLabel;

    // Real interactive map
    private WebView mapView;
    private WebEngine mapEngine;
    private Firestore firestore;
    private Timeline mapRefreshTimeline;
    private Double pendingLatitude;
    private Double pendingLongitude;
    private boolean mapLoaded;
    private MapBridge mapBridge;
    private long routeRequestSequence = 0L;
    private String lastSuccessfulRouteKey = "";

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public LiveMapPage() {
    }

    // ============================================================
    // SCENE
    // ============================================================

    public Scene getLiveMapScene() {

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: "
                        + MAIN_BACKGROUND
                        + ";"
        );

        HBox header =
                UserHeader.createHeader();

        ScrollPane sidebar =
                UserSideBar.createSidebar(
                        "Live Map"
                );

        VBox content =
                createContent();

        ScrollPane contentScroll =
                new ScrollPane(content);

        contentScroll.setFitToWidth(true);
        contentScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );
        contentScroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );
        contentScroll.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-background: "
                        + MAIN_BACKGROUND
                        + ";"
                        + "-fx-border-color: transparent;"
        );

        root.setTop(header);
        root.setLeft(sidebar);
        root.setCenter(contentScroll);

        Scene scene =
                new Scene(
                        root,
                        DEFAULT_WIDTH,
                        DEFAULT_HEIGHT
                );

        return scene;
    }

    // ============================================================
    // CONTENT
    // ============================================================

    private VBox createContent() {

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(
                        26,
                        28,
                        32,
                        28
                )
        );

        content.setFillWidth(true);
        content.setStyle(
                "-fx-background-color: "
                        + MAIN_BACKGROUND
                        + ";"
        );

        content.getChildren()
                .add(
                        createPageHeader()
                );

        trackingHost =
                new VBox(18);

        trackingHost.setFillWidth(true);

        content.getChildren()
                .add(
                        trackingHost
                );

        VBox.setVgrow(
                trackingHost,
                Priority.ALWAYS
        );

        initializeControllerAndLoad();

        return content;
    }

    // ============================================================
    // PAGE HEADER
    // ============================================================

    private HBox createPageHeader() {

        HBox header =
                new HBox(16);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox text =
                new VBox(5);

        Label title =
                new Label(
                        "Live Tracking"
                );

        title.setStyle(
                "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-font-size: 31px;"
                        + "-fx-font-weight: bold;"
        );

        pageSubtitle =
                new Label(
                        "Loading your active service request..."
                );

        pageSubtitle.setWrapText(true);
        pageSubtitle.setStyle(
                "-fx-text-fill: "
                        + SECONDARY_TEXT
                        + ";"
                        + "-fx-font-size: 13px;"
        );

        text.getChildren()
                .addAll(
                        title,
                        pageSubtitle
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        refreshButton =
                new Button(
                        "Refresh Tracking"
                );

        refreshButton.setStyle(
                "-fx-background-color: "
                        + BLUE
                        + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 10 16 10 16;"
                        + "-fx-cursor: hand;"
        );

        refreshButton.setOnAction(
                event -> refreshTracking()
        );

        header.getChildren()
                .addAll(
                        text,
                        spacer,
                        refreshButton
                );

        return header;
    }

    // ============================================================
    // INITIALIZE
    // ============================================================

    private void initializeControllerAndLoad() {

        try {

            controller =
                    new LiveTrackingController();

            try {
                firestore = FirebaseConfig.getFirestore();
            } catch (Exception firebaseError) {
                firebaseError.printStackTrace();
            }

            refreshTracking();

        } catch (Exception e) {

            e.printStackTrace();

            if (pageSubtitle != null) {
                pageSubtitle.setText(
                        "Unable to connect to Firebase tracking data."
                );
            }

            showErrorState(
                    "Unable to initialize live tracking."
            );
        }
    }

    // ============================================================
    // REFRESH TRACKING
    // ============================================================

    private void refreshTracking() {

        if (controller == null
                || trackingHost == null) {

            return;
        }

        if (refreshButton != null) {
            refreshButton.setDisable(true);
        }

        try {

            if (currentRequest != null
                    && clean(currentRequest.getRequestId()) != null) {

                ServiceRequest refreshed =
                        controller.refreshRequest(
                                currentRequest.getRequestId()
                        );

                if (refreshed != null
                        && isTrackable(refreshed)) {

                    currentRequest = refreshed;

                } else if (currentRequest == null) {

                    currentRequest =
                            controller
                                    .getCurrentTrackingRequest();
                }

            } else {

                currentRequest =
                        controller
                                .getCurrentTrackingRequest();
            }

            if (currentRequest == null) {

                currentMechanic = null;

                pageSubtitle.setText(
                        "No assigned or accepted mechanic is currently available to track."
                );

                trackingHost
                        .getChildren()
                        .setAll(
                                createEmptyState()
                        );

                return;
            }

            currentMechanic =
                    controller.getMechanic(
                            currentRequest
                    );

            if (mapLoaded) {
                updateMapFromRequest();
            }

            pageSubtitle.setText(
                    controller.getTrackingHeadline(
                            currentRequest
                    )
            );

            trackingHost
                    .getChildren()
                    .setAll(
                            createStatusOverview(),
                            createMainRow(),
                            createRequestInformation()
                    );

        } catch (Exception e) {

            e.printStackTrace();

            showErrorState(
                    "Unable to refresh tracking data."
            );

        } finally {

            if (refreshButton != null) {
                refreshButton.setDisable(false);
            }
        }
    }

    // ============================================================
    // STATUS OVERVIEW
    // ============================================================

    private HBox createStatusOverview() {

        HBox row =
                new HBox(12);

        row.setAlignment(
                Pos.CENTER_LEFT
        );

        String status =
                controller.getStatusDisplay(
                        currentRequest
                );

        row.getChildren()
                .addAll(
                        createMetricCard(
                                "REQUEST STATUS",
                                status,
                                getStatusColor(status)
                        ),
                        createMetricCard(
                                "NAVIGATION",
                                currentRequest.isNavigationStarted()
                                        ? "Started"
                                        : "Not Started",
                                currentRequest.isNavigationStarted()
                                        ? BLUE
                                        : SECONDARY_TEXT
                        ),
                        createMetricCard(
                                "ARRIVAL",
                                currentRequest.hasArrived()
                                        ? "Arrived"
                                        : "Not Arrived",
                                currentRequest.hasArrived()
                                        ? GREEN
                                        : SECONDARY_TEXT
                        ),
                        createMetricCard(
                                "REQUEST ID",
                                firstNonBlank(
                                        currentRequest.getRequestId(),
                                        "-"
                                ),
                                HEADING
                        )
                );

        for (javafx.scene.Node node :
                row.getChildren()) {

            HBox.setHgrow(
                    node,
                    Priority.ALWAYS
            );
        }

        return row;
    }

    // ============================================================
    // MAIN ROW
    // ============================================================

    private HBox createMainRow() {

        HBox row =
                new HBox(18);

        row.setAlignment(
                Pos.TOP_LEFT
        );

        VBox mapCard =
                createTrackingDiagram();

        VBox mechanicCard =
                createMechanicCard();

        HBox.setHgrow(
                mapCard,
                Priority.ALWAYS
        );

        row.getChildren()
                .addAll(
                        mapCard,
                        mechanicCard
                );

        return row;
    }

    // ============================================================
    // TRACKING DIAGRAM
    // ============================================================

    private VBox createTrackingDiagram() {

        VBox card = createCard();
        card.setMinHeight(520);

        Label title = createCardTitle("Live Tracking Map");

        Label note = new Label(
                "Interactive map. Click to select your service location, then lock it. "
                        + "The mechanic location and road route update automatically when GPS data is available."
        );
        note.setWrapText(true);
        note.setStyle("-fx-text-fill: " + SECONDARY_TEXT + "; -fx-font-size: 10px;");

        HBox controls = new HBox(8);
        controls.setAlignment(Pos.CENTER_LEFT);

        Button myLocation = new Button("My Location");
        Button lockLocation = new Button("Lock / Confirm Location");
        Button fitRoute = new Button("Fit Route");
        Button viewRoute = new Button("View Route");
        ToggleButton satellite = new ToggleButton("Satellite");

        String buttonStyle = "-fx-background-color: " + BLUE + "; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 7; -fx-padding: 8 12; -fx-cursor: hand;";
        myLocation.setStyle(buttonStyle);
        lockLocation.setStyle(buttonStyle);
        fitRoute.setStyle(buttonStyle);
        viewRoute.setStyle(buttonStyle);
        satellite.setStyle(buttonStyle);

        myLocation.setOnAction(e -> runMapScript("requestMyLocation();"));
        lockLocation.setOnAction(e -> confirmSelectedLocation());
        fitRoute.setOnAction(e -> runMapScript("fitRoute();"));
        viewRoute.setOnAction(e -> runMapScript("calculateRoute();"));
        satellite.setOnAction(e -> runMapScript("setSatellite(" + satellite.isSelected() + ");"));

        controls.getChildren().addAll(myLocation, lockLocation, fitRoute, viewRoute, satellite);

        mapView = new WebView();
        mapEngine = mapView.getEngine();
        mapView.setContextMenuEnabled(false);
        mapView.setMinHeight(500);
        mapView.setPrefHeight(500);
        mapView.setMaxWidth(Double.MAX_VALUE);

        mapBridge = new MapBridge();
        mapEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                mapLoaded = true;
                JSObject window = (JSObject) mapEngine.executeScript("window");
                window.setMember("roadGuardian", mapBridge);
                updateMapFromRequest();
            }
        });

        mapEngine.loadContent(buildLeafletHtml());

        externalEtaLabel = createSmallValue("ETA", "Not available");
        externalDistanceLabel = createSmallValue("DISTANCE", "Not available");
        externalRoadLabel = createSmallValue(
                "CUSTOMER GPS",
                controller.getCoordinatesDisplay(currentRequest)
        );
        externalMechanicLabel = createSmallValue(
                "MECHANIC GPS",
                currentRequest.getMechanicLatitude() != null && currentRequest.getMechanicLongitude() != null
                        ? String.format(Locale.US, "%.6f, %.6f", currentRequest.getMechanicLatitude(), currentRequest.getMechanicLongitude())
                        : "Waiting for GPS"
        );

        HBox gpsRow = new HBox(10,
                (VBox) externalEtaLabel.getParent(),
                (VBox) externalDistanceLabel.getParent(),
                (VBox) externalRoadLabel.getParent(),
                (VBox) externalMechanicLabel.getParent()
        );
        gpsRow.setAlignment(Pos.CENTER_LEFT);
        for (javafx.scene.Node node : gpsRow.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        card.getChildren().addAll(title, note, controls, mapView, gpsRow);
        VBox.setVgrow(mapView, Priority.ALWAYS);

        startMapRefresh();
        return card;
    }

    private void startMapRefresh() {
        if (mapRefreshTimeline != null) {
            mapRefreshTimeline.stop();
        }

        mapRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> refreshMechanicLocation())
        );
        mapRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        mapRefreshTimeline.play();
    }

    private void refreshMechanicLocation() {
        if (currentRequest == null || firestore == null) return;
        String requestId = clean(currentRequest.getRequestId());
        if (requestId == null) return;

        Thread t = new Thread(() -> {
            try {
                DocumentSnapshot doc = firestore.collection("serviceRequests")
                        .document(requestId).get().get();
                if (!doc.exists()) return;

                Double mechanicLat = numberValue(doc.get("mechanicLatitude"));
                Double mechanicLon = numberValue(doc.get("mechanicLongitude"));

                // If the live fields are not present yet, use the assigned
                // mechanic's stored profile coordinates as the initial position.
                if ((mechanicLat == null || mechanicLon == null)
                        && clean(currentRequest.getMechanicId()) != null) {
                    String mechanicId = clean(currentRequest.getMechanicId());
                    DocumentSnapshot mechanic = firestore.collection("mechanics")
                            .document(mechanicId).get().get();

                    if (!mechanic.exists()) {
                        QuerySnapshot byMechanicId = firestore.collection("mechanics")
                                .whereEqualTo("mechanicId", mechanicId)
                                .limit(1).get().get();
                        if (!byMechanicId.isEmpty()) {
                            mechanic = byMechanicId.getDocuments().get(0);
                        } else {
                            QuerySnapshot byEmail = firestore.collection("mechanics")
                                    .whereEqualTo("email", mechanicId)
                                    .limit(1).get().get();
                            if (!byEmail.isEmpty()) mechanic = byEmail.getDocuments().get(0);
                        }
                    }

                    if (mechanic.exists()) {
                        mechanicLat = numberValue(mechanic.get("latitude"));
                        mechanicLon = numberValue(mechanic.get("longitude"));
                        if (mechanicLat != null && mechanicLon != null) {
                            Map<String, Object> update = new HashMap<>();
                            update.put("mechanicLatitude", mechanicLat);
                            update.put("mechanicLongitude", mechanicLon);
                            update.put("mechanicLocationUpdatedAt", String.valueOf(System.currentTimeMillis()));
                            firestore.collection("serviceRequests").document(requestId)
                                    .set(update, SetOptions.merge()).get();
                        }
                    }
                }

                String navigationStatus = doc.getString("navigationStatus");
                String arrivalStatus = doc.getString("arrivalStatus");
                String requestStatus = doc.getString("status");
                final Double finalMechanicLat = mechanicLat;
                final Double finalMechanicLon = mechanicLon;

                Platform.runLater(() -> {
                    currentRequest.setNavigationStatus(navigationStatus);
                    currentRequest.setArrivalStatus(arrivalStatus);
                    if (requestStatus != null) currentRequest.setStatus(requestStatus);
                    if (trackingHost != null && !trackingHost.getChildren().isEmpty()) {
                        trackingHost.getChildren().set(0, createStatusOverview());
                    }
                    if (pageSubtitle != null) pageSubtitle.setText(controller.getTrackingHeadline(currentRequest));

                    if (finalMechanicLat != null && finalMechanicLon != null) {
                        currentRequest.setMechanicLatitude(finalMechanicLat);
                        currentRequest.setMechanicLongitude(finalMechanicLon);
                        if (externalDistanceLabel != null) externalDistanceLabel.setText("Calculating...");
                        if (externalEtaLabel != null) externalEtaLabel.setText("Calculating...");
                        if (externalMechanicLabel != null) {
                            externalMechanicLabel.setText(String.format(Locale.US, "%.6f, %.6f", finalMechanicLat, finalMechanicLon));
                        }
                        runMapScript("setMechanicLocation(" + finalMechanicLat + "," + finalMechanicLon + ",false);");
                        requestRoadRouteFromJava();
                    } else {
                        if (externalMechanicLabel != null) externalMechanicLabel.setText("Waiting for mechanic location");
                        if (externalDistanceLabel != null) externalDistanceLabel.setText("Waiting...");
                        if (externalEtaLabel != null) externalEtaLabel.setText("Waiting...");
                    }
                });
            } catch (Exception ex) {
                System.err.println("Live tracking refresh failed: " + ex.getMessage());
            }
        }, "roadguardian-live-location-refresh");
        t.setDaemon(true);
        t.start();
    }

    private void updateMapFromRequest() {
        if (!mapLoaded || currentRequest == null) return;

        Double lat = currentRequest.getLatitude();
        Double lon = currentRequest.getLongitude();
        if (lat != null && lon != null) {
            runMapScript("setCustomerLocation(" + lat + "," + lon + ",true);");
        }
        refreshMechanicLocation();
    }

    private void updateRouteSummary() {
        if (!mapLoaded) return;
        requestRoadRouteFromJava();
    }

    private void requestRoadRouteFromJava() {
        if (currentRequest == null
                || currentRequest.getLatitude() == null
                || currentRequest.getLongitude() == null
                || currentRequest.getMechanicLatitude() == null
                || currentRequest.getMechanicLongitude() == null) {
            return;
        }

        final double mechanicLat = currentRequest.getMechanicLatitude();
        final double mechanicLon = currentRequest.getMechanicLongitude();
        final double customerLat = currentRequest.getLatitude();
        final double customerLon = currentRequest.getLongitude();

        Thread worker = new Thread(() -> {
            try {
                String url = String.format(Locale.US,
                        "https://router.project-osrm.org/route/v1/driving/%.7f,%.7f;%.7f,%.7f?overview=full&geometries=polyline",
                        mechanicLon, mechanicLat, customerLon, customerLat);
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(java.time.Duration.ofSeconds(5)).build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "RoadGuardian/1.0")
                        .timeout(java.time.Duration.ofSeconds(10)).GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IllegalStateException("Route HTTP " + response.statusCode());
                }
                String body = response.body();
                Matcher code = Pattern.compile("\"code\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
                Matcher distance = Pattern.compile("\"distance\"\\s*:\\s*([0-9.]+)").matcher(body);
                Matcher duration = Pattern.compile("\"duration\"\\s*:\\s*([0-9.]+)").matcher(body);
                Matcher geometry = Pattern.compile("\"geometry\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
                if (!code.find() || !"Ok".equals(code.group(1)) || !distance.find() || !duration.find() || !geometry.find()) {
                    throw new IllegalStateException("No road route");
                }
                double distanceKm = Double.parseDouble(distance.group(1)) / 1000.0;
                double etaMin = Double.parseDouble(duration.group(1)) / 60.0;
                List<double[]> points = decodePolyline(geometry.group(1));
                if (points.isEmpty()) throw new IllegalStateException("Empty route geometry");
                StringBuilder js = new StringBuilder("[");
                int limit = Math.min(points.size(), 420);
                double step = points.size() <= limit ? 1.0 : (points.size() - 1.0) / (limit - 1.0);
                for (int i = 0; i < limit; i++) {
                    double[] pt = points.get((int)Math.round(i * step));
                    if (i > 0) js.append(',');
                    js.append('[').append(pt[1]).append(',').append(pt[0]).append(']');
                }
                js.append(']');
                Platform.runLater(() -> {
                    runMapScript("setRouteFromJava(" + js + "," + distanceKm + "," + etaMin + ");");
                    if (externalDistanceLabel != null) externalDistanceLabel.setText(String.format(Locale.US, "%.1f km", distanceKm));
                    if (externalEtaLabel != null) externalEtaLabel.setText(String.format(Locale.US, "%.0f min", etaMin));
                });
            } catch (Exception ex) {
                System.err.println("Live map route request failed: " + ex.getMessage());
                Platform.runLater(() -> {
                    if (externalDistanceLabel != null) externalDistanceLabel.setText("Calculating...");
                    if (externalEtaLabel != null) externalEtaLabel.setText("Calculating...");
                    runMapScript("fallbackRoadRoute();");
                });
            }
        }, "roadguardian-live-route");
        worker.setDaemon(true);
        worker.start();
    }

    private List<double[]> decodePolyline(String encoded) {
        List<double[]> result = new ArrayList<>();
        int index = 0, lat = 0, lon = 0;
        while (index < encoded.length()) {
            int shift = 0, value = 0, b;
            do { b = encoded.charAt(index++) - 63; value |= (b & 0x1f) << shift; shift += 5; } while (b >= 0x20);
            lat += ((value & 1) != 0) ? ~(value >> 1) : (value >> 1);
            shift = 0; value = 0;
            do { b = encoded.charAt(index++) - 63; value |= (b & 0x1f) << shift; shift += 5; } while (b >= 0x20);
            lon += ((value & 1) != 0) ? ~(value >> 1) : (value >> 1);
            result.add(new double[]{lat / 1e5, lon / 1e5});
        }
        return result;
    }

    private Double numberValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private void runMapScript(String script) {
        if (mapEngine == null || !mapLoaded) return;
        try {
            mapEngine.executeScript(script);
        } catch (Exception ignored) {
        }
    }

    private void confirmSelectedLocation() {
        if (pendingLatitude == null || pendingLongitude == null) {
            showInfo("Location Required", "Click on the map first or use My Location.");
            return;
        }

        if (currentRequest == null || clean(currentRequest.getRequestId()) == null || firestore == null) {
            showInfo("Location Error", "The active service request is not available.");
            return;
        }

        double lat = pendingLatitude;
        double lon = pendingLongitude;
        if (!Double.isFinite(lat) || !Double.isFinite(lon)
                || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            showInfo("Invalid Location", "The selected coordinates are invalid.");
            return;
        }

        String requestId = currentRequest.getRequestId();
        Thread t = new Thread(() -> {
            try {
                // Only update the existing customer's latitude/longitude fields.
                // SetOptions.merge() preserves all other request/workflow fields.
                Map<String, Object> update = new HashMap<>();
                update.put("latitude", lat);
                update.put("longitude", lon);
                update.put("locationUpdatedAt", String.valueOf(System.currentTimeMillis()));

                firestore.collection("serviceRequests")
                        .document(requestId)
                        .set(update, SetOptions.merge())
                        .get();

                DocumentSnapshot verify = firestore.collection("serviceRequests")
                        .document(requestId).get().get();
                Double savedLat = numberValue(verify.get("latitude"));
                Double savedLon = numberValue(verify.get("longitude"));
                if (savedLat == null || savedLon == null
                        || Math.abs(savedLat - lat) > 0.000001
                        || Math.abs(savedLon - lon) > 0.000001) {
                    throw new IllegalStateException("Firebase verification failed");
                }

                Platform.runLater(() -> {
                    currentRequest.setLatitude(savedLat);
                    currentRequest.setLongitude(lon);
                    externalRoadLabel.setText(String.format(Locale.US, "%.6f, %.6f", savedLat, savedLon));
                    runMapScript("lockCustomerLocation(" + savedLat + "," + savedLon + ");");
                    showInfo("Location Saved", String.format(Locale.US, "Your service location has been saved successfully.\n\nCoordinates: %.6f, %.6f", savedLat, savedLon));
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> showInfo("Location Save Failed",
                        "Unable to save the selected location. Please try again."));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private String buildLeafletHtml() {
        Double lat = currentRequest == null ? null : currentRequest.getLatitude();
        Double lon = currentRequest == null ? null : currentRequest.getLongitude();
        String initialLat = lat == null ? "null" : lat.toString();
        String initialLon = lon == null ? "null" : lon.toString();

        return """
                <!doctype html>
                <html><head>
                <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                  html,body{width:100%;height:100%;margin:0;overflow:hidden;font-family:Arial,sans-serif;background:#dfe3e8}
                  #map{position:relative;width:100%;height:100%;overflow:hidden;cursor:grab;background:#dfe3e8;user-select:none}#map.dragging{cursor:grabbing}
                  #tiles{position:absolute;left:0;top:0}.tile{position:absolute;width:256px;height:256px;user-select:none;pointer-events:none}
                  #routeSvg{position:absolute;left:0;top:0;width:100%;height:100%;pointer-events:none}\n                  @keyframes routePulse{0%,100%{opacity:.78}50%{opacity:1}} .routePulse{animation:routePulse 1.8s ease-in-out infinite}#markers{position:absolute;left:0;top:0;width:100%;height:100%;pointer-events:none}
                  .marker{position:absolute;width:30px;height:30px;border-radius:50%;border:3px solid white;box-shadow:0 2px 8px rgba(0,0,0,.45);transform:translate(-50%,-50%);display:flex;align-items:center;justify-content:center;color:white;font-weight:bold;font-size:14px}
                  .customer{background:#F59E0B}.mechanic{background:#22C55E}.selected{background:#2563EB}
                  .panel{position:absolute;z-index:20;background:rgba(20,20,24,.95);color:white;border-radius:9px;box-shadow:0 2px 12px rgba(0,0,0,.35)}
                  #searchPanel{top:10px;left:10px;padding:7px;display:flex;gap:6px}#placeSearch{width:210px;border:0;outline:0;border-radius:6px;padding:8px 9px;font-size:12px}
                  .panel button{border:0;border-radius:6px;padding:8px 10px;background:#2563EB;color:white;font-weight:bold;cursor:pointer}#searchResults{top:52px;left:10px;width:320px;display:none;max-height:210px;overflow:auto}
                  .result{padding:9px 10px;border-bottom:1px solid rgba(255,255,255,.1);font-size:11px;cursor:pointer}.result:hover{background:rgba(255,255,255,.1)}
                  #tools{top:10px;right:10px;padding:7px;display:flex;gap:5px;flex-wrap:wrap;max-width:360px}#tools button{padding:7px 9px;font-size:11px}
                  #info{bottom:10px;left:10px;padding:9px 11px;font-size:11px;line-height:1.45;min-width:200px}#zoom{position:absolute;z-index:20;right:10px;bottom:78px;display:flex;flex-direction:column;gap:4px}#zoom button{width:34px;height:34px;padding:0;font-size:20px}
                  #credit{position:absolute;z-index:20;right:6px;bottom:5px;background:rgba(255,255,255,.8);color:#333;padding:2px 5px;border-radius:3px;font-size:9px}
                </style></head><body>
                <div id="map"><div id="tiles"></div><svg id="routeSvg"></svg><div id="markers"></div>
                  <div id="searchPanel" class="panel"><input id="placeSearch" placeholder="Search place or area..."><button id="searchButton">Search</button></div><div id="searchResults" class="panel"></div>
                  <div id="tools" class="panel"><button id="roadButton">Road</button><button id="satButton">Satellite</button><button id="myButton">My Location</button><button id="lockButton">Lock / Confirm</button><button id="fitButton">Fit Route</button></div>
                  <div id="zoom"><button id="zoomIn">+</button><button id="zoomOut">−</button></div>
                  <div id="info" class="panel"><strong>LIVE SERVICE MAP</strong><br><span id="status">Loading map...</span><br><span id="coords">Customer: --<br>Mechanic: --</span></div><div id="credit">© OpenStreetMap contributors</div>
                </div>
                <script>
                const customer={lat:INITIAL_LAT,lon:INITIAL_LON}, mechanic={lat:null,lon:null};let selected={lat:null,lon:null};let locked=false,zoom=customer.lat!==null?15:5,center=customer.lat!==null?{lat:customer.lat,lon:customer.lon}:{lat:20.5937,lon:78.9629};let satellite=false,route=null,searchPoint=null,dragging=false,moved=false,startX=0,startY=0,startCenter=null;
                const TILE=256,mapEl=document.getElementById('map'),tilesEl=document.getElementById('tiles'),markersEl=document.getElementById('markers'),svg=document.getElementById('routeSvg'),statusEl=document.getElementById('status'),coordsEl=document.getElementById('coords');
                function clamp(v,a,b){return Math.max(a,Math.min(b,v));}function worldXY(lat,lon,z){const n=2**z,lr=clamp(lat,-85.05112878,85.05112878)*Math.PI/180;return{x:(lon+180)/360*n,y:(1-Math.log(Math.tan(lr)+1/Math.cos(lr))/Math.PI)/2*n};}function latLonFromWorld(x,y,z){const n=2**z,x2=((x%n)+n)%n,lon=x2/n*360-180,a=Math.PI*(1-2*y/n);return{lat:Math.atan(Math.sinh(a))*180/Math.PI,lon};}
                function centerWorld(){return worldXY(center.lat,center.lon,zoom);}function screenFromLatLon(lat,lon){const c=centerWorld(),p=worldXY(lat,lon,zoom),n=2**zoom;let dx=p.x-c.x;if(dx>n/2)dx-=n;if(dx<-n/2)dx+=n;return{x:mapEl.clientWidth/2+dx*TILE,y:mapEl.clientHeight/2+(p.y-c.y)*TILE};}
                function render(){const c=centerWorld(),w=mapEl.clientWidth,h=mapEl.clientHeight;if(!w||!h)return;const left=c.x-w/(2*TILE),top=c.y-h/(2*TILE),right=c.x+w/(2*TILE),bottom=c.y+h/(2*TILE),minX=Math.floor(left)-1,maxX=Math.floor(right)+1,minY=Math.max(0,Math.floor(top)-1),maxY=Math.min(2**zoom-1,Math.floor(bottom)+1);tilesEl.innerHTML='';for(let ty=minY;ty<=maxY;ty++)for(let tx=minX;tx<=maxX;tx++){const n=2**zoom,wx=((tx%n)+n)%n,img=document.createElement('img');img.className='tile';img.draggable=false;img.style.left=((tx-left)*TILE)+'px';img.style.top=((ty-top)*TILE)+'px';img.src=satellite?'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/'+zoom+'/'+ty+'/'+wx:'https://tile.openstreetmap.org/'+zoom+'/'+wx+'/'+ty+'.png';img.onerror=function(){this.style.opacity='.15'};tilesEl.appendChild(img);}renderMarkers();renderRoute();coordsEl.innerHTML='Customer: '+fmt(customer)+'<br>Mechanic: '+fmt(mechanic);}
                function fmt(p){return p.lat===null?'--':Number(p.lat).toFixed(6)+', '+Number(p.lon).toFixed(6);}function marker(p,cls){const s=screenFromLatLon(p.lat,p.lon),m=document.createElement('div');m.className='marker '+cls;m.style.left=s.x+'px';m.style.top=s.y+'px';markersEl.appendChild(m);}function renderMarkers(){markersEl.innerHTML='';if(customer.lat!==null)marker(customer,'customer');if(mechanic.lat!==null)marker(mechanic,'mechanic');if(selected.lat!==null&&!locked)marker(selected,'selected');if(searchPoint)marker(searchPoint,'selected');}
                function renderRoute(){svg.innerHTML='';if(!route||!route.length)return;const pts=route.map(p=>screenFromLatLon(p[1],p[0])),d=pts.map(p=>p.x+','+p.y).join(' ');const under=document.createElementNS('http://www.w3.org/2000/svg','polyline');under.setAttribute('points',d);under.setAttribute('fill','none');under.setAttribute('stroke','#FFFFFF');under.setAttribute('stroke-width','10');under.setAttribute('stroke-linecap','round');under.setAttribute('stroke-linejoin','round');under.setAttribute('opacity','.95');svg.appendChild(under);const poly=document.createElementNS('http://www.w3.org/2000/svg','polyline');poly.setAttribute('points',d);poly.setAttribute('fill','none');poly.setAttribute('stroke','#2563EB');poly.setAttribute('stroke-width','6');poly.setAttribute('stroke-linecap','round');poly.setAttribute('stroke-linejoin','round');poly.setAttribute('class','routePulse');svg.appendChild(poly);}
                function setStatus(t){statusEl.textContent=t;}function setCustomerLocation(lat,lon,lock){customer.lat=Number(lat);customer.lon=Number(lon);selected={lat:customer.lat,lon:customer.lon};locked=!!lock;center={lat:customer.lat,lon:customer.lon};render();calculateRoute();}
                function setMechanicLocation(lat,lon){
                  const nextLat=Number(lat),nextLon=Number(lon);
                  if(!Number.isFinite(nextLat)||!Number.isFinite(nextLon))return;
                  const moved=mechanic.lat===null||Math.abs(mechanic.lat-nextLat)>0.00015||Math.abs(mechanic.lon-nextLon)>0.00015;
                  mechanic.lat=nextLat; mechanic.lon=nextLon; render();
                  if(moved) calculateRoute();
                }
                function requestMyLocation(){if(!navigator.geolocation){setStatus('GPS unavailable. Click the map to choose your service location.');return;}setStatus('Requesting current location...');navigator.geolocation.getCurrentPosition(p=>{selected={lat:p.coords.latitude,lon:p.coords.longitude};locked=false;center={lat:selected.lat,lon:selected.lon};zoom=16;render();setStatus('Current location selected. Press Lock / Confirm to save it.');if(window.roadGuardian)window.roadGuardian.locationSelected(selected.lat,selected.lon);},()=>setStatus('Current location unavailable. Enable Windows Location Services or click the map.'),{enableHighAccuracy:true,timeout:10000,maximumAge:5000});}
                function confirmLocation(){if(selected.lat===null){setStatus('Click the map first or use My Location.');return;}locked=true;customer.lat=selected.lat;customer.lon=selected.lon;render();if(window.roadGuardian)window.roadGuardian.locationSelected(customer.lat,customer.lon);setStatus('Location locked. Calculating actual road route...');calculateRoute();}
                function setSatellite(on){satellite=!!on;document.getElementById('credit').textContent=satellite?'Tiles © Esri':'© OpenStreetMap contributors';render();}
                function fitRoute(){const pts=[];if(customer.lat!==null)pts.push([customer.lat,customer.lon]);if(mechanic.lat!==null)pts.push([mechanic.lat,mechanic.lon]);if(route&&route.length)route.forEach(p=>pts.push([p[1],p[0]]));if(!pts.length)return;let minLat=90,maxLat=-90,minLon=180,maxLon=-180;pts.forEach(p=>{minLat=Math.min(minLat,p[0]);maxLat=Math.max(maxLat,p[0]);minLon=Math.min(minLon,p[1]);maxLon=Math.max(maxLon,p[1]);});center={lat:(minLat+maxLat)/2,lon:(minLon+maxLon)/2};if(minLat===maxLat&&minLon===maxLon)zoom=16;else{let z=18;for(;z>3;z--){const a=worldXY(minLat,minLon,z),b=worldXY(maxLat,maxLon,z);if(Math.abs(b.x-a.x)*TILE<mapEl.clientWidth*.72&&Math.abs(b.y-a.y)*TILE<mapEl.clientHeight*.72)break;}zoom=z;}render();}
                function sampleRoute(points,maxPoints){if(!points||points.length<=maxPoints)return points||[];const out=[],step=(points.length-1)/(maxPoints-1);for(let i=0;i<maxPoints;i++)out.push(points[Math.round(i*step)]);return out;}
                let routeBusy=false;
                let routeKey='';
                function calculateRoute(){
                  if(customer.lat===null||mechanic.lat===null){setStatus('Waiting for mechanic live location...');return;}
                  const key=Number(mechanic.lat).toFixed(6)+','+Number(mechanic.lon).toFixed(6)+'>'+Number(customer.lat).toFixed(6)+','+Number(customer.lon).toFixed(6);
                  if(routeBusy)return;
                  if(key===routeKey&&route&&route.length)return;
                  routeKey=key;routeBusy=true;setStatus('Calculating actual road route...');
                  if(window.roadGuardian){
                    window.roadGuardian.requestRoadRoute(String(mechanic.lat),String(mechanic.lon),String(customer.lat),String(customer.lon));
                  }else{
                    routeBusy=false;setStatus('Map bridge unavailable. Please reload the page.');
                  }
                }
function setRouteFromJava(points,distanceKm,etaMin){route=sampleRoute(points,700);routeBusy=false;render();fitRoute();setStatus('ROAD ROUTE • '+Number(distanceKm).toFixed(1)+' km • '+Math.max(1,Math.round(Number(etaMin)))+' min');showRouteStats(distanceKm,etaMin);if(window.roadGuardian)window.roadGuardian.routeUpdated(Number(distanceKm),Number(etaMin));}
                function showRouteStats(distanceKm,etaMin){const el=document.getElementById('info');if(el)el.innerHTML='<b>LIVE ROAD ROUTE</b><br>Customer: '+fmt(customer)+'<br>Mechanic: '+fmt(mechanic)+'<br><b>Distance: '+Number(distanceKm).toFixed(1)+' km</b><br><b>ETA: '+Math.max(1,Math.round(Number(etaMin)))+' min</b>';}
                async function fallbackRoadRoute(){if(customer.lat===null||mechanic.lat===null)return;try{setStatus('Calculating road route...');const u='https://router.project-osrm.org/route/v1/driving/'+mechanic.lon+','+mechanic.lat+';'+customer.lon+','+customer.lat+'?overview=full&geometries=geojson';const r=await fetch(u);const data=await r.json();if(!data.routes||!data.routes.length)throw new Error('No route');const rr=data.routes[0];route=sampleRoute(rr.geometry.coordinates,700);render();fitRoute();const km=rr.distance/1000,mins=rr.duration/60;setStatus('ROAD ROUTE • '+km.toFixed(1)+' km • '+Math.max(1,Math.round(mins))+' min');showRouteStats(km,mins);if(window.roadGuardian)window.roadGuardian.routeUpdated(km,mins);}catch(e){setStatus('Road route unavailable. Check internet connection.');}}
                function routeUnavailableFromJava(){route=null;render();routeBusy=false;setStatus('Road route unavailable. Check the route coordinates or internet connection.');}
                async function searchPlace(){const q=document.getElementById('placeSearch').value.trim(),box=document.getElementById('searchResults');if(!q)return;box.style.display='block';box.innerHTML='<div class="result">Searching...</div>';try{const r=await fetch('https://nominatim.openstreetmap.org/search?format=jsonv2&limit=5&q='+encodeURIComponent(q),{headers:{'Accept':'application/json'}}),places=await r.json();box.innerHTML='';if(!places.length){box.innerHTML='<div class="result">No place found.</div>';return;}places.forEach(pl=>{const d=document.createElement('div');d.className='result';d.textContent=pl.display_name;d.onclick=(ev)=>{ev.stopPropagation();searchPoint={lat:Number(pl.lat),lon:Number(pl.lon)};center={lat:searchPoint.lat,lon:searchPoint.lon};zoom=15;box.style.display='none';render();};box.appendChild(d);});}catch(e){box.innerHTML='<div class="result">Search unavailable. Check internet connection.</div>';}}
                function zoomAt(z,x,y){z=clamp(z,2,19);const r=mapEl.getBoundingClientRect(),px=x-r.left,py=y-r.top,c=centerWorld(),before=latLonFromWorld(c.x+(px-mapEl.clientWidth/2)/TILE,c.y+(py-mapEl.clientHeight/2)/TILE,zoom);zoom=z;const a=worldXY(before.lat,before.lon,zoom);center=latLonFromWorld(a.x-(px-mapEl.clientWidth/2)/TILE,a.y-(py-mapEl.clientHeight/2)/TILE,zoom);render();}
                document.getElementById('searchPanel').addEventListener('mousedown',e=>e.stopPropagation());document.getElementById('searchResults').addEventListener('mousedown',e=>e.stopPropagation());
                mapEl.addEventListener('mousedown',e=>{dragging=true;moved=false;startX=e.clientX;startY=e.clientY;startCenter={...center};mapEl.classList.add('dragging');});window.addEventListener('mousemove',e=>{if(!dragging)return;const dx=e.clientX-startX,dy=e.clientY-startY;if(Math.abs(dx)+Math.abs(dy)>4)moved=true;const c=worldXY(startCenter.lat,startCenter.lon,zoom);center=latLonFromWorld(c.x-dx/TILE,c.y-dy/TILE,zoom);render();});window.addEventListener('mouseup',()=>{dragging=false;mapEl.classList.remove('dragging');});
                mapEl.addEventListener('click',e=>{if(moved)return;const r=mapEl.getBoundingClientRect(),c=centerWorld(),p=latLonFromWorld(c.x+(e.clientX-r.left-mapEl.clientWidth/2)/TILE,c.y+(e.clientY-r.top-mapEl.clientHeight/2)/TILE,zoom);selected={lat:p.lat,lon:p.lon};locked=false;center={lat:p.lat,lon:p.lon};render();if(window.roadGuardian)window.roadGuardian.locationSelected(p.lat,p.lon);setStatus('Location selected. Press Lock / Confirm to save.');});
                mapEl.addEventListener('wheel',e=>{e.preventDefault();zoomAt(e.deltaY<0?zoom+1:zoom-1,e.clientX,e.clientY);},{passive:false});document.getElementById('zoomIn').onclick=()=>zoomAt(zoom+1,mapEl.clientWidth/2,mapEl.clientHeight/2);document.getElementById('zoomOut').onclick=()=>zoomAt(zoom-1,mapEl.clientWidth/2,mapEl.clientHeight/2);document.getElementById('roadButton').onclick=()=>setSatellite(false);document.getElementById('satButton').onclick=()=>setSatellite(true);document.getElementById('myButton').onclick=requestMyLocation;document.getElementById('lockButton').onclick=confirmLocation;document.getElementById('fitButton').onclick=fitRoute;document.getElementById('searchButton').onclick=searchPlace;document.getElementById('placeSearch').addEventListener('keydown',e=>{if(e.key==='Enter')searchPlace();});window.addEventListener('resize',render);
                window.setCustomerLocation=setCustomerLocation;window.lockCustomerLocation=(lat,lon)=>{selected={lat:Number(lat),lon:Number(lon)};locked=true;customer.lat=selected.lat;customer.lon=selected.lon;render();calculateRoute();};window.setMechanicLocation=setMechanicLocation;window.requestMyLocation=requestMyLocation;window.setSatellite=setSatellite;window.fitRoute=fitRoute;window.calculateRoute=calculateRoute;
                setTimeout(()=>{render();if(customer.lat!==null)calculateRoute();else setStatus('Click the map to choose the service location.');},100);
                </script></body></html>
                """.replace("INITIAL_LAT", initialLat).replace("INITIAL_LON", initialLon);
    }

    public class MapBridge {
        public void locationSelected(double lat, double lon) {
            Platform.runLater(() -> {
                pendingLatitude = lat;
                pendingLongitude = lon;
                if (externalRoadLabel != null) {
                    externalRoadLabel.setText(String.format(Locale.US, "%.6f, %.6f", lat, lon));
                }
            });
        }

        public void requestRoadRoute(String mechanicLatitude, String mechanicLongitude, String customerLatitude, String customerLongitude) {
            try {
                currentRequest.setMechanicLatitude(Double.parseDouble(mechanicLatitude));
                currentRequest.setMechanicLongitude(Double.parseDouble(mechanicLongitude));
                requestRoadRouteFromJava();
            } catch (Exception e) {
                System.err.println("Invalid live route coordinates: " + e.getMessage());
            }
        }

        public void routeUpdated(double distanceKm, double durationMin) {
            Platform.runLater(() -> {
                if (externalDistanceLabel != null) {
                    externalDistanceLabel.setText(String.format(Locale.US, "%.1f km", distanceKm));
                }
                if (externalEtaLabel != null) {
                    externalEtaLabel.setText(String.format(Locale.US, "%.0f min", durationMin));
                }
            });
        }

        public void routeUnavailable() {
            Platform.runLater(() -> {
                if (externalDistanceLabel != null) externalDistanceLabel.setText("Route unavailable");
                if (externalEtaLabel != null) externalEtaLabel.setText("Route unavailable");
            });
        }
    }

    // ============================================================
    // ROUTE MARKER
    // ============================================================

    private VBox createRouteMarker(
            String letter,
            String title,
            String subtitle,
            String color
    ) {

        VBox box =
                new VBox(5);

        box.setAlignment(
                Pos.CENTER
        );

        Circle marker =
                new Circle(20);

        marker.setFill(
                Color.web(color)
        );
        marker.setStroke(
                Color.WHITE
        );
        marker.setStrokeWidth(3);

        StackPane markerPane =
                new StackPane();

        Label markerText =
                new Label(letter);

        markerText.setStyle(
                "-fx-text-fill: white;"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
        );

        markerPane.getChildren()
                .addAll(
                        marker,
                        markerText
                );

        Label titleLabel =
                new Label(
                        firstNonBlank(
                                title,
                                "-"
                        )
                );

        titleLabel.setMaxWidth(175);
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(
                Pos.CENTER
        );
        titleLabel.setStyle(
                "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );

        Label subtitleLabel =
                new Label(
                        firstNonBlank(
                                subtitle,
                                "-"
                        )
                );

        subtitleLabel.setMaxWidth(175);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setAlignment(
                Pos.CENTER
        );
        subtitleLabel.setStyle(
                "-fx-text-fill: "
                        + SECONDARY_TEXT
                        + ";"
                        + "-fx-font-size: 9px;"
        );

        box.getChildren()
                .addAll(
                        markerPane,
                        titleLabel,
                        subtitleLabel
                );

        return box;
    }

    // ============================================================
    // MECHANIC CARD
    // ============================================================

    private VBox createMechanicCard() {

        VBox card =
                createCard();

        card.setPrefWidth(330);
        card.setMinWidth(300);

        Label title =
                createCardTitle(
                        "Assigned Mechanic"
                );

        Label mechanicName =
                new Label(
                        controller.getMechanicName(
                                currentRequest
                        )
                );

        mechanicName.setWrapText(true);
        mechanicName.setStyle(
                "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-font-size: 18px;"
                        + "-fx-font-weight: bold;"
        );

        String specialization =
                currentMechanic == null
                        ? "Specialization not available"
                        : firstNonBlank(
                                currentMechanic.getSpecialization(),
                                "Specialization not available"
                        );

        Label specializationLabel =
                new Label(
                        specialization
                );

        specializationLabel.setWrapText(true);
        specializationLabel.setStyle(
                "-fx-text-fill: "
                        + SECONDARY_TEXT
                        + ";"
                        + "-fx-font-size: 11px;"
        );

        VBox state =
                createInformationBox(
                        "TRACKING STATE",
                        getTrackingState()
                );

        VBox phone =
                createInformationBox(
                        "PHONE",
                        currentMechanic == null
                                ? "Not available"
                                : firstNonBlank(
                                currentMechanic.getPhone(),
                                "Not available"
                        )
                );

        VBox city =
                createInformationBox(
                        "MECHANIC CITY",
                        currentMechanic == null
                                ? "Not available"
                                : firstNonBlank(
                                currentMechanic.getCity(),
                                "Not available"
                        )
                );

        Button call =
                new Button(
                        "Call Mechanic"
                );

        call.setMaxWidth(
                Double.MAX_VALUE
        );
        call.setStyle(
                "-fx-background-color: "
                        + GREEN
                        + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 10 14 10 14;"
                        + "-fx-cursor: hand;"
        );

        String mechanicPhone =
                currentMechanic == null
                        ? null
                        : clean(
                                currentMechanic.getPhone()
                        );

        call.setDisable(
                mechanicPhone == null
        );

        call.setOnAction(
                event -> showInfo(
                        "Mechanic Contact",
                        mechanicPhone == null
                                ? "Mechanic phone number is not available."
                                : mechanicPhone
                )
        );

        card.getChildren()
                .addAll(
                        title,
                        mechanicName,
                        specializationLabel,
                        state,
                        phone,
                        city,
                        call
                );

        return card;
    }

    // ============================================================
    // REQUEST INFORMATION
    // ============================================================

    private VBox createRequestInformation() {

        VBox card =
                createCard();

        Label title =
                createCardTitle(
                        "Service Request Information"
                );

        HBox first =
                new HBox(10);

        first.getChildren()
                .addAll(
                        createInformationBox(
                                "CUSTOMER",
                                firstNonBlank(
                                        currentRequest.getCustomerName(),
                                        UserSession.getUserName(),
                                        "Customer"
                                )
                        ),
                        createInformationBox(
                                "VEHICLE",
                                controller.getVehicleDisplay(
                                        currentRequest
                                )
                        ),
                        createInformationBox(
                                "SERVICE",
                                firstNonBlank(
                                        currentRequest.getServiceType(),
                                        "Service Request"
                                )
                        ),
                        createInformationBox(
                                "STATUS",
                                controller.getStatusDisplay(
                                        currentRequest
                                )
                        )
                );

        for (javafx.scene.Node node :
                first.getChildren()) {
            HBox.setHgrow(
                    node,
                    Priority.ALWAYS
            );
        }

        HBox second =
                new HBox(10);

        second.getChildren()
                .addAll(
                        createInformationBox(
                                "SELECTED LOCATION",
                                controller.getCoordinatesDisplay(
                                        currentRequest
                                )
                        ),
                        createInformationBox(
                                "PROBLEM",
                                firstNonBlank(
                                        currentRequest.getDescription(),
                                        currentRequest.getServiceType(),
                                        "Not provided"
                                )
                        ),
                        createInformationBox(
                                "NAVIGATION STARTED",
                                formatTime(
                                        currentRequest.getNavigationStartedDate()
                                )
                        ),
                        createInformationBox(
                                "ARRIVED",
                                formatTime(
                                        currentRequest.getArrivedDate()
                                )
                        )
                );

        for (javafx.scene.Node node :
                second.getChildren()) {
            HBox.setHgrow(
                    node,
                    Priority.ALWAYS
            );
        }

        card.getChildren()
                .addAll(
                        title,
                        first,
                        second
                );

        return card;
    }

    // ============================================================
    // METRIC CARD
    // ============================================================

    private VBox createMetricCard(
            String title,
            String value,
            String accent
    ) {

        VBox card =
                new VBox(5);

        card.setPadding(
                new Insets(14)
        );
        card.setMaxWidth(
                Double.MAX_VALUE
        );
        card.setStyle(
                "-fx-background-color: "
                        + CARD_SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 10;"
                        + "-fx-background-radius: 10;"
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-text-fill: "
                        + SECONDARY_TEXT
                        + ";"
                        + "-fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
        );

        Label valueLabel =
                new Label(
                        firstNonBlank(
                                value,
                                "-"
                        )
                );

        valueLabel.setWrapText(true);
        valueLabel.setStyle(
                "-fx-text-fill: "
                        + accent
                        + ";"
                        + "-fx-font-size: 14px;"
                        + "-fx-font-weight: bold;"
        );

        card.getChildren()
                .addAll(
                        titleLabel,
                        valueLabel
                );

        return card;
    }

    // ============================================================
    // CARD
    // ============================================================

    private VBox createCard() {

        VBox card =
                new VBox(14);

        card.setPadding(
                new Insets(20)
        );
        card.setMaxWidth(
                Double.MAX_VALUE
        );
        card.setStyle(
                "-fx-background-color: "
                        + CARD_SURFACE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 14;"
                        + "-fx-background-radius: 14;"
        );

        return card;
    }

    private Label createCardTitle(
            String text
    ) {

        Label title =
                new Label(text);

        title.setStyle(
                "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
        );

        return title;
    }

    // ============================================================
    // INFORMATION BOX
    // ============================================================

    private VBox createInformationBox(
            String title,
            String value
    ) {

        VBox box =
                new VBox(5);

        box.setPadding(
                new Insets(11)
        );
        box.setMaxWidth(
                Double.MAX_VALUE
        );
        box.setStyle(
                "-fx-background-color: "
                        + WHITE
                        + ";"
                        + "-fx-border-color: "
                        + BORDER
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-text-fill: "
                        + SECONDARY_TEXT
                        + ";"
                        + "-fx-font-size: 8px;"
                        + "-fx-font-weight: bold;"
        );

        String accent = switch (title) {
            case "ETA" -> "#60A5FA";
            case "DISTANCE" -> "#F59E0B";
            case "CUSTOMER GPS" -> "#22C55E";
            case "MECHANIC GPS" -> "#A78BFA";
            default -> BLUE;
        };

        Label valueLabel =
                new Label(
                        firstNonBlank(
                                value,
                                "-"
                        )
                );

        valueLabel.setWrapText(true);
        valueLabel.setStyle(
                "-fx-text-fill: "
                        + accent
                        + ";"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
        );

        box.getChildren()
                .addAll(
                        titleLabel,
                        valueLabel
                );

        return box;
    }

    // ============================================================
    // SMALL VALUE
    //
    // Returns the value label. Its parent is the complete box so
    // legacy updateTracking() can update the value directly.
    // ============================================================

    private Label createSmallValue(
            String title,
            String value
    ) {

        VBox box =
                new VBox(4);

        box.setPadding(
                new Insets(10)
        );
        box.setMaxWidth(
                Double.MAX_VALUE
        );
        String accent = switch (title) {
            case "ETA" -> "#60A5FA";
            case "DISTANCE" -> "#F59E0B";
            case "CUSTOMER GPS" -> "#22C55E";
            case "MECHANIC GPS" -> "#A78BFA";
            default -> BLUE;
        };

        box.setStyle(
                "-fx-background-color: " + CARD_SURFACE + ";"
                        + "-fx-border-color: " + accent + ";"
                        + "-fx-border-width: 1.2;"
                        + "-fx-border-radius: 10;"
                        + "-fx-background-radius: 10;"
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-text-fill: "
                        + SECONDARY_TEXT
                        + ";"
                        + "-fx-font-size: 8px;"
                        + "-fx-font-weight: bold;"
        );

        Label valueLabel =
                new Label(
                        firstNonBlank(
                                value,
                                "Not available"
                        )
                );

        valueLabel.setWrapText(true);
        valueLabel.setStyle(
                "-fx-text-fill: "
                        + accent
                        + ";"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );

        box.getChildren()
                .addAll(
                        titleLabel,
                        valueLabel
                );

        return valueLabel;
    }

    // ============================================================
    // EMPTY STATE
    // ============================================================

    private VBox createEmptyState() {

        VBox card =
                createCard();

        card.setAlignment(
                Pos.CENTER
        );
        card.setPadding(
                new Insets(55)
        );

        Label title =
                new Label(
                        "No Mechanic to Track"
                );

        title.setStyle(
                "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-font-size: 19px;"
                        + "-fx-font-weight: bold;"
        );

        Label description =
                new Label(
                        "Live tracking becomes available after a mechanic is assigned to an active service request."
                );

        description.setWrapText(true);
        description.setMaxWidth(600);
        description.setAlignment(
                Pos.CENTER
        );
        description.setStyle(
                "-fx-text-fill: "
                        + SECONDARY_TEXT
                        + ";"
                        + "-fx-font-size: 12px;"
        );

        card.getChildren()
                .addAll(
                        title,
                        description
                );

        return card;
    }

    // ============================================================
    // ERROR STATE
    // ============================================================

    private void showErrorState(
            String message
    ) {

        if (trackingHost == null) {
            return;
        }

        VBox card =
                createCard();

        card.setAlignment(
                Pos.CENTER
        );

        Label title =
                new Label(
                        "Tracking Data Error"
                );

        title.setStyle(
                "-fx-text-fill: "
                        + RED
                        + ";"
                        + "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
        );

        Label description =
                new Label(
                        firstNonBlank(
                                message,
                                "Unable to load tracking data."
                        )
                );

        description.setStyle(
                "-fx-text-fill: "
                        + HEADING
                        + ";"
                        + "-fx-font-size: 11px;"
        );

        card.getChildren()
                .addAll(
                        title,
                        description
                );

        trackingHost
                .getChildren()
                .setAll(card);
    }

    // ============================================================
    // TRACKING STATE
    // ============================================================

    private String getTrackingState() {

        if (currentRequest == null) {
            return "Not available";
        }

        if (currentRequest.hasArrived()) {
            return "Arrived";
        }

        if (currentRequest.isNavigationStarted()) {
            return "Navigation Started";
        }

        String status =
                controller.getStatusDisplay(
                        currentRequest
                );

        if ("Assigned".equalsIgnoreCase(status)) {
            return "Waiting for Mechanic Acceptance";
        }

        if ("Accepted".equalsIgnoreCase(status)) {
            return "Accepted - Navigation Pending";
        }

        if ("In Progress".equalsIgnoreCase(status)) {
            return "Repair In Progress";
        }

        return status;
    }

    // ============================================================
    // TRACKABLE
    // ============================================================

    private boolean isTrackable(
            ServiceRequest request
    ) {

        if (request == null
                || !request.hasMechanic()) {
            return false;
        }

        String status =
                firstNonBlank(
                        request.getStatus(),
                        "Pending"
                );

        return status.equalsIgnoreCase("Assigned")
                || status.equalsIgnoreCase("Accepted")
                || status.equalsIgnoreCase("In Progress")
                || status.equalsIgnoreCase("InProgress")
                || status.equalsIgnoreCase("Active");
    }

    // ============================================================
    // STATUS COLOR
    // ============================================================

    private String getStatusColor(
            String status
    ) {

        if (status == null) {
            return SECONDARY_TEXT;
        }

        if (status.equalsIgnoreCase("Pending")) {
            return ORANGE;
        }

        if (status.equalsIgnoreCase("Assigned")) {
            return BLUE;
        }

        if (status.equalsIgnoreCase("Accepted")) {
            return PURPLE;
        }

        if (status.equalsIgnoreCase("In Progress")) {
            return BLUE;
        }

        if (status.equalsIgnoreCase("Completed")) {
            return GREEN;
        }

        if (status.equalsIgnoreCase("Cancelled")) {
            return RED;
        }

        return HEADING;
    }

    // ============================================================
    // FORMAT TIME
    // ============================================================

    private String formatTime(
            String value
    ) {

        value = clean(value);

        if (value == null) {
            return "Not available";
        }

        try {

            long millis =
                    Long.parseLong(value);

            if (millis > 0
                    && millis < 100000000000L) {
                millis *= 1000L;
            }

            java.time.Instant instant =
                    java.time.Instant.ofEpochMilli(
                            millis
                    );

            java.time.ZonedDateTime dateTime =
                    instant.atZone(
                            java.time.ZoneId.systemDefault()
                    );

            return java.time.format.DateTimeFormatter
                    .ofPattern(
                            "dd MMM yyyy, hh:mm a"
                    )
                    .format(dateTime);

        } catch (Exception ignored) {

            return value;
        }
    }

    // ============================================================
    // LEGACY EXTERNAL GPS UPDATE HOOK
    //
    // Kept for compatibility with the old LiveMapPage API.
    // This updates the visible labels only. It does NOT create
    // fake Firebase data or overwrite the service request.
    // ============================================================

    public void updateTracking(
            String mechanic,
            int eta,
            double distance,
            String road
    ) {

        if (externalEtaLabel != null) {
            externalEtaLabel.setText(
                    eta >= 0
                            ? eta + " min"
                            : "Not available"
            );
        }

        if (externalDistanceLabel != null) {
            externalDistanceLabel.setText(
                    distance >= 0
                            ? String.format(
                            "%.1f km",
                            distance
                    )
                            : "Not available"
            );
        }

        if (externalRoadLabel != null
                && clean(road) != null) {
            externalRoadLabel.setText(road);
        }

        if (pageSubtitle != null
                && clean(mechanic) != null) {
            pageSubtitle.setText(
                    mechanic
                            + " tracking data updated."
            );
        }
    }

    // ============================================================
    // ALERT
    // ============================================================

    private void showInfo(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "RoadGuardian"
        );
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ============================================================
    // STRING HELPERS
    // ============================================================

    private String firstNonBlank(
            String... values
    ) {

        if (values == null) {
            return null;
        }

        for (String value : values) {

            String cleaned = clean(value);

            if (cleaned != null) {
                return cleaned;
            }
        }

        return null;
    }

    private String clean(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }

    // ============================================================
    // TEST APPLICATION
    // ============================================================

    public static void main(
            String[] args
    ) {

        javafx.application.Application.launch(
                LiveMapApplication.class,
                args
        );
    }

    public static class LiveMapApplication
            extends javafx.application.Application {

        @Override
        public void start(
                Stage stage
        ) {

            LiveMapPage page =
                    new LiveMapPage();

            stage.setTitle(
                    "RoadGuardian - Live Tracking"
            );
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            stage.setScene(
                    page.getLiveMapScene()
            );
            stage.show();
        }
    }
}
