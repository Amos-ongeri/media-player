package controller;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class MplayerController implements Initializable{

    //fxml variables by fx:id
    @FXML StackPane home;
    @FXML StackPane music;
    @FXML StackPane video;
    @FXML BorderPane home_window;
    @FXML BorderPane music_window;
    @FXML BorderPane videos_window;
    @FXML StackPane videoViews;
    @FXML ProgressBar progress;
    @FXML Label currentTime;
    @FXML Label totalTime;
    @FXML Button prevButton;
    @FXML Button nextButton;
    @FXML Button pausePlayButton;
    @FXML private ListView<String> musicList;
    private final ListView<String> videoList = new ListView<>();
    private final MediaView media_view = new MediaView();
    private MediaPlayer player;
    private final StackPane mediaContainer = new StackPane();


    //event methods
    @FXML
    private void homeWindow(){
        home_window.toFront();
    }
    @FXML
    private void musicWindow(){
        music_window.toFront();
    }
    @FXML
    private void videoWindow(){
        videos_window.toFront();
    }
    @FXML void prevMedia(){

    }
    @FXML void nextMedia(){

    }

    private boolean playState = false;
    @FXML void pausePlayMedia(){
        playState = !playState;
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

    ObservableList<String> music_list = FXCollections.observableArrayList();
    ObservableList<String> videos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initIcons();
        home_window.toFront();
        mediaContainer.setAlignment(Pos.CENTER);
        mediaContainer.setId("media-container");

        mediaContainer.getChildren().add(media_view);
        media_view.fitWidthProperty().bind(mediaContainer.widthProperty());
        media_view.fitHeightProperty().bind(mediaContainer.heightProperty());
        //set to fit without distortion
        media_view.setPreserveRatio(true);

        fetchMedia("Videos", ".mp4",videos);
        videoList.setItems(videos);
        videoViews.getChildren().setAll(videoList);
        cellFactory(videoList);
        fetchMedia("Music", ".mp3",music_list);
        musicList.setItems(music_list);
        cellFactory(musicList);
    }

    public void initIcons(){
        FontIcon prev = new FontIcon("mdi-skip-previous");
        prevButton.setGraphic(prev);
        prev.setIconSize(20);
        FontIcon next = new FontIcon("mdi-skip-next");
        nextButton.setGraphic(next);
        next.setIconSize(20);
        FontIcon play = new FontIcon("mdi-pause");
        play.setIconSize(30);
        pausePlayButton.setGraphic(play);
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

    //cellFactory
    public void cellFactory(ListView<String> l){
        l.setCellFactory(lv->{
            ListCell<String> cell = new ListCell<String>(){
                @Override
                protected  void updateItem(String path, boolean empty){
                    super.updateItem(path,empty);
                    setText(empty ? null : new File(path).getName());
                }
            };
            cell.setOnMouseClicked(e->{
                if(!cell.isEmpty()){
                    playState = false;
                    String path = cell.getItem();
                    String uri = new File(path).toURI().toString();
                    System.out.println(uri);

                    Media media = new Media(uri);
                    player = new MediaPlayer(media);

                    if(player.getStatus() == MediaPlayer.Status.PLAYING){
                        player.stop();
                        progress.progressProperty().unbind();
                    }

                    player.setOnReady(()->{
                        if(new File(path).getName().toLowerCase().endsWith(".mp4")){
                            mediaContainer.getChildren().setAll(media_view);
                            media_view.setMediaPlayer(player);
                        }

                        DoubleBinding bind = Bindings.createDoubleBinding(()->{
                            if(player.getTotalDuration().toMillis() <= 0 ) return 0.0;
                            return player.getCurrentTime().toMillis() / player.getTotalDuration().toMillis();
                        },player.currentTimeProperty(), player.totalDurationProperty());

                        player.currentTimeProperty().addListener((obs, newTime, oldTime)->{
                            Duration Total = player.getTotalDuration();

                            currentTime.setText(format(newTime));
                            totalTime.setText(format(Total));
                        });

                        progress.progressProperty().bind(bind);
                        player.play();
                    });
                }
            });
            return cell;
        });
    }

    public List<String> scan(File dir, String e){
        File[] list = dir.listFiles();

        List<String> ls = new ArrayList<>();

        if(list == null) return null;

        for(File f : list){
            if(f.isDirectory()) ls.addAll(scan(f,e));
            else if(f.isFile()){
                if(f.getName().toLowerCase().endsWith(e)){
                    ls.add(f.getAbsolutePath());
                }
            }
        }
        return ls;
    }

    public void fetchMedia(String dir, String et, ObservableList<String> view){
        Task<List<String>> Task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                String home = System.getProperty("user.home");

                File folder = new File(home, dir);

                return scan(folder,et);
            }
        };
        Task.setOnSucceeded(eh -> view.setAll(Task.getValue()));
        new Thread(Task).start();
    }
}
