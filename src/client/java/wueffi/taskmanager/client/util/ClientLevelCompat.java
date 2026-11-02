package wueffi.taskmanager.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helpers for client world data that newer Minecraft versions expose directly but 1.21.1 does not.
 */
public final class ClientLevelCompat {

    private static ClientLevel cachedLevel;
    private static long cachedGameTime = Long.MIN_VALUE;
    private static List<BlockEntity> cachedBlockEntities = List.of();

    private ClientLevelCompat() {
    }

    /**
     * Returns every block entity in the client chunks around the player, cached per game tick.
     * Must be called on the client thread.
     */
    public static synchronized List<BlockEntity> blockEntities(ClientLevel level) {
        if (level == null) {
            return List.of();
        }
        long gameTime = level.getGameTime();
        if (level == cachedLevel && gameTime == cachedGameTime) {
            return cachedBlockEntities;
        }

        Minecraft client = Minecraft.getInstance();
        List<BlockEntity> result = new ArrayList<>();
        if (client.player != null) {
            ChunkPos center = client.player.chunkPosition();
            int radius = Math.max(2, client.options.getEffectiveRenderDistance()) + 1;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    LevelChunk chunk = level.getChunkSource().getChunk(center.x + dx, center.z + dz, ChunkStatus.FULL, false);
                    if (chunk != null && !chunk.isEmpty()) {
                        result.addAll(chunk.getBlockEntities().values());
                    }
                }
            }
        }

        cachedLevel = level;
        cachedGameTime = gameTime;
        cachedBlockEntities = Collections.unmodifiableList(result);
        return cachedBlockEntities;
    }
}
