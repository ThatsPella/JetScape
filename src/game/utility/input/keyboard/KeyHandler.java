package game.utility.input.keyboard;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The <code>KeyHandler</code> class is used for knowing 
 * which keys are being pressed or released on the keyboard.
 * 
 * @author Daniel Pellanda
 */
public class KeyHandler implements KeyListener{

	/**
	 * A map that tells if a key is being pressed or not.
	 * 
	 * <p>
	 * If <code>input.get(key)</code> is <code>true</code> means
	 * that the current key is being pressed.
	 * <br>
	 * If <code>input.get(key)</code> is <code>false</code> means
	 * that the current key is not being pressed.
	 * </p>
	 * 
	 */
	public Map<String,Boolean> input = new HashMap<>();
	private Optional<Integer> lastKeyPressed = Optional.empty();
	
	/**
	 * Initializes a <code>KeyHandler</code>.
	 */
	public KeyHandler() {
		input.put("spacebar", false);
		input.put("x", false);
		input.put("z", false);
		input.put("enter", false);
		input.put("e", false);
		input.put("r", false);
		input.put("p", false);
		input.put("c", false);
		input.put("v", false);
		input.put("up", false);
		input.put("down", false);
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				input.replace("spacebar", true);
				break;
			case KeyEvent.VK_X:
				input.replace("x", true);
				break;
			case KeyEvent.VK_Z:
				input.replace("z", true);
				break;
			case KeyEvent.VK_ENTER:
				input.replace("enter", true);
				break;
			case KeyEvent.VK_E:
				input.replace("e", true);
				break;
			case KeyEvent.VK_C:
				input.replace("c", true);
				break;
			case KeyEvent.VK_V:
				input.replace("v", true);
				break;
			case KeyEvent.VK_P:
				input.replace("p", true);
				break;
			case KeyEvent.VK_R:
				input.replace("r", true);
				break;
			case KeyEvent.VK_UP:
				input.replace("up", true);
				break;
			case KeyEvent.VK_DOWN:
				input.replace("down", true);
				break;
		}
		this.lastKeyPressed = Optional.of(e.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				input.replace("spacebar", false);
				break;
			case KeyEvent.VK_X:
				input.replace("x", false);
				break;
			case KeyEvent.VK_Z:
				input.replace("z", false);
				break;
			case KeyEvent.VK_ENTER:
				input.replace("enter", false);
				break;
			case KeyEvent.VK_E:
				input.replace("e", false);
				break;
			case KeyEvent.VK_C:
				input.replace("c", false);
				break;
			case KeyEvent.VK_V:
				input.replace("v", false);
				break;
			case KeyEvent.VK_P:
				input.replace("p", false);
				break;
			case KeyEvent.VK_R:
				input.replace("r", false);
				break;
			case KeyEvent.VK_UP:
				input.replace("up", true);
				break;
			case KeyEvent.VK_DOWN:
				input.replace("down", true);
				break;
		}
	}
	
	public boolean isKeyTyped (int e) {
		if (this.lastKeyPressed.isPresent() &&
			this.lastKeyPressed.get() == e) {
			 this.lastKeyPressed = Optional.empty();
			 return true;
		}
		return false;
	}
}