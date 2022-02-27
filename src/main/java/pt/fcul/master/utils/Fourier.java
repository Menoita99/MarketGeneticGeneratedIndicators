package pt.fcul.master.utils;

import static java.lang.Math.*;

import lombok.AllArgsConstructor;
import lombok.Data;


public class Fourier {
	
	private Fourier() {}
	
	/**
	 * Discrete Fourier Transform
	 * https://en.wikipedia.org/wiki/Discrete_Fourier_transform
	 */
	public static Complex[] dft(double[] points) {
		final double N = points.length;
		
		Complex[] signal = new Complex[(int)N];
		
		for (int k = 0; k < N; k++) {
			double re = 0;
			double im = 0;
			for (int n = 0; n < N; n++) {
				double phi = (2 * PI * n * k) / N;
				re += (double) cos(phi) * points[n];
				im -= (double) sin(phi) * points[n]  ;
			}
			signal[k] = new Complex(re/N, im/N,k);
		}
		
		return signal;
	}

	@AllArgsConstructor
	@Data
	public static class Complex{
		protected double real;
		protected double imaginary; 
		
		protected double amplitude;
		protected double frequency;
		protected double phase;
		
		public Complex(double real, double imaginary, double frequency) {
			this.real = real;
			this.imaginary = imaginary;
			this.frequency = frequency;
			this.amplitude = sqrt(real * real + imaginary * imaginary); // Pythagoras theorem
			this.phase = atan2(imaginary, real);
		}
		
		public Pair<Double, Double> pointAtTime(double time, double angle){
			double x = amplitude * cos(phase + angle + frequency * time);
			double y = amplitude * sin(phase + angle + frequency * time);
			return new Pair<>(x, y);
		}
		
		public Pair<Double, Double> pointAtTime(double time){
			double x = amplitude * cos(phase + frequency * time);
			double y = amplitude * sin(phase + frequency * time);
			return new Pair<>(x, y);
		}
	}
}
