package com.github.oobila.bukkit.worldedit.scheduling;

import com.github.oobila.bukkit.common.scheduling.Job;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class WorldEditMaterialPlacer extends Job {

    private final EditSession editSession;
    private final List<Pattern> materialList = new ArrayList<>();
    private final List<BlockVector3> locations = new ArrayList<>();

    public WorldEditMaterialPlacer(EditSession editSession) {
        this.editSession = editSession;
    }

    public void addBlock(Material material, Location location) {
        this.materialList.add(BukkitAdapter.adapt(material.createBlockData()));
        this.locations.add(BukkitAdapter.asBlockVector(location));
    }

    @Override
    public void run() {
        for(int i = 0; i < this.locations.size(); ++i) {
            try {
                editSession.setBlock(locations.get(i), materialList.get(i));
            } catch (MaxChangedBlocksException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
