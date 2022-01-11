package pt.fcul.masters.problems;

public interface Validator<T,C extends Comparable<? super C>> {

	C validate(T args);
}
