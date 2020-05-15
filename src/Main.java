import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;


import java.util.ArrayList;



public class Main extends Application {

    private int screenWidth = (int) Screen.getPrimary().getBounds().getWidth();
    private int screenHeight = (int) Screen.getPrimary().getBounds().getHeight();
    private Stage window;
    private VBox root;
    private HBox topMenu;
    private GridPane boardPane;
    private ArrayList<ArrayList<Integer>> board = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> futureBoard = new ArrayList<>();
    private double decorationWidth;
    private double decorationHeight;
    private Scene scene;
    private boolean brushMode = false, eraserMode = false;
    private boolean isRunning = false;
    private int gameSpeed = 1000;
    private int rows = 23, cols = 48;

    private void invertCell(Pane cell){
        int row = GridPane.getRowIndex(cell);
        int col = GridPane.getColumnIndex(cell);
        int life = board.get(row).get(col);
        board.get(row).set(col, (life == 0) ? 1 : 0);
        futureBoard.get(row).set(col, (life == 0) ? 1 : 0);
        boolean isDead = life != 0;
        if (isDead) {
            cell.getStyleClass().remove("cell-alive");
            cell.getStyleClass().add("cell-dead");
        } else {
            cell.getStyleClass().remove("cell-dead");
            cell.getStyleClass().add("cell-alive");
        }
    }

    private void brushMode(Pane cell){
        int row = GridPane.getRowIndex(cell);
        int col = GridPane.getColumnIndex(cell);
        int life = board.get(row).get(col);
        board.get(row).set(col, 1);
        futureBoard.get(row).set(col, 1);
        boolean isDead = life == 0;
        if (isDead) {
            cell.getStyleClass().remove("cell-dead");
            cell.getStyleClass().add("cell-alive");
        }
    }

    private void eraserMode(Pane cell){
        int row = GridPane.getRowIndex(cell);
        int col = GridPane.getColumnIndex(cell);
        int life = board.get(row).get(col);
        board.get(row).set(col, 0);
        futureBoard.get(row).set(col, 0);
        boolean isAlive = life == 1;
        if (isAlive) {
            cell.getStyleClass().remove("cell-alive");
            cell.getStyleClass().add("cell-dead");
        }
    }

    private Pane createCell() {
        Pane cell = new Pane();
        double size = 40; //48
        cell.setMaxSize(size, size);
        cell.setMinSize(size, size);
        cell.getStyleClass().add("cell-dead");
        cell.setOnMouseClicked(e -> invertCell(cell));

        cell.setOnMouseEntered(e -> {
            if (brushMode)
                brushMode(cell);
            else if (eraserMode)
                eraserMode(cell);
        });

        return cell;
    }

    private void paintSquares(int rows, int cols) {
        for (int i = 0; i < rows; i++) {
            board.add(new ArrayList<>());
            futureBoard.add(new ArrayList<>());
            for (int j = 0; j < cols; j++) {
                board.get(i).add(0);
                futureBoard.get(i).add(0);
                Pane cell = createCell();
                GridPane.setConstraints(cell, j, i);
                boardPane.getChildren().add(cell);
            }
        }
    }

    private void clearBoard(){
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                boolean isAlive = board.get(i).get(j) == 1;
                if (isAlive)
                    killCell(i, j);
                board.get(i).set(j, 0);
                futureBoard.get(i).set(j, 0);
            }
        }
    }

    private void updateCorners(){
        int rowLim = board.size() - 1;
        int colLim = board.get(0).size() - 1;
        int cornerSum = board.get(0).get(1) + board.get(1).get(0) + board.get(1).get(1);
        if (board.get(0).get(0) == 0 && cornerSum == 3)
            bringToLife(0, 0);
        else if ((board.get(0).get(0) == 1) && (cornerSum < 2 || cornerSum > 3))
            killCell(0, 0);

        cornerSum = board.get(rowLim - 1).get(0) + board.get(rowLim).get(1) + board.get(rowLim - 1).get(1);
        if (board.get(rowLim).get(0) == 0 && cornerSum == 3)
            bringToLife(rowLim, 0);
        else if ((board.get(rowLim).get(0) == 1) && (cornerSum < 2 || cornerSum > 3))
            killCell(rowLim, 0);

        cornerSum = board.get(0).get(colLim - 1) + board.get(1).get(colLim) + board.get(1).get(colLim - 1);
        if (board.get(0).get(colLim) == 0 && cornerSum == 3)
            bringToLife(0, colLim);
        else if ((board.get(0).get(colLim) == 1) && (cornerSum < 2 || cornerSum > 3))
            killCell(0, colLim);

        cornerSum = board.get(rowLim - 1).get(colLim) + board.get(rowLim - 1).get(colLim - 1) + board.get(rowLim).get(colLim - 1);
        if (board.get(rowLim).get(colLim) == 0 && cornerSum == 3)
            bringToLife(rowLim, colLim);
        else if ((board.get(rowLim).get(colLim) == 1) && (cornerSum < 2 || cornerSum > 3))
            killCell(rowLim, colLim);
    }

    private void updateTop(){
        for (int i = 1; i < board.get(0).size() - 1; i++) {
            int surroundSum = board.get(0).get(i - 1) + board.get(0).get(i + 1) +
                    board.get(1).get(i - 1) + board.get(1).get(i) + board.get(1).get(i + 1);
            int life = board.get(0).get(i);
            boolean isDead = life == 0;
            if (isDead && surroundSum == 3)
                bringToLife(0, i);
            else if (!isDead && (surroundSum < 2 || surroundSum > 3))
                killCell(0, i);
        }
    }

    private void updateLeft(){
        for (int i = 1; i < board.size() - 1; i++) {
            int surroundSum = board.get(i - 1).get(0) + board.get(i + 1).get(0) +
                    board.get(i - 1).get(1) + board.get(i).get(1) + board.get(i + 1).get(1);
            int life = board.get(i).get(0);
            boolean isDead = life == 0;
            if (isDead && surroundSum == 3)
                bringToLife(i, 0);
            else if (!isDead && (surroundSum < 2 || surroundSum > 3))
                killCell(i, 0);
        }
    }

    private void updateRight(){
        int colLim = board.get(0).size() - 1;
        for (int i = 1; i < board.size() - 1; i++) {
            int surroundSum = board.get(i - 1).get(colLim) + board.get(i + 1).get(colLim) +
                    board.get(i - 1).get(colLim - 1) + board.get(i).get(colLim - 1) + board.get(i + 1).get(colLim - 1);
            int life = board.get(i).get(colLim);
            boolean isDead = life == 0;
            if (isDead && surroundSum == 3)
                bringToLife(i, colLim);
            else if (!isDead && (surroundSum < 2 || surroundSum > 3))
                killCell(i, colLim);
        }
    }

    private void updateBot(){
        int rowLim = board.size() - 1;
        for (int i = 1; i <  board.get(0).size() - 1; i++) {
            int surroundSum = board.get(rowLim).get(i - 1) + board.get(rowLim).get(i + 1) +
                    board.get(rowLim - 1).get(i - 1) + board.get(rowLim - 1).get(i) + board.get(rowLim - 1).get(i + 1);
            int life = board.get(rowLim).get(i);
            boolean isDead = life == 0;
            if (isDead && surroundSum == 3)
                bringToLife(rowLim, i);
            else if (!isDead && (surroundSum < 2 || surroundSum > 3)){
                killCell(rowLim, i);
            }

        }
    }

    private void nextFrame() {
        updateCorners();
        updateTop();
        updateLeft();
        updateRight();
        updateBot();

        for (int i = 1; i < board.size() - 1; i++) {
            for (int j = 1; j < board.get(0).size() - 1; j++) {
                int surroundSum = (board.get(i - 1).get(j - 1) + board.get(i - 1).get(j) + board.get(i - 1).get(j + 1) +
                        board.get(i).get(j - 1) + board.get(i).get(j + 1) +
                        board.get(i + 1).get(j - 1) + board.get(i + 1).get(j) + board.get(i + 1).get(j + 1)
                );
                int life = board.get(i).get(j);
                boolean isDead = life == 0;
                if (isDead && surroundSum == 3)
                    bringToLife(i, j);
                else if (!isDead && (surroundSum < 2 || surroundSum > 3))
                    killCell(i, j);
            }
        }

        for (int i = 0; i < board.size(); i++) {
            for (int j = 0; j < board.get(0).size(); j++) {
                board.get(i).set(j, futureBoard.get(i).get(j));
            }
        }
    }

    private void bringToLife(int row, int col) {
        Pane cell = (Pane) boardPane.getChildren().get((row * board.get(0).size()) + col);
        futureBoard.get(row).set(col, 1);
        cell.getStyleClass().remove("cell-dead");
        cell.getStyleClass().add("cell-alive");
    }

    private void killCell(int row, int col) {
        Pane cell = (Pane) boardPane.getChildren().get((row * board.get(0).size()) + col);
        futureBoard.get(row).set(col, 0);
        cell.getStyleClass().remove("cell-alive");
        cell.getStyleClass().add("cell-dead");
    }

    private void runGameAt() {
        try {
            Thread object = new Thread(new gameRunEnv());;
            object.start();
        } catch (Exception e){
            System.out.println("F");
        }
    }

    private void topMenuInterface() {
        topMenu = new HBox();
        topMenu.getStyleClass().add("top-menu");
        topMenu.setSpacing(100);

        Button nextFrameButton = new Button("Next frame");
        nextFrameButton.setPrefSize(200, 50);
        nextFrameButton.setOnAction(e -> nextFrame());

        Button clearBoard = new Button("Clear board");
        clearBoard.setPrefSize(200, 50);
        clearBoard.setOnAction(e -> clearBoard());

        Button runButton = new Button("Run Game");
        runButton.setPrefSize(200, 50);
        runButton.setOnAction(e -> {
            isRunning = !isRunning;
            if (isRunning){
                runButton.setText("Stop Game");
                runButton.getStyleClass().remove("button-unselected");
                runButton.getStyleClass().add("button-unselected");
            } else {
                runButton.getStyleClass().remove("button-unselected");
                runButton.setText("Run Game");
            }
            runGameAt();
        });

        int[] speeds = new int[]{1, 5, 15, 25, 50, 100, 250, 500, 750, 1000};
        ComboBox<Integer> gameSpeedComboBox = new ComboBox<>();
        gameSpeedComboBox.setPrefSize(200, 50);
        for (int speed : speeds) {
            gameSpeedComboBox.getItems().add(speed);
        }
        gameSpeedComboBox.setOnAction(e -> gameSpeed = gameSpeedComboBox.getValue());

        topMenu.getChildren().addAll(nextFrameButton, clearBoard, runButton, gameSpeedComboBox);
        topMenu.setAlignment(Pos.CENTER);
    }

    @Override
    public void start(Stage stage) {
        window = stage;
        root = new VBox();
        root.getStyleClass().add("root-pane");


        topMenuInterface();

        boardPane = new GridPane();
        boardPane.getStyleClass().add("GridPane");

        // Responsive Design
        int sceneWidth = 0;
        int sceneHeight = 0;
        if (screenWidth <= 800 && screenHeight <= 600) {
            sceneWidth = 640;
            sceneHeight = 360;
        } else if (screenWidth <= 1280 && screenHeight <= 768) {
            sceneWidth = 1120;
            sceneHeight = 630;
        } else if (screenWidth <= 1920 && screenHeight <= 1080) {
            sceneWidth = 1600;
            sceneHeight = 900;
        } else if (screenWidth > 1920 && screenHeight > 1080) {
            sceneWidth = 1922;
            sceneHeight = 1080;
        }

        root.getChildren().addAll(topMenu, boardPane);

        // Scene
        scene = new Scene(root, sceneWidth, sceneHeight, Color.TRANSPARENT);

        scene.getStylesheets().add("Style.css");

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                nextFrame();
            else if (event.getCode() == KeyCode.CONTROL)
                brushMode = true;
            else if (event.getCode() == KeyCode.SHIFT)
                eraserMode = true;
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SHIFT)
                eraserMode = false;

            if (event.getCode() == KeyCode.CONTROL)
                brushMode = false;
        });


        window.setScene(scene);
        window.setResizable(true);
        window.show();
        paintSquares(rows, cols); //19, 40 23, 48
        //root.heightProperty().bind(scene.widthProperty());
        decorationWidth = window.getWidth() - sceneWidth;
        decorationHeight = window.getHeight() - sceneHeight;

        //scene.widthProperty().addListener((obs, oldVal, newVal) -> scene.w(scene.getWidth()));
        root.setMinWidth(window.getWidth() - decorationWidth);
        root.setMinHeight((window.getHeight() - decorationHeight));
        topMenu.setMinWidth(window.getWidth() - decorationWidth);
        topMenu.setMinHeight((window.getHeight() - decorationHeight) * 0.153);
        boardPane.setMinWidth(window.getWidth() - decorationWidth);
        boardPane.setMinHeight((window.getHeight() - decorationHeight) * 0.747);


    }

    public void resizeScene(double width, double height) {
        window.setWidth(width + decorationWidth);
        window.setHeight(height + decorationHeight);
    }

    public static void main(String[] args) {
        launch(args);
    }

    class gameRunEnv implements Runnable
    {
        public void run()
        {
            try
            {
                while (isRunning) {
                    nextFrame();
                    Thread.sleep(gameSpeed);
                }
            }
            catch (Exception e)
            {
                System.out.println ("Exception is caught");
            }
        }
    }
}


