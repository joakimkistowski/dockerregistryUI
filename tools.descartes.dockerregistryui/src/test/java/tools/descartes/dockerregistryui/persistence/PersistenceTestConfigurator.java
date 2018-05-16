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

import java.util.HashMap;
import java.util.logging.Logger;

public class PersistenceTestConfigurator {

	private static final Logger LOG = Logger.getLogger(PersistenceTestConfigurator.class.getName());
	
	private static final String JDBC_URL_PROPERTY_NAME = "javax.persistence.jdbc.url";
	private static final String JDBC_URL_PROPERTY_VALUE = "jdbc:hsqldb:mem:test";
	private static final String DDL_PROPERTY = "eclipselink.ddl-generation";
	private static final String DDL_VALUE = "drop-and-create-tables";
	private static final String DDL_OUTPUT_PROPERTY = "eclipselink.ddl-generation.output-mode";
	private static final String DDL_OUTPUT_VALUE = "database";
	
	public static void configureEMF() {
		LOG.info("Using test database with jdbc url: " + JDBC_URL_PROPERTY_VALUE);
		HashMap<String, String> persistenceProperties = new HashMap<String, String>();
		persistenceProperties.put(JDBC_URL_PROPERTY_NAME, JDBC_URL_PROPERTY_VALUE);
		persistenceProperties.put(DDL_PROPERTY, DDL_VALUE);
		persistenceProperties.put(DDL_OUTPUT_PROPERTY, DDL_OUTPUT_VALUE);
		ImageDescriptionRepository.configureEMFWProperties(persistenceProperties);
	}
	
}
