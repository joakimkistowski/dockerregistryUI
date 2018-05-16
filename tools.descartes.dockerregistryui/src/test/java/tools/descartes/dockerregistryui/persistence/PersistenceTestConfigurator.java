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
