package us.rockhopper.utility;

/**
 * A tuple of floats, for storing physics data.
 * 
 * @author Tim Clancy
 * @version 1.0.0
 * @date 9.29.2017
 */
public class Vector3<T> {

	// Vector data.
	public T x;
	public T y;
	public T z;

	/**
	 * Create a new Vector3.
	 * 
	 * @param x
	 *            the x-component.
	 * @param y
	 *            the y-component.
	 * @param z
	 *            the z-component.
	 */
	public Vector3(T x, T y, T z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public String toString() {
		return "[x: " + this.x + ", y: " + this.y + ", z: " + this.z + "]";
	}
}
