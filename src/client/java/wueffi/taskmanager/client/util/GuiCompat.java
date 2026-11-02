package wueffi.taskmanager.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * GUI helpers that paper over differences between the 1.21.1 GuiGraphics API and newer versions.
 */
public final class GuiCompat {

    private GuiCompat() {
    }

    /**
     * Enables a scissor rectangle given in the current pose's coordinate space.
     * <p>
     * The Task Manager screen renders under a scale/translate pose. Newer Minecraft versions apply the
     * pose to scissor rectangles automatically, 1.21.1 does not, so the rectangle is transformed here
     * and then submitted under an identity pose (which makes the result correct either way).
     */
    public static void enableScissor(GuiGraphics ctx, int x1, int y1, int x2, int y2) {
        PoseStack pose = ctx.pose();
        Matrix4f matrix = pose.last().pose();
        Vector3f min = matrix.transformPosition(new Vector3f(x1, y1, 0.0f));
        Vector3f max = matrix.transformPosition(new Vector3f(x2, y2, 0.0f));
        int left = (int) Math.floor(Math.min(min.x, max.x));
        int top = (int) Math.floor(Math.min(min.y, max.y));
        int right = (int) Math.ceil(Math.max(min.x, max.x));
        int bottom = (int) Math.ceil(Math.max(min.y, max.y));

        pose.pushPose();
        pose.setIdentity();
        ctx.enableScissor(left, top, right, bottom);
        pose.popPose();
    }
}
