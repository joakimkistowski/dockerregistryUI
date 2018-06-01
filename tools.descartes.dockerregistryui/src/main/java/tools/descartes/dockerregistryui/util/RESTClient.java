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
package tools.descartes.dockerregistryui.util;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Default Client that transfers Entities to/from a service that has a standard conforming REST-API.
 * @author Joakim von Kistowski
 * @param <T> Entity type for the client to handle.
 */
public class RESTClient {
	
	/** The constant logging instance. */
	private static final Logger LOG = Logger.getLogger(RESTClient.class.getName());
	private static final String USER_AGENT = "Mozilla/5.0";
	
	
	
	public static final RESTClient CLIENT = new RESTClient();
	private final HttpClient httpClient;
	private final ObjectMapper jsonMapper = new ObjectMapper();
	
	private String prefix;
	
	private RESTClient() {
		intitializePrefix();
		httpClient = initializeHttpClient();
	}
	
	private HttpClient initializeHttpClient() {
			HttpClient client = new HttpClient(new SslContextFactory(true));
			try {
				client.start();
			} catch (Exception e) {
				LOG.severe("Could not start HTTP client; Exception: " + e.getMessage());
			}
			return client;
	}
	
	/**
	 * Get the response as plaintext. Uri is uri after the global URL prefix
	 * @param uri URI to be appended to the global URL prefix.
	 * @return The response as plaintext.
	 */
	public String getResponseAsText(String uri) {
		Request request = httpClient.newRequest(getPrefix() + uri).header("User-Agent", USER_AGENT);
		try {
			ContentResponse response = request.send();
			if (response.getStatus() < 400) {
				return response.getContentAsString();
			}
		} catch (InterruptedException e) {
			LOG.severe("InterruptedException getting the HTTP response from "
					+ getPrefix() + uri + ": " + e.getMessage());
		} catch (TimeoutException e) {
			LOG.severe("TimeoutException getting the HTTP response from "
					+ getPrefix() + uri + ": " + e.getMessage());
		} catch (ExecutionException e) {
			LOG.severe("ExecutionException getting the HTTP response from "
					+ getPrefix() + uri + ": " + e.getMessage());
		}
		return null;
	}
	
	public List<String> getReponseAsJsonListWithName(String uri, String listName) {
		String plaintext = getResponseAsText(uri);
		if (plaintext != null) {
			try {
				JsonNode node = jsonMapper.readTree(plaintext).get(listName);
				if (node != null && node.isArray()) {
					List<String> stringList = new LinkedList<String>();
					for (final JsonNode element : node) {
						stringList.add(element.asText());
					}
					return stringList;
				}
			} catch (JsonParseException e) {
				LOG.severe("JsonParseException reading JSON response from " + getPrefix() + uri + ": " + e.getMessage());
			} catch (JsonMappingException e) {
				LOG.severe("JsonMappingException reading JSON response from " + getPrefix() + uri + ": " + e.getMessage());
			} catch (IOException e) {
				LOG.severe("IOException reading JSON response from " + getPrefix() + uri + ": " + e.getMessage());
			}
		}
		return null;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	private void intitializePrefix() {
		prefix = RegistryUISettings.SETTINGS.getRegistryProtocol() + "://" + RegistryUISettings.SETTINGS.getRegistryHost();
		prefix = prefix.trim();
		if (!prefix.endsWith("/")) {
			prefix += "/";
		}
		LOG.info("Communicating with registry using URL prefix: " + prefix);
	}
}
