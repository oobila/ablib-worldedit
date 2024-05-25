package com.github.oobila.bukkit.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Material;

public class FillRegionOperationBuilder {

    private final Region region;
    private final RandomPattern pattern = new RandomPattern();

    public FillRegionOperationBuilder(Region region) {
        this.region = region;
    }

    public void material(Material material) {
        material(material, 1);
    }

    public void material(Material material, double weight) {
        pattern.add(BukkitAdapter.adapt(material.createBlockData()), weight);
    }

    public void fill() throws MaxChangedBlocksException {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(region.getWorld())) {
            editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
            editSession.setBlocks(region, pattern);
        }
    }
}
