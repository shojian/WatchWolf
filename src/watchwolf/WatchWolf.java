/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package watchwolf;

import java.io.File;
import java.nio.file.Path;

/**
 *
 * @author tsukasa
 */
public class WatchWolf {
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        WatchDirs watchDirs = new WatchDirs((new File("/Users/tsukasa/Projects/ToDoLister")).toPath());
    }
    
}
