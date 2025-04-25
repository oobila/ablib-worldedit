package com.github.oobila.bukkit.worldedit.scheduling;

import com.github.oobila.bukkit.common.scheduling.AsyncJob;
import com.github.oobila.bukkit.common.scheduling.jobs.MaterialPlacer;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Objects;

public class RegionFillOperation extends AsyncJob {

    private final World world;
    private final Region region;
    private final Material material;
    private final int maxBlockBatchCount;
    private final boolean neighbors;

    public RegionFillOperation(Region region, Material material, int maxBlockBatchCount) {
        this(region, material, maxBlockBatchCount, false);
    }

    public RegionFillOperation(Region region, Material material, int maxBlockBatchCount, boolean neighbors) {
        this.world = BukkitAdapter.adapt(Objects.requireNonNull(region.getWorld()));
        this.region = region;
        this.material = material;
        this.maxBlockBatchCount = maxBlockBatchCount;
        this.neighbors = neighbors;
    }

    @Override
    public void run() {
        MaterialPlacer materialPlacer = new MaterialPlacer(world);
        int i = 0;
        for (BlockVector3 blockVector3 : region) {
            if(i >= maxBlockBatchCount){
                addChildJob(materialPlacer);
                materialPlacer = new MaterialPlacer(world);
                i = 0;
            }
            Location location = BukkitAdapter.adapt(world, blockVector3);
            materialPlacer.addBlock(material, location);
            i++;
        }
        addChildJob(materialPlacer);
    }

    @Override
    public String toString() {
        return "RegionFillOperation{" +
                "material=" + material.toString() +
                '}';
    }
}