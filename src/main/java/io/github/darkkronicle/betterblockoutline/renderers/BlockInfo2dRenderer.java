package io.github.darkkronicle.betterblockoutline.renderers;

import io.github.darkkronicle.betterblockoutline.blockinfo.AbstractBlockInfo;
import io.github.darkkronicle.betterblockoutline.blockinfo.info2d.AbstractBlockInfo2d;
import io.github.darkkronicle.betterblockoutline.blockinfo.info2d.NoteblockInfo;
import io.github.darkkronicle.betterblockoutline.blockinfo.info2d.RedstoneInfo;
import io.github.darkkronicle.betterblockoutline.blockinfo.info2d.SignTextInfo;
import io.github.darkkronicle.betterblockoutline.config.ConfigStorage;
import io.github.darkkronicle.betterblockoutline.connectedblocks.AbstractConnectedBlock;
import io.github.darkkronicle.betterblockoutline.interfaces.IOverlayRenderer;
import io.github.darkkronicle.darkkore.config.options.BooleanOption;
import io.github.darkkronicle.darkkore.hotkeys.HotkeySettings;
import io.github.darkkronicle.darkkore.hotkeys.HotkeySettingsOption;
import lombok.Getter;
import net.minecraft.block.InfestedBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.darkkronicle.betterblockoutline.blockinfo.info2d.AbstractBlockInfo2d.constructSimple;

/**
 * Renderer for {@link AbstractBlockInfo2d}
 */
public class BlockInfo2dRenderer implements IOverlayRenderer {

    private final static BlockInfo2dRenderer INSTANCE = new BlockInfo2dRenderer();

    private final MinecraftClient client;

    @Getter
    private final List<AbstractBlockInfo2d> renderers = new ArrayList<>();

    public static BlockInfo2dRenderer getInstance() {
        return INSTANCE;
    }

    public void add(AbstractBlockInfo2d info) {
        renderers.add(info);
    }

    public void setup() {
        // Dedicated classes
        add(new RedstoneInfo());
        add(new NoteblockInfo());
        add(new SignTextInfo());

        // Simple ones
        add(constructSimple(AbstractBlockInfo.Order.ALL, "coordinatestext", (block) -> true, (block) -> {
            BlockPos pos = block.getBlock().getPos();
            return "X: " + pos.getX() + "\nY: " + pos.getY() + "\nZ: " + pos.getZ();
        }));
        add(constructSimple(AbstractBlockInfo.Order.BLOCK, "facingtext", "Facing: %s", Properties.FACING, Properties.HOPPER_FACING, Properties.HORIZONTAL_FACING));
        add(constructSimple(AbstractBlockInfo.Order.BLOCK, "blocklevel", "Level: %s", Properties.LEVEL_3, Properties.LEVEL_8));
        add(constructSimple(AbstractBlockInfo.Order.BLOCK, "waterloggedtext", "Waterlogged: %s", Properties.WATERLOGGED));
        add(constructSimple(AbstractBlockInfo.Order.BLOCK, "opentext", "Open: %s", Properties.OPEN));
        add(constructSimple(AbstractBlockInfo.Order.BLOCK, "persistent", "Persistent: %s", Properties.PERSISTENT));
        add(constructSimple(AbstractBlockInfo.Order.BLOCK, "distancetext", "Distance: %s", Properties.DISTANCE_0_7, Properties.DISTANCE_1_7));
        add(constructSimple(AbstractBlockInfo.Order.BLOCK, "layerstext", "Layers: %s", Properties.LAYERS));
        add(constructSimple(AbstractBlockInfo.Order.BLOCK, "dripleaftilttext", "Tilt: %s", Properties.TILT));
        add(constructSimple(AbstractBlockInfo.Order.BLOCK, "chargestext", "Charges: %s", Properties.CHARGES));
        add(constructSimple(AbstractBlockInfo.Order.BLOCK, "bitestext", "Bites: %s", Properties.BITES));
        add(constructSimple(AbstractBlockInfo.Order.BLOCK, "agetext", "Age: %s", Properties.AGE_1, Properties.AGE_2, Properties.AGE_3, Properties.AGE_5, Properties.AGE_7, Properties.AGE_15, Properties.AGE_25));
        add(constructSimple(AbstractBlockInfo.Order.BLOCK, "beetext", "Honey Level: %s", Properties.HONEY_LEVEL));
        add(constructSimple(AbstractBlockInfo.Order.BLOCK, "leveltext", "Level: %s", Properties.LEVEL_15));
        add(constructSimple(AbstractBlockInfo.Order.SPECIFIC, "infested", (block) -> block.getBlock().getState().getBlock() instanceof InfestedBlock, (block) -> "Infested"));

        // Setup order so that generic ones get rendered last
        Collections.sort(renderers);
    }

    public List<HotkeySettings> getHotkeys() {
        List<HotkeySettings> keys = new ArrayList<>();
        for (AbstractBlockInfo info : getRenderers()) {
            keys.add(info.getActiveKey().getValue());
        }
        return keys;
    }

    public List<HotkeySettingsOption> getHotkeyConfigs() {
        List<HotkeySettingsOption> keys = new ArrayList<>();
        for (AbstractBlockInfo info : getRenderers()) {
            keys.add(info.getActiveKey());
        }
        return keys;
    }

    public List<BooleanOption> getActiveConfigs() {
        List<BooleanOption> active = new ArrayList<>();
        for (AbstractBlockInfo info : getRenderers()) {
            active.add(info.getActive());
        }
        return active;
    }

    private BlockInfo2dRenderer() {
        this.client = MinecraftClient.getInstance();
    }

    @Override
    public boolean render(MatrixStack matrices, Vector3d camera, Entity entity, AbstractConnectedBlock block) {
        if (!ConfigStorage.getBlockInfo2d().getActive().getValue()) {
            return false;
        }
        renderTextInfo(renderers, client, matrices, block);
        return false;
    }

    public static void renderTextInfo(List<AbstractBlockInfo2d> texts, MinecraftClient client, MatrixStack matrices, AbstractConnectedBlock block) {
        List<String> lines = new ArrayList<>();
        for (AbstractBlockInfo2d text : texts) {
            if (text.isActive() && text.shouldRender(block)) {
                text.getLines(block).ifPresent(lines::addAll);
            }
        }
        if (lines.size() == 0) {
            return;
        }
        BlockPos pos = block.getBlock().getPos();
        Vector3d vec = new Vector3d(pos.getX(), pos.getY(), pos.getZ());

        vec.add(new Vector3d(0.5, 0.5, 0.5));
        AbstractBlockInfo2d.drawStringLines(matrices, client, lines, vec);
    }
}
