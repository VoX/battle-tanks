package battletanks.game.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import battletanks.game.CollisionResult;
import battletanks.game.Gamestate;
import battletanks.game.Logger;
import battletanks.graphics.MODELS;

public class Bullet extends GameObjectImp {

	private float bulletSpeed = 1.2f;
	private Vector3f gravity = new Vector3f(0, -.0005f, 0);

	private LinkedList<Vector3f> oldPos;
	private LinkedList<Vector2f> oldDir;
	private int count;
	private int team;

	public Bullet() {
		super();
		base = new Part(MODELS.BULLET);
		parts.add(base);
		base.getPhys().setDragconst(0.0f);
		base.getPhys().setMaxvel(10f);
		base.getPhys().setMaxaccel(10.0f);
		base.getPhys().setRadius(.15f);


		oldPos = new LinkedList<Vector3f>();
		oldDir = new LinkedList<Vector2f>();

	}

	public void fire(Vector3f origin, Vector2f direction) {

		this.base.getPhys().setPos(origin);
		this.base.getPhys().setDir(direction);

		double radphi = Math.toRadians(direction.y);
		double radtheta = Math.toRadians(direction.x);
		this.base.getPhys().setRotdragconst(0);
		//this.base.getPhys().setDirSpeed(0f, 1f);
		
		Vector3f v = new Vector3f();

		v.x = +(float) Math.sin(radtheta) * bulletSpeed;
		v.z = -(float) (Math.cos(radtheta)) * bulletSpeed;
		v.y = -(float) (Math.sin(radphi)) * bulletSpeed;

		this.base.getPhys().setVel(v);

		this.base.getPhys().setAccel(gravity);
		count = 0;

	}

	public Iterator<Vector3f> getOldPos() {
		return oldPos.descendingIterator();
	}

	public Iterator<Vector2f> getOldDir() {
		return oldDir.descendingIterator();
	}

	public void update(long dtime) {

		//Logger.getInstance().Log("bulletpos:" + this.base.getPhys().getPos());
		this.base.getPhys().setAccel(gravity);
		if (this.base.getPos().y < -.5) {
			Gamestate.getInstance().removeObject(this);
		}
		
		

		super.update(dtime);
		
		Vector3f v = this.base.getPhys().getVel();
		Vector2f d = new Vector2f();
		float r = (float) Math.sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
		
		//d.x = (float) Math.toDegrees(Math.acos(v.z/r));
		d.y = (float) Math.toDegrees(Math.atan2(v.y,v.x));
		
		
		
		//this.base.getPhys().setDir(d);


		oldPos.add(new Vector3f(this.base.getPhys().getPos()));
		oldDir.add(new Vector2f(this.base.getPhys().getDir()));

	}

	@Override
	public void doCollision(CollisionResult c) {
		Gamestate.getInstance().removeObject(this);
		
	}

	@Override
	public int getTeam() {
		return team;
	}

	@Override
	public void setTeam(int i) {
		team = i;
	}

}
