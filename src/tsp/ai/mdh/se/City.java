package tsp.ai.mdh.se;

import java.util.List;

public class City {
	
	private int id;
	private int x;
	private int y;
	
	private List<Integer> closestNeighbours;
	
	public City(int id, int x, int y){
		this.id=id;
		this.x=x;
		this.y=y;
	}
	
	public double distanceTo(int x,int y){
		return Math.sqrt(Math.pow(x-this.x, 2)+Math.pow(y-this.y, 2));
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getX() {
		return x;
	}


	public void setX(int x) {
		this.x = x;
	}


	public int getY() {
		return y;
	}


	public void setY(int y) {
		this.y = y;
	}

	public List<Integer> getClosestNeighbours() {
		return closestNeighbours;
	}

	public void setClosestNeighbours(List<Integer> closestNeighbours) {
		this.closestNeighbours = closestNeighbours;
	}

	@Override
	public String toString() {
		return "City [id=" + id + ", x=" + x + ", y=" + y
				+ ", closestNeighbours=" + closestNeighbours + "]";
	}

	
	
	
}
