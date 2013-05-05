package battletanks.graphics;

import java.util.Iterator;
import java.awt.Point;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;

import com.sun.opengl.util.awt.TextRenderer;
import com.sun.opengl.util.gl2.GLUT;

import battletanks.TanksMain;
import battletanks.game.Gamestate;
import battletanks.game.Logger;
import battletanks.game.objects.Bullet;
import battletanks.game.objects.GameObject;
import battletanks.game.objects.Part;
import battletanks.game.objects.Tank;

public class Drawer {

	private Model tankmodel;
	private Model tankbase;

	GL2 gl;
	GLU glu;
	GLUT glut;
	private float znear, zfar;
	private int width, height;

	public Drawer(GL2 gl2, GLU glu, GLUT glut) {

		this.gl = gl2;
		this.glu = glu;
		this.glut = glut;
		width = 1440;
		height = 900;

		znear = 0.01f;
		zfar = 1000.f;

		gl2.glClearColor(.1f, .1f, .1f, 1f);
		gl2.glClearDepth(1.0f);

		gl2.glEnable(GL.GL_DEPTH_TEST);
		gl2.glDepthFunc(GL.GL_LESS);
		gl2.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
		gl2.glCullFace(GL.GL_BACK);
		gl2.glEnable(GL.GL_CULL_FACE);

	}

	public void LoadRes() {
		MODELS.LoadModels();

	}

	private void moveCamera(float theta, float phi) {

		gl.glRotatef(phi, 1.0f, 0, 0f);
		gl.glRotatef(theta, 0, 1.0f, 0);

	}

	private void DrawGeometry(Vector3f color) {
		gl.glPushMatrix();
		gl.glLineWidth(1.5f);

		if (color == null)
			gl.glColor3f(0.5F, 0.5F, .5F);
		else
			gl.glColor3f(color.x, color.y, color.z);
		for (GameObject ob : Gamestate.getInstance().getObstacles()) {
			gl.glPushMatrix();
			gl.glTranslatef(ob.getBase().getPos().x, ob.getBase().getPos().y,
					ob.getBase().getPos().z);
			gl.glRotatef(ob.getBase().getDir().x, 1.0f, 0, 0f);
			gl.glRotatef(ob.getBase().getDir().y, 0, 1.0f, 0);
			//glut.glutSolidCube(1);
			gl.glPopMatrix();
		}

		if (color == null)
			gl.glColor3f(0.9F, 0.0F, .0F);
		else
			gl.glColor3f(color.x, color.y, color.z);

		for (GameObject ob : Gamestate.getInstance().getTanks()) {
			for (Part p : ob.getParts()) {
				gl.glPushMatrix();
				gl.glTranslatef(p.getPos().x, p.getPos().y, p.getPos().z);
				gl.glRotatef(p.getDir().x, 0, 1.0f, 0f);
				gl.glRotatef(p.getDir().y, 1.0f,0 , 0);
				gl.glTranslatef(p.getCenterrot().x, p.getCenterrot().y, p.getCenterrot().z);
				p.getModel().Draw(gl);
				gl.glPopMatrix();
			}
		}
		
		
		if (color == null)
			gl.glColor3f(0.0F, 1F, 1F);
		else
			gl.glColor3f(color.x, color.y, color.z);
		for (GameObject ob : Gamestate.getInstance().getBullets()) {
			Bullet b = (Bullet)ob;
			Iterator<Vector3f> it = b.getOldPos();
			Iterator<Vector2f> id = b.getOldDir();
			int c = 0;
			
			while (c < 25 && it.hasNext()) {
				c++;
				Vector3f p = it.next();
				Vector2f d = id.next();
				gl.glPushMatrix();
				gl.glTranslatef(p.x, p.y, p.z);
				gl.glRotatef(-d.x, 0, 1.0f, 0f);
				gl.glRotatef(d.y, 1.0f,0 , 0);
				ob.getBase().getModel().Draw(gl);
				gl.glPopMatrix();
			}
		}

		gl.glPopMatrix();
	}

	private void DrawBackground() {
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
		gl.glColor3f(0, 1, 0);
		gl.glPushMatrix();
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(500.0f, -0.5f, -500.0f);
		gl.glVertex3f(-500.0f, -0.5f, -500.0f);
		gl.glVertex3f(-500.0f, -0.5f, 500.0f);
		gl.glVertex3f(500.0f, -0.5f, 500.0f);
		gl.glEnd();
		gl.glPopMatrix();

	}

	private void drawPlayerTank(float rot) {
		gl.glPushMatrix();
		gl.glColor3f(.1f, .1f, .1f);
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
		gl.glRotatef(-rot - 90, 0f, 1, 0f);
		gl.glTranslatef(0f, -.185f, 0f);

		MODELS.TANKBASE.Draw(gl);
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
		gl.glColor3f(.1f, 1f, .1f);
		MODELS.TANKBASE.Draw(gl);
		gl.glPopMatrix();
	}

	public void DrawUI() {
		
		// aiming reticle defining vertices
		Point center = new Point(width/2, height/2);
		float lx = (float)center.x - 100.f;
		float lh = (float)center.y - 50.f;
		float rx = (float)center.x + 100.f;
		float rh = (float)center.y + 50.f;
		
		// set up 2d drawing
		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluOrtho2D(0.0, width, height, 0.0);
	    gl.glMatrixMode(gl.GL_MODELVIEW);
	    gl.glLoadIdentity();
	    gl.glTranslatef(0.375f, 0.375f, 0.0f);
	    gl.glDisable(gl.GL_DEPTH_TEST);

	    // draw HUD
		gl.glBegin(gl.GL_LINE_LOOP);
		gl.glColor3f(1, 0, 0);
		gl.glVertex2d(lx, lh);
		gl.glVertex2d(rx, lh);
		gl.glVertex2d(rx, rh);
		gl.glVertex2d(lx, rh);
		gl.glEnd();
	    
		// set up 3d drawing
	    gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45.0f, (float) width / (float) height, znear, zfar);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
	    gl.glEnable(gl.GL_DEPTH_TEST);
		
	}

	public void Draw(Gamestate g) {

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glShadeModel(GL2.GL_FLAT);

		gl.glLoadIdentity();
		gl.glPushMatrix();

		Tank player = (Tank) Gamestate.getInstance().getPlayer();

		moveCamera(player.getLookDir().x , player.getLookDir().y);

		DrawBackground();

		drawPlayerTank(player.getBase().getDir().x);

		Vector3f pos = player.getBase().getPos();
		gl.glTranslatef(-pos.x, -pos.y, -pos.z);

		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
		DrawGeometry(new Vector3f(.1f, .1f, .1f));

		gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
		gl.glPolygonOffset(1, 1);
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
		DrawGeometry(null);

		gl.glPopMatrix();

		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);

		DrawUI();

	}

	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45.0f, (float) width / (float) height, znear, zfar);
		gl.glMatrixMode(GL2.GL_MODELVIEW);

	}

}
