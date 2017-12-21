/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matchmaker;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Vitor Castro
 *
 * Simple demo that uses java.util.Timer to schedule a task to execute once 5
 * seconds have passed.
 */
public class SelectionTimer {

    Timer timer;

    public SelectionTimer() {
        timer = new Timer();
        timer.schedule(new SelectionTask(), 4 * 1000);
    }

    class SelectionTask extends TimerTask {

        @Override
        public void run() {
            System.out.println("Time's up!");
            timer.cancel(); //Terminate the timer thread
        }
    }

}
