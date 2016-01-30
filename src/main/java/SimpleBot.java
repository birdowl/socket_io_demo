import com.codeminders.socketio.common.SocketIOException;
import com.codeminders.socketio.server.Room;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2015 Happy Gears
 * author: vadim2
 * Date: 5/27/15
 */
public class SimpleBot {
    private static final Logger log = Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final String authorName;
    private final Room room;


    public SimpleBot(String authorName, Room room) {
        this.authorName = authorName;
        this.room = room;
    }


    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                writeAll(getMessage());
            }
//        }, 0, 1, TimeUnit.MINUTES);
        }, 0, 10, TimeUnit.SECONDS);

    }

    public void stop() {

    }

    protected String getMessage() {
        return "bot says hello";
    }

    public void writeAll(final String message)
    {
        try
        {
            room.emit("chat message", ChatServlet.createMessageObject(authorName, message));
        }
        catch (SocketIOException e)
        {
            e.printStackTrace();
        }
    }
}
