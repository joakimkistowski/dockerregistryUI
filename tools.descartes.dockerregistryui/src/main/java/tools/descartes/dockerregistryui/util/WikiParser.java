package tools.descartes.dockerregistryui.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.output.HtmlRenderer;
import org.sweble.wikitext.engine.output.HtmlRendererCallback;
import org.sweble.wikitext.engine.output.MediaInfo;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.nodes.WtUrl;
import org.sweble.wikitext.parser.parser.LinkTargetException;

/**
 * Parses Mediawiki-markup text and returns the result as HTML.
 * @author Joakim von Kistowski
 *
 */
public class WikiParser {

	private static final WikiConfig SWEBLE_CONFIG = DefaultConfigEnWp.generate();
	private static final WtEngineImpl SWEBLE_ENGINE = new WtEngineImpl(SWEBLE_CONFIG);
	
	public static String wikiMarkupToHTML(File markupFile) {
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
		return wikiMarkupToHTML(lineBuffer.toString());
	}
	
	public static String wikiMarkupToHTML(String markupText) {
		PageTitle title;
		try {
			title = PageTitle.make(SWEBLE_CONFIG, "testtitle");
			PageId pageId = new PageId(title, -1);
			EngProcessedPage cp = SWEBLE_ENGINE.postprocess(pageId, markupText, null);
			return HtmlRenderer.print(new RegistryUIRendererCallback(), SWEBLE_CONFIG, title, cp.getPage());
		} catch (LinkTargetException e) {
			System.out.println("Exception creating/finding link target in wiki text: " + e.getMessage());
			System.out.println("\t at substring: " + e.getOffendingSubstring());
			System.out.println("\t Reason: " + e.getReason());
		} catch (EngineException e) {
			System.out.println("EngineException parsing wiki text: " + e.getMessage());
		}
		return "";
	}

	private static final class RegistryUIRendererCallback implements HtmlRendererCallback {

		@Override
		public MediaInfo getMediaInfo(String arg0, int arg1, int arg2) {
			// should not be needed
			return null;
		}

		@Override
		public String makeUrl(PageTitle arg0) {
			// should not be needed
			return null;
		}

		@Override
		public String makeUrl(WtUrl urlTarget) {
			if (urlTarget.getProtocol() == "") {
				return urlTarget.getPath();
			}
			return urlTarget.getProtocol() + ":" + urlTarget.getPath();
		}

		@Override
		public String makeUrlMissingTarget(String arg0) {
			// no internal linking
			return "";
		}

		@Override
		public boolean resourceExists(PageTitle arg0) {
			// no internal linking
			return false;
		}
		
	}
}
