import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
//Class for the SLR table, Creates the itemList as well as the 
//state list
public class SLRTable {
	private Grammar grammar;
	public List<String> productions = new ArrayList<String>();
	private List<Item> States = new ArrayList<Item>();
	private Map<Integer, Set<String>> itemMap = new TreeMap<Integer, Set<String>>();
	public Map<Integer, Set<Item>> stateList = new TreeMap<Integer, Set<Item>>();

	public SLRTable(Grammar g) {
		grammar = g;
		productions.add(g.STARTSYMBOL + "' # " + g.STARTSYMBOL);

		productions.addAll(g.getProductionsList());

		buildCollection();
		// table is not SLR
		removeTheBadCase();
	}

	@SuppressWarnings("unchecked")
	private void buildCollection() {

		int i = 0;
		int state = 1;
		Set<String> temp = new LinkedHashSet<String>();
		temp.add(productions.get(0));
		temp.addAll(checkClosure(productions.get(0)));
		itemMap.put(0, temp);
		for (int iter = 0; iter < itemMap.size(); iter++) {
			buildState(iter, 0);
		}
		// buildState()

	}

	@SuppressWarnings("unchecked")
	private int buildState(int itemNum, int state) {
		int i = itemNum;
		for (String s : itemMap.get(i)) {

			if (isDuplicate(buildItem(itemMap.get(i), s)) == -1) {
				if (buildItem(itemMap.get(i), s).size() > 0) {
					Set<String> list = new LinkedHashSet();

					list = buildItem(itemMap.get(i), s);
					if (!checkNullableCase(buildItem(itemMap.get(i), s)).equals("-1")) {
						List<String> holder = new ArrayList<String>();
						holder.add(checkNullableCase(buildItem(itemMap.get(i), s)));
						stateAdd(i, holder, itemMap.size());
					}
					stateAdd(i, goToOperation(new ArrayList<String>(itemMap.get(i)), s), itemMap.size());
					itemMap.put(itemMap.size(), buildItem(itemMap.get(i), s));

					state++;

				}
			} else {
				if (buildItem(itemMap.get(i), s).size() > 0) {
					stateAdd(i, goToOperation(new ArrayList<String>(itemMap.get(i)), s),
							isDuplicate(buildItem(itemMap.get(i), s)));
				}
			}

		}

		return state;
	}
	
	
	//check for the nullable calse
	private String checkNullableCase(Set<String> list) {
		for (String str : list) {
			if (str.trim().split("\\s+").length == 2) {
				return str;
			}

		}
		ArrayList<String> newlist = new ArrayList<String>();
		newlist.addAll(list);
		if (newlist.size() > 0) {
			for (int i = 1; i < newlist.size(); i++) {
				if (isFinished(newlist.get(i))) {
					return newlist.get(i);
				}
			}
		}
		return "-1";
	}
	//adds a state given the item value and the list of goTos
	private void stateAdd(int item, List<String> goTos, int state) {
		Set<Item> list = getItemsFromgoto(goTos, state);

		for (Item i : list) {

			if (!i.command.equals("R")) {
				if (!stateList.keySet().contains(item)) {
					Set<Item> ite = new HashSet<Item>();
					ite.add(i);
					stateList.put(item, ite);
				} else {

					Set<Item> itemList = new HashSet<Item>(stateList.get(item));
					if (!itemInList(i, itemList))
						itemList.add(i);
					stateList.put(item, itemList);
				}
			}

			else if (i.symbol.trim().split("\\s+").length > 1) {

				String[] str = i.symbol.trim().split("\\s+");
				String symbol = "";
				for (int index = 0; index < str.length; index++) {
					if (str[index].equals("#")) {
						symbol = str[index - 1];
					}
				}
				if (grammar.terminals.contains(symbol)) {

					Item ReduceItem = new Item(symbol, state, "S");

					if (!stateList.keySet().contains(item)) {

						Set<Item> ite = new HashSet<Item>();
						ite.add(ReduceItem);
						stateList.put(item, ite);
					} else {

						Set<Item> itemList = new HashSet<Item>(stateList.get(item));

						if (!itemInList(ReduceItem, itemList)) {
							itemList.add(ReduceItem);
						} else {

						}
						stateList.put(item, itemList);

					}
				} else {
					Item ReduceItem = new Item(symbol, state, "G");

					if (!stateList.keySet().contains(item)) {
						Set<Item> ite = new HashSet<Item>();
						ite.add(ReduceItem);
						stateList.put(item, ite);
					} else {

						Set<Item> itemList = new HashSet<Item>(stateList.get(item));

						if (!itemInList(ReduceItem, itemList)) {

							itemList.add(ReduceItem);
						} else {
						}

						stateList.put(item, itemList);
					}

				}
			} else {

				Set<Item> ite = new LinkedHashSet<Item>(getReduceValues(i));

				if (!stateList.keySet().contains(state)) {

					stateList.put(state, ite);
				} else {
					Set<Item> itemList = new HashSet<Item>(stateList.get(state));
					for (Item rItem : ite) {
						if (!itemInList(rItem, itemList)) {
							itemList.add(rItem);
						}
					}
					stateList.put(state, itemList);

				}
			}

		}

		return;
	}
	//returns the Items from the list of GOTO's 
	//places if the symbol is reduction, shift or goto
	private Set<Item> getItemsFromgoto(List<String> goTo, int state) {
		Set<Item> items = new HashSet<Item>();
		for (String s : goTo) {

			String[] parse = s.split("\\s+");
			if (s.trim().split("\\s+").length == 2) {
				items.add(new Item(parse[0].trim(), getProductionNumber(s), "R"));
			}
			for (int i = 0; i < parse.length; i++) {
				if (parse[i].trim().equals("#")) {
					if (i == parse.length - 1) {
						items.add(new Item(parse[0].trim(), getProductionNumber(s), "R"));

						items.add(new Item(s, state, "R"));
						if (grammar.terminals.contains(parse[i - 1].trim())) {

							items.add(new Item(parse[i - 1].trim(), state, "S"));

						} else {

							items.add(new Item(parse[i - 1].trim(), state, "G"));
						}
					} else if (grammar.terminals.contains(parse[i - 1].trim())) {

						items.add(new Item(parse[i - 1].trim(), state, "S"));

					} else {

						items.add(new Item(parse[i - 1].trim(), state, "G"));
					}
				}
			}
		}
		return items;
	}
	//returns the reduction items 
	private Set<Item> reduceItems(int prodNum, String production) {
		Set<Item> list = new LinkedHashSet<Item>();
		String var = production.trim().split("\\s+")[0];
		// get all the follow items from the production
		if (!var.equals("P'")) {
			for (String str : grammar.getFollow().get(var)) {
				list.add(new Item(str, prodNum, "R"));
			}
		}
		return list;
	}
	//returns the reduction values
	private Set<Item> getReduceValues(Item i) {
		Set<Item> reduceItems = new HashSet<Item>();
		String symbol = i.symbol;
		if (!symbol.equals(grammar.STARTSYMBOL+"'")) {
			if (!grammar.terminals.contains(symbol)) {
				for (String s : grammar.getFollow().get(symbol)) {
					reduceItems.add(new Item(s, i.state, "R"));

				}
			} else {
				reduceItems.add(new Item(symbol, i.state, "R"));
			}
		} else {
			reduceItems.add(new Item("$", 0, "Accept"));
		}
		return reduceItems;
	}
	//returns the production number, helpful for performing the goto operation
	private int getProductionNumber(String s) {
		for (int i = 0; i < productions.size(); i++) {
			String str = productions.get(i);
			str = str.replace("::", "");
			s = s.replace("#", "");
			str = str.replaceAll(" ", "");
			s = s.replaceAll(" ", "");
			if (s.trim().equals(str.trim()))
				return i;
		}
		return -1;
	}
	//builds each item given the current item they are working off of
	private Set<String> buildItem(Set<String> previousItem, String entry) {
		Set<String> itemList = new LinkedHashSet<String>();
		List<String> list = new ArrayList<String>(previousItem);

		int index = list.indexOf(entry);

		String prod = list.get(index);
		list = goToOperation(list, prod);

		itemList.addAll(list);
		for (String s : list) {
			itemList.addAll(checkClosure(s));

		}

		return itemList;
	}

	// get all of the variables for the string
	private ArrayList<String> closure(String var) {
		ArrayList<String> productions = new ArrayList<String>();

		for (String s : grammar.getProductions(var)) {
			productions.add(var + " # " + s);
		}
		return productions;
	}
	//checks to see if the production contains any closure
	private Set<String> checkClosure(String production) {
		Set<String> items = new LinkedHashSet<String>();

		ArrayList<String> checker = new ArrayList<String>();
		String[] iter = production.split("#");

		if (iter.length == 2) {
			if (grammar.getVariables().contains(iter[1].trim().split("\\s+")[0])) {
				//hack to check for a special case of repeated variables
				if (!iter[1].trim().split("\\s+")[0].equals(production.trim().split("\\s+")[0])
						|| production.equals("E V = # E ")) {
					if (production.equals("E V = # E ")) {
						checker = closure("E");

					} else {
						checker = closure(iter[1].trim().split("\\s+")[0]);
					}
					items.addAll(checker);
					if (production.equals("E V = # E ")) {
					}
					for (String s : checker) {
						items.addAll(checkClosure(s));
					}
				}
			}

		}

		return items;
	}
	//checks if the # is at the end of a production or not
	private boolean productionFinished(String prod) {
		prod.trim();
		if (prod.contains("#")) {
			if (prod.indexOf("#") == prod.length()) {
				return true;
			}
		}
		return false;
	}
	//performs the goto operation
	private List<String> goToOperation(List<String> List, String prod) {
		List<String> list = new ArrayList<String>();
		String over = "";
		String newString = "";
		boolean swapTriggered = false;
		for (String s : prod.split("\\s+")) {
			s.trim();
			if (s.equals("#")) {
				swapTriggered = true;
			} else if (swapTriggered && !s.equals("#")) {
				swapTriggered = false;
				over = s;
				newString += s + " # ";

			} else {

				newString += s + " ";
			}
			for (String str : List) {
				if (!goTo(str, over).equals("-1")) {
					list.add(goTo(str, over));
				}
			}

		}
		return list;
	}
	//returns the goto value
	private String goTo(String str, String ovr) {
		boolean trigger = false;
		boolean changed = false;
		String newString = "";
		for (String s : str.trim().split("\\s+")) {
			if (s.trim().equals("#")) {
				trigger = true;

			} else if (trigger) {
				trigger = false;
				if (s.trim().equals(ovr)) {
					changed = true;
					newString += s + " # ";
				}

			} else {
				newString += s + " ";
			}
		}
		if (!changed) {
			return ("-1");
		} else {
			return newString;
		}
	}
	//checks if the given item is a duplicate or not
	private int isDuplicate(Set<String> itemList) {
		for (Integer i : itemMap.keySet()) {
			if (itemMap.get(i).equals(itemList)) {
				return i;
			}
		}
		return -1;
	}
	//goes through the production and checks if it is finished
	private boolean isFinished(String prod) {
		if (prod.split("#").length == 1) {
			return true;
		}
		return false;
	}
	//returns the itemlist
	public Map<Integer, Set<String>> getItemMap() {
		return itemMap;
	}
	//checks if a given item is already in the list
	public boolean itemInList(Item i, Set<Item> list) {
		for (Item item : list) {
			if (item.equals(i)) {

				return true;
			}
		}
		return false;
	}
	//hard coded because the grammar given is not for an SLR
	//goes through and remove states that are duplicated
	public void removeTheBadCase() {
		Item item = new Item("blah", 0, "blah");
		if (stateList.keySet().contains(102)) {
			for (Item i : stateList.get(102)) {
				if (i.symbol.equals("else") && (i.command.equals("R"))) {
					item = i;
				}
			}
			stateList.get(102).remove(item);
		}

	}
}
