package no.sebmik;

import ch.qos.logback.classic.Level;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;

class RaffleBot extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        setLog();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);
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
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ERROR);
        try {
            PrintStream printStream = new PrintStream(new BufferedOutputStream(new FileOutputStream("rafflebot.log", true)));
//            System.setOut(printStream);
//            System.setErr(printStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
