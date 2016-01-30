import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


import java.net.URL;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by bird on 12/3/15.
 */
public class WebServer {

    private static final Logger log = Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

    private static final URL base_url;

    static {
        try {
            base_url = new URL("http://localhost:9100/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Server server;

    public WebServer() {
        server = new Server();
    }

    /**
     * Build HTTP connector
     */
    private ServerConnector getServerConnectorHttp() {

        // HTTP Configuration
        // HttpConfiguration is a collection of configuration information appropriate for http and https. The default
        // scheme for http is <code>http</code> of course, as the default for secured http is <code>https</code> but
        // we show setting the scheme to show it can be done.  The port for secured communication is also set here.
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setOutputBufferSize(131072);

        // HTTP connector
        // The first server connector we create is the one for http, passing in the http configuration we configured
        // above so it can get things like the output buffer size, etc. We also set the port (8080) and configure an
        // idle timeout.
        ServerConnector http = new ServerConnector(server);
        http.addConnectionFactory(new HttpConnectionFactory(httpConfig));
//        http.addConnectionFactory(new WebSocketServerFactory());
        http.setPort(base_url.getPort());
        http.setIdleTimeout(30000);
        return http;
    }

    public ServletContextHandler makeContext() {
        ServletContextHandler context = new ServletContextHandler(
                server, "/", ServletContextHandler.SESSIONS | ServletContextHandler.SECURITY);


        ServletHolder holder = new ServletHolder(new ChatServlet());
        context.addServlet(holder, "/socket.io/*");

        // Static assets
        holder = context.addServlet(DefaultServlet.class, "/*");
        URL web = this.getClass().getClassLoader().getResource("web");
        holder.setInitParameter("resourceBase", web.toExternalForm());
        holder.setInitParameter("dirAllowed", "false");
        holder.setInitParameter("pathInfoOnly", "true");

        return context;
    }


    public void start() throws Exception {

        ServerConnector http = getServerConnectorHttp();
        server.setConnectors(new Connector[] { http });

//        String logsDir = System.getProperty("LOG_DIR");  //  configManager.getNw2home() + File.separator + configManager.getConfig().getString("ui.logsSubdir");
        String logsDir = "/tmp";
        NCSARequestLog requestLog = new NCSARequestLog(logsDir + "/access-yyyy_mm_dd.request.log");
        requestLog.setRetainDays(7);
        requestLog.setAppend(true);
        requestLog.setExtended(true);
        requestLog.setLogTimeZone(TimeZone.getDefault().getID());

        RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(requestLog);

        ServletContextHandler mainContext = makeContext();

        HandlerList hlist = new HandlerList();
        // order is important, alertsContext should come before apiContext
        hlist.addHandler(mainContext);      // this context has path "/" and should be the last

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[]{hlist, requestLogHandler});
        server.setHandler(handlers);


        List<String> sl = Lists.newArrayList();
        for (Connector conn : server.getConnectors()) {
            sl.add(conn.toString());
        }
        String msg = String.format("%s serving on %s", getClass().getName(), Joiner.on(",").join(sl));
        System.out.println(msg);
        log.info(msg);

        try {
            server.start();
        } catch (Exception e) {
            String err = "Can not start web server: " + e;
            System.err.println(err);
            log.error(err);
            System.exit(1);
        }
    }

    public void stop() throws Exception {
//        if (sessionManager != null) sessionManager.doStop();
        server.stop();
    }

}
