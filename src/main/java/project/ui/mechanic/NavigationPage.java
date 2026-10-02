package project.ui.mechanic;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import project.controller.mechanic.ActiveJobController;
import project.dao.mechanic.ActiveJobDAO;
import project.model.ServiceRequest;
import project.util.Theme;
import project.util.IconUtil;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import project.firebase.FirebaseConfig;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mechanic Navigation page.
 *
 * Uses the mechanic's real current Accepted / In Progress job.
 * Start Navigation and Mark Arrived update the SAME serviceRequests
 * document that the customer LiveMapPage reads.
 *
 * No fake customer, vehicle, distance, ETA or location values.
 */
public class NavigationPage {

    // =====================================================
    // CONTROLLER / DATA
    // =====================================================

    private final ActiveJobController controller;
    private ServiceRequest currentJob;

    // =====================================================
    // UI
    // =====================================================

    private VBox contentHost;
    private Button refreshButton;

    private final ActiveJobDAO activeJobDAO;
    private final Firestore firestore;

    private WebView mapWebView;
    private WebEngine mapEngine;
    private Label distanceValueLabel;
    private Label etaValueLabel;
    private long routeRequestSequence = 0L;
    private Label mechanicCoordinatesLabel;
    private Label mechanicLocationStatusLabel;
    private Button mechanicLockButton;
    private boolean mechanicLocationLocked;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public NavigationPage() {

        controller =
                new ActiveJobController();

        activeJobDAO =
                new ActiveJobDAO();
        try {
            firestore = FirebaseConfig.getFirestore();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Firestore.", e);
        }
    }

    // =====================================================
    // PUBLIC CONTENT
    // =====================================================

    public ScrollPane getContent() {

        VBox root =
                new VBox(20);

        root.setPadding(
                new Insets(
                        30,
                        35,
                        35,
                        35
                )
        );

        root.setStyle(
                "-fx-background-color: "
                        + Theme.BACKGROUND
                        + ";"
        );

        root.getChildren()
                .add(
                        createHeader()
                );

        contentHost =
                new VBox(18);

        contentHost.setFillWidth(true);

        root.getChildren()
                .add(
                        contentHost
                );

        VBox.setVgrow(
                contentHost,
                Priority.ALWAYS
        );

        ScrollPane scrollPane =
                new ScrollPane(root);

        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );
        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );
        scrollPane.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-background: "
                        + Theme.BACKGROUND
                        + ";"
                        + "-fx-border-color: transparent;"
        );

        refreshJob();

        return scrollPane;
    }

    // =====================================================
    // HEADER
    // =====================================================

    private HBox createHeader() {

        HBox header =
                new HBox(15);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox text =
                new VBox(5);

        Label title =
                new Label(
                        "Navigation"
                );

        title.setStyle(
                "-fx-font-size: 27px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.HEADING
                        + ";"
        );

        Label subtitle =
                new Label(
                        "Navigate to the customer using the active Firebase service request."
                );

        subtitle.setStyle(
                "-fx-font-size: 12px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        text.getChildren()
                .addAll(
                        title,
                        subtitle
                );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        refreshButton =
                new Button(
                        "Refresh"
                );

        refreshButton.setStyle(
                "-fx-background-color: "
                        + Theme.INFO
                        + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 9 15 9 15;"
                        + "-fx-cursor: hand;"
        );

        refreshButton.setOnAction(
                event -> refreshJob()
        );

        header.getChildren()
                .addAll(
                        text,
                        spacer,
                        refreshButton
                );

        return header;
    }

    // =====================================================
    // REFRESH JOB
    // =====================================================

    private void refreshJob() {

        if (contentHost == null) {
            return;
        }

        if (refreshButton != null) {
            refreshButton.setDisable(true);
        }

        try {

            currentJob =
                    controller
                            .refreshCurrentActiveJob();

            if (currentJob != null
                    && (currentJob.getMechanicLatitude() == null
                    || currentJob.getMechanicLongitude() == null)) {
                loadAssignedMechanicProfileLocation();
            }

            mechanicLocationLocked = currentJob != null
                    && currentJob.getMechanicLatitude() != null
                    && currentJob.getMechanicLongitude() != null;

            if (currentJob == null) {

                contentHost
                        .getChildren()
                        .setAll(
                                createEmptyState()
                        );

                return;
            }

            contentHost
                    .getChildren()
                    .setAll(
                            createInfoCards(),
                            createMainRow(),
                            createTrackingTimeline()
                    );

        } catch (Exception e) {

            e.printStackTrace();

            contentHost
                    .getChildren()
                    .setAll(
                            createErrorState(
                                    "Unable to load the active job."
                            )
                    );

        } finally {

            if (refreshButton != null) {
                refreshButton.setDisable(false);
            }
        }
    }

    // =====================================================
    // LOAD ASSIGNED MECHANIC PROFILE LOCATION
    // =====================================================

    private void loadAssignedMechanicProfileLocation() {
        if (currentJob == null || firestore == null) return;

        String mechanicId = clean(currentJob.getMechanicId());
        if (mechanicId == null) return;
        String requestId = clean(currentJob.getRequestId());
        if (requestId == null) return;

        Thread worker = new Thread(() -> {
            try {
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

                if (!mechanic.exists()) return;

                Double lat = numberValue(mechanic.get("latitude"));
                Double lon = numberValue(mechanic.get("longitude"));
                if (lat == null || lon == null) return;

                Map<String, Object> update = new HashMap<>();
                update.put("mechanicLatitude", lat);
                update.put("mechanicLongitude", lon);
                update.put("mechanicLocationUpdatedAt", String.valueOf(System.currentTimeMillis()));
                firestore.collection("serviceRequests").document(requestId)
                        .set(update, SetOptions.merge()).get();

                Platform.runLater(() -> {
                    currentJob.setMechanicLatitude(lat);
                    currentJob.setMechanicLongitude(lon);
                    currentJob.setMechanicLocationUpdatedAt(String.valueOf(System.currentTimeMillis()));
                    mechanicLocationLocked = true;
                    if (mechanicCoordinatesLabel != null) {
                        mechanicCoordinatesLabel.setText(String.format(Locale.US, "%.6f, %.6f", lat, lon));
                    }
                    if (mechanicLocationStatusLabel != null) {
                        mechanicLocationStatusLabel.setText("Mechanic GPS: location loaded and saved");
                    }
                    if (mechanicLockButton != null) {
                        mechanicLockButton.setText("Mechanic Locked");
                        styleMapButton(mechanicLockButton, Theme.SUCCESS);
                    }
                    executeMapScript("setMechanicLocation(" + lat + "," + lon + ",false);");
                    requestRoadRouteFromJava();
                });
            } catch (Exception e) {
                System.err.println("Unable to load mechanic profile location: " + e.getMessage());
            }
        }, "roadguardian-mechanic-profile-location");
        worker.setDaemon(true);
        worker.start();
    }

    // =====================================================
    // INFO CARDS
    // =====================================================

    private HBox createInfoCards() {

        HBox cards =
                new HBox(12);

        cards.getChildren()
                .addAll(
                        createInfoCardWithLabel(
                                "DISTANCE",
                                "Waiting for GPS",
                                "Live road distance",
                                Theme.INFO,
                                true
                        ),
                        createInfoCardWithLabel(
                                "EST. ARRIVAL",
                                "Waiting for GPS",
                                "Live road ETA",
                                Theme.PRIMARY,
                                false
                        ),
                        createInfoCard(
                                "CUSTOMER",
                                controller.getCustomerName(
                                        currentJob
                                ),
                                "Current service request",
                                Theme.SUCCESS
                        ),
                        createInfoCard(
                                "STATUS",
                                controller.getStatusDisplay(
                                        currentJob
                                ),
                                "Firebase lifecycle status",
                                getStatusColor(
                                        currentJob.getStatus()
                                )
                        )
                );

        for (javafx.scene.Node node :
                cards.getChildren()) {

            HBox.setHgrow(
                    node,
                    Priority.ALWAYS
            );
        }

        return cards;
    }

    private VBox createInfoCard(
            String title,
            String value,
            String subtitle,
            String accent
    ) {

        VBox card =
                new VBox(5);

        card.setPadding(
                new Insets(15)
        );
        card.setMaxWidth(
                Double.MAX_VALUE
        );
        card.setStyle(
                "-fx-background-color: "
                        + Theme.CARD
                        + ";"
                        + "-fx-border-color: "
                        + "#FACC15"
                        + ";"
                        + "-fx-border-radius: 10;"
                        + "-fx-background-radius: 10;"
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-font-size: 8px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
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
                "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + accent
                        + ";"
        );

        Label subtitleLabel =
                new Label(subtitle);

        subtitleLabel.setWrapText(true);
        subtitleLabel.setStyle(
                "-fx-font-size: 9px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        card.getChildren()
                .addAll(
                        titleLabel,
                        valueLabel,
                        subtitleLabel
                );

        return card;
    }

    private VBox createInfoCardWithLabel(
            String title,
            String value,
            String subtitle,
            String accent,
            boolean distance
    ) {

        VBox card =
                createInfoCard(
                        title,
                        value,
                        subtitle,
                        accent
                );

        if (distance) {
            distanceValueLabel =
                    (Label) card.getChildren().get(1);
        } else {
            etaValueLabel =
                    (Label) card.getChildren().get(1);
        }

        return card;
    }

    // =====================================================
    // MAIN ROW
    // =====================================================

    private HBox createMainRow() {

        HBox row =
                new HBox(18);

        row.setAlignment(
                Pos.TOP_LEFT
        );

        VBox mapPanel =
                createRoutePanel();

        VBox customerPanel =
                createCustomerPanel();

        HBox.setHgrow(
                mapPanel,
                Priority.ALWAYS
        );

        row.getChildren()
                .addAll(
                        mapPanel,
                        customerPanel
                );

        return row;
    }

    // =====================================================
    // ROUTE PANEL
    // =====================================================

    private VBox createRoutePanel() {

        VBox panel =
                createCard();

        panel.setMinHeight(520);

        Label title =
                createSectionTitle(
                        "Live Customer Route"
                );

        Label note =
                new Label(
                        "Live road map • mechanic location is updated from current device location • route, distance and ETA use road-network data."
                );

        note.setWrapText(true);
        note.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        mapWebView =
                new WebView();

        mapWebView.setPrefHeight(500);
        mapWebView.setMinHeight(500);
        mapWebView.setPrefWidth(900);
        mapWebView.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(mapWebView, Priority.ALWAYS);
        mapWebView.setFocusTraversable(false);

        mapEngine =
                mapWebView.getEngine();

        mapEngine.setJavaScriptEnabled(true);

        String customerLat =
                currentJob.getLatitude() == null
                        ? "null"
                        : currentJob.getLatitude().toString();

        String customerLon =
                currentJob.getLongitude() == null
                        ? "null"
                        : currentJob.getLongitude().toString();

        String mechanicLat =
                currentJob.getMechanicLatitude() == null
                        ? "null"
                        : currentJob.getMechanicLatitude().toString();

        String mechanicLon =
                currentJob.getMechanicLongitude() == null
                        ? "null"
                        : currentJob.getMechanicLongitude().toString();

        mapEngine.getLoadWorker()
                .stateProperty()
                .addListener(
                        (obs, oldState, newState) -> {
                            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                                try {
                                    JSObject window =
                                            (JSObject) mapEngine.executeScript("window");
                                    window.setMember("javaBridge", new MapBridge());
                                    requestRoadRouteFromJava();
                                    if (currentJob != null && currentJob.isNavigationStarted()) {
                                        executeMapScript("startLocationTracking();");
                                    }
                                } catch (Exception e) {
                                    System.err.println(
                                            "Unable to connect map bridge: "
                                                    + e.getMessage()
                                    );
                                }
                            }
                        }
                );

        mapEngine.loadContent(
                createMapHtml(
                        customerLat,
                        customerLon,
                        mechanicLat,
                        mechanicLon
                )
        );

        HBox controls =
                new HBox(8);

        controls.setAlignment(Pos.CENTER_LEFT);

        Button locationButton =
                new Button("My Location");

        Button roadButton =
                new Button("Road");

        Button satelliteButton =
                new Button("Satellite");

        Button fitButton =
                new Button("Fit Route");

        Button lockButton =
                new Button(mechanicLocationLocked
                        ? "Mechanic Locked"
                        : "Lock / Confirm Mechanic");

        Button viewRouteButton =
                new Button("View Route");

        styleMapButton(locationButton, Theme.INFO);
        styleMapButton(roadButton, Theme.PRIMARY);
        styleMapButton(satelliteButton, Theme.SUCCESS);
        styleMapButton(fitButton, Theme.INFO);
        styleMapButton(lockButton, mechanicLocationLocked ? Theme.SUCCESS : Theme.PRIMARY);
        styleMapButton(viewRouteButton, Theme.INFO);
        mechanicLockButton = lockButton;

        locationButton.setOnAction(
                event -> executeMapScript("requestMyLocation()")
        );

        roadButton.setOnAction(
                event -> executeMapScript("setMapLayer('road')")
        );

        satelliteButton.setOnAction(
                event -> executeMapScript("setMapLayer('satellite')")
        );

        fitButton.setOnAction(
                event -> executeMapScript("fitRoute()")
        );

        lockButton.setOnAction(
                event -> executeMapScript("confirmMechanicLocation()")
        );

        viewRouteButton.setOnAction(
                event -> executeMapScript("viewRoute()")
        );

        controls.getChildren()
                .addAll(
                        locationButton,
                        roadButton,
                        satelliteButton,
                        fitButton,
                        lockButton,
                        viewRouteButton
                );

        HBox details =
                new HBox(10);

        VBox customerGpsBox = createInformationBox(
                "CUSTOMER GPS",
                getCoordinatesDisplay(currentJob)
        );

        VBox mechanicGpsBox = createInformationBox(
                "MECHANIC GPS",
                currentJob.getMechanicLatitude() != null
                        && currentJob.getMechanicLongitude() != null
                        ? String.format(java.util.Locale.US, "%.6f, %.6f",
                                currentJob.getMechanicLatitude(), currentJob.getMechanicLongitude())
                        : "Not locked / not available"
        );
        mechanicCoordinatesLabel = (Label) mechanicGpsBox.getChildren().get(1);

        details.getChildren()
                .addAll(
                        customerGpsBox,
                        mechanicGpsBox,
                        createInformationBox(
                                "REQUEST ID",
                                currentJob.getRequestId()
                        )
                );

        for (javafx.scene.Node node :
                details.getChildren()) {
            HBox.setHgrow(
                    node,
                    Priority.ALWAYS
            );
        }

        Label locationStatus =
                new Label(
                        currentJob.getMechanicLatitude() != null
                                && currentJob.getMechanicLongitude() != null
                                ? "Mechanic GPS: live location available"
                                : "Mechanic GPS: waiting for current device location"
                );

        mechanicLocationStatusLabel = locationStatus;

        locationStatus.setWrapText(true);
        locationStatus.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        panel.getChildren()
                .addAll(
                        title,
                        note,
                        mapWebView,
                        controls,
                        locationStatus,
                        details
                );

        return panel;
    }

    private void styleMapButton(
            Button button,
            String color
    ) {

        button.setStyle(
                "-fx-background-color: "
                        + color
                        + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 7;"
                        + "-fx-padding: 8 12 8 12;"
                        + "-fx-cursor: hand;"
        );
    }

    private void executeMapScript(
            String script
    ) {

        if (mapEngine == null
                || script == null) {
            return;
        }

        try {
            mapEngine.executeScript(script);
        } catch (Exception ignored) {
            // Map may not be loaded yet.
        }
    }

    private String createMapHtml(
            String customerLat,
            String customerLon,
            String mechanicLat,
            String mechanicLon
    ) {
        String html = """
                <!doctype html>
                <html><head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                  html,body{width:100%;height:100%;margin:0;overflow:hidden;font-family:Arial,sans-serif;background:#dfe3e8}
                  #map{position:relative;width:100%;height:100%;overflow:hidden;cursor:grab;background:#dfe3e8;user-select:none}
                  #map.dragging{cursor:grabbing}
                  #tiles{position:absolute;left:0;top:0;will-change:transform}
                  .tile{position:absolute;width:256px;height:256px;user-select:none;pointer-events:none}
                  #routeSvg{position:absolute;left:0;top:0;width:100%;height:100%;pointer-events:none;overflow:visible}\n                  @keyframes routePulse{0%,100%{opacity:.78}50%{opacity:1}} .routePulse{animation:routePulse 1.8s ease-in-out infinite}
                  #markers{position:absolute;left:0;top:0;width:100%;height:100%;pointer-events:none}
                  .marker{position:absolute;width:30px;height:30px;border-radius:50%;border:3px solid white;box-shadow:0 2px 8px rgba(0,0,0,.45);transform:translate(-50%,-50%);display:flex;align-items:center;justify-content:center;color:white;font-weight:bold;font-size:14px;pointer-events:none}
                  .customer{background:#F59E0B}.mechanic{background:#22C55E}.searchMarker{background:#2563EB}
                  .panel{position:absolute;z-index:20;background:rgba(20,20,24,.95);color:white;border-radius:9px;box-shadow:0 2px 12px rgba(0,0,0,.35)}
                  #searchPanel{top:10px;left:10px;padding:7px;display:flex;gap:6px}
                  #placeSearch{width:210px;border:0;outline:0;border-radius:6px;padding:8px 9px;font-size:12px}
                  .panel button{border:0;border-radius:6px;padding:8px 10px;background:#2563EB;color:white;font-weight:bold;cursor:pointer}
                  #searchResults{top:52px;left:10px;width:320px;display:none;max-height:210px;overflow:auto}
                  .result{padding:9px 10px;border-bottom:1px solid rgba(255,255,255,.1);font-size:11px;cursor:pointer}.result:hover{background:rgba(255,255,255,.1)}
                  #mapTools{top:10px;right:10px;padding:7px;display:flex;gap:5px;flex-wrap:wrap;max-width:330px}
                  #mapTools button{padding:7px 9px;font-size:11px}
                  #info{bottom:10px;left:10px;padding:9px 11px;font-size:11px;line-height:1.45;min-width:185px}
                  #zoom{position:absolute;z-index:20;right:10px;bottom:78px;display:flex;flex-direction:column;gap:4px}
                  #zoom button{width:34px;height:34px;padding:0;font-size:20px}
                  #credit{position:absolute;z-index:20;right:6px;bottom:5px;background:rgba(255,255,255,.8);color:#333;padding:2px 5px;border-radius:3px;font-size:9px}
                </style></head>
                <body>
                <div id="map">
                  <div id="tiles"></div><svg id="routeSvg"></svg><div id="markers"></div>
                  <div id="searchPanel" class="panel"><input id="placeSearch" placeholder="Search place or area..."><button id="searchButton">Search</button></div>
                  <div id="searchResults" class="panel"></div>
                  <div id="mapTools" class="panel">
                    <button id="roadButton">Road</button><button id="satButton">Satellite</button>
                    <button id="myButton">My Location</button><button id="fitButton">Fit Route</button>
                  </div>
                  <div id="zoom"><button id="zoomIn">+</button><button id="zoomOut">−</button></div>
                  <div id="info" class="panel"><strong>ROADGUARDIAN MAP</strong><br><span id="status">Loading map...</span><br><span id="coords">Mechanic: --<br>Customer: --</span></div>
                  <div id="credit">© OpenStreetMap contributors</div>
                </div>
                <script>
                const customer = {lat:CUSTOMER_LAT,lon:CUSTOMER_LON};
                const mechanic = {lat:MECHANIC_LAT,lon:MECHANIC_LON};
                let zoom = customer.lat!==null ? 15 : 5;
                let center = customer.lat!==null ? {lat:customer.lat,lon:customer.lon} : {lat:20.5937,lon:78.9629};
                let satellite=false, route=null, searchPoint=null, mechanicLocked=mechanic.lat!==null&&mechanic.lon!==null, dragging=false, moved=false, startX=0,startY=0,startCenter=null;
                const TILE=256, mapEl=document.getElementById('map'), tilesEl=document.getElementById('tiles'), markersEl=document.getElementById('markers'), svg=document.getElementById('routeSvg');
                const statusEl=document.getElementById('status'), coordsEl=document.getElementById('coords');
                function clamp(v,a,b){return Math.max(a,Math.min(b,v));}
                function worldXY(lat,lon,z){
                  const n=Math.pow(2,z), lr=clamp(lat,-85.05112878,85.05112878)*Math.PI/180;
                  return {x:(lon+180)/360*n,y:(1-Math.log(Math.tan(lr)+1/Math.cos(lr))/Math.PI)/2*n};
                }
                function latLonFromWorld(x,y,z){
                  const n=Math.pow(2,z); x=((x%n)+n)%n;
                  const lon=x/n*360-180, a=Math.PI*(1-2*y/n);
                  return {lat:Math.atan(Math.sinh(a))*180/Math.PI,lon:lon};
                }
                function centerWorld(){return worldXY(center.lat,center.lon,zoom);}
                function screenFromLatLon(lat,lon){
                  const c=centerWorld(), p=worldXY(lat,lon,zoom), n=Math.pow(2,zoom);
                  let dx=p.x-c.x; if(dx>n/2)dx-=n;if(dx<-n/2)dx+=n;
                  return {x:mapEl.clientWidth/2+dx*TILE,y:mapEl.clientHeight/2+(p.y-c.y)*TILE};
                }
                function render(){
                  const c=centerWorld(), w=mapEl.clientWidth,h=mapEl.clientHeight;
                  if(!w||!h)return;
                  const left=c.x-w/(2*TILE), top=c.y-h/(2*TILE), right=c.x+w/(2*TILE), bottom=c.y+h/(2*TILE);
                  const minX=Math.floor(left)-1,maxX=Math.floor(right)+1,minY=Math.max(0,Math.floor(top)-1),maxY=Math.min(Math.pow(2,zoom)-1,Math.floor(bottom)+1);
                  const needed=new Set(); tilesEl.innerHTML='';
                  for(let ty=minY;ty<=maxY;ty++) for(let tx=minX;tx<=maxX;tx++){
                    const n=Math.pow(2,zoom), wx=((tx%n)+n)%n, key=zoom+'/'+wx+'/'+ty; needed.add(key);
                    const img=document.createElement('img');img.className='tile';img.draggable=false;img.alt='';
                    img.style.left=((tx-left)*TILE)+'px';img.style.top=((ty-top)*TILE)+'px';
                    img.src=satellite
                      ? 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/'+zoom+'/'+ty+'/'+wx
                      : 'https://tile.openstreetmap.org/'+zoom+'/'+wx+'/'+ty+'.png';
                    img.onerror=function(){this.style.opacity='.15'};tilesEl.appendChild(img);
                  }
                  renderMarkers(); renderRoute();
                  coordsEl.innerHTML='Mechanic: '+fmt(mechanic)+'<br>Customer: '+fmt(customer);
                }
                function fmt(p){return p.lat===null?'--':Number(p.lat).toFixed(6)+', '+Number(p.lon).toFixed(6);}
                function marker(p,cls){
                  const s=screenFromLatLon(p.lat,p.lon),m=document.createElement('div');m.className='marker '+cls;m.style.left=s.x+'px';m.style.top=s.y+'px';markersEl.appendChild(m);
                }
                function renderMarkers(){markersEl.innerHTML='';if(customer.lat!==null)marker(customer,'customer');if(mechanic.lat!==null)marker(mechanic,'mechanic');if(searchPoint)marker(searchPoint,'searchMarker');}
                function renderRoute(){
                  svg.innerHTML=''; if(!route||route.length)return;
                  const pts=route.map(p=>screenFromLatLon(p[1],p[0]));
                  const base=document.createElementNS('http://www.w3.org/2000/svg','polyline');
                  base.setAttribute('points',pts.map(p=>p.x+','+p.y).join(' '));base.setAttribute('fill','none');base.setAttribute('stroke','white');base.setAttribute('stroke-width','11');base.setAttribute('stroke-linecap','round');base.setAttribute('stroke-linejoin','round');base.setAttribute('opacity','.95');svg.appendChild(base);
                  const poly=document.createElementNS('http://www.w3.org/2000/svg','polyline');
                  poly.setAttribute('points',pts.map(p=>p.x+','+p.y).join(' '));poly.setAttribute('fill','none');poly.setAttribute('stroke','#2563EB');poly.setAttribute('stroke-width','7');poly.setAttribute('stroke-linecap','round');poly.setAttribute('stroke-linejoin','round');poly.setAttribute('class','routePulse');poly.setAttribute('opacity','.95');svg.appendChild(poly);
                }
                function setStatus(t){statusEl.textContent=t;}
                function setMechanicLocation(lat,lon,selected){const a=Number(lat),b=Number(lon);if(!Number.isFinite(a)||!Number.isFinite(b))return;mechanic.lat=a;mechanic.lon=b;if(selected){mechanicLocked=true;route=null;render();setStatus('Saving mechanic location...');if(window.javaBridge){window.javaBridge.confirmMechanicLocation(String(a),String(b));}else{setStatus('Map bridge unavailable. Please reload the page.');}}else{mechanicLocked=true;render();calculateRoute();}}
                function setCustomerLocation(lat,lon){customer.lat=Number(lat);customer.lon=Number(lon);render();calculateRoute();}
                function confirmMechanicLocation(){
                  if(mechanic.lat===null||mechanic.lon===null){setStatus('Select the mechanic location on the map first.');return;}
                  mechanicLocked=true;
                  render();
                  setStatus('Saving mechanic location...');
                  if(window.javaBridge){window.javaBridge.confirmMechanicLocation(String(mechanic.lat),String(mechanic.lon));}
                  else{setStatus('Map bridge unavailable. Please reload the page.');}
                }
                function sampleRoute(points,maxPoints){if(!points||points.length<=maxPoints)return points||[];const out=[],step=(points.length-1)/(maxPoints-1);for(let i=0;i<maxPoints;i++)out.push(points[Math.round(i*step)]);return out;}
                let routeBusy=false;
                let routeKey='';
                function calculateRoute(){
                  if(customer.lat===null||mechanic.lat===null){setStatus('Waiting for both locations...');return;}
                  if(!mechanicLocked){setStatus('Lock the mechanic location first.');return;}
                  const key=Number(mechanic.lat).toFixed(6)+','+Number(mechanic.lon).toFixed(6)+'>'+Number(customer.lat).toFixed(6)+','+Number(customer.lon).toFixed(6);
                  if(routeBusy)return;
                  if(key===routeKey&&route&&route.length)return;
                  routeKey=key;routeBusy=true;setStatus('Calculating actual road route...');
                  if(window.javaBridge){window.javaBridge.requestRoadRoute(String(mechanic.lat),String(mechanic.lon),String(customer.lat),String(customer.lon));}
                  else{routeBusy=false;setStatus('Map bridge unavailable. Please reload the page.');}
                }
                function setRouteFromJava(points,distanceKm,etaMin){
                  route=Array.isArray(points)?sampleRoute(points,700):[];
                  routeBusy=false;
                  render();
                  if(route.length){
                    setStatus('ROAD ROUTE • '+Number(distanceKm).toFixed(1)+' km • '+Math.round(Number(etaMin))+' min');
                    showRouteStats(distanceKm,etaMin);
                    fitRoute();
                  } else {
                    setStatus('No drivable road route found.');
                  }
                }
                function showRouteStats(distanceKm,etaMin){
                  const el=document.getElementById('info');
                  if(el) el.innerHTML='<b>ROADGUARDIAN ROUTE</b><br>Mechanic: '+fmt(mechanic)+'<br>Customer: '+fmt(customer)+'<br><b>Distance: '+Number(distanceKm).toFixed(1)+' km</b><br><b>ETA: '+Math.max(1,Math.round(Number(etaMin)))+' min</b>';
                }
                async function fallbackRoadRoute(){
                  if(customer.lat===null||mechanic.lat===null)return;
                  try{
                    setStatus('Calculating road route...');
                    const u='https://router.project-osrm.org/route/v1/driving/'+mechanic.lon+','+mechanic.lat+';'+customer.lon+','+customer.lat+'?overview=full&geometries=geojson';
                    const r=await fetch(u); const data=await r.json();
                    if(!data.routes||!data.routes.length)throw new Error('No route');
                    const rr=data.routes[0]; route=sampleRoute(rr.geometry.coordinates,700); render(); fitRoute();
                    const km=rr.distance/1000, mins=rr.duration/60;
                    setStatus('ROAD ROUTE • '+km.toFixed(1)+' km • '+Math.max(1,Math.round(mins))+' min');
                    showRouteStats(km,mins);
                  }catch(e){setStatus('Road route unavailable. Check internet connection.');}
                }
                function fitRoute(){
                  const pts=[];if(customer.lat!==null)pts.push([customer.lat,customer.lon]);if(mechanic.lat!==null)pts.push([mechanic.lat,mechanic.lon]);if(route&&route.length)route.forEach(p=>pts.push([p[1],p[0]]));
                  if(!pts.length)return;
                  let minLat=90,maxLat=-90,minLon=180,maxLon=-180;pts.forEach(p=>{minLat=Math.min(minLat,p[0]);maxLat=Math.max(maxLat,p[0]);minLon=Math.min(minLon,p[1]);maxLon=Math.max(maxLon,p[1]);});
                  if(minLat===maxLat&&minLon===maxLon){center={lat:minLat,lon:minLon};zoom=16;render();return;}
                  center={lat:(minLat+maxLat)/2,lon:(minLon+maxLon)/2};
                  let z=18;for(;z>3;z--){const a=worldXY(minLat,minLon,z),b=worldXY(maxLat,maxLon,z);if(Math.abs(b.x-a.x)*TILE<mapEl.clientWidth*.72&&Math.abs(b.y-a.y)*TILE<mapEl.clientHeight*.72)break;}zoom=z;render();
                }
                function zoomAt(z,x,y){z=clamp(z,2,19);const rect=mapEl.getBoundingClientRect(),px=x-rect.left,py=y-rect.top,c=centerWorld(),before=latLonFromWorld(c.x+(px-mapEl.clientWidth/2)/TILE,c.y+(py-mapEl.clientHeight/2)/TILE,zoom);zoom=z;const after=worldXY(before.lat,before.lon,zoom);center=latLonFromWorld(after.x-(px-mapEl.clientWidth/2)/TILE,after.y-(py-mapEl.clientHeight/2)/TILE,zoom);render();}
                function requestMyLocation(){
                  if(!navigator.geolocation){setStatus('GPS unavailable. Click the map to choose mechanic location.');return;}
                  setStatus('Requesting current location...');navigator.geolocation.getCurrentPosition(p=>{setMechanicLocation(p.coords.latitude,p.coords.longitude,true);if(window.javaBridge)window.javaBridge.updateLocation(String(p.coords.latitude),String(p.coords.longitude));},()=>setStatus('GPS unavailable. Click the map to choose the mechanic location, or enable Windows Location Services.'),{enableHighAccuracy:true,timeout:10000,maximumAge:5000});
                }
                let watchId=null;
                function startLocationTracking(){
                  if(!navigator.geolocation){setStatus('GPS unavailable. Saved mechanic location remains active.');return;}
                  if(watchId!==null)navigator.geolocation.clearWatch(watchId);
                  setStatus('Navigation started. Live mechanic GPS tracking active...');
                  watchId=navigator.geolocation.watchPosition(p=>{
                    mechanic.lat=p.coords.latitude; mechanic.lon=p.coords.longitude; mechanicLocked=true; render();
                    if(window.javaBridge)window.javaBridge.updateLocation(String(mechanic.lat),String(mechanic.lon));
                  },()=>setStatus('Live GPS unavailable. Saved mechanic location remains active.'),{enableHighAccuracy:true,maximumAge:3000,timeout:15000});
                }
                function setSatellite(on){satellite=!!on;document.getElementById('credit').textContent=satellite?'Tiles © Esri':'© OpenStreetMap contributors';render();}
                async function searchPlace(){
                  const q=document.getElementById('placeSearch').value.trim(),box=document.getElementById('searchResults');if(!q)return;box.style.display='block';box.innerHTML='<div class="result">Searching...</div>';
                  try{const r=await fetch('https://nominatim.openstreetmap.org/search?format=jsonv2&limit=5&q='+encodeURIComponent(q),{headers:{'Accept':'application/json'}});const places=await r.json();box.innerHTML='';if(!places.length){box.innerHTML='<div class="result">No place found.</div>';return;}places.forEach(pl=>{const d=document.createElement('div');d.className='result';d.textContent=pl.display_name;d.onclick=(ev)=>{ev.stopPropagation();const lat=Number(pl.lat),lon=Number(pl.lon);searchPoint={lat:lat,lon:lon};center={lat:lat,lon:lon};zoom=15;box.style.display='none';setMechanicLocation(lat,lon,true);};box.appendChild(d);});}catch(e){box.innerHTML='<div class="result">Search unavailable. Check internet connection.</div>';}
                }
                document.getElementById('searchPanel').addEventListener('mousedown',e=>e.stopPropagation());document.getElementById('searchResults').addEventListener('mousedown',e=>e.stopPropagation());
                mapEl.addEventListener('mousedown',e=>{dragging=true;moved=false;startX=e.clientX;startY=e.clientY;startCenter={...center};mapEl.classList.add('dragging');});
                window.addEventListener('mousemove',e=>{if(!dragging)return;const dx=e.clientX-startX,dy=e.clientY-startY;if(Math.abs(dx)+Math.abs(dy)>4)moved=true;const c=worldXY(startCenter.lat,startCenter.lon,zoom);center=latLonFromWorld(c.x-dx/TILE,c.y-dy/TILE,zoom);render();});
                window.addEventListener('mouseup',()=>{dragging=false;mapEl.classList.remove('dragging');});
                mapEl.addEventListener('click',e=>{if(moved)return;const r=mapEl.getBoundingClientRect(),c=centerWorld(),p=latLonFromWorld(c.x+(e.clientX-r.left-mapEl.clientWidth/2)/TILE,c.y+(e.clientY-r.top-mapEl.clientHeight/2)/TILE,zoom);setMechanicLocation(p.lat,p.lon,true);});
                mapEl.addEventListener('wheel',e=>{e.preventDefault();zoomAt(e.deltaY<0?zoom+1:zoom-1,e.clientX,e.clientY);},{passive:false});
                document.getElementById('zoomIn').onclick=()=>zoomAt(zoom+1,mapEl.clientWidth/2,mapEl.clientHeight/2);document.getElementById('zoomOut').onclick=()=>zoomAt(zoom-1,mapEl.clientWidth/2,mapEl.clientHeight/2);
                document.getElementById('roadButton').onclick=()=>setSatellite(false);document.getElementById('satButton').onclick=()=>setSatellite(true);document.getElementById('myButton').onclick=requestMyLocation;document.getElementById('fitButton').onclick=fitRoute;document.getElementById('searchButton').onclick=searchPlace;document.getElementById('placeSearch').addEventListener('keydown',e=>{if(e.key==='Enter')searchPlace();});
                window.addEventListener('resize',render);
                function viewRoute(){if(!mechanicLocked){setStatus('Lock the mechanic location first.');return;}if(window.javaBridge){setStatus('Calculating road route...');window.javaBridge.requestRoadRoute(String(mechanic.lat),String(mechanic.lon),String(customer.lat),String(customer.lon));}else fallbackRoadRoute();}
                window.setMechanicLocation=setMechanicLocation;window.setCustomerLocation=setCustomerLocation;window.requestMyLocation=requestMyLocation;window.startLocationTracking=startLocationTracking;window.confirmMechanicLocation=confirmMechanicLocation;window.setSatellite=setSatellite;window.setMapLayer=(name)=>setSatellite(name==='satellite');window.fitRoute=fitRoute;window.viewRoute=viewRoute;window.calculateRoute=calculateRoute;window.setRouteFromJava=setRouteFromJava;
                setTimeout(()=>{render();if(customer.lat!==null)calculateRoute();else setStatus('Click the map to choose the mechanic location.');},100);
                </script></body></html>
                """;
        return html.replace("CUSTOMER_LAT", customerLat)
                .replace("CUSTOMER_LON", customerLon)
                .replace("MECHANIC_LAT", mechanicLat)
                .replace("MECHANIC_LON", mechanicLon);
    }

    private void requestRoadRouteFromJava() {
        if (currentJob == null
                || currentJob.getLatitude() == null
                || currentJob.getLongitude() == null
                || currentJob.getMechanicLatitude() == null
                || currentJob.getMechanicLongitude() == null) {
            return;
        }

        requestRoadRoute(
                currentJob.getMechanicLatitude(),
                currentJob.getMechanicLongitude(),
                currentJob.getLatitude(),
                currentJob.getLongitude()
        );
    }

    private void requestRoadRoute(
            double mechanicLat,
            double mechanicLon,
            double customerLat,
            double customerLon
    ) {
        Thread worker = new Thread(() -> {
            try {
                String url = String.format(
                        java.util.Locale.US,
                        "https://router.project-osrm.org/route/v1/driving/%.7f,%.7f;%.7f,%.7f?overview=full&geometries=polyline",
                        mechanicLon, mechanicLat, customerLon, customerLat
                );

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(java.time.Duration.ofSeconds(5))
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "RoadGuardian/1.0")
                        .timeout(java.time.Duration.ofSeconds(10))
                        .GET()
                        .build();
                HttpResponse<String> response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IllegalStateException("Route HTTP " + response.statusCode());
                }

                String body = response.body();
                Matcher codeMatcher = Pattern.compile("\"code\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
                if (!codeMatcher.find() || !"Ok".equals(codeMatcher.group(1))) {
                    throw new IllegalStateException("No road route");
                }

                Matcher distanceMatcher = Pattern.compile("\"distance\"\\s*:\\s*([0-9.]+)").matcher(body);
                Matcher durationMatcher = Pattern.compile("\"duration\"\\s*:\\s*([0-9.]+)").matcher(body);
                Matcher geometryMatcher = Pattern.compile("\"geometry\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
                if (!distanceMatcher.find() || !durationMatcher.find() || !geometryMatcher.find()) {
                    throw new IllegalStateException("Incomplete route response");
                }

                double distanceKm = Double.parseDouble(distanceMatcher.group(1)) / 1000.0;
                double etaMin = Double.parseDouble(durationMatcher.group(1)) / 60.0;
                List<double[]> points = decodePolyline(geometryMatcher.group(1));
                if (points.isEmpty()) throw new IllegalStateException("Empty route geometry");

                StringBuilder js = new StringBuilder("[");
                int limit = Math.min(points.size(), 420);
                double step = points.size() <= limit ? 1.0 : (points.size() - 1.0) / (limit - 1.0);
                for (int i = 0; i < limit; i++) {
                    double[] point = points.get((int) Math.round(i * step));
                    if (i > 0) js.append(',');
                    js.append('[').append(point[1]).append(',').append(point[0]).append(']');
                }
                js.append(']');

                javafx.application.Platform.runLater(() -> {
                    executeMapScript(
                            "setRouteFromJava(" + js + ","
                                    + distanceKm + "," + etaMin + ");"
                    );
                    if (distanceValueLabel != null) {
                        distanceValueLabel.setText(String.format(java.util.Locale.US, "%.1f km", distanceKm));
                    }
                    if (etaValueLabel != null) {
                        etaValueLabel.setText(String.format(java.util.Locale.US, "%.0f min", etaMin));
                    }
                });
            } catch (Exception e) {
                System.err.println("Road route request failed: " + e.getMessage());
                javafx.application.Platform.runLater(() -> {
                    if (distanceValueLabel != null) distanceValueLabel.setText("Calculating...");
                    if (etaValueLabel != null) etaValueLabel.setText("Calculating...");
                    executeMapScript("fallbackRoadRoute();");
                });
            }
        }, "roadguardian-road-route");
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

    private final class MapBridge {

        public void updateLocation(
                String latitude,
                String longitude
        ) {

            try {
                double lat = Double.parseDouble(latitude);
                double lon = Double.parseDouble(longitude);

                if (currentJob == null
                        || clean(currentJob.getRequestId()) == null) {
                    return;
                }

                String requestId =
                        currentJob.getRequestId();

                Thread worker =
                        new Thread(
                                () -> {
                                    try {
                                        boolean saved =
                                                activeJobDAO.saveMechanicLocation(
                                                        requestId,
                                                        lat,
                                                        lon
                                                );

                                        if (saved) {
                                            currentJob.setMechanicLatitude(lat);
                                            currentJob.setMechanicLongitude(lon);
                                            currentJob.setMechanicLocationUpdatedAt(
                                                    String.valueOf(System.currentTimeMillis())
                                            );
                                            mechanicLocationLocked = true;
                                            javafx.application.Platform.runLater(() -> {
                                                if (mechanicCoordinatesLabel != null) {
                                                    mechanicCoordinatesLabel.setText(String.format(java.util.Locale.US, "%.6f, %.6f", lat, lon));
                                                }
                                                if (mechanicLocationStatusLabel != null) {
                                                    mechanicLocationStatusLabel.setText("Mechanic GPS: location locked and saved");
                                                }
                                                if (mechanicLockButton != null) {
                                                    mechanicLockButton.setText("Mechanic Locked");
                                                    styleMapButton(mechanicLockButton, Theme.SUCCESS);
                                                }
                                                executeMapScript("setMechanicLocation(" + lat + "," + lon + ",false);");
                                                requestRoadRouteFromJava();
                                            });
                                        }
                                    } catch (Exception e) {
                                        System.err.println(
                                                "Mechanic location update failed: "
                                                        + e.getMessage()
                                        );
                                    }
                                },
                                "roadguardian-location-update"
                        );

                worker.setDaemon(true);
                worker.start();

            } catch (Exception e) {
                System.err.println(
                        "Invalid mechanic GPS: "
                                + e.getMessage()
                );
            }
        }

        public void confirmMechanicLocation(
                String latitude,
                String longitude
        ) {
            try {
                double lat = Double.parseDouble(latitude);
                double lon = Double.parseDouble(longitude);
                if (currentJob == null || clean(currentJob.getRequestId()) == null) return;
                String requestId = currentJob.getRequestId();
                Thread worker = new Thread(() -> {
                    try {
                        Map<String, Object> update = new HashMap<>();
                        update.put("mechanicLatitude", lat);
                        update.put("mechanicLongitude", lon);
                        update.put("mechanicLocationUpdatedAt", String.valueOf(System.currentTimeMillis()));
                        firestore.collection("serviceRequests")
                                .document(requestId)
                                .set(update, SetOptions.merge())
                                .get();

                        DocumentSnapshot verify = firestore.collection("serviceRequests")
                                .document(requestId).get().get();
                        Double savedLat = verify.getDouble("mechanicLatitude");
                        Double savedLon = verify.getDouble("mechanicLongitude");
                        boolean saved = savedLat != null && savedLon != null
                                && Math.abs(savedLat - lat) < 0.000001
                                && Math.abs(savedLon - lon) < 0.000001;
                        if (saved) {
                            // Keep the mechanic profile in sync as a fallback source for customer tracking.
                            try {
                                String mechanicId = clean(currentJob.getMechanicId());
                                if (mechanicId != null) {
                                    Map<String, Object> profileLocation = new HashMap<>();
                                    profileLocation.put("latitude", savedLat);
                                    profileLocation.put("longitude", savedLon);
                                    profileLocation.put("locationUpdatedAt", String.valueOf(System.currentTimeMillis()));
                                    firestore.collection("mechanics").document(mechanicId)
                                            .set(profileLocation, SetOptions.merge()).get();
                                }
                            } catch (Exception profileError) {
                                System.err.println("Mechanic profile location sync skipped: " + profileError.getMessage());
                            }

                            currentJob.setMechanicLatitude(savedLat);
                            currentJob.setMechanicLongitude(savedLon);
                            currentJob.setMechanicLocationUpdatedAt(String.valueOf(System.currentTimeMillis()));
                            mechanicLocationLocked = true;
                            javafx.application.Platform.runLater(() -> {
                                if (mechanicCoordinatesLabel != null) {
                                    mechanicCoordinatesLabel.setText(String.format(java.util.Locale.US, "%.6f, %.6f", lat, lon));
                                }
                                if (mechanicLocationStatusLabel != null) {
                                    mechanicLocationStatusLabel.setText("Mechanic GPS: location locked and saved");
                                }
                                if (mechanicLockButton != null) {
                                    mechanicLockButton.setText("Mechanic Locked");
                                    styleMapButton(mechanicLockButton, Theme.SUCCESS);
                                }
                                executeMapScript("setMechanicLocation(" + lat + "," + lon + ",false);");
                                showInfo("Location Saved",
                                        String.format(java.util.Locale.US,
                                                "Mechanic location saved successfully.\n\nCoordinates: %.6f, %.6f",
                                                lat, lon));
                                requestRoadRouteFromJava();
                            });
                        } else {
                            javafx.application.Platform.runLater(() -> {
                                if (mechanicLocationStatusLabel != null) {
                                    mechanicLocationStatusLabel.setText("Mechanic GPS: save failed - try Lock / Confirm again");
                                }
                                if (mechanicLockButton != null) {
                                    mechanicLockButton.setText("Lock / Confirm Mechanic");
                                    styleMapButton(mechanicLockButton, Theme.PRIMARY);
                                }
                                executeMapScript("setStatus('Unable to save mechanic location to Firebase.');");
                            });
                        }
                    } catch (Exception e) {
                        System.err.println("Mechanic location lock failed: " + e.getMessage());
                        javafx.application.Platform.runLater(() -> {
                            if (mechanicLocationStatusLabel != null) {
                                mechanicLocationStatusLabel.setText("Mechanic GPS: save failed - " + e.getMessage());
                            }
                        });
                    }
                }, "roadguardian-lock-location");
                worker.setDaemon(true);
                worker.start();
            } catch (Exception e) {
                System.err.println("Invalid mechanic location: " + e.getMessage());
            }
        }

        public void requestRoadRoute(
                String mechanicLatitude,
                String mechanicLongitude,
                String customerLatitude,
                String customerLongitude
        ) {
            try {
                NavigationPage.this.requestRoadRoute(
                        Double.parseDouble(mechanicLatitude),
                        Double.parseDouble(mechanicLongitude),
                        Double.parseDouble(customerLatitude),
                        Double.parseDouble(customerLongitude)
                );
            } catch (Exception e) {
                System.err.println("Invalid route coordinates: " + e.getMessage());
            }
        }

        public void updateRouteStats(
                String distance,
                String eta
        ) {

            javafx.application.Platform.runLater(
                    () -> {
                        if (distanceValueLabel != null) {
                            distanceValueLabel.setText(
                                    distance.equals("Unavailable")
                                            ? "Unavailable"
                                            : distance + " km"
                            );
                        }

                        if (etaValueLabel != null) {
                            etaValueLabel.setText(
                                    eta
                            );
                        }
                    }
            );
        }

        public void locationError(
                String message
        ) {
            System.err.println(
                    "Mechanic location: "
                            + message
            );
        }
    }

    // =====================================================
    // ROUTE MARKER
    // =====================================================

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

        Circle circle =
                new Circle(21);

        circle.setFill(
                Color.web(color)
        );
        circle.setStroke(
                Color.WHITE
        );
        circle.setStrokeWidth(3);

        Label letterLabel =
                new Label(letter);

        letterLabel.setStyle(
                "-fx-text-fill: white;"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
        );

        StackPane marker =
                new StackPane(
                        circle,
                        letterLabel
                );

        Label titleLabel =
                new Label(
                        firstNonBlank(
                                title,
                                "-"
                        )
                );

        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(180);
        titleLabel.setAlignment(
                Pos.CENTER
        );
        titleLabel.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        Label subtitleLabel =
                new Label(
                        firstNonBlank(
                                subtitle,
                                "Not available"
                        )
                );

        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(180);
        subtitleLabel.setAlignment(
                Pos.CENTER
        );
        subtitleLabel.setStyle(
                "-fx-font-size: 9px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        box.getChildren()
                .addAll(
                        marker,
                        titleLabel,
                        subtitleLabel
                );

        return box;
    }

    // =====================================================
    // CUSTOMER PANEL
    // =====================================================

    private VBox createCustomerPanel() {

        VBox panel =
                createCard();

        panel.setPrefWidth(325);
        panel.setMinWidth(300);

        Label title =
                createSectionTitle(
                        "Customer Details"
                );

        Label customerName =
                new Label(
                        controller.getCustomerName(
                                currentJob
                        )
                );

        customerName.setWrapText(true);
        customerName.setStyle(
                "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        Label customerId =
                new Label(
                        firstNonBlank(
                                currentJob.getCustomerId(),
                                "Customer ID not available"
                        )
                );

        customerId.setWrapText(true);
        customerId.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        VBox vehicleBox =
                createInformationBox(
                        "VEHICLE",
                        controller.getVehicleDisplay(
                                currentJob
                        )
                );

        VBox problemBox =
                createInformationBox(
                        "PROBLEM",
                        controller.getProblemDisplay(
                                currentJob
                        )
                );

        VBox serviceBox =
                createInformationBox(
                        "SERVICE",
                        controller.getServiceTypeDisplay(
                                currentJob
                        )
                );

        VBox locationBox =
                createInformationBox(
                        "CUSTOMER LOCATION",
                        getCoordinatesDisplay(
                                currentJob
                        )
                );

        Label navigationStatus =
                createNavigationStateLabel();

        Button navigationButton =
                createPrimaryButton(
                        currentJob.isNavigationStarted()
                                ? "Navigation Started"
                                : "Start Navigation"
                );

        navigationButton.setDisable(
                currentJob.isNavigationStarted()
                        || currentJob.hasArrived()
        );

        navigationButton.setOnAction(
                event -> startNavigation()
        );

        Button arrivedButton =
                createSuccessButton(
                        currentJob.hasArrived()
                                ? "✓ Arrived"
                                : "Mark as Arrived"
                );

        arrivedButton.setDisable(
                !currentJob.isNavigationStarted()
                        || currentJob.hasArrived()
        );

        arrivedButton.setOnAction(
                event -> markArrived()
        );

        /*
         * The service request model does not currently store the
         * customer's phone number. Do not show a fake number.
         */
        Button phoneButton =
                new Button(
                        "Customer phone not stored in request"
                );

        phoneButton.setMaxWidth(
                Double.MAX_VALUE
        );
        phoneButton.setDisable(true);
        phoneButton.setStyle(
                "-fx-background-color: "
                        + Theme.SURFACE
                        + ";"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
                        + "-fx-border-color: "
                        + Theme.BORDER
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 10;"
        );

        panel.getChildren()
                .addAll(
                        title,
                        customerName,
                        customerId,
                        vehicleBox,
                        serviceBox,
                        problemBox,
                        locationBox,
                        navigationStatus,
                        navigationButton,
                        arrivedButton,
                        phoneButton
                );

        return panel;
    }

    // =====================================================
    // TRACKING TIMELINE
    // =====================================================

    private VBox createTrackingTimeline() {

        VBox card =
                createCard();

        Label title =
                createSectionTitle(
                        "Navigation Tracking"
                );

        HBox timeline =
                new HBox(8);

        timeline.setAlignment(
                Pos.CENTER_LEFT
        );

        timeline.getChildren()
                .addAll(
                        createTimelineStep(
                                "Job Accepted",
                                true,
                                Theme.SUCCESS
                        ),
                        createArrow(),
                        createTimelineStep(
                                "Navigation Started",
                                currentJob.isNavigationStarted(),
                                Theme.INFO
                        ),
                        createArrow(),
                        createTimelineStep(
                                "Arrived",
                                currentJob.hasArrived(),
                                Theme.SUCCESS
                        ),
                        createArrow(),
                        createTimelineStep(
                                "Repair",
                                currentJob.isInProgress(),
                                Theme.PRIMARY
                        )
                );

        Label sync =
                new Label(
                        "These tracking flags are saved in serviceRequests/"
                                + firstNonBlank(
                                currentJob.getRequestId(),
                                "request"
                        )
                                + " and are visible to the customer after refresh."
                );

        sync.setWrapText(true);
        sync.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        card.getChildren()
                .addAll(
                        title,
                        timeline,
                        sync
                );

        return card;
    }

    private VBox createTimelineStep(
            String text,
            boolean active,
            String color
    ) {

        VBox box =
                new VBox(4);

        box.setAlignment(
                Pos.CENTER
        );
        box.setPadding(
                new Insets(
                        9,
                        12,
                        9,
                        12
                )
        );
        box.setStyle(
                "-fx-background-color: "
                        + (active ? color + "18" : Theme.SURFACE)
                        + ";"
                        + "-fx-border-color: "
                        + (active ? color : Theme.BORDER)
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
        );

        Label label =
                new Label(text);

        label.setStyle(
                "-fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + (active ? color : Theme.SECONDARY_TEXT)
                        + ";"
        );

        box.getChildren()
                .add(label);

        return box;
    }

    private Label createArrow() {

        Label arrow =
                new Label("→");

        arrow.setStyle(
                "-fx-font-size: 14px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        return arrow;
    }

    // =====================================================
    // START NAVIGATION
    // =====================================================

    private void startNavigation() {

        if (currentJob == null
                || clean(currentJob.getRequestId()) == null) {

            showError(
                    "Navigation Error",
                    "No active service request is available."
            );

            return;
        }

        if (!mechanicLocationLocked
                || currentJob.getMechanicLatitude() == null
                || currentJob.getMechanicLongitude() == null) {
            showError(
                    "Mechanic Location Required",
                    "Please select the mechanic location on the map and press Lock / Confirm Mechanic before starting navigation."
            );
            return;
        }

        if (currentJob.isNavigationStarted()) {

            showInfo(
                    "Navigation",
                    "Navigation has already been started for this request."
            );

            return;
        }

        boolean success =
                controller.startNavigation(
                        currentJob.getRequestId()
                );

        if (!success) {

            showError(
                    "Navigation Error",
                    "Unable to start navigation. The job must belong to you and be Accepted or In Progress."
            );

            refreshJob();
            return;
        }

        // Keep the already locked mechanic coordinate on the same request
        // after navigation changes the lifecycle status.
        try {
            if (currentJob.getMechanicLatitude() != null
                    && currentJob.getMechanicLongitude() != null) {
                Map<String, Object> locationUpdate = new HashMap<>();
                locationUpdate.put("mechanicLatitude", currentJob.getMechanicLatitude());
                locationUpdate.put("mechanicLongitude", currentJob.getMechanicLongitude());
                locationUpdate.put("mechanicLocationUpdatedAt", String.valueOf(System.currentTimeMillis()));
                firestore.collection("serviceRequests")
                        .document(currentJob.getRequestId())
                        .set(locationUpdate, SetOptions.merge())
                        .get();
            }
        } catch (Exception locationError) {
            System.err.println("Unable to preserve mechanic location after navigation: " + locationError.getMessage());
        }

        showInfo(
                "Navigation Started",
                "Navigation status was saved to the service request. The customer can now see that navigation has started."
        );

        refreshJob();
    }

    // =====================================================
    // MARK ARRIVED
    // =====================================================

    private void markArrived() {

        if (currentJob == null
                || clean(currentJob.getRequestId()) == null) {

            showError(
                    "Arrival Error",
                    "No active service request is available."
            );

            return;
        }

        if (!currentJob.isNavigationStarted()) {

            showError(
                    "Navigation Required",
                    "Start navigation before marking the job as arrived."
            );

            return;
        }

        if (currentJob.hasArrived()) {

            showInfo(
                    "Arrival",
                    "This service request is already marked as arrived."
            );

            return;
        }

        boolean success =
                controller.markArrived(
                        currentJob.getRequestId()
                );

        if (!success) {

            showError(
                    "Arrival Error",
                    "Unable to mark arrival for this service request."
            );

            refreshJob();
            return;
        }

        showInfo(
                "Arrived",
                "Arrival status was saved to the same service request and is now visible to the customer."
        );

        refreshJob();
    }

    // =====================================================
    // NAVIGATION STATE LABEL
    // =====================================================

    private Label createNavigationStateLabel() {

        String text;
        String background;
        String color;

        if (currentJob.hasArrived()) {

            text = "✓ Arrived at customer location";
            background = Theme.SUCCESS_BG;
            color = Theme.SUCCESS;

        } else if (currentJob.isNavigationStarted()) {

            text = "Navigation started";
            background = Theme.INFO_BG;
            color = Theme.INFO;

        } else {

            text = "Ready to start navigation";
            background = Theme.INFO_BG;
            color = Theme.INFO;
        }

        Label label =
                new Label(text);

        label.setMaxWidth(
                Double.MAX_VALUE
        );
        label.setWrapText(true);
        label.setStyle(
                "-fx-background-color: "
                        + background
                        + ";"
                        + "-fx-text-fill: "
                        + color
                        + ";"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 10 12 10 12;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );

        return label;
    }

    // =====================================================
    // BUTTONS
    // =====================================================

    private Button createPrimaryButton(
            String text
    ) {

        Button button =
                new Button(text);

        button.setMaxWidth(
                Double.MAX_VALUE
        );
        button.setStyle(
                "-fx-background-color: "
                        + Theme.INFO
                        + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 11;"
                        + "-fx-cursor: hand;"
        );

        return button;
    }

    private Button createSuccessButton(
            String text
    ) {

        Button button =
                new Button(text);

        button.setMaxWidth(
                Double.MAX_VALUE
        );
        button.setStyle(
                "-fx-background-color: "
                        + Theme.SUCCESS
                        + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 11;"
                        + "-fx-cursor: hand;"
        );

        return button;
    }

    // =====================================================
    // INFORMATION BOX
    // =====================================================

    private VBox createInformationBox(
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
        box.setStyle(
                "-fx-background-color: #1A1A1A;"
                        + "-fx-border-color: "
                        + Theme.BORDER
                        + ";"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-font-size: 8px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
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
                "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        if ("VEHICLE".equalsIgnoreCase(title)) {
            valueLabel.setGraphic(
                    IconUtil.createVehicleIcon(
                            0.55,
                            Theme.PRIMARY
                    )
            );
            valueLabel.setGraphicTextGap(7);
        }

        box.getChildren()
                .addAll(
                        titleLabel,
                        valueLabel
                );

        return box;
    }

    // =====================================================
    // CARD
    // =====================================================

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
                        + Theme.CARD
                        + ";"
                        + "-fx-border-color: "
                        + "#FACC15"
                        + ";"
                        + "-fx-border-radius: 14;"
                        + "-fx-background-radius: 14;"
        );

        return card;
    }

    private Label createSectionTitle(
            String text
    ) {

        Label label =
                new Label(text);

        label.setStyle(
                "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        return label;
    }

    // =====================================================
    // EMPTY / ERROR
    // =====================================================

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
                        "No Active Navigation Job"
                );

        title.setStyle(
                "-fx-font-size: 19px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        Label description =
                new Label(
                        "Accept a customer service request first. Accepted or In Progress jobs will appear here automatically."
                );

        description.setWrapText(true);
        description.setMaxWidth(620);
        description.setAlignment(
                Pos.CENTER
        );
        description.setStyle(
                "-fx-font-size: 11px;"
                        + "-fx-text-fill: "
                        + Theme.SECONDARY_TEXT
                        + ";"
        );

        card.getChildren()
                .addAll(
                        title,
                        description
                );

        return card;
    }

    private VBox createErrorState(
            String message
    ) {

        VBox card =
                createCard();

        card.setAlignment(
                Pos.CENTER
        );

        Label title =
                new Label(
                        "Navigation Data Error"
                );

        title.setStyle(
                "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + Theme.ERROR
                        + ";"
        );

        Label description =
                new Label(
                        firstNonBlank(
                                message,
                                "Unable to load navigation data."
                        )
                );

        description.setStyle(
                "-fx-font-size: 11px;"
                        + "-fx-text-fill: "
                        + Theme.TEXT
                        + ";"
        );

        card.getChildren()
                .addAll(
                        title,
                        description
                );

        return card;
    }

    // =====================================================
    // COORDINATES
    // =====================================================

    private String getCoordinatesDisplay(
            ServiceRequest request
    ) {

        if (request == null
                || request.getLatitude() == null
                || request.getLongitude() == null) {

            return "Not available";
        }

        return String.format(
                "%.6f, %.6f",
                request.getLatitude(),
                request.getLongitude()
        );
    }

    // =====================================================
    // STATUS COLOR
    // =====================================================

    private String getStatusColor(
            String status
    ) {

        status = clean(status);

        if (status == null) {
            return Theme.SECONDARY_TEXT;
        }

        if (status.equalsIgnoreCase("Accepted")) {
            return "#F59E0B";
        }

        if (status.equalsIgnoreCase("In Progress")
                || status.equalsIgnoreCase("InProgress")) {
            return Theme.INFO;
        }

        if (status.equalsIgnoreCase("Completed")) {
            return Theme.SUCCESS;
        }

        if (status.equalsIgnoreCase("Cancelled")) {
            return Theme.ERROR;
        }

        return Theme.PRIMARY;
    }

    // =====================================================
    // ALERTS
    // =====================================================

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

    private void showError(
            String title,
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "RoadGuardian"
        );
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // =====================================================
    // STRING HELPERS
    // =====================================================

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

    private Double numberValue(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            double number = ((Number) value).doubleValue();
            return Double.isFinite(number) ? number : null;
        }

        try {
            String text = String.valueOf(value).trim();

            if (text.isEmpty()) {
                return null;
            }

            double number = Double.parseDouble(text);
            return Double.isFinite(number) ? number : null;

        } catch (Exception e) {
            return null;
        }
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
}
