package project.ui.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import project.controller.user.DocumentsController;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentsPage {

    // ============================================================
    // COLORS
    // ============================================================

    private static final String BG      = "#0F0F0F";
    private static final String CARD    = "#1A1A1A";
    private static final String WHITE   = "#242424";
    private static final String BORDER  = "#F59E0B";
    private static final String HEADING = "#F3F4F6";
    private static final String TEXT    = "#A1A1AA";
    private static final String BLUE    = "#F59E0B";
    private static final String GREEN   = "#22C55E";
    private static final String RED     = "#EF4444";
    private static final String ORANGE  = "#F59E0B";

    // ============================================================
    // DOCUMENT DATA
    // ============================================================

    private static final String[] REQUIRED_DOCUMENTS = {
            "Registration Certificate",
            "Motor Insurance Policy",
            "Pollution Under Control Certificate",
            "Extended Warranty Certificate",
            "Driving Licence"
    };

    private final String[] uploadedFileNames =
            new String[REQUIRED_DOCUMENTS.length];

    private final String[] uploadedFilePaths =
            new String[REQUIRED_DOCUMENTS.length];

    /*
     * Firestore मधून आलेला document data
     *
     * Key   = document type
     * Value = Firestore data
     */
    private final Map<String, Map<String, Object>> documentData =
            new HashMap<>();

    private int uploadStep = 0;

    private String lastUploadStatus = "";

    private Scene documentsScene;

    private final String userEmail;

    private final DocumentsController controller;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public DocumentsPage() {

        String sessionEmail =
                UserSession.getUserEmail();

        this.userEmail =
                sessionEmail == null
                        ? ""
                        : sessionEmail.trim().toLowerCase();

        this.controller =
                new DocumentsController();

        loadDocuments();
    }

    // ============================================================
    // LOAD DOCUMENTS FROM FIRESTORE
    // ============================================================

    private void loadDocuments() {

        documentData.clear();

        for (int i = 0;
             i < REQUIRED_DOCUMENTS.length;
             i++) {

            uploadedFileNames[i] = null;
            uploadedFilePaths[i] = null;
        }

        uploadStep = 0;

        if (userEmail.isEmpty()) {

            lastUploadStatus =
                    "Please login to access your documents.";

            return;
        }

        List<Map<String, Object>> documents =
                controller.getDocuments(userEmail);

        if (documents == null) {
            documents = List.of();
        }

        // --------------------------------------------------------
        // Store Firestore data by document type
        // --------------------------------------------------------

        for (Map<String, Object> data : documents) {

            if (data == null) {
                continue;
            }

            Object typeObject =
                    data.get("documentType");

            if (typeObject == null) {
                continue;
            }

            String type =
                    String.valueOf(typeObject).trim();

            if (!type.isEmpty()) {

                documentData.put(
                        type,
                        new HashMap<>(data)
                );
            }
        }

        // --------------------------------------------------------
        // Populate local display data
        // --------------------------------------------------------

        for (int i = 0;
             i < REQUIRED_DOCUMENTS.length;
             i++) {

            String requiredType =
                    REQUIRED_DOCUMENTS[i];

            Map<String, Object> data =
                    documentData.get(requiredType);

            if (data != null) {

                Object fileName =
                        data.get("fileName");

                Object filePath =
                        data.get("localFilePath");

                if (fileName != null) {

                    uploadedFileNames[i] =
                            String.valueOf(fileName);
                }

                if (filePath != null) {

                    uploadedFilePaths[i] =
                            String.valueOf(filePath);
                }
            }
        }

        // --------------------------------------------------------
        // Find first missing document
        // --------------------------------------------------------

        uploadStep = 0;

        while (uploadStep < REQUIRED_DOCUMENTS.length
                && isUploaded(uploadStep)) {

            uploadStep++;
        }

        if (uploadStep >= REQUIRED_DOCUMENTS.length) {

            lastUploadStatus =
                    "All required documents are uploaded.";

        } else if (!documentData.isEmpty()) {

            lastUploadStatus =
                    "Document vault synced with Firebase.";
        }
    }

    // ============================================================
    // PUBLIC SCENE
    // ============================================================

    public Scene getDocumentsScene() {

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: #0F0F0F;"
        );

        // --------------------------------------------------------
        // Common Header
        // --------------------------------------------------------

        HBox header =
                UserHeader.createHeader();

        // --------------------------------------------------------
        // Common Sidebar
        // --------------------------------------------------------

        ScrollPane sidebar =
                UserSideBar.createSidebar(
                        "Documents"
                );

        // --------------------------------------------------------
        // Main Content
        // --------------------------------------------------------

        VBox content =
                createContent();

        ScrollPane contentScroll =
                new ScrollPane(content);

        contentScroll.setFitToWidth(true);
        contentScroll.setFitToHeight(false);
        contentScroll.setPannable(true);

        contentScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        contentScroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.ALWAYS
        );

        contentScroll.setStyle(
                "-fx-background-color: #0F0F0F;" +
                "-fx-border-color: transparent;"
        );

        root.setTop(header);
        root.setLeft(sidebar);
        root.setCenter(contentScroll);

        // --------------------------------------------------------
        // Safe stage dimensions
        // --------------------------------------------------------

        double width = 1200;
        double height = 750;

        if (UserDashboard.dashboardStage != null) {

            if (UserDashboard.dashboardStage.getWidth() > 0) {

                width =
                        UserDashboard.dashboardStage.getWidth();
            }

            if (UserDashboard.dashboardStage.getHeight() > 0) {

                height =
                        UserDashboard.dashboardStage.getHeight();
            }
        }

        documentsScene =
                new Scene(
                        root,
                        width,
                        height
                );

        return documentsScene;
    }

    // ============================================================
    // MAIN CONTENT
    // ============================================================

    private VBox createContent() {

        VBox content =
                new VBox(20);

        content.setPadding(
                new Insets(
                        25,
                        30,
                        30,
                        30
                )
        );

        content.setStyle(
                "-fx-background-color: #0F0F0F;"
        );

        HBox heading =
                createHeading();

        VBox uploadBox =
                createUploadBox();

        GridPane documentsGrid =
                createDocumentsGrid();

        content.getChildren().addAll(
                heading,
                uploadBox,
                documentsGrid
        );

        return content;
    }

    // ============================================================
    // HEADING
    // ============================================================

    private HBox createHeading() {

        HBox heading =
                new HBox();

        heading.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox headingText =
                new VBox(2);

        Label title =
                new Label("Documents");

        title.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #F3F4F6;"
        );

        Label subtitle =
                new Label(
                        "Encrypted vault · shared automatically with responders during an SOS"
                );

        subtitle.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #A1A1AA;"
        );

        headingText.getChildren().addAll(
                title,
                subtitle
        );

        heading.getChildren().add(
                headingText
        );

        return heading;
    }

    // ============================================================
    // UPLOAD BOX
    // ============================================================

    private VBox createUploadBox() {

        boolean allDocumentsUploaded =
                uploadStep >= REQUIRED_DOCUMENTS.length;

        VBox uploadBox =
                new VBox(12);

        uploadBox.setPrefHeight(280);

        uploadBox.setAlignment(
                Pos.CENTER
        );

        uploadBox.setPadding(
                new Insets(25)
        );

        uploadBox.setStyle(
                "-fx-background-color: #1A1A1A;" +
                "-fx-border-color: #F59E0B;" +
                "-fx-border-radius: 22;" +
                "-fx-background-radius: 22;"
        );

        // --------------------------------------------------------
        // Upload icon
        // --------------------------------------------------------

        Label uploadIcon =
                new Label("⇧");

        uploadIcon.setPrefWidth(60);
        uploadIcon.setPrefHeight(60);

        uploadIcon.setAlignment(
                Pos.CENTER
        );

        uploadIcon.setStyle(
                "-fx-background-color: #1F2D4A;" +
                "-fx-background-radius: 40;" +
                "-fx-text-fill: #F59E0B;" +
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;"
        );

        // --------------------------------------------------------
        // Upload title
        // --------------------------------------------------------

        String uploadTitleText;

        if (allDocumentsUploaded) {

            uploadTitleText =
                    "All required documents uploaded";

        } else if (userEmail.isEmpty()) {

            uploadTitleText =
                    "Login required";

        } else {

            uploadTitleText =
                    "Step " +
                    (uploadStep + 1) +
                    " of " +
                    REQUIRED_DOCUMENTS.length +
                    ": Upload " +
                    REQUIRED_DOCUMENTS[uploadStep];
        }

        Label uploadTitle =
                new Label(uploadTitleText);

        uploadTitle.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #F3F4F6;"
        );

        // --------------------------------------------------------
        // Description
        // --------------------------------------------------------

        String descriptionText;

        if (allDocumentsUploaded) {

            descriptionText =
                    "Your document vault is complete.";

        } else if (userEmail.isEmpty()) {

            descriptionText =
                    "Please login to upload and manage documents.";

        } else {

            descriptionText =
                    "Upload your " +
                    REQUIRED_DOCUMENTS[uploadStep] +
                    " (PDF, JPG or PNG up to 10 MB).";
        }

        Label uploadDescription =
                new Label(descriptionText);

        uploadDescription.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #A1A1AA;"
        );

        // --------------------------------------------------------
        // Browse button
        // --------------------------------------------------------

        Button browseButton =
                new Button(
                        allDocumentsUploaded
                                ? "Upload complete"
                                : "Browse files"
                );

        browseButton.setPrefHeight(40);

        browseButton.setPadding(
                new Insets(
                        0,
                        20,
                        0,
                        20
                )
        );

        browseButton.setStyle(
                "-fx-background-color: #F59E0B;" +
                "-fx-text-fill: #0F0F0F;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 25;"
        );

        browseButton.setDisable(
                allDocumentsUploaded
                        || userEmail.isEmpty()
        );

        // --------------------------------------------------------
        // Status
        // --------------------------------------------------------

        Label uploadStatus =
                new Label(lastUploadStatus);

        uploadStatus.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #22C55E;"
        );

        browseButton.setOnAction(
                event ->
                        uploadDocument(browseButton)
        );

        uploadBox.getChildren().addAll(
                uploadIcon,
                uploadTitle,
                uploadDescription,
                browseButton,
                uploadStatus
        );

        return uploadBox;
    }

    // ============================================================
    // UPLOAD DOCUMENT
    // ============================================================

    private void uploadDocument(
            Button browseButton
    ) {

        if (userEmail.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Login Required",
                    "Please login before uploading documents."
            );

            return;
        }

        if (uploadStep >= REQUIRED_DOCUMENTS.length) {
            return;
        }

        String documentType =
                REQUIRED_DOCUMENTS[uploadStep];

        FileChooser fileChooser =
                new FileChooser();

        fileChooser.setTitle(
                "Upload " + documentType
        );

        fileChooser.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "Documents (PDF, JPG, PNG)",
                                "*.pdf",
                                "*.jpg",
                                "*.jpeg",
                                "*.png"
                        )
                );

        File file =
                fileChooser.showOpenDialog(
                        browseButton
                                .getScene()
                                .getWindow()
                );

        if (file == null) {
            return;
        }

        // --------------------------------------------------------
        // File size validation
        // --------------------------------------------------------

        long maxFileSize =
                10L * 1024L * 1024L;

        if (file.length() > maxFileSize) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "File Too Large",
                    "Please select a file smaller than 10 MB."
            );

            return;
        }

        // --------------------------------------------------------
        // Save metadata to Firestore
        // --------------------------------------------------------

        boolean saved =
                controller.saveDocument(
                        userEmail,
                        documentType,
                        file.getName(),
                        file.getAbsolutePath()
                );

        if (!saved) {

            lastUploadStatus =
                    "Unable to save document. Please try again.";

            showAlert(
                    Alert.AlertType.ERROR,
                    "Upload Failed",
                    "The document information could not be saved to Firebase."
            );

            refreshScene();

            return;
        }

        // --------------------------------------------------------
        // Update local display
        // --------------------------------------------------------

        uploadedFileNames[uploadStep] =
                file.getName();

        uploadedFilePaths[uploadStep] =
                file.getAbsolutePath();

        lastUploadStatus =
                "Saved successfully: " +
                file.getName();

        uploadStep++;

        // --------------------------------------------------------
        // Move to next missing document
        // --------------------------------------------------------

        while (uploadStep < REQUIRED_DOCUMENTS.length
                && isUploaded(uploadStep)) {

            uploadStep++;
        }

        refreshScene();
    }

    // ============================================================
    // DOCUMENT GRID
    // ============================================================

    private GridPane createDocumentsGrid() {

        GridPane grid =
                new GridPane();

        grid.setHgap(18);
        grid.setVgap(18);

        grid.add(
                documentCard(
                        "▤",
                        "Registration Certificate",
                        uploadedFileName(0),
                        isUploaded(0)
                ),
                0,
                0
        );

        grid.add(
                documentCard(
                        "▤",
                        "Motor Insurance Policy",
                        uploadedFileName(1),
                        isUploaded(1)
                ),
                1,
                0
        );

        grid.add(
                documentCard(
                        "▤",
                        "Pollution Under Control",
                        uploadedFileName(2),
                        isUploaded(2)
                ),
                2,
                0
        );

        grid.add(
                documentCard(
                        "▤",
                        "Extended Warranty",
                        uploadedFileName(3),
                        isUploaded(3)
                ),
                0,
                1
        );

        grid.add(
                documentCard(
                        "▤",
                        "Driving Licence",
                        uploadedFileName(4),
                        isUploaded(4)
                ),
                1,
                1
        );

        return grid;
    }

    // ============================================================
    // FILE NAME
    // ============================================================

    private String uploadedFileName(
            int documentIndex
    ) {

        if (documentIndex < 0
                || documentIndex >= uploadedFileNames.length) {

            return "Not uploaded yet";
        }

        String fileName =
                uploadedFileNames[documentIndex];

        return fileName == null
                || fileName.trim().isEmpty()
                ? "Not uploaded yet"
                : fileName;
    }

    // ============================================================
    // UPLOADED CHECK
    // ============================================================

    private boolean isUploaded(
            int documentIndex
    ) {

        if (documentIndex < 0
                || documentIndex >= uploadedFileNames.length) {

            return false;
        }

        return uploadedFileNames[documentIndex] != null
                && !uploadedFileNames[documentIndex]
                .trim()
                .isEmpty();
    }

    // ============================================================
    // DOCUMENT CARD
    // ============================================================

    private VBox documentCard(
            String icon,
            String title,
            String details,
            boolean verified
    ) {

        VBox card =
                new VBox(7);

        card.setPrefWidth(430);
        card.setPrefHeight(155);

        card.setPadding(
                new Insets(20)
        );

        card.setStyle(
                "-fx-background-color: #1A1A1A;" +
                "-fx-border-color: #F59E0B;" +
                "-fx-border-radius: 20;" +
                "-fx-background-radius: 20;"
        );

        // --------------------------------------------------------
        // Top Row
        // --------------------------------------------------------

        HBox topRow =
                new HBox();

        topRow.setAlignment(
                Pos.CENTER_LEFT
        );

        Label iconLabel =
                new Label(icon);

        iconLabel.setPrefWidth(42);
        iconLabel.setPrefHeight(42);

        iconLabel.setAlignment(
                Pos.CENTER
        );

        iconLabel.setStyle(
                "-fx-background-color: #242424;" +
                "-fx-background-radius: 30;" +
                "-fx-text-fill: #A1A1AA;" +
                "-fx-font-size: 18px;"
        );

        Region space =
                new Region();

        HBox.setHgrow(
                space,
                Priority.ALWAYS
        );

        Label status =
                new Label();

        if (verified) {

            status.setText(
                    "✓ Uploaded"
            );

            status.setStyle(
                    "-fx-background-color: #1C3A2C;" +
                    "-fx-background-radius: 20;" +
                    "-fx-text-fill: #22C55E;" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;"
            );

        } else {

            status.setText(
                    "Pending upload"
            );

            status.setStyle(
                    "-fx-background-color: #3A1C1C;" +
                    "-fx-background-radius: 20;" +
                    "-fx-text-fill: #EF4444;" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;"
            );
        }

        status.setPadding(
                new Insets(
                        5,
                        11,
                        5,
                        11
                )
        );

        topRow.getChildren().addAll(
                iconLabel,
                space,
                status
        );

        // --------------------------------------------------------
        // Title
        // --------------------------------------------------------

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-font-size: 17px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #F3F4F6;"
        );

        // --------------------------------------------------------
        // Details
        // --------------------------------------------------------

        Label detailsLabel =
                new Label(details);

        detailsLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #A1A1AA;"
        );

        // --------------------------------------------------------
        // Validity / status
        // --------------------------------------------------------

        Label validityLabel =
                new Label(
                        verified
                                ? "Saved in your Firebase vault"
                                : "Required document"
                );

        validityLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #F3F4F6;"
        );

        card.getChildren().addAll(
                topRow,
                titleLabel,
                detailsLabel,
                validityLabel
        );

        return card;
    }

    // ============================================================
    // REFRESH SCENE
    // ============================================================

    private void refreshScene() {

        if (documentsScene == null) {
            return;
        }

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: #0F0F0F;"
        );

        // IMPORTANT:
        // Existing code had an empty HBox here.
        // We now use the common header.

        HBox header =
                UserHeader.createHeader();

        ScrollPane sidebar =
                UserSideBar.createSidebar(
                        "Documents"
                );

        VBox content =
                createContent();

        ScrollPane contentScroll =
                new ScrollPane(content);

        contentScroll.setFitToWidth(true);
        contentScroll.setFitToHeight(false);
        contentScroll.setPannable(true);

        contentScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        contentScroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.ALWAYS
        );

        contentScroll.setStyle(
                "-fx-background-color: #0F0F0F;" +
                "-fx-border-color: transparent;"
        );

        root.setTop(header);
        root.setLeft(sidebar);
        root.setCenter(contentScroll);

        documentsScene.setRoot(root);
    }

    // ============================================================
    // ALERT
    // ============================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

        Alert alert =
                new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}