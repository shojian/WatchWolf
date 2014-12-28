package watchwolf;

import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author tsukasa
 */
public class XMLHandler extends DefaultHandler {
    // Constants start    
    private final static String FTP = "ftp";
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";
    private final static String PORT = "port";
    private final static String SERVER_PATH = "server-path";
    private final static String DIR = "dir";
    private final static String URI = "uri";
    private final static String IGNORE = "ignore";
    private final static String FILE = "file";
    //Constants end
    private String hostname;
    private String username;
    private String password;
    private int port;
    private String uri;
    private final ArrayList<String> ignored = new ArrayList<>();
    private boolean hasValue = false;
    private StringBuffer value = new StringBuffer(100);
    private String serverPath;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName.toLowerCase()) {
            case USERNAME:
            case PASSWORD:
            case PORT:
            case URI:
            case FILE:
            case SERVER_PATH:
                hasValue = true;
                this.value = new StringBuffer(100);
                break;
            default:
                hasValue = false;
        }
    }    

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (hasValue) {
            value.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName.toLowerCase()) {
            case USERNAME:
                this.username = this.value.toString();
                break;
            case PASSWORD:
                this.password = this.value.toString();
                break;
            case PORT:
                this.port =Integer.parseInt(this.value.toString());
                break;
            case URI:
                this.uri = this.value.toString();
                break;
            case FILE:
                this.ignored.add(this.value.toString());
                break;
            case SERVER_PATH:
                this.serverPath = this.value.toString();
                break;
            default:
        }
    }
    
    
    

    public String getHostname() {
        return hostname;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getURI() {
        return URI;
    }

    public ArrayList<String> getIgnored() {
        return ignored;
    }

    public String getServerPath() {
        return serverPath;
    }      
            
}
