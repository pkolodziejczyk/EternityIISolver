package org.k.eternity;
/**
 * 
 */


/**
 * @author EXT-PKO
 *
 */
public enum Orientation {
	TOP("T"),BOT ("B"),RIGHT("R"),LEFT("L");
	
	private final String name;       

    private Orientation(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    }

    public String toString(){
       return name;
    }

}
