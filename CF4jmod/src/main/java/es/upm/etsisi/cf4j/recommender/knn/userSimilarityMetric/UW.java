package es.upm.etsisi.cf4j.recommender.knn.userSimilarityMetric;

import java.util.HashMap;

import es.upm.etsisi.cf4j.data.DataModel;
import es.upm.etsisi.cf4j.data.GenreReader;
import es.upm.etsisi.cf4j.data.Item;
import es.upm.etsisi.cf4j.data.User;

public class UW extends UserSimilarityMetric{
	GenreReader gn;
	/** Matrix that contains the similarity between each pair of users */
	protected double[][] similarities;
	protected double[][] wieghts;
	protected HashMap<Integer, String> userWeights = new HashMap<Integer,String>();
	protected HashMap<Integer, String> itemWeights = new HashMap<Integer,String>();

	@Override
	public double similarity(User user, User otherUser) {
		System.out.println("==============UW sim===========");
//		System.out.println(user.getUserIndex());
		
		return wieghts[user.getUserIndex()][otherUser.getUserIndex()];
		
//		return 0;
	}

	public UW(GenreReader gn, DataModel dm) {
		this.gn = gn;
		this.datamodel = dm;
		System.out.println("==============UW Constructor===========");
		this.calculateWeightMatrix(true);
	}
	public void calculateWeightMatrix(boolean testOnly) {
		System.out.println("==============calculateWeightMatrix===========");
		//users all the ratings in data model 
		if (testOnly) {
			
			Item[] tlist = datamodel.getItems();
//			System.out.println(this.datamodel.getItems().length);
			this.datamodel.getUsers();
			User[] tulist = this.datamodel.getUsers();
//			System.out.println("==============true===========");
			
			
			for (int i = 0; i < tulist.length; i++) {
				User user = tulist[i];
				
//				System.out.println("\nUser "+ user.getUserIndex()+" # of ratings"+ user.getNumberOfRatings());
				String collector = "";
				if (this.gn.separator.equals("|")){
					collector = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
				}else if (this.gn.separator.equals("::")) {
					 collector = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
				}
				
				
				for (int j = 0; j < user.getNumberOfRatings(); j++) {
//					System.out.println(" "+user.getRatingAt(j));
//					System.out.println("j = "+j);
//					System.out.println("String.valueOf(user.getItemAt(j))"+String.valueOf(user.getItemAt(j)+1));
					String itgenre = gn.getItemGenres(String.valueOf(user.getItemAt(j)+1));
//					System.out.println("itgenre = "+itgenre);
					double rate = user.getRatingAt(j);
					String rates = itgenre.replace("1",String.valueOf(rate));
					
					
//					System.out.println("==============result===========");
					collector = vectorAdd(collector, rates, ",");
				}
				
//				System.out.println("\n==============Colletcor===========");
//				System.out.println(collector);
//				System.out.println(normalizeCat(collector,","));
				String normlaizedUserCat = normalizeCat(collector,",");
				userWeights.put(user.getUserIndex(), normlaizedUserCat);
				
				
			}
			
			
			for (int i = 0; i < tlist.length; i++) {
				Item item = tlist[i];
//				System.out.println("\nUser "+ item.getItemIndex()+" # of ratings"+ item.getNumberOfRatings());
				String collector = "";
				if (this.gn.separator.equals("|")){
					collector = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
				}else if (this.gn.separator.equals("::")) {
					 collector = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
				}
				for (int j = 0; j < item.getNumberOfRatings(); j++) {
					System.out.print(" "+item.getRatingAt(j));

					double rate = item.getRatingAt(j);
					int u = item.getUserAt(j);
//					System.out.println("==============result===========");
					String wR = weightedRating(userWeights.get(u), rate, ",");
					collector = vectorAdd(collector, wR, ",");	

				}
				
//				System.out.println("\n=============Item Colletcor===========");
//				System.out.println(collector);
//				System.out.println(normalizeCat(collector,","));
				String normlaizedItemCat = normalizeCat(collector,",");
				itemWeights.put(item.getItemIndex(), normlaizedItemCat);
						
			}
			
			wieghts = new double[tulist.length][tlist.length];
			for (int i = 0; i < tulist.length; i++) {
				for (int j = 0; j < tlist.length; j++) {
					
					wieghts[i][j] = dotProductbyString(userWeights.get(tulist[i].getUserIndex()), itemWeights.get(tlist[j].getItemIndex()), ",");
				}
				
			}
			
		}
		
	}
	
	public String vectorAdd(String v1, String v2, String sep) 
    { 
//		System.out.println("==============vectorAdd===========");
//		System.out.println("v2 = "+v2);
//		System.out.println("v1 = "+v1);
		
		String[] s1 = v1.split(sep);
		String[] s2 = v2.split(sep);
		
		StringBuilder sumt = new StringBuilder();
        for (int i = 0; i < s1.length; i++) {
        	 double r = (Double.valueOf(s1[i])) + Double.valueOf(s2[i]);
        	sumt.append(sep+Double.toString(r));
        	
        }
//        System.out.println("sum= "+sumt.toString().replaceFirst(sep, "")); 
        return sumt.toString().replaceFirst(sep, ""); 
    }
	
	public String normalizeCat(String sumt, String sep) {
		double sumratings = 0 ;
		
		String[] s1 = sumt.split(sep);
		StringBuilder normval = new StringBuilder();
		
		for (int i = 0; i < s1.length; i++) {
	       	  sumratings += Double.valueOf(s1[i]);    	
	       }
		
		for (int i = 0; i < s1.length; i++) {
       	 double r = (Double.valueOf(s1[i])) / sumratings;
       	 normval.append(sep+Double.toString(r));
       	
       }
		
//		System.out.println("nor= "+normval.toString().replaceFirst(sep, "")); 
        return normval.toString().replaceFirst(sep, ""); 
	}
	
	public String weightedRating(String uWeight,double rating, String sep) {
		
		String[] s1 = uWeight.split(sep);
		StringBuilder wRating = new StringBuilder();
		
		
		for (int i = 0; i < s1.length; i++) {
       	 double r = Double.valueOf(s1[i]) * rating;
       	 wRating.append(sep+Double.toString(r));
       	
       }
		
//		System.out.println("wR = "+wRating.toString().replaceFirst(sep, "")); 
        return wRating.toString().replaceFirst(sep, ""); 
	}
	
	public double[] weightedRating(double[] uWeight,double rating) {
		
		double[] wR = new double[uWeight.length];

		for (int i = 0; i < uWeight.length; i++) {
       	 double r = uWeight[i] * rating;
       	 wR[i] = r;
       	
       }
//		System.out.println("wR = "+wR); 
        return wR; 
	}
	
	public double dotProductbyString(String v1, String v2, String sep){
		
		String[] s1 = v1.split(sep);
		String[] s2 = v2.split(sep);
		
		double result = 0; 		  
		for (int i = 0; i < s1.length; i++) {
       	  result = result + (Double.valueOf(s1[i])) * Double.valueOf(s2[i]);
       	 
		} 	
		return result;
	}
	
	public double dotProduct(double[] v1, double[] v2){
		
		double result = 0; 		  
		for (int i = 0; i < v1.length; i++) {
			
       	  result = result + v1[i] * v2[i];
		} 	
		return result;
	}


}
