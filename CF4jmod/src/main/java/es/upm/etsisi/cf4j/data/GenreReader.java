package es.upm.etsisi.cf4j.data;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class GenreReader {

	/** Item and genres map */
	protected HashMap<String, String> genres = new HashMap<String, String>();
	protected List<String> genreList = new ArrayList<String>();
	public String separator;

	public GenreReader(String filename, String separator, String genre_separator) throws IOException {
		this.separator = separator;
		System.out.println("\nLoading genres...");

		// Dataset reader
		BufferedReader datasetFile = new BufferedReader(new FileReader(new File(filename)));
		String line;

		int numLines = 0;
		if (separator.equals("|")) {
			System.out.println("separator = | = " + separator);
			
			while ((line = datasetFile.readLine()) != null) {
				System.out.println("WHILE INSIDE separator = | = " + separator);
				// Loading feedback
//	        System.out.println("\nLoading feedback...");

				numLines++;
				if (numLines % 1000000 == 0)
					System.out.print(".");
				if (numLines % 10000000 == 0)
					System.out.println(numLines + " ratings");

				// Parse line
				System.out.println(line);
				String[] s = line.split(separator);
//		        String userId = s[0];
				String itemId = line.substring(0, line.indexOf(separator));
//		        double rating = Double.parseDouble(s[2]);

//		        This works fine for ml-100k dataset
				String itemGenre = line.substring(line.length() - 38);
//		        System.out.println(line.substring(1, 30));

				itemGenre = itemGenre.replace(genre_separator, ",");
				itemGenre = itemGenre.replaceFirst(",", "");
//		        
//		        int number0 = Integer.parseInt(itemGenre, 2);

//		        int number1 = Integer.parseInt(input1, 2);
				this.genres.put(itemId, itemGenre);

//		        System.out.println(Integer.toBinaryString(number0));

				System.out.println(itemId);
				System.out.println(itemGenre);
				

			}
			//end of while loop
		} else if (separator.equals("::")) {
			System.out.println("separator = :: = " + separator);
			
			// First loop get all categories and insert into a list
			while ((line = datasetFile.readLine()) != null) {
				System.out.println("WHILE INSIDE separator = | = " + separator);

				
				String[] s2 = line.split(separator);
				String categories = s2[s2.length-1];
				String[] cat = categories.split(genre_separator);
//				System.out.println(categories);
				
				for (int i = 0; i < cat.length; i++) {
					if (!(genreList.contains(cat[i]))) {
						genreList.add(cat[i]);	
					}
				}
				
				System.out.println(genreList);
				System.out.println(genreList.size());
			}
			//Second loop
			
			
			System.out.println("============second loop====");

//			char[] chars = new char[genreList.size()];
//			Arrays.fill(chars, '0');
//			String itemGenre = new String(chars);
			datasetFile.close();
			datasetFile = new BufferedReader(new FileReader(new File(filename)));
			int counter = 0;
			while ((line = datasetFile.readLine()) != null) {
				System.out.println("============Insdie second loop====");
				counter += 1;
				StringBuilder itemGenreB = new StringBuilder();
				for (int i = 0; i < genreList.size(); i++) {
					itemGenreB.append("0");
				}
				
				System.out.println(line);
				String[] s2 = line.split(separator);
//				String itemId = line.substring(0, line.indexOf(separator));
				String itemId = Integer.toString(counter);
				
				String categories = s2[s2.length-1];
				String[] cat = categories.split(genre_separator);
				for (int i = 0; i < cat.length; i++) {
					itemGenreB.setCharAt(genreList.indexOf(cat[i]), '1');
				}
				
				 char[] itemGenrechar = itemGenreB.toString().toCharArray();
				 StringBuilder builder = new StringBuilder();
				 for (int i = 0; i < itemGenrechar.length; i++){
				     builder.append(itemGenrechar[i]);
				     if (i < itemGenrechar.length - 1){
				         builder.append(",");
				     }
				 }
				
				
				String itG = builder.toString();
				this.genres.put(itemId, itG);
				System.out.println(itG);
				System.out.println(itemId);
				
				
			
			}

			

		}
		System.out.println("this.genres.size() =" + this.genres.size());
	}

	public HashMap<String, String> getAllItemGenres() {
		return this.genres;
	}

	public String getItemGenres(String itemId) {
		return this.genres.get(itemId);
	}

	public int getItemGenresInBinaryInt(String itemId) {
		return Integer.parseInt(this.genres.get(itemId), 2);
	}

}
