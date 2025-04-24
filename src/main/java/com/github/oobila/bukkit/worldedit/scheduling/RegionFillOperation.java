package com.github.oobila.bukkit.worldedit.scheduling;

import com.github.oobila.bukkit.common.scheduling.AsyncJob;
import com.github.oobila.bukkit.common.scheduling.jobs.MaterialPlacer;
import com.github.oobila.bukkit.common.utils.MaterialUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class RegionFillOperation extends AsyncJob {

    private final World world;
    private final Region region;
    private final Material material;
    private final int maxBlockBatchCount;
    private final boolean lookAhead;
    private final Function<Location, Boolean> meetsCriteriaForLookahead;

    public RegionFillOperation(Region region, Material material, int maxBlockBatchCount) {
        this(region, material, maxBlockBatchCount, false);
    }

    public RegionFillOperation(Region region, Material material, int maxBlockBatchCount, boolean lookAhead) {
        this(region, material, maxBlockBatchCount, lookAhead, RegionFillOperation::defaultLookaheadCheck);
    }

    public RegionFillOperation(Region region, Material material, int maxBlockBatchCount, boolean lookAhead,
                               Function<Location, Boolean> meetsCriteriaForLookahead) {
        this.world = BukkitAdapter.adapt(Objects.requireNonNull(region.getWorld()));
        this.region = region;
        this.material = material;
        this.maxBlockBatchCount = maxBlockBatchCount;
        this.lookAhead = lookAhead;
        this.meetsCriteriaForLookahead = meetsCriteriaForLookahead;
    }

    @Override
    public void run() {
        MaterialPlacer materialPlacer = new MaterialPlacer(world);
        Set<BlockVector3> lookAheadBv3s = new HashSet<>();
        if (lookAhead) {
            for (BlockVector3 blockVector3 : region) {
                Location location = BukkitAdapter.adapt(world, blockVector3);
                boolean meetsCriteria = meetsCriteriaForLookahead.apply(location);
                if (meetsCriteria) {
                    lookAheadBv3s.add(blockVector3);
                    materialPlacer.addBlock(material, location);
                }
            }
            addChildJob(materialPlacer);
            materialPlacer = new MaterialPlacer(world);
        }
        int i = 0;
        for (BlockVector3 blockVector3 : region) {
            if (lookAhead && lookAheadBv3s.contains(blockVector3)) {
                continue;
            }
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

    private static Boolean defaultLookaheadCheck(Location location) {
        return MaterialUtil.isSign(location.getBlock().getType());
    }
}