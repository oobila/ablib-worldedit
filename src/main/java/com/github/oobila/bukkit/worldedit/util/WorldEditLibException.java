package com.github.oobila.bukkit.worldedit.util;

import com.sk89q.worldedit.WorldEditException;

public class WorldEditLibException extends WorldEditException {

    public WorldEditLibException(Throwable cause) {
        super(cause);
    }

    public WorldEditLibException(String s) {
        super(s);
    }
}
