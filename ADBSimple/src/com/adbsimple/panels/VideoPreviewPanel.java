/*
 * package com.adbsimple.panels;
 * 
 * import java.io.File; import java.net.MalformedURLException;
 * 
 * import javafx.application.Platform; import javafx.embed.swing.JFXPanel;
 * import javafx.scene.Scene; import javafx.scene.layout.StackPane; import
 * javafx.scene.media.Media; import javafx.scene.media.MediaPlayer.Status;
 * import javafx.scene.media.MediaView; import javafx.scene.paint.Color;
 * 
 * public class VideoPreviewPanel extends JFXPanel {
 * 
 * private int width, height; javafx.scene.media.MediaPlayer player;
 * 
 * public VideoPreviewPanel(int width, int height) { setVisible(true);
 * setLayout(null); this.width = width; this.height = height; }
 * 
 * public void startPlay(String filepath) {
 * 
 * File f = new File(filepath);
 * 
 * // Converts media to string URL Media media = null; try { media = new
 * Media(f.toURI().toURL().toString()); } catch (MalformedURLException e) {
 * e.printStackTrace(); }
 * 
 * player = new javafx.scene.media.MediaPlayer(media); MediaView viewer = new
 * MediaView(player); viewer.setFitWidth(width); viewer.setFitHeight(height);
 * 
 * final StackPane root = new StackPane(); root.getChildren().add(viewer);
 * Platform.runLater(new Runnable() {
 * 
 * @Override public void run() { // set the Scene Scene scenes = new Scene(root,
 * width, height, Color.BLACK); setScene(scenes); }
 * 
 * }); }
 * 
 * public void playVideo() { if (player != null) { if
 * (player.getStatus().equals(Status.PLAYING)) { player.stop(); } player.play();
 * } }
 * 
 * public void pauseVideo() { if (player != null) { player.pause(); } }
 * 
 * public void stopVideo() { if (player != null) { player.stop(); } }
 * 
 * }
 */