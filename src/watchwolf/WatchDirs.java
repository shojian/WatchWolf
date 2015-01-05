package watchwolf;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FileTransferClient;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tsukasa
 */
public class WatchDirs {

    private WatchService watcher;
    private Map<WatchKey, Path> keys;
    private boolean trace = false;
    private ArrayList<String> ignore;
    

    /**
     * Creates a WatchService and registers the given directory
     * @param uri
     * @param ignore
     */
    public WatchDirs(String uri, ArrayList<String> ignore) {
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
            this.keys = new HashMap<>();
            this.ignore = ignore;
            System.out.format("Scanning %s ...\n", (new File(uri)).toPath());
            registerDirs((new File(uri)).toPath());
            System.out.println("Done.");

            // enable trace after initial registration
            this.trace = true;
        } catch (IOException ex) {
            System.out.println(ex);
            Logger.getLogger(WatchDirs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerDirs(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                if (trace) {                    
                    Path prev = keys.get(key);
                    if (prev == null) {
                        System.out.format("register: %s\n", dir);
                    } else {
                        if (!dir.equals(prev)) {
                            System.out.format("update: %s -> %s\n", prev, dir);
                        }
                    }
                }
                if (!ignore.contains(key)) {
                    keys.put(key, dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * Process all events for keys queued to the watcher
     * @param ftp
     * @param pathToDir
     */
    public void processEvents(FileTransferClient ftp, String pathToDir) {
        for (;;) {
 
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }
 
            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }
 
            key.pollEvents().stream().forEach((event) -> {
                WatchEvent.Kind kind = event.kind();
                if (!(kind == OVERFLOW)) {
                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path child = dir.resolve(name);
                    System.out.format("%s: %s\n", event.kind().name(), child);
                    try {
                        // Magic happens here
                        ftp.uploadFile(child.toString(), pathToDir);
                    } catch (FTPException | IOException ex) {
                        Logger.getLogger(WatchDirs.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // Magic starts here
                    if (kind == ENTRY_CREATE) {
                        try {
                            if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                registerDirs(child);
                            }
                        } catch (IOException x) {
                            // ignore to keep sample readbale
                        }
                    }
                }
            });
 
            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
 
                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

}
