package project.ui.theme;

/**
 * Shared presentation-only palette and JavaFX CSS snippets.
 * This class contains no navigation, data, Firebase, or business logic.
 */
public final class DarkTheme {
    public static final String PRIMARY = "#F59E0B";
    public static final String PRIMARY_DARK = "#D97706";
    public static final String PRIMARY_LIGHT = "#FBBF24";
    public static final String BACKGROUND = "#0F0F0F";
    public static final String SURFACE = "#1A1A1A";
    public static final String SURFACE_LIGHT = "#242424";
    public static final String SURFACE_HOVER = "#2B2B2B";
    public static final String SIDEBAR = "#161616";
    public static final String SIDEBAR_SELECTED = "#3A2A0C";
    public static final String BORDER = "#F59E0B";
    public static final String BORDER_HOVER = "#F59E0B";
    public static final String TEXT_PRIMARY = "#F3F4F6";
    public static final String TEXT_SECONDARY = "#A1A1AA";
    public static final String TEXT_MUTED = "#71717A";
    public static final String SUCCESS = "#22C55E";
    public static final String WARNING = "#F59E0B";
    public static final String ERROR = "#EF4444";
    public static final String INFO = "#3B82F6";

    private DarkTheme() { }

    public static void apply(javafx.scene.Parent root) {
        if (root == null) {
            return;
        }
        String stylesheet = DarkTheme.class
                .getResource("roadguardian-dark.css")
                .toExternalForm();
        if (!root.getStylesheets().contains(stylesheet)) {
            root.getStylesheets().add(stylesheet);
        }
    }

    public static String root() { return "-fx-background-color:" + BACKGROUND + ";-fx-font-family:'Segoe UI';"; }
    public static String header() { return "-fx-background-color:" + SIDEBAR + ";-fx-border-color:" + BORDER + ";-fx-border-width:0 0 1 0;"; }
    public static String sidebar() { return "-fx-background-color:" + SIDEBAR + ";-fx-border-color:" + BORDER + ";-fx-border-width:0 1 0 0;"; }
    public static String selectedSidebarItem() { return "-fx-background-color:" + SIDEBAR_SELECTED + ";-fx-text-fill:" + PRIMARY_LIGHT + ";-fx-background-radius:10;"; }
    public static String navigationItem() { return "-fx-background-color:transparent;-fx-text-fill:" + TEXT_SECONDARY + ";-fx-background-radius:10;-fx-cursor:hand;"; }
    public static String card() { return "-fx-background-color:" + SURFACE + ";-fx-border-color:" + BORDER + ";-fx-border-width:1;-fx-border-radius:14;-fx-background-radius:14;"; }
    public static String hoverCard() { return "-fx-background-color:" + SURFACE_HOVER + ";-fx-border-color:" + BORDER_HOVER + ";-fx-border-radius:14;-fx-background-radius:14;"; }
    public static String primaryButton() { return "-fx-background-color:" + PRIMARY + ";-fx-text-fill:#111111;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;"; }
    public static String secondaryButton() { return "-fx-background-color:transparent;-fx-text-fill:" + PRIMARY_LIGHT + ";-fx-border-color:" + PRIMARY + ";-fx-border-radius:10;-fx-background-radius:10;-fx-cursor:hand;"; }
    public static String dangerButton() { return "-fx-background-color:#3A1717;-fx-text-fill:" + ERROR + ";-fx-border-color:" + ERROR + ";-fx-border-radius:10;-fx-background-radius:10;-fx-cursor:hand;"; }
    public static String textField() { return "-fx-background-color:" + SURFACE_LIGHT + ";-fx-text-fill:" + TEXT_PRIMARY + ";-fx-prompt-text-fill:" + TEXT_MUTED + ";-fx-border-color:#3F3F46;-fx-border-radius:9;-fx-background-radius:9;"; }
    public static String passwordField() { return textField(); }
    public static String comboBox() { return textField(); }
    public static String datePicker() { return textField(); }
    public static String searchBox() { return textField() + "-fx-padding:9 12;"; }
    public static String tableView() { return "-fx-background-color:" + SURFACE + ";-fx-control-inner-background:" + SURFACE + ";-fx-table-cell-border-color:" + BORDER + ";-fx-text-background-color:" + TEXT_PRIMARY + ";"; }
    public static String listView() { return "-fx-background-color:" + SURFACE + ";-fx-control-inner-background:" + SURFACE + ";-fx-border-color:" + BORDER + ";"; }
    public static String scrollPane() { return "-fx-background-color:transparent;-fx-background:" + BACKGROUND + ";-fx-border-color:transparent;"; }
    public static String dialog() { return card() + "-fx-text-fill:" + TEXT_PRIMARY + ";"; }
    public static String statusBadge(String colour) { return "-fx-text-fill:" + colour + ";-fx-background-color:" + colour + "22;-fx-border-color:" + colour + "66;-fx-border-radius:12;-fx-background-radius:12;-fx-padding:4 9;-fx-font-weight:bold;"; }
    public static String successBadge() { return statusBadge(SUCCESS); }
    public static String warningBadge() { return statusBadge(WARNING); }
    public static String errorBadge() { return statusBadge(ERROR); }
    public static String sectionHeading() { return "-fx-text-fill:" + TEXT_PRIMARY + ";-fx-font-size:18px;-fx-font-weight:bold;"; }
    public static String pageTitle() { return "-fx-text-fill:" + TEXT_PRIMARY + ";-fx-font-size:28px;-fx-font-weight:bold;"; }
    public static String secondaryText() { return "-fx-text-fill:" + TEXT_SECONDARY + ";"; }
    public static String tooltip() { return "-fx-background-color:" + SURFACE_HOVER + ";-fx-text-fill:" + TEXT_PRIMARY + ";-fx-border-color:" + BORDER + ";-fx-background-radius:8;-fx-border-radius:8;"; }
}
