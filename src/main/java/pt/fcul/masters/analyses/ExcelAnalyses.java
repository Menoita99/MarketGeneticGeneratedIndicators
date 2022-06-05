package pt.fcul.masters.analyses;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.plotter.gui.Plotter;

public class ExcelAnalyses {


	private final static List<String> AGENTS_GP_1_SLICE_PATH = List.of(
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\05-06-2022 17_06_02\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 18_37_31\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 18_50_22\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 20_24_24\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 21_29_31\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 22_51_31\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 00_58_58\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 19_37_44\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 21_07_47\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 23_21_17\\fitnessData.csv"
			);

	private final static List<String> AGENTS_GP_3_SLICE_PATH = List.of(
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 12_41_10\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 13_07_26\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 13_45_51\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 14_21_39\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 14_48_16\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 15_27_18\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 15_46_44\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 16_34_27\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 16_52_58\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\SBUX_3slices\\22-05-2022 17_23_14\\fitnessData.csv"
			);


	private final static List<String> AGENTS_VGP_1_SLICE_PATH = List.of(
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\05-06-2022 17_06_02\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 18_37_31\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 18_50_22\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 20_24_24\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 21_29_31\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\27-05-2022 22_51_31\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 00_58_58\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 19_37_44\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 21_07_47\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingGP\\28-05-2022 23_21_17\\fitnessData.csv"
			);

	private final static List<String> AGENTS_VGP_3_SLICE_PATH = List.of(
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\22-05-2022 20_02_28\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\23-05-2022 12_43_05\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\23-05-2022 14_57_50\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\23-05-2022 17_30_09\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\24-05-2022 15_29_07\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\24-05-2022 15_49_08\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\24-05-2022 16_50_18\\fitnessData.csv",
			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\24-05-2022 17_33_50\\fitnessData.csv"
			//			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\24-05-2022 17_33_50\\fitnessData.csv",
			//			"C:\\Users\\Owner\\Desktop\\GP_SAVES\\ProfitSeekingVGP\\SBUX_3slices\\22-05-2022 20_02_28\\fitnessData.csv"
			);
	
	public static void main(String[] args) throws IOException {
		Map<Integer,List<Double>> valuesBestFitness = new HashMap<>();
		Map<Integer,List<Double>> valuesValidationFitness = new HashMap<>();
		
		for (String path : AGENTS_VGP_3_SLICE_PATH) {
			List<String> lines = FileUtils.readLines(new File(path), StandardCharsets.UTF_8);
			for (int i = 1; i < lines.size(); i++) { 
				valuesBestFitness.computeIfAbsent(i, key-> new ArrayList<>());
				valuesBestFitness.get(i).add(Double.parseDouble(lines.get(i).split(",")[1]));
				valuesValidationFitness.computeIfAbsent(i, key-> new ArrayList<>());
				valuesValidationFitness.get(i).add(Double.parseDouble(lines.get(i).split(",")[2]));
			}	
		}
		
		List<Double> mediana = new LinkedList<>();
		valuesBestFitness.forEach((k,v)->mediana.add(calculateMediana(v)));
		Plotter.builder().lineChart(mediana, "Mediana Best Fitness").build().plot();
		
		List<Double> medianavalidation = new LinkedList<>();
		valuesValidationFitness.forEach((k,v)->medianavalidation.add(calculateMediana(v)));
		Plotter.builder().lineChart(medianavalidation, "Mediana Validatio Fitness").build().plot();
		
		List<Double> media = new LinkedList<>();
		valuesBestFitness.forEach((k,v)->media.add(mean(v)));
		Plotter.builder().lineChart(media, "Media Best Fitness").build().plot();
		
		List<Double> mediavalidation = new LinkedList<>();
		valuesValidationFitness.forEach((k,v)->mediavalidation.add(mean(v)));
		Plotter.builder().lineChart(mediavalidation, "Media Validatio Fitness").build().plot();
	}

	public static Double calculateMediana(List<Double> v) {
		v.sort(Double::compare);
		if(v.size()%2== 0) {
			return (v.get((int)(v.size()/2)) + v.get((int)(v.size()/2 - 1))) / 2;
		}else
			return v.get((int)(v.size()/2));
	}
	

	public static Double mean(List<Double> v) {
		return v.stream().mapToDouble(d->d).average().getAsDouble();
	}
}
