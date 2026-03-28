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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class MplayerController implements Initializable{

    //fxml variables by fx:id
    @FXML StackPane home;
    @FXML StackPane music;
    @FXML StackPane video;
    @FXML ImageView forward;
    @FXML ImageView backward;
    @FXML ImageView play_pause;
    @FXML BorderPane home_window;
    @FXML BorderPane music_window;
    @FXML BorderPane videos_window;
    @FXML ListView<String> musicList;
    @FXML ListView<String> videoList;

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
    @FXML
    private void next(){
        System.out.println("forwarding");
    }
    @FXML
    private void previous(){
        System.out.println("previous");
    }
    @FXML
    private void pause_Play(){
        System.out.println("pausing/playing");
    }

    ObservableList<String> music_list = FXCollections.observableArrayList();

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
    public void fetchMusic(){
        Task<List<String>> task = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                String home = System.getProperty("user.home");

                File folder = new File(home, "Music");

                return scan(folder,".mp3");
            }
        };
        task.setOnSucceeded(eh -> music_list.setAll(task.getValue()));
        new Thread(task).start();
    }

    ObservableList<String> videos = FXCollections.observableArrayList();
    public void fetchVideos(){
        Task<List<String>> vTask = new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                String home = System.getProperty("user.home");
                File folder = new File(home, "Videos");

                return scan(folder,".mp4");
            }
        };
        vTask.setOnSucceeded(eh-> videos.setAll(vTask.getValue()));
        new Thread(vTask).start();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        home_window.toFront();
        fetchMusic();
        fetchVideos();
        videoList.setItems(videos);
        videoList.setCellFactory(lv->{
            ListCell<String> cell = new ListCell<String>(){
                @Override
                protected void updateItem(String path, boolean empty){
                    super.updateItem(path, empty);
                    setText(empty ? null : new File(path).getAbsolutePath());
                }
            };
            cell.setOnMouseClicked(event->{
                System.out.print("clicked");
                if(!cell.isEmpty()){
                    String path = cell.getItem();
                    System.out.print(path);
                }
            });
            return cell;
        });
        musicList.setItems(music_list);
    }
}
