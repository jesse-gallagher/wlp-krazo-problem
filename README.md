# wlp-krazo-problem

This project demonstrates the problem described in [this blog post](https://frostillic.us/blog/posts/2021/11/30/journeys-debugging-open-liberty-and-mvc) and [this issue](https://github.com/eclipse-ee4j/krazo/issues/282): namely, that Krazo's Servlet-based view engine fails on recent Open Liberty builds.

This can be observed by executing `mvn liberty:run` in the project directory.

The core trouble is that Liberty's dispatcher expects an `IExtendedRequest`, but Krazo is given a proxy object from RESTEasy (replacing CXF in recent Liberty) that dos not wrap that request in the official way.

This problem can be worked around using a local build of Krazo that changes the end of `ServletViewEngine` to retrieve the `IExtendedRequest` that Liberty wants reflectively:

```java
try {
  Class<?> requestStateClass = Class.forName("com.ibm.wsspi.webcontainer.WebContainerRequestState", false, request.getClass().getClassLoader());
  Method getInstance = requestStateClass.getDeclaredMethod("getInstance", boolean.class);
  Object requestState = getInstance.invoke(null, false);
  Method getCurrentThreadsIExtendedRequest = requestStateClass.getDeclaredMethod("getCurrentThreadsIExtendedRequest");
  request = (HttpServletRequest)getCurrentThreadsIExtendedRequest.invoke(requestState);
} catch (Throwable e1) {
  // Not on WAS
}
rd.forward(new HttpServletRequestWrapper(request), new HttpServletResponseWrapper(response));
```

This is not a _good_ solution, but it technically works.