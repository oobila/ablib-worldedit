package com.github.oobila.bukkit.worldedit;

import com.sk89q.worldedit.world.block.BaseBlock;

public interface BaseBlockFilter {

    boolean matches(BaseBlock baseBlock);

}
