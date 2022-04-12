package game.logics.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.awt.Graphics2D;

import java.lang.Runnable;

import game.logics.entities.basic.*;
import game.logics.entities.obstacles.Obstacle;
import game.logics.entities.obstacles.ZapperBaseInstance;
import game.logics.entities.obstacles.ZapperRayInstance;
import game.logics.entities.player.PlayerInstance;
import game.logics.interactions.Generator;
import game.logics.interactions.SpeedHandler;
import game.logics.interactions.TileGenerator;
import game.utility.debug.Debugger;
import game.utility.input.keyboard.KeyHandler;
import game.utility.screen.Screen;
import game.utility.other.GameState;
import game.display.DisplayController;

/**
 * The <code>LogicsHandler</code> class helps <class>GameWindow</class> to update
 * and draw logical parts of the game like the Interface, Entities, Collisions, etc....
 * 
 * @author Daniel Pellanda
 */
public class LogicsHandler implements Logics{
	
	/**
	 * Contains the current active entities on the game environment.
	 */
	private final Map<String, Set<Entity>> entities = new HashMap<>();
	
	/**
	 * Generates sets of obstacles on the environment.
	 */
	private final Generator spawner;
	private final Screen gScreen;
	private final KeyHandler keyH;
	private final DisplayController displayController;
	
	//private final Pair<Double,Double> obstaclesPos;
	private int score = 0;
	/**
	 * Keeps the game timer.
	 */
	private long updateTimer = System.nanoTime();
	/**
	 * Defines how many seconds have to pass for the spawner to generate
	 * another set of obstacles.
	 */
	private int spawnInterval = 3;
	/**
	 * Defines the interval of each check for entities to clean.
	 */
	private int cleanInterval = 1;
	
	private Debugger debugger;
	private GameState gState = GameState.MENU;
	
	/**
	 * Constructor that gets the screen information, the keyboard listener and the debugger, 
	 * initialize each entity category on the entities map and initialize the obstacle spawner.
	 * 
	 * @param screen the screen information of the game window
	 * @param keyH the keyboard listener linked to the game window
	 * @param debugger the debugger used
	 */
	public LogicsHandler(final Screen screen, final KeyHandler keyH, final Debugger debugger) {
		this.gScreen = screen;
		this.keyH = keyH;
		this.debugger = debugger;
		this.displayController = new DisplayController(keyH,screen, g -> this.gState = g, () -> gState);
		
		entities.put("player", new HashSet<>());
		entities.put("zappers", new HashSet<>());
		
		entities.get("player").add(new PlayerInstance(this));
		
		spawner = new TileGenerator(entities, spawnInterval);
		spawner.setZapperBaseCreator(p -> new ZapperBaseInstance(this, p, new SpeedHandler()));
		spawner.setZapperRayCreator((b,p) -> new ZapperRayInstance(this, p, b.getX(), b.getY()));
		
		spawner.initialize();
		spawner.pause();
	}


	private void beginGame() {
		entities.get("player").add(new PlayerInstance(this));
		spawner.resume();
	}
	
	private void endGame() {
		spawner.pause();
		entities.forEach((s, se) -> se.clear());
	}
	
	private void pauseGame() {
		spawner.pause();
	}
	
	private void resumeGame() {
		spawner.resume();
	}

	/**
	 * Method for test enabling and disabling entity spawner
	 */
	private void checkSpawner() {
		if(keyH.input.get("c")) {
			if(spawner.isRunning()) {
				spawner.resume();
			} else {
				spawner.start();
			}
		} else if(keyH.input.get("v")) {
			spawner.pause();
		}
	}
	
	/**
	 * Handles the enabling and disabling of the Debug Mode 
	 * by using Z (enable) and X (disable).
	 */
	private void checkDebugMode() {
		if(keyH.input.get("z")) {
			debugger.setDebugMode(true);
		} else if(keyH.input.get("x")) {
			debugger.setDebugMode(false);
		}
	}
	
	/**
	 * Removes all entities that are on the "clear area" [x < -tile size].
	 */
	private void cleanEntities() {
		entities.get("zappers").removeIf(e -> {
			Obstacle o = (Obstacle)e;
			if(o.isOnClearArea()) {
				o.resetPosition();
				if(debugger.isFeatureEnabled("log: entities cleaner working")) {
					System.out.println("reset");
				}
			}
			return o.isOnClearArea();
		});
	}
	
	/**
	 * Utility function for running a certain block of code every given interval of time.
	 * 
	 * @param interval the interval in nanoseconds that has to pass after each execution
	 * @param timeStart the system time from when the last execution happened
	 * @param r the block of the code to execute
	 * @return <code>true</code> if given code has been executed, <code>false</code> if not
	 */
	private boolean updateEachInterval(final long interval, final long timeStart, final Runnable r) {
		long timePassed = System.nanoTime() - timeStart;
		if(timePassed >= interval) {
			r.run();
			return true;
		}
		return false;
	}
	
	public Screen getScreenInfo() {
		return gScreen;
	}
	
	public KeyHandler getKeyHandler() {
		return keyH;
	}
	
	public Debugger getDebugger() {
		return debugger;
	}
	
	public void updateAll() {
		if(isInGame()) {
			if(updateEachInterval(this.cleanInterval * 1000000000, updateTimer, () -> cleanEntities())) {
				updateTimer = System.nanoTime();
				if(debugger.isFeatureEnabled("log: entities cleaner check")) {
					System.out.println("clean");
				}
			}
			synchronized(entities) {
				entities.forEach((s, se) -> se.forEach(e -> e.update()));
			}
			incScore();
			this.displayController.updateHUD(this.score);
		}
		this.displayController.updateScreen();
		checkDebugMode();
		checkSpawner();
		updateGameState();
	}
	
	public void drawAll(final Graphics2D g) {
		if (isInGame() || this.isPaused()) {
			synchronized(entities) {
				entities.forEach((s, se) -> se.forEach(e -> e.draw(g)));
			}
		}
		this.displayController.drawScreen(g);
	}

	private void updateGameState() {
		switch (this.gState) {
		case EXIT:
			System.exit(0);
			// TODO FIX BRUTAL EXIT
			break;
		case MENU:
			this.spawner.pause();
			break;
		case INGAME:
			this.spawner.resume();
			if(updateEachInterval(this.cleanInterval * 1000000000, updateTimer, () -> cleanEntities())) {
				updateTimer = System.nanoTime();
				if(debugger.isFeatureEnabled("log: entities cleaner check")) {
					System.out.println("clean");
				}
			}
			if(keyH.input.get("p") && this.isInGame()) {
				setGameState(GameState.PAUSED);
			}
			break;
		case PAUSED:
			this.spawner.pause();
			if (keyH.input.get("r")) {
				setGameState(GameState.INGAME);
				this.resumeGame();
			} else if(keyH.input.get("e")) {
				setGameState(GameState.MENU);
				this.score = 0;
				this.endGame();
			}
		}
	}
	
	private boolean isInGame() {
		return this.gState == GameState.INGAME;
	}
	
	private boolean isPaused() {
		return this.gState == GameState.PAUSED;
	}
	
	private boolean isInMenu() {
		return this.gState == GameState.MENU;
	}
	
	private void setGameState(GameState gs) {
		this.gState = gs;
	}
	
	private void incScore() {
		this.score ++;
	}
	
}
