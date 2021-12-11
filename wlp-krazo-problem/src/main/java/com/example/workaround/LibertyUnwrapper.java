package com.example.workaround;

import java.lang.reflect.Method;
import java.util.ServiceLoader;

import org.eclipse.krazo.core.HttpCommunicationUnwrapper;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This class, when registered as a {@link HttpCommunicationUnwrapper} service with
 * {@link ServiceLoader}, will work around the problem described in
 * <a href="https://github.com/eclipse-ee4j/krazo/issues/282">Krazo issue #282</a>
 * when running on Open Liberty.
 * 
 * @author Jesse Gallagher
 */
@Priority(1)
public class LibertyUnwrapper implements HttpCommunicationUnwrapper {
  @Override
  public boolean supports(Object obj) {
    if(obj instanceof HttpServletRequest) {
        try {
        Class.forName("com.ibm.wsspi.webcontainer.WebContainerRequestState", false, obj.getClass().getClassLoader()); //$NON-NLS-1$
        return true;
      } catch (ClassNotFoundException e) {
        return false;
      }
    }
    return false;
  }

  @Override
  public HttpServletRequest unwrapRequest(HttpServletRequest obj, Class<HttpServletRequest> type) {
    try {
      Class<?> requestStateClass = Class.forName("com.ibm.wsspi.webcontainer.WebContainerRequestState", false, obj.getClass().getClassLoader()); //$NON-NLS-1$
      Method getInstance = requestStateClass.getDeclaredMethod("getInstance", boolean.class); //$NON-NLS-1$
      Object requestState = getInstance.invoke(null, false);
      Method getCurrentThreadsIExtendedRequest = requestStateClass.getDeclaredMethod("getCurrentThreadsIExtendedRequest"); //$NON-NLS-1$
      return (HttpServletRequest)getCurrentThreadsIExtendedRequest.invoke(requestState);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public HttpServletResponse unwrapResponse(HttpServletResponse obj, Class<HttpServletResponse> type) {
    return obj;
  }

}
