/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/

 import java.lang.Math;
 
public class MyLZW {
    private static int R = 256;        // number of input chars
    private static int L = 512;       // number of codewords = 2^W
    private static int W = 9;         // codeword width
	private static char mode = 0;
	private static double numerator = 0;
	private static double denominator = 0;
	private static double cRatio = 0;
	private static double sRatio = 0;
	private static boolean ratio = true;

    public static void compressNothing() { 
        String input = BinaryStdIn.readString();
		BinaryStdOut.write('n');
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF

        while (input.length() > 0) {
			if (code == L && W != 16)
			{
				W++;
				L = (int) Math.pow(2, W);
			}
			
			String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
            if (t < input.length() && code < L)    // Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 


    public static void expandNothing() {
        String[] st = new String[L];
		
		if (mode == 0)
		{
			mode = BinaryStdIn.readChar();
		}
		
		if (mode == 'r')
		{
			expandReset();
			return;
		}
		else if (mode == 'm')
		{
			expandMonitor();
			return;
		}
		
        int i; // next available codeword value
		int z = 0;
		
        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];

        while (true) {
			if (i == L-1 && W != 16)
			{
				W++;
				String[] temp = new String[L];
				L = (int) Math.pow(2, W);
				for (int x=0; x<temp.length; x++)
				{
					temp[x] = st[x];
				}
				st = new String[L];
				
				for (int x = 0; x<temp.length; x++)
				{
					st[x] = temp[x];
				}
			}
            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L) st[i++] = val + s.charAt(0);
            val = s;
        }
        BinaryStdOut.close();
    }



    public static void main(String[] args) {
        if (args[0].equals("-")) 
		{
			if (args[1].equals("n"))
			{
				compressNothing();
			}
			else if (args[1].equals("r"))
			{
				compressReset();
			}
			else if (args[1].equals("m"))
			{
				compressMonitor();
			}
		}
        else if (args[0].equals("+")) 
		{
			expandNothing();
		}
        else throw new IllegalArgumentException("Illegal command line argument");
    }
	
	public static void compressReset() { 
        String input = BinaryStdIn.readString();
		BinaryStdOut.write('r');
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF

        while (input.length() > 0) {
			String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
			int t = s.length();
			if (t < input.length() && code < L)    // Add s to symbol table.
            st.put(input.substring(0, t + 1), code++); 
			
			if (code == 65536)
			{
				st = new TST<Integer>();
				for (int i = 0; i < R; i++)
				st.put("" + (char) i, i);
				code = R+1;
				W = 9;
				L = 512;
			}
			if ((W<16) && ((int)Math.pow(2, W) == code))
			{
				W++;
				L = (int)Math.pow(2, W);
				st.put(input.substring(0, t+1), code++);
			}
			input = input.substring(t);
			
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 
	
	public static void compressMonitor() { 
        String input = BinaryStdIn.readString();
		BinaryStdOut.write('m');
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF

        while (input.length() > 0) {
			String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
			int t = s.length();
			if (t < input.length() && code < L)    // Add s to symbol table.
            st.put(input.substring(0, t + 1), code++); 
			
			if (code == 65536)
			{
				numerator += s.length()*8;
				denominator += W;
				cRatio = numerator/denominator;
				if (ratio)
				{
					sRatio = cRatio;
					ratio = false;
				}
				if (sRatio/cRatio > 1.1)
				{
					st = new TST<Integer>();
					for (int i = 0; i < R; i++)
					st.put("" + (char) i, i);
					code = R+1;  // R is codeword for EOF
					W = 9;
					L = 512;
					sRatio = 0;
					cRatio = 0;
					ratio = true;
				}
			}
			if ((W<16) && ((int)Math.pow(2, W) == code))
			{
				W++;
				L = (int)Math.pow(2, W);
				st.put(input.substring(0, t+1), code++);
			}
			input = input.substring(t);
			
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 
	
	public static void expandReset() {
		boolean toBreak = false;
		String [] st = new String[(int) Math.pow(2, 16)];
		int i;
		if (mode ==0)
		{
			mode = BinaryStdIn.readChar();
		}
		if (mode == 'n')
		{
			expandNothing();
			return;
		}
		else if (mode == 'm')
		{
			expandMonitor();
			return;
		}
		
		for (i = 0; i < R; i++)
		st[i] = "" + (char) i;
		st[i++] = "";
		
		int codeword = BinaryStdIn.readInt(W);
		if (codeword == R) return;
		String val = st[codeword];
		
		while (true)
		{
			BinaryStdOut.write(val);
			codeword = BinaryStdIn.readInt(W);
			
			if (codeword == R) break;
			String s = st[codeword];
			if (i == codeword) s = val + val.charAt(0);
			if (i < L-1) st[i++] = val + s.charAt(0);
			
			if (i == L-1 && W<16)
			{
				st[i++] = val + s.charAt(0);
				W++;
				L = (int) Math.pow(2, W);
			}
			val = s;
			
			if (i == 65535)
			{
				W = 9;
				L = 512;
				st = new String[(int)Math.pow(2, 16)];
				for (i = 0; i<R; i++)
					st[i] = "" + (char) i;
				st[i++] = "";
				
				BinaryStdOut.write(val);
				
				codeword = BinaryStdIn.readInt(W);
				if (codeword == R) return;
				val = st[codeword];
			}
		}
		BinaryStdOut.close();
        
    }
	
	public static void expandMonitor() {
		boolean toBreak = false;
		String [] st = new String[(int) Math.pow(2, 16)];
		int i;
		if (mode ==0)
		{
			mode = BinaryStdIn.readChar();
		}
		if (mode == 'n')
		{
			expandNothing();
			return;
		}
		else if (mode == 'r')
		{
			expandReset();
			return;
		}
		
		for (i = 0; i < R; i++)
		st[i] = "" + (char) i;
		st[i++] = "";
		
		int codeword = BinaryStdIn.readInt(W);
		if (codeword == R) return;
		String val = st[codeword];
		
		while (true)
		{
			BinaryStdOut.write(val);
			codeword = BinaryStdIn.readInt(W);
			
			if (codeword == R) break;
			String s = st[codeword];
			if (i == codeword) s = val + val.charAt(0);
			if (i < L-1) st[i++] = val + s.charAt(0);
			
			if (i == L-1 && W<16)
			{
				st[i++] = val + s.charAt(0);
				W++;
				L = (int) Math.pow(2, W);
			}
			val = s;
			
			if (i == 65535)
			{
				numerator += val.length() * 8;
				denominator += W;
				cRatio = numerator/denominator;
				
				if (ratio)
				{
					sRatio = cRatio;
					ratio = false;
				}
				
				if (sRatio/cRatio > 1.1)
				{
					W = 9;
					L = 512;
					st = new String[(int)Math.pow(2, 16)];
					for (i = 0; i<R; i++)
						st[i] = "" + (char) i;
					st[i++] = "";
					
					BinaryStdOut.write(val);
					
					codeword = BinaryStdIn.readInt(W);
					if (codeword == R) return;
					val = st[codeword];
					sRatio = 0;
					cRatio = 0;
					ratio = true;
				}
			}
		}
		BinaryStdOut.close();
    }
}