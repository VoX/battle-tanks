package battletanks.game;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.vecmath.Vector3f;

import com.sun.opengl.util.awt.TextRenderer;

import battletanks.game.objects.DemoController;
import battletanks.game.objects.EnemyTankController;
import battletanks.game.objects.ExplosionCluster;
import battletanks.game.objects.GameObject;
import battletanks.game.objects.Obstacle;
import battletanks.game.objects.PlayerTankController;
import battletanks.game.objects.Tank;

public class Gamestate {

	private List<GameObject> obstacles;
	private List<GameObject> removelist;
	private List<GameObject> tanks;
	private List<GameObject> bullets;

	private List<GameObject> explosions;

	private GameObject player;

	private static int startTankCount = 0;
	private int tankCount, wave;
	private boolean roundover;
	int alivecount;

	private List<GameInput> playerInput;
	private int starthealth;
	long lasttime;
	private boolean gameover;

	private Gamestate(int wave, int health) {
		gameover = false;
		starthealth = health;
		alivecount =0;
		this.wave = wave;
		removelist = new ArrayList<GameObject>();
		lasttime = System.currentTimeMillis();
		obstacles = new ArrayList<GameObject>();
		tanks = new ArrayList<GameObject>();
		explosions = new ArrayList<GameObject>();

		roundover = false;
		bullets = new ArrayList<GameObject>();
		player = new Tank();
		playerInput = new ArrayList<GameInput>();
		if (wave == 0) {
			setUpMap();
			Tank et = new Tank();
			et.setController(new PlayerTankController(et));
	
			Vector3f pos = new Vector3f(-.75f,0,-4);
	
			et.getBase().getPhys().setPos(pos);
			et.getBase().getPhys().setDir(125f, 0);
			et.setTeam(2);
			addObject(et);
			
		} else {
			player.setController(new PlayerTankController((Tank) player));
			
			tankCount = wave + (int) Math.pow(wave, 1.2d);

			setUpMap();
		}
	}



	public void addObject(GameObject o) {
		if (o instanceof Obstacle) {
			obstacles.add(o);
		} else if (o instanceof Tank) {
			tanks.add(o);
		}

		else if (o instanceof ExplosionCluster) {
			explosions.add(o);
		} else {
			bullets.add(o);
		}

	}

	public void removeObject(GameObject o) {
		removelist.add(o);

	}

	public List<GameObject> getExplosions() {
		return explosions;
	}

	public GameObject getPlayer() {
		return player;
	}

	public List<GameObject> getObstacles() {
		return obstacles;

	}

	public List<GameObject> getTanks() {
		return tanks;

	}

	public List<GameObject> getBullets() {
		return bullets;

	}

	public CollisionResult doCollision(GameObject a, GameObject b) {
		return null;

	}

	public void AddInput(GameInput o) {
		playerInput.add(o);
	}

	public List<GameInput> getPlayerInput() {
		return playerInput;

	}

	public void UpdateState(long dtime) {

		int i;
		for (GameObject o : removelist) {
			if (o instanceof Obstacle) {
				i = obstacles.indexOf(o);
				if (i != -1)
					obstacles.remove(i);
			} else if (o instanceof Tank) {
				i = tanks.indexOf(o);
				if (i != -1)
					tanks.remove(i);
			} else if (o instanceof ExplosionCluster) {
				i = explosions.indexOf(o);
				if (i != -1)
					explosions.remove(i);
			}

			else {
				i = bullets.indexOf(o);
				if (i != -1)
					bullets.remove(i);
			}
		}

		long deltaTime = dtime - lasttime;
		Logger.getInstance().debugVal("Dtime", Float.toString(deltaTime));
		lasttime = dtime;
		if (deltaTime > 500) {
			deltaTime = 500;
		}

		this.alivecount = 0;
		for (GameObject go : tanks) {
			go.update(deltaTime);
			if (go.isDead() == false)
				this.alivecount++;

		}

		for (GameObject go : bullets) {
			go.update(deltaTime);
		}

		for (GameObject go : explosions) {
			go.update(deltaTime);
		}

		player.update(deltaTime);

		float x, y, z, d;
		CollisionResult cr;
		Vector3f overlap;
		float scalar;

		List<CollisionResult> tankObjCol = new ArrayList<CollisionResult>();
		for (GameObject go : tanks) {
			for (GameObject ob : obstacles) {
				x = ob.getBase().getPhys().getPos().x
						- go.getBase().getPhys().getPos().x;
				y = ob.getBase().getPhys().getPos().y
						- go.getBase().getPhys().getPos().y;
				z = ob.getBase().getPhys().getPos().z
						- go.getBase().getPhys().getPos().z;
				d = (float) Math.sqrt(x * x + y * y + z * z);
				if (d < (ob.getBase().getPhys().getRadius() + go.getBase()
						.getPhys().getRadius())) {
					cr = new CollisionResult();
					cr.setCollided(ob);
					overlap = new Vector3f(ob.getBase().getPhys().getPos());
					overlap.sub(go.getBase().getPhys().getPos());
					overlap.normalize();
					scalar = ob.getBase().getPhys().getRadius()
							+ go.getBase().getPhys().getRadius() - d;
					overlap.scale(scalar);
					cr.setVector(overlap);
					tankObjCol.add(cr);
				}
			}
			for (GameObject ob : tanks) {
				x = ob.getBase().getPhys().getPos().x
						- go.getBase().getPhys().getPos().x;
				y = ob.getBase().getPhys().getPos().y
						- go.getBase().getPhys().getPos().y;
				z = ob.getBase().getPhys().getPos().z
						- go.getBase().getPhys().getPos().z;
				d = (float) Math.sqrt(x * x + y * y + z * z);
				if (d < (ob.getBase().getPhys().getRadius() + go.getBase()
						.getPhys().getRadius())) {
					cr = new CollisionResult();
					cr.setCollided(ob);
					overlap = new Vector3f(ob.getBase().getPhys().getPos());
					overlap.sub(go.getBase().getPhys().getPos());
					overlap.normalize();
					scalar = ob.getBase().getPhys().getRadius()
							+ go.getBase().getPhys().getRadius() - d;
					overlap.scale(scalar);
					cr.setVector(overlap);
					tankObjCol.add(cr);
				}
			}
			CollisionResult c = new CollisionResult();
			Vector3f max = new Vector3f(0, 0, 0);
			for (CollisionResult r : tankObjCol) {
				if (Math.abs(r.getOverlapVector().x) > Math.abs(max.x)) {
					max.x = r.getOverlapVector().x;
					c.collidedWith = r.getCollided();
				}
				if (Math.abs(r.getOverlapVector().z) > Math.abs(max.z)) {
					max.z = r.getOverlapVector().z;
				}
			}
			c.setVector(max);
			if (!tankObjCol.isEmpty())
				go.doCollision(c);
			tankObjCol.clear();
		}

		List<CollisionResult> playerObjCol = new ArrayList<CollisionResult>();
		// player collision detection
		for (GameObject ob : tanks) {
			x = ob.getBase().getPhys().getPos().x
					- player.getBase().getPhys().getPos().x;
			y = ob.getBase().getPhys().getPos().y
					- player.getBase().getPhys().getPos().y;
			z = ob.getBase().getPhys().getPos().z
					- player.getBase().getPhys().getPos().z;
			d = (float) Math.sqrt(x * x + y * y + z * z);
			if (d < (ob.getBase().getPhys().getRadius() + player.getBase()
					.getPhys().getRadius())) {
				cr = new CollisionResult();
				cr.setCollided(ob);
				overlap = new Vector3f(ob.getBase().getPhys().getPos());
				overlap.sub(player.getBase().getPhys().getPos());
				overlap.normalize();
				scalar = ob.getBase().getPhys().getRadius()
						+ player.getBase().getPhys().getRadius() - d;
				overlap.scale(scalar);
				cr.setVector(overlap);
				playerObjCol.add(cr);
			}
		}
		for (GameObject ob : obstacles) {
			x = ob.getBase().getPhys().getPos().x
					- player.getBase().getPhys().getPos().x;
			y = ob.getBase().getPhys().getPos().y
					- player.getBase().getPhys().getPos().y;
			z = ob.getBase().getPhys().getPos().z
					- player.getBase().getPhys().getPos().z;
			d = (float) Math.sqrt(x * x + y * y + z * z);
			if (d < (ob.getBase().getPhys().getRadius() + player.getBase()
					.getPhys().getRadius())) {
				cr = new CollisionResult();
				cr.setCollided(ob);
				overlap = new Vector3f(ob.getBase().getPhys().getPos());
				overlap.sub(player.getBase().getPhys().getPos());
				overlap.normalize();
				scalar = ob.getBase().getPhys().getRadius()
						+ player.getBase().getPhys().getRadius() - d;
				overlap.scale(scalar);
				cr.setVector(overlap);
				playerObjCol.add(cr);
			}
		}
		CollisionResult c = new CollisionResult();
		Vector3f max = new Vector3f(0, 0, 0);
		for (CollisionResult r : playerObjCol) {
			if (Math.abs(r.getOverlapVector().x) > Math.abs(max.x)) {
				max.x = r.getOverlapVector().x;
				c.collidedWith = r.getCollided();
			}
			if (Math.abs(r.getOverlapVector().z) > Math.abs(max.z)) {
				max.y = r.getOverlapVector().z;
			}
		}
		c.setVector(max);
		if (!playerObjCol.isEmpty())
			player.doCollision(c);
		playerObjCol.clear();

		cr = new CollisionResult();
		for (GameObject go : bullets) {
			if(go.isDead())
				continue;
			x = go.getBase().getPhys().getPos().x
					- player.getBase().getPhys().getPos().x;
			y = go.getBase().getPhys().getPos().y
					- player.getBase().getPhys().getPos().y;
			z = go.getBase().getPhys().getPos().z
					- player.getBase().getPhys().getPos().z;
			d = (float) Math.sqrt(x * x + y * y + z * z);
			if (d < (go.getBase().getPhys().getRadius() + player.getBase()
					.getPhys().getRadius())) {
				cr.setCollided(go);
				player.doCollision(cr);
				cr.setCollided(player);
				go.doCollision(cr);
				continue;
			}
			for (GameObject et : tanks) {
				x = go.getBase().getPhys().getPos().x
						- et.getBase().getPhys().getPos().x;
				y = go.getBase().getPhys().getPos().y
						- et.getBase().getPhys().getPos().y;
				z = go.getBase().getPhys().getPos().z
						- et.getBase().getPhys().getPos().z;
				d = (float) Math.sqrt(x * x + y * y + z * z);
				if (d < (go.getBase().getPhys().getRadius() + et.getBase()
						.getPhys().getRadius())) {
					cr.setCollided(go);
					et.doCollision(cr);
					cr.setCollided(et);
					go.doCollision(cr);
					break;
				}
			}
			for (GameObject ob : obstacles) {
				x = go.getBase().getPhys().getPos().x
						- ob.getBase().getPhys().getPos().x;
				y = go.getBase().getPhys().getPos().y
						- ob.getBase().getPhys().getPos().y;
				z = go.getBase().getPhys().getPos().z
						- ob.getBase().getPhys().getPos().z;
				d = (float) Math.sqrt(x * x + y * y + z * z);
				if (d < (go.getBase().getPhys().getRadius() + ob.getBase()
						.getPhys().getRadius())) {
					cr.setCollided(go);
					ob.doCollision(cr);
					cr.setCollided(ob);
					go.doCollision(cr);
					break;
				}
			}
		}
		
		if(((Tank)player).getHealth() <= 0){
			this.gameover  = true;
		
		}

		if (alivecount == 0) {
			roundover = true;
		}

	}

	public void nextRound() {
		if(gameover)
			reset();
		
		else if (roundover || wave == 0) {
			nextWave();
		}
	}

	public boolean isRoundover() {
		return roundover;
	}

	public void setUpMap() {

		Obstacle ob;
		Tank et;
		Random rf = new Random(System.nanoTime());
		// obstacles
		Vector3f tmp, pos;
		boolean foundspot;
		
		obstacles.clear();
		Obstacle zero = new Obstacle(1);
		zero.getBase().getPhys().setPos(new Vector3f(0,0,0));

		addObject(zero);
		
		for (int i = 0; i < 75; i++) {
			float size = 1f;
			ob = new Obstacle(size);
			do {
				foundspot = true;
				pos = new Vector3f(rf.nextFloat() * 80f - 40f,
						size / 2f - .145f, rf.nextFloat() * 80f - 40f);
				for (GameObject o : obstacles) {
					tmp = new Vector3f(o.getBase().getPhys().getPos());
					tmp.sub(pos);
					if (tmp.length() < 5) {
						foundspot = false;
					}
				}
			} while (!foundspot);

			ob.getBase().getPhys().setPos(pos);
			ob.getBase().getPhys().setDir(0, rf.nextFloat() * 180f);
			ob.setTeam(3);
			addObject(ob);
		}
		removeObject(zero);
		// enemy tanks
		for (int i = 0; i < tankCount; i++) {
			et = new Tank();
			et.setController(new EnemyTankController(et));
			do {
				pos = new Vector3f(rf.nextFloat() * 80f - 40f, 0f,
						rf.nextFloat() * 80f - 40f);
			} while (pos.length() < 35);
			et.getBase().getPhys().setPos(pos);
			et.getBase().getPhys().setDir(rf.nextFloat() * 180, 0);
			et.setTeam(2);
			addObject(et);
		}
		// player
		player.getBase().getPhys().setPos(0, 0, 0);
		player.getBase().getPhys().setDir(0, 0);
		player.setTeam(1);

		((Tank) player).setHealth(starthealth);

	}

	public void nextWave() {
		Gamestate.instance = new Gamestate(wave + 1, starthealth +1);
	}

	public int getWave() {
		return wave;
	}

	public int getTankCount() {
		return alivecount;
	}

	public void reset() {
		Gamestate.instance = new Gamestate(startTankCount, 2);

	}



	private static Gamestate instance = null;

	public static Gamestate getInstance() {
		if (Gamestate.instance == null)
			Gamestate.instance = new Gamestate(startTankCount, 2);

		return Gamestate.instance;
	}



	public boolean isGameOver() {
		return gameover;
	}

}
