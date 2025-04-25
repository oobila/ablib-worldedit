package com.github.oobila.bukkit.worldedit.scheduling;

import com.github.oobila.bukkit.common.scheduling.AsyncJob;
import com.github.oobila.bukkit.common.scheduling.Job;
import com.github.oobila.bukkit.worldedit.util.WorldEditLibException;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Map;
import java.util.Objects;

import static com.github.oobila.bukkit.worldedit.util.WorldEditUtil.editSessionTransaction;

public class RegionFillOperation extends AsyncJob {

    private final World world;
    private final Region region;
    private final Material material;
    private final int maxBlockBatchCount;


    public RegionFillOperation(Region region, Material material, int maxBlockBatchCount) {
        this.world = BukkitAdapter.adapt(Objects.requireNonNull(region.getWorld()));
        this.region = region;
        this.material = material;
        this.maxBlockBatchCount = maxBlockBatchCount;
    }

    @Override
    public void run() {
        WorldEdit worldEdit = WorldEdit.getInstance();
        EditSession editSession = worldEdit.newEditSession(region.getWorld());
        editSession.setSideEffectApplier(new SideEffectSet(Map.of(SideEffect.NEIGHBORS, SideEffect.State.OFF)));

        WorldEditMaterialPlacer materialPlacer = new WorldEditMaterialPlacer(editSession);
        int i = 0;
        for (BlockVector3 blockVector3 : region) {
            if(i >= maxBlockBatchCount){
                addChildJob(materialPlacer);
                materialPlacer = new WorldEditMaterialPlacer(editSession);
                i = 0;
            }
            Location location = BukkitAdapter.adapt(world, blockVector3);
            materialPlacer.addBlock(material, location);
            i++;
        }
        addChildJob(materialPlacer);
        addChildJob(new Job() {
            @Override
            public void run() {
                editSession.close();
            }
        });
    }

    @Override
    public String toString() {
        return "RegionFillOperation{" +
                "material=" + material.toString() +
                '}';
    }
}