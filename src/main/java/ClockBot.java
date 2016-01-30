
import com.codeminders.socketio.server.Room;

import java.util.Date;

/**
 * Copyright (c) 2015 Happy Gears
 * author: vadim2
 * Date: 5/27/15
 */
public class ClockBot extends SimpleBot {
    private static ClockBot instance = null;

    public static ClockBot getInstance() {
        return instance;
    }

    public static void setInstance(ClockBot instance) {
        ClockBot.instance = instance;
    }

    public ClockBot(Room room) {
        super("clock", room);
    }

    @Override
    protected String getMessage() {
        return new Date().toString();
    }
}
