package com.ticxo.camera.camerautils.utils;

import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class Quaternion {

	private double w;
	private final Vector vec;

	public Quaternion(double w, Vector vec) {

		this.w = w;
		this.vec = vec;

	}

	public static Quaternion toQuaternion(EulerAngle e) {

		double c1 = Math.cos(e.getX() * 0.5);
		double c2 = Math.cos(e.getY() * -0.5);
		double c3 = Math.cos(e.getZ() * 0.5);

		double s1 = Math.sin(e.getX() * 0.5);
		double s2 = Math.sin(e.getY() * -0.5);
		double s3 = Math.sin(e.getZ() * 0.5);

		Vector vec = new Vector(s1 * c2 * c3 + c1 * s2 * s3, c1 * s2 * c3 - s1 * c2 * s3, c1 * c2 * s3 + s1 * s2 * c3);
		double w = c1 * c2 * c3 - s1 * s2 * s3;

		return new Quaternion(w, vec);

	}

	public static EulerAngle toEuler(Quaternion q) {

		double x = q.getVec().getX(), y = q.getVec().getY(), z = q.getVec().getZ(), w = q.getW();
		double x2 = x + x, y2 = y + y, z2 = z + z;
		double xx = x * x2, xy = x * y2, xz = x * z2;
		double yy = y * y2, yz = y * z2, zz = z * z2;
		double wx = w * x2, wy = w * y2, wz = w * z2;

		double ex, ey, ez,
				m11 = 1 - (yy + zz),
				m12 = xy - wz,
				m13 = xz + wy,
				m22 = 1 - (xx + zz),
				m23 = yz - wx,
				m32 = yz + wx,
				m33 = 1 - (xx + yy);

		ey = Math.asin(clamp(m13, -1, 1));
		if (Math.abs(m13) < 0.99999) {
			ex = Math.atan2(-m23, m33);
			ez = Math.atan2(-m12, m11);
		} else {
			ex = Math.atan2(m32, m22);
			ez = 0;
		}

		return new EulerAngle(ex, -ey, ez);

	}

	public static Quaternion multiply(Quaternion a, Quaternion b) {

		double qax = a.getVec().getX(), qay = a.getVec().getY(), qaz = a.getVec().getZ(), qaw = a.getW();
		double qbx = b.getVec().getX(), qby = b.getVec().getY(), qbz = b.getVec().getZ(), qbw = b.getW();

		Vector vec = new Vector(
				qax * qbw + qaw * qbx + qay * qbz - qaz * qby,
				qay * qbw + qaw * qby + qaz * qbx - qax * qbz,
				qaz * qbw + qaw * qbz + qax * qby - qay * qbx);
		double w = qaw * qbw - qax * qbx - qay * qby - qaz * qbz;

		return new Quaternion(w, vec);

	}

	public static Quaternion multiply(Quaternion a, double b) {
		return new Quaternion(a.getW() * b, a.getVec().multiply(b));
	}

	public static double dot(Quaternion a, Quaternion b) {
		return a.getW()*b.getW() + a.getVec().dot(b.getVec());
	}

	public static Quaternion add(Quaternion a, Quaternion b) {
		return new Quaternion(a.getW() + b.getW(), a.getVec().add(b.getVec()));
	}

	public static Quaternion subtract(Quaternion a, Quaternion b) {
		return new Quaternion(a.getW() - b.getW(), a.getVec().subtract(b.getVec()));
	}

	// Global Rotation: previous, change
	public static EulerAngle combine(EulerAngle origin, EulerAngle delta) {
		return toEuler(multiply(toQuaternion(origin), toQuaternion(delta)));
	}

	public static EulerAngle lerp(EulerAngle a, EulerAngle b, double t) {
		return new EulerAngle(
				(b.getX() - a.getX()) * t + a.getX(),
				(b.getY() - a.getY()) * t + a.getY(),
				(b.getZ() - a.getZ()) * t + a.getZ()
		);
	}

	public static EulerAngle splineLerp(EulerAngle a, EulerAngle b, EulerAngle c, EulerAngle d, double ratio) {
		double t0 = 0, t1 = 1, t2 = 2, t3 = 3;
		double t = (t2 - t1) * ratio + t1;
		EulerAngle a1 = add(multiply(a, (t1 - t) / (t1 - t0)), multiply(b, (t - t0) / (t1 - t0)));
		EulerAngle a2 = add(multiply(b, (t2 - t) / (t2 - t1)), multiply(c, (t - t1) / (t2 - t1)));
		EulerAngle a3 = add(multiply(c, (t3 - t) / (t3 - t2)), multiply(d, (t - t2) / (t3 - t2)));

		EulerAngle b1 = add(multiply(a1, (t2 - t) / (t2 - t0)), multiply(a2, (t - t0) / (t2 - t0)));
		EulerAngle b2 = add(multiply(a2, (t3 - t) / (t3 - t1)), multiply(a3, (t - t1) / (t3 - t1)));

		return add(multiply(b1, (t2 - t) / (t2 - t1)), multiply(b2, (t - t1) / (t2 - t1)));
	}

	public static EulerAngle slerp(EulerAngle a, EulerAngle b, double t) {

		Quaternion qA = toQuaternion(a);
		Quaternion qB = toQuaternion(b);
		double dot = dot(qA, qB);
		if(dot < 0) {
			qB.multiply(-1);
			dot = -dot;
		}

		// Use Linear Interpolation
		if(dot > 0.9995) {
			Quaternion result = subtract(qB, qA);
			result = multiply(result, t);
			result = add(qA, result);

			return toEuler(result);
		}else {
			double theta_0 = Math.acos(dot);
			double theta = theta_0 * t;
			double sin_theta = Math.sin(theta);
			double sin_theta_0 = Math.sin(theta_0);

			double sA = Math.cos(theta) - dot * sin_theta / sin_theta_0;
			double sB = sin_theta / sin_theta_0;

			Quaternion rQA = multiply(qA, sA);
			Quaternion rQB = multiply(qB, sB);

			return toEuler(add(rQA, rQB));
		}
	}

	public void multiply(double b) {
		w *= b;
		vec.multiply(b);
	}

	public void normalize() {
		double norm = Math.sqrt(w*w + vec.getX()*vec.getX() + vec.getY()*vec.getY() + vec.getZ()*vec.getZ());
		w /= norm;
		vec.multiply(1.0/norm);
	}

	public double getW() {
		return w;
	}

	public Vector getVec() {
		return vec;
	}

	public String toFormula() {
		return "[" + w + "+" + vec.getX() + "i+" + vec.getY() + "j+" + vec.getZ() + "k" + "]";
	}

	private static double clamp(double value, double min, double max) {
		return Math.min(Math.max(value, min), max);
	}

	private static EulerAngle add(EulerAngle a, EulerAngle b) {
		return new EulerAngle(a.getX() + b.getX(), a.getY() + b.getY(), a.getZ() + b.getZ());
	}

	private static EulerAngle multiply(EulerAngle a, double m) {
		return new EulerAngle(a.getX() * m, a.getY() * m, a.getZ() * m);
	}

}