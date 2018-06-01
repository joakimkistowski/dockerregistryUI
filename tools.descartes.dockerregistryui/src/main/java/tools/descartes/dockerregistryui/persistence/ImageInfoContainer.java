/**
 * Copyright 2018 Joakim von Kistowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.descartes.dockerregistryui.persistence;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tools.descartes.dockerregistryui.util.MarkupObserver;

/**
 * Transient Class for holding image information from both the registry and the database.
 * @author Joakim von Kistowski
 *
 */
public class ImageInfoContainer {

	private static final Pattern LINK_PATTERN = Pattern.compile("(^|\\s)(?<link>https??://\\S+)($|\\s)");
	
	private String imageName;
	private List<String> tags;
	private ImageDescription description;
	
	
	public static ImageInfoContainer fromDatabase(String imageName, List<String> tags) {
		return new ImageInfoContainer(imageName, tags,
				ImageDescriptionRepository.REPOSITORY.getImageDescriptionForName(imageName));
	}
	
	private ImageInfoContainer(String imageName, List<String> tags, ImageDescription description) {
		setImageName(imageName);
		setTags(tags);
		setDescription(description);
	}



	public String getImageName() {
		return imageName;
	}


	private void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public List<String> getTags() {
		return tags;
	}
	
	public String getFormattedTags() {
		String tagText = "";
		if (tags != null) {
			for (String tag : tags) {
				tagText += tag + ", ";
			}
			if (tagText.length() > 2) {
				tagText = tagText.substring(0, tagText.length() - 2); 
			}
		}
		return tagText;
	}

	private void setTags(List<String> tags) {
		this.tags = tags;
	}

	public ImageDescription getDescription() {
		return description;
	}
	
	public String getFormattedDescription() {
		if (description == null) {
			return "";
		}
		return MarkupObserver.markupToHTML(description.getDescription());
	}
	
	public String getFormattedExampleCommand() {
		if (description == null) {
			return "";
		}
		return formatLinks(description.getExampleCommand());
	}

	private void setDescription(ImageDescription description) {
		this.description = description;
	}
	
	private static String formatLinks(String original) {
		String formatted = original;
		Matcher m = LINK_PATTERN.matcher(formatted);
		formatted = m.replaceAll("$1<a href=\"$2\">$2</a>$3");
		return formatted;
	}
}
