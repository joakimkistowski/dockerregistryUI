package tools.descartes.dockerregistryui.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Parses Common Markup (MD-Markup) text and returns the result as HTML.
 * @author Joakim von Kistowski
 *
 */
public class MarkupHandler {

	private static final Parser markupParser = Parser.builder().build();
	private static final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();
	
	private WatchService volumeWatcher = null;
	
	/**
	 * Creates a new MarkupHandler for a file in a directory. The handler watches the file and calls a consumer
	 * each time the file changes. The called consumer gets the filename and the file contents already converted
	 * and rendered to HTML.
	 * @param updatedConvertedMarkupConsumer The consumer that gets called when the file is updated.
	 * 		It gets the file name and the file contents, already parsed and converted to rendered HTML.
	 * @param directory The directory to watch for files.
	 * @param fileNames The names of the files to watch.
	 */
	public MarkupHandler(BiConsumer<String,String> updatedMarkupConsumer,
			String directory, String... fileNames) {
		Path volumeDir = Paths.get(RegistryUISettings.VOLUME_PATH);
		try {
			volumeWatcher = volumeDir.getFileSystem().newWatchService();
			volumeDir.register(volumeWatcher, StandardWatchEventKinds.ENTRY_MODIFY);
			new BackgroundVolumeWatcher(volumeWatcher, updatedMarkupConsumer,
					directory, Arrays.asList(fileNames)).start();
		} catch (IOException e) {
			System.out.println("Cannot initialize watcher service for volume dir  at: " + volumeDir.toString());
			System.out.println(e.getMessage());
		}
	}
	
	private static String markupToHTML(File markupFile) {
		StringBuffer lineBuffer = new StringBuffer();
		try (BufferedReader br = new BufferedReader(new FileReader(markupFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				lineBuffer.append(line);
			}
		} catch (IOException e) {
			System.out.println("WARN: IOException reading file at " + markupFile.getAbsolutePath());
			System.out.println("\tThis is expected behavior if no markup file was provided.");
			return "";
		}
		return markupToHTML(lineBuffer.toString());
	}
	
	private static String markupToHTML(String markupText) {
		Node markupNode = markupParser.parse(markupText);
		return htmlRenderer.render(markupNode);
	}
	
	private static class BackgroundVolumeWatcher extends Thread {
		
		private WatchService volumeWatcher = null;
		private BiConsumer<String,String> updatedMarkupConsumer;
		private Collection<String> fileNames;
		
		private BackgroundVolumeWatcher(WatchService volumeWatcher,
				BiConsumer<String,String> updatedMarkupConsumer,
				String directory,
				Collection<String> fileNames) {
			this.volumeWatcher = volumeWatcher;
			this.updatedMarkupConsumer = updatedMarkupConsumer;
			this.fileNames = fileNames;
			
			//initial file handling at startup
			Path dirPath = new File(directory).toPath().toAbsolutePath();
			for (String fileName: fileNames) {
				updatedMarkupConsumer.accept(fileName,
						markupToHTML(dirPath.resolve(fileName).toAbsolutePath().toFile()));
			}
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					WatchKey key = volumeWatcher.take();
					for (WatchEvent<?> event : key.pollEvents()) {
						Path filePath = (Path)event.context();
						String fileName = filePath.getFileName().toString();
						if (fileNames.contains(fileName)) {
							System.out.println("Detected Change of File \"" + fileName + "\". Reprocessing.");
							updatedMarkupConsumer.accept(fileName, markupToHTML(filePath.toAbsolutePath().toFile()));
						}
					}
					key.reset();
				} catch (InterruptedException e) {
					System.out.println("Interrupted waiting for watcher service");
					System.out.println(e.getMessage());
				}
			}
		}
	}
}
