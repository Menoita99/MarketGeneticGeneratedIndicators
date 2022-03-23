package pt.fcul.masters.utils;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import pt.fcul.masters.gp.op.statefull.Ema;
import pt.fcul.masters.gp.op.statefull.Rsi;
import pt.fcul.masters.table.ComplexVectorTable;
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
	
	
	
	public static void addEma(VectorTable table, String column,int length, int vectorSize) {
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
		}, "ema"+length);
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
