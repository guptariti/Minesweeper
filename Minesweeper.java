
import java.util.ArrayList;
import java.util.Arrays;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;

//global constants
interface IConstants {
  ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(Color.BLUE, Color.GREEN, Color.RED,
      Color.MAGENTA, Color.ORANGE, Color.DARK_GRAY, Color.LIGHT_GRAY, Color.PINK));

  Color tileColor = Color.CYAN;
  Color unlockedTileColor = Color.GRAY;
  Color explodedMineTile = Color.RED;

  int tileSize = 30; // FEEL FREE TO CHANGE
}

//to represent a game piece (tile/mine)
abstract class AGamePiece {

  ArrayList<AGamePiece> neighbors;
  Color color;
  boolean uncovered = false;
  boolean marked = false;

  AGamePiece() {
    this.neighbors = new ArrayList<AGamePiece>();
    this.color = Color.CYAN;
  }

  AGamePiece(ArrayList<AGamePiece> neighbors) {
    this.neighbors = neighbors;
    this.color = Color.CYAN;
  }

  // EFFECT: Changes this AGamePiece's list of neighbors by adding AGamePiece's to
  // the list
  // adds all the neighboring tiles in a grid to this AGamePiece's list of
  // neighbors
  void addNeighbors(ArrayList<ArrayList<AGamePiece>> grid, int row, int col) {
    int width = grid.get(0).size();
    int height = grid.size();

    for (int i = -1; i < 2; i++) {
      for (int j = -1; j < 2; j++) {
        if (i + row >= 0 && i + row < height && j + col >= 0 && j + col < width
            && (i != j || i + j != 0)) {
          this.neighbors.add(grid.get(row + i).get(col + j));
        }
      }
    }
  }

  // counts the number of mines present in this AGamePiece's neighbors
  public int countMines() {
    int sum = 0;
    for (int i = 0; i < this.neighbors.size(); i++) {
      sum += this.neighbors.get(i).countMinesHelp();
    }
    return sum;
  }

  // counts the number of mines present in this AGamePiece's neighbors
  abstract int countMinesHelp();

  // EFFECT: changes color to its uncovered color, gray. Changes the uncovered
  // value to true.
  // EFFECT: Uncovers all tiles in this AGamePiece's list of neighbors
  // Uncovers this AGamePiece to show what it value is
  abstract void uncover();

  // determines if the gamePiece is a clicked mine
  abstract boolean clickedMine();

  // updates specific properties of the game piece when the game is over
  abstract void gameOverStatus();

  // determines whether the game piece is a covered tile
  abstract boolean coveredTile();

  // sets the property of nearby mines for the game piece
  abstract void setMines();

  // draws a square image of this AGamePiece
  abstract WorldImage drawTile();
}

//to represent a tile
class Tile extends AGamePiece {
  int nearMines;

  Tile() {
    super();
  }

  Tile(ArrayList<AGamePiece> neighbors) {
    super(neighbors);
  }

  // this is not a mine, so output is 0
  public int countMinesHelp() {
    return 0;
  }

  // a tile is not a clicked mine
  public boolean clickedMine() {
    return false;
  }

  // nothing to update when the game is over
  public void gameOverStatus() {
    return;
  }

  // is this tile covered?
  public boolean coveredTile() {
    return !this.uncovered;
  }

  // Performs the flood fill effect if needed
  // Updates uncovered properties of the tile
  // EFFECT: Changes color and uncovered property of a tile
  public void uncover() {
    if (!this.marked) {
      if (this.nearMines == 0) {
        this.color = IConstants.unlockedTileColor;
        this.uncovered = true;
        for (AGamePiece tile : this.neighbors) {
          if (tile.coveredTile()) {
            tile.uncover();
          }
        }
      }
      else {
        this.uncovered = true;
        this.color = IConstants.unlockedTileColor;
      }

    }
  }

  // sets the nearby mines of this tile
  public void setMines() {
    this.nearMines = this.countMines();
  }

  // draws this tile into a square image
  public WorldImage drawTile() {
    WorldImage tile = new RectangleImage(IConstants.tileSize, IConstants.tileSize,
        OutlineMode.SOLID, this.color);
    WorldImage tileBorder = new RectangleImage(IConstants.tileSize, IConstants.tileSize,
        OutlineMode.OUTLINE, Color.BLACK);
    if (this.marked && !this.uncovered) {
      WorldImage marker = new EquilateralTriangleImage(IConstants.tileSize / 2, OutlineMode.SOLID,
          Color.YELLOW);
      WorldImage temp = new OverlayImage(tileBorder, tile);
      return new OverlayImage(marker, temp);
    }
    if (this.uncovered && this.nearMines > 0) {
      WorldImage nbr = new TextImage(Integer.toString(this.countMines()), IConstants.tileSize / 2,
          IConstants.colors.get(this.countMines() - 1));
      WorldImage temp = new OverlayImage(tileBorder, tile);
      return new OverlayImage(nbr, temp);
    }
    else {
      return new OverlayImage(tileBorder, tile);
    }

  }

}

//to represent a mine
class Mine extends AGamePiece {

  Mine() {
    super();
  }

  Mine(ArrayList<AGamePiece> neighbors) {
    super(neighbors);
  }

  // this is a mine so outputs 1
  public int countMinesHelp() {
    return 1;
  }

  // is this a clicked mine?
  public boolean clickedMine() {
    return this.uncovered;
  }

  // updates the mine properties when the game is over
  // EFFECT: Changes the uncovered and color properties of the mine
  public void gameOverStatus() {
    this.uncovered = true;
    this.color = IConstants.explodedMineTile;

  }

  // this is not a tile
  public boolean coveredTile() {
    return false;
  }

  // Changes the status of a mine if it is uncovered and not marked
  // EFFECT: Changes the uncovered and color properties of the mine
  public void uncover() {
    if (!this.marked) {
      this.gameOverStatus();
    }
  }

  // mines don't need to keep track of nearby mines
  public void setMines() {
    return;
  }

  // draws this mine as a square image
  public WorldImage drawTile() {
    WorldImage tile = new RectangleImage(IConstants.tileSize, IConstants.tileSize,
        OutlineMode.SOLID, this.color);
    WorldImage tileBorder = new RectangleImage(IConstants.tileSize, IConstants.tileSize,
        OutlineMode.OUTLINE, Color.BLACK);
    if (this.clickedMine()) {
      WorldImage bomb = new CircleImage(IConstants.tileSize / 4, OutlineMode.SOLID, Color.BLACK);
      WorldImage temp = new OverlayImage(tileBorder, tile);
      return new OverlayImage(bomb, temp);
    }
    if (this.marked && !this.uncovered) {
      WorldImage marker = new EquilateralTriangleImage(IConstants.tileSize / 2, OutlineMode.SOLID,
          Color.YELLOW);
      WorldImage temp = new OverlayImage(tileBorder, tile);
      return new OverlayImage(marker, temp);
    }
    else {
      return new OverlayImage(tileBorder, tile);
    }
  }

}

//to represent the minesweeper game
class Minesweeper extends World {

  ArrayList<ArrayList<AGamePiece>> grid = new ArrayList<ArrayList<AGamePiece>>();
  int rows;
  int cols;
  WorldScene scene;
  Random rand;
  boolean gameOver = false;
  int numMines;
  boolean winner;

  Minesweeper(int rows, int cols, int numMines) {
    this.rows = rows;
    this.cols = cols;
    this.scene = new WorldScene(IConstants.tileSize * cols, IConstants.tileSize * rows);
    this.numMines = numMines;
    this.rand = new Random();
    this.createGrid();
    this.updateNeighbors();
    this.winner = false;
    this.updateTiles();
  }

  Minesweeper(int rows, int cols, int numMines, Random r) {
    this.rows = rows;
    this.cols = cols;
    this.scene = new WorldScene(IConstants.tileSize * cols, IConstants.tileSize * rows);
    this.numMines = numMines;
    this.rand = r;
    this.createGrid();
    this.updateNeighbors();
    this.winner = false;
    this.updateTiles();

  }

  Minesweeper(int rows, int cols, int numMines, ArrayList<ArrayList<AGamePiece>> grid) {
    this.rows = rows;
    this.cols = cols;
    this.rand = new Random();
    this.scene = new WorldScene(IConstants.tileSize * cols, IConstants.tileSize * rows);
    this.numMines = numMines;
    this.grid = grid;
    this.updateNeighbors();
    this.winner = false;
    this.updateTiles();
  }

  // updates all of the tiles in the grid to keep track of nearby mines
  // EFFECT: Changes the nearMines property of a tile
  void updateTiles() {
    for (int i = 0; i < this.rows; i++) {
      for (int j = 0; j < this.cols; j++) {
        grid.get(i).get(j).setMines();
      }
    }

  }

  // EFFECT: Grid changes to become an X by X grid containing rows and columns
  // with tiles and mines
  // creates a grid of randomly places mines and tiles
  void createGrid() {
    ArrayList<AGamePiece> row;
    ArrayList<Posn> posn = new ArrayList<Posn>();
    int counter = 0;
    for (int i = 0; i < this.rows; i++) {
      for (int j = 0; j < this.cols; j++) {
        posn.add(new Posn(i, j));
      }
    }

    ArrayList<Posn> minePositions = new ArrayList<Posn>();
    while (counter < this.numMines) {
      Posn p = posn.get(rand.nextInt(posn.size()));
      int idx = posn.indexOf(p);
      minePositions.add(p);
      posn.remove(idx);
      counter++;
    }

    for (int i = 0; i < this.rows; i++) {
      row = new ArrayList<AGamePiece>();
      for (int j = 0; j < this.cols; j++) {
        if (minePositions.contains(new Posn(i, j))) {
          row.add(new Mine());
        }
        else {
          row.add(new Tile());
        }
      }
      grid.add(row);
    }
  }

  // EFFECT: Changes each AGamePiece's list of neighbors to add all of the
  // tiles/mines around
  // EFFECT: them in the grid
  // Updates each AGamePiece's list of neighbors
  void updateNeighbors() {
    for (int i = 0; i < this.rows; i++) {
      for (int j = 0; j < this.cols; j++) {
        grid.get(i).get(j).addNeighbors(grid, i, j);
      }
    }
  }

  // draws the Minesweeper game onto a board of inputted dimension
  public WorldScene makeScene() {
    for (int i = 0; i < this.rows; i++) {
      for (int j = 0; j < this.cols; j++) {
        if (this.gameOver) {
          grid.get(i).get(j).gameOverStatus();
        }
        this.scene.placeImageXY(grid.get(i).get(j).drawTile(),
            j * IConstants.tileSize + IConstants.tileSize / 2,
            i * IConstants.tileSize + IConstants.tileSize / 2);
      }
    }
    if (this.gameOver || this.winner) {
      String gameMessage = "";
      if (this.winner) {
        gameMessage = "YOU WIN";
      }
      else {
        gameMessage = "GAME OVER";
      }
      WorldImage back = new RectangleImage(this.cols * IConstants.tileSize / 2,
          this.rows * IConstants.tileSize / 5, OutlineMode.SOLID, Color.BLACK);
      WorldImage text = new TextImage(gameMessage, IConstants.tileSize / 1.5, Color.MAGENTA);
      WorldImage gameOver = new OverlayImage(text, back);
      this.scene.placeImageXY(gameOver, this.scene.width / 2, this.scene.height / 2);
    }
    return this.scene;
  }

  // Checks whether the game has been won or not
  // EFFECT: Changes the game world property winner based off of the status
  public void onTick() {
    if (this.gameOver) {
      this.endOfWorld("The game has stopped!");
    }
    this.winner = true;
    for (int i = 0; i < this.rows; i++) {
      for (int j = 0; j < this.cols; j++) {
        if (grid.get(i).get(j).coveredTile()) {
          this.winner = false;
        }
      }
    }
    if (this.winner) {
      this.endOfWorld("The game has stopped!");
    }

  }

  // Left Click: Uncovers the clicked on tile
  // Right Click: Marks the clicked tile with a flag
  public void onMouseReleased(Posn p, String button) {
    AGamePiece clicked = this.grid.get(p.y / IConstants.tileSize).get(p.x / IConstants.tileSize);
    if (button.equals("RightButton")) {
      clicked.marked = !clicked.marked;
    }
    else {
      clicked.uncovered = true;
      if (clicked.clickedMine()) {
        this.gameOver = true;

      }
      clicked.uncover();
    }
  }

}

//tests and examples for Minesweeper
class Examples {
  ArrayList<AGamePiece> mt;
  ArrayList<AGamePiece> row1;
  ArrayList<AGamePiece> row2;
  ArrayList<AGamePiece> row3;
  ArrayList<ArrayList<AGamePiece>> grid3x4;
  AGamePiece middle;
  AGamePiece middleTop;
  AGamePiece middleBottom;
  AGamePiece bottomRightCorner;
  AGamePiece tile1;
  AGamePiece tile2;
  AGamePiece mine1;
  AGamePiece mine2;

  // initial data
  void initData() {
    tile1 = new Tile();
    tile2 = new Tile();
    mine1 = new Mine();
    mine2 = new Mine();
    this.mt = new ArrayList<AGamePiece>();
    this.row1 = new ArrayList<AGamePiece>(
        Arrays.asList(new Tile(), new Mine(), new Tile(), new Tile()));
    this.row2 = new ArrayList<AGamePiece>(
        Arrays.asList(new Tile(), new Tile(), new Tile(), new Tile()));
    this.row3 = new ArrayList<AGamePiece>(
        Arrays.asList(new Mine(), new Tile(), new Tile(), new Tile()));
    this.grid3x4 = new ArrayList<ArrayList<AGamePiece>>(Arrays.asList(row1, row2, row3));
    this.middle = this.grid3x4.get(1).get(1);
    this.middleTop = this.row1.get(1);
    this.middleBottom = this.row3.get(1);
    this.bottomRightCorner = this.row3.get(2);
  }

  // tests for addNeighbors
  void testAddNeighbors(Tester t) {
    this.initData();
    t.checkExpect(middle.neighbors, this.mt);
    t.checkExpect(middleTop.neighbors, this.mt);

    middle.addNeighbors(this.grid3x4, 1, 1);
    t.checkExpect(middle.neighbors.size(), 8);
    t.checkExpect(middle.neighbors, new ArrayList<AGamePiece>(Arrays.asList(new Tile(), new Mine(),
        new Tile(), new Tile(), new Tile(), new Mine(), new Tile(), new Tile())));
    t.checkExpect(middleTop.neighbors, this.mt);

    middleTop.addNeighbors(this.grid3x4, 0, 1);
    t.checkExpect(middleTop.neighbors.size(), 5);
    t.checkExpect(middleTop.neighbors, new ArrayList<AGamePiece>(
        Arrays.asList(new Tile(), new Tile(), new Tile(), middle, new Tile())));

    bottomRightCorner.addNeighbors(this.grid3x4, 2, 3);
    t.checkExpect(bottomRightCorner.neighbors.size(), 3);
  }

  // testing the setMines method
  void testSetMines(Tester t) {
    this.initData();
    this.middle.addNeighbors(this.grid3x4, 1, 1);
    this.middle.setMines();
    t.checkExpect(((Tile) middle).nearMines, 2);
    AGamePiece middleTopCopy = this.row1.get(1);
    this.middleTop.addNeighbors(this.grid3x4, 0, 1);
    middleTopCopy.addNeighbors(this.grid3x4, 0, 1);
    this.middleTop.setMines();
    t.checkExpect(this.middleTop, middleTopCopy);
    this.middleBottom.addNeighbors(this.grid3x4, 2, 1);
    this.middleBottom.setMines();
    t.checkExpect(((Tile) (this.middleBottom)).nearMines, 1);
    this.bottomRightCorner.addNeighbors(this.grid3x4, 2, 2);
    this.bottomRightCorner.setMines();
    t.checkExpect(((Tile) (this.bottomRightCorner)).nearMines, 0);
  }

  // tests for countMine/countMineHelp
  void testCountMines(Tester t) {
    this.initData();
    middle.addNeighbors(this.grid3x4, 1, 1);
    middleTop.addNeighbors(this.grid3x4, 0, 1);
    middleBottom.addNeighbors(this.grid3x4, 2, 1);
    t.checkExpect(this.middleTop.countMinesHelp(), 1);
    t.checkExpect(this.middle.countMinesHelp(), 0);
    t.checkExpect(this.middleBottom.countMinesHelp(), 0);
    t.checkExpect(this.middleTop.countMines(), 0);
    t.checkExpect(this.middle.countMines(), 2);
    t.checkExpect(this.middleBottom.countMines(), 1);
  }

  // tests for drawTile
  void testDrawTile(Tester t) {
    this.initData();
    WorldImage tileCovered = new RectangleImage(IConstants.tileSize, IConstants.tileSize,
        OutlineMode.SOLID, IConstants.tileColor);
    WorldImage tileUncovered = new RectangleImage(IConstants.tileSize, IConstants.tileSize,
        OutlineMode.SOLID, IConstants.unlockedTileColor);
    WorldImage tileBorder = new RectangleImage(IConstants.tileSize, IConstants.tileSize,
        OutlineMode.OUTLINE, Color.BLACK);

    // Tile with TWO neighboring mines
    t.checkExpect(this.middle.drawTile(), new OverlayImage(tileBorder, tileCovered));
    this.middle.uncovered = true;
    this.middle.color = IConstants.unlockedTileColor;
    this.middle.addNeighbors(this.grid3x4, 1, 1);
    this.middle.setMines();
    t.checkExpect(this.middle.drawTile(),
        new OverlayImage(new TextImage("2", IConstants.tileSize / 2, Color.GREEN),
            new OverlayImage(tileBorder, tileUncovered)));

    // Tile with NO neighboring mines
    t.checkExpect(this.bottomRightCorner.drawTile(), new OverlayImage(tileBorder, tileCovered));
    this.bottomRightCorner.uncovered = true;
    this.bottomRightCorner.color = IConstants.unlockedTileColor;
    this.bottomRightCorner.addNeighbors(this.grid3x4, 2, 3);
    t.checkExpect(this.bottomRightCorner.drawTile(), new OverlayImage(tileBorder, tileUncovered));

    // MINE
    t.checkExpect(this.middleTop.drawTile(), new OverlayImage(tileBorder, tileCovered));
    this.middleTop.uncovered = true;
    this.middleTop.color = IConstants.explodedMineTile;
    this.middleTop.addNeighbors(this.grid3x4, 0, 1);
    t.checkExpect(this.middleTop.drawTile(),
        new OverlayImage(new CircleImage(IConstants.tileSize / 4, OutlineMode.SOLID, Color.BLACK),
            new OverlayImage(tileBorder, new RectangleImage(IConstants.tileSize,
                IConstants.tileSize, OutlineMode.SOLID, IConstants.explodedMineTile))));

    this.middleTop.uncovered = false;
    this.middleTop.marked = true;
    this.middleTop.color = Color.CYAN;
    t.checkExpect(this.middleTop.drawTile(),
        new OverlayImage(
            new EquilateralTriangleImage(IConstants.tileSize / 2, OutlineMode.SOLID, Color.YELLOW),
            new OverlayImage(tileBorder, new RectangleImage(IConstants.tileSize,
                IConstants.tileSize, OutlineMode.SOLID, Color.CYAN))));
  }

  // tests for uncover
  void testUncover(Tester t) {
    this.initData();

    // Tile with TWO neighboring mines
    t.checkExpect(this.middle.uncovered, false);
    t.checkExpect(this.middle.color, IConstants.tileColor);
    this.middle.uncover();
    t.checkExpect(this.middle.uncovered, true);
    t.checkExpect(this.middle.color, IConstants.unlockedTileColor);

    // MINE
    t.checkExpect(this.middleTop.uncovered, false);
    t.checkExpect(this.middleTop.color, IConstants.tileColor);
    this.middleTop.uncover();
    t.checkExpect(this.middleTop.uncovered, true);
    t.checkExpect(this.middleTop.color, IConstants.explodedMineTile);

    // Tile with NO neighboring mines (flooding)
    this.bottomRightCorner.addNeighbors(this.grid3x4, 2, 3);
    t.checkExpect(this.bottomRightCorner.uncovered, false);
    t.checkExpect(this.bottomRightCorner.color, IConstants.tileColor);
    for (int i = 0; i < this.bottomRightCorner.neighbors.size(); i++) {
      t.checkExpect(this.bottomRightCorner.neighbors.get(i).uncovered, false);
    }
    this.bottomRightCorner.uncover();
    t.checkExpect(this.bottomRightCorner.uncovered, true);
    t.checkExpect(this.bottomRightCorner.color, IConstants.unlockedTileColor);
    for (int i = 0; i < this.bottomRightCorner.neighbors.size(); i++) {
      t.checkExpect(this.bottomRightCorner.neighbors.get(i).uncovered, true);
    }
    for (int i = 0; i < this.bottomRightCorner.neighbors.size(); i++) {
      t.checkExpect(this.bottomRightCorner.neighbors.get(i).color, IConstants.unlockedTileColor);
    }

  }

  // tests for CreateGrid
  void testCreateGrid(Tester t) {
    // test with seed 4
    Random r = new Random(4);
    int rand = r.nextInt(4);
    ArrayList<Posn> posn = new ArrayList<Posn>();
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        posn.add(new Posn(i, j));
      }
    }
    Posn p = posn.get(rand);
    Minesweeper msTestGrid = new Minesweeper(2, 2, 1, new Random(4));
    msTestGrid.grid.get(p.x).get(p.y).uncovered = true;
    t.checkExpect(msTestGrid.grid.get(p.x).get(p.y).clickedMine(), true);

    // test with seed 6
    Random r1 = new Random(6);
    int rand1 = r1.nextInt(9);
    ArrayList<Posn> posn1 = new ArrayList<Posn>();
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        posn1.add(new Posn(i, j));
      }
    }
    Posn p1 = posn1.get(rand1);
    Minesweeper msTestGrid1 = new Minesweeper(3, 3, 1, new Random(6));
    msTestGrid1.grid.get(p1.x).get(p1.y).uncovered = true;
    t.checkExpect(msTestGrid1.grid.get(p1.x).get(p1.y).clickedMine(), true);
  }

  // testing updateNeighbors
  void testUpdateNeighbors(Tester t) {
    this.initData();
    Minesweeper msTestGrid1 = new Minesweeper(3, 4, 2, this.grid3x4);
    t.checkExpect(this.grid3x4.get(0).get(0).neighbors.size(), 3);
    t.checkExpect(this.grid3x4.get(0).get(1).neighbors.size(), 5);
    t.checkExpect(this.grid3x4.get(1).get(1).neighbors.size(), 8);
    t.checkExpect(this.grid3x4.get(0).get(0).neighbors.get(0),
        this.grid3x4.get(0).get(2).neighbors.get(0));
    t.checkExpect(this.grid3x4.get(2).get(0), this.grid3x4.get(2).get(1).neighbors.get(3));

  }

  // testing the updateTiles method
  void testUpdateTiles(Tester t) {
    this.initData();
    Minesweeper game1 = new Minesweeper(3, 4, 2, this.grid3x4);
    Tile fTile = (Tile) game1.grid.get(0).get(0);
    Tile fTile1 = (Tile) game1.grid.get(1).get(1);
    Mine fMine = (Mine) game1.grid.get(0).get(1);
    t.checkExpect(fTile.nearMines, 1);
    t.checkExpect(fTile1.nearMines, 2);
    // ensure that nothing changes for a mine
    t.checkExpect(fMine, this.middleTop);

  }

  // testing makeScene
  void testMakeScene(Tester t) {
    this.initData();
    WorldScene scene = new WorldScene(IConstants.tileSize * 4, IConstants.tileSize * 3);
    WorldScene scene1 = new WorldScene(IConstants.tileSize * 2, IConstants.tileSize);
    WorldScene scene2 = new WorldScene(IConstants.tileSize, IConstants.tileSize);
    Minesweeper msGrid0 = new Minesweeper(1, 2, 0);
    Minesweeper msGrid1 = new Minesweeper(1, 1, 0);
    scene1.placeImageXY(new Tile().drawTile(), IConstants.tileSize / 2, IConstants.tileSize / 2);
    scene1.placeImageXY(new Tile().drawTile(), IConstants.tileSize + IConstants.tileSize / 2,
        IConstants.tileSize / 2);
    scene2.placeImageXY(new Tile().drawTile(), IConstants.tileSize / 2, IConstants.tileSize / 2);

    Minesweeper msTestGrid1 = new Minesweeper(3, 4, 2, this.grid3x4);
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 4; j++) {
        scene.placeImageXY(msTestGrid1.grid.get(i).get(j).drawTile(),
            j * IConstants.tileSize + IConstants.tileSize / 2,
            i * IConstants.tileSize + IConstants.tileSize / 2);
      }
    }
    t.checkExpect(scene, msTestGrid1.makeScene());
    t.checkExpect(scene1, msGrid0.makeScene());
    t.checkExpect(scene2, msGrid1.makeScene());

    Minesweeper winnerGrid = new Minesweeper(1, 2, 0);
    winnerGrid.winner = true;
    WorldImage back = new RectangleImage(IConstants.tileSize / 2, IConstants.tileSize / 5,
        OutlineMode.SOLID, Color.BLACK);
    WorldImage text = new TextImage("YOU WIN", IConstants.tileSize / 1.5, Color.MAGENTA);
    WorldImage gameOver = new OverlayImage(text, back);
    winnerGrid.scene.placeImageXY(gameOver, winnerGrid.scene.width / 2,
        winnerGrid.scene.height / 2);

    t.checkExpect(winnerGrid.makeScene(), winnerGrid.scene);

    Minesweeper gameOverGrid = new Minesweeper(4, 2, 3);
    gameOverGrid.gameOver = true;
    WorldImage backOver = new RectangleImage(IConstants.tileSize / 2, IConstants.tileSize / 5,
        OutlineMode.SOLID, Color.BLACK);
    WorldImage textOver = new TextImage("YOU WIN", IConstants.tileSize / 1.5, Color.MAGENTA);
    WorldImage gameOver1 = new OverlayImage(textOver, backOver);
    gameOverGrid.scene.placeImageXY(gameOver1, gameOverGrid.scene.width / 2,
        gameOverGrid.scene.height / 2);

    t.checkExpect(gameOverGrid.makeScene(), gameOverGrid.scene);

  }

  // testing onMouseReleased
  void testOnMouseRelease(Tester t) {
    this.initData();
    Minesweeper msTestGrid1 = new Minesweeper(3, 4, 2, this.grid3x4);
    Posn p = new Posn(IConstants.tileSize / 2, IConstants.tileSize / 2);
    Posn p1 = new Posn(2 * IConstants.tileSize + IConstants.tileSize / 2,
        IConstants.tileSize + IConstants.tileSize / 2);
    // represents a mine
    Posn p2 = new Posn(IConstants.tileSize + IConstants.tileSize / 2, IConstants.tileSize / 2);
    Posn p3 = new Posn(IConstants.tileSize + IConstants.tileSize / 2,
        IConstants.tileSize + IConstants.tileSize / 2);
    AGamePiece convP1 = msTestGrid1.grid.get(p.y / IConstants.tileSize)
        .get(p.x / IConstants.tileSize);
    AGamePiece convP2 = msTestGrid1.grid.get(p1.y / IConstants.tileSize)
        .get(p1.x / IConstants.tileSize);
    AGamePiece convP3 = msTestGrid1.grid.get(p2.y / IConstants.tileSize)
        .get(p2.x / IConstants.tileSize);
    AGamePiece convP4 = msTestGrid1.grid.get(p3.y / IConstants.tileSize)
        .get(p3.x / IConstants.tileSize);
    msTestGrid1.onMouseReleased(p, "UnknownButton");
    msTestGrid1.onMouseReleased(p1, "UnknownButton");
    msTestGrid1.onMouseReleased(p2, "UnknownButton");
    msTestGrid1.onMouseReleased(p3, "RightButton");
    t.checkExpect(convP1.color, Color.GRAY);
    t.checkExpect(convP1.uncovered, true);
    t.checkExpect(convP2.color, Color.GRAY);
    t.checkExpect(convP2.uncovered, true);
    t.checkExpect(convP3.clickedMine(), true);
    t.checkExpect(convP3.color, Color.RED);
    t.checkExpect(convP3.uncovered, true);
    t.checkExpect(convP4.uncovered, false);
    t.checkExpect(convP4.marked, true);
    t.checkExpect(convP4.color, Color.CYAN);

  }

  // testing the clickedMine method
  void testClickedMine(Tester t) {
    this.initData();
    t.checkExpect(tile1.clickedMine(), false);
    t.checkExpect(tile2.clickedMine(), false);
    mine1.uncovered = true;
    t.checkExpect(mine1.clickedMine(), true);
    t.checkExpect(mine2.clickedMine(), false);
    mine2.uncovered = true;
    t.checkExpect(mine2.clickedMine(), true);
  }

  // testing the gameOverStatus method
  void testgameOverStatus(Tester t) {
    this.initData();
    tile1.gameOverStatus();
    t.checkExpect(tile1, new Tile());
    tile2.gameOverStatus();
    t.checkExpect(tile2, new Tile());
    mine1.gameOverStatus();
    t.checkExpect(mine1.uncovered, true);
    t.checkExpect(mine1.color, IConstants.explodedMineTile);
    mine2.gameOverStatus();
    t.checkExpect(mine2.uncovered, true);
    t.checkExpect(mine2.color, IConstants.explodedMineTile);
  }

  // testing the coveredTile method
  void testCoveredTile(Tester t) {
    this.initData();
    t.checkExpect(tile1.coveredTile(), true);
    tile1.uncovered = true;
    t.checkExpect(tile1.coveredTile(), false);
    t.checkExpect(tile2.coveredTile(), true);
    t.checkExpect(mine1.coveredTile(), false);
    mine2.uncovered = true;
    t.checkExpect(mine2.coveredTile(), false);
  }

  // testing the onTick method
  void testOnTick(Tester t) {
    this.initData();
    Minesweeper msTestGrid1 = new Minesweeper(3, 4, 2, this.grid3x4);
    msTestGrid1.onTick();
    t.checkExpect(msTestGrid1.winner, false);
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 4; j++) {
        if (msTestGrid1.grid.get(i).get(j).coveredTile()) {
          msTestGrid1.grid.get(i).get(j).uncovered = true;
        }
      }
    }
    msTestGrid1.onTick();
    t.checkExpect(msTestGrid1.winner, true);
    Minesweeper msTestGrid2 = new Minesweeper(3, 4, 2, this.grid3x4);
    msTestGrid2.gameOver = true;
    msTestGrid2.onTick();
    msTestGrid2.grid.get(0).get(2).uncovered = true;
    // testing endOfWorld. A click should not change any property of a piece
    t.checkExpect(msTestGrid2.grid.get(0).get(2).color, Color.CYAN);

  }

  // test world
  void testRun(Tester t) {
    int rows = 30;
    int cols = 30;
    int numMines = 12;
    Minesweeper ms1 = new Minesweeper(rows, cols, numMines);
    ms1.bigBang(IConstants.tileSize * cols, IConstants.tileSize * rows, 1);

  }

}
