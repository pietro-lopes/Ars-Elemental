package alexthw.ars_elemental.event;

import alexthw.ars_elemental.ArsElemental;
import alexthw.ars_elemental.ArsNouveauRegistry;
import alexthw.ars_elemental.api.IUndeadSummon;
import alexthw.ars_elemental.api.item.ISchoolFocus;
import alexthw.ars_elemental.common.entity.summon.*;
import alexthw.ars_elemental.common.items.armor.SummonPerk;
import alexthw.ars_elemental.common.items.foci.NecroticFocus;
import alexthw.ars_elemental.registry.ModRegistry;
import com.hollingsworth.arsnouveau.api.entity.ISummon;
import com.hollingsworth.arsnouveau.api.event.SummonEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.setup.registry.ModPotions;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = ArsElemental.MODID)
public class SummonEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void summonedEvent(SummonEvent event) {

        SpellSchool focus = ISchoolFocus.hasFocus(event.shooter);

        if (!event.world.isClientSide && focus != null) {

            // boost summoned entities if necromancy focus is equipped
            if (focus == ArsNouveauRegistry.NECROMANCY) {
                if (event.summon.getLivingEntity() != null) {
                    event.summon.getLivingEntity().addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, 1));
                    event.summon.getLivingEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 500, 1));
                }
            }


            // change summoned entities if water/fire/necromancy focus is equipped
            if (event.summon instanceof SummonHorse oldHorse && event.shooter instanceof ServerPlayer summoner) {
                switch (focus.getId()) {
                    case "water" -> {
                        SummonDolphin newHorse = new SummonDolphin(oldHorse, summoner);
                        if (newHorse.getOwnerUUID() != null) {
                            oldHorse.remove(Entity.RemovalReason.DISCARDED);
                            event.summon = newHorse;
                            event.world.addFreshEntity(newHorse);
                            CriteriaTriggers.SUMMONED_ENTITY.trigger(summoner, newHorse);
                        }
                    }
                    case "fire" -> {
                        SummonStrider newHorse = new SummonStrider(oldHorse, summoner);
                        if (newHorse.getOwnerUUID() != null) {
                            oldHorse.remove(Entity.RemovalReason.DISCARDED);
                            event.summon = newHorse;
                            event.world.addFreshEntity(newHorse);
                            CriteriaTriggers.SUMMONED_ENTITY.trigger(summoner, newHorse);
                        }
                    }
                    case "earth" -> {
                        SummonCamel newHorse = new SummonCamel(oldHorse, summoner);
                        if (newHorse.getOwnerUUID() != null) {
                            oldHorse.remove(Entity.RemovalReason.DISCARDED);
                            event.summon = newHorse;
                            event.world.addFreshEntity(newHorse);
                            CriteriaTriggers.SUMMONED_ENTITY.trigger(summoner, newHorse);
                        }
                    }
                    case "necromancy" -> {
                        SummonSkeleHorse newHorse = new SummonSkeleHorse(oldHorse, summoner);
                        if (newHorse.getOwnerUUID() != null) {
                            oldHorse.remove(Entity.RemovalReason.DISCARDED);
                            event.summon = newHorse;
                            event.world.addFreshEntity(newHorse);
                            CriteriaTriggers.SUMMONED_ENTITY.trigger(summoner, newHorse);
                        }
                    }
                    default -> {
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void reRaiseSummon(SummonEvent.Death event) {
        if (!event.world.isClientSide) {
            ServerLevel world = (ServerLevel) event.world;
            var owner = event.summon instanceof IFollowingSummon summon ? summon.getSummoner() : event.summon.getOwner();
            if ((owner instanceof Player player) && !(event.summon instanceof IUndeadSummon)) {
                // re-raise summoned entities if necrotic focus is equipped
                if (NecroticFocus.hasFocus(event.world, player)) {
                    LivingEntity toRaise = null;
                    if (event.summon instanceof SummonWolf wolf) {
                        toRaise = new SummonDirewolf(world, player, wolf);
                    } else if (event.summon instanceof EntityAllyVex vex) {
                        toRaise = new AllyVhexEntity(world, vex, player);
                    } else if (event.summon instanceof SummonSkeleton skelly) {
                        toRaise = new SummonUndead(world, skelly, player);
                    }
                    if (toRaise instanceof IUndeadSummon undead) {
                        undead.inherit(event.summon);
                        event.world.addFreshEntity(toRaise);
                        NecroticFocus.spawnDeathPoof(world, toRaise.blockPosition());
                    }
                }
            }
        }
    }


    @SubscribeEvent
    public static void summonSickReduction(MobEffectEvent.Added event) {
        if (event.getEntity() != null && event.getEffectInstance().getEffect() == ModPotions.SUMMONING_SICKNESS_EFFECT.get() && PerkUtil.countForPerk(SummonPerk.INSTANCE, event.getEntity()) > 0) {
            event.getEffectInstance().duration = event.getEffectInstance().getDuration() * (1 - PerkUtil.countForPerk(SummonPerk.INSTANCE, event.getEntity()) / 10);
        }
    }

    @SubscribeEvent
    public static void summonPowerup(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof ISummon summon && event.getEntity().level() instanceof ServerLevel) {
            if (summon.getOwner() instanceof Player player) {
                float summonPower = (float) player.getAttributeValue(ModRegistry.SUMMON_POWER.get());
                event.setAmount(event.getAmount() + summonPower);

                if (summon instanceof SummonWolf) {
                    SpellSchool school = ISchoolFocus.hasFocus(player);
                    if (school != null) switch (school.getId()) {
                        case "fire" -> event.getEntity().setSecondsOnFire(5);
                        case "water" ->
                                event.getEntity().addEffect(new MobEffectInstance(ModPotions.FREEZING_EFFECT.get(), 100, 1));
                        case "air" ->
                                event.getEntity().addEffect(new MobEffectInstance(ModPotions.SHOCKED_EFFECT.get(), 100, 1));
                        case "earth" -> event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, 100));
                    }
                }
            }
        }
    }
}
