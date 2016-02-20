package learning;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.text.BadLocationException;

public class Typing extends JPanel{// implements ActionListener {
    protected JTextField textField;
    protected JTextArea textArea;
    private final static String newline = 
			System.getProperty("line.separator");
	private KeyListener constructingText = getConstructingText();
	private KeyListener startTyping = getStartTyping();
	private int errors = 0;
	private Timer timer;
	private char[] lettersToSave;
	private static final String SAVE_LETTERS_FILE = 
			/*	"learning" + File.separatorChar +*/ "letters.txt";
	private Typing typing = null;
	
    public Typing() {
        super(new GridBagLayout());

		String savedLetters = this.getSavedLetters();
		lettersToSave = savedLetters.toCharArray();
		
        textField = new JTextField(40);
		textField.setText(savedLetters);
		textField.addKeyListener(constructingText);

        textArea = new JTextArea(10, 40);
        textArea.setEditable(false);
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
		textArea.setText(Typing.getCharsAbundantlyMixed(lettersToSave));
        JScrollPane scrollPane = new JScrollPane(textArea);

        //Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;

        c.fill = GridBagConstraints.HORIZONTAL;
        add(textField, c);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(scrollPane, c);
		
		//timer = new Timer(this);
		typing = this;
    }
	
	
	private static char[] getDualHacker(String letters){
		Pattern p = Pattern.compile("\\p{Alnum}|\\p{Punct}");
		Matcher matcher = p.matcher(""); //not relevent 
		
		//get rid of duplicates
		Set<String> dualHacker = new LinkedHashSet();

		for(int index=0; index<letters.length(); index++){
			String letter = letters.substring(index, index+1);
			matcher.reset(letter);
			
			if(matcher.matches()){
				dualHacker.add(letter);
			}
		}
		
		char[] toReturn = new char[dualHacker.size()];
		Iterator<String> dualHackerIterator = dualHacker.iterator();
		
		for(int index=0; index<toReturn.length; index++){
			toReturn[index] = dualHackerIterator.next().charAt(0);
		}

		return toReturn;
	}
	
	
	private static String getCharsAbundantlyMixed(char[] chars){
		int charsToWrite = 2500;

		if(chars.length == 0){
			return "";
		}
		
		Random random = new Random();
		StringBuffer toReturn = new StringBuffer();

		//make copy so we can mix it up without mixing up methods parameter
		char[] copyOfChars = new char[chars.length];
		
		for(int index=0; index<chars.length; index++){
			copyOfChars[index] = chars[index];
		}
		
		//go through loop untill words filled
		for(int spacesCounter = random.nextInt(5) + 1; 
				charsToWrite > 0; 
				charsToWrite = charsToWrite - copyOfChars.length){
			
			//mix around the letters
			for(int charsIndex=0; charsIndex<copyOfChars.length; charsIndex++){
				char temp = copyOfChars[charsIndex];
				int swapIndex = random.nextInt(copyOfChars.length);
				
				//swap them
				copyOfChars[charsIndex] = copyOfChars[swapIndex];
				copyOfChars[swapIndex] = temp;
			}

			//put spaces in the letters
			for(int charsIndex = 0; 
					charsIndex < copyOfChars.length; 
					charsIndex++, spacesCounter--){

				if(spacesCounter == 0){
					toReturn.append(' ');

					spacesCounter = random.nextInt(5) + 1;
				}
			
				toReturn.append(copyOfChars[charsIndex]);
			}
		}
		
		//text will have a space at the end, but I couldn't give a crap.
		return toReturn.toString();
	}

	
	//readdy set go!
	private KeyListener getStartTyping(){
		return new KeyAdapter(){
			public void keyReleased(KeyEvent e){
				int end = textArea.getSelectionEnd();
				char letter = ' ';
				
				try{
					letter = textArea.getText(end, 1).charAt(0);
					//System.out.println("letter = " + letter);
				} catch(BadLocationException x){
					x.printStackTrace();
				}
				
				if(letter != e.getKeyChar()){
					errors++;
					textField.setText("ERRS:  " + errors);
				}
				
				textArea.setSelectionEnd(++end);
			}
		};
	}
	
	
	//gets the listener for constructing the typing lesson.
	//This listener is for the text field.
	private KeyListener getConstructingText(){
		return new KeyAdapter(){
			public void keyReleased(KeyEvent e){
				if(e.getKeyCode() != KeyEvent.VK_ENTER){
					lettersToSave = getDualHacker(
							textField.getText());
		
		
					textArea.setText(getCharsAbundantlyMixed(lettersToSave));
				} else {
					setSavedLetters();
					
					textField.setEnabled(false);
					textArea.setEnabled(true);
					textArea.setSelectionStart(0);
					textArea.setSelectionEnd(0);

					textField.removeKeyListener(constructingText);
					textArea.addKeyListener(startTyping);
					
					//worked first time without
					textArea.requestFocus();

					timer = new Timer(typing);
					timer.start();
				}
			}
		};
	}

	
	private int getErrors(){
		return errors;
	}
	
	
	private int getLettersTyped(){
		return textArea.getSelectionEnd();
	}
	
	
	public synchronized void getResults(){
		BufferedWriter write;

		//Think equation error if typing test different than 1 minute.
		//Could cause a hickup if time length anything diffent.
		//Here's the equations if I want to change.
		long grossWpm = Math.round(
				getLettersTyped() / 5.0 / Timer.getTimeInMin());
		long netWpm = (Math.round(
				(getLettersTyped() / 5.0 - errors) / Timer.getTimeInMin()));
		
		String stats = "Net WPM:  " + netWpm + newline +
				"Gross WPM:  " + grossWpm + newline +
				"Letters Typed:  " + getLettersTyped() + newline +
				"Errors:  " + getErrors() + newline +
				"Test Length:  " + Timer.getTimeInMin() + 
				" minute(s)" + newline +
				"Letters focused on:  " + this.lettersToSave.length + newline + 
				"(More Precise Net:  " + 
				((int)(Math.round(((getLettersTyped()/5.0 - getErrors())) / 
				Timer.getTimeInMin() * 100)) / 100d) +
				")" + newline +
				"(More Precise Gross:  " + 
				((int)((getLettersTyped()/5.0) / Timer.getTimeInMin() * 100) / 
				100d) + ")";

/*				String.format("%.2f", 
				(getLettersTyped()/5.0 - getErrors()) / Timer.getTimeInMin()) +
				")" + newline +
				"(More Precise Gross:  " + 
				String.format("%.2f", 
				(getLettersTyped()/5.0) / Timer.getTimeInMin()) + ")";
*/
				
		try {
			write = new BufferedWriter(
					new FileWriter(//	"learning" + File.separatorChar + 
					"typing_output.txt", true));

			write.append(getDate());
			write.newLine();
			write.append(stats);
			write.newLine();
			write.append(
					"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			write.newLine();
			write.close();

		} catch (IOException ex) {
			Logger.getLogger(
					Typing.class.getName()).log(Level.SEVERE, null, ex);
		}

		JOptionPane.showMessageDialog(
				this, stats);

		textField.addKeyListener(constructingText);
//		textField.setText(getSavedLetters()); //inefficient accessing disk
		textField.setText(new String(lettersToSave));
		textField.setEnabled(true);

		textArea.removeKeyListener(startTyping);
		textArea.setText(getCharsAbundantlyMixed(lettersToSave));
		textArea.setEnabled(false);
		textArea.setSelectionStart(0);
		textArea.setSelectionEnd(0);


	}

	
	private void setSavedLetters(){
		try {
			Writer write = new FileWriter(SAVE_LETTERS_FILE);

			write.write(lettersToSave);
			write.close();
		} catch (IOException ex) {
			Logger.getLogger(
					Typing.class.getName()).log(Level.SEVERE, null, ex);
		}		
	}

	
	public String getSavedLetters(){
		String toReturn;
		BufferedReader read;

		try {
			read = new BufferedReader(new FileReader(SAVE_LETTERS_FILE));

			toReturn = read.readLine();			
			read.close();
		} catch (IOException ex) {
			toReturn = "";
			System.err.println("No file???");
			Logger.getLogger(
					Typing.class.getName()).log(Level.SEVERE, null, ex);
		}

		if(toReturn == null){
			return "";
		}
		
		return toReturn;
	}

	
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Learning Typing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Add contents to the window.
        frame.add(new Typing());

        //Display the window.
        frame.pack();

		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();      
		int x=(int)((dimension.getWidth() - frame.getWidth())/2);
		int y=(int)((dimension.getHeight() - frame.getHeight())/2);
		frame.setLocation(x, y);

		frame.setVisible(true);

	}

	
	public static String getDate(){
		// get the supported ids for GMT-08:00 (Pacific Standard Time)
		String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
		// if no ids were returned, something is wrong. get out.
		if (ids.length == 0)
			System.exit(0);

		// create a Pacific Standard Time time zone
		SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);

		// set up rules for Daylight Saving Time
		pdt.setStartRule(
				Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
		pdt.setEndRule(
				Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);

		// create a GregorianCalendar with the Pacific Daylight time zone
		// and the current date and time
		Calendar calendar = new GregorianCalendar(pdt);
		Date trialTime = new Date();
		calendar.setTime(trialTime);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd   HH:mm");
		return df.format(calendar.getTime());
	}

	
    public static void main(String[] args) {

		//Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}