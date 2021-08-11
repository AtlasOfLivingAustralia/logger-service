package au.org.ala.logger

/**
 * Interceptor for web service calls to logger
 */
class RestInterceptor {

    RestInterceptor() {
        match(controller: "logger")
    }

    boolean before() {
        header("Access-Control-Allow-Origin", "*")
        header("Access-Control-Allow-Credentials", "true")
        header("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
        header("Access-Control-Max-Age", "3600")
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
