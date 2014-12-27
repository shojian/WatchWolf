/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package watchwolf;

import com.enterprisedt.net.ftp.FileTransferClient;
import java.io.File;

/**
 *
 * @author tsukasa
 */
public class WatchWolf {
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        loadConf();
        // Watch dirs init
        WatchDirs watchDirs = new WatchDirs((new File(args[0])).toPath());
        /* ftp conn start */
        FileTransferClient ftp = new FileTransferClient();
        /* ftp conn end*/
        // Watch starts
        watchDirs.processEvents();
    }

    private static void loadConf() {
        
    }
    
}
