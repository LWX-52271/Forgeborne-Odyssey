package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 自定义音效注册类
 */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = 
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ForgeborneOdyssey.MOD_ID);

    // 石砧锻打音效
    public static final RegistryObject<SoundEvent> ANVIL_HIT = SOUND_EVENTS.register("anvil.hit", 
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "anvil.hit")));

    public static final RegistryObject<SoundEvent> ANVIL_STRETCH_START = SOUND_EVENTS.register("anvil.stretch_start", 
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "anvil.stretch_start")));

    public static final RegistryObject<SoundEvent> ANVIL_STRETCH_COMPLETE = SOUND_EVENTS.register("anvil.stretch_complete", 
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "anvil.stretch_complete")));

    // 锻造进度里程碑音效（每 10% 播放一次）
    public static final RegistryObject<SoundEvent> FORGING_MILESTONE_10 = SOUND_EVENTS.register("forging.milestone_10",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "forging.milestone_10")));
    
    public static final RegistryObject<SoundEvent> FORGING_MILESTONE_50 = SOUND_EVENTS.register("forging.milestone_50",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "forging.milestone_50")));
    
    public static final RegistryObject<SoundEvent> FORGING_MILESTONE_90 = SOUND_EVENTS.register("forging.milestone_90",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "forging.milestone_90")));

    // 锤子材质音效
    public static final RegistryObject<SoundEvent> HAMMER_STONE_LIGHT = SOUND_EVENTS.register("hammer.stone_light",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "hammer.stone_light")));
    
    public static final RegistryObject<SoundEvent> HAMMER_STONE_HEAVY = SOUND_EVENTS.register("hammer.stone_heavy",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "hammer.stone_heavy")));

    // 岩石挖掘音效
    public static final RegistryObject<SoundEvent> ROCK_PICK_HIT = SOUND_EVENTS.register("rock.pick_hit",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "rock.pick_hit")));
    
    public static final RegistryObject<SoundEvent> ROCK_CRACK_1 = SOUND_EVENTS.register("rock.crack_1",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "rock.crack_1")));
    
    public static final RegistryObject<SoundEvent> ROCK_CRACK_2 = SOUND_EVENTS.register("rock.crack_2",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "rock.crack_2")));
    
    public static final RegistryObject<SoundEvent> ROCK_CRACK_3 = SOUND_EVENTS.register("rock.crack_3",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "rock.crack_3")));
    
    // 岩石碎裂音效
    public static final RegistryObject<SoundEvent> ROCK_BREAK = SOUND_EVENTS.register("rock.break",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "rock.break")));

    // 火裂采矿 - 蒸汽嘶嘶声
    public static final RegistryObject<SoundEvent> ROCK_SIZZLE = SOUND_EVENTS.register("rock.sizzle",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "rock.sizzle")));

    // 火裂采矿 - 热裂崩碎声
    public static final RegistryObject<SoundEvent> ROCK_THERMAL_CRACK = SOUND_EVENTS.register("rock.thermal_crack",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "rock.thermal_crack")));

    // 火裂采矿 - 蒸汽嘶嘶声（水击）
    public static final RegistryObject<SoundEvent> ROCK_STEAM = SOUND_EVENTS.register("rock.steam",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "rock.steam")));

    // 塌方音效
    public static final RegistryObject<SoundEvent> ROCK_CAVE_IN = SOUND_EVENTS.register("rock.cave_in",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "rock.cave_in")));

    // 冶炼陶吹管吹气音效
    public static final RegistryObject<SoundEvent> BLOWPIPE_BLOW = SOUND_EVENTS.register("blowpipe.blow",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "blowpipe.blow")));

    // 投石索甩动音效
    public static final RegistryObject<SoundEvent> SLING_SPIN = SOUND_EVENTS.register("sling.spin",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "sling.spin")));

    // 投石索释放音效
    public static final RegistryObject<SoundEvent> SLING_RELEASE = SOUND_EVENTS.register("sling.release",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "sling.release")));

    /**
     * 注册音效到事件总线
     */
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}