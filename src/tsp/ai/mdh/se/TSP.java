package tsp.ai.mdh.se;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import org.math.plot.Plot2DPanel;

public class TSP {

	//Path to the file
	private String path;
	
	// Number or cities
	private int numOfCities;
	// List of cities
	private List<City> cities;
	
	//Matrix containing the distances form each to each city
	private double [][] distances;
	
	// Number of routes in the population
	private int populationSize;
	// List of the populations' chromosomes(routes)
	private List<Route> population;
	
	// Number of closest neighbors kept 
	private int numClosestNeighbors;
	
	// Probablity that while creating the initial population routes, a neighbor will be next picked
	private float selectNeighborProb;
	
	// The numer of cities chosen from the route to be given the chance to connect with neighbors,using the prob above,during initial
	private int numChosenCitiesNeigh;
	
	// Number of routes in a sample taken when selecting, using roulette wheel
	private int sampleSizeSelection;
	
	// Number of chromosomes(routes) to copy into the next generation before crossover
	private int numOfBestToKeep;

	// True for roulette wheel, false for tournament selection
	private boolean typeSelection;
	
	// if typesel=false, prob that the better will be selected
	private float tournamentBestProb;
	
	// Probability that the selected routes will crossover
	private float crossoverProb;
	// True for cycle crossover, false for order crossover
	private boolean typeCrosssover;
	
	// Probability that the selected route will mutate
	private float muatationProb;
	
	// Best route found so far
	private Route bestRoute;
	
	// Number of iterations (new populations created)
	private int numOfIterations;
	
	// count of Number of iterations same (new populations created)
	private int numOfIterationsSame;
	
	//Number of the iteration that last had the same route
	private int numOfI;
	
	// True for the following stopping criteria: fitness of best solution has not changed for a stopNumOfIterations
	// False for: the average fitness of a population is less than deltaFitness away from the best fitness
	private boolean typeStoppingCondition;
	private int stopNumOfIterations;
	private int deltaFitness;
	
	public TSP(int populationSize, float crossoverProb, float mutationProb ){
		this.populationSize=populationSize;
		this.crossoverProb=crossoverProb;
		this.muatationProb=mutationProb;
		
		this.selectNeighborProb=0.85f;
		
		this.cities= new ArrayList<City>();
		this.population=new ArrayList<Route>();
		
		
		this.sampleSizeSelection=Math.round(this.populationSize/(1.5f));
		this.numOfBestToKeep=this.sampleSizeSelection/5;
		if(this.numOfBestToKeep%2==1){
			this.numOfBestToKeep++;
		}
		// roulette true, tournament false
		this.typeSelection=true;
		
		this.typeCrosssover=false;
		
		this.stopNumOfIterations=500;
		this.numOfIterationsSame=0;
		this.numOfI=-1;
		
	}
	
	List<String> readLines() throws IOException{
		
		FileReader file=new FileReader(this.path);
		BufferedReader br=new BufferedReader(file);
		
		String str;
		List<String> lines= new ArrayList<String>();
		
		while((str=br.readLine())!=null){
			lines.add(str);
		}
		
		return lines;
	}
	public void readCitiesFromFile(String path){
		
		this.path=path;
		List<String> lines=new ArrayList<String>();
		
		try {
			lines=readLines();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Create cities 
		boolean b=false;
		for(String s: lines){
			//System.out.println(s);
			if(s.contains("DIMENSION")){
				this.numOfCities=Integer.parseInt(s.substring(11));
				//System.out.println("Dimension found: "+this.numOfCities);
			}
			if(b==true&&(!s.contains("EOF"))){
				String [] coordinates=s.split(" ");
				//System.out.println("Coordinates:"+coordinates[1]+","+coordinates[2]);
				City c= new City(Integer.parseInt(coordinates[0])-1,Integer.parseInt(coordinates[1]),Integer.parseInt(coordinates[2]));
				this.cities.add(c);
				System.out.println(c);
				
			}
			
			if(s.contains("NODE_COORD_SECTION")){
				b=true;
			}
			
		}
		//int numZero=0;
		this.distances=new double[this.numOfCities][this.numOfCities];
		// Num closest members formula
		this.numClosestNeighbors=this.numOfCities/5;
	//	System.out.println("Num closest :"+numClosestNeighbors);
		
		// Set up distances and closest neighbors
		
		for(int i=0;i<this.numOfCities;i++){
			City c=cities.get(i);
			//System.out.println("City "+c.getX()+", "+c.getY());
			//System.out.print("Distances: ");
			List<Double> minimums= new ArrayList<Double>();
			List<Integer> indices= new ArrayList<Integer>();
			
			for(int j=0;j<this.numOfCities;j++){
				City c1=cities.get(j);
				
				//System.out.print(c.distanceTo(c1.getX(), c1.getY())+" ");
				//if(c.distanceTo(c1.getX(), c1.getY())==0){
					//numZero++;
				//}
			
				// Set up distance
				double d=c.distanceTo(c1.getX(), c1.getY());
				distances[i][j]=d;
				
				if(d!=0){
					boolean added=false;
					for(int k=0;k<minimums.size();k++){
						if(d<minimums.get(k)){
							minimums.add(k,d);
							indices.add(k,j);
							added=true;
							break;
						}
					}
					if(!added&&minimums.size()<this.numClosestNeighbors){
						minimums.add(d);
						indices.add(j);
					}
					if(minimums.size()>this.numClosestNeighbors){
						minimums.remove(minimums.size()-1);
						indices.remove(indices.size()-1);
					}
				}
				
			}
			
			System.out.println("");
			System.out.print("Minimums: ");
			for(int j=0;j<this.numClosestNeighbors;j++){
				System.out.print("("+minimums.get(j)+","+indices.get(j)+") ");
			}
			System.out.println("");
			
			
			c.setClosestNeighbours(indices);
			/*
			int [] ls=c.getClosestNeighbours();
			for(int j=0;j<this.numClosestNeighbors;j++){
				System.out.print(ls[j]+" ");
			}*/
			//System.out.println();
		}
		/*System.out.println("NUMBER OF ZEROES: "+numZero);
		if(numZero==numOfCities){
			System.out.println("NO DUPLICATES");
		}*/
		
		// Closest members calculation
	
		
		
	}
	
	public void createInitialPopulation(){
		
		Random r=new Random();
		
		//formula how many cities to inspect in each route to change to connect to closest
		this.numChosenCitiesNeigh=this.numOfCities/5;
		
		List<Integer> lst=new ArrayList<Integer>();
		for(int j=0;j<this.numOfCities;j++){
			lst.add(j);
		}
		
		
		
		for(int i=0;i<this.populationSize;i++){
			Collections.shuffle(lst);
			
			//for(Integer br:lst){System.out.print(br+" ");}
			
			//Pick numChosenCitiesNeigh random elements
			for(int j=0;j<this.numChosenCitiesNeigh;j++){
				//Choose a random city 
				int rand=r.nextInt(this.numOfCities);
				//System.out.println("random city: "+rand);
				
				//Get the id of the city which corresponds to the index in the cities list
				City c=(City)cities.get(lst.get(rand));
				//System.out.println("city obj:"+c);
				
				//if rand has neighbors that are not in close neighbor list, change them with probability 
				//index left neighbor
				int leftNeighbor=-1;
				if(rand!=0){
				leftNeighbor=(rand-1)%(numOfCities-1);}
				else {
					leftNeighbor=this.numOfCities-1;
				}
				//System.out.println("Left n:"+leftNeighbor+": "+cities.get(lst.get(leftNeighbor)));
				//index right neighbor
				int rightNeighbor=(rand+1)%(numOfCities-1);
				//System.out.println("right n:"+rightNeighbor+": "+cities.get(lst.get(rightNeighbor)));
				//Get the list of closest neighbors of the chosen random city
				List<Integer> closest=c.getClosestNeighbours();
				//If the city is not connected with any of its closest neighbors 
				if(!closest.contains(lst.get(leftNeighbor))&!closest.contains(lst.get(rightNeighbor))){
					
					float prob=r.nextFloat();
					
					// If the probability is smaller then chose from close neighbors
				
					
					if(prob<this.selectNeighborProb){
						
						//Index of random close neighbor from the list
						int randomClose=r.nextInt(numClosestNeighbors);
						
						//Choose whether to replace the left or the right neighbor with the random close neighbour
						int swapLeftorRight=r.nextInt();
						int index=-1;
						// Find the element  in the route, find its index
						
						for(int k=0;k<lst.size();k++){
							
							if(lst.get(k)==closest.get(randomClose)){
								index=k;
								break;
							}
						}
						
						if(swapLeftorRight%2==0){
						Collections.swap(lst, rightNeighbor, index);
						}
						else {
							Collections.swap(lst, leftNeighbor, index);
						}
						
						
					} // end if probability
				} // end if it is not connected
				
				
			} //end pick random num of cities in the route to spap elements with closest neighbors potentially
			
			
			// assign list to route , calculate total first
			double fitness=0;
			for(int j=0;j<this.numOfCities;j++){
				fitness+=distances[lst.get(j)][lst.get((j+1)%(this.numOfCities))];
				//System.out.println("Distance from city "+lst.get(j)+" to city "+lst.get((j+1)%(this.numOfCities))+"is:"+distances[lst.get(j)][lst.get((j+1)%(this.numOfCities))]);
				
				
			}
			/*
			System.out.println("");
			for(int k=0;k<this.numOfCities;k++){
				for(int j=0;j<this.numOfCities;j++){
					System.out.print("["+k+","+j+"]="+distances[k][j]+" ");
				}
				System.out.println("");
			}*/
			
			//Create route and add it to population
			Route route= new Route(i,lst,fitness);
			this.population.add(route);
			
			// Check if it is best route
			if (this.bestRoute==null){
				this.bestRoute= new Route(route.getId(),route.getListCities(), route.getTotalCost());
				
			}
			else {
				if(this.bestRoute.getTotalCost()>route.getTotalCost()){
					bestRoute.setId(route.getId());
					bestRoute.setListCities(route.getListCities());
					bestRoute.setTotalCost(route.getTotalCost());
				}
			}
			//System.out.println("fitness"+fitness);
			//System.out.println("best"+bestRoute.getTotalCost());
			
		} // end population for

	}
	
	public void iteration(){
		
		// Choose num of best to replace worst
		List<Route> newGeneration=new ArrayList<>();
		Route bestprev= new Route(this.bestRoute.getId(),this.bestRoute.getListCities(), this.bestRoute.getTotalCost());
		
		// Add best from previous generation
		
		List<Route> sample1= new ArrayList<Route>();
		Random r= new Random();
		
		for(int i=0;i<this.sampleSizeSelection;i++){
			boolean b=sample1.add(this.population.get(r.nextInt(this.populationSize)));
			while(b==false){
				b=sample1.add(this.population.get(r.nextInt(this.populationSize)));
			}
		}
		
		Collections.sort(sample1);
		for(int brBest=0;brBest<this.numOfBestToKeep;brBest++){
			newGeneration.add(sample1.get(brBest));
		}
		
		//Increment number of iterations
		this.numOfIterations++;
		
		//System.out.println("before creating new generation ");
		
		while(newGeneration.size()!=this.populationSize){
		
		List<Route> sample= new ArrayList<Route>();
		
		for(int i=0;i<this.sampleSizeSelection;i++){
			boolean b=sample.add(this.population.get(r.nextInt(this.populationSize)));
			while(b==false){
				b=sample.add(this.population.get(r.nextInt(this.populationSize)));
			}
		}
		//Collections.sort(sample);
		
		
		Route p1=null;
		Route p2=null;
		
		//System.out.println("before selection ");
		
		if(this.typeSelection==true){
			//get total
			double total=0;
			for(Route ro : sample){
				total+=ro.getTotalCost();
				//System.out.println(ro);
			}
			
			//assign probabilities
			double total1=0;
			double [] rouletteP=new double[this.sampleSizeSelection];
			for(int i=0;i<this.sampleSizeSelection;i++){
				rouletteP[i]=sample.get(i).getTotalCost()/total;
				//System.out.println(rouletteP[i]+" ");
				total1+=rouletteP[i];
			}
			//System.out.println("TOTAL !"+total1);
			
			// select 2 parents
			
			double cumulative=0;
			double d1=r.nextDouble();
			double d2=r.nextDouble();
			
			for(int i=0;i<this.sampleSizeSelection;i++){
				
				if(d1>=cumulative&&d1<(cumulative+rouletteP[i])){
					p1=sample.get(i);
				}
				if(d2>=cumulative&&d2<(cumulative+rouletteP[i])){
					p2=sample.get(i);
				}
				
				cumulative+=rouletteP[i];
				
				if(p1!=null&&p2!=null){
					break;
				}
			}
			
			while(p1.equals(p2)){
				cumulative=0;
				d1=r.nextDouble();
				d2=r.nextDouble();
				
				for(int i=0;i<this.sampleSizeSelection;i++){
					
					if(d1>=cumulative&&d1<(cumulative+rouletteP[i])){
						p1=sample.get(i);
					}
					if(d2>=cumulative&&d2<(cumulative+rouletteP[i])){
						p2=sample.get(i);
					}
					
					cumulative+=rouletteP[i];
					
					if(p1!=null&&p2!=null){
						break;
					}
				}
			}
			
			
			//System.out.println("Roulette p1:"+p1);
			//System.out.println("Roulette p2:"+p2);
			//Now we have selected parents with roulette
			
		}
		else {
			// Selection with tournament
			this.tournamentBestProb=0.8f;
			
			int r1=r.nextInt(this.sampleSizeSelection);
			int r2=r1;
			while(r2==r1){
			r2=r.nextInt(this.sampleSizeSelection);
			}
			int r3=r.nextInt(this.sampleSizeSelection);
			int r4=r3;
			while(r4==r3){
			r4=r.nextInt(this.sampleSizeSelection);
			}
			
			float prob=r.nextFloat();
			float prob1=r.nextFloat();
			
			if(sample.get(r2).getTotalCost()>=sample.get(r1).getTotalCost()){
				//r1 is better or equal
				if(prob<this.tournamentBestProb){
					p1=sample.get(r1);
				}
				else {p1=sample.get(r2);}
				
			}
			if(sample.get(r1).getTotalCost()>sample.get(r2).getTotalCost()){
				//r2 is better
				if(prob<this.tournamentBestProb){
					p1=sample.get(r2);
				}
				else {p1=sample.get(r1);}
				
			}
			if(sample.get(r4).getTotalCost()>=sample.get(r3).getTotalCost()){
				//r3 is better or equal
				if(prob1<this.tournamentBestProb){
					p2=sample.get(r3);
				}
				else {p2=sample.get(r4);}
				
			}
			if(sample.get(r3).getTotalCost()>sample.get(r4).getTotalCost()){
				//r4 is better
				if(prob1<this.tournamentBestProb){
					p2=sample.get(r4);
				}
				else {p2=sample.get(r3);}
				
			}
			
	//		System.out.println(p1);
	//		System.out.println(p2);
			
			//end of tournament
			
		} // end of else of selection type choice
		
		// Now selection is finished , we move on to crossover and mutation
		//System.out.println("before crossover  ");
		
		float crossover=r.nextFloat();
		
		List<Integer> offspring1= new ArrayList<Integer>();
		
		List<Integer> offspring2= new ArrayList<Integer>();
		List<Integer> lst1=new ArrayList<Integer>();
		List<Integer> lst2=new ArrayList<Integer>();
		if(p1!=null&&p2!=null){
		lst1=p1.getListCities();
		lst2=p2.getListCities();
		}
		else {
			System.out.println("what it wrong");
		}
		
		if(crossover<this.crossoverProb){
			
			
		
			for(int i=0;i<this.numOfCities;i++){
				offspring1.add(-1);
				offspring2.add(-1);
			}
			
			if(this.typeCrosssover==true){
				
			boolean b=false;
			int ind=0;
			while(offspring1.get(ind)==-1){
				offspring1.remove(ind);
				offspring1.add(ind,lst1.get(ind));
				int ind1=-1;
				for(int j=0;j<lst1.size();j++){
					// searching in parent 1 for the element index in parent2
					if(lst2.get(ind)==lst1.get(j)){
						ind1=j;
						break;
					}
				}
				if(ind1!=-1){
					ind=ind1;
				}
				else {System.out.println("SHOULD NEVER HAPPEN");}
				
			}// end of while
			
			
			ind=0;
			while(offspring2.get(ind)==-1){
				offspring2.remove(ind);
				offspring2.add(ind,lst2.get(ind));
				int ind1=-1;
				for(int j=0;j<lst2.size();j++){
					// searching in parent 2 for the element index in parent1
					if(lst1.get(ind)==lst2.get(j)){
						ind1=j;
						break;
					}
				}
				if(ind1!=-1){
					ind=ind1;
				}
				else {System.out.println("SHOULD NEVER HAPPEN");}
				
			}// end of while
			
	//		System.out.println("offspring1: "+offspring1);
	//		System.out.println("offspring2: "+offspring2);
			
			for(int j=0;j<offspring1.size();j++){
				if(offspring1.get(j)==-1){
					offspring1.remove(j);
					offspring1.add(j,lst2.get(j));
				}
				if(offspring2.get(j)==-1){
					offspring2.remove(j);
					offspring2.add(j,lst1.get(j));
				}
			}
			
	//		System.out.println("offspring1: "+offspring1);
	//		System.out.println("offspring2: "+offspring2);
			/*
			int ch=0;
				for(int j=0;j<tmp1.size();j++){
					
					if(!tmp1.contains(j)) {ch++;
					System.out.println(j);
					}
				}
			System.out.println("chekck: "+ch);*/
			
			
			}// end of if crossover tyoe =true, cycle crossover
			else {
				// Generate two crossover points between 1 and numofcities -1, 1 to 50 example 
				 
				int crossover1= r.nextInt((((this.numOfCities-1)*(2/5)) - ((this.numOfCities-1)*(1/5))) + 1) + ((this.numOfCities-1)*(1/5));
				int crossover2= r.nextInt((((this.numOfCities-1)*(4/5)) - ((this.numOfCities-1)*(3/5))) + 1) + ((this.numOfCities-1)*(3/5));
				
				
				
				for(int j=crossover1;j<crossover2;j++){
					offspring1.remove(j);
					offspring1.add(j,lst2.get(j));
					offspring2.remove(j);
					offspring2.add(j,lst1.get(j));
				}
				
		//		System.out.println("offspring1: "+offspring1);
		//		System.out.println("offspring2: "+offspring2);
				
				// now create the rest
				
				List<Integer> tmp1=new ArrayList(offspring1);
				List<Integer> tmp2=new ArrayList(offspring2);
				
				int tmpBr1=crossover2;
				int tmpBr2=crossover2;
				for(int j=0;j<this.numOfCities;j++){
					int offset=(crossover2+j)%(this.numOfCities);
						if(!offspring1.contains(lst1.get(offset))){
							tmp1.remove(tmpBr1);
							tmp1.add(tmpBr1,lst1.get(offset));
							tmpBr1=(tmpBr1+1)%(this.numOfCities);
						}
						if(!offspring2.contains(lst2.get(offset))){
							tmp2.remove(tmpBr2);
							tmp2.add(tmpBr2,lst2.get(offset));
							tmpBr2=(tmpBr2+1)%(this.numOfCities);
						}
				
					
				}
				
	//			System.out.println("offspring1: "+tmp1);
	//			System.out.println("offspring2: "+tmp2);
				
				/*
				int ch=0;
				for(int j=0;j<tmp1.size();j++){
					
					if(!tmp1.contains(j)) {ch++;
					System.out.println(j);
					}
				}*/
				//System.out.println("chekck: "+ch+" "+offspring1.size());
				
				offspring1=tmp1;
				offspring2=tmp2;
				
				
			} // end of type crossover
			
		} //end of crossover 
		else {
			offspring1=lst1;
			offspring2=lst2;
		}
		
		// Now we have offspring after crossover
		//System.out.println("before mutation ");
		
		float mutProb=r.nextFloat();
		
		if(mutProb<this.muatationProb){
			// Do mutation 
			for(int br1=0;br1<this.numOfCities/7;br1++){
			// select two random edges from the offsprings , edge (r1,r1+1) and (r2,r2+1)
			int r1= r.nextInt(this.numOfCities-1);
			int r2= r.nextInt(this.numOfCities-1);
			while(r2==r1 || r2==((r1+1)%this.numOfCities) || r2==((r1-1)%this.numOfCities)){
			r2= r.nextInt(this.numOfCities-1);
			}
			
			//System.out.println("YES MUTATION r1 and r2:"+r1+","+r2);
			
			// check distance, if better switch
			double left1= distances[offspring1.get(r1)][offspring1.get(r2)]+distances[offspring1.get(r2)][offspring1.get((r2+1)%this.numOfCities)];
			double right1= distances[offspring1.get(r1)][offspring1.get(r2)]+distances[offspring1.get((r1+1)%this.numOfCities)][offspring1.get((r2+1)%this.numOfCities)];
			
			//System.out.println("distances left right:"+ left1+","+right1);
			
			//System.out.println("off1:"+offspring1);
			if(right1<left1){
				Collections.swap(offspring1,(r1+1)%this.numOfCities, r2);
				//System.out.println("off1 after swap:"+offspring1);
			}
			
			
			double left2= distances[offspring2.get(r1)][offspring2.get(r2)]+distances[offspring2.get(r2)][offspring2.get((r2+1)%this.numOfCities)];
			double right2= distances[offspring2.get(r1)][offspring2.get(r2)]+distances[offspring2.get((r1+1)%this.numOfCities)][offspring2.get((r2+1)%this.numOfCities)];
			
			//System.out.println("distances left right:"+ left2+","+right2);
			
			//System.out.println("off2:"+offspring2);
			
			if(right2<left2){
				Collections.swap(offspring2,(r1+1)%this.numOfCities, r2);
				//System.out.println("off2 after swap:"+offspring2);
			}
		}
		} // end mutation
		
		//System.out.println("after mutation ");
		
		// Calculate fitness 
		double fitness1=0;
		double fitness2=0;
		for(int j=0;j<this.numOfCities;j++){
			fitness1+=distances[offspring1.get(j)][offspring1.get((j+1)%(this.numOfCities))];
			//System.out.println("Distance from city "+lst.get(j)+" to city "+lst.get((j+1)%(this.numOfCities))+"is:"+distances[lst.get(j)][lst.get((j+1)%(this.numOfCities))]);
			fitness2+=distances[offspring2.get(j)][offspring2.get((j+1)%(this.numOfCities))];
			
		}
		
		Route route1= new Route(newGeneration.size()-1,offspring1,fitness1);
		Route route2= new Route(newGeneration.size()-1,offspring2,fitness2);
		newGeneration.add(route1);
		newGeneration.add(route2);
		
		if(this.bestRoute.getTotalCost()>fitness1){
			System.out.println("beat the best r1: "+route1);
			bestRoute.setId(route1.getId());
			bestRoute.setListCities(offspring1);
			bestRoute.setTotalCost(fitness1);
		}
		
		if(this.bestRoute.getTotalCost()>fitness2){
			System.out.println("beat the best r2: "+route2);
			bestRoute.setId(route2.getId());
			bestRoute.setListCities(offspring2);
			bestRoute.setTotalCost(fitness2);
		}
	
		
		}
			//enf od while big 
		
		//System.out.println("after big while"+ " ");
		
		if(bestRoute.equals(bestprev)){
			
			this.numOfIterationsSame++;
			
		}
		else {
			this.numOfIterationsSame=0;
		}
		
		
		
		this.population=newGeneration;
		
		System.out.println("END ITERATION");
		
	}
	
	public void startIterating(){
		while(this.numOfIterationsSame<this.stopNumOfIterations){
			iteration();
		}
		System.out.println("Number of iterations to reach solution:"+this.numOfIterations);
		System.out.println("Tour:"+this.bestRoute.getListCities());
		System.out.println("Fitness:"+this.bestRoute.getTotalCost());
	}
	
	
	public int getPopulationSize() {
		return populationSize;
	}



	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}



	public int getNumClosestNeighbors() {
		return numClosestNeighbors;
	}



	public void setNumClosestNeighbors(int numClosestNeighbors) {
		this.numClosestNeighbors = numClosestNeighbors;
	}



	public float getSelectNeighborProb() {
		return selectNeighborProb;
	}



	public void setSelectNeighborProb(float selectNeighborProb) {
		this.selectNeighborProb = selectNeighborProb;
	}


	public boolean isTypeSelection() {
		return typeSelection;
	}



	public void setTypeSelection(boolean typeSelection) {
		this.typeSelection = typeSelection;
	}



	public float getCrossoverProb() {
		return crossoverProb;
	}



	public void setCrossoverProb(float crossoverProb) {
		this.crossoverProb = crossoverProb;
	}



	public boolean isTypeCrosssover() {
		return typeCrosssover;
	}



	public void setTypeCrosssover(boolean typeCrosssover) {
		this.typeCrosssover = typeCrosssover;
	}



	public float getMuatationProb() {
		return muatationProb;
	}



	public void setMuatationProb(float muatationProb) {
		this.muatationProb = muatationProb;
	}



	public Route getBestRoute() {
		return bestRoute;
	}



	public void setBestRoute(Route bestRoute) {
		this.bestRoute = bestRoute;
	}



	public int getNumOfIterations() {
		return numOfIterations;
	}



	public void setNumOfIterations(int numOfIterations) {
		this.numOfIterations = numOfIterations;
	}



	public boolean isTypeStoppingCondition() {
		return typeStoppingCondition;
	}



	public void setTypeStoppingCondition(boolean typeStoppingCondition) {
		this.typeStoppingCondition = typeStoppingCondition;
	}



	public int getStopNumOfIterations() {
		return stopNumOfIterations;
	}



	public void setStopNumOfIterations(int stopNumOfIterations) {
		this.stopNumOfIterations = stopNumOfIterations;
	}



	public int getDeltaFitness() {
		return deltaFitness;
	}



	public void setDeltaFitness(int deltaFitness) {
		this.deltaFitness = deltaFitness;
	}



	public static void main(String[] args) {
		// TODO Auto-generafted method stub
		TSP tsp= new TSP(15000,0.925f,0.95f);
		tsp.readCitiesFromFile("C:\\Users\\tamara\\Downloads\\TSP(1)\\st70.tsp");
		tsp.createInitialPopulation();
		tsp.startIterating();
		
	/*	 Plot2DPanel plot = new Plot2DPanel();
		 

		  double[] fitness = new double[3];
		  double[] numOfIterations = new double[3];
		  

		int it=100;
		for(int i=0;i<3;i++){
			TSP tsp1= new TSP(it,0.9f,0.95f);
			tsp1.readCitiesFromFile("C:\\Users\\tamara\\Downloads\\TSP(1)\\eil51.tsp");
			tsp1.createInitialPopulation();
			tsp1.startIterating();
			fitness[i]=tsp1.getBestRoute().getTotalCost();
			numOfIterations[i]=tsp1.getNumOfIterations();
			it*=10;
			
		}
		
		plot.addLinePlot("my plot", fitness, numOfIterations);
		 
		  // put the PlotPanel in a JFrame, as a JPanel
		  JFrame frame = new JFrame("a plot panel");
		  frame.setContentPane(plot);
		  frame.setVisible(true);
		
		float p1=0.8f;
		float p2=0.85f;
		
		fitness=new double[5];
		numOfIterations= new double[5];
		
		Plot2DPanel plot1 = new Plot2DPanel();
		
		for(int i=0;i<5;i++){
			TSP tsp1= new TSP(10000,p1,p2);
			tsp1.readCitiesFromFile("C:\\Users\\tamara\\Downloads\\TSP(1)\\eil51.tsp");
			tsp1.createInitialPopulation();
			tsp1.startIterating();
			fitness[i]=tsp1.getBestRoute().getTotalCost();
			numOfIterations[i]=tsp1.getNumOfIterations();
			p1+=0.2f;
			p2+=0.2f;
			
		}
		plot.addLinePlot("my plot", fitness, numOfIterations);
		 
		  // put the PlotPanel in a JFrame, as a JPanel
		  JFrame frame1 = new JFrame("a plot panel");
		  frame1.setContentPane(plot);
		  frame1.setVisible(true);
		 
		*/
		
	}

}
