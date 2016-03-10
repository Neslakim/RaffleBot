package no.sebmik;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RaffleBot extends Application {
    Log log = new Log(getClass().getSimpleName());
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        setLog();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.getIcons().add(new Image(getClass().getClassLoader().getResource("Kappa.png").toExternalForm()));
        stage.setTitle("Raffle Bot");
        stage.setScene(scene);
        stage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

    private void setLog() {
        Logger log = (Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        log.setLevel(Level.ERROR);
    }
}
