package com.github.oobila.bukkit.worldedit.scheduling;

import com.github.oobila.bukkit.common.scheduling.Job;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class WorldEditBlockPlacer extends Job {

    private final World world;
    private final List<BaseBlock> blockDataList = new ArrayList<>();
    private final List<BlockVector3> locations = new ArrayList<>();

    WorldEditBlockPlacer(World world) {
        this.world = world;
    }

    void addBlock(BaseBlock baseBlock, BlockVector3 location){
        blockDataList.add(baseBlock);
        locations.add(location);
    }

    @Override
    public void run() {
        for(int i = 0; i < locations.size(); i++){
            try {
                world.setBlock(locations.get(i), blockDataList.get(i));
            } catch (WorldEditException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Failure in WorldEditBlockPlacer job", e);
            }
        }
    }
}
