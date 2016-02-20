/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Timer extends Thread{
	private Typing typing;
	public static final long TIME = 60 * 1000;
	
	
	public Timer(Typing typing){
		this.typing = typing;
	}
	
	
	public static double getTimeInMin(){
		return (Timer.TIME / (60.0 * 1000.0));
	}
	
	
	public void run(){
		try {
			Thread.sleep(TIME);
			
			typing.getResults();
		} catch (InterruptedException ex) {
			Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}