/*
 	Name: Naman Gangwani
 	NetID: nkg160030
 	Class: CS 3345.501
 	Date: October 9, 2017
 */

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Homework3 {
	public static void main(String[] args) throws IOException
	{
		BufferedReader dict = new BufferedReader(new FileReader("dictionary.txt")); // Opens dictionary file
		
		// Initializes and instantiates all data structures
		LinearHashTable linear = new LinearHashTable(310571, (double) 155285/310571);
		QuadraticHashTable quadratic = new QuadraticHashTable(310571, (double) 155285/310571);
		DoubleHashingHashTable doubleHashing = new DoubleHashingHashTable(310571, (double) 155285/310571);
		SeparateChainingHashTable separateChaining = new SeparateChainingHashTable(155285, (double) 155285/155285);
		
		//long startTime = System.nanoTime();
		
		int numWords = 0; // Count for the number of words in the dictionary
		while (dict.ready()) // While there are more lines to read
		{
			String line = dict.readLine();
			numWords++; // Adds 1 to the count of the number of words
			
			// Inserts the line into all types of data structures simultaneously
			linear.put(line);
			quadratic.put(line);
			separateChaining.put(line);
			doubleHashing.put(line);
		}
				
		//long estimatedTime = System.nanoTime() - startTime;
		//System.out.println(((double)estimatedTime)/1000000000);
		
		Scanner kb = new Scanner(System.in);
		String word = "";
		
		while (true) // Infinitely ask for new words
		{
			System.out.print("Word to search for (or \'!exit\' to exit): "); // Prompts for user input
			word = kb.nextLine();
			word = word.toLowerCase().trim().replaceAll(" ", "_"); // Adjusts input so that it can be found in the dictionary
			
			if (word.equals("!exit"))
				break; // Exit
			
			// Retrieves all the words from all the respective data structures
			String dictionarySearch1 = linear.get(word);
			String dictionarySearch2 = quadratic.get(word);
			String dictionarySearch3 = separateChaining.get(word);
			String dictionarySearch4 = doubleHashing.get(word);
			
			// Displays output in an orderly format
			System.out.println("\n--------------------------------------------------------------------------");
			if (dictionarySearch1 == null && dictionarySearch2 == null && dictionarySearch3 == null && dictionarySearch4 == null)
				System.out.println("Sorry, this word is not in the dictionary."); // Word could not be found
			else
			{
				// Finds the one(s) that is/are not null
				String definition = dictionarySearch1;
				if (dictionarySearch2 != null) definition = dictionarySearch2;
				if (dictionarySearch3 != null) definition = dictionarySearch3;
				if (dictionarySearch4 != null) definition = dictionarySearch4;
				
				String[] categories = definition.split(Pattern.quote("|"));

				System.out.println(categories[0].replaceAll("_", " ")+ " ("+categories[1]+"):\n" +categories[2]); // Prints word, type, and definition
			}
			
			// Displays the results of all the data structures
			System.out.println("\nTotal Words: "+numWords);
			System.out.printf("\n%-22s%-14s%-9s%-11s%-22s\n", "Data Structure", "Table Size", "Lamda", "Success", "Items Investigated");
			System.out.printf("\n%-22s%-14d%-9.3f%-11s%-22d", "Linear Probing", linear.size, Math.floor(linear.loadFactor * 1000)/1000, linear.success, linear.itemsInvestigated);
			System.out.printf("\n%-22s%-14d%-9.3f%-11s%-22d", "Quadratic Probing", quadratic.size, Math.floor(quadratic.loadFactor * 1000)/1000, quadratic.success, quadratic.itemsInvestigated);
			System.out.printf("\n%-22s%-14d%-9.3f%-11s%-22d", "Separate Chaining", separateChaining.size, Math.floor(separateChaining.loadFactor * 1000)/1000, separateChaining.success, separateChaining.itemsInvestigated);
			System.out.printf("\n%-22s%-14d%-9.3f%-11s%-22d\n", "Double Hashing", doubleHashing.size, Math.floor(doubleHashing.loadFactor * 1000)/1000, doubleHashing.success, doubleHashing.itemsInvestigated);
			
			System.out.println("--------------------------------------------------------------------------\n");
		}
		
		System.out.println("\nDictionary closed.");

		dict.close(); // Closes the dictionary file
		kb.close(); // Closes the keyboard input scanner
	}
}

/*
	Class that all the different data structures inherit for a basic hash table structure
	Includes methods that converts string to a key, insertion, & retrieval
	Index retrieval must be implemented by any class that inherits it
 */
abstract class HashTable
{
	public String[] table;
	public double loadFactor;
	public int size, itemsInvestigated;
	public String success;
	
	/* Primary Constructor */
	public HashTable(int size, double loadFactor)
	{
		this.size = size;
		this.loadFactor = loadFactor;
		table = new String[size];
		itemsInvestigated = 0;
		success = "No";
	}
	
	/* Hash Function method */
	public int getKey(String name)
	{
		// Multiplies the ASCII values of the first, middle, and last character and returns it
		int first = name.charAt(0), middle = name.charAt(name.length()/2), last = name.charAt(name.length() - 1);
		return (first * middle * last);
	}
	
	/* Insertion method that inserts the given line at it calculated index */
	public void put(String line) 
	{
		table[getIndex(line.substring(0, line.indexOf("|")), false)] = line;
	}
	
	/* Retrieval method that retrieves the information of the given word at its calculated index */
	public String get(String name) 
	{
		int index = getIndex(name, true); // Gets where it should be in the hash table
		
		success = "No";
		if (index != -1) // If it was found in the hash table
		{
			if (table[index] != null) // If there's a value for it stored in the hash table
				success = "Yes"; // Success
			return table[index];
		}
		else
			return null; // It was caught in infinite probing
	}
	
	/* Method to be implemented by any class that inherits this class; Retrieves the index from a given string */
	public abstract int getIndex(String name, boolean searchFor);
}

/* Linear hash table data structure */
class LinearHashTable extends HashTable
{
	/* Primary Constructor */
	public LinearHashTable(int size, double loadFactor) { super(size, loadFactor); }
	
	/* 
	 	Gets the index of the name in the table based on its key
	 	Utilizes linear probing for collision handling
	 	If searchFor is false, it will only look for an open spot
	 	If searchFor is true, it will look for a the index that contains the given name
	 */
	@Override
	public int getIndex(String name, boolean searchFor)
	{
		int key = getKey(name); // Retrieves key
		int adder = 0, index = (key + adder) % size; // Initial index
		itemsInvestigated = 1;
		// While it can't find an open spot or a spot that contains the given name
		while ((!searchFor && table[index] != null) ||
		(searchFor && table[index] != null && !table[index].substring(0, table[index].indexOf("|")).equals(name)))
		{
			index = (key + ++adder) % size; // Linearly probes
			if (searchFor) itemsInvestigated++; // Adds one to the count of number investigated
		}
		
		return index;
	}
}

/* Quadratic hash table data structure */
class QuadraticHashTable extends HashTable
{
	/* Primary Constructor */
	public QuadraticHashTable(int size, double loadFactor) { super(size, loadFactor); }
	
	/* 
	 	Gets the index of the name in the table based on its key
	 	Utilizes quadratic probing for collision handling
	 	If searchFor is false, it will only look for an open spot
	 	If searchFor is true, it will look for a the index that contains the given name
	 */
	@Override
	public int getIndex(String name, boolean searchFor)
	{
		int key = getKey(name); // Retrieves the key
		int adder = 0, index = (key + (int) Math.pow(adder, 2)) % size; // Initial index
		itemsInvestigated = 1;
		// While it can't find an open spot or a spot that contains the given name
		while ((!searchFor && table[index] != null) ||
		(searchFor && table[index] != null && !table[index].substring(0, table[index].indexOf("|")).equals(name)))
		{
			index = (key + (int) Math.pow(++adder, 2)) % size; // Quadratically probes
			if (searchFor) itemsInvestigated++; // Adds one to the count of number investigated
			if (index == key % size) // If it has completely cycled through the table
				return -1; // It was not found
		}
		
		return index;
	}
}

/* Double hashing hash table data structure */
class DoubleHashingHashTable extends HashTable
{
	private int p, q = 310567;
	
	public DoubleHashingHashTable(int size, double loadFactor) {
		super(size, loadFactor);
		p = size;
	}
	
	/* The h(key) function that is specific to double hashing only; p must be prime */
	public int h(int key) { return key % p; }
	
	/* The h(key) function that is specific to double hashing only; q must be prime and < p and > 2 */
	public int g(int key)
	{
		int g = q - (key % p);
		if (g == 0) // Assures that g(key) does not return 0 (otherwise it would be useless)
			g = 3;
		return g;
	}
	
	/* 
	 	Gets the index of the name in the table based on its key
	 	Utilizes double hashing for collision handling
	 	If searchFor is false, it will only look for an open spot
	 	If searchFor is true, it will look for a the index that contains the given name
	 */
	@Override
	public int getIndex(String name, boolean searchFor)
	{
		int key = getKey(name); // Retrieves the key
		int h = h(key), g = g(key);
		int adder = 0, index = (h + adder * g) % size; // Initial index
		itemsInvestigated = 1;
		// While it can't find an open spot or a spot that contains the given name
		while ((!searchFor && table[index] != null) || 
		(searchFor && table[index] != null && !table[index].substring(0, table[index].indexOf("|")).equals(name)))
		{
			if (searchFor) itemsInvestigated++; // Adds 1 to the count of number investigated
			index = (h + ++adder * g) % size; // Double hashes to the next index
			if (index == h % size) // If it has completely cycled through the table
				return -1; // It was not found
		}
		return index;
	}
	
}

/* Separate chaining hash table data structure; not open addressing unlike the other three */
class SeparateChainingHashTable extends HashTable
{
	private Chain[] table; // Needs its own hash table containing chains
	public String success; // Calculates its own success
	
	/* Primary Constructor */
	public SeparateChainingHashTable(int size, double loadFactor) {
		super(size, loadFactor);
		table = new Chain[size];
		for (int i = 0; i < size; i++)
			table[i] = new Chain(); // Instantiates a new chain for all the elements
		success = "No";
	}
	
	/* Chain requires its own insertion method since it is not open addressing */
	@Override
	public void put(String line) 
	{
		// Inserts simply from the key value into its respective chain
		int index = getIndex(line.substring(0, line.indexOf("|")), false);
		table[index].insert(line);
	}
	
	/* Chain requires its own retrieval method since it is not open addressing */
	@Override
	public String get(String name)
	{
		int index = getIndex(name, false); // Retrieves index based on the name
		WordInformation cur = table[index].head; // Starts from the head
		success = "No";
		itemsInvestigated = 1;
		while (cur != null) // Goes through the entire chain
		{
			if (cur.value.substring(0, cur.value.indexOf("|")).equals(name)) // If it's the word we're looking for
			{
				success = "Yes";
				return cur.value; // Return its word
			}
			cur = cur.right; // Move to the next word
			itemsInvestigated++; // Add 1 to the count of number investigated
		}
		return null;
	}
	
	/* 
 	Gets the index of the name in the table based on its key
	searchFor is useless for the implementation of chaining
	 */
	@Override
	public int getIndex(String name, boolean searchFor) { return getKey(name) % size; }
}

/* LinkedList stack implementation of a chain for the chaining hash table data structure */
class Chain
{
	public WordInformation head;
	
	public void insert(String value)
	{
		WordInformation info = new WordInformation(value); // Creates new node of the info
		info.right = head; // Sets it at the beginning
		head = info; // Newly inserted node is the new head
	}
}

/* Object to contain the word information (name, type, definition) of a word */
class WordInformation
{
	public String value; // Contains its info
	public WordInformation right; // Has only a right pointer
	
	/* Primary Constructor */
	public WordInformation(String value) { this.value = value; }
}