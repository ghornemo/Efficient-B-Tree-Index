/*		
		A file that creates a disk B-tree index.
		The index can then be used to query a single value
		or queried for a range of values.
		

		Author: Gemal Horne

*/

			//TODO:::: ADJUST BULK LOAD TO SUPPORT SAVING IN DISK INSTEAD OF MEMORY


import java.util.*;
import java.io.*;
import java.nio.*;

public class database {
	
	public final int COLUMN = 0; // Column to build index(starting from 0)
	public final int node_size = 4; //Max keys per node
	public BTree Tree = new BTree();
	public static RandomAccessFile treeFile, bigFile, indexFile, sortedIndex;
	public static BufferedWriter treeWriter;
	public static final long nodeSize = 100; // Byte size per node on disk
	public static long time, time2;
	public void treeWrite(String msg) { // Pads our string to the correct size, then writes it to the file.
		try {
		while(msg.length() < nodeSize-1) {
			msg += " ";
		}
		treeWriter.write(msg);
		treeWriter.newLine();
		//System.out.println("wrote a node");
		}catch(Exception e) {
			System.out.println("Failed to write node.");
		}
		
	}
	public static long nodeLocation(long position) {
		return nodeSize*position;
	}

	public database(String file, boolean build) {
		if(build) {
		loadDatabase(file);
		time2 = System.currentTimeMillis();
		Tree.load();
		System.out.println("Time to build tree index: "+ (System.currentTimeMillis()-time2)/1000+" seconds.");
		}else{
			try {
			treeFile = new RandomAccessFile(new File("tree.txt"), "r");
			bigFile = new RandomAccessFile(file, "r");
			}catch(Exception e) {System.out.println("failed to open file");
						System.out.println(e);}
		}
		System.out.println("Total time to build index system: "+ (System.currentTimeMillis() - time) / 1000 +" seconds.");
		Scanner scan = new Scanner(System.in);
		System.out.println("Specify search key to query");
		System.out.println("For range query, enter two search keys");
		while(true) {
			System.out.println("Ready for next command");
			String input = scan.nextLine();
			String[] s = input.split(" ");
			System.out.println("Command received: "+s[0]);
			
			if(s.length == 2) { // Range query
				time = System.currentTimeMillis();
				Tree.query(s[0], s[1]);
				System.out.println("Time to execute query: "+ (System.currentTimeMillis()-time)+" milliseconds.");
			} else if(s[0].equalsIgnoreCase("view")) { // View sorted data
				Tree.scan();
				Tree.printTree();
			} else if(s[0].equalsIgnoreCase("head")) { // View sorted data
				System.out.println(Tree.head());
			} else if(s[0].equalsIgnoreCase("quit")) { // View sorted data
				System.exit(0);
			} else if(s[0].equalsIgnoreCase("size")) { // View sorted data
				System.out.println("Size of tree: "+Tree.size());
			} else if(s.length == 1) {
				key key = Tree.query(s[0]);
				if(key != null)
					key.retrieve();
				else System.out.println("Record not found.");
			} else
				System.out.println("Invalid format.");
		}
	}

	public static void main(String[] args) {
		try {
		 time = System.currentTimeMillis();
		//treeFile = new RandomAccessFile(new File("tree.txt"), "rwd");
		if(args[1].equals("build")) {
		treeWriter = new BufferedWriter(new FileWriter("tree.txt", false));
		new database(args[0], true);	
		} else {
		new database(args[0], false);
		}	
		}catch(Exception e) {}
	}
		//Using this method to build an index file using randomAccess
		public void testRead(File file) {
			try {
				File index = new File("index.txt");
				bigFile = new RandomAccessFile(file, "r");
				indexFile = new RandomAccessFile(index, "rwd");	
				
				
				long pointerLocation = 0;
				String[] string;
				System.out.println("Attempting to read data file.");
				time2 = System.currentTimeMillis();
				BufferedWriter writer = new BufferedWriter(new FileWriter(index, false));
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String s = reader.readLine();
				long position = 0;//Position of our cursor.
				while(s != null) {
				Tree.size++;
				
					string = s.split(", ");
					writer.write(string[0]+" "+position/*pointerLocation*/+"\n");
					position += s.length()+1; // +1 for end of line character!
					//pointerLocation = bigFile.getFilePointer();
					s = /*bigFile*/reader.readLine();
				}
				writer.flush();
				System.out.println("tree size: "+Tree.size);
				System.out.println("Time to build index: "+ (System.currentTimeMillis()-time2)/1000+" seconds.");
				File sorted = new File("sortedIndex.txt");
				sorted.createNewFile();
				time2 = System.currentTimeMillis();
				//sortedIndex.setLength(0);
				System.out.println("Attempting to sort index file...");
				Runtime r = Runtime.getRuntime();
				Process p = new ProcessBuilder("sort.sh").start();//r.exec("./Home/Desktop/database/sort.sh");
				p.waitFor();
				sortedIndex = new RandomAccessFile(sorted, "rwd");
				System.out.println("index successfully sorted.");
				System.out.println("Time to sort index: "+ (System.currentTimeMillis()-time2)+" milliseconds.");
			}catch(Exception E) {System.out.println("I faced an exception!");}
		}

		//Reading keys from input file
	public void loadDatabase(String file) {
		File f = new File(file);
		if(!f.exists()) {
			System.out.println("Error: file "+file+" not found.");
			return;
		}
		testRead(f);
	}

	class BTree {
		public node head;
		public long size = 0;

		public node head() {
			node n = new node();
			n.location = 0;
			n.read(0);
			return n;
		}

		public void seek(key key) {
			if(key == null) { // No key was found.
				
				return;
			}
			// Do magic to retrieve the record from the file here.
			System.out.println(key);
		}

		public key query(String input) {
			//Begin search algorithm...	
			node tmp = head();
			long compare;
			long tmp1, tmp2;
			tmp2 = Integer.decode(input);
			while(!tmp.isLeaf()) { // Searching for appropriate leaf node.
				for(int i = 0; i < tmp.size(); i++) {
					//System.out.println(tmp);
					tmp1 = Long.decode(tmp.key[i].data());
					compare = tmp2-tmp1;                
					if(compare >= 1) { // input greater than key
						System.out.println("greater than");
						if(i < tmp.size()-1) continue;// condition to check next key
						tmp = tmp.pointer(i+1);
						break;
					}else if(compare == 0) { // input equal to key
						System.out.println("equal to");
						tmp = tmp.pointer(i+1);
						break;
					}else { // input less than key 
						//System.out.println("less than");
						tmp = tmp.pointer(i);
						break;
					}
				}
			}
			System.out.println("Leaf located:" +tmp);
			//Now, we have the leaf that the key would be in, if it exists. Time to search the leaf.
			for(int i = 0; i < tmp.size(); i++) {
				if(i >= tmp.size()) break;
				if(tmp.key[i].data().equals(input)) {
					return tmp.key[i]; // Found exact key!
				}
			}
		return null; // no match
		}

	public void query(String input, String input2) {
			//Begin search algorithm...	
			System.out.println("range query");
			node tmp = head();
			int compare;
			int longRange = Integer.decode(input2);
			int tmp2 = Integer.decode(input);
			int tmp1;
			while(!tmp.isLeaf()) { // Searching for appropriate leaf node.
				for(int i = 0; i < tmp.size(); i++) {
					tmp1 = Integer.decode(tmp.key[i].data());
					compare = tmp2-tmp1;                
					if(compare >= 1) { // input greater than key

						if(i < tmp.size()-1) continue;// condition to check next key

						tmp = tmp.pointer(i+1);
						break;
					}else if(compare == 0) { // input equal to key
						tmp = tmp.pointer(i+1);
						break;
					}else { // input less than key 
						tmp = tmp.pointer(i);
						break;
					}
				}
			}
			System.out.println("leaf located: "+tmp);
			//Now, we have the leaf that the key would be in, if it exists. Time to search the leaf.
			boolean finished = false;
				for(int i = 0; i < node_size; i++) {
					//if(i >= tmp.size()) break;
					if(Integer.decode(tmp.key[i].data())-tmp2 >= 0) { // First accepted value.	
						while(!finished) {				
							key tmpKey = tmp.key[i];
							//if(Integer.decode(tmpKey.data()) <= longRange)
							//tmpKey.retrieve(); // Found exact key!
							// Check to see if next key is also accepted.
							if(Integer.decode(tmp.key[i].data()) <= longRange) { // Next key is accepted!
								tmpKey.retrieve();	
								if(i == tmp.size()-1) {// Move to next leaf.
									tmp = tmp.next();
									i = 0;
								}else{//Read next key of this leaf.
									i++;
								}
							}else { // Went over! Whoops
								return; // not accepted
							}
						}
					}
					if(i == node_size-1) {
						i = -1;
						tmp = tmp.next();
						System.out.println(tmp);
					}
				}
	}
		
		public void load() { // bulk load operation. Converts variable 'data' to a B-Tree structure.
try {
			System.out.println("Building treeIndex.txt");
			int nodeCount = (int) Math.ceil(size() / node_size); // # leaf nodes
			node[] node = new leafNode[nodeCount];
			String[] firstKey = new String[nodeCount+1];
			node current = new leafNode();
			node previous = new leafNode();
			key k;
			int index = 1;
			int previousIndex = 0;
			boolean finished = false;
			BufferedReader reader = new BufferedReader(new FileReader(new File("sortedIndex.txt")));
			sortedIndex.seek(0);
			treeWrite(" ");
			//Creating our leaves...
			System.out.println("Creating "+nodeCount+" leaf nodes!");
			for(int i = 0; i < nodeCount; i++) { // Creating a single leaf
				current = new leafNode();
			
				for(int j = 0; j < node_size; j++) { // Looping through leaf keys
						String s = reader.readLine();
						//System.out.println("im just testing");
						if(s == null) continue;
						if(j == 0) current.location = nodeLocation(index++);
						String[] line = s.split(" ");
						k = new key(line[0]);
						k.setLocation(Long.decode(line[1]));
						current.add(k);
				}
				if(i != 0) { // Setting next reference
					previous.next = current.location();
					previous.write();
				}
				if(current.size() > 0)
				firstKey[index-1] = current.key[0].data();
				previous = current;
				//System.out.println("iteration "+i+" of "+nodeCount);
			}
			System.out.println("made it");
			current.write();
			//Creating inner nodes...
			String[] previousLayer = firstKey;
			int currentIndex = 0; // Index of previous layer that we are scanning.
			previousIndex = 1; // Where leaf nodes begin.
			while(!finished) { // This loop runs once for every layer of inner nodes
				nodeCount = (int) Math.ceil(nodeCount / (node_size+1)); // # inner nodes in layer + 1 (because of pointer variable next)
				previousLayer = firstKey;
				currentIndex = 1;
				if(nodeCount < 1) nodeCount = 1; // We need at least one node!
				firstKey = new String[nodeCount+1];
				System.out.println("Creating "+nodeCount+" inner nodes!");
				
				for(int i = 0; i < nodeCount; i++) { // Creating a single inner node
					if(currentIndex >= previousLayer.length || previousLayer[currentIndex] == null) break;// If out of keys: break. 

					current = new innerNode(); 
					current.location = nodeLocation(index++); // Index = next available slot to write in tree.txt
					current.pointer[0] = nodeLocation(previousIndex++); // PreviousIndex = next node in previous layer
					currentIndex++;

					for(int j = 0; j < node_size; j++) { //Allocating inner node pointers 
						if(currentIndex >= previousLayer.length || previousLayer[currentIndex] == null) break;
						k = new key(previousLayer[currentIndex++]);
						if(i == 0) head = current;
						if(j == 0) firstKey[i+1] = k.data();
						current.add(k); // Add key... Help?!
						current.pointer[j+1] = nodeLocation(previousIndex++); // Add pointer...
					}
				current.write();
				}
				if(nodeCount <= 1) { // Create our head. End of loop
					treeWriter.flush();
					treeFile = new RandomAccessFile(new File("tree.txt"), "rwd");
					finished = true;
					System.out.println("Head successfully created! "+index);
					current.writeHead();
					//current.printKids();
				}
				//previousIndex = index-nodeCount-1;
			}
			//if(head.key[0] == null) { 
			//head.setLocation(0);
			//head.write();
			System.out.println(head);
			//}
			System.out.println("Successfully built treeIndex.txt");
}catch(Exception e) {System.out.println("Error building tree.txt");
			System.out.println(e);}
			finally{
			}
		}
		public BTree() {
		}
		public void printTree(node tmp) {
			if(tmp.isLeaf()) return;
			System.out.println();
			/*for(node index : tmp.pointer) {
				if(index != null) {
					System.out.print(index);
				}
				System.out.print("      ");
			}
			System.out.println();
			for(node index : tmp.pointer) {
				if(index != null) {
					printTree(index);
				}
			}*/
		}
		public void printTree() {
			System.out.println(head);
			printTree(head);
		}
		public void scan() { // Print our sorted data
			/*Iterator iterator = data.iterator();
			int i = 0;
			while(iterator.hasNext()) {
				System.out.println("Record "+ ++i +": "+iterator.next());
			}
			System.out.println("Total records detected: "+i);*/
		}
		public long size() {
			return size;
		}
	}

	class node {

		long next;
		key[] key = new key[node_size];
		long location = 0; // Byte value of location inside our big data file		
		long[] pointer = new long[node_size+1];
		public int size = 0; // Number of keys in node
		boolean isLeaf = false;

		//Retrieves node at pointer p
		public node pointer(int p) {

			/*if(p >= size-1) { // Node requested is outside of our scope, unfortunately.
				System.out.println("Requesting ndoe from invalid pointer");
				return null;
			}*/
			node n = new node();
			n.read(pointer[p]);
			return n;
		}

		public void printKids() {
			for(int i = 0; i < size; i++) {
				System.out.println(pointer(i));
			}
		}

		//Retrieves node next
		public node next() {
			node n = new node();
			n.read(next);
			return n;
		}

		public long location() {
			return location;
		}

		public void read(long diskLocation) { // Initializes a node from diskLocation(for leaf nodes only!)
			location = diskLocation;
			/*treeFile is our file !
			I am going to need to read 4 things to disk.
			1: All of the values of this node's keys ( key[] )
			2: Leaf status
			3: All of the pointer values this node contains. (pointer[] variable and node next)
			4: size of the node
			*/
			try {
				treeFile.seek(location());

				String line = treeFile.readLine();
				String[] s = line.split(" ");

				int index = 0;

				size = Integer.decode(s[index++]); // Reading size of our node...

				isLeaf = (s[index++].equals("leaf")); //Reading node type
				if(isLeaf) System.out.println("Reading a leaf");

			for(int i = 0; i < size; i++) {// Reading key values.
				key k = new key(s[index++]);
				key[i] = k;
			}
			if(isLeaf) {
				for(int i = 0; i < size; i++) {// Reading key locations
					key[i].setLocation(Long.decode(s[index++]));
				}
				next = Long.decode(s[index]);
			}else{
				for(int i = 0; i < size+1; i++) {// Reading pointer locations
					pointer[i] = Long.decode(s[index++]);
				}
			}
	
			//Lastly, read the location of the next node(if one exists)...
			}catch(Exception E) {
				System.out.println(E);
				System.out.println("Failed to read a node");
			}
			
		}

		public void writeHead() { // Method to write our node to disk(used for inner nodes only) ! 
			/*treeFile is our file !
			I am going to need to write four things to disk.
			1: size of the node
			2: Whether I am a leaf
			3: key values ( key.data() )
			4: key pointers (pointer[] variable and node next)
			*/
			System.out.println("attempting to write head");
			String line = size+" "; 
			if(isLeaf()) {
				line = line+"leaf ";
			}else{
				line = line+"inner ";
			}
			for(int i = 0; i < size; i++) {// Writing keys to disk
				line = line+""+key[i].data()+" ";
			}

			for(int i = 0; i < size; i++) {// Writing our node pointers to disk
				line = line+""+pointer[i]+" ";
			}

			//Finally, adding the pointer to the next leaf node on disk
			if(isLeaf())
			line = line+""+next+" ";	
			else
			line += pointer[size];	

			// Next, attempt to write the information to disk.
			try {
			//treeFile.seek(location());
			// Pad our nodes...
			treeFile.seek(0);
			treeFile.writeBytes(line);
			//treeFile.writeBytes(line+"\n");
			}catch(Exception e) {
			System.out.println("Failed to add node. oh balls");			
			}
			
		}

		public void write() { // Method to write our node to disk(used for inner nodes only) ! 
			/*treeFile is our file !
			I am going to need to write four things to disk.
			1: size of the node
			2: Whether I am a leaf
			3: key values ( key.data() )
			4: key pointers (pointer[] variable and node next)
			*/
			String line = size+" "; 
			if(isLeaf()) {
				line = line+"leaf ";
			}else{
				line = line+"inner ";
			}
			for(int i = 0; i < size; i++) {// Writing keys to disk
				line = line+""+key[i].data()+" ";
			}

			for(int i = 0; i < size; i++) {// Writing our node pointers to disk
				line = line+""+pointer[i]+" ";
			}

			//Finally, adding the pointer to the next leaf node on disk
			if(isLeaf())
			line = line+""+next+" ";	
			else
			line += pointer[size];	

			// Next, attempt to write the information to disk.
			try {
			//treeFile.seek(location());
			// Pad our nodes...
			/*while(line.length() < nodeSize) {
				line += " ";
			}
			treeWriter.write(line+"\n");*/
			//treeFile.writeBytes(line+"\n");
			treeWrite(line);
			}catch(Exception e) {
			System.out.println("Failed to add node.");			
			}
			
		}

		public void setLocation(long destination) {
			location = destination;
		}

		public key nextKey(key current) {

			int index = 0;
			//Special case: Must hop to next leaf!
			if(key[size-1] == current && next() != null) {
				return next().key[0];
			}
			//Otherwise, search for index of current
			for(int i = 0; i < size-1; i++) {
				if(key[i] == current)
					return key[i+1];
			}			
		return null; // If null key returned, then there were no more keys to iterate.
		}

		public boolean isLeaf() {
			return isLeaf;
		}

		public void add(key toAdd) {
			key[size++] = toAdd;
		}

		public int size() {
			return size;
		}

		public String toString() {
			String s = "|";
			for(int i = 0; i < node_size; i++)
				s = s + " "+key[i]+" |";
			return s;
		}

		boolean isEmpty() {
			if(size == 0)
				return true;
			return false;
		}

	}

	class innerNode extends node {
		public innerNode() {
			isLeaf = false;
		}
	}

	class leafNode extends node {
		public void write() { // Method to write our node to disk(used for leaf nodes only) ! 
			/*treeFile is our file !
			I am going to need to write four things to disk.
			1: size of the node
			2: Whether I am a leaf
			3: key values ( key.data() )
			4: key pointers (pointer[] variable and node next)
			*/
			String line = size+" "; 
			if(isLeaf()) {
				line = line+"leaf ";
			}else{
				line = line+"inner ";
			}
			for(int i = 0; i < size; i++) {// Writing keys to disk
				line = line+""+key[i].data()+" ";
			}

			for(int i = 0; i < size; i++) {// Writing location of data origin from inside big Data CSV
				line = line+""+key[i].location()+" ";
			}

			//Finally, adding the pointer to the next leaf node on disk
			line = line+""+next+" ";			

			// Next, attempt to write the information to disk.
			try {
			//treeFile.seek(location());
			//treeFile.writeBytes(line+"\n");
			treeWrite(line);
			//System.out.println("Printing leaf. Pointer to next: "+next);
			}catch(Exception e) {
			System.out.println("Failed to add node.");			
			}
			
		}
		public leafNode() {
			isLeaf = true;
		}
		public boolean isLeaf() {
			return true;
		}
	}

	class key {
		String info;

		long location = 0; // Location of the data from the original big CSV file
		public long location() {
			return location;
		}
		public void setLocation(long destination) {
			location = destination;
		}
		public key(String data) {
			info = data;
		}
		public String toString() {
			return info;
		}

		public void retrieve() { //Retrieve data from our actual big CSV file.
			try {
			bigFile.seek(location());
			System.out.println(bigFile.readLine());
			}catch(Exception e) {
				System.out.println("Error retrieving record!");
			}
		}

		public String data() {
			return info;
		}
	}
class keyCompare implements Comparator<key> {

    @Override
		public int compare(key key1, key key2) {
			return key1.info.compareTo(key2.info);
		}
}
}
