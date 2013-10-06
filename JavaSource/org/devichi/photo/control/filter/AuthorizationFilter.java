package org.devichi.photo.control.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.devichi.photo.control.servlet.NavigationServlet;

/**
 * A filter to protect the static resources of the web-application.
 * Note: dynamic resources are protected by authorization code in the controllers.
 * 
 * It should be called after the main filter, since the latter is in
 * charge of auto login (which may be needed for authorization checks).
 * 
 * @author Hadrien Devichi
 */
public class AuthorizationFilter implements Filter {
	
	static final Logger log = Logger.getLogger(AuthorizationFilter.class);
	
	private static final String FAVICON_URI = "favicon.ico"; //$NON-NLS-1$
	
	public void init(FilterConfig arg0) throws ServletException {
		// nothing to do for this filter
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
				
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		
		String requestedURI = httpRequest.getRequestURI();
		
		// favicon.ico is always accessible
		if (requestedURI.toLowerCase().endsWith(FAVICON_URI)) {
			chain.doFilter(request, response);
			return;
		}
		
		// ensure direct access to JSP isn't allowed
		if (requestedURI.endsWith(".jsp")) {
			httpResponse.sendRedirect(httpRequest.getContextPath()+NavigationServlet.URI);
			return;
		}
		chain.doFilter(request, response);
	}

	public void destroy() {
		// nothing to do for this filter
	}
}
