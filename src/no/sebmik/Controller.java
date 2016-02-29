package no.sebmik;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Controller implements Initializable {
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private TwitchListener listener;
    @FXML
    public TextFlow main;
    @FXML
    public ToggleButton connect;
    @FXML
    public ToggleButton connectForsen;
    @FXML
    public ToggleButton connectPajlada;
    @FXML
    public ScrollPane scroll;
    @FXML
    public Label connectionstatus;
    @FXML
    public Label forsenLabel;
    @FXML
    public Label pajLabel;
    @FXML
    public Label raffleTextForsen;
    @FXML
    public Label raffleTimeForsen;
    @FXML
    public Label raffleTextPajlada;
    @FXML
    public Label raffleTimePajlada;
    @FXML
    public TextField winMessage;
    @FXML
    public CheckBox autoJoinRaffle;
    @FXML
    public CheckBox autoRoulette;
    @FXML
    public TextField forsenRouletteText;
    @FXML
    public TextField pajladaRouletteText;
    @FXML
    public Pane forsenPane;
    @FXML
    public Pane pajPane;
    @FXML
    public TextField spamTextForsen;
    @FXML
    public TextField spamTextPajlada;
    @FXML
    public Button forsenSpamButton;
    @FXML
    public Button pajladaSpamButton;
    private Spam forsenSpam;
    private Spam pajladaSpam;
    public Config config;
    private ArrayList<String> emotes;
    private ImageView forsenBadge;
    private ImageView pajBadge;

    @FXML
    public void connect(ActionEvent actionEvent) {
        if (config.validNameAndToken()) {
            if (listener == null || !listener.getBot().isConnected()) {
                addTextToMain("Connecting", null);
                listener = new TwitchListener(this);
                listener.init(config);
                listener.start();
            } else {
                addTextToMain("Disconnecting", null);
                listener.stop();
            }
        } else {
            addTextToMain("Invalid username or token", null);
        }
    }

    public void addTextToMain(String s, String channelName) {
        Platform.runLater(() -> {
            main.getChildren().add(new Text(String.format("[%s] ", getTime())));
            if (channelName != null) {
                if (channelName.contains("forsen")) {
                    main.getChildren().add(newBadge(forsenBadge));
                } else if (channelName.contains("pajlada")) {
                    main.getChildren().add(newBadge(pajBadge));
                }
                main.getChildren().add(new Text(" "));
            }
            List<String> split = Arrays.asList(s.split(" "));
            for (String e : split) {
                if (emotes.contains(e)) {
                    ImageView imageView = new ImageView(ClassLoader.getSystemResource(String.format("emotes/%s.png", e)).toExternalForm());
                    imageView.setFitWidth(imageView.getImage().getWidth() * 0.8);
                    imageView.setFitHeight(imageView.getImage().getHeight() * 0.8);
                    imageView.setTranslateY(4);
                    main.getChildren().add(imageView);
                } else {
                    main.getChildren().add(new Text(e));
                }
                main.getChildren().add(new Text(" "));
            }
            main.getChildren().add(new Text("\n"));
            scroll.setVvalue(1.0);
        });
    }

    public void addURL(String url, String channelName, String username) {
        Platform.runLater(() -> {
            main.getChildren().add(new Text(String.format("[%s] ", getTime())));
            if (channelName != null) {
                if (channelName.contains("forsen")) {
                    main.getChildren().add(newBadge(forsenBadge));
                } else if (channelName.contains("pajlada")) {
                    main.getChildren().add(newBadge(pajBadge));
                }
            }
            main.getChildren().add(new Text(String.format(" %s:", username)));
            Hyperlink hyperlink = new Hyperlink(url);
            hyperlink.setOnAction(t -> {
                try {
                    new ProcessBuilder("x-www-browser", url).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            main.getChildren().add(hyperlink);
            main.getChildren().add(new Text("\n"));
            scroll.setVvalue(1.0);
        });
    }

    public void updateRaffleText(String s, String name) {
        Platform.runLater(() -> {
            if (name.contains("forsen")) {
                raffleTextForsen.setText(s);
            } else if (name.contains("pajlada")) {
                raffleTextPajlada.setText(s);
            }
        });
    }

    public void updateRaffleTime(int t, String name) {
        Platform.runLater(() -> {
            Label label;
            if (name.contains("forsen")) {
                label = raffleTimeForsen;
            } else if (name.contains("pajlada")) {
                label = raffleTimePajlada;
            } else {
                return;
            }
            if (t > 0) {
                label.setVisible(true);
                label.setText(String.valueOf(t));
                label.setAlignment(Pos.BASELINE_CENTER);
            } else {
                label.setVisible(false);
            }
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
    public void forsen(ActionEvent e) {
        ToggleButton tb = (ToggleButton) e.getSource();
        if (tb.isSelected()) {
            if (listener != null && listener.getBot().isConnected()) {
                listener.getBot().send().joinChannel("#forsenlol");
            } else {
                addTextToMain("You need to be connected to the server first", null);
                tb.setSelected(false);
            }
        } else {
            if (listener != null && listener.getBot().isConnected()) {
                listener.getBot().getUserChannelDao().getChannel("#forsenlol").send().part();
            }
        }
    }

    @FXML
    public void pajlada(ActionEvent e) {
        ToggleButton tb = (ToggleButton) e.getSource();
        if (tb.isSelected()) {
            if (listener != null && listener.getBot().isConnected()) {
                listener.getBot().send().joinChannel("#pajlada");
            } else {
                addTextToMain("You need to be connected to the server first", null);
                tb.setSelected(false);
            }
        } else {
            if (listener != null && listener.getBot().isConnected()) {
                listener.getBot().getUserChannelDao().getChannel("#pajlada").send().part();
            }
        }
    }

    public void setConnectionStatus(boolean b) {
        Platform.runLater(() -> {
            if (b) {
                connectionstatus.setTextFill(Color.GREEN);
                connectionstatus.setText("Connected");
                connectionstatus.setAlignment(Pos.BASELINE_RIGHT);
                connect.setText("Disconnect");
                connect.setSelected(true);
                addTextToMain("Connected to server", null);
            } else {
                connectionstatus.setTextFill(Color.RED);
                connectionstatus.setText("Not connected");
                connectionstatus.setAlignment(Pos.BASELINE_RIGHT);
                connect.setText("Connect");
                connect.setSelected(false);
                addTextToMain("Disconnected from server", null);
                setForsenStatus(false);
                setPajladaStatus(false);
                forsenSpam.stop();
                pajladaSpam.stop();
            }
        });
    }

    public void setForsenStatus(boolean b) {
        Platform.runLater(() -> {
            forsenPane.setVisible(b);
            forsenPane.setManaged(b);
            if (b) {
                connectForsen.setText("Disconnect");
                addTextToMain("Joined #forsenlol", "forsen");
            } else {
                connectForsen.setText("Connect");
                connectForsen.setSelected(false);
                addTextToMain("Left #forsenlol", "forsen");
            }
        });
    }

    public void setPajladaStatus(boolean b) {
        Platform.runLater(() -> {
            pajPane.setVisible(b);
            pajPane.setManaged(b);
            if (b) {
                connectPajlada.setText("Disconnect");
                addTextToMain("Joined #pajlada", "pajlada");
            } else {
                connectPajlada.setText("Connect");
                connectPajlada.setSelected(false);
                addTextToMain("Left #pajlada", "pajlada");
            }
        });
    }

    private void setConfig(Config config) {
        this.config = config;
        autoJoinRaffle.setSelected(config.autoJoinRaffle);
        autoRoulette.setSelected(config.autoRoulette);
        winMessage.setText(config.winMessage);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setConfig(new Config());
        try {
            loadEmoteNames();
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadBadges();
    }

    private void loadEmoteNames() throws IOException {
        emotes = new ArrayList<>();
        File folder = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        final JarFile jar = new JarFile(folder);
        final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
        while (entries.hasMoreElements()) {
            final String name = entries.nextElement().getName();
            if (name.startsWith("emotes" + "/") && name.contains(".")) { //filter according to the path
                emotes.add(name.substring(name.indexOf("/") + 1, name.indexOf(".")));
            }
        }
        jar.close();
    }

    private void loadBadges() {
        forsenBadge = new ImageView(ClassLoader.getSystemResource("forsen_badge.png").toExternalForm());
        forsenBadge.setTranslateY(4);
        forsenLabel.setGraphic(new ImageView(forsenBadge.getImage()));
        pajBadge = new ImageView(ClassLoader.getSystemResource("paj_badge.png").toExternalForm());
        pajBadge.setTranslateY(4);
        pajLabel.setGraphic(new ImageView(pajBadge.getImage()));
    }

    private ImageView newBadge(ImageView iw) {
        ImageView i = new ImageView(iw.getImage());
        i.setTranslateY(4);
        return i;
    }

    private String getTime() {
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    @FXML
    public void setWinMessage(ActionEvent event) {
        config.setWinMessage(winMessage.getText());
        addTextToMain("Win message saved.", null);
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
        TextField name = new TextField();
        TextField token = new TextField();
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
    public void forsenRoulette(ActionEvent event) {
        int p = Integer.parseInt(forsenRouletteText.getText());
        if (listener.getBot().isConnected()) {
            listener.getBot().getUserChannelDao().getChannel("#forsenlol").send().message(String.format("!roulette %s", p));
        }
        forsenRouletteText.setText("");
    }

    public void forsenAllIn(ActionEvent event) {
        if (listener.getBot().isConnected()) {
            listener.getBot().getUserChannelDao().getChannel("#forsenlol").send().message(String.format("!roulette %s", "all"));
        }
    }

    public void pajladaRoulette(ActionEvent event) {
        int p = Integer.parseInt(pajladaRouletteText.getText());
        if (listener.getBot().isConnected()) {
            listener.getBot().getUserChannelDao().getChannel("#pajlada").send().message(String.format("!roulette %s", p));
        }
        pajladaRouletteText.setText("");
    }

    public void pajladaAllIn(ActionEvent event) {
        if (listener.getBot().isConnected()) {
            listener.getBot().getUserChannelDao().getChannel("#pajlada").send().message(String.format("!roulette %s", "all"));
        }
    }

    public void spamForsen(ActionEvent event) {
        if (forsenSpam == null || !forsenSpam.running) {
            String s = spamTextForsen.getText();
            if (s != null && s.length() > 0) {
                forsenSpam = new Spam(s, listener.getBot().getUserChannelDao().getChannel("#forsenlol"), listener.getHistory());
                forsenSpamButton.setText("Stop");
                forsenSpam.start();
            }
        } else {
            forsenSpamButton.setText("Spam");
            forsenSpam.stop();
        }
    }

    public void spamPajlada(ActionEvent event) {
        if (pajladaSpam == null || !pajladaSpam.running) {
            String s = spamTextPajlada.getText();
            if (s != null && s.length() > 0) {
                pajladaSpam = new Spam(s, listener.getBot().getUserChannelDao().getChannel("#pajlada"), listener.getHistory());
                pajladaSpamButton.setText("Stop");
                pajladaSpam.start();
            }
        } else {
            pajladaSpamButton.setText("Spam");
            pajladaSpam.stop();
        }
    }

    public static void main(String[] args) {
        Spam spam = new Spam("asd", null, new History(20));
        spam.start();
    }
}
