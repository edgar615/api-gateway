package com.github.edgar615.gateway.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class WatcherVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatcherVerticle.class);

    private static final String DEFAULT_ADDR = "api.reload";

    private final Map<WatchKey, Path> keys = new HashMap<>();

    private WatchService watcher;

    private boolean trace = true;

    private boolean dirty;

    @Override
    public void start() throws Exception {
        LOGGER.info("[Verticle] [start] start {}",
                    WatcherVerticle.class.getSimpleName());
        String path = config().getString("path");
        Path watchingPath = Paths.get(path);

        watcher = FileSystems.getDefault().newWatchService();
        registerAll(watchingPath);

        vertx.setPeriodic(200, l -> processEvents());
    }

    @Override
    public void stop() throws Exception {
        watcher.close();
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
        boolean hadEvents = false;

        while (true) {
            WatchKey key = watcher.poll();
            if (key == null) {
                break;
            }
            hadEvents = true;
            dirty = true;
            handleEvent(key);
        }

        if (dirty && !hadEvents) {
            vertx.eventBus()
                    .send(config().getString("reload.address", DEFAULT_ADDR), new JsonObject());
            dirty = false;
        }
    }

    private void handleEvent(WatchKey key) {
        Path dir = keys.get(key);
        if (dir == null) {
            LOGGER.error("[WatcherVerticle] WatchKey not recognized!!");
            return;
        }

        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();

            if (kind == StandardWatchEventKinds.OVERFLOW) {
                continue;
            }

            @SuppressWarnings("unchecked")
            WatchEvent<Path> ev = (WatchEvent<Path>) event;
            Path name = ev.context();

            Path child = dir.resolve(name);

//       print out event
            String eventName = event.kind().name();
//      String filename = child.getFileName().toString();

            if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                //System.out.println("restart " + name);
            }

            // if directory is created, and watching recursively, then
            // register it and its sub-directories
            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                try {
                    if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                        registerAll(child);
                    }
                } catch (IOException x) {
                    // ignore to keep sample readable
                }
            }
        }

        // reset key and remove from set if directory no longer accessible
        boolean valid = key.reset();
        if (!valid) {
            keys.remove(key);

            // all directories are inaccessible
            if (keys.isEmpty()) {
                return;
            }
        }
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                                    StandardWatchEventKinds.ENTRY_MODIFY,
                                    StandardWatchEventKinds.ENTRY_DELETE);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                LOGGER.info("[WatcherVerticle] register: {}", dir);
            } else {
                if (!dir.equals(prev)) {
                    LOGGER.info("[WatcherVerticle] update: {} -> {}", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                                                     BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}