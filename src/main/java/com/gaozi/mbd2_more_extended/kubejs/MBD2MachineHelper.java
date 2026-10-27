package com.gaozi.mbd2_more_extended.kubejs;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * MBD2 机器事件的通用朝向/坐标辅助方法，通过 KubeJS 绑定为 {@code MBD2Helper}。
 *
 * <p>只处理水平朝向的 front/back/left/right；垂直朝向返回空结果，
 * 避免 up/down 朝向下 left/right 的参考轴歧义。</p>
 */
public final class MBD2MachineHelper {

    /**
     * 返回机器相对方位到绝对方向的映射：
     * {@code front/back/left/right/up/down -> north/south/...}。
     * 机器没有水平朝向时返回空 Map。
     */
    public Map<String, String> getRelativeDirections(MBDMachine machine) {
        Direction front = machine == null ? null : machine.getFrontFacing().orElse(null);
        return getRelativeDirections(front);
    }

    /** 直接按朝向解析相对方位，便于测试和非机器场景复用。 */
    public Map<String, String> getRelativeDirections(Direction front) {
        if (front == null || !front.getAxis().isHorizontal()) {
            return Collections.emptyMap();
        }

        Map<String, String> directions = new LinkedHashMap<>();
        directions.put("front", front.getSerializedName());
        directions.put("back", front.getOpposite().getSerializedName());
        directions.put("left", front.getCounterClockWise().getSerializedName());
        directions.put("right", front.getClockWise().getSerializedName());
        directions.put("up", Direction.UP.getSerializedName());
        directions.put("down", Direction.DOWN.getSerializedName());
        return directions;
    }

    /** 返回机器某个相对方位对应的绝对方向名；不支持时返回 null。 */
    public String getRelativeDirection(MBDMachine machine, String relative) {
        Direction front = machine == null ? null : machine.getFrontFacing().orElse(null);
        return getRelativeDirection(front, relative);
    }

    /** 直接按朝向解析相对方位名；不支持时返回 null。 */
    public String getRelativeDirection(Direction front, String relative) {
        if (relative == null) {
            return null;
        }
        return getRelativeDirections(front).get(relative.toLowerCase(Locale.ROOT));
    }

    /**
     * 按机器相对方位偏移坐标，例如 {@code back, 2}。
     * 机器没有水平朝向或相对方位无效时返回 null。
     */
    public BlockPos offsetRelative(BlockPos pos, MBDMachine machine, String relative, int amount) {
        Direction front = machine == null ? null : machine.getFrontFacing().orElse(null);
        return offsetRelative(pos, front, relative, amount);
    }

    /** 直接按朝向做相对偏移；朝向或相对方位无效时返回 null。 */
    public BlockPos offsetRelative(BlockPos pos, Direction front, String relative, int amount) {
        String absolute = getRelativeDirection(front, relative);
        return absolute == null ? null : offsetAbsolute(pos, absolute, amount);
    }

    /**
     * 按绝对方向偏移坐标，方向支持 {@link Direction} 或方向名字符串。
     * 方向无效时返回 null。
     */
    public BlockPos offsetAbsolute(BlockPos pos, Object direction, int amount) {
        Direction dir = toDirection(direction);
        if (pos == null || dir == null) {
            return null;
        }
        return pos.offset(
                dir.getStepX() * amount,
                dir.getStepY() * amount,
                dir.getStepZ() * amount
        );
    }

    private static Direction toDirection(Object direction) {
        if (direction instanceof Direction dir) {
            return dir;
        }
        if (direction == null) {
            return null;
        }
        return Direction.byName(direction.toString().toLowerCase(Locale.ROOT));
    }
}
