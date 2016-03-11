package no.sebmik;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements Initializable {
    public VBox rightPane;
    public HBox root;
    private Log log = new Log("bot");
    private final String urlRegex = "\\(?(?:(http|https):\\/\\/)?(?:((?:[^\\W\\s]|\\.|-|[:]{1})+)@{1})?((?:www.)?(?:[^\\W\\s]|\\.|-)+[\\.][^\\W\\s]{2,4}|localhost(?=\\/)|\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})(?::(\\d*))?([\\/]?[^\\s\\?]*[\\/]{1})*(?:\\/?([^\\s\\n\\?\\[\\]\\{\\}\\#]*(?:(?=\\.)){1}|[^\\s\\n\\?\\[\\]\\{\\}\\.\\#]*)?([\\.]{1}[^\\s\\?\\#]*)?)?(?:\\?{1}([^\\s\\n\\#\\[\\]]*))?([\\#][^\\s\\n]*)?\\)?";
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private Listener listener;
    @FXML
    private CheckBox mentions;
    @FXML
    public CheckBox allMessages;
    @FXML
    private TextField duelPoints;
    @FXML
    private TextField duelName;
    @FXML
    private TextField betPoints;
    @FXML
    private FlowPane main;
    @FXML
    private Button connect;
    @FXML
    private Button connectChannel;
    @FXML
    private ScrollPane scroll;
    @FXML
    private Label connectionstatus;
    @FXML
    private TextField channelNameInput;
    @FXML
    private Label raffleText;
    @FXML
    private Label raffleTime;
    @FXML
    private TextField downloadEmotes;
    @FXML
    private CheckBox autoJoinRaffle;
    @FXML
    private CheckBox autoRoulette;
    @FXML
    private TextField rouletteText;
    @FXML
    private Pane channelContent;
    @FXML
    private TextArea textInput;
    @FXML
    private Button spamButton;
    @FXML
    private Pane overlayPane;
    @FXML
    private CheckBox bttvOverlayCB;
    @FXML
    private CheckBox darkMode;
    @FXML
    public CheckBox whispers;
    @FXML
    private CheckBox filter;
    private boolean connectedChannel;
    private Spam spam;
    public Config config;
    private List<String> emotes;
    private ImageView badge;
    private ImageView botBadge;
    private ImageView bcBadge;
    private ImageView modBadge;
    private Random r;
    private Stage overlayStage;
    protected boolean overlay;
    protected boolean bttvOverlay;
    private boolean alternate;
    protected final String sysColor = "#008000";
    public String channelName;
    public HashMap<String, String> bttvEmotes;

    @FXML
    public void connect() {
        if (config.validNameAndToken()) {
            if (listener == null || !listener.getMain().isConnected()) {
                printMessage("", "", "Connecting", false, false, false, sysColor);
                listener = new Listener(this);
                listener.init(config);
                listener.start();
            } else {
                printMessage("", "", "Disconnecting", false, false, false, sysColor);
                listener.stop();
            }
        } else {
            printMessage("asd", "", "Invalid username or token", false, false, false, sysColor);
        }
    }

    public synchronized void printMessage(String sender, String receiver, String message, boolean bot, boolean mod, boolean sub, String color) {
        if (color == null || color.length() != 7) {
            color = getRandomColor();
        }
        final String finalColor = color;
        Platform.runLater(() -> {
            FlowPane pane = new FlowPane();
            pane.setPrefWrapLength(scroll.getWidth() - 10);
            pane.setPadding(new Insets(4, 0, 4, 0));
            pane.setBorder(new Border(
                    new BorderStroke(Paint.valueOf("#fff"),
                            Paint.valueOf("#fff"),
                            Paint.valueOf("#000"),
                            Paint.valueOf("#fff"),
                            BorderStrokeStyle.NONE,
                            BorderStrokeStyle.NONE,
                            BorderStrokeStyle.SOLID,
                            BorderStrokeStyle.NONE,
                            CornerRadii.EMPTY,
                            BorderWidths.DEFAULT,
                            new Insets(0))));

            if (message.toLowerCase().contains(config.name.toLowerCase())) {
                pane.setStyle("-fx-background-color: #8E0F0F");
            } else if (alternate) {
                if (config.darkMode) {
                    pane.setStyle("-fx-background-color: #1E1E1E;");
                } else {
                    pane.setStyle("-fx-background-color: #D3D3D3;");
                }
            } else {
                if (config.darkMode) {
                    pane.setStyle("-fx-background-color: #343434;");
                } else {
                    pane.setStyle("-fx-background-color: #F4F4F4;");
                }
            }
            alternate = !alternate;
            pane.getChildren().add(createText(String.format(" %s", getTime()), "#8c8c8c", false));
            if (sender.equalsIgnoreCase(channelName)) {
                pane.getChildren().add(new Text(" "));
                pane.getChildren().add(newBadge(bcBadge));
            }
            if (bot) {
                pane.getChildren().add(new Text(" "));
                pane.getChildren().add(newBadge(botBadge));
            }
            if (mod && !bot) {
                pane.getChildren().add(new Text(" "));
                pane.getChildren().add(newBadge(modBadge));
            }
            if (sub) {
                pane.getChildren().add(new Text(" "));
                pane.getChildren().add(newBadge(badge));
            }
            if (sender.length() > 0) {
                if (message.toLowerCase().contains(config.name.toLowerCase())) {
                    pane.getChildren().add(createText(String.format(" %s", sender), "#D3D3D3", true));
                } else {
                    pane.getChildren().add(createText(String.format(" %s", sender), finalColor, true));
                }
                if (receiver.length() > 0) {
                    pane.getChildren().add(createText(String.format(" %c ", 0x25B8), null, false));
                    if (message.toLowerCase().contains(config.name.toLowerCase())) {
                        pane.getChildren().add(createText(String.format("%s", receiver), "#D3D3D3", true));
                    } else {
                        pane.getChildren().add(createText(String.format("%s", receiver), listener.getColor(), true));
                    }
                }
                pane.getChildren().add(createText(":", null, false));
            }
            List<String> split = Arrays.asList(message.split(" "));
            for (String e : split) {
                pane.getChildren().add(new Text(" "));
                if (isUrl(e)) {
                    Hyperlink hyperlink = new Hyperlink(e);
                    hyperlink.setPadding(new Insets(0));
                    hyperlink.setOnAction(t -> {
                        try {
                            new ProcessBuilder("x-www-browser", e).start();
                        } catch (IOException ioe) {
                            log.e(ioe, true);
                        }
                    });
                    pane.getChildren().add(hyperlink);
                } else if (emotes.contains(e)) {
                    pane.getChildren().add(createEmoteLabel(String.format("emotes/1x/%s.png", e), e));
                } else if (bttvEmotes.get(e) != null) {
                    pane.getChildren().add(createEmoteLabel(String.format("emotes/bttv/1x/%s", bttvEmotes.get(e)), e));
                } else {
                    pane.getChildren().add(createText(e, null, false));
                }
            }
            if (main.getChildren().size() > config.scrollBack) {
                main.getChildren().remove(0);
            }
            main.getChildren().add(pane);
            scroll.layout();
            scroll.setVvalue(1.0);
        });
    }

    public boolean isUrl(String s) {
        Matcher m = Pattern.compile(urlRegex).matcher(s);
        return m.find();
    }

    private Label createEmoteLabel(String e, String n) {
        ImageView imageView = new ImageView(new File(e).toURI().toString());
        imageView.setFitWidth(imageView.getImage().getWidth() * 0.8);
        imageView.setFitHeight(imageView.getImage().getHeight() * 0.8);
        Label label = new Label();
        label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        label.setGraphic(imageView);
        label.setTooltip(new Tooltip(n));
        return label;
    }

    private Text createText(String message, String color, boolean bold) {
        Text text = new Text(message);
        if (color != null && color.length() == 7) {
            text.setFill(Color.valueOf(color));
        } else if (config.darkMode) {
            text.setFill(Color.valueOf("#D3D3D3"));
        }
        if (bold) {
            text.setStyle("-fx-font-weight: bold");
        }
        return text;
    }

    public void updateRaffleText(String s) {
        Platform.runLater(() -> raffleText.setText(s));
    }

    public void updateRaffleTime(int t) {
        Platform.runLater(() -> {
            Label label = raffleTime;
            label.setText(String.valueOf(t));
            label.setAlignment(Pos.BASELINE_CENTER);
        });
    }

    @FXML
    public void setAutoRoulette(ActionEvent e) {
        CheckBox c = (CheckBox) e.getSource();
        config.setAutoRoulette(c.selectedProperty().getValue());
    }

    @FXML
    public void setAutoJoinRaffle(ActionEvent e) {
        CheckBox c = (CheckBox) e.getSource();
        config.setAutoJoinRaffle(c.selectedProperty().getValue());
    }

    @FXML
    public void connectChannel() {
        if (!connectedChannel) {
            channelName = channelNameInput.getText().toLowerCase();
            if (listener != null && listener.getMain().isConnected()) {
                loadBadge();
                listener.getMain().send().joinChannel("#" + channelName);
                new Thread(() -> {
                    Downloader downloader = new Downloader();
                    downloader.downloadBadge(channelName);
                    downloader.downloadTwitchEmotes(channelName, 1);
                    downloader.downloadTwitchEmotes(channelName, 3);
                    downloader.downloadBTTVEmotes(channelName, 1);
                    downloader.downloadBTTVEmotes(channelName, 3);
                }).start();
            } else {
                printMessage("", "", "You need to be connected to the server first", false, false, false, sysColor);
            }
        } else {
            if (listener != null && listener.getMain().isConnected()) {
                listener.getMain().getUserChannelDao().getChannel("#" + channelName).send().part();
            }
        }
    }

    public void setConnectionStatus(boolean b) {
        Platform.runLater(() -> {
            if (b) {
                connectionstatus.setText("Connected");
                connectionstatus.setAlignment(Pos.BASELINE_RIGHT);
                connect.setText("Disconnect");
                printMessage("", "", "Connected to server", false, false, false, sysColor);
            } else {
                connectionstatus.setText("Not connected");
                connectionstatus.setAlignment(Pos.BASELINE_RIGHT);
                connect.setText("Connect");
                connectedChannel = false;
                printMessage("", "", "Disconnected from server", false, false, false, sysColor);
                setChannelStatus(false);
                spam.stop();
            }
        });
    }

    public void setChannelStatus(boolean b) {
        Platform.runLater(() -> {
            channelContent.setDisable(!b);
            if (b) {
                connectChannel.setText("Disconnect");
                connectedChannel = true;
                printMessage("", "", "Joined #" + channelName, false, false, false, sysColor);
            } else {
                connectChannel.setText("Connect");
                connectedChannel = false;
                printMessage("", "", "Left #" + channelName, false, false, false, sysColor);
            }
        });
    }

    private void setConfig(Config config) {
        this.config = config;
        autoJoinRaffle.setSelected(config.autoJoinRaffle);
        autoRoulette.setSelected(config.autoRoulette);
        mentions.setSelected(config.mentions);
        allMessages.setSelected(config.allMessages);
        filter.setSelected(config.filter);
        darkMode.setSelected(config.darkMode);
        whispers.setSelected(config.whispers);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        r = new Random();
        setConfig(new Config());
        loadBotBadge();
        loadBcBadge();
        loadModBadge();
        downloadGlobalEmotes();
        try {
            loadEmoteNames();
            loadBTTVEmoteNames();
        } catch (IOException e) {
            e.printStackTrace();
        }
        channelNameInput.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                connectChannel();
            }
        });
        setDarkMode(config.darkMode);
    }

    private void loadBotBadge() {
        botBadge = new ImageView(getClass().getClassLoader().getResource("bot.png").toExternalForm());
    }

    private void loadBcBadge() {
        bcBadge = new ImageView(getClass().getClassLoader().getResource("badge_broadcaster.png").toExternalForm());
    }

    private void loadModBadge() {
        modBadge = new ImageView(getClass().getClassLoader().getResource("badge_mod.png").toExternalForm());
    }

    private void loadEmoteNames() throws IOException {
        emotes = new ArrayList<>();
        File folder = new File("emotes/1x");
        File[] files = folder.listFiles();
        for (File file : files != null ? files : new File[0]) {
            String name = file.getName();
            String a = name.substring(name.indexOf("/") + 1, name.indexOf("."));
            emotes.add(a);
        }
    }

    private void loadBTTVEmoteNames() throws IOException {
        bttvEmotes = new HashMap<>();
        File folder = new File("emotes/bttv/1x");
        File[] files = folder.listFiles();
        for (File file : files != null ? files : new File[0]) {
            String name = file.getName();
            String a = name.substring(name.indexOf("/") + 1, name.lastIndexOf('.'));
            String b = name.substring(name.indexOf("/") + 1);
            bttvEmotes.put(a, b);
        }
    }

    private void loadBadge() {
        File file = new File(String.format("badge/%s.png", channelName));
        badge = new ImageView(file.toURI().toString());
    }

    private ImageView newBadge(ImageView iw) {
        return new ImageView(iw.getImage());
    }

    private String getTime() {
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    @FXML
    public void downloadEmotes() {
        String name = downloadEmotes.getText();
        if (name != null && name.length() > 0) {
            new Thread(() -> {
                Downloader downloader = new Downloader();
                downloader.downloadTwitchEmotes(name, 1);
                downloader.downloadTwitchEmotes(name, 3);
                downloader.downloadBTTVEmotes(name, 1);
                downloader.downloadBTTVEmotes(name, 3);
            }).start();
        }
    }

    public void loginDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, loginButtonType);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));
        TextField name = new TextField(config.name);
        TextField token = new TextField(config.oauth);
        Label error = new Label("");
        error.setTextFill(Color.RED);
        grid.add(new Label("Username:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("OAuth:"), 0, 1);
        grid.add(token, 1, 1);
        grid.add(error, 1, 2);
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        name.textProperty().addListener((observable, oldValue, newValue) -> {
            String un = name.getText();
            String pw = token.getText();
            if (un.length() > 0 && pw.length() == 36 && pw.contains("oauth:")) {
                loginButton.setDisable(false);
            } else {
                error.setText("");
            }
        });
        token.textProperty().addListener((observable, oldValue, newValue) -> {
            String un = name.getText();
            String pw = token.getText();
            if (un.length() > 0 && pw.length() == 36 && pw.contains("oauth:")) {
                loginButton.setDisable(false);
                error.setText("");
            } else {
                error.setText("Invalid username or token");
            }
        });
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(name::requestFocus);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(name.getText(), token.getText());
            }
            return null;
        });
        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(res -> {
            config.setName(res.getKey());
            config.setOauth(res.getValue());
        });
    }

    @FXML
    public void roulette() {
        int p = Integer.parseInt(rouletteText.getText());
        if (listener.getMain().isConnected()) {
            listener.sendMessage(String.format("!roulette %s", p));
        }
        rouletteText.setText("");
    }

    @FXML
    public void allIn() {
        if (listener.getMain().isConnected()) {
            listener.sendMessage(String.format("!roulette %s", "all"));
        }
    }

    @FXML
    public void spam() {
        if (spam == null || !spam.running) {
            String s = textInput.getText();
            if (s != null && s.length() > 0) {
                spam = new Spam(s, ".", listener, listener.getHistory());
                spamButton.setText("Stop");
                spam.start();
            }
        } else {
            spamButton.setText("Spam");
            spam.stop();
        }
    }

    @FXML
    public void message() {
        String message = textInput.getText();
        if (message != null && message.length() > 0) {
            listener.sendMessage(message);
            textInput.setText("");
        }
    }

    @FXML
    public void duel() {
        String name = duelName.getText();
        int points = Integer.parseInt(duelPoints.getText());
        if (name != null && name.length() > 0 && points > 0) {
            listener.sendMessage(String.format("!duel %s %s", name, points));
        }
    }

    @FXML
    public void acceptDuel() {
        listener.sendMessage("!accept");
    }

    @FXML
    public void cancelDuel() {
        listener.sendMessage("!cancel");
    }

    @FXML
    public void overlay(ActionEvent event) {
        CheckBox cb = (CheckBox) event.getSource();
        if (cb.isSelected()) {
            try {
                overlayStage = new Stage();
                overlayStage.setTitle("Emote overlay");
                overlayPane = new FXMLLoader(getClass().getResource("overlay.fxml")).load();
                overlayPane.setMouseTransparent(true);
                Scene scene = new Scene(overlayPane);
                scene.setFill(null);
                overlayStage.setScene(scene);
                overlayStage.setFullScreen(true);
                overlayStage.initStyle(StageStyle.TRANSPARENT);
                overlayStage.setAlwaysOnTop(true);
                overlayStage.initModality(Modality.NONE);
                overlayStage.show();
                overlay = true;
                bttvOverlayCB.setDisable(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            overlayStage.close();
            overlay = false;
            bttvOverlayCB.setSelected(false);
            bttvOverlayCB.setDisable(true);
        }

    }

    public void addEmotes(List<String> e) {
        e.forEach(this::addTwitchEmote);
    }

    public void addBTTVEmotes(String message) {
        bttvEmotes.entrySet().stream().filter(e -> message.contains((String) e.getKey())).forEach(e -> {
            addBTTVEmote(e.getKey());
        });
    }

    public void addTwitchEmote(String name) {
        addEmote(new File(String.format("emotes/3x/%s.png", name)));
    }

    public void addBTTVEmote(String name) {
        addEmote(new File("emotes/bttv/3x/" + bttvEmotes.get(name)));
    }

    public void addEmote(File file) {
        try {
            ImageView imageView = new ImageView(file.toURI().toString());
            imageView.setMouseTransparent(true);
            addImageView(imageView);
        } catch (Exception e) {
            log.e(e, true);
        }
    }

    public void addImageView(ImageView imageView) {
        Platform.runLater(() -> {
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            imageView.setX(r.nextInt((int) (primScreenBounds.getWidth() - imageView.getImage().getWidth())));
            imageView.setY(r.nextInt((int) (primScreenBounds.getHeight() - imageView.getImage().getHeight())));
            overlayPane.getChildren().add(imageView);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    removeImageView(imageView);
                }
            }, 5000);
        });
    }

    public void removeImageView(ImageView imageView) {
        Platform.runLater(() -> overlayPane.getChildren().remove(imageView));
    }

    public void downloadGlobalEmotes() {
        new Thread(() -> {
            Downloader downloader = new Downloader();
            downloader.downloadTwitchEmotes(1);
            downloader.downloadTwitchEmotes(3);
            downloader.downloadBTTVEmotes(1);
            downloader.downloadBTTVEmotes(3);
        }).start();
    }

    @FXML
    public void setMentions(ActionEvent event) {
        CheckBox cb = (CheckBox) event.getSource();
        config.setMentions(cb.isSelected());
    }

    @FXML
    public void betWin() {
        int points = Integer.parseInt(betPoints.getText());
        listener.sendMessage(String.format("!hsbet win %s", points));
    }

    @FXML
    public void betLose() {
        int points = Integer.parseInt(betPoints.getText());
        listener.sendMessage(String.format("!hsbet lose %s", points));
    }

    @FXML
    public void bttvOverlay(ActionEvent event) {
        CheckBox cb = (CheckBox) event.getSource();
        bttvOverlay = cb.isSelected();
    }

    @FXML
    public void setAllMessages() {
        config.setAllMessages(allMessages.isSelected());
        mentions.setDisable(allMessages.isSelected());
    }

    @FXML
    public void setFilter(ActionEvent event) {
        CheckBox cb = (CheckBox) event.getSource();
        config.setFilter(cb.isSelected());
        if (cb.isSelected()) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    listener.clearShownMessages();
                }
            }, 600000);
        }
    }

    @FXML
    public void setWhispers(ActionEvent event) {
        CheckBox cb = (CheckBox) event.getSource();
        config.setWhispers(cb.isSelected());
    }

    String[] colorValues = {"#FF0000", "#0000FF", "#008000", "#B22222", "#FF7F50", "#9ACD32", "#FF4500", "#2E8B57",
            "#DAA520", "#D2691E", "#5F9EA0", "#1E90FF", "#FF69B4", "#8A2BE2", "#00FF7F"};

    public String getRandomColor() {
        return colorValues[r.nextInt(colorValues.length)];
    }

    @FXML
    public void darkMode(ActionEvent event) {
        CheckBox cb = (CheckBox) event.getSource();
        config.setDarkMode(cb.isSelected());
        setDarkMode(cb.isSelected());
        for (Node node : main.getChildren()) {
            if (node.getStyle().contains("-fx-background-color: #343434")) {
                node.setStyle("-fx-background-color: #F4F4F4");
            } else if (node.getStyle().contains("-fx-background-color: #F4F4F4")) {
                node.setStyle("-fx-background-color: #343434");
            } else if (node.getStyle().contains("-fx-background-color: #1E1E1E")) {
                node.setStyle("-fx-background-color: #D3D3D3");
            } else if (node.getStyle().contains("-fx-background-color: #D3D3D3")) {
                node.setStyle("-fx-background-color: #1E1E1E");
            }
            final boolean[] first = {true};
            ((FlowPane) node).getChildren().stream().filter(node2 -> node2 instanceof Text).forEach(node2 -> {
                Text text = (Text) node2;
                if (first[0]) {
                    first[0] = false;
                } else {
                    if (config.darkMode) {
                        text.setFill(Color.valueOf("#D3D3D3"));
                    } else {
                        text.setFill(Color.valueOf("#000"));
                    }
                }
            });
        }
    }

    private void setDarkMode(boolean b) {
        if (b) {
            root.getStylesheets().clear();
            root.getStylesheets().add("/resources/dark.css");
        } else {
            root.getStylesheets().clear();
            root.getStylesheets().add("/resources/light.css");
        }
    }
}
