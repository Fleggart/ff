package noisethreader;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import zone.rong.mixinbooter.IEarlyMixinLoader;
import zone.rong.mixinbooter.ILateMixinLoader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
public class NoiseThreaderPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader, ILateMixinLoader {

    public NoiseThreaderPlugin() {
        MixinBootstrap.init();
    }

    // 早期 Mixin：不依赖其他模组，可以早期加载
    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();
        configs.add("mixins.noisethreader.vanilla.json");
        return configs;
    }

    // 晚期 Mixin：依赖其他模组，等模组加载完成后再加载
    @Override
    public List<String> getLateMixinConfigs() {
        List<String> configs = new ArrayList<>();

        if (Loader.isModLoaded("bettercaves")) {
            configs.add("mixins.noisethreader.vanilla.cache.json");
            configs.add("mixins.noisethreader.bettercaves.json");
        }

        if (Loader.isModLoaded("openterraingenerator")) {
            configs.add("mixins.noisethreader.otg.json");
            if (Loader.isModLoaded("bettercaves")) {
                configs.add("mixins.noisethreader.otg.cache.json");
            }
        }

        return configs;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    @Nullable
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
