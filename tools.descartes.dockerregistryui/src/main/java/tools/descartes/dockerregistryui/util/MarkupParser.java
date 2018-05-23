package tools.descartes.dockerregistryui.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Parses Common Markup (MD-Markup) text and returns the result as HTML.
 * @author Joakim von Kistowski
 *
 */
public class MarkupParser {

	private static final Parser MARKUP_PARSER = Parser.builder().build();
	private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();
	
	public static String markupToHTML(File markupFile) {
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
	
	public static String markupToHTML(String markupText) {
		Node markupNode = MARKUP_PARSER.parse(markupText);
		return HTML_RENDERER.render(markupNode);
	}
}
