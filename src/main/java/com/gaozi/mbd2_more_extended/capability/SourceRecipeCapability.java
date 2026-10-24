package com.gaozi.mbd2_more_extended.capability;

import com.gaozi.mbd2_more_extended.util.ArsSourceTextures;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.NumberConfigurator;
import com.lowdragmc.lowdraglib.gui.widget.TextTextureWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.mbd2.api.capability.recipe.RecipeCapability;
import com.lowdragmc.mbd2.api.recipe.content.Content;
import com.lowdragmc.mbd2.api.recipe.content.SerializerInteger;
import com.lowdragmc.mbd2.common.gui.recipe.CornerNumberWidget;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * MBD2 的魔源配方能力（整数）。
 *
 * <p>配方 JSON 里以 {@code "ars_source"} 作为键，数值表示魔源数量；实际读写由
 * {@code SourceTrait} 里注册的 {@code RecipeHandlerTrait} 完成。</p>
 */
public class SourceRecipeCapability extends RecipeCapability<Integer> {

    public static final String NAME = "ars_source";
    public static final SourceRecipeCapability CAP = new SourceRecipeCapability();

    private SourceRecipeCapability() {
        super(NAME, SerializerInteger.INSTANCE);
    }

    @Override
    public Integer createDefaultContent() {
        return 1000;
    }

    @Override
    public Widget createPreviewWidget(Integer amount) {
        WidgetGroup group = new WidgetGroup(0, 0, 18, 18);
        group.setBackground(ArsSourceTextures.preview());
        CornerNumberWidget number = new CornerNumberWidget(0, 0, 18, 18);
        number.setValue(amount);
        group.addWidget(number);
        return group;
    }

    @Override
    public Widget createXEITemplate() {
        WidgetGroup group = new WidgetGroup(0, 0, 50, 14);
        group.setBackground(ArsSourceTextures.xei());
        TextTextureWidget text = new TextTextureWidget(4, 3, 42, 8, "0 S")
                .textureStyle(texture -> texture.setDropShadow(true));
        group.addWidget(text);
        return group;
    }

    @Override
    public void bindXEIWidget(Widget widget, Content content, IngredientIO ingredientIO) {
        if (widget instanceof WidgetGroup group) {
            for (TextTextureWidget text : group.getWidgetsByType(TextTextureWidget.class)) {
                text.setText(of(content.content) + " S");
            }
        }
    }

    @Override
    public void createContentConfigurator(ConfiguratorGroup group,
                                          Supplier<Integer> supplier, Consumer<Integer> consumer) {
        NumberConfigurator amount = new NumberConfigurator(
                "recipe.capability.ars_source.amount",
                () -> supplier.get(),
                number -> consumer.accept(number == null ? 0 : number.intValue()),
                createDefaultContent(), false);
        amount.setRange(1, Integer.MAX_VALUE);
        group.addConfigurators(amount);
    }

    @Override
    public Component getLeftErrorInfo(List<Integer> contents) {
        int sum = contents.stream().mapToInt(Integer::intValue).sum();
        return Component.translatable("recipe.capability.ars_source.error", sum);
    }
}
