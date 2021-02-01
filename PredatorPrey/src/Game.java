import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.lang.Math;

import processing.core.PApplet;
import processing.data.IntList;

//includes general settings
public class Game {
	private PApplet parent;

	// Window size
	int height = 800;
	int width = 1400;

	// World matrix
	int[][] worldMatrix;
	int spotSize;
	int columns;
	int rows;

	// amount of steps
	int stepNumber;

	// colors
	private IntList colors;
	private float transparency = 240;
	private int fillColorBright;
	private int textColorBright;
	private int fillColorDark;
	private int textColorDark;
	private int fillColorMiddle;
	private int textColorMiddle;
	private int fillColorActivated;
	private int backgroundColor;
	private int colorPrey;
	private int colorPredator;
	private int colorNoLife;

	// Buttons
	private Button predator; // create predator
	private Button prey; // create prey
	private Button delete; // delete life
	private Button run; // starts running steps until it stops
	private Button step; // starts running one step and stops
	private Button stop; // stops running
	private Button randomField; // creates a random field
	private Button deleteField; // deletes the field
	private Button textProbabilityPrey; // text over probabilityPreySlider
	private Button textProbabilityPredator; // text over probabilityPredatorSlider
	private Button textProbability; // text over probabilitySlider
	private Button textSpotSize; // text over spotSizeSlider

	// Slider
	private Slider probabilitySlider; // slider to influence the life creating chance
	private Slider probabilityPreySlider; // slider to influence the prey life creating chance
	private Slider probabilityPredatorSlider; // slider to influence the predator life creating chance
	private Slider spotSizeSlider; // slider to influence the spot size (amount of fields)

	// probability of creating life (for creating a random field) of predator and
	// prey
	private float probabilityPrey;
	private float probabilityPredator;
	private float probability; // probability of creating life overall

	// States
	private int state = 0; // if 0 - nothing happens; if 1 - creates prey; if 2 - creates predator; if 3 -
							// deletes life
	private boolean running = false; // state running

	// text factors
	private int buttonSizeX;
	private int buttonSizeY;
	private int textSize;
	private int correctionFactor;

	public Game(PApplet parent) {
		this.parent = parent;
		buttonSizeX = getWidth() / 8;
		buttonSizeY = getHeight() / 20;
		textSize = getHeight() / 36;
		correctionFactor = textSize / 8;
		colors = new IntList();
		fillColorBright = parent.color(150, 150, 150, transparency); // color #1
		colors.append(fillColorBright);
		textColorBright = parent.color(230, 230, 230, transparency); // color #2
		colors.append(textColorBright);
		fillColorDark = parent.color(60, 60, 60, transparency); // color #3
		colors.append(fillColorDark);
		textColorDark = parent.color(20, 20, 20, transparency); // color #4
		colors.append(textColorDark);
		fillColorActivated = parent.color(45, 177, 76, transparency); // color #5
		colors.append(fillColorActivated);
		backgroundColor = parent.color(255, 255, 255); // color #6
		colors.append(backgroundColor);
		colorPrey = parent.color(0, 64, 128); // color #7
		colors.append(colorPrey);
		colorPredator = parent.color(128, 0, 0); // color #8
		colors.append(colorPredator);
		createWorldMatrix(6);
		this.probability = 20;
		this.probabilityPrey = 98;
		this.probabilityPredator = 100 - probabilityPrey;
		createButtons();
		createSlider();
	}

	// creates the world matrix
	private void createWorldMatrix(int spotSize) {
		this.spotSize = spotSize;
		stepNumber = 0;
		this.columns = (int) Math.floor((width * 0.8) / spotSize);
		this.rows = (int) Math.floor(height / spotSize);
		worldMatrix = new int[rows][columns];
		for (int i = 0; i < worldMatrix.length; i++) {
			for (int j = 0; j < worldMatrix[0].length; j++) {
				worldMatrix[i][j] = 0;
			}
		}
	}

	// returns window height
	public int getHeight() {
		return height;
	}

	// returns window width
	public int getWidth() {
		return width;
	}

	// displays every in settings created element
	public void display() {
		parent.background(getColor(6));
		for (int i = 0; i < worldMatrix.length; i++) {
			for (int j = 0; j < worldMatrix[0].length; j++) {
				parent.rectMode(parent.CENTER);
				parent.stroke(getColor(1));
				parent.strokeWeight(1);
				if (worldMatrix[i][j] == 0) {
					parent.fill(getColor(6));
					parent.square(spotSize / 2 + spotSize * j, spotSize / 2 + spotSize * i, spotSize);
				} else if (worldMatrix[i][j] == 1) {
					parent.fill(getColor(7));
					parent.square(spotSize / 2 + spotSize * j, spotSize / 2 + spotSize * i, spotSize);
				} else if (worldMatrix[i][j] == 2) {
					parent.fill(getColor(8));
					parent.square(spotSize / 2 + spotSize * j, spotSize / 2 + spotSize * i, spotSize);
				}
			}
		}
		predator.display();
		prey.display();
		delete.display();
		run.display();
		step.display();
		stop.display();
		randomField.display();
		deleteField.display();
		probabilitySlider.display();
		probabilityPreySlider.display();
		probabilityPredatorSlider.display();
		spotSizeSlider.display();
		textProbabilityPrey.display();
		textProbabilityPredator.display();
		textProbability.display();
		textSpotSize.display();
		// evolution stage
		if (running == true) {
			calculateNextStep(); // calculate the next step
		}
		Button evoStage = new Button(parent, this, "Step No.: " + stepNumber, getWidth() * 9 / 10,
				getHeight() * 61 / 100, buttonSizeX, 25, 17, 3);
		evoStage.setNonActive(true);
		evoStage.display();
	}

	// returns the numbers color
	public int getColor(int number) {
		if (number <= colors.size() || number <= 0) {
			return colors.get(number - 1);
		} else {
			System.out.println("Color does not exist");
			return colors.get(0); // if number does not exist - return "fillColorBright"
		}
	}

	private void createButtons() {
		// Buttons
		predator = new Button(parent, this, "Predator", getWidth() * 9 / 10, getHeight() * 3 / 100, buttonSizeX,
				buttonSizeY, textSize, correctionFactor);
		prey = new Button(parent, this, "Prey", getWidth() * 9 / 10, getHeight() * 9 / 100, buttonSizeX, buttonSizeY,
				textSize, correctionFactor);
		delete = new Button(parent, this, "Delete", getWidth() * 9 / 10, getHeight() * 15 / 100, buttonSizeX,
				buttonSizeY, textSize, correctionFactor);
		run = new Button(parent, this, "Run", getWidth() * 9 / 10, getHeight() * 25 / 100, buttonSizeX, buttonSizeY,
				textSize, correctionFactor);
		step = new Button(parent, this, "Step", getWidth() * 9 / 10, getHeight() * 31 / 100, buttonSizeX, buttonSizeY,
				textSize, correctionFactor);
		stop = new Button(parent, this, "Stop", getWidth() * 9 / 10, getHeight() * 37 / 100, buttonSizeX, buttonSizeY,
				textSize, correctionFactor);
		randomField = new Button(parent, this, "Random Field", getWidth() * 9 / 10, getHeight() * 47 / 100, buttonSizeX,
				buttonSizeY, textSize, correctionFactor);
		deleteField = new Button(parent, this, "Delete Field", getWidth() * 9 / 10, getHeight() * 53 / 100, buttonSizeX,
				buttonSizeY, textSize, correctionFactor);
	}

	private void createSlider() {
		// Text fields
		textProbability = new Button(parent, this, "Life %", getWidth() * 96 / 100, getHeight() * 67 / 100, 52, 25, 15,
				2);
		textProbability.setNonActive(true);
		textProbabilityPrey = new Button(parent, this, "Prey %", getWidth() * 92 / 100, getHeight() * 67 / 100, 52, 25,
				15, 2);
		textProbabilityPrey.setNonActive(true);
		textProbabilityPredator = new Button(parent, this, "Pred %", getWidth() * 88 / 100, getHeight() * 67 / 100, 52,
				25, 15, 2);
		textProbabilityPredator.setNonActive(true);
		textSpotSize = new Button(parent, this, "Size", getWidth() * 84 / 100, getHeight() * 67 / 100, 52, 25, 15, 2);
		textSpotSize.setNonActive(true);
		// Slider
		probabilitySlider = new Slider(parent, this, probability, getWidth() * 96 / 100, getHeight() * 84 / 100, 4, 200,
				getColor(3), 0, 100, 50, 30, 15, 2, 1);
		probabilityPreySlider = new Slider(parent, this, probabilityPrey, getWidth() * 92 / 100, getHeight() * 84 / 100,
				4, 200, getColor(7), 0, 100, 50, 30, 15, 2, 1);
		probabilityPredatorSlider = new Slider(parent, this, probabilityPredator, getWidth() * 88 / 100,
				getHeight() * 84 / 100, 4, 200, getColor(8), 0, 100, 50, 30, 15, 2, 1);
		spotSizeSlider = new Slider(parent, this, spotSize, getWidth() * 84 / 100, getHeight() * 84 / 100, 4, 200,
				getColor(3), 4, 60, 50, 30, 15, 2, 0);
	}

	public void action() {
		if (predator.mouseOverButton() == true) {
			if (predator.getState() == false) {
				state = 2;
				predator.setState(true);
				prey.setState(false);
				delete.setState(false);
			} else if (predator.getState() == true) {
				state = 0;
				predator.setState(false);
				prey.setState(false);
				delete.setState(false);
			}
		} else if (prey.mouseOverButton() == true) {
			if (prey.getState() == false) {
				state = 1;
				prey.setState(true);
				delete.setState(false);
				predator.setState(false);
			} else if (prey.getState() == true) {
				state = 0;
				prey.setState(false);
				delete.setState(false);
				predator.setState(false);
			}
		} else if (delete.mouseOverButton() == true) {
			if (delete.getState() == false) {
				state = 3;
				prey.setState(false);
				delete.setState(true);
				predator.setState(false);
			} else if (delete.getState() == true) {
				state = 0;
				prey.setState(false);
				delete.setState(false);
				predator.setState(false);
			}
		} else if (run.mouseOverButton() == true) {
			running = true;
		} else if (step.mouseOverButton() == true) {
			running = false;
			calculateNextStep();
		} else if (stop.mouseOverButton() == true) {
			running = false;
		} else if (randomField.mouseOverButton() == true) {
			running = false;
			createRandomField();
			stepNumber = 0;
		} else if (deleteField.mouseOverButton() == true) {
			running = false;
			createWorldMatrix(spotSize);
		} else {
			if (parent.mouseX / spotSize < worldMatrix[0].length && parent.mouseY / spotSize < worldMatrix.length) {
				if (state == 1) {
					worldMatrix[parent.mouseY / spotSize][parent.mouseX / spotSize] = 1;
				} else if (state == 2) {
					worldMatrix[parent.mouseY / spotSize][parent.mouseX / spotSize] = 2;
				} else if (state == 3) {
					worldMatrix[parent.mouseY / spotSize][parent.mouseX / spotSize] = 0;
				}
			}
		}
		parent.mouseButton = 0;
	}

	// calculates the next evolution step
	public void calculateNextStep() {
		int tempValuePredator = 0;
		int tempValuePrey = 0;
		stepNumber++; // increase step number
		int[][] tempMatrix = new int[rows][columns];
		for (int i = 0; i < tempMatrix.length; i++) {
			for (int j = 0; j < tempMatrix[0].length; j++) {
				// count the neighbors
				tempValuePredator = 0;
				tempValuePrey = 0;
				for (int m = -1; m <= 1; m++) {
					for (int n = -1; n <= 1; n++) {
						if (!(m == 0 && n == 0)) {
							// toroidal behavior
							if (i + m >= tempMatrix.length) {
								if (j + n >= tempMatrix[0].length) {
									if (worldMatrix[-1 + m][-1 + n] == 1) {
										tempValuePrey = tempValuePrey + 1;
									} else if (worldMatrix[-1 + m][-1 + n] == 2) {
										tempValuePredator = tempValuePredator + 1;
									}
								} else if (j + n < 0) {
									if (worldMatrix[-1 + m][tempMatrix[0].length + n] == 1) {
										tempValuePrey = tempValuePrey + 1;
									} else if (worldMatrix[-1 + m][tempMatrix[0].length + n] == 2) {
										tempValuePredator = tempValuePredator + 1;
									}
								} else {
									if (worldMatrix[-1 + m][j + n] == 1) {
										tempValuePrey = tempValuePrey + 1;
									} else if (worldMatrix[-1 + m][j + n] == 2) {
										tempValuePredator = tempValuePredator + 1;
									}
								}
							} else if (i + m < 0) {
								if (j + n >= tempMatrix[0].length) {
									if (worldMatrix[tempMatrix.length + m][-1 + n] == 1) {
										tempValuePrey = tempValuePrey + 1;
									} else if (worldMatrix[tempMatrix.length + m][-1 + n] == 2) {
										tempValuePredator = tempValuePredator + 1;
									}
								} else if (j + n < 0) {
									if (worldMatrix[tempMatrix.length + m][tempMatrix[0].length + n] == 1) {
										tempValuePrey = tempValuePrey + 1;
									} else if (worldMatrix[tempMatrix.length + m][tempMatrix[0].length + n] == 2) {
										tempValuePredator = tempValuePredator + 1;
									}
								} else {
									if (worldMatrix[tempMatrix.length + m][j + n] == 1) {
										tempValuePrey = tempValuePrey + 1;
									} else if (worldMatrix[tempMatrix.length + m][j + n] == 2) {
										tempValuePredator = tempValuePredator + 1;
									}
								}
							} else if (j + n >= tempMatrix[0].length) {
								if (worldMatrix[i + m][-1 + n] == 1) {
									tempValuePrey = tempValuePrey + 1;
								} else if (worldMatrix[i + m][-1 + n] == 2) {
									tempValuePredator = tempValuePredator + 1;
								}
							} else if (j + n < 0) {
								if (worldMatrix[i + m][tempMatrix[0].length + n] == 1) {
									tempValuePrey = tempValuePrey + 1;
								} else if (worldMatrix[i + m][tempMatrix[0].length + n] == 2) {
									tempValuePredator = tempValuePredator + 1;
								}
							} else {
								if (worldMatrix[i + m][j + n] == 1) {
									tempValuePrey = tempValuePrey + 1;
								} else if (worldMatrix[i + m][j + n] == 2) {
									tempValuePredator = tempValuePredator + 1;
								}
							}
						}
					}
				}
				// Rules
				// #1: no life - 2 or 3 predator neighbors and 3 or more prey neighbors - new
				// predator gets born
				// #2: no life - 3 or more prey neighbors and less than 2 or more than 3
				// predator neighbors - new prey gets born
				// #3: no life - else - nothing happens
				// #4: prey - 1 or more predator neighbors and 2 or more prey neighbors - new
				// predator gets born
				// #5: prey - 1 or less prey neighbors or 4 or more prey neighbors or 5 or more
				// predator neighbors - prey dies
				// #6: prey - else - prey stays
				// #7: predator - similar or more predators than preys in the neighborhood -
				// predator dies
				// #8: predator - else - nothing happens
				if (worldMatrix[i][j] == 0 && (tempValuePredator == 2 || tempValuePredator == 3)
						&& tempValuePrey >= 3) {
					tempMatrix[i][j] = 2;
				} else if (worldMatrix[i][j] == 0 && (tempValuePrey >= 3)) {
					tempMatrix[i][j] = 1;
				} else if (worldMatrix[i][j] == 0) {
					tempMatrix[i][j] = 0;
				}
				if (worldMatrix[i][j] == 1 && tempValuePredator >= 1 && tempValuePrey >= 2) {
					tempMatrix[i][j] = 2;
				} else if (worldMatrix[i][j] == 1
						&& (tempValuePrey <= 1 || tempValuePrey >= 4 || tempValuePredator >= 5)) {
					tempMatrix[i][j] = 0;
				} else if (worldMatrix[i][j] == 1) {
					tempMatrix[i][j] = 1;
				}
				if (worldMatrix[i][j] == 2 && (tempValuePredator >= tempValuePrey)) {
					tempMatrix[i][j] = 0;
				} else if (worldMatrix[i][j] == 2) {
					tempMatrix[i][j] = 2;
				}
			}
		}
		// overwrite the world matrix with the new parameters
		for (int i = 0; i < tempMatrix.length; i++) {
			for (int j = 0; j < tempMatrix[0].length; j++) {
				worldMatrix[i][j] = tempMatrix[i][j];
			}
		}
	}

	public void actionSlider() {
		if (probabilitySlider.mouseOverButton() == true) {
			probabilitySlider.moveSlider();
			this.probability = probabilitySlider.getValue();
			createSlider();
		} else if (spotSizeSlider.mouseOverButton() == true) {
			running = false;
			spotSizeSlider.moveSlider();
			if ((int) spotSizeSlider.getValue() % 2 == 0) {
				spotSize = (int) spotSizeSlider.getValue();
			} else {
				spotSize = (int) spotSizeSlider.getValue() - ((int) spotSizeSlider.getValue() % 2);
			}
			createWorldMatrix(spotSize);
			createSlider();
		} else if (probabilityPreySlider.mouseOverButton() == true) {
			probabilityPreySlider.moveSlider();
			this.probabilityPrey = probabilityPreySlider.getValue();
			probabilityPredator = 100 - probabilityPrey;
			createSlider();
		} else if (probabilityPredatorSlider.mouseOverButton() == true) {
			probabilityPredatorSlider.moveSlider();
			this.probabilityPredator = probabilityPredatorSlider.getValue();
			probabilityPrey = 100 - probabilityPredator;
			createSlider();
		}
		parent.mouseButton = 0;
	}

	private void createRandomField() {
		int tempProbability = 0;
		int tempPrey = 0;
		for (int i = 0; i < worldMatrix.length; i++) {
			for (int j = 0; j < worldMatrix[0].length; j++) {
				tempProbability = (int) parent.random(0, 101); // create random number between from 0 to 100
				tempPrey = (int) parent.random(0, 101); // create random number between from 0 to 100
				if (tempProbability >= 100 - probability && probability != 0) {
					if (tempPrey >= 100 - probabilityPrey && probabilityPrey != 0) {
						worldMatrix[i][j] = 1;
					} else {
						worldMatrix[i][j] = 2;
					}

				} else {
					worldMatrix[i][j] = 0;
				}
			}
		}
	}
}
