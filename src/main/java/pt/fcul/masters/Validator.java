package pt.fcul.masters;

public interface Validator<T,C extends Comparable<? super C>> {

	C validate(T args);
}
