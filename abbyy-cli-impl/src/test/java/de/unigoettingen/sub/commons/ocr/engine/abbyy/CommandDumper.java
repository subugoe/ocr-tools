package de.unigoettingen.sub.commons.ocr.engine.abbyy;

public class CommandDumper {

	/**
	 * @param args
	 */
	public static void main (String[] args) {
		System.out.println("This dumps just it's arguments. It can be used to simulate shell calls.");
		   StringBuffer command = new StringBuffer();
		    if (args.length > 0) {
		    	command.append(args[0]);
		        for (int i=1; i<args.length; i++) {
		        	command.append(" ");
		        	command.append(args[i]);
		        }
		    } else {
		    	command.append("No arguments given"); 
		    }
		    System.out.println(command.toString());
	}

}
