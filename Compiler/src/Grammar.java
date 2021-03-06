import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

/*Assuming a grammar is given in a text file and 
//uses :: to signify a production, given the usual 
//best practice having Variables as capitals and symbols as
//non-capital characters/symbols
* Also finds first and follow given the grammar
*/
public class Grammar {

	public Set<String> terminals = new LinkedHashSet<String>();
	public Set<String> variables = new LinkedHashSet<String>();
	public List<String> productions = new ArrayList<String>();
	private Map<String, Set<String>> First = new TreeMap<String, Set<String>>();
	private Map<String, Set<String>> Follow = new TreeMap<String, Set<String>>();
	public String STARTSYMBOL;

	
	public Grammar(String fileName) {
		try {
			getProductionsFromFile(fileName);
			findTerminalsAndVarsFromList();
			terminals.add("$");
			findFirstFromList();
			findFollowFromList();
			//clean up empty case
			for(String s: Follow.keySet()){
				if(Follow.get(s).contains("empty")){
					Follow.get(s).remove("empty");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<String> newProd = new ArrayList<String>();
		for (String s : productions) {

			if (s.contains("empty")) {

				s = s.replace("empty", "");
				newProd.add(s);

			} else {
				newProd.add(s);
			}
		}
		productions.clear();
		productions.addAll(newProd);

	}
	//this takes in the grammar file and places all of the productions into the list
	private void getProductionsFromFile(String name) throws IOException {
		String fileText;
		try {
			File f = new File(name);
			FileReader inputFile = new FileReader(name);
			BufferedReader br = new BufferedReader(inputFile);

			while ((fileText = br.readLine()) != null) {
				productions.add(fileText);
			}
			STARTSYMBOL = productions.get(0).split("\\s+")[0];
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// go through the list and find all the terminals
	private void findTerminalsAndVarsFromList() {
		// iterate through productions identifying terminals
		for (String s : productions) {
			String[] VarList = s.split("\\s+");
			for (String str : VarList) {
				// check if it is not a Capital letter or production symbol

				if (str.charAt(0) < 'A' || str.charAt(0) > 'Z') {
					if (str.charAt(0) != ':') {

						terminals.add(str);
					}
				}
				// make sure it is not the production symbol
				else if (str.charAt(0) != ':') {
					variables.add(str);

				}
			}

		}
	}
	//go through the list and place the follow in the Map structure
	private void findFollowFromList() {
		ArrayList<String> list = new ArrayList<String>();
		for (String s : variables) {
			Follow.put(s, getFollow(s, list));
			list.clear();
		}

	}

	// check if the sets have changed. with a before and after
	// go through and add to the sets all of the follows, as well as the follow
	// of each set,
	// if the set has not changed do nothing.
	// go through until you get through the entire set and nothing has changed

	private boolean isFollowFinished(String str) {
		for (String s : Follow.get(str)) {
			if (!(str.indexOf("Follow") > -1)) {
				return false;
			}
		}
		return true;
	}
	//the follow helper function 
	private Set<String> getFollow(String v, ArrayList<String> RecursionList) {
		Set<String> FolList = new LinkedHashSet<String>();
		Set<String> iterList = getFollowProd(v);
		// take care of first case
		if (v.trim().equals(STARTSYMBOL)) {
			FolList.add("$");
		}
		if (iterList.size() == 0) {
			RecursionList.clear();
			return FolList;
		}
		
		for (String s : iterList) {
			s.trim();

			String[] temp = s.split("::");
			String[] terms = temp[1].split("\\s+");
			for (int i = 0; i < terms.length; i++) {

				if (terms[i].equals(v)) {

					if (!(i == terms.length - 1)) {
						if (terminals.contains(terms[i + 1].trim())) {

							FolList.add(terms[i + 1].trim());
						} else {
							if (First.get(terms[i + 1].trim()).contains("empty")) {
								FolList.addAll(First.get(terms[i + 1].trim()));
								if (!(i == terms.length - 2)) {
									if (terminals.contains(terms[i + 2].trim())) {
										FolList.add(terms[i + 2].trim());
									} else {
										FolList.addAll(First.get(terms[i + 2].trim()));
									}
								} else {
									if (!RecursionList.contains(temp[0].trim())) {

										RecursionList.add(temp[0].trim());
										FolList.addAll(getFollow(temp[0].trim(), RecursionList));

									}

								}
							} else {
								
								FolList.addAll(First.get(terms[i + 1].trim()));
							}
						}
					} else {

						if (temp[0].trim().equals(v)) {

							break;
						}

						if (!RecursionList.contains(temp[0].trim())) {
							RecursionList.add(temp[0].trim());

							FolList.addAll(getFollow(temp[0].trim(), RecursionList));

						} else {

							break;
						}
					}

				}
			}
		}
		return FolList;
	}
	//returns the Follow list given the variable
	private Set<String> getFollowProd(String v) {
		Set<String> prodList = new LinkedHashSet<String>();
		for (String s : productions) {
			String[] parse = s.split("::");
			for (String iter : parse[1].split("\\s+")) {
				if (iter.equals(v)) {
					prodList.add(s);
				}
			}

		}
		return prodList;
	}
	//go through the list and place the first of each variable
	private void findFirstFromList() {
		for (String s : variables) {
			First.put(s, getFirst(s));

		}

	}

	// to check if variable produces epsilon
	private boolean epsilonCase(String v) {

		for (String item : getProductions(v)) {
			if (item.trim().equals("empty")) {
				return true;
			}
		}
		return false;

	}
	//recursively get the first given the variable value
	private Set<String> getFirst(String v) {
		Set<String> ProdList = new LinkedHashSet<String>(getProductions(v));
		Set<String> FirstSet = new LinkedHashSet<String>();
		// hacking this to erase concurrent modification
		String[] items = ProdList.toArray(new String[FirstSet.size()]);
		for (int i = 0; i < ProdList.size(); i++) {

			String item = items[i];
			String[] itemList = item.split("\\s+");

			if (terminals.contains(itemList[0].trim())) {

				FirstSet.add(itemList[0].trim());

			} else if (variables.contains(itemList[0].trim())) {
				if (epsilonCase(itemList[0].trim())) {
					String temp = new String(item.substring(itemList[0].length(), item.length()));

					if (temp.length() > 0) {
						items[i] = (temp.trim());
						// go back to check on the loop
						i--;
					}
					// make sure to add empty to the first set
					FirstSet.add("empty");
				} else {
					if (!itemList[0].trim().equals(v)) {
						FirstSet.addAll(getFirst(itemList[0].trim()));

					} else {
						FirstSet.remove(item);
					}

				}

			}

		}
		return FirstSet;
	}

	// iterate through the list and find all the productions for
	// the given variable
	public Set<String> getProductions(String var) {
		Set<String> ProdSet = new LinkedHashSet<String>();
		for (String s : productions) {
			String[] prodList = s.split("\\s+");

			if (prodList[0].equals(var)) {

				String[] list = s.split("::");

				ProdSet.add(list[1].trim());
			}
		}

		return ProdSet;
	}
	//Return the private values of all of the class
	public Set<String> getTerminals() {
		return terminals;
	}

	public Set<String> getVariables() {
		return variables;
	}

	public Map<String, Set<String>> getFollow() {
		return Follow;
	}

	public Map<String, Set<String>> getFirst() {
		return First;
	}

	public List<String> getProductionsList() {
		return productions;
	}

}
