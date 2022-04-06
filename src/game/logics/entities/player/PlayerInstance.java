package game.logics.entities.player;

import java.awt.Graphics2D;
import java.awt.Color;

import game.logics.entities.basic.EntityInstance;
import game.logics.handler.Logics;
import game.utility.input.keyboard.KeyHandler;
import game.utility.other.Pair;
import game.utility.textures.Texture;

/**
 * The <code>PlayerInstance</code> class represents the player's entity in
 * the game environment.
 * 
 * @author Daniel Pellanda
 */
public class PlayerInstance extends EntityInstance implements Player{
	
	/**
	 * The current jump speed of the player.
	 */
	private final double jumpSpeed;
	/**
	 * The current fall speed of the player.
	 */
	private final double fallSpeed;

	/**
	 * The current multiplier applied to the speed jump.
	 */
	private double jumpMultiplier = initialJumpMultiplier;
	/**
	 * The current multiplier applied to the speed fall.
	 */
	private double fallMultiplier = initialFallMultiplier;
	
	/**
	 * Manages the textures of the object.
	 */
	private final Texture texture = new Texture("player", Color.white);
	private final KeyHandler keyH;
	
	/**
	 * A string describing the current action of the player.
	 * It can either "<code>idle</code>", "<code>jump</code>" (jumping) and "<code>fall</code>" (falling).
	 */
	private String action;
	
	/**
	 * Constructor used for initializing basic parts of the player entity.
	 * 
	 * @param l the logics handler which the entity is linked to
	 */
	public PlayerInstance(final Logics l) {
		super(l);
		this.keyH = l.getKeyHandler();
		
		fallSpeed = baseFallSpeed / maximumFPS;
		jumpSpeed = baseJumpSpeed / maximumFPS;

		position = new Pair<>(xPosition, yGround);
		action = "idle";
		entityTag = "player";
	}
	
	private void jump() {
		position.setY(position.getY() - jumpSpeed * jumpMultiplier > yRoof ? position.getY() - jumpSpeed * jumpMultiplier : yRoof);
		action = "jump";
	}
	
	private void fall() {
		if(position.getY() + fallSpeed * fallMultiplier < yGround) {
			position.setY(position.getY() + fallSpeed * fallMultiplier);
			action = "fall";
		} else {
			position.setY(yGround);
			action = "idle";
		}
	}

	@Override
	public void update() {
		super.update();
		if(keyH.input.get("spacebar")) {
			jump();
			jumpMultiplier += jumpMultiplierIncrease;
			fallMultiplier = initialFallMultiplier;
		} else if(action != "idle") {
			fall();
			fallMultiplier += fallMultiplierIncrease;
			jumpMultiplier = initialJumpMultiplier;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		texture.draw(g, position, screen.getTileSize());
		super.draw(g);
	}
}
