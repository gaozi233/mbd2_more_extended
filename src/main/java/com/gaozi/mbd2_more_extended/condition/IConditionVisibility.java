package com.gaozi.mbd2_more_extended.condition;

/**
 * 由 {@code RecipeConditionMixin} 注入到 MBD2 所有配方条件上的可见性接口。
 */
public interface IConditionVisibility {

    boolean mbd2_more_extended$isShowInGui();

    void mbd2_more_extended$setShowInGui(boolean show);
}
