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
package tools.descartes.dockerregistryui;


import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

import tools.descartes.dockerregistryui.util.RegistryUISettings;
import tools.descartes.dockerregistryui.util.WikiParser;

public class Launcher {

	public static void main(String[] args) {
		if (args.length > 0) {
			RegistryUISettings.SETTINGS.setRegistryHost(args[0].trim());
		}
		if (args.length > 1) {
			RegistryUISettings.SETTINGS.setRegistryProtocol(args[1].trim());
		}
		new Launcher().launchJetty();
	}
	
	private void launchJetty() {
		//Workaround to prevend asm failures
		org.objectweb.asm.Opcodes.class.getName();
		
		//Actual Server start
		Server server = new Server( 8080 );
		
		//The actual webapp
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath(RegistryUISettings.SETTINGS.getContext());
		String webDir = Launcher.class.getClassLoader().getResource("tools/descartes/dockerregistryui/serve").toExternalForm();
		webapp.setResourceBase(webDir);
		
		Configuration.ClassList classlist = Configuration.ClassList
                .setServerDefault(server);
        classlist.addBefore(
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration" );
        webapp.setAttribute(
                "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$|.*/ui.jar$|.*/dockerregistryui.jar$" );
        
		webapp.addServlet(AddCategoryToImageServlet.class, "/addcategorytoimage");
		webapp.addServlet(CreateCategoryServlet.class, "/createcategory");
		webapp.addServlet(ImageDescriptionServlet.class, "/imagedescription");
		webapp.addServlet(ManagerUIServlet.class, "");
		webapp.addServlet(RemoveCategoryFromImageServlet.class, "/removecategoryfromimage");
		webapp.addServlet(RemoveCategoryServlet.class, "/removecategory");
		//Reads the wikifile on startup and renders the greeting
		webapp.getServletContext().setAttribute("greeting", WikiParser.wikiMarkupToHTML(new File(RegistryUISettings.VOLUME_PATH + RegistryUISettings.GREETING_FILE)));
		//Redirection servlet, redirects root calls to the webapp.
		ServletHandler rootRedirectHandler = new ServletHandler();
		rootRedirectHandler.addServletWithMapping(RootRedirectServlet.class, "");
		HandlerList handlers = new HandlerList(webapp, rootRedirectHandler);
        server.setHandler(handlers);
        try {
			server.start();
			//server.dumpStdErr();
	        server.join();
		} catch (Exception e) {
			System.out.println("Exception starting Jetty: \\" + e.getMessage());
			e.printStackTrace();
		}
        
	}
	
}
