package thunder.hack.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import thunder.hack.ThunderHack;
import thunder.hack.core.Core;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.*;
import thunder.hack.injection.accesors.ISPacketEntityVelocity;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.MovementUtility;

import static thunder.hack.utility.player.MovementUtility.isMoving;

public class Strafe extends Module {
    private final Setting<Boost> boost = new Setting<>("Boost", Boost.None);
    public Setting<Float> setSpeed = new Setting<>("speed", 1.3F, 0.0F, 2f, v -> boost.getValue() == Boost.Elytra);
    private final Setting<Float> velReduction = new Setting<>("Reduction", 6.0f, 0.1f, 10f, v -> boost.getValue() == Boost.Damage);
    private final Setting<Float> maxVelocitySpeed = new Setting<>("MaxVelocity", 0.8f, 0.1f, 2f, v -> boost.getValue() == Boost.Damage);

    public static double oldSpeed, contextFriction, fovval;
    public static boolean needSwap, needSprintState,disabled;
    public static int noSlowTicks,waterTicks,jumpTicks;
    static long disableTime;

    public Strafe() {
        super("Strafe", "testMove", Category.MOVEMENT);
    }

    public double calculateSpeed(EventMove move) {
        float speedAttributes = getAIMoveSpeed();
        final float frictionFactor = mc.world.getBlockState(new BlockPos.Mutable().set(mc.player.getX(), getBoundingBox().getMin(Direction.Axis.Y) - move.get_y(), mc.player.getZ())).getBlock().getSlipperiness() * 0.91F;
        float n6 = mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && mc.player.isUsingItem() ? 0.88f : (float) (oldSpeed > 0.32 && mc.player.isUsingItem() ? 0.88 : 0.91F);
        if (mc.player.isOnGround()) {
            n6 = frictionFactor;
        }
        float n7 = (float) (0.1631f / Math.pow(n6, 3.0f));
        float n8;
        if (mc.player.isOnGround()) {
            n8 = speedAttributes * n7;
            if (move.get_y() > 0) {
                n8 += boost.getValue() == Boost.Elytra && InventoryUtility.getElytra() != -1 && disabled ? 0.65 : 0.2f;
            }
            disabled = false;
        } else {
            n8 = 0.0255f;
        }
        boolean noslow = false;
        double max2 = oldSpeed + n8;
        double max = 0.0;
        if (mc.player.isUsingItem() && move.get_y() <= 0) {
            double n10 = oldSpeed + n8 * 0.25;
            double motionY2 = move.get_y();
            if (motionY2 != 0.0 && Math.abs(motionY2) < 0.08) {
                n10 += 0.055;
            }
            if (max2 > (max = Math.max(0.043, n10))) {
                noslow = true;
                ++noSlowTicks;
            } else {
                noSlowTicks = Math.max(noSlowTicks - 1, 0);
            }
        } else {
            noSlowTicks = 0;
        }
        if (noSlowTicks > 3) {
            max2 = max - 0.019;
        } else {
            max2 = Math.max(noslow ? 0 : 0.25, max2) - (mc.player.age % 2 == 0 ? 0.001 : 0.002);
        }
        contextFriction = n6;
        if (!mc.player.isOnGround()) {
            needSprintState = !mc.player.lastSprinting;
            needSwap = true;
        } else {
            needSprintState = false;
        }
        return max2;
    }

    public float getAIMoveSpeed() {
        boolean prevSprinting = mc.player.isSprinting();
        mc.player.setSprinting(false);
        float speed = mc.player.getMovementSpeed() * 1.3f;
        mc.player.setSprinting(prevSprinting);
        return speed;
    }

    public static void disabler(int elytra) {
        if (elytra == -1) return;
        if (System.currentTimeMillis() - disableTime > 190L) {
            if (elytra != -2) {
                mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, mc.player);
            }

            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

            if (elytra != -2) {
                mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, mc.player);
            }
            disableTime = System.currentTimeMillis();
        }
        disabled = true;
    }



    @Override
    public void onEnable() {
        oldSpeed = 0.0;
        fovval = mc.options.getFovEffectScale().getValue();
        mc.options.getFovEffectScale().setValue(0d);
    }

    @Override
    public void onDisable(){
        mc.options.getFovEffectScale().setValue(fovval);
    }

    public boolean canStrafe() {
        if (mc.player.isSneaking()) {
            return false;
        }
        if (mc.player.isInLava()) {
            return false;
        }
        if (ModuleManager.scaffold.isEnabled()) {
            return false;
        }
        if (ModuleManager.speed.isEnabled()) {
            return false;
        }
        if (mc.player.isSubmergedInWater() || waterTicks > 0) {
            return false;
        }
        return !mc.player.getAbilities().flying;
    }

    public Box getBoundingBox() {
        return new Box(mc.player.getX() - 0.1, mc.player.getY(), mc.player.getZ() - 0.1, mc.player.getX() + 0.1, mc.player.getY() + 1, mc.player.getZ() + 0.1);
    }

    @EventHandler
    public void onMove(EventMove event) {
        int elytraSlot = InventoryUtility.getElytra();

        if (boost.getValue() == Boost.Elytra && elytraSlot != -1) {
            if (isMoving() && !mc.player.isOnGround() && mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, event.get_y(), 0.0f)).iterator().hasNext() && disabled) {
                oldSpeed = setSpeed.getValue();
            }
        }
        if (canStrafe()) {
            if (isMoving()) {
                double[] motions = MovementUtility.forward(calculateSpeed(event));

                event.set_x(motions[0]);
                event.set_z(motions[1]);
            } else {
                oldSpeed = 0;
                event.set_x(0);
                event.set_z(0);
            }
            event.cancel();
        } else {
            oldSpeed = 0;
        }

    }

    @EventHandler
    public void updateValues(EventSync e) {
        oldSpeed = ThunderHack.playerManager.currentPlayerSpeed * contextFriction;
        if (mc.player.isSubmergedInWater()) {
            waterTicks = 10;
        } else {
            waterTicks--;
        }
        if (jumpTicks > 0) {
            jumpTicks--;
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            oldSpeed = 0;
        }
        EntityVelocityUpdateS2CPacket velocity;
        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket && (velocity = e.getPacket()).getId() == mc.player.getId() && boost.getValue() == Boost.Damage) {
            if (mc.player.isOnGround()) return;

            int vX = velocity.getVelocityX();
            int vZ = velocity.getVelocityZ();

            if (vX < 0) vX *= -1;
            if (vZ < 0) vZ *= -1;

            oldSpeed = (vX + vZ) / (velReduction.getValue() * 1000f);
            oldSpeed = Math.min(oldSpeed, maxVelocitySpeed.getValue());

            ((ISPacketEntityVelocity) velocity).setMotionX(0);
            ((ISPacketEntityVelocity) velocity).setMotionY(0);
            ((ISPacketEntityVelocity) velocity).setMotionZ(0);
        }
    }

    @EventHandler
    public void actionEvent(EventSprint eventAction) {
        if (canStrafe()) {
            if (Core.serversprint != needSprintState) {
                eventAction.setSprintState(!Core.serversprint);
            }
        }
        if (needSwap) {
            eventAction.setSprintState(!mc.player.lastSprinting);
            needSwap = false;
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if ((boost.getValue() == Boost.Elytra && InventoryUtility.getElytra() != -1 && !mc.player.isOnGround() && mc.player.fallDistance > 0 && !disabled)) {
            disabler(InventoryUtility.getElytra());
        }
    }

    private enum Boost {
        None, Elytra, Damage
    }
}
