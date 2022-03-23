package pt.fcul.masters.utils;

import java.util.ArrayList;
import java.util.List;

public class Slicer {

	/**
	 * 
	 * @param range
	 * @param slices
	 * @return
	 */
	public static List<Pair<Integer, Integer>> slice(Pair<Integer, Integer> range, int slices) {
		List<Pair<Integer, Integer>> pairSlices = new ArrayList<>();
		int length = (range.value() - range.key()) / slices;
		for (int i = 0; i < slices; i++) {
			int start = i * length + range.key();
			int end = i + 1 >= slices ? range.value() : (i + 1) * length + range.key();
			pairSlices.add(new Pair<>(start, end));
		}
		return pairSlices;
	}

	/**
	 * 
	 * @param range
	 * @param divideInSlices
	 * @param slice
	 * @return
	 */
	public static Pair<Integer, Integer> getSlice(Pair<Integer, Integer> range, int divideInSlices, int slice) {
		if (slice < 0 || divideInSlices < 1 || slice >= divideInSlices)
			throw new IllegalArgumentException(
					"Slice can't be negative or bigger then number of slices and divideInslices can be less then 1 "
							+ slice + "/" + divideInSlices);

		int length = (range.value() - range.key()) / divideInSlices;
		int start = slice * length + range.key();
		int end = slice + 1 >= divideInSlices ? range.value() : (slice + 1) * length + range.key();
		
		return new Pair<>(start, end);
	}

	public static void main(String[] args) {
		Pair<Integer, Integer> pair = new Pair<>(110, 200);
		slice(pair, 20).forEach(System.out::println);
		System.out.println("-----------------------");
		System.out.println(getSlice(pair, 20, 14));
	}
}
