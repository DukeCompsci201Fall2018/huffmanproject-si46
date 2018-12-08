
/**
 * Although this class has a history of several years,
 * it is starting from a blank-slate, new and clean implementation
 * as of Fall 2018.
 * <P>
 * Changes include relying solely on a tree for header information
 * and including debug and bits read/written information
 * 
 * @author Owen Astrachan
 */

public class HuffProcessor {

	public static final int BITS_PER_WORD = 8;
	public static final int BITS_PER_INT = 32;
	public static final int ALPH_SIZE = (1 << BITS_PER_WORD); 
	public static final int PSEUDO_EOF = ALPH_SIZE;
	public static final int HUFF_NUMBER = 0xface8200;
	public static final int HUFF_TREE  = HUFF_NUMBER | 1;

	private final int myDebugLevel;
	
	public static final int DEBUG_HIGH = 4;
	public static final int DEBUG_LOW = 1;
	
	public HuffProcessor() {
		this(0);
	}
	
	public HuffProcessor(int debug) {
		myDebugLevel = debug;
	}

	/**
	 * Compresses a file. Process must be reversible and loss-less.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be compressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	
	
	public int[] readForCounts(BitInputStream in) {
		int[] store = new int[ALPH_SIZE + 1];
		int bits= in.readBits(BITS_PER_WORD);
		if (bits == -1) {
			throw new HuffException("out of bits");
		}
		store[bits]+=1;
		return null;
	}
	
	public HuffNode makeTreeFromCounts(int[] counts) {
		return null;
	}
	
	public String[] makeCodingsFromTree(HuffNode rootnode) {
		return null;
	}
	
	public void writeHeader(HuffNode rootnode, BitOutputStream out) {
		
	}
	
	public void writeCompressedBits(String[] encodings, BitInputStream in, BitOutputStream out) {
		
	}
	
	public void compress(BitInputStream in, BitOutputStream out){

		/*while (true){
			int val = in.readBits(BITS_PER_WORD);
			if (val == -1) break;
			out.writeBits(BITS_PER_WORD, val);
		}*/
		int[] counts = readForCounts(in); //determine frequency of every 8 bit character chunk in file being compressed
		HuffNode root = makeTreeFromCounts(counts); //create tree
		String[] codings = makeCodingsFromTree(root); //from tree, create encodings for each 8 bit character chunk
		
		out.writeBits(BITS_PER_INT, HUFF_TREE);   // write magic number and tree to
		writeHeader(root,out);                    // the beginning/header of the compressed file
		
		in.reset();                                      //read file again 
		writeCompressedBits(codings,in,out);             //write encodings for 8 bit chunk followed by encoding for
		out.close();                                     //PSEUDO_EOF, then close file being written
	}
	/**
	 * Decompresses a file. Output file must be identical bit-by-bit to the
	 * original.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be decompressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	
	public HuffNode readTreeHeader(BitInputStream in) {
		int bit = in.readBits(1);
		if (bit == -1) {
			throw new HuffException("out of bits in reading tree header");
		}
		if (bit==0) {
			HuffNode left= readTreeHeader(in);
		    HuffNode right = readTreeHeader(in);
		    HuffNode node= new HuffNode(0,0,left,right);
		    return node;
		}
		else {
		    int value = in.readBits(BITS_PER_WORD +1);
		    HuffNode node = new HuffNode(value,0,null,null);
		    return node;
		}
		
		
	}
	
	public void readCompressedBits(HuffNode rootnode, BitInputStream in, BitOutputStream out) {
		HuffNode current = rootnode;
		while (true) {
			int bits = in.readBits(1);
			if (bits== -1) {
				throw new HuffException("bad input, no PSEUDO_EOF");
			}
			else {
				if (bits == 0) current = current.myLeft;
				if (bits == 1) current=current.myRight;
				
				if (current.myLeft==null && current.myRight==null) {
					if (current.myValue == PSEUDO_EOF)
						break; //out of loop
					else {
						out.writeBits(BITS_PER_WORD,current.myValue);
						current= rootnode; //start back after leaf
					}
				}
			}
		}
	}
	
	public void decompress(BitInputStream in, BitOutputStream out){
		
		int bits= in.readBits(BITS_PER_INT);
		if (bits != HUFF_TREE) {
			throw new HuffException("illegal header starts with "+bits);
		}   
		//make sure file is Huffman decoded by checking that first 32 bits are the value HUFF_TREE
		
		HuffNode root = readTreeHeader(in);  
		//Helper Method to read tree used to decompress
		
		readCompressedBits(root,in,out);    
		//Helper Method; read bits from compressed file and use them to
		//traverse root-to-leaf paths, writing leaf values to the output file. STOP when finding PSEUDO_EOF
		
		out.close(); 
		//close output file

	}
}