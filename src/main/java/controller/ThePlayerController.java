package controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;

public class ThePlayerController {
    private MediaPlayer player;
    private ObservableList<String> list;
    private ProgressBar progressBar;
    private Label currentTime;
    private Label totalDuration;
    private final StackPane mediaContainer = new StackPane();
    private StackPane view;
    private int currentMedia = -1;

    public void setProgressBar(ProgressBar progressBar){
        this.progressBar = progressBar;
    }

    //Formating function
    public String format(Duration duration){
        //check if null
        if(duration == null || duration.isUnknown()){
            return "00 : 00";
        }

        //convert duration to seconds and cast from duration to int
        int totalSeconds = (int) Math.floor((duration.toSeconds()));

        //easy to work with the cast int than Duration to avoid errors
        int runtimeMinutes = totalSeconds / 60; //to know the minutes
        int hours = runtimeMinutes / 60;
        int minutes = runtimeMinutes % 60;
        int seconds = totalSeconds % 60; //the remainder of the totalSeconds(runtime) used as seconds

        return String.format("%2d : %02d : %02d",hours, minutes, seconds);
    }

    public void setLabels(Label currentTime, Label totalDuration){
        this.currentTime = currentTime;
        this.totalDuration = totalDuration;
    }

    public void play(int index){
        if(index < 0 || index > list.size()) return;
        currentMedia = index;

        if(player != null){
            player.stop();
            player.dispose();
        }

        Media media = new Media(new File(list.get(index)).toURI().toString());
        player = new MediaPlayer(media);

        player.setOnReady(()->{
            player.play();

            if(new File(list.get(index)).getName().endsWith(".mp4")){
                this.mediaView();
            }

            DoubleBinding binding = Bindings.createDoubleBinding(()->{
                if(player.getTotalDuration().toMillis() <= 0) return 0.0;

                return player.getCurrentTime().toMillis() / player.getTotalDuration().toMillis();
            },player.currentTimeProperty(), player.totalDurationProperty());

            progressBar.progressProperty().bind(binding);

            player.currentTimeProperty().addListener((obs,newTime,oldTime)->{
                Duration total = player.getTotalDuration();
                currentTime.setText(this.format(newTime));
                totalDuration.setText(this.format(total));
            });

            player.setOnEndOfMedia(this::playNextMedia);
        });
    }
    public void setList(ObservableList<String> mediaList){
        this.list = mediaList;
    }

    public void setView(StackPane view){
        this.view = view;
    }

    public void mediaView(){
        mediaContainer.setAlignment(Pos.CENTER);
        mediaContainer.setId("media-container");
        MediaView media_view = new MediaView(player);
        mediaContainer.getChildren().add(media_view);

        media_view.fitWidthProperty().bind(mediaContainer.widthProperty());
        media_view.fitHeightProperty().bind(mediaContainer.heightProperty());
        //set to fit without distortion
        media_view.setPreserveRatio(true);

        view.getChildren().setAll(mediaContainer);
    }

    public void pausePlay(Button pausePlayButton, boolean playState){
        if(playState) {
            player.pause();
            pausePlayButton.setGraphic(new FontIcon("mdi-play"));
            ((FontIcon) pausePlayButton.getGraphic()).setIconSize(30);
        }else{
            player.play();
            pausePlayButton.setGraphic(new FontIcon("mdi-pause"));
            ((FontIcon) pausePlayButton.getGraphic()).setIconSize(30);
        }
    }


    public void playNextMedia(){
        currentMedia++;

        if(player != null) {
            if (currentMedia >= 0 && currentMedia < list.size()) {
                play(currentMedia);
            }
        }
    }

    public void playPreviousMedia(){
        currentMedia--;

        if(player != null) {
            if (currentMedia >= 0 && currentMedia < list.size()) {
                play(currentMedia);
            }
        }
    }
}
