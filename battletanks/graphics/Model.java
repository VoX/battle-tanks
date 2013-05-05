package battletanks.graphics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.sun.opengl.util.BufferUtil;

public class Model {
	public FloatBuffer vertexBuffer;
	public IntBuffer faceBuffer;
	public FloatBuffer normalBuffer;
	public Point3f center;
	public int num_verts; // number of vertices
	public int num_faces; // number of triangle faces
	
	public float rotx = 0;
	public float roty = 0;
	private float scale = 6;
	public FloatBuffer qvertexBuffer;
	public IntBuffer qfaceBuffer;
	public FloatBuffer qnormalBuffer;
	public int qnum_verts; // number of vertices
	public int qnum_faces; // number of triangle faces


	public void Draw(GL2 gl) {
		
		gl.glRotatef(rotx, 0, 1, 0);
		gl.glRotatef(roty, 1, 0, 0);
		
		vertexBuffer.rewind();
		normalBuffer.rewind();
		faceBuffer.rewind();
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);

		gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertexBuffer);
		gl.glNormalPointer(GL.GL_FLOAT, 0, normalBuffer);

		gl.glDrawElements(GL2.GL_TRIANGLES, num_faces * 3, GL2.GL_UNSIGNED_INT,
				faceBuffer);

		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		

		qvertexBuffer.rewind();
		qnormalBuffer.rewind();
		qfaceBuffer.rewind();
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);

		gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertexBuffer);
		gl.glNormalPointer(GL.GL_FLOAT, 0, qnormalBuffer);

		gl.glDrawElements(GL2.GL_QUADS, qnum_faces * 4, GL2.GL_UNSIGNED_INT,
				qfaceBuffer);

		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
	}

	public Model(String filename,float rotx,float roty,float scale) {
		/* load a triangular mesh model from a .obj file */
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filename));
		} catch (IOException e) {
			System.out.println("Error reading from file " + filename);
			System.exit(0);
		}

		center = new Point3f();
		float x, y, z;
		int v1, v2, v3, v4;
		float minx, miny, minz;
		float maxx, maxy, maxz;
		float bbx, bby, bbz;
		minx = miny = minz = 10000.f;
		maxx = maxy = maxz = -10000.f;

		String line;
		String[] tokens;
		ArrayList<Point3f> input_verts = new ArrayList<Point3f>();
		ArrayList<Point3f> qinput_verts = new ArrayList<Point3f>();
		ArrayList<Integer> input_faces = new ArrayList<Integer>();
		ArrayList<Integer> qinput_faces = new ArrayList<Integer>();
		ArrayList<Vector3f> input_norms = new ArrayList<Vector3f>();
		ArrayList<Vector3f> qinput_norms = new ArrayList<Vector3f>();
		try {
			while ((line = in.readLine()) != null) {
				if (line.length() == 0)
					continue;
				switch (line.charAt(0)) {
				case 'v':
					tokens = line.split("[ ]+");
					x = Float.valueOf(tokens[1]);
					y = Float.valueOf(tokens[2]);
					z = Float.valueOf(tokens[3]);
					minx = Math.min(minx, x);
					miny = Math.min(miny, y);
					minz = Math.min(minz, z);
					maxx = Math.max(maxx, x);
					maxy = Math.max(maxy, y);
					maxz = Math.max(maxz, z);
				
					input_verts.add(new Point3f(x, y, z));
					
					center.add(new Point3f(x, y, z));
					break;
				case 'f':
					tokens = line.split("[ ]+");

					v1 = Integer.valueOf(tokens[1]) - 1;
					v2 = Integer.valueOf(tokens[2]) - 1;
					v3 = Integer.valueOf(tokens[3]) - 1;
					if (tokens.length == 5) {
						v4 = Integer.valueOf(tokens[4]) - 1;
						qinput_faces.add(v1);
						qinput_faces.add(v2);
						qinput_faces.add(v3);
						qinput_faces.add(v4);
						qinput_verts.add(input_verts.get(v1));
						qinput_verts.add( input_verts.get(v2));
						qinput_verts.add( input_verts.get(v3));
						qinput_verts.add( input_verts.get(v4));
					} else {
						
						input_faces.add(v1);
						input_faces.add(v2);
						input_faces.add(v3);
					}
					break;
				default:
					continue;
				}
			}
			in.close();
		} catch (IOException e) {
			System.out.println("Unhandled error while reading input file.");
		}

		System.out.println("Read " + input_verts.size() + " vertices and "
				+ input_faces.size() + " faces.");

		center.scale(1.f / (float) input_verts.size());

		bbx = maxx - minx;
		bby = maxy - miny;
		bbz = maxz - minz;
		float bbmax = Math.max(bbx, Math.max(bby, bbz));
		bbmax = scale;
		for (Point3f p : input_verts) {

			p.x = (p.x - center.x) / bbmax;
			p.y = (p.y - center.y) / bbmax;
			p.z = (p.z - center.z) / bbmax;
		}
		center.x = center.y = center.z = 0.f;

		/* estimate per vertex average normal */
		int i;
		for (i = 0; i < input_verts.size(); i++) {
			input_norms.add(new Vector3f());
		}

		Vector3f e1 = new Vector3f();
		Vector3f e2 = new Vector3f();
		Vector3f tn = new Vector3f();
		for (i = 0; i < input_faces.size(); i += 3) {
			v1 = input_faces.get(i + 0);
			v2 = input_faces.get(i + 1);
			v3 = input_faces.get(i + 2);

			e1.sub(input_verts.get(v2), input_verts.get(v1));
			e2.sub(input_verts.get(v3), input_verts.get(v1));
			tn.cross(e1, e2);
			input_norms.get(v1).add(tn);

			e1.sub(input_verts.get(v3), input_verts.get(v2));
			e2.sub(input_verts.get(v1), input_verts.get(v2));
			tn.cross(e1, e2);
			input_norms.get(v2).add(tn);

			e1.sub(input_verts.get(v1), input_verts.get(v3));
			e2.sub(input_verts.get(v2), input_verts.get(v3));
			tn.cross(e1, e2);
			input_norms.get(v3).add(tn);

		}
		

		for (i = 0; i < qinput_verts.size(); i++) {
			qinput_norms.add(new Vector3f());
		}

		for (int q = 0; q < qinput_faces.size(); q += 4) {

			Vector3f n = qinput_norms.get(q);
			for (int h = 0; h < 4; h++) {
				int q1 = qinput_faces.get(q + h);
				Point3f q2 = qinput_verts.get(q1);

				int q3 = qinput_faces.get((q + h + 1) % 4);
				Point3f q4 = qinput_verts.get(q3);

				Vector3f p = new Vector3f(q2.x, q2.y, q2.z);
				Vector3f t = new Vector3f(q4.x, q4.y, q4.z);
				p.cross(p, t);
				n.add(p);

			}
			n.normalize();

		}

		/* convert to buffers to improve display speed */
		for (i = 0; i < input_verts.size(); i++) {
			input_norms.get(i).normalize();
		}

		vertexBuffer = BufferUtil.newFloatBuffer(input_verts.size() * 3);
		normalBuffer = BufferUtil.newFloatBuffer(input_verts.size() * 3);
		faceBuffer = BufferUtil.newIntBuffer(input_faces.size());
		
		qvertexBuffer = BufferUtil.newFloatBuffer(qinput_verts.size() * 4);
		qnormalBuffer = BufferUtil.newFloatBuffer(qinput_verts.size() * 4);
		qfaceBuffer = BufferUtil.newIntBuffer(qinput_faces.size());

		for (i = 0; i < input_verts.size(); i++) {
			vertexBuffer.put(input_verts.get(i).x);
			vertexBuffer.put(input_verts.get(i).y);
			vertexBuffer.put(input_verts.get(i).z);
			normalBuffer.put(input_norms.get(i).x);
			normalBuffer.put(input_norms.get(i).y);
			normalBuffer.put(input_norms.get(i).z);
		}
		
		for (i = 0; i < qinput_verts.size(); i++) {
			qvertexBuffer.put(qinput_verts.get(i).x);
			qvertexBuffer.put(qinput_verts.get(i).y);
			qvertexBuffer.put(qinput_verts.get(i).z);
			qnormalBuffer.put(qinput_norms.get(i).x);
			qnormalBuffer.put(qinput_norms.get(i).y);
			qnormalBuffer.put(qinput_norms.get(i).z);
		}
		
		
		for (i = 0; i < qinput_faces.size(); i++) {
			qfaceBuffer.put(qinput_faces.get(i));
		}

		for (i = 0; i < input_faces.size(); i++) {
			faceBuffer.put(input_faces.get(i));
		}
		
		qnum_verts = qinput_verts.size();
		qnum_faces = qinput_faces.size() / 4;
		
		num_verts = input_verts.size();
		num_faces = input_faces.size() / 3;
	}
}
