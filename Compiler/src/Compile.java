import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;


/*
 * Compile CLass
 * Class takes care of parsing out the testFile given the grammar and
 * SLR table. If all goes well it outputs success, if it does not pass it says
 * which item and state it failed at.
 * */
public class Compile {
	public class StackStruct {
		public String symbol;
		public Integer state;

		public StackStruct(String s, int st) {
			symbol = s;
			state = st;
		}
	}

	private SLRTable slr;
	private Grammar grammar;
	private String text;
	private ArrayList<String> SpecialCase = new ArrayList<String>();
	private ArrayList<String> KEYS;
	private Stack<String> stack = new Stack<String>();
	private ArrayList<String> tree = new ArrayList<String>();
	
	public Compile(SLRTable S, Grammar G, String textFile) throws IOException {
		slr = S;
		grammar = G;
		KEYS = getKeywords();
		specialCases();
		text = readFile(textFile);

		modifyText();
		System.out.println(text);
		makeLexar();

	}
	//create a list of specific symbols to ignore
	public void specialCases() {
		SpecialCase.add("(");
		SpecialCase.add(")");
		SpecialCase.add("{");
		SpecialCase.add("}");
		SpecialCase.add("id");
		SpecialCase.add("[");
		SpecialCase.add("]");
		SpecialCase.add("+");
		SpecialCase.add("*");
		SpecialCase.add("=");
		SpecialCase.add("/");
	}
	//parse out all of the unneccessary values
	public void modifyText() {
		// remove comments
		text = text.replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", "");
		// remove newlines
		text = text.replaceAll("\n", " ");
		// remove tabs
		text = text.replaceAll("\t", " ");
		// text = text.replaceAll("[(]", " ( ");
		// space out identifiers
		spaceWords();
		parseIDs();
		text = text.replace("  ", " ");
		

	}
	//convert ID's to id and data types to there effective types
	public void parseIDs() {
		// for numbers
		String newString = "";
		text = text.replaceAll(",", " , ");
		text = text.replaceAll("\\d+", " num ");

		for (String s : text.split("\\s+")) {
			if (!grammar.terminals.contains(s.trim()) && !s.equals("")) {
				newString += " id ";
			} else {
				newString += " " + s.trim() + " ";
			}
		}

		text = "";
		text = newString;
	}
	//space out all of the symbols, this will make it easier for parsing
	
	public void spaceWords() {
		for (String s : grammar.getTerminals()) {
			if (!s.isEmpty()) {
				if (!SpecialCase.contains(s.trim())) {
					text = text.replace(s.trim(), " " + s.trim() + " ");

				} else if (s.trim().equals("id")) {
					text = text.replaceAll("void", " " + "void" + "");
					text = text.replaceAll("\\s+id\\s+", " " + "id" + "");
				} else if (s.trim().equals("=")) {
					text = text.replaceAll("\\==", " == ");
					text = text.replaceAll("\\=^=", " = ");
				} else {
					text = text.replaceAll("\\" + s.trim(), " " + s.trim() + " ");
				}

			}
		}
	}

	// get all the keywords for C-
	public ArrayList<String> getKeywords() {
		ArrayList<String> l = new ArrayList<String>();
		l.add("else");
		l.add("if");
		l.add("int");
		l.add("return");
		l.add("void");
		l.add("white");
		return l;
	}
	//Basic file reader, returns the file as an array of text
	public String readFile(String textFile) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(textFile));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	//Makes the Lexar table, if it is not able to be parsed 
	//then it will show an error and the value it failed on
	public void makeLexar() {

		stack.push("0");
		for (String str : text.split("\\s+")) {
			System.out.println(str);
			if (!str.isEmpty()) {
				if (compute(str.trim())) {
					
				} else {
					break;
				}
			}

		}
		if (compute("$")) {
			System.out.println("Success!");
		}

	}
	//this takes the input of a given symbol and decides where to go 
	//given the SLR table
	public boolean compute(String token) {
		while (true) {
			if (token.equals("$") && (stack.peek().equals("1"))) {
				return true;
			}
			StackStruct move = getMove(token, stack.peek());

			if (move.symbol.equals("Invalid")) {
				return false;
			} else {

				if (move.symbol.equals("G")) {
					stack.push(move.state.toString());

				} else if (move.symbol.equals("S")) {
					stack.push(token);
					stack.push(move.state.toString());
					break;
				} else if (move.symbol.equals("R")) {
					reductionMove(move);
					// break;
				}
			}
		}

		return true;

	}
	//the is the move in which the symbol is consumed and the production is
	//pushed onto the tree
	public void reductionMove(StackStruct move) {
		String var = slr.productions.get(move.state);
		String lhs = var.trim().split("::")[0];
		int length = 0;
		// parse out the rhs, then get that length
		if (var.trim().split("\\s+").length == 2) {
			length = 0;
		} else {
			length = var.trim().split("::")[1].trim().split("\\s+").length;
		}
		
		length *= 2;
		// System.out.println(length);
		if (length != 0) {
			for (int i = 0; i < length; ++i) {
				stack.pop();
			}
		}
		tree.add(" Production #: " + move.state + " Production: " + var);
		String holder = stack.peek();
		stack.push(lhs);
		stack.push(getShift(lhs, holder));
	}
	//this is when the shift value is given and they need to shift 
	//through the values
	public String getShift(String Symbol, String stateNum) {
		for (Item it : slr.stateList.get(Integer.parseInt(stateNum))) {
			if (it.symbol.trim().equals(Symbol.trim())) {
				return String.valueOf(it.state);
			}
		}
		return "-1";
	}
	//This is a structure that contains the item and the state
	public StackStruct getMove(String item, String state) {

		for (Item it : slr.stateList.get(Integer.parseInt(state.trim()))) {
			if (it.symbol.equals(item)) {

				return (new StackStruct(it.command, it.state));
			}
		}
		System.out.println("Failed State: " + state + "Symbol:" + item);
		return new StackStruct("Invalid", -1);
	}
	//returns the lexar tree
	public ArrayList<String> getTree() {
		return tree;
	}
}
