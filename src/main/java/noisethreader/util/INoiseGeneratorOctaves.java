package noisethreader.util;

public interface INoiseGeneratorOctaves {
	
	double[] noisethreader$generateNoiseOctavesVanilla(double[] noiseArray, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale);
	
	double[] noisethreader$generateNoiseOctavesThreaded(double[] noiseArray, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale, int octaveStart, int octaveEnd);
}