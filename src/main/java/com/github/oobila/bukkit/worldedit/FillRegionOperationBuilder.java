package com.github.oobila.bukkit.worldedit;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Material;

import static com.github.oobila.bukkit.worldedit.util.WorldEditUtil.editSessionTransaction;

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

    public void fill() throws WorldEditException {
        editSessionTransaction(region.getWorld(), editSession ->
            editSession.setBlocks(region, pattern)
        );
    }
}
