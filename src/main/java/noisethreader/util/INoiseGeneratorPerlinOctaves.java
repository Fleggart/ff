package noisethreader.util;

public interface INoiseGeneratorPerlinOctaves {
	
	double[] noisethreader$generateNoiseOctavesOTG(double[] doubleArray, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale);
	
	double[] noisethreader$generateNoiseOctavesThreaded(double[] doubleArray, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale, int octaveStart, int octaveEnd);
}