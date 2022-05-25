package org.hyde.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static java.nio.file.StandardWatchEventKinds.*;

public class RecursiveFileWatcher {
	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;
	private List<WatchEvent<?>> eventsBuffer;
	private WatchKey currentKey;

	public RecursiveFileWatcher(Path dir) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();
		this.eventsBuffer = null;
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
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
					throws IOException
			{
			register(dir);
			return FileVisitResult.CONTINUE;
			}
		});
	}

	public FileEvent fetchEvent() {
		// Has to be a while, be cause we sometimes get empty event buffer... Thanks Java !
		while (eventsBuffer == null || eventsBuffer.isEmpty()) {
			if (currentKey != null)
				currentKey.reset();

			try {
				currentKey = watcher.take();
			} catch (InterruptedException e) {
				return null;
			}
			eventsBuffer = currentKey.pollEvents();
		}
		Path dir = keys.get(currentKey);
		WatchEvent<?> event = eventsBuffer.get(0);
		eventsBuffer.remove(0);
		WatchEvent.Kind kind = event.kind();

		// Context for directory entry event is the file name of entry
		WatchEvent<Path> ev = (WatchEvent<Path>) event;
		Path name = ev.context();
		Path child = dir.resolve(name);

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
