package view.gui;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.List;

class GUILogger extends ScrollPane {

    private final GUIViewJavaFX view;
    private List<String> stringList;
    private TextArea textArea;

    private int startLineToShow = 0, endLineToShow = 0;

    /**
     * This Scroll Pane is used as logger to display information during the game
     * @param width the width of the pane
     * @param height the height of the pane
     * @param viewJavaFX the GUI instance
     */
    GUILogger(double width, double height, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        this.setPrefSize(width, height);
        this.setMaxSize(width, height);
        this.setMinSize(width, height);
        textArea = new TextArea();
        stringList = new ArrayList<String>();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setOpacity(0.4f);
        textArea.setId("Adrenaline Logger");
        textArea.setPrefSize(width, height);
        textArea.setMaxSize(width,height);
        setContent(textArea);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    }

    void addText(String string){
        stringList.add(string+ "\n");
        textArea.appendText(string+ "\n");
        endLineToShow = stringList.size();
    }

}
