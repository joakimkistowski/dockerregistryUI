package tools.descartes.dockerregistryui.util;

public class RegistryUISettings {

	/**
	 * Fallback constants:
	 */
	private static final String FALLBACK_REGISTRY_HOST = "docker-registry";
	private static final String FALLBACK_REGISTRY_PROTOCOL = "https";
	private static final String FALLBACK_CONTEXT = "/ui";
	
	public static final RegistryUISettings SETTINGS = new RegistryUISettings();
	
	private String registryHost = FALLBACK_REGISTRY_HOST;
	private String registryProtocol = FALLBACK_REGISTRY_PROTOCOL;
	
	
	public String getRegistryProtocol() {
		return registryProtocol;
	}
	public void setRegistryProtocol(String registryProtocol) {
		this.registryProtocol = registryProtocol;
	}
	public String getRegistryHost() {
		return registryHost;
	}
	public void setRegistryHost(String registryHost) {
		this.registryHost = registryHost;
	}
	public String getContext() {
		return FALLBACK_CONTEXT;
	}
	
}
