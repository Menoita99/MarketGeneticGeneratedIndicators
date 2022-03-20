package pt.fcul.master.utils;

import java.util.ArrayList;
import java.util.List;

public class Slicer {

	public static List<Pair<Integer,Integer>> slice(Pair<Integer,Integer> range,int slices) {
		List<Pair<Integer,Integer>> pairSlices = new ArrayList<>();
		int length = (range.value() - range.key()) / slices;
		for(int i = 0; i < slices ; i++) {
			int start = i * length + range.key();
			int end = i+1 >= slices ? range.value() : (i+1) * length + range.key();
			pairSlices.add(new Pair<>(start	, end));
		}
		return pairSlices;
	}
	
	
	public static void main(String[] args) {
		Pair<Integer, Integer> pair = new Pair<>(110,200);
		slice(pair, 20).forEach(System.out::println);
	}
}
