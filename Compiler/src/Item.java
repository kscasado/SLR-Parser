import java.util.ArrayList;

//Class for the Item object
public class Item {
	
	//Assign the private variables
	public Item(String Symbol,int State,String Command){
		//TODO: Error Check
		
		symbol = Symbol;
		state=State;
		command = Command;
		
		
	}
	public String toString(){
		String s = "Symbol: "+ symbol+"\n"+"Command: "+command+"\n";
		s+="Number: "+state+"\n";
		return s;
		
	}
	public boolean equals(Object o){
		if (o == this) {
            return true;
        }
 
        if (!(o instanceof Item)) {
            return false;
        }
         
        Item c = (Item) o;
        //if(symbol.trim().equals(c.symbol.trim())){
        if(command.equals(c.command)&&symbol.equals(c.symbol)&&state==c.state){
        	return true;
        }
        return false;
	}
	public String command;
	public String symbol;
	public int state;
	

}
