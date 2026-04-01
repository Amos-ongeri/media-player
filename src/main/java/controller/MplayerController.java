package controller;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public class MplayerController implements Initializable{

    //fxml variables by fx:id
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
    @FXML ToggleButton homeButton;
    @FXML ToggleButton musicButton;
    @FXML ToggleButton videosButton;
    @FXML private ListView<String> musicList;
    private final ListView<String> videoList = new ListView<>();
    private final MediaView media_view = new MediaView();
    private final StackPane mediaContainer = new StackPane();
    private final ThePlayerController playerController = new ThePlayerController();



    //event methods
    @FXML
    private void homeView(){
        home_window.toFront();
    }
    @FXML
    private void musicView(){
        music_window.toFront();
        fetchMedia("Music", ".mp3",music_list);
        musicList.setItems(music_list);
        cellFactory(musicList);
    }
    @FXML
    private void videoView(){
        videos_window.toFront();
        fetchMedia("Videos", ".mp4",videos);
        videoList.setItems(videos);
        videoViews.getChildren().setAll(videoList);
        cellFactory(videoList);
    }
    @FXML void prevMedia(){
        playerController.playPreviousMedia();
    }
    @FXML void nextMedia(){
        playerController.playNextMedia();
    }

    private boolean playState = false;
    @FXML void pausePlayMedia(){
        playState = !playState;
        playerController.pausePlay(pausePlayButton, playState);
    }

    ObservableList<String> music_list = FXCollections.observableArrayList();
    ObservableList<String> videos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initIcons();
        home_window.toFront();
    }

    public void initIcons(){
        FontIcon prev = new FontIcon("mdi-skip-previous");
        prevButton.setGraphic(prev);
        prev.setIconColor(Color.rgb(41, 53, 57));
        prev.setIconSize(20);
        FontIcon next = new FontIcon("mdi-skip-next");
        nextButton.setGraphic(next);
        next.setIconColor(Color.rgb(41, 53, 57));
        next.setIconSize(20);
        FontIcon play = new FontIcon("mdi-pause");
        play.setIconSize(30);
        play.setIconColor(Color.rgb(41, 53, 57));
        pausePlayButton.setGraphic(play);
        FontIcon home = new FontIcon("mdi-home");
        home.setIconSize(30);
        home.setIconColor(Color.rgb(41, 53, 57));
        homeButton.setGraphic(home);
        FontIcon music = new FontIcon("mdi-music-box-outline");
        music.setIconSize(30);
        music.setIconColor(Color.rgb(41, 53, 57));
        musicButton.setGraphic(music);
        FontIcon film = new FontIcon("mdi-filmstrip");
        film.setIconSize(30);
        film.setIconColor(Color.rgb(41, 53, 57));
        videosButton.setGraphic(film);
    }

    //cellFactory
    public void cellFactory(ListView<String> l){
        l.setCellFactory(lv -> new ListCell<>(){
            final FontIcon playI = new FontIcon("mdi-play");
            final Button playB = new Button();
            final HBox cell = new HBox(10);

            {
                playI.setIconColor(Color.rgb(53, 231, 199));
                playI.setIconSize(10);
                playB.setGraphic(playI);
                playB.setId("cell-play-button");
            }
            @Override
            protected  void updateItem(String path, boolean empty){
                super.updateItem(path,empty);
                if(empty){
                    setGraphic(null);
                }else{
                    Label name = new Label( new File(path).getName());
                    cell.getChildren().setAll(playB,name);
                    setGraphic(cell);
                    playB.setOnAction(e->{
                        name.setId("current-media");
                        int cellIndex = getIndex();
                        playerController.setProgressBar(progress);
                        playerController.setLabels(currentTime,totalTime);
                        playerController.setView(videoViews);
                        playerController.play(cellIndex);
                    });
                }
            }
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
        playerController.setList(view);
        Task<List<String>> Task = new Task<>() {
            @Override
            protected List<String> call() {
                String home = System.getProperty("user.home");

                File folder = new File(home, dir);

                return scan(folder,et);
            }
        };
        Task.setOnSucceeded(eh -> view.setAll(Task.getValue()));
        new Thread(Task).start();
    }
}
