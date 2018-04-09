package ichttt.mods.firstaid.client.util;

import com.google.common.base.MoreObjects;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlayerModelRenderer {
    private static ModelPlayer activePlayerModel;
    private static ResourceLocation location;
    private static final ModelPlayer bigArms = new ModelPlayer(1F, false);
    private static final ModelPlayer smallArms = new ModelPlayer(1F, true);

    private static void init() {
        Minecraft mc = Minecraft.getMinecraft();
        //This is some long statement with many casts, let's hope it works
        ModelPlayer renderModel = (ModelPlayer) ((RenderPlayer) MoreObjects.firstNonNull(mc.getRenderManager().<AbstractClientPlayer>getEntityRenderObject(mc.player), bigArms)).mainModel;
        activePlayerModel = renderModel.smallArms ? smallArms : bigArms;
        location = mc.player.getLocationSkin();
    }

    //TODO feet? some color blending?
    public static void renderPlayer(AbstractPlayerDamageModel damageModel, float scale, float alpha) {
        init();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
        GlStateManager.colorMask(true, true, true, true);
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        GlStateManager.translate(20, 100, 0);
        GlStateManager.rotate(180, 0, 1,0);
        GlStateManager.scale(scale, scale, scale);
        setGLColorFor(damageModel.HEAD, alpha);
        activePlayerModel.bipedHead.render(1);
        GlStateManager.color(1F, 1F, 1F, alpha);
        activePlayerModel.bipedHeadwear.render(1);
        setGLColorFor(damageModel.BODY, alpha);
        activePlayerModel.bipedBody.render(1);
        setGLColorFor(damageModel.RIGHT_ARM, alpha);
        activePlayerModel.bipedRightArm.render(1);
        setGLColorFor(damageModel.LEFT_ARM, alpha);
        activePlayerModel.bipedLeftArm.render(1);
        setGLColorFor(damageModel.RIGHT_LEG, alpha);
        activePlayerModel.bipedRightLeg.render(1);
        setGLColorFor(damageModel.LEFT_LEG, alpha);
        activePlayerModel.bipedLeftLeg.render(1);
        GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
        GlStateManager.popMatrix();
    }

    private static void setGLColorFor(AbstractDamageablePart part, float alpha) {
        float otherColor = part.currentHealth / part.getMaxHealth();
        if (otherColor > 1F || otherColor < 0F) {
            FirstAid.logger.error(String.format("Computed invalid color %s for part %s with current health %s and max health %d", otherColor, part.part, part.currentHealth, part.getMaxHealth()));
            GlStateManager.color(1F, 1F, 1F, alpha);
        }
        else {
            GlStateManager.color(1F, otherColor, otherColor, alpha);
        }
    }
}
