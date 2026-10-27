package com.gaozi.mbd2_more_extended.kubejs;

import com.lowdragmc.mbd2.api.blockentity.IMachineBlockEntity;
import com.lowdragmc.mbd2.api.machine.IMachine;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * MBD2 机器事件的通用朝向/坐标辅助方法，通过 KubeJS 绑定为 {@code MBD2Helper}。
 *
 * <p>只处理水平朝向的 front/back/left/right；垂直朝向返回空结果，
 * 避免 up/down 朝向下 left/right 的参考轴歧义。</p>
 */
public final class MBD2MachineHelper {

    /**
     * 取指定坐标上的 MBD2 机器；不是机器方块时返回 null。
     */
    public MBDMachine getMachineAt(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof IMachineBlockEntity machineBlockEntity)) {
            return null;
        }
        IMachine machine = machineBlockEntity.getMetaMachine();
        return machine instanceof MBDMachine mbdMachine ? mbdMachine : null;
    }

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

    /**
     * 读取机器 customData 的单个键。
     * 数值/字符串/布尔返回普通 Java 值；复合/列表返回对应 NBT Tag。
     * 键不存在时返回 defaultValue。
     */
    public Object getCustomData(MBDMachine machine, String key, Object defaultValue) {
        if (machine == null || key == null) {
            return defaultValue;
        }
        CompoundTag data = machine.getCustomData();
        if (data == null || !data.contains(key)) {
            return defaultValue;
        }
        return toJavaValue(data.get(key));
    }

    /**
     * 写入机器 customData 的单个键。
     * value 支持 Number / Boolean / String / Tag / Map / Iterable；
     * 传 null 时删除该键。
     */
    public void setCustomData(MBDMachine machine, String key, Object value) {
        if (machine == null || key == null) {
            return;
        }
        CompoundTag data = machine.getCustomData();
        if (data == null) {
            data = new CompoundTag();
        }
        if (value == null) {
            data.remove(key);
        } else {
            data.put(key, toTag(value));
        }
        machine.setCustomData(data);
    }

    /**
     * 仅在值变化时写入，返回是否发生了修改。
     * 适合 Tick 事件里做 customData 缓存。
     */
    public boolean setCustomDataIfChanged(MBDMachine machine, String key, Object value) {
        if (machine == null || key == null) {
            return false;
        }
        CompoundTag data = machine.getCustomData();
        if (data == null) {
            data = new CompoundTag();
            machine.setCustomData(data);
        }

        if (value == null) {
            if (!data.contains(key)) {
                return false;
            }
            data.remove(key);
            machine.setCustomData(data);
            return true;
        }

        Tag newTag = toTag(value);
        if (Objects.equals(data.get(key), newTag)) {
            return false;
        }
        data.put(key, newTag);
        machine.setCustomData(data);
        return true;
    }

    private static Tag toTag(Object value) {
        if (value instanceof Tag tag) {
            return tag;
        }
        if (value instanceof Boolean bool) {
            return ByteTag.valueOf((byte) (bool ? 1 : 0));
        }
        if (value instanceof Byte number) {
            return ByteTag.valueOf(number);
        }
        if (value instanceof Short number) {
            return ShortTag.valueOf(number);
        }
        if (value instanceof Integer number) {
            return IntTag.valueOf(number);
        }
        if (value instanceof Long number) {
            return LongTag.valueOf(number);
        }
        if (value instanceof Float number) {
            return FloatTag.valueOf(number);
        }
        if (value instanceof Double number) {
            return DoubleTag.valueOf(number);
        }
        if (value instanceof String text) {
            return StringTag.valueOf(text);
        }
        if (value instanceof Map<?, ?> map) {
            CompoundTag compound = new CompoundTag();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    compound.put(entry.getKey().toString(), toTag(entry.getValue()));
                }
            }
            return compound;
        }
        if (value instanceof Iterable<?> iterable) {
            ListTag list = new ListTag();
            for (Object element : iterable) {
                list.add(toTag(element));
            }
            return list;
        }
        return StringTag.valueOf(String.valueOf(value));
    }

    private static Object toJavaValue(Tag tag) {
        if (tag == null) {
            return null;
        }
        if (tag instanceof ByteTag byteTag) {
            return byteTag.getAsByte() != 0;
        }
        if (tag instanceof ShortTag shortTag) {
            return shortTag.getAsShort();
        }
        if (tag instanceof IntTag intTag) {
            return intTag.getAsInt();
        }
        if (tag instanceof LongTag longTag) {
            return longTag.getAsLong();
        }
        if (tag instanceof FloatTag floatTag) {
            return floatTag.getAsFloat();
        }
        if (tag instanceof DoubleTag doubleTag) {
            return doubleTag.getAsDouble();
        }
        if (tag instanceof StringTag stringTag) {
            return stringTag.getAsString();
        }
        return tag;
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
