package view.gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import model.player.PlayerView;
import network.ClientContext;

class ScoreBoardScene extends Scene {

    private GUIViewJavaFX view;
    private String yourPlace = "";
    private String leaderbordNames = "";
    private String leaderbordScores = "";
    private Color color = Color.WHITE;

    /**
     * Create the Scene containing the leaderboard that will be displayed at the end of the game.
     * It contains the list of player sorted by their points, their points, and the placement in that game
     * @param hBox the main pane of the scene
     * @param view the GUI instace
     * @param width the width of the window
     * @param height the height of the window
     */
    ScoreBoardScene(HBox hBox, GUIViewJavaFX view, double width, double height) {
        super(hBox);
        this.view = view;
        String path = "/images/leaderboard.png";
        String url = getClass().getResource(path).toExternalForm();
        Image backgroundImange = new Image(url);
        hBox.setBackground(new Background(new BackgroundImage(backgroundImange, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));

        elaboratePlace();
        elaborateColor();

        double widthRatio = 0.85f;
        double offsetRatio = 0.03f;
        double firstRowRatio = 0.2f;
        double secondRowRatio = 0.6f;
        double thirdRowRatio = 0.041f;
        double fourthRowRatio = 0.1f;

        Button emptyLine = new Button("Empty");
        emptyLine.setMinSize(width * offsetRatio, height);
        emptyLine.setMaxSize(width * offsetRatio, height);
        emptyLine.setOpacity(0);
        emptyLine.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        hBox.getChildren().add(emptyLine);

        VBox vBox = new VBox();

        Button firstLabel = new Button("FIRST LABEL");
        firstLabel.setMinSize(width * widthRatio, height * firstRowRatio);
        firstLabel.setMaxSize(width * widthRatio, height * firstRowRatio);
        firstLabel.setOpacity(0);
        firstLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        vBox.getChildren().add(firstLabel);

        double scoreRatio = 0.65f;
        HBox scoreHBox = new HBox();
        Button secondLabel = new Button(leaderbordNames);
        secondLabel.setMinSize(width*widthRatio*scoreRatio, height * secondRowRatio);
        secondLabel.setMaxSize(width*widthRatio*scoreRatio, height * secondRowRatio);
        secondLabel.setDisable(true);
        secondLabel.setTextFill(Color.WHITE);
        secondLabel.setStyle("-fx-background-color: transparent; -fx-font: 28px Algerian;");
        secondLabel.setAlignment(Pos.TOP_LEFT);
        scoreHBox.getChildren().add(secondLabel);

        Button scoreLabel = new Button(leaderbordScores);
        scoreLabel.setMinSize(width*widthRatio*(1-scoreRatio), height * secondRowRatio);
        scoreLabel.setMaxSize(width*widthRatio*(1-scoreRatio), height * secondRowRatio);
        scoreLabel.setDisable(true);
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setStyle("-fx-background-color: transparent; -fx-font: 28px Algerian; -fx-text-alignment: right");
        scoreLabel.setAlignment(Pos.TOP_RIGHT);
        scoreHBox.getChildren().add(scoreLabel);

        vBox.getChildren().add(scoreHBox);

        Button thirdLabel = new Button("THIRD LABEL");
        thirdLabel.setMinSize(width*widthRatio, height * thirdRowRatio);
        thirdLabel.setMaxSize(width*widthRatio, height * thirdRowRatio);
        thirdLabel.setOpacity(0);
        thirdLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        vBox.getChildren().add(thirdLabel);

        Button fourthLabel = new Button(yourPlace);
        fourthLabel.setMinSize(width*widthRatio, height * fourthRowRatio);
        fourthLabel.setMaxSize(width*widthRatio, height * fourthRowRatio);
        fourthLabel.setTextFill(color);
        fourthLabel.setStyle("-fx-background-color: transparent; -fx-font: 28px Algerian;");
        vBox.getChildren().add(fourthLabel);

        hBox.getChildren().add(vBox);

    }

    /**
     * Method to set the right color of the current GUI player
     */
    private void elaborateColor() {
        switch (ClientContext.get().getCurrentPlayer().getPlayerColor()) {
            case GREEN:
                color = Color.GREEN;
                break;
            case PURPLE:
                color = Color.PURPLE;
                break;
            case YELLOW:
                color = Color.YELLOW;
                break;
            case BLUE:
                color = Color.BLUE;
                break;
            default:
                color = Color.GREY;
        }
    }

    /**
     * Method that using informations of score rank and points create the text to print in the final leaderboard.
     */
    private void elaboratePlace() {
        int i = 1;
        StringBuilder namesStringBuilder = new StringBuilder();
        StringBuilder scoresStringBuilder = new StringBuilder();
        for (PlayerView p : view.getEndRank()) {
            switch (i) {
                case 1:
                    namesStringBuilder.append("1. ");
                    namesStringBuilder.append(p.getPlayerID());
                    namesStringBuilder.append(System.getProperty("line.separator"));
                    namesStringBuilder.append(System.getProperty("line.separator"));

                    scoresStringBuilder.append(view.getPointsList().get(i - 1));
                    scoresStringBuilder.append(" points");
                    scoresStringBuilder.append(System.getProperty("line.separator"));
                    scoresStringBuilder.append(System.getProperty("line.separator"));
                    if (p.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
                        this.yourPlace = "1st! YOU WIN!";
                        view.playAudio("/sounds/victory.wav");
                    }
                    break;
                case 2:
                    namesStringBuilder.append("2. ");
                    namesStringBuilder.append(p.getPlayerID());
                    namesStringBuilder.append(System.getProperty("line.separator"));
                    namesStringBuilder.append(System.getProperty("line.separator"));

                    scoresStringBuilder.append(view.getPointsList().get(i-1));
                    scoresStringBuilder.append(" points");
                    scoresStringBuilder.append(System.getProperty("line.separator"));
                    scoresStringBuilder.append(System.getProperty("line.separator"));
                    if (p.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
                        this.yourPlace = "2nd!";
                        view.playAudio("/sounds/fail.wav");
                    }
                    break;
                case 3:
                    namesStringBuilder.append("3. ");
                    namesStringBuilder.append(p.getPlayerID());
                    namesStringBuilder.append(System.getProperty("line.separator"));
                    namesStringBuilder.append(System.getProperty("line.separator"));

                    scoresStringBuilder.append(view.getPointsList().get(i-1));
                    scoresStringBuilder.append(" points");
                    scoresStringBuilder.append(System.getProperty("line.separator"));
                    scoresStringBuilder.append(System.getProperty("line.separator"));
                    if (p.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
                        this.yourPlace = "3rd";
                        view.playAudio("/sounds/fail.wav");
                    }
                    break;
                case 4:
                    namesStringBuilder.append("4. ");
                    namesStringBuilder.append(p.getPlayerID());
                    namesStringBuilder.append(System.getProperty("line.separator"));
                    namesStringBuilder.append(System.getProperty("line.separator"));

                    scoresStringBuilder.append(view.getPointsList().get(i-1));
                    scoresStringBuilder.append(" points");
                    scoresStringBuilder.append(System.getProperty("line.separator"));
                    scoresStringBuilder.append(System.getProperty("line.separator"));
                    if (p.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
                        this.yourPlace = "4th";
                        view.playAudio("/sounds/fail.wav");
                    }
                    break;
                case 5:
                    namesStringBuilder.append("5. ");
                    namesStringBuilder.append(p.getPlayerID());

                    scoresStringBuilder.append(view.getPointsList().get(i-1));
                    scoresStringBuilder.append(" points");
                    if (p.getPlayerID().equals(ClientContext.get().getCurrentPlayer().getPlayerID())) {
                        this.yourPlace = "5th";
                        view.playAudio("/sounds/fail.wav");
                    }
                    break;
                default:
                    break;
            }
            i++;
        }
        leaderbordNames = namesStringBuilder.toString();
        leaderbordScores = scoresStringBuilder.toString();
    }
}
