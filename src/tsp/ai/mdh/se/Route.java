package tsp.ai.mdh.se;

import java.util.ArrayList;
import java.util.List;

public class Route implements Comparable<Route>{
	
	private int id;
	
	private List<Integer> listCities;
	
	private double totalCost;
	
	public Route(int id, List<Integer> listCities, double totalCost){
		this.id=id;
		this.listCities= new ArrayList<Integer>(listCities);
		this.totalCost=totalCost;
	}

	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}



	public List<Integer> getListCities() {
		return listCities;
	}

	public void setListCities(List<Integer> listCities) {
		this.listCities = new ArrayList<Integer>(listCities);
	}

	public double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}
	
	@Override
	public int compareTo(Route arg0) {
		double d=this.totalCost-arg0.totalCost;
		int res=0;
		if(d>0) res=1;
		if(d==0) res=0;
		if(d<0) res=-1;
		return res;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((listCities == null) ? 0 : listCities.hashCode());
		long temp;
		temp = Double.doubleToLongBits(totalCost);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Route other = (Route) obj;
		if (Double.doubleToLongBits(totalCost) != Double
				.doubleToLongBits(other.totalCost))
			return false;
		if (listCities == null) {
			if (other.listCities != null)
				return false;
		} else {
			int index=-1;
			for(int i=0;i<other.listCities.size();i++){
				if(other.listCities.get(i)==this.listCities.get(0)){
					index=i;
					break;
				}
			}
			if(index!=-1){
				for(int j=0;j<this.listCities.size();j++){
					if(listCities.get(j)!=other.listCities.get((index+j)%(other.listCities.size()))){
						return false;
					}
				}
			}
			else {
				return false;
			}
			
		}
		
	/*	System.out.println("DUPLICATE");
		System.out.println("f1: "+this.totalCost+"id:"+this.id+"list:"+this.listCities);
		System.out.println("f2: "+other.totalCost+"id"+other.id+"list:"+this.listCities);
		System.out.println("DUPLICATE");*/
		return true;
	}

	@Override
	public String toString() {
		return "Route [id "+id +" listCities=" + listCities + ", totalCost=" + totalCost
				+ "]";
	}
	
	
	


}
