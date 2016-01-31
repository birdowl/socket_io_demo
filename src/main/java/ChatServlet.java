import com.codeminders.socketio.common.SocketIOException;
import com.codeminders.socketio.server.ConnectionException;
import com.codeminders.socketio.server.ConnectionListener;
import com.codeminders.socketio.server.EventListener;
import com.codeminders.socketio.server.Socket;
import com.codeminders.socketio.server.transport.jetty.JettySocketIOServlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ChatServlet extends JettySocketIOServlet
{
    private static final long                     serialVersionUID = 1L;
    private static final Logger                   log              = Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());
    private              ScheduledExecutorService scheduler        = Executors.newSingleThreadScheduledExecutor();

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        ChuckNorrisJokesBot chuckNorrisJokesBot = new ChuckNorrisJokesBot(of("/v2").in("cnj"));
        ChuckNorrisJokesBot.setInstance(chuckNorrisJokesBot);
        chuckNorrisJokesBot.start();

        of("/v2").on(new ConnectionListener()
        {
            @Override
            public void onConnect(final Socket socket) throws ConnectionException
            {
                socket.join("chat");
                socket.on("cnj", new EventListener()
                {
                    @Override
                    public Object onEvent(String name, Object[] args, boolean ackRequested)
                    {
                        socket.join("cnj");
                        return "OK";
                    }
                });
                socket.on("chat message", new EventListener()
                {
                    @Override
                    public Object onEvent(String name, Object[] args, boolean ackRequested)
                    {
                        try
                        {
                            socket.broadcast("chat", name, args);
                        }
                        catch (SocketIOException e)
                        {
                            e.printStackTrace();
                        }
                        return "OK"; //confirm reception
                    }
                });
                socket.on("discover", new EventListener()
                {
                    @Override
                    public Object onEvent(String name, Object[] args, boolean ackRequested)
                    {
                        final ScheduledFuture<?> discoverHandle =
                                scheduler.scheduleAtFixedRate(new Runnable()
                                {
                                    private int counter = 0;
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            socket.emit("status", createStatusObject("Discovering...",
                                                    true,
                                                    "Abracadabra... " + counter  , 1, "working..."));
                                            counter++;
                                        }
                                        catch (SocketIOException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 0, 1, TimeUnit.SECONDS);
                        scheduler.schedule(new Runnable()
                        {
                            public void run()
                            {
                                discoverHandle.cancel(true);
                                try
                                {
                                    socket.emit("status", createStatusObject("Discover",
                                            false,
                                            "Last discover: " + new Date(), 0, "success"));
                                }
                                catch (SocketIOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }, 20, TimeUnit.SECONDS);

                        return null;
                    }
                });
            }
        });

    }

    public static Map<String, String> createMessageObject(String author, String message)
    {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("author", author);
        map.put("text", message);
        map.put("time", new Date().toString());

        return map;
    }

    private static Map<String, Object> createStatusObject(String buttonText,
                                                          boolean operationInProgress,
                                                          String message,
                                                          int statusCode,
                                                          String statusMessage)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("buttonText", buttonText);
        map.put("operationInProgress", operationInProgress);
        map.put("message", message);
        map.put("statusCode", statusCode);
        map.put("statusMessage", statusMessage);

        return map;
    }
}
