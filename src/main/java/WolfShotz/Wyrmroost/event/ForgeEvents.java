package WolfShotz.Wyrmroost.event;

import WolfShotz.Wyrmroost.content.entities.sliverglider.SilverGliderEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import org.lwjgl.opengl.GL11;

import java.util.Optional;

/**
 * Class Used to hook into forge events to perform particular tasks when the event is called
 * (e.g. player uses an anvil, cancel the normal behaviour and do something else instead)
 */
@SuppressWarnings("unused")
public class ForgeEvents
{
    /**
     * Responsible for handling fall damage
     */
    public static void cancelFall(LivingFallEvent event) {
        PlayerEntity player;

        if (!(event.getEntity() instanceof PlayerEntity)) return;
        else player = (PlayerEntity) event.getEntity();
        if (player.getPassengers().isEmpty()) return;

        Optional<Entity> entity = player.getPassengers()
                                          .stream()
                                          .filter(SilverGliderEntity.class::isInstance)
                                          .findFirst();

        if (!entity.isPresent()) return;

        SilverGliderEntity glider = (SilverGliderEntity) entity.get();

        if (glider.isGliding) event.setDamageMultiplier(0); // If gliding with a silver glider, cancel fall damage
    }

     /**
      * Handles the camera setup for what the player is looking at
      * @deprecated Currently not functional. See Issue #5911
      */
     @OnlyIn(Dist.CLIENT)
     public static void ridingPerspective(EntityViewRenderEvent.CameraSetup event) {
         Minecraft mc = Minecraft.getInstance();
         ClientPlayerEntity player = mc.player;

         if (player.getPassengers().stream().anyMatch(SilverGliderEntity.class::isInstance))
             if (mc.gameSettings.thirdPersonView == 1) GL11.glTranslatef(0, -0.5f, -0.5f);
     }
}