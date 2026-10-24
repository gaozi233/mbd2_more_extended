package com.gaozi.mbd2_more_extended.trait;

import com.gaozi.mbd2_more_extended.capability.SourceRecipeCapability;
import com.gaozi.mbd2_more_extended.trait.SourceTraitDefinition.SourceIO;
import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import com.hollingsworth.arsnouveau.api.source.ISpecialSourceProvider;
import com.hollingsworth.arsnouveau.api.source.SourceManager;
import com.hollingsworth.arsnouveau.api.util.SourceUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.mbd2.api.capability.recipe.IO;
import com.lowdragmc.mbd2.api.capability.recipe.IRecipeHandlerTrait;
import com.lowdragmc.mbd2.api.recipe.MBDRecipe;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.trait.RecipeCapabilityTrait;
import com.lowdragmc.mbd2.common.trait.RecipeHandlerTrait;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 魔源（Ars Nouveau Source）存储特质。
 *
 * <p>交互模式只有 {@link SourceIO#IN} / {@link SourceIO#OUT} / {@link SourceIO#BOTH}，不区分机器的面：</p>
 * <ul>
 *     <li><b>IN</b>：机器只接收魔源。通过 {@link SourceManager} 注册 provider，Ars 的魔源链接
 *     （Sourcelink）会按 Ars 自己的逻辑把魔源推进来；对 Ars 的“取魔源”接口隐藏库存，避免被仪式抽走。</li>
 *     <li><b>OUT</b>：机器只输出魔源。provider 暴露真实库存，Ars 的仪式可以直接抽取；
 *     同时每 10 tick 按 Ars 的 {@code SourcelinkTile} 逻辑，把魔源推给 5 格内可接收的魔源罐/provider。</li>
 *     <li><b>BOTH</b>：类似 Ars 的魔源罐。既能接收魔源链接推入的魔源，也能被 Ars 仪式抽取；
 *     不主动向其他方块推送（推送是 OUT 的职责）。</li>
 * </ul>
 *
 * <p>本类引用 Ars Nouveau 的类，只有在 ars_nouveau 已加载时才会被注册和实例化。</p>
 */
public class SourceTrait extends RecipeCapabilityTrait implements ISourceTile {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(SourceTrait.class);

    private static final int PUSH_INTERVAL = 10;
    private static final int PUSH_RANGE = 5;

    /** 当前存储的魔源，持久化并同步到客户端（UI 依赖它）。 */
    @Persisted
    @DescSynced
    private int source;

    /** 给 Ars 使用的视图，按 IN/OUT 隐藏或暴露不同操作。 */
    private final ISourceTile arsView = new ArsView();
    private final ISpecialSourceProvider arsProvider = new ArsProvider();
    private final SourceRecipeHandler recipeHandlerIn = new SourceRecipeHandler(IO.IN);
    private final SourceRecipeHandler recipeHandlerOut = new SourceRecipeHandler(IO.OUT);

    private Level providerLevel;
    private boolean providerRegistered;

    public SourceTrait(MBDMachine machine, SourceTraitDefinition definition) {
        super(machine, definition);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public SourceTraitDefinition getDefinition() {
        return (SourceTraitDefinition) super.getDefinition();
    }

    public SourceIO getIoMode() {
        return getDefinition().getIoMode();
    }

    /** 把魔源暴露给 MBD2 的配方系统（输入消耗 / 输出产出）。 */
    @Override
    public List<IRecipeHandlerTrait<?>> getRecipeHandlerTraits() {
        // MBD2 的 proxy 按 handler.getHandlerIO() 建表，BOTH 需要拆成 IN + OUT 两个 handler，
        // 否则 recipe 逻辑按 IO.IN / IO.OUT 查表时找不到 BOTH 这个 key。
        return switch (getDefinition().getRecipeHandlerIO()) {
            case IN -> List.<IRecipeHandlerTrait<?>>of(recipeHandlerIn);
            case OUT -> List.<IRecipeHandlerTrait<?>>of(recipeHandlerOut);
            case BOTH -> List.<IRecipeHandlerTrait<?>>of(recipeHandlerIn, recipeHandlerOut);
            default -> List.of();
        };
    }

    // ------------------------------------------------------------------
    // 机器生命周期：注册/移除 Ars provider
    // ------------------------------------------------------------------

    @Override
    public void onMachineLoad() {
        registerSourceProvider();
    }

    @Override
    public void onChunkUnloaded() {
        unregisterSourceProvider();
    }

    @Override
    public void onMachineUnLoad() {
        unregisterSourceProvider();
    }

    @Override
    public void onMachineRemoved() {
        unregisterSourceProvider();
    }

    @Override
    public void serverTick() {
        if (getIoMode() != SourceIO.OUT) {
            return;
        }
        Level level = getMachine().getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        registerSourceProvider();
        if (source <= 0 || level.getGameTime() % PUSH_INTERVAL != 0) {
            return;
        }
        pushToNearbyTargets(level);
    }

    private void registerSourceProvider() {
        if (providerRegistered) {
            return;
        }
        Level level = getMachine().getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        SourceManager.INSTANCE.addInterface(level, arsProvider);
        providerLevel = level;
        providerRegistered = true;
    }

    private void unregisterSourceProvider() {
        if (!providerRegistered) {
            return;
        }
        if (providerLevel != null) {
            SourceManager.INSTANCE.getSetForLevel(providerLevel).remove(arsProvider);
        }
        providerRegistered = false;
        providerLevel = null;
    }

    /** 按 Ars 的 SourcelinkTile 逻辑，把魔源推给附近能接收的魔源罐 / provider。 */
    private void pushToNearbyTargets(Level level) {
        List<ISpecialSourceProvider> targets = SourceUtil.canGiveSource(getMachine().getPos(), level, PUSH_RANGE);
        BlockPos selfPos = getMachine().getPos();
        for (ISpecialSourceProvider target : targets) {
            if (target.getCurrentPos().equals(selfPos)) {
                continue;
            }
            ISourceTile tile = target.getSource();
            if (tile == arsView) {
                continue;
            }
            int rate = Math.min(Math.min(getTransferRate(), source),
                    tile.getMaxSource() - tile.getSource());
            if (rate <= 0) {
                continue;
            }
            tile.addSource(rate);
            removeSource(rate);
            if (source <= 0) {
                break;
            }
        }
    }

    // ------------------------------------------------------------------
    // 内部存储（ISourceTile，真实数据）
    // ------------------------------------------------------------------

    @Override
    public int getTransferRate() {
        return getDefinition().getTransferRate();
    }

    @Override
    public boolean canAcceptSource() {
        return source < getMaxSource();
    }

    @Override
    public int getSource() {
        return source;
    }

    @Override
    public int getMaxSource() {
        return getDefinition().getCapacity();
    }

    @Override
    public void setMaxSource(int maxSource) {
        // 容量属于机器定义上的配置（editor 里改），这里不做运行时修改。
    }

    @Override
    public int setSource(int value) {
        int clamped = Mth.clamp(value, 0, getMaxSource());
        if (clamped != source) {
            source = clamped;
            onChanged();
        }
        return source;
    }

    @Override
    public int addSource(int amount) {
        if (amount <= 0) {
            return source;
        }
        return setSource(source + amount);
    }

    @Override
    public int removeSource(int amount) {
        if (amount <= 0) {
            return source;
        }
        return setSource(source - amount);
    }

    /** MBD2 配方 handler：按固定方向消耗/产出魔源。成功时返回 null（MBD2 约定），有剩余才返回列表。 */
    public final class SourceRecipeHandler extends RecipeHandlerTrait<Integer> {

        private final IO handlerIO;

        SourceRecipeHandler(IO handlerIO) {
            super(SourceTrait.this, SourceRecipeCapability.CAP);
            this.handlerIO = handlerIO;
        }

        @Override
        public IO getHandlerIO() {
            return handlerIO;
        }

        @Override
        public List<Integer> handleRecipeInner(IO io, MBDRecipe recipe, List<Integer> left,
                                               String slotName, boolean simulate) {
            if (!compatibleWith(io)) {
                return left;
            }
            int total = left.stream().mapToInt(Integer::intValue).sum();
            if (total <= 0) {
                return null;
            }
            if (handlerIO == IO.IN) {
                int consumed = Math.min(total, source);
                if (!simulate) {
                    removeSource(consumed);
                }
                int remaining = total - consumed;
                return remaining > 0 ? List.of(remaining) : null;
            }
            if (handlerIO == IO.OUT) {
                int accepted = Math.min(total, Math.max(0, getMaxSource() - source));
                if (!simulate) {
                    addSource(accepted);
                }
                int remaining = total - accepted;
                return remaining > 0 ? List.of(remaining) : null;
            }
            return left;
        }
    }

    /**
     * Ars 看到的视图：IN 模式隐藏库存（不能被取），OUT 模式隐藏接收能力（不会被灌）。
     * 这样 {@code SourceUtil.canGiveSource} / {@code canTakeSource} 会按方向自动筛选。
     */
    private final class ArsView implements ISourceTile {
        @Override
        public int getTransferRate() {
            return SourceTrait.this.getTransferRate();
        }

        @Override
        public boolean canAcceptSource() {
            return getIoMode() != SourceIO.OUT && SourceTrait.this.canAcceptSource();
        }

        @Override
        public int getSource() {
            return getIoMode() == SourceIO.IN ? 0 : source;
        }

        @Override
        public int getMaxSource() {
            return SourceTrait.this.getMaxSource();
        }

        @Override
        public void setMaxSource(int maxSource) {
            SourceTrait.this.setMaxSource(maxSource);
        }

        @Override
        public int setSource(int value) {
            return SourceTrait.this.setSource(value);
        }

        @Override
        public int addSource(int amount) {
            return getIoMode() == SourceIO.OUT ? source : SourceTrait.this.addSource(amount);
        }

        @Override
        public int removeSource(int amount) {
            return getIoMode() == SourceIO.IN ? source : SourceTrait.this.removeSource(amount);
        }
    }

    /** 让 Ars 的 Sourcelink / 仪式通过 SourceManager 找到这台机器。 */
    private final class ArsProvider implements ISpecialSourceProvider {
        @Override
        public ISourceTile getSource() {
            return arsView;
        }

        @Override
        public boolean isValid() {
            Level level = getMachine().getLevel();
            return level != null && !level.isClientSide
                    && getMachine().getHolder() != null
                    && !getMachine().getHolder().isRemoved();
        }

        @Override
        public BlockPos getCurrentPos() {
            return getMachine().getPos();
        }
    }
}
