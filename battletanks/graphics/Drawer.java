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
import battletanks.game.objects.ExplosionCluster;
import battletanks.game.objects.ExplosionPoint;
import battletanks.game.objects.GameObject;
import battletanks.game.objects.Obstacle;
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
	private boolean showBoundSphere;
	int lasthealth = 0;
	int hitfade = 0;

	public Drawer(GL2 gl2, GLU glu, GLUT glut) {

		this.gl = gl2;
		this.glu = glu;
		this.glut = glut;
		width = 1440;
		height = 900;

		znear = 0.01f;
		zfar = 900.0f;

		showBoundSphere = false;

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
			Obstacle o = (Obstacle) ob;
			gl.glPushMatrix();
			gl.glTranslatef(ob.getBase().getPos().x, ob.getBase().getPos().y,
					ob.getBase().getPos().z);
			gl.glRotatef(-ob.getBase().getDir().y, 0, 1.0f, 0);
			gl.glRotatef(-ob.getBase().getDir().x, 1.0f, 0, 0f);
			glut.glutSolidCube(o.getSize());
			if (showBoundSphere)
				glut.glutSolidSphere(ob.getBase().getPhys().getRadius(), 20, 20);
			gl.glPopMatrix();
		}

		for (GameObject ob : Gamestate.getInstance().getTanks()) {
			Tank t = (Tank) ob;
			int hp = t.getHealth();

			if (color == null)
				if (t.isDead() == true)
					gl.glColor3f(.2f, .2F, .2F);
				else
					gl.glColor3f(hp * .2f + .4f, 0.0F, .0F);
			else
				gl.glColor3f(color.x, color.y, color.z);

			for (Part p : ob.getParts()) {
				gl.glPushMatrix();
				gl.glTranslatef(p.getPos().x, p.getPos().y, p.getPos().z);
				gl.glRotatef(270, 0, 1, 0f);
				gl.glRotatef(-p.getDir().x, 0, 1.0f, 0f);
				gl.glRotatef(p.getDir().y, 0.0f, 0, 1);

				gl.glTranslatef(p.getCenterrot().x, p.getCenterrot().y,
						p.getCenterrot().z);

				p.getModel().Draw(gl);
				if (showBoundSphere)
					glut.glutSolidSphere(p.getPhys().getRadius(), 20, 20);
				gl.glPopMatrix();
			}
		}

		gl.glPopMatrix();
	}

	private void DrawBackground() {
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
		gl.glColor3f(.13f, .13f, .13f);
		gl.glPushMatrix();
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(-500.0f, -0.321f, -500.0f);
		gl.glVertex3f(-500.0f, -0.321f, 500.0f);
		gl.glVertex3f(500.0f, -0.321f, 500.0f);
		gl.glVertex3f(500.0f, -0.321f, -500.0f);

		gl.glEnd();

		gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
		gl.glPolygonOffset(1, 1);

		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
		gl.glColor3f(.7f, .7f, .7f);

		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(-500.0f, -0.321f, -500.0f);
		gl.glVertex3f(-500.0f, -0.321f, 500.0f);
		gl.glVertex3f(500.0f, -0.321f, 500.0f);
		gl.glVertex3f(500.0f, -0.321f, -500.0f);

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
		Point center = new Point(width / 2, height / 2);
		float lx = (float) center.x - 100.f;
		float lh = (float) center.y - 50.f;
		float rx = (float) center.x + 100.f;
		float rh = (float) center.y + 50.f;

		float l2x = (float) center.x - 50.f;
		float l2h = (float) center.y - 25.f;
		float r2x = (float) center.x + 50.f;
		float r2h = (float) center.y + 25.f;

		// set up 2d drawing
		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluOrtho2D(0.0, width, height, 0.0);
		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0.375f, 0.375f, 0.0f);
		gl.glDisable(gl.GL_DEPTH_TEST);

		gl.glEnable(gl.GL_BLEND);
		gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);

		gl.glPointSize(7f);
		gl.glEnable(gl.GL_POINT_SMOOTH);

		Vector3f p = Gamestate.getInstance().getPlayer().getBase().getPos();
		float dirp = Gamestate.getInstance().getPlayer().getBase().getDir().x + 90;

		gl.glBegin(gl.GL_POINTS);
		gl.glColor4f(0, 1, 0, .5f);
		gl.glVertex2f(0 + width / 2, 150f);
		gl.glEnd();

		for (GameObject go : Gamestate.getInstance().getTanks()) {
			Vector3f t = go.getBase().getPos();
			float x = (t.x - p.x) * 2f;
			float y = (t.z - p.z) * 2f;

			float angle = (float) Math.toRadians(dirp
					- Math.toDegrees(Math.atan2(y, x)));

			float r = (float) Math.sqrt(x * x + y * y);

			x = (float) Math.sin(angle) * r;
			y = (float) Math.cos(angle) * r;

			if (x > 100f || x < -100f || y > 100f || y < -100f) {

			} else {

				if (go.isDead() == true)
					gl.glColor4f(.6f, .6f, .6f, .5f);
				else
					gl.glColor4f(1, 0, 0, .5f);

				gl.glBegin(gl.GL_POINTS);
				gl.glVertex2f(x + width / 2, y + 150f);
				gl.glEnd();
			}
		}
		
		gl.glDisable(gl.GL_POINT_SMOOTH);
		for (GameObject go : Gamestate.getInstance().getObstacles()) {
			Vector3f t = go.getBase().getPos();
			float x = (t.x - p.x) * 2f;
			float y = (t.z - p.z) * 2f;

			float angle = (float) Math.toRadians(dirp
					- Math.toDegrees(Math.atan2(y, x)));

			float r = (float) Math.sqrt(x * x + y * y);

			x = (float) Math.sin(angle) * r;
			y = (float) Math.cos(angle) * r;

			if (x > 100f || x < -100f || y > 100f || y < -100f) {

			} else {

			
				gl.glColor4f(1f, 2f, 1f, .1f);

				gl.glBegin(gl.GL_POINTS);
				gl.glVertex2f(x + width / 2, y + 150f);
				gl.glEnd();
			}
		}
		
		gl.glColor4f(0, 1, 0, .5f);

		// draw HUD
		// aiming reticle
		gl.glBegin(gl.GL_LINES);

		gl.glVertex2f(lx, lh);
		gl.glVertex2f(l2x, l2h);
		gl.glEnd();
		gl.glBegin(gl.GL_LINES);

		gl.glVertex2f(lx, rh);
		gl.glVertex2f(l2x, r2h);
		gl.glEnd();

		gl.glBegin(gl.GL_LINES);

		gl.glVertex2f(rx, rh);
		gl.glVertex2f(r2x, r2h);
		gl.glEnd();
		gl.glBegin(gl.GL_LINES);

		gl.glVertex2f(rx, lh);
		gl.glVertex2f(r2x, l2h);
		gl.glEnd();
		// firing status

		gl.glEnable(gl.GL_BLEND);
		gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);

		gl.glColor4f(0, 1, 0, .3f);

		gl.glBegin(gl.GL_LINE_LOOP);
		rx += 60;
		gl.glVertex2d(rx + 20, lh - 45);
		gl.glVertex2d(rx + 50, lh - 45);
		gl.glVertex2d(rx + 50, lh + 135);
		gl.glVertex2d(rx + 20, lh + 135);
		gl.glEnd();
		Tank player = (Tank) Gamestate.getInstance().getPlayer();
		float per = player.getReloadPer();
		gl.glEnable(gl.GL_BLEND);
		gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
		gl.glBegin(gl.GL_QUADS);
		gl.glColor4f(0, 1, 0, .1f);
		gl.glVertex2f(rx + 20, lh + 135);
		gl.glVertex2f(rx + 50, lh + 135);
		gl.glVertex2f(rx + 50, lh + 135 - (per * 180.f));
		gl.glVertex2f(rx + 20, lh + 135 - (per * 180.f));
		gl.glEnd();

		gl.glColor4f(0, 1, 0, .3f);

		gl.glBegin(gl.GL_LINE_LOOP);

		gl.glVertex2f(center.x - 100, 150 + 100);
		gl.glVertex2f(center.x + 100, 150 + 100);
		gl.glVertex2f(center.x + 100, 150 - 100);
		gl.glVertex2f(center.x - 100, 150 - 100);
		gl.glEnd();

		gl.glBegin(gl.GL_QUADS);
		gl.glColor4f(0, 1, 0, .1f);
		gl.glVertex2f(center.x - 100, 150 + 100);
		gl.glVertex2f(center.x + 100, 150 + 100);
		gl.glVertex2f(center.x + 100, 150 - 100);
		gl.glVertex2f(center.x - 100, 150 - 100);
		gl.glEnd();

		// player health status
		int hp = player.getHealth();
		float startX = center.x - hp / 2f * 60f;
		float startY = rh + 300f;
		for (int i = 0; i < hp; i++) {
			gl.glEnable(gl.GL_BLEND);
			gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
			gl.glBegin(gl.GL_QUADS);
			gl.glColor4f(0, 1, 0, .4f);

			gl.glVertex2f(startX, startY + 50);
			gl.glVertex2f(startX + 50, startY + 50);
			gl.glVertex2f(startX + 50, startY);
			gl.glVertex2f(startX, startY);
			gl.glEnd();
			startX += 60;
		}

		if (lasthealth > hp) {

			hitfade = 35;

		}
		lasthealth = hp;

		if (hitfade > 0) {

			gl.glEnable(gl.GL_BLEND);
			gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
			gl.glBegin(gl.GL_QUADS);
			gl.glColor4f(1, 0, 0, (float) hitfade / 70f);

			gl.glVertex2f(0, 0);
			gl.glVertex2f(0, height);
			gl.glVertex2f(width, height);
			gl.glVertex2f(width, 0);
			gl.glEnd();
			hitfade--;
		}

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

		moveCamera(player.getLookDir().x, player.getLookDir().y);

		DrawBackground();

		drawPlayerTank(player.getBase().getDir().x);

		Vector3f pos = player.getTurret().getPos();
		gl.glTranslatef(-pos.x, -pos.y, -pos.z);

		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
		DrawGeometry(new Vector3f(.1f, .1f, .1f));

		gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
		gl.glPolygonOffset(1, 1);
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);

		DrawGeometry(null);
		DrawLineGeometry();

		gl.glPopMatrix();

		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);

		DrawUI();

	}

	private void DrawLineGeometry() {

		gl.glEnable(gl.GL_BLEND);
		gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);

		for (GameObject ob : Gamestate.getInstance().getBullets()) {
			Bullet b = (Bullet) ob;
			Iterator<Vector3f> it = b.getOldPos();
			Iterator<Vector2f> id = b.getOldDir();
			int c = 0;

			float acolor = 1f - (float) b.getAge() / 30f;
			while (c < 40 && it.hasNext()) {

				Vector3f p = it.next();
				Vector2f d = id.next();
				acolor = acolor - 0.025f;
				if (acolor <= 0.f)
					acolor = 0.f;
				else {

					gl.glColor4f(0, 1, 1, acolor);
					c++;

					gl.glPushMatrix();
					gl.glTranslatef(p.x, p.y, p.z);

					gl.glRotatef(270, 0, 1, 0f);

					gl.glRotatef(-d.x, 0, 1.0f, 0f);
					gl.glRotatef(d.y, 0.0f, 0, 1);

					gl.glRotatef(90, 0, 1, 0f);
					ob.getBase().getModel().Draw(gl);
					if (showBoundSphere)
						glut.glutSolidSphere(
								ob.getBase().getPhys().getRadius(), 5, 5);

					gl.glPopMatrix();
				}
			}
		}

		for (GameObject ob : Gamestate.getInstance().getExplosions()) {
			ExplosionCluster t = (ExplosionCluster) ob;
			for (Part p : ob.getParts()) {
				ExplosionPoint e = (ExplosionPoint) p;

				gl.glColor3f(e.getColor().x, e.getColor().y, e.getColor().z);

				gl.glPushMatrix();
				gl.glTranslatef(e.getPos().x, e.getPos().y, e.getPos().z);
				gl.glRotatef(270, 0, 1, 0f);
				gl.glRotatef(-e.getDir().x, 0, 1.0f, 0f);
				gl.glRotatef(e.getDir().y, 0.0f, 0, 1);

				glut.glutSolidSphere(e.getScale(), 20, 20);

				if (showBoundSphere)
					glut.glutSolidSphere(p.getPhys().getRadius(), 20, 20);
				gl.glPopMatrix();
			}
		}

	}

	public void setShowBoundSphere(boolean value) {
		showBoundSphere = value;
	}

	public boolean getShowBoundSphere() {
		return showBoundSphere;
	}

	public void drawUIText(TextRenderer renderer) {
		// renderer.draw("HEALTH", 30, height - 25);
		renderer.draw("Press Q to quit ",
				10, height - 45);
		
		
		renderer.draw("WAVE: " + Gamestate.getInstance().getWave(),
				width - 350, height - 45);
		if (Gamestate.getInstance().getWave() == 0) {
			renderer.draw("PRESS SPACE TO START", width / 2 - 200,
					height / 2 + 150);
			renderer.draw("CONTROLS", width / 2 - 650, height / 2 + 300);
			renderer.draw("Move: ", width / 2 - 650, height / 2 + 260);
			renderer.draw("Arrow Keys", width / 2 - 525, height / 2 + 260);
			renderer.draw("W A S D", width / 2 - 525, height / 2 + 220);
			renderer.draw("Fire: ", width / 2 - 650, height / 2 + 180);
			renderer.draw("Left Click", width / 2 - 525, height / 2 + 180);
			renderer.draw("Aim: ", width / 2 - 650, height / 2 + 140);
			renderer.draw("Mouse", width / 2 - 525, height / 2 + 140);
			renderer.draw("HEALTH", width / 2 - 60, 185);
			
			renderer.draw("BY:", width - 300, 150);
			renderer.draw("ADAM DAEHLING", width - 300, 110);
			renderer.draw("IAN HILLS", width - 300, 70);
		}

		else if (Gamestate.getInstance().isGameOver()) {
			renderer.draw("YOU DIED", width / 2 - 75, height / 2 + 200);

			renderer.draw("PRESS SPACE TO RESTART", width / 2 - 215,
					height / 2 + 150);
		}

		else if (Gamestate.getInstance().isRoundover()) {
			renderer.draw("WAVE COMPLETE", width / 2 - 125, height / 2 + 200);
			renderer.draw("PRESS SPACE TO CONTINUE", width / 2 - 215,
					height / 2 + 150);
		}

		renderer.draw(
				"ENEMIES LEFT: " + Gamestate.getInstance().getTankCount(),
				width - 350, height - 95);
		
		


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
