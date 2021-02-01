import processing.core.PApplet;

public class PredatorPrey extends PApplet {
	public static void main(String[] args) {
		PApplet.main("PredatorPrey");
	}

	Game game;

	// setting window size
	public void settings() {
		game = new Game(this);
		size(game.getWidth(), game.getHeight());
	}

	public void setup() {
		surface.setTitle("Predator Prey Simulation");
		frameRate(140); // try to create a high framerate of about 140
	}

	public void draw() {
		game.display();
	}

	// mouse operations
	public void mouseClicked() {
		if (mouseButton == LEFT) {
			game.action();
		}
	}

	public void mouseDragged() {
		if (mouseButton == LEFT) {
			game.actionSlider();
		}
	}
}
