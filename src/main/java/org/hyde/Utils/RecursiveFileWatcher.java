/* hyde | Licenced under MIT 2022 | Golay, Marzullo, Matias, Monti & Perrenoud */
package org.hyde.Utils;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecursiveFileWatcher {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private List<WatchEvent<?>> eventsBuffer;
    private WatchKey currentKey;
    private boolean needsRefresh;

    public RecursiveFileWatcher(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
        this.eventsBuffer = null;
        this.needsRefresh = true;
        registerAll(dir);
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
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
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void refreshKeyAndEventBuffer() throws InterruptedException {
        // Has to be a while, because we sometimes get empty event buffer... What a nice API !
        while (eventsBuffer == null || eventsBuffer.isEmpty()) {
            currentKey = watcher.take();
            eventsBuffer = currentKey.pollEvents();
        }
    }

    public FileEvent fetchEvent() throws InterruptedException, IOException {
        if (needsRefresh) refreshKeyAndEventBuffer();

        Path dir = keys.get(currentKey);
        WatchEvent<?> event = eventsBuffer.get(0);
        eventsBuffer.remove(0);
        WatchEvent.Kind kind = event.kind();

        // Context for directory entry event is the file name of entry
        WatchEvent<Path> ev = (WatchEvent<Path>) event;
        Path name = ev.context();
        Path child = dir.resolve(name);

        // Register new directories
        if (kind == ENTRY_CREATE) registerAll(child);

        // Cleanup for next call
        if (eventsBuffer.isEmpty()) {
            if (!currentKey.reset()) keys.remove(currentKey);

            needsRefresh = true;
        }

        return new FileEvent(child, kind);
    }

    public static class FileEvent {
        public final Path path;
        public final WatchEvent.Kind<?> kind;

        FileEvent(Path path, WatchEvent.Kind<?> kind) {
            this.path = path;
            this.kind = kind;
        }
    }
}
