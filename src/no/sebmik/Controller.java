package no.sebmik;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private TwitchListener listener;
    @FXML
    public TextField name;
    @FXML
    public TextField oauth;
    @FXML
    public TextArea main;
    @FXML
    public Button connect;
    @FXML
    public ScrollPane scroll;
    @FXML
    public Label connectionstatus;
    @FXML
    public Label forsenLabel;
    @FXML
    public Label pajLabel;
    @FXML
    public TextField winMessage;
    @FXML
    public CheckBox autoRoulette;
    public Config config;

    public void connect(javafx.event.ActionEvent actionEvent) {
        if (listener == null || !listener.bot.isConnected()) {
            addTextToMain("Connecting");
            config = new Config(name.getText(), oauth.getText(), winMessage.getText(), autoRoulette.isSelected());
            config.save();
            listener = new TwitchListener(this);
            listener.init(config);
            listener.start();
        } else {
            addTextToMain("Disconnecting");
            listener.stop();
        }
    }

    public void addTextToMain(String s) {
        Platform.runLater(() -> {
            main.appendText(String.format("[%s] %s\n", getTime(), s));
            scroll.setVvalue(1.0);
        });
    }

    public void setAutoRoulette(javafx.event.ActionEvent e) {
        CheckBox c = (CheckBox) e.getSource();
        config.setAutoRoulette(c.selectedProperty().getValue());
    }

    public void forsen(javafx.event.ActionEvent e) {
        ToggleButton tb = (ToggleButton) e.getSource();
        if (tb.isSelected()) {
            if (listener != null && listener.bot.isConnected()) {
                addTextToMain("Joining #forsenlol");
                listener.bot.send().joinChannel("#forsenlol");
            } else {
                addTextToMain("You need to be connected to the server first");
                tb.setSelected(false);
            }
        } else {
            if (listener != null && listener.bot.isConnected()) {
                // TODO LEAVE
                listener.bot.getUserChannelDao().getChannel("#forsenlol").send().part();
                addTextToMain("Leaving #forsenlol");
            }
        }
    }

    public void pajlada(javafx.event.ActionEvent e) {
        ToggleButton tb = (ToggleButton) e.getSource();
        if (tb.isSelected()) {
            if (listener != null && listener.bot.isConnected()) {
                addTextToMain("Joining #pajlada");
                listener.bot.send().joinChannel("#pajlada");
            } else {
                addTextToMain("You need to be connected to the server first");
                tb.setSelected(false);
            }
        } else {
            if (listener != null && listener.bot.isConnected()) {
                // TODO LEAVE
                listener.bot.getUserChannelDao().getChannel("#pajlada").send().part();
                addTextToMain("Leaving #pajlada");
            }
        }
    }

    public void setConnectionStatus(boolean b) {
        Platform.runLater(() -> {
            if (b) {
                connectionstatus.setTextFill(Color.GREEN);
                connectionstatus.setText("Connected");
                connect.setText("Disconnect");
                addTextToMain("Connected to server");
            } else {
                connectionstatus.setTextFill(Color.RED);
                connectionstatus.setText("Not connected");
                connect.setText("Connect");
                addTextToMain("Disconnected from server");
            }
        });
    }

    public void setForsenJoined(boolean b) {
        Platform.runLater(() -> {
            if (b) {
                forsenLabel.setTextFill(Color.GREEN);
                forsenLabel.setText("Connected");
            } else {
                forsenLabel.setTextFill(Color.RED);
                forsenLabel.setText("Not connected");
            }
        });
    }

    public void setPajladaJoined(boolean b) {
        Platform.runLater(() -> {
            if (b) {
                pajLabel.setTextFill(Color.GREEN);
                pajLabel.setText("Connected");
            } else {
                pajLabel.setTextFill(Color.RED);
                pajLabel.setText("Not connected");
            }
        });
    }

    private void setConfig(Config config) {
        this.config = config;
        name.setText(config.botName);
        oauth.setText(config.oauth);
        winMessage.setText(config.winMessage);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setConfig(new Config());
    }

    private String getTime() {
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    public void setWinMessage(ActionEvent event) {
        config.setWinMessage(winMessage.getText());
        addTextToMain("Win message saved.");
    }
}
