package com.github.bckfnn.reactstreams;

public class Tuple<I1, I2> {
	private I1 value1;
	private I2 value2;
	
	public Tuple(I1 value1, I2 value2) {
		this.value1 = value1;
		this.value2 = value2;
	}
	
	public I1 getValue1() {
		return value1;
	}

	public I2 getValue2() {
		return value2;
	}

	public I1 left() {
		return getValue1();
	}

	public I2 right() {
		return getValue2();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value1 == null) ? 0 : value1.hashCode());
		result = prime * result + ((value2 == null) ? 0 : value2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tuple<?, ?> other = (Tuple<?, ?>) obj;
		if (value1 == null) {
			if (other.value1 != null)
				return false;
		} else if (!value1.equals(other.value1))
			return false;
		if (value2 == null) {
			if (other.value2 != null)
				return false;
		} else if (!value2.equals(other.value2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Tuple [value1=" + value1 + ", value2=" + value2 + "]";
	}
}