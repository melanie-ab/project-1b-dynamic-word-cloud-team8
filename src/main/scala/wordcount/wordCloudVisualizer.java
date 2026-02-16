package wordcount;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class wordCloudVisualizer extends Application {

    private static wordCloudVisualizer instance; // Singleton instance
    private Text wordCloudText;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        instance = this; // Assign the singleton instance
        wordCloudText = new Text();
        StackPane root = new StackPane(wordCloudText);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Word Cloud Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to update the displayed word cloud
    public void updateWordCloud(String wordCloud) {
        // Ensure this method runs on the JavaFX Application Thread
        Platform.runLater(() -> wordCloudText.setText(wordCloud));
    }

    // Static method to access the singleton instance
    public static wordCloudVisualizer getInstance() {
        return instance;
    }
}
