package noisethreader.mixin.otg;

import com.pg85.otg.generator.noise.NoiseGeneratorPerlin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NoiseGeneratorPerlin.class)
public interface INoiseGeneratorPerlinMixin {
	
	@Invoker(value = "populateNoiseArray3D", remap = false)
	void invokePopulateNoiseArray3D(double[] NoiseArray, double xOffset, double yOffset, double zOffset, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale, double noiseScale);
}