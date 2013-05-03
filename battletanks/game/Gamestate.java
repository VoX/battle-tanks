package battletanks.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import battletanks.game.objects.EnemyTank;
import battletanks.game.objects.GameObject;
import battletanks.game.objects.Obstacle;
import battletanks.game.objects.PlayerTank;


public class Gamestate {
	
	private List<GameObject> obstacles;
	private List<GameObject> enemies;
	private List<GameObject> bullets;
	
	private PlayerTank player;
	private List<GameInput> playerInput;
	
	long lasttime;
	
	private Gamestate(){
		lasttime = System.currentTimeMillis();
		obstacles = new ArrayList<GameObject>();
		enemies = new ArrayList<GameObject>();
		bullets = new ArrayList<GameObject>();
		player = new PlayerTank();
		playerInput = new ArrayList<GameInput>();
		setUpMap();
	}
	
	public void addObject(GameObject o){
		if(o instanceof Obstacle){
			obstacles.add(o);
		}
		else if(o instanceof EnemyTank){
			enemies.add(o);
		}
		else{
			bullets.add(o);
		}
		
	}
	
	public void removeObject(GameObject o){
		if(o instanceof Obstacle){
			obstacles.remove(obstacles.indexOf(o));
		}
		else if(o instanceof EnemyTank){
			enemies.remove(enemies.indexOf(o));
		}
		else{
			bullets.remove(bullets.indexOf(o));
		}
	}
	
	public GameObject getPlayer(){
		return player;
	}
	
	public List<GameObject> getObstacles(){
		return obstacles;
		
	}
	
	public List<GameObject> getEnemies(){
		return enemies;
		
	}
	
	public List<GameObject> getBullets(){
		return bullets;
		
	}
	
	public CollisionResult doCollision(GameObject a, GameObject b){
		return null;
		
	}
	
	public void AddInput(GameInput o){
		playerInput.add(o);
	}
	
	private void processInput(){
		INPUT_TYPE input;
		for (GameInput o : playerInput) {
			Logger.getInstance().debugVal("LastKey" , o.getInputType().name());
			Logger.getInstance().Log("LastKey:" + o.getInputType().name());
			input = o.getInputType();
			switch (input) {
			
			case FORWARD_PRESSED:
				player.moveForward();
				break;
			case BACKWARD_PRESSED:
				player.moveBackward();
				break;
			case LEFT_PRESSED:
				player.turnLeft();
				break;
			case RIGHT_PRESSED:
				player.turnRight();
				break;
			case FIRE_PRESSED:
				break;
			case FORWARD_RELEASED:
				player.stopForwards();
				break;
			case BACKWARD_RELEASED:
				player.stopBackwards();
				break;
			case LEFT_RELEASED:
				player.stopLeft();
				break;
			case RIGHT_RELEASED:
				player.stopRight();
				break;
			case FIRE_RELEASED:
				//player stop
				break;
			}
		}
		playerInput.clear();
	}
	
	public void UpdateState(long dtime) {
		long deltaTime = dtime - lasttime;
		Logger.getInstance().debugVal("Dtime" , Float.toString(deltaTime));
		lasttime = dtime;
		if (deltaTime > 500){
			deltaTime = 500;
		}
		
		processInput();
		
		for(GameObject go : enemies){
			//go.update(deltaTime);
		}
		
		player.update(deltaTime);
		

	}

	public void setUpMap(){
		Obstacle ob;
		EnemyTank et;
		Random rf = new Random(System.nanoTime());
		// obstacles
		for(int i = 0; i < 30; i++){
			ob = new Obstacle();
			ob.setPos(rf.nextFloat() * 20 - 10, 0, rf.nextFloat() * 20 - 10);
			ob.setDir(0, rf.nextFloat() * 180);
			addObject(ob);
		}
		// enemy tanks
		for(int i = 0; i < 5; i++){
			et = new EnemyTank();
			et.setPos(rf.nextFloat() * 20 - 10, 0, rf.nextFloat() * 20 - 10);
			et.setDir(0, rf.nextFloat() * 180);
			addObject(et);
		}
		// player
		player.setPos(0, 0, 0);
		player.setDir(0, 0);
		
	}
	
	public void reset(){
		Gamestate.instance = null;
	}
	
	private static Gamestate instance = null;
	
	public static Gamestate getInstance(){
		if (Gamestate.instance == null)
			Gamestate.instance = new Gamestate();
		
		return Gamestate.instance;
	}
	

}

