package env.cow;




import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.Cobweb;
import weka.clusterers.EM;
import weka.clusterers.FarthestFirst;
import weka.clusterers.XMeans;
import weka.clusterers.sIB;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.lang.Math;

import jia.Vec;


/**
 * Make the clustering of the cows and stores the Clusters in a Model, it calculates the clustering with the weka tool.
 * 
 * @author Gustavo Pacianotto Gouveia
 * @author Victor Lassance Oliveira e Silva 
 */

public class ClusterModel implements Runnable,IClusterModel{
	/* How to use Tuner:
	 * for cluster Methods:
	 *   1: Expectation Maximization
	 *   2: XMeans
	 *   3: Cobweb
	 *   4: sIB
	 *   5: FarthestFirst
	 * for setting the Radius Property:
	 *   1: Max distance between the center and a cow
	 *   2: Use the Standard Deviation
	 *   3: Use 2*A: A = Average Radius
	 *   4: 2*Median
	 */

	private boolean spc = true;
	private boolean useTuner = false;
	private int useClusterer = 1;
	public static final int CLUSTERER_EM = 1;
	public static final int CLUSTERER_XMEANS = 2;
	public static final int CLUSTERER_COBWEB = 3;
	public static final int CLUSTERER_sIB = 4;
	public static final int CLUSTERER_FF = 5;
	private int prefRadius=10, prefnCows=50,kPTC=100;
	private int useRadius = 4;
	public static final int RADIUS_MAXDIST = 1;
	public static final int RADIUS_SD = 2;
	public static final int RADIUS_DBLDAVERAGE = 3;
	final private static String NAME_FILE = "cows.arff";
	final private static String CENTERS_NAME_FILE = "centers.arff";
	private File file;
	private File previous;
	private int[][] data = new int[2][2];
	private int dataNumber;
	private int[] alloc = new int[2];
	private Vec[] Center;
	private int n;
	private int[] numcows;
	private int[] maxDist;
	private Logger logger = Logger.getLogger(ClusterModel.class.getName());
	ClusterEvaluation ce;
	private Cow[] cows;
	private int actStep = -1;
	private int clStep;
	private int[] s;// = new int[1000];
	//private Map <Integer, Integer> s = new HashMap<Integer, Integer>();
	private int gH = 0;
	private int gW = 0;
	private int[][] cowsCluster;
	private boolean[][] trees;
	final int DOWN = 0, UP = 1, LEFT = 2, RIGHT = 3;
	List<Integer> treeToAdd = new Vector<Integer>();
	ICowModel cModel = CowModelFactory.getModel("ClusterModel");
	
	/**
	 * Constructs this Model and verify if the tuner needs to be called
	 */
	public ClusterModel(){
		if(useTuner){
			configTable.main(null);
		}
	}
	
	/**
	 * It is the function that implements the process, it needs to be called every step.
	 */
	public synchronized void run(){
		if(actStep<clStep){
			actStep = clStep;
			alloc = calculate();
			cCenters();
//			separateClusters();
			setMap();
			cMaxDist();
//			printClusters();
		}
	}

	/**
	 * Just for debuging, it prints the clusters and it's positions.
	 */
	private void printClusters(){
		String str = new String();
		str = "HHH the number of clusters is "+n+"| ";
		for(int i = 0;i<n;i++){
			str+="cluster "+i+" with position ("+Center[i].getX()+","+(gH-Center[i].getY()-1)+") and size = "+maxDist[i]+"|";
		}
		logger.info(str);
	}
	/**
	 * It returns the Radius of the Cluster, i.e. the distance of the faraway cow from the Cluster center;
	 * @return maxDist
	 */
	public int[] getMaxDist(){
		return maxDist;
	}
	/**
	 * Sets the step of the simulation before the clustering.
	 * @param step
	 */
	public void setStepcl(int step){
		if(step>clStep)	clStep = step;
		if(step == -1) {
			clStep = 0;
			actStep = -1;
		}
	}
	/**
	 * It calculates the Radius of each Cluster and stores it on the model.
	 */
	private void cMaxDist(){
		switch(useRadius){
		case 1:
			maxDist = new int[Center.length];
			for(int i = 0;i<dataNumber;i++){
				int Dist =  Math.abs(Center[alloc[i]].getX() - data[0][i]);
				int Dist2 = Math.abs(Center[alloc[i]].getY() - data[1][i]);
				Dist = Math.max(Dist, Dist2);
				if(Dist>maxDist[alloc[i]]) maxDist[alloc[i]] = Dist;
				
			}
			break;
		case 2:			
			maxDist = new int[Center.length];
			for(int i = 0;i<dataNumber;i++){
				int Dist =  Math.abs(Center[alloc[i]].getX() - data[0][i]);
				int Dist2 = Math.abs(Center[alloc[i]].getY() - data[1][i]);
				Dist = Math.max(Dist, Dist2);
				maxDist[alloc[i]] += Dist*Dist;
			}
			for(int i = 0;i<maxDist.length;i++){
				maxDist[i] = (int)Math.sqrt((double)maxDist[i]/((double)numcows[i]-1.0)); // gives the standard deviation.
			}
			break;
		case 3:
			maxDist = new int[Center.length];
			for(int i = 0;i<dataNumber;i++){
				int Dist =  Math.abs(Center[alloc[i]].getX() - data[0][i]);
				int Dist2 = Math.abs(Center[alloc[i]].getY() - data[1][i]);
				Dist = Math.max(Dist, Dist2);
				maxDist[alloc[i]] += Dist;
				
			}
			for(int i = 0;i<maxDist.length;i++){
				maxDist[i] = 2*maxDist[i]/numcows[i]; 
			}
			break;
		
		case 4:
			Vec[] CenterTemp = new Vec[Center.length];
			for(int i = 0;i<CenterTemp.length;i++){
				CenterTemp[i] = new Vec(0,0);
			}
			float[] distOut = new float[Center.length];
			Vector<Vector<Integer>> temp = new Vector<Vector<Integer>>();
			Vector<Vector<Integer>> temp2 = new Vector<Vector<Integer>>();
			for(int j = 0;j<Center.length;j++){
				temp.add( new Vector<Integer>());
				temp2.add( new Vector<Integer>());
			}
			maxDist = new int[Center.length];
			for(int i = 0;i<dataNumber;i++){
				int Dist =  Math.abs(Center[alloc[i]].getX() - data[0][i]);
				int Dist2 = Math.abs(Center[alloc[i]].getY() - data[1][i]);
				Dist = Math.max(Dist, Dist2);
				temp.get(alloc[i]).add(Dist);
			}
			for(int i = 0;i<temp.size();i++){
				//logger.info("III"+temp.get(i));
				Collections.sort(temp.get(i));
				if (temp.get(i).size() <= 3)
					distOut[i] = 0;
				else {				
					int pos3Quartil = Math.round((float)0.75*(temp.get(i).size()+1))-1;
					int pos1Quartil = Math.round((float)0.25*(temp.get(i).size()+1))-1;
					distOut[i] = temp.get(i).get(pos3Quartil)+(float)1.5*(temp.get(i).get(pos3Quartil)-temp.get(i).get(pos1Quartil));    
				}
				
			}
			for(int i  = 0;i<dataNumber;i++){
				//if(distOut[alloc[i]]<=0) continue;//there are no Outliers
				int Dist =  Math.abs(Center[alloc[i]].getX() - data[0][i]);
				int Dist2 = Math.abs(Center[alloc[i]].getY() - data[1][i]);
				Dist = Math.max(Dist, Dist2);
				//if(Dist<temp.get(alloc[i]).get(distOut[alloc[i]])){
				if(Dist<=distOut[alloc[i]] || (distOut[alloc[i]]<=0)){
					temp2.get(alloc[i]).add(Dist);
					CenterTemp[alloc[i]] = CenterTemp[alloc[i]].add(new Vec(data[0][i],data[1][i]));					
				}
				
			}

			for(int i = 0;i<temp2.size();i++){
				Collections.sort(temp2.get(i));
				switch(temp2.get(i).size()%2){
					case 1:{
						maxDist[i] = 2*temp2.get(i).get(temp2.get(i).size()/2);
						break;
					}
					case 0:{
						maxDist[i] = temp2.get(i).get(temp2.get(i).size()/2);
						maxDist[i]+= temp2.get(i).get(temp2.get(i).size()/2-1);
						break;
					}
				}
				maxDist[i]+=1;
				CenterTemp[i] = CenterTemp[i].newMagnitude(CenterTemp[i].magnitude()/((double)temp2.get(i).size()));
				
			}
			/*for(int i = 0;i<CenterTemp.length;i++){
				if(distOut[i]>0){
					//logger.info("III distOut="+distOut[i]+" of "+numcows[i]+" Dist = "+temp.get(i).get(distOut[i]));
					CenterTemp[i] = CenterTemp[i].newMagnitude(CenterTemp[i].magnitude()/((double)temp2.get(i).size()));
				}else{
					CenterTemp[i] = Center[i];
					
				}
				
			}*/
			Center = CenterTemp.clone();
			break;
			
		}
			
	}
	
	/**
	 * It gives the allocation of the cows, the i-th cow is in the al[i] cluster.
	 * @return alloc
	 */
	public int[] getAlloc(){
		int[] al = alloc.clone();
		return al;
	}
	
	/**
	 * It returns the number of clusters on the evaluation;
	 */
	public int getNumberOfCluster(){
		return n;
	}
	/**
	 * It returns the number of Cows in each Cluster.
	 * @return
	 */
	public int[] getNumCows(){
		return numcows;
	}
	/**
	 * It sets the id of the cows to the id of the Cluster
	 */
	private void setMap(){
		s  = new int[1000];
		for(int i = 0 ; i<998 ; i++) 
			s[i] = -1;
		
		for(int i = 0;i<dataNumber;i++){
			s[cows[i].id] = alloc[i];
			//s.put(cows[i].id, alloc[i]);
		}
		/*
		s = new HashMap<Integer,Integer>();
		for(int i = 0;i<dataNumber;i++){
			s.put(cows[i].id, alloc[i]);
		}
		*/
	}
	
	public int[] getMap(){
		/*
		String str = new String();
		for(int i =0;i<s.length;i++){
			str+=" "+s[i];
		}
		logger.info(str);
		*/
		return s;
	}
	
	
	/**
	 * It returns the HashMap with the ClustersIds
	 */
	/*
	public Map<Integer,Integer> getMap(){
		return s;
	}
	*/
	/**
	 * Reallocate the cows into the numcows.
	 */
	private void realloc(){
		cowsCluster = new int[dataNumber][dataNumber];
		n = 0;
		numcows = new int[dataNumber];
		for(int i = 0; i < dataNumber; i++){
			if(alloc[i]>dataNumber){
				logger.info("Something is wrong with the allocation");
			}
			cowsCluster[alloc[i]][numcows[alloc[i]]] = i;
			numcows[alloc[i]]++;
			if(alloc[i]+1>n){
				n = alloc[i]+1;
			}
		}
		int[] temp2 = new int[n];
		for(int i = 0;i<n;i++){
			temp2[i] = numcows[i];
		}
		numcows = temp2.clone();
	}
	/**
	 * it calculates the center of each cluster, a simple Center of mass.
	 */
	private void cCenters(){
		cowsCluster = new int[dataNumber][dataNumber];
		n = 0;
		Center = new Vec[dataNumber]; //the max number of clusters is the number of cows.
		for(int i = 0;i<dataNumber;i++) Center[i] = new Vec(0,0);
		numcows = new int[dataNumber];
		for(int i = 0; i < dataNumber; i++){
			if(alloc[i]>dataNumber){
				logger.info("Something is wrong with the allocation");
			}
			cowsCluster[alloc[i]][numcows[alloc[i]]] = i;
			numcows[alloc[i]]++;
			if(alloc[i]+1>n){
				n = alloc[i]+1;
			}
			Vec tmp = new Vec(data[0][i],data[1][i]);//Vec tmp = new Vec(data[0][i],gH-1-data[1][i]);
			Center[alloc[i]] = Center[alloc[i]].add(tmp);
		}		
		for(int i = 0;i<n;i++){
			Center[i] = Center[i].newMagnitude(Center[i].magnitude()/((double)numcows[i]));
		}
		Vec[] temp = new Vec[n];
		for(int i = 0 ;i<n;i++){
			temp[i] = (Vec)(Center[i].clone());
		}
		int[] temp2 = new int[n];
		for(int i = 0;i<n;i++){
			temp2[i] = numcows[i];
		}
		numcows = temp2.clone();
		Center = temp.clone();
	}
	
	/**
	 * It separates clusters that have trees between then. Not fully tested yet.
	 */
	private void separateClusters(){
		int[][] datatmp;
		int[][] extremes = new int[4][n];
		if(data == null) return;
		datatmp = data.clone();
		
		for(int i = 0;i<n;i++){
			Vec cellUp;
			Vec cellDown;
			extremes[UP][i] = extremes[DOWN][i] = datatmp[1][cowsCluster[i][0]];
			extremes[LEFT][i] = extremes[RIGHT][i] = datatmp[0][cowsCluster[i][0]];
			cellUp = cellDown = new Vec(extremes[RIGHT][i],extremes[UP][i]);

			for(int j = 1;j<numcows[i];j++){
				int x = datatmp[0][cowsCluster[i][j]];
				int y = datatmp[1][cowsCluster[i][j]];
				if(x>extremes[RIGHT][i]){
					extremes[RIGHT][i] = x;
				}else if(x<extremes[LEFT][i]){
					extremes[LEFT][i] = x;
				}
				if(y>extremes[UP][i]){
					extremes[UP][i] = y;
					cellUp = new Vec(x,y);
				}else if(y<extremes[DOWN][i]){
					extremes[DOWN][i] = y;
					cellDown = new Vec(x,y);
				}

			}
			
			List<Integer> Lista = new Vector<Integer>();
			Vec act = cellUp.sub(cellDown);
			double mag = act.r;
			Vec init = new Vec(0,0);
			for(int j = 0;j<(int)mag+1;j++){
				Vec position = cellDown.add(act.newMagnitude(j));
				int posx = (int)position.x;
				int posy = (int)position.y;
				if(trees[posx][posy]){
					init = new Vec(posx,posy);
				}
			}
			// Going Up
			int[] dx = {0 ,1 ,-1};
			int[] dy = {-1,0 ,0 };
			Vec Up = goThroughCells(dx,dy,init,extremes,i);
			// Going Down
			dx[0] = 0;dx[1] = 1;dx[2] = -1;
			dy[0] = 1;dy[1] = 0;dy[2] = 0;
			Vec Down = goThroughCells(dx,dy,init,extremes,i);
			// Going Left
			dx[0] = 1;dx[1] = 0;dx[2] = 0;
			dy[0] = 0;dy[1] = 1;dy[2] = -1;
			Vec Left = goThroughCells(dx,dy,init,extremes,i);
			// Going Right
			dx[0] = -1;dx[1] = 0;dx[2] = 0;
			dy[0] = 0;dy[1] = 1;dy[2] = -1;
			Vec Right = goThroughCells(dx,dy,init,extremes,i);
			
			
			Vec D2U = Up.sub(Down);
			Vec L2R = Right.sub(Left);
			
			
			for(int cow: cowsCluster[i]){
				if(D2U.dot(new Vec(data[0][cow],data[1][cow]))<0){
					Lista.add(cow);
				}
			}
			
			if(Lista.size()>0 && Lista.size()!=numcows[i]){
				for(int j = 0;j<Lista.size();j++){
					alloc[Lista.get(j)] = n;
				}
				n++;
			}
			
			realloc();
			for(int cow: cowsCluster[i]){
				if(L2R.dot(new Vec(data[0][cow],data[1][cow]))<0){
					Lista.add(cow);
				}
			}
			
			if(Lista.size()>0 && Lista.size()!=numcows[i]){
				for(int j = 0;j<Lista.size();j++){
					alloc[Lista.get(j)] = n;
				}
				n++;
			}			
		}
				

	}
	/**
	 * Used on the separateClusters to find a tree between the farthest cows in one direction
	 */
	private Vec goThroughCells(int dx[],int dy[], Vec beg, int[][]extremes,int Cluster){
		Vec cur = beg;
		boolean nextOk;
		while(true){
			nextOk = false;
			Vec cur2 = cur;
			for(int i = 0;i<dx.length;i++){
				cur2 = cur.add(new Vec(dx[i]+0.1,dy[i]+0.1));
				if(isIn((int)cur2.x,(int)cur2.y,extremes,Cluster) && trees[(int)cur2.x][(int)cur2.y]){
					nextOk = true;
					break;
				}
			
			}
			if(!nextOk){
				beg = cur;
				break;
			}else{
				cur = cur2;
			}
		}
		return beg;
	}
	
	/**
	 * checks if the position is in the cluster
	 */
	private boolean isIn(int x, int y,int[][] extremes,int Cluster){
		if(x>extremes[RIGHT][Cluster]) return false;
		if(x<extremes[LEFT][Cluster]) return false;
		if(y>extremes[UP][Cluster]) return false;
		if(y<extremes[DOWN][Cluster]) return false;
		return true;
		
	}
	
	/**
	 * Print the centers on the logger, just for debbuging.
	 */
	private void printCenters(){
		Vec[] c = Center.clone();
		String str = new String();
		str+=">";
		for(int i = 0;i<c.length;i++){
			logger.info(str);
			str+="  ["+i+"]"+"("+(int)c[i].x+","+(int)c[i].y+")";
		}
		logger.info(str);
	}
	/**
	 * It returns the center of each cluster.
	 * @return Centers
	 */
	public Vec[] getCenters(){
		return Center;
	}
	/**
	 * it returns the data used for the Clustering, i.e. the position of each cow.
	 * @return
	 */
	public int[][] getData(){
		return data.clone();
	}
	
	/**
	 * Saves the centers of the clusters inside a file, where the new iteration can read the values, 
	 * It should help to maintain the id of the clusters more stable. But it also 
	 * makes the number of clusters constant. Not in use.
	 */
	private void createCentersFile() {
		try {
			previous = new File(CENTERS_NAME_FILE);
			if(!previous.exists())
				previous.createNewFile();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(previous,false));
			out.write("@relation 'centers'\n");
			out.write("@attribute X real\n@attribute Y real\n@data\n\n");
			for(int i = 0;i<Center.length;i++){
				out.write(Center[i].getX()+","+Center[i].getY()+"\n");
			}
			out.write("\n");
			out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * It creates the files used for the weka tool.
	 * @param null
	 */
	private void createFile() {
		int i;
		try {	        
	        file = new File(NAME_FILE);
	        if (!file.exists())
	        	file.createNewFile();
	        
	        BufferedWriter out = new BufferedWriter(new FileWriter(file, false));
	        out.write("@relation 'cows'\n");
	        out.write("@attribute X real\n@attribute Y real\n@data\n\n");
	        for (i = 0; i < dataNumber; i++)
            	out.write(data[0][i]+","+data[1][i]+"\n");
	        out.write("\n");
	        out.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	/**
	 * It gets the Cows from CowModel and do the clustering. It returns the allocation of each cow to it's cluster.
	 * @param NumCows
	 * @return alloc
	 */
	private void defineData(int[][] inf){
		data = inf.clone();
	}
	
	/**
	 * Change the class that will be used for clustering.
	 * @Use 1 EM; 2 Xmeans;3 cobWeb;4 sIB;5 FF
	 */
	public void changeClusterer(int a){
		useClusterer = a;
	}
	
	/**
	 * Change the way of calculating the radius of a cluster.
	 */
	public void changeMaxDist(int a){
		useRadius = a;
	}
	/**
	 * Change the values used on Weights of clusters.
	 * @param radius
	 * @param nCows
	 */
	public void changeWCprop(int radius,int nCows){
		prefRadius = radius;
		prefnCows = nCows;
	}
	/**
	 * returns the preferential radius for a cluster. (WofC)
	 * @return
	 */
	public int getPrefRadius(){
		return prefRadius;
	}
	/**
	 * returns the preferential number of cows to be herded.
	 * @return
	 */
	public int getPrefNCows(){
		return prefnCows;
	}
	/**
	 * change the k factor used in position_to_cluster.
	 * @param k
	 */
	public void changePTCprop(int k){
		kPTC = k;
	}
	/**
	 * returns the k factor used in position_to_cluster.
	 * @return
	 */
	public double getPrefkPTC(){
		return (double)kPTC/100.0;
	}
	/**
	 * It inserts trees on the model.
	 */
	synchronized public void insertTree(int x, int y){
		if(!(x>=gW || x<0 || y>=gH || y<0)){
			while(treeToAdd.size()>0){
				int tmp = treeToAdd.remove(0);
				insertTree(tmp/10000,tmp%10000);
			}
			try{
				trees[x][-y+gH-1] = true;
			}catch (Exception e){
				e.printStackTrace();
			}
		}else{
			if(gW == 0 || gH == 0){
				treeToAdd.add(x*10000+y);
			}
		}
	}
	public void setCows(Cow[] c,int H,int W){
		spc = false;
		cows = c;
		gH = H;
		gW = W;
	}
	/**
	 * It is the kernel of the algorithm of clustering, it calls weka and parses the String returned.
	 * @return Allocations
	 */
	private int[] calculate() {
		ce = new ClusterEvaluation();
		int countStepsWithProblem = 0;
		if(spc){
			cModel.updateCows();
			cows = cModel.getCows();
			while(gH <= 0 || gW <= 0|| clStep == -1){
				gH = cModel.getSizeh();
				gW = cModel.getSizew();
				if(countStepsWithProblem++ > 20){
					break;
				}
			}
		}
		if(cows==null || cows.length <=0)
		{
			dataNumber=0;
			gH=gW=0;
		}
		else
			dataNumber = cows.length;
			
		trees = new boolean[gW][gH];
		int[][] infos = new int[2][dataNumber];
		if (dataNumber == 0)
			return null;
		for (int i = 0; i < dataNumber; i++) {
			infos[0][i] = cows[i].x;
			infos[1][i] = gH-1-cows[i].y;//gH -cows[i].y-1;
		}
		defineData(infos);
		createFile();
		/* useClusterer:
		 * 1: Expectation Maximization
		 * 2: XMeans
		 * 3: Cobweb
		 * 4: sIB
		 * 5: FarthestFirst
		 */
		Clusterer cluster = null;
		String[] options = null;
		switch(useClusterer){
		case 1:cluster = new EM();
			options = new String[4];
			options[0] = "-t";
			options[1] = file.getAbsolutePath();
			options[2] = "-p";
			options[3] = "0";
			break;
		case 2:cluster = new XMeans();
			options = new String[8];
			options[0] = "-t";
			options[1] = file.getAbsolutePath();
			options[2] = "-p";
			options[3] = "0";
			options[4] = "-H";
			options[5] = "6"; // this is the maximum number of clusters.
			options[6] = "-D";
			options[7] = "weka.core.ChebyshevDistance"; 
			//	options[8] = "-N";
			//	options[9] = previous.getAbsolutePath();
			break;
		case 3:cluster = new Cobweb();
			options = new String[4];
			options[0] = "-t";
			options[1] = file.getAbsolutePath();
			options[2] = "-p";
			options[3] = "0";
			break;
		case 4:cluster = new sIB();
			options = new String[4];
			options[0] = "-t";
			options[1] = file.getAbsolutePath();
			options[2] = "-p";
			options[3] = "0";
			break;
		case 5:cluster = new FarthestFirst();
			options = new String[4];
			options[0] = "-t";
			options[1] = file.getAbsolutePath();
			options[2] = "-p";
			options[3] = "0";
			break;
		}

		try {
			String str = ClusterEvaluation.evaluateClusterer(cluster,options);
			//logger.info(str);
			//logger.info("HHH calculated with "+ useClusterer);
			//logger.info("HHH"+str);
			int[] assign = new int[dataNumber];
			String[] tmp = str.split("\n");
			for(int i = 0;i<dataNumber;i++){
				assign[i] = (int)(Double.parseDouble(tmp[i].split(" ")[1])+0.1);
			}
			
			return assign;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void update() {
		//just to do nothing
	}
}
