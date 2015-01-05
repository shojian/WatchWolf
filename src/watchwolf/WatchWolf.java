package watchwolf;

import com.enterprisedt.net.ftp.FileTransferClient;
import java.io.File;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.XMLReader;

/**
 *
 * @author tsukasa
 */
public class WatchWolf {

    private final static String confFile = "wwconf.xml";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            XMLHandler handlesStuff = new XMLHandler();
            spf.setValidating(false);
            SAXParser saxLevel1 = spf.newSAXParser();
            XMLReader parser = saxLevel1.getXMLReader();
            parser.setContentHandler(handlesStuff);
            parser.parse(confFile);
            // Watch dirs init
            WatchDirs watchDirs = new WatchDirs(handlesStuff.getLocalPath(), handlesStuff.getIgnored());
            /* ftp conn start */
            FileTransferClient ftp = new FileTransferClient();            
            ftp.setRemoteHost(handlesStuff.getHostname());
            ftp.setUserName(handlesStuff.getUsername());
            ftp.setPassword(handlesStuff.getPassword());
            ftp.setRemotePort(handlesStuff.getPort());
            ftp.connect();
            /* ftp conn end*/
            // Watch starts
            watchDirs.setPathToDir(handlesStuff.getRemotePath());
            watchDirs.processEvents(ftp);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
