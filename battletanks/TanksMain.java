package battletanks;

import java.awt.*;

import java.awt.event.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;

import battletanks.game.GameInput;
import battletanks.game.Gamestate;
import battletanks.game.INPUT_TYPE;
import battletanks.game.Logger;
import battletanks.graphics.Drawer;
import battletanks.graphics.SOUNDS;

import java.awt.Image;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;

import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.awt.TextRenderer;
import com.sun.opengl.util.gl2.GLUT;

import java.nio.ByteBuffer;

public class TanksMain extends Frame implements GLEventListener, KeyListener,
		MouseListener, MouseMotionListener, ActionListener{

	// mouse control variables
	private boolean isfocused = true;
	private boolean debugOut = false;
	boolean fullscreen = false;
	private final GLCanvas canvas;
	private int winW = 1440, winH = 900;
	private Point center = new Point(winW / 2, winH / 2);;
	private FPSAnimator animator;
	Drawer draw;
	private long time = System.nanoTime();
	TextRenderer rendererlg;
	TextRenderer renderersm;
	Robot robot;
	SOUNDS sfx;

	public static void main(String args[]) {
		new TanksMain();
	}

	// constructor
	public TanksMain() {
		super("Battle Tanks");
		canvas = new GLCanvas();
		canvas.addGLEventListener(this);
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.setFocusable(true);
		this.add(canvas);
		if(fullscreen == true)
			setUndecorated(true);

		
		setSize(winW, winH);

		Toolkit t = Toolkit.getDefaultToolkit();
		Image img = new java.awt.image.BufferedImage(1, 1,
				java.awt.image.BufferedImage.TYPE_INT_ARGB);
		Cursor pointer = t.createCustomCursor(img, new Point(0, 0), "none");

		setCursor(pointer);
		setVisible(true);
		animator = new FPSAnimator(canvas, 30); // create a 30 fps animator
		animator.start();
		canvas.requestFocus();

		if (fullscreen == true) {
			
			GraphicsEnvironment ge = GraphicsEnvironment
					.getLocalGraphicsEnvironment();

			ge.getDefaultScreenDevice().setFullScreenWindow(this);
		} else {
			

			// this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}

		Rectangle r = this.getBounds();
		center = new Point(r.x + r.width / 2, r.y + r.height / 2);

		try {
			robot = new Robot();
		} catch (AWTException e) {

			e.printStackTrace();
		}
		center = new Point(r.x + r.width / 2, r.y + r.height / 2);
	}

	// gl display function
	public void display(GLAutoDrawable drawable) {
		Point mousepos = MouseInfo.getPointerInfo().getLocation();

		int mousex = 0;
		int mousey = 0;
		if (robot != null && isfocused == true ){
			 mousex = center.x - mousepos.x;
			 mousey = center.y - mousepos.y;
			robot.mouseMove(center.x, center.y);
		}
		Gamestate.getInstance().AddInput(
				new GameInput(INPUT_TYPE.VIEWX, mousex));
		Gamestate.getInstance().AddInput(
				new GameInput(INPUT_TYPE.VIEWY, mousey));
		Logger.getInstance().debugVal("Mouse:",
				"<" + mousex + ">," + "<" + mousey + ">");

		time = System.currentTimeMillis();

		Gamestate.getInstance().UpdateState(time);

		draw.Draw(Gamestate.getInstance());

		if (debugOut) {
			renderersm
					.beginRendering(drawable.getWidth(), drawable.getHeight());
			renderersm.setColor(.0f, 1f, 0.f, 1f);

			Logger.getInstance().display(renderersm);

			renderersm.endRendering();
		}

		rendererlg.beginRendering(drawable.getWidth(), drawable.getHeight());
		rendererlg.setColor(.0f, 1f, 0.f, .8f);
		draw.drawUIText(rendererlg);
		rendererlg.endRendering();
		


	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
		// TODO Auto-generated method stub
	}

	// initialization
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.setSwapInterval(1);
		GLU glu = new GLU();
		GLUT glut = new GLUT();
		rendererlg = new TextRenderer(new Font("SansSerif", Font.BOLD, 32));
		rendererlg.setSmoothing(true);
		renderersm = new TextRenderer(new Font("Dialog", Font.BOLD, 12));
		draw = new Drawer(gl, glu, glut);
		draw.LoadRes();

	}

	// reshape callback function: called when the size of the window changes
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		winW = width;
		winH = height;
		draw.resize(width, height);
		Rectangle r = this.getBounds();
		center = new Point(r.x + r.width / 2, r.y + r.height / 2);
	}

	// mouse pressed even callback function
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == e.BUTTON1) {
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.FIRE_PRESSED));
		}

	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == e.BUTTON1) {
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.FIRE_RELEASED));
		}

	}

	public void mouseDragged(MouseEvent e) {

	}

	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ESCAPE:
		case KeyEvent.VK_Q:
			System.exit(0);
			break;
		case KeyEvent.VK_Z:
			debugOut = !debugOut;

			break;

		case KeyEvent.VK_W:
		case KeyEvent.VK_UP:
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.FORWARD_PRESSED));
			break;
		case KeyEvent.VK_S:
		case KeyEvent.VK_DOWN:
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.BACKWARD_PRESSED));
			break;
		case KeyEvent.VK_A:
		case KeyEvent.VK_LEFT:
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.LEFT_PRESSED));
			break;
		case KeyEvent.VK_D:
		case KeyEvent.VK_RIGHT:
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.RIGHT_PRESSED));
			break;
		case KeyEvent.VK_SPACE:
			Gamestate.getInstance().nextRound();
			break;
		case KeyEvent.VK_B:
			draw.setShowBoundSphere(!draw.getShowBoundSphere());
			break;
		case KeyEvent.VK_F1:
			Gamestate.getInstance().reset();
			break;
		case KeyEvent.VK_F5:
			isfocused = !isfocused;
			break;

		}

	}

	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ESCAPE:
		case KeyEvent.VK_Q:
			System.exit(0);
		case KeyEvent.VK_W:
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.FORWARD_RELEASED));
			break;
		case KeyEvent.VK_UP:
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.FORWARD_RELEASED));
			break;
		case KeyEvent.VK_S:
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.BACKWARD_RELEASED));
			break;
		case KeyEvent.VK_DOWN:
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.BACKWARD_RELEASED));
			break;
		case KeyEvent.VK_A:
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.LEFT_RELEASED));
			break;
		case KeyEvent.VK_LEFT:
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.LEFT_RELEASED));
			break;
		case KeyEvent.VK_D:
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.RIGHT_RELEASED));
			break;
		case KeyEvent.VK_RIGHT:
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.RIGHT_RELEASED));
			break;
		case KeyEvent.VK_SPACE:
			Gamestate.getInstance().AddInput(
					new GameInput(INPUT_TYPE.FIRE_RELEASED));
			break;

		}
	}

	// these event functions are not used for this assignment
	// but may be useful in the future
	public void keyTyped(KeyEvent e) {
	}

	public void mouseMoved(MouseEvent e) {

	}

	public void actionPerformed(ActionEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub

	}

}
