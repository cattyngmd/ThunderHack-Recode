package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.modules.Module;
import thunder.hack.modules.render.HoleESP;
import thunder.hack.setting.Setting;

public class HoleAnchor extends Module {
    public HoleAnchor() {
        super("HoleAnchor", Category.MOVEMENT);
    }


    private final Setting<Integer> pitch = new Setting<>("Pitch", 60, 0, 90);
    private final Setting<Boolean> pull = new Setting<>("Pull", true);


    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent e) {
        if (mc.player.getPitch() > pitch.getValue()) {
            if (
                    (HoleESP.validIndestructible(BlockPos.ofFloored(mc.player.getPos()).down(1)) || HoleESP.validBedrock(BlockPos.ofFloored(mc.player.getPos()).down(1)))
                            || (HoleESP.validIndestructible(BlockPos.ofFloored(mc.player.getPos()).down(2)) || HoleESP.validBedrock(BlockPos.ofFloored(mc.player.getPos()).down(2)))
                            || (HoleESP.validIndestructible(BlockPos.ofFloored(mc.player.getPos()).down(3)) || HoleESP.validBedrock(BlockPos.ofFloored(mc.player.getPos()).down(3)))
                            || (HoleESP.validTwoBlockIndestructibleXZ(BlockPos.ofFloored(mc.player.getPos()).down(1)) || HoleESP.validTwoBlockBedrockXZ(BlockPos.ofFloored(mc.player.getPos()).down(1)))
                            || (HoleESP.validTwoBlockIndestructibleXZ(BlockPos.ofFloored(mc.player.getPos()).down(2)) || HoleESP.validTwoBlockBedrockXZ(BlockPos.ofFloored(mc.player.getPos()).down(2)))
                            || (HoleESP.validTwoBlockIndestructibleXZ(BlockPos.ofFloored(mc.player.getPos()).down(3)) || HoleESP.validTwoBlockBedrockXZ(BlockPos.ofFloored(mc.player.getPos()).down(3)))
                            || (HoleESP.validTwoBlockIndestructibleXZ1(BlockPos.ofFloored(mc.player.getPos()).down(1)) || HoleESP.validTwoBlockBedrockXZ1(BlockPos.ofFloored(mc.player.getPos()).down(1)))
                            || (HoleESP.validTwoBlockIndestructibleXZ1(BlockPos.ofFloored(mc.player.getPos()).down(2)) || HoleESP.validTwoBlockBedrockXZ1(BlockPos.ofFloored(mc.player.getPos()).down(2)))
                            || (HoleESP.validTwoBlockIndestructibleXZ1(BlockPos.ofFloored(mc.player.getPos()).down(3)) || HoleESP.validTwoBlockBedrockXZ1(BlockPos.ofFloored(mc.player.getPos()).down(3)))
            ) {
                if (!pull.getValue()) {
                    mc.player.setVelocity(0,mc.player.getVelocity().getY(),0);
                } else {
                    Vec3d center = new Vec3d(Math.floor(mc.player.getX()) + 0.5, Math.floor(mc.player.getY()), Math.floor(mc.player.getZ()) + 0.5);

                    if (Math.abs(center.x - mc.player.getX()) > 0.1 || Math.abs(center.z - mc.player.getZ()) > 0.1) {
                        double d3 = center.x - mc.player.getX();
                        double d4 = center.z - mc.player.getZ();
                        mc.player.setVelocity(Math.min(d3 / 2.0,0.2),mc.player.getVelocity().getY(),Math.min(d4 / 2.0,0.2));
                    }
                }
            }
        }
    }
}
