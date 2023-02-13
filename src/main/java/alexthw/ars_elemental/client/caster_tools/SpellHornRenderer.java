package alexthw.ars_elemental.client.caster_tools;

import alexthw.ars_elemental.common.items.caster_tools.SpellHorn;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.hollingsworth.arsnouveau.client.renderer.item.FixedGeoItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.ars_nouveau.geckolib3.core.util.Color;
import software.bernie.ars_nouveau.geckolib3.geo.render.built.GeoBone;

public class SpellHornRenderer extends FixedGeoItemRenderer<SpellHorn> {

    public SpellHornRenderer() {
        super(new SpellHornModel());
    }

    @Override
    public void renderRecursively(GeoBone bone, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        //we override the color getter for a specific bone, this means the other ones need to use the neutral color
        if (bone.getName().equals("gems") || (bone.getParent() != null && bone.getParent().getName().equals("gems"))) {
            //NOTE: if the bone have a parent, the recursion will get here with the neutral color, making the color getter useless
            super.renderRecursively(bone, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        } else {
            super.renderRecursively(bone, poseStack, buffer, packedLight, packedOverlay, Color.WHITE.getRed() / 255f, Color.WHITE.getGreen() / 255f, Color.WHITE.getBlue() / 255f, Color.WHITE.getAlpha() / 255f);
        }
    }

    @Override
    public Color getRenderColor(Object animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight) {
        ParticleColor color = ParticleColor.defaultParticleColor();
        if (currentItemStack.hasTag()) {
            color = ((SpellHorn) animatable).getSpellCaster(currentItemStack).getColor();
        }
        return Color.ofRGBA(color.toWrapper().r, color.toWrapper().g, color.toWrapper().b, 200);
    }

    @Override
    public RenderType getRenderType(Object animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        return RenderType.entityTranslucent(textureLocation);
    }

}
