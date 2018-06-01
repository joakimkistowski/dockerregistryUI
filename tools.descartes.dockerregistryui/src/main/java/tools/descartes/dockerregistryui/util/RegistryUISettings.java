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

import java.util.logging.Logger;

public class RegistryUISettings {

	private static final Logger LOG = Logger.getLogger(RegistryUISettings.class.getName());
	
	/**
	 * Fallback constants:
	 */
	private static final String FALLBACK_REGISTRY_HOST = "docker-registry";
	private static final String FALLBACK_REGISTRY_PROTOCOL = "https";
	private static final String FALLBACK_CONTEXT = "/ui";
	
	/**
	 * Public constants.
	 */
	public static final String HELLO_FILE = "hello.md";
	
	// This path is identical to the path specified in the persistence.xml 
	public static final String VOLUME_PATH = determineVolumePath();
	
	public static final RegistryUISettings SETTINGS = new RegistryUISettings();
	
	private String registryHost = FALLBACK_REGISTRY_HOST;
	private String registryProtocol = FALLBACK_REGISTRY_PROTOCOL;
	
	
	public String getRegistryProtocol() {
		return registryProtocol;
	}
	public void setRegistryProtocol(String registryProtocol) {
		LOG.info("Registry Protocol set to: " + registryProtocol);
		this.registryProtocol = registryProtocol;
	}
	public String getRegistryHost() {
		return registryHost;
	}
	public void setRegistryHost(String registryHost) {
		LOG.info("Registry Host set to: " + registryHost);
		this.registryHost = registryHost;
	}
	public String getContext() {
		return FALLBACK_CONTEXT;
	}
	
	private static String determineVolumePath() {
		String path;
		if (System.getProperty("os.name").startsWith("Windows")) {
			path = System.getProperty("java.io.tmpdir");
		} else {
			path = "/var/lib/dockerregistryui/";
		}
		LOG.info("Using volume path to write DB and look for md files: " + path);
		return path;
	}
	
}
