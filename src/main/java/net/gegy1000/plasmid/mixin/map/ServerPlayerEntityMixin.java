package net.gegy1000.plasmid.mixin.map;

import com.mojang.authlib.GameProfile;
import net.gegy1000.plasmid.game.map.template.MapTemplateViewer;
import net.gegy1000.plasmid.game.map.template.StagingMapTemplate;
import net.gegy1000.plasmid.game.map.template.trace.PartialRegion;
import net.gegy1000.plasmid.game.map.template.trace.RegionTraceMode;
import net.gegy1000.plasmid.game.map.template.trace.RegionTracer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements MapTemplateViewer, RegionTracer {
    private StagingMapTemplate viewing;

    private PartialRegion tracing;
    private PartialRegion ready;

    private RegionTraceMode traceMode = RegionTraceMode.OFFSET;

    public ServerPlayerEntityMixin(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

    @Override
    public void setViewing(StagingMapTemplate map) {
        this.viewing = map;
    }

    @Nullable
    @Override
    public StagingMapTemplate getViewing() {
        return this.viewing;
    }

    @Override
    public void startTracing(BlockPos origin) {
        this.ready = null;
        this.tracing = new PartialRegion(origin);
    }

    @Override
    public void trace(BlockPos pos) {
        if (this.tracing != null) {
            this.tracing.setTarget(pos);
        }
    }

    @Override
    public void finishTracing(BlockPos pos) {
        this.tracing.setTarget(pos);
        this.ready = this.tracing;
        this.tracing = null;
    }

    @Override
    public boolean isTracing() {
        return this.tracing != null;
    }

    @Nullable
    @Override
    public PartialRegion getTracing() {
        return this.tracing != null ? this.tracing : this.ready;
    }

    @Nullable
    @Override
    public PartialRegion takeReady() {
        PartialRegion ready = this.ready;
        this.ready = null;
        return ready;
    }

    @Override
    public void setMode(RegionTraceMode traceMode) {
        this.traceMode = traceMode;
    }

    @Override
    public RegionTraceMode getMode() {
        return this.traceMode;
    }
}
