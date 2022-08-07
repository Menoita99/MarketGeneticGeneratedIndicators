package pt.fcul.masters.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.math3.complex.Complex;

import pt.fcul.masters.data.normalizer.Normalizer;
import pt.fcul.masters.gp.op.statefull.Ema;
import pt.fcul.masters.gp.op.statefull.Rsi;
import pt.fcul.masters.stvgp.StvgpType;
import pt.fcul.masters.table.ComplexVectorTable;
import pt.fcul.masters.table.DoubleTable;
import pt.fcul.masters.table.StvgpTable;
import pt.fcul.masters.table.VectorTable;
import pt.fcul.masters.vgp.util.ComplexVector;
import pt.fcul.masters.vgp.util.Vector;

public class ColumnUtil {

	
	
	
	public static void addEma(ComplexVectorTable table, String column,int length, int vectorSize) {
		Ema ema = new Ema(length);
		List<Double> list = new ShiftList<>(vectorSize);
		table.createValueFrom((row, index) -> {
			ComplexVector cv = row.get(table.columnIndexOf(column));
			if(index == 0)
				for(Complex c : cv.getArr())
					list.add(ema.apply(new Double[] {c.getReal()}));
			else
				list.add(ema.apply(new Double[] {cv.last().getReal()}));
			
			return ComplexVector.of(list.toArray(new Double[vectorSize]));
		}, "ema"+length);
	}
	
	
	public static void normalizeColumn(ComplexVectorTable table, String column, int vectorSize,Normalizer normalizer) {
		List<Double> normalizedData = normalizer.apply(table.getColumn(column).stream().map(v -> v.last().getReal()).toList());
		ShiftList<Double> list = new ShiftList<>(vectorSize);
		List<ComplexVector> normalizedColumn = new LinkedList<>();
		
		list.fill(0D);
		for (Double norm : normalizedData) {
			list.add(norm);
			normalizedColumn.add(ComplexVector.of(list.toArray(new Double[vectorSize])));
		}
		
		table.addColumn(normalizedColumn, column+"norm");
	}
	
	
	public static void normalizeColumn(VectorTable table, String column, int vectorSize,Normalizer normalizer) {
		List<Double> normalizedData = normalizer.apply(table.getColumn(column).stream().map(v -> v.last()).toList());
		List<Double> list = new ShiftList<>(vectorSize);
		List<Vector> normalizedColumn = new LinkedList<>();
		
		for (Double norm : normalizedData) {
			list.add(norm);
			normalizedColumn.add(Vector.of(list.toArray(new Double[vectorSize])));
		}
		
		table.addColumn(normalizedColumn, column+"norm");
	}
	
	
	public static void normalizeColumn(DoubleTable table, String column, int vectorSize,Normalizer normalizer) {
		table.addColumn(normalizer.apply(table.getColumn(column)), column+"norm");
	}
	
	public static void addEma(VectorTable table, String column,int length, int vectorSize,String name) {
		Ema ema = new Ema(length);
		List<Double> list = new ShiftList<>(vectorSize);
		table.createValueFrom((row, index) -> {
			Vector cv = row.get(table.columnIndexOf(column));
			if(index == 0)
				for(double c : cv.getArr())
					list.add(ema.apply(new Double[] {c}));
			else
				list.add(ema.apply(new Double[] {cv.last()}));
			
			return Vector.of(list.toArray(new Double[vectorSize]));
		}, name);
	}
	


	public static void addEma(StvgpTable table, String column, int length, int vectorSize) {
		Ema ema = new Ema(length);
		List<Double> list = new ShiftList<>(vectorSize);
		table.createValueFrom((row, index) -> {
			StvgpType cv = row.get(table.columnIndexOf(column));
			if(index == 0)
				for(double c : cv.getAsVectorType().getArr())
					list.add(ema.apply(new Double[] {c}));
			else
				list.add(ema.apply(new Double[] {cv.getAsVectorType().last()}));
			
			return StvgpType.of(Vector.of(list.toArray(new Double[vectorSize])));
		}, "ema"+length);
		
	}
	
	
	
	public static void addEma(DoubleTable table, String column,int length) {
		Ema ema = new Ema(length);
		table.createValueFrom((row, index) -> ema.apply(new Double[]{row.get(table.columnIndexOf(column))}), "ema"+length);
	}
	
	
	
	public static void add(VectorTable table, int vectorSize,BiFunction<List<Vector>,Integer, Double> func, String columName) {
		ShiftList<Double> list = new ShiftList<>(vectorSize);
		table.createValueFrom((row, index) -> {
			list.add(func.apply(row, index));
			return list.isFull() ? Vector.of(list.toArray(new Double[vectorSize])) : Vector.of(0);
		},columName);
	}
	


	public static void add(StvgpTable table, int vectorSize, BiFunction<List<StvgpType>, Integer, Double> func,String columName) {
		ShiftList<Double> list = new ShiftList<>(vectorSize);
		table.createValueFrom((row, index) -> {
			list.add(func.apply(row, index));
			return list.isFull() ?  StvgpType.of(Vector.of(list.toArray(new Double[vectorSize]))) :  StvgpType.of(Vector.of(0));
		},columName);
		
	}
	
	
	public static void add(ComplexVectorTable table, int vectorSize,BiFunction<List<ComplexVector>,Integer, Double> func, String columName) {
		ShiftList<Double> list = new ShiftList<>(vectorSize);
		table.createValueFrom((row, index) -> {
			list.add(func.apply(row, index));
			return list.isFull() ? ComplexVector.of(list.toArray(new Double[vectorSize])) : ComplexVector.of(0);
		},columName);
	}
	
	
	public static void addRsi(ComplexVectorTable table, String column, int vectorSize) {
		Rsi rsi = new Rsi();
		List<Double> list = new ShiftList<>(vectorSize);
		table.createValueFrom((row, index) -> {
			ComplexVector cv = row.get(table.columnIndexOf(column));
			if(index == 0)
				for(Complex c : cv.getArr())
					list.add(rsi.apply(new Double[] {c.getReal()}));
			else
				list.add(rsi.apply(new Double[] {cv.last().getReal()}));
			
			return ComplexVector.of(list.toArray(new Double[vectorSize]));
		}, "rsi");
	}

}
