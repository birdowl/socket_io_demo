
import com.codeminders.socketio.server.Room;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Copyright (c) 2015 Happy Gears
 * author: vadim2
 * Date: 5/27/15
 */
public class ChuckNorrisJokesBot extends SimpleBot {
    private static final Logger log = Logger.getLogger(new Throwable().getStackTrace()[0].getClassName());

    private static ChuckNorrisJokesBot instance = null;

    public static ChuckNorrisJokesBot getInstance() {
        return instance;
    }

    public static void setInstance(ChuckNorrisJokesBot instance) {
        ChuckNorrisJokesBot.instance = instance;
    }

    public ChuckNorrisJokesBot(Room room) {
        super("Chuck Norris", room);
    }

    @Override
    protected String getMessage() {
        String jokes = "";
        try (InputStream strm = ChuckNorrisJokesBot.class.getClassLoader().getResourceAsStream("chuck_norris_jokes")) {
            jokes = IOUtils.toString(strm);
        } catch (IOException e) {
            log.error("", e);
        }

        Random random = new Random(System.currentTimeMillis());
        String[] lines = jokes.split("\n");
        return lines[random.nextInt(lines.length - 1)];
    }
}
