/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

import java.util.Random;

/**
 *
 * @author Owner
 */
public class Testing {
	public static void main(String[] aatg){
		Random rand = new Random();
		
		for(int index=0; index<10; index++){
			System.out.print(rand.nextInt(3));
		}
	}
}
