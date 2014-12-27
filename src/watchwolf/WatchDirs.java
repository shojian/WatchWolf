/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package watchwolf;

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
    

    /**
     * Creates a WatchService and registers the given directory
     * @param pathToDir
     */
    public WatchDirs(Path pathToDir) {
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
            this.keys = new HashMap<>();

            System.out.format("Scanning %s ...\n", pathToDir);
            registerDirs(pathToDir);
            System.out.println("Done.");

            // enable trace after initial registration
            this.trace = true;
        } catch (IOException ex) {
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
                keys.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * Process all events for keys queued to the watcher
     */
    public void processEvents() {
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
