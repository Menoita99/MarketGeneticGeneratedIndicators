package pt.fcul.masters.examples;

public interface Validator<T,C extends Comparable<? super C>> {

	C validate(T args);
}
