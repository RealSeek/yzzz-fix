package me.realseek.yzzzfix.mixin.modernui;

import icyllis.arc3d.engine.RecordingContext;
import icyllis.arc3d.granite.ClipStack;
import icyllis.arc3d.granite.DrawTask;
import icyllis.arc3d.granite.SurfaceDrawContext;
import icyllis.arc3d.granite.geom.BoundsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "icyllis.arc3d.granite.GraniteDevice", remap = false)
public abstract class ModernUiGraniteDeviceMixin {

    @Shadow
    private RecordingContext mRC;

    @Shadow
    private SurfaceDrawContext mSDC;

    @Shadow
    private ClipStack mClipStack;

    @Shadow
    private int mCurrentDepth;

    @Shadow
    private BoundsManager mColorDepthBoundsManager;

    /**
     * @author qihua233
     * @reason 避免快速点击 F11 切换窗口化和全屏状态导致崩溃。
     */
    @Overwrite
    public void flushPendingWork() {
        RecordingContext recordingContext = this.mRC;
        SurfaceDrawContext surfaceDrawContext = this.mSDC;
        if (recordingContext == null || surfaceDrawContext == null) {
            return;
        }

        recordingContext.getAtlasProvider().recordUploads(surfaceDrawContext);
        this.mClipStack.recordDeferredClipDraws();
        surfaceDrawContext.flush(recordingContext);
        this.mColorDepthBoundsManager.clear();
        this.mCurrentDepth = 0;
        recordingContext.getAtlasProvider().compact();
        DrawTask task = surfaceDrawContext.snapDrawTask(recordingContext);
        if (task != null) {
            recordingContext.addTask(task);
        }
    }
}
