package com.gaozi.mbd2_more_extended;

import com.gaozi.mbd2_more_extended.capability.SourceRecipeCapability;
import com.gaozi.mbd2_more_extended.recipe.NBTCompareCondition;
import com.gaozi.mbd2_more_extended.trait.SourceTraitDefinition;
import com.lowdragmc.mbd2.common.event.MBDRegistryEvent;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * MBD2 More Extended - MBD2 的功能拓展。
 *
 * <p>本模组是纯功能扩展：不注册任何物品、方块或创造模式标签页，只通过 MBD2 的 API
 * 扩展多方块机器的行为（机器事件、配方类型、机器逻辑等）。</p>
 */
@Mod(MBD2MoreExtended.MODID)
public class MBD2MoreExtended
{
    /** 模组 id，必须与 META-INF/mods.toml 中的一致。 */
    public static final String MODID = "mbd2_more_extended";

    /** 供其他类使用的日志器。 */
    public static final Logger LOGGER = LogUtils.getLogger();

    public MBD2MoreExtended(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // MBD2 功能扩展的入口：
        // 在这里向 modEventBus / MinecraftForge.EVENT_BUS 注册监听器，
        // 或注册自定义的 MBD2 机器事件、配方逻辑等。
        modEventBus.addListener(this::registerRecipeConditions);
        modEventBus.addListener(this::registerTraitTypes);
        modEventBus.addListener(this::registerRecipeCapabilities);
        LOGGER.info("MBD2 More Extended loaded");
    }

    /**
     * 在 MBD2 的配方条件注册表里注册 {@link NBTCompareCondition}。
     * MBD2 会在 FMLConstructModEvent 阶段发出该事件，所有模组的构造器此时都已经执行完，
     * 因此在这里注册的监听器一定能收到。
     */
    private void registerRecipeConditions(MBDRegistryEvent.RecipeCondition event) {
        event.register(NBTCompareCondition.TYPE, NBTCompareCondition.class);
    }

    /**
     * 注册魔源存储特质。只有在 Ars Nouveau 已加载时才注册，
     * 避免未安装 Ars 的整合包加载到依赖 Ars 的类。
     */
    private void registerTraitTypes(MBDRegistryEvent.TraitType event) {
        if (ModList.get().isLoaded("ars_nouveau")) {
            event.register(SourceTraitDefinition.class);
            LOGGER.info("Registered Ars Nouveau source trait type 'ars_source_storage'");
        }
    }

    /**
     * 注册魔源配方能力（配方 JSON 里的 {@code "ars_source"}）。
     * 只有装了 Ars 时才有意义，因此同样按 Ars 是否加载来决定。
     */
    private void registerRecipeCapabilities(MBDRegistryEvent.RecipeCapability event) {
        if (ModList.get().isLoaded("ars_nouveau")) {
            event.register(SourceRecipeCapability.NAME, SourceRecipeCapability.CAP);
            LOGGER.info("Registered Ars Nouveau source recipe capability '{}'", SourceRecipeCapability.NAME);
        }
    }
}
