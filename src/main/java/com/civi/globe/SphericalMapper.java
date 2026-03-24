package com.civi.globe;

public final class SphericalMapper {
    private static final Vec2 TRI_A = new Vec2(-0.5, 0.0);
    private static final Vec2 TRI_B = new Vec2(0.5, 0.0);
    private static final Vec2 TRI_C = new Vec2(0.0, Math.sqrt(3.0) / 2.0);

    public double[] barycentric(Vec2 p) {
        double l3 = p.y * 2.0 / Math.sqrt(3.0);
        double l2 = p.x + (0.5 * (1.0 - l3));
        double l1 = 1.0 - l2 - l3;
        return new double[]{l1, l2, l3};
    }

    public Vec3 slerp(Vec3 p0, Vec3 p1, double t) {
        Vec3 a = p0.normalize();
        Vec3 b = p1.normalize();
        double dot = Math.max(-1.0, Math.min(1.0, a.dot(b)));
        if (1.0 - Math.abs(dot) < 1.0e-8) {
            return a.scale(1.0 - t).add(b.scale(t)).normalize().scale(p0.length());
        }
        double angle = Math.acos(dot);
        double sinAngle = Math.sin(angle);
        double w0 = Math.sin((1.0 - t) * angle) / sinAngle;
        double w1 = Math.sin(t * angle) / sinAngle;
        return a.scale(w0).add(b.scale(w1)).normalize().scale(p0.length());
    }

    public Vec3 mapPoint(Vec2 p, Vec3 s1, Vec3 s2, Vec3 s3, double radius) {
        double[] bary = barycentric(p);
        double l1 = bary[0];
        double l2 = bary[1];
        double l3 = bary[2];
        if (Math.abs(l3 - 1.0) < 1.0e-10) {
            return s3.withLength(radius);
        }
        double denominator = l1 + l2;
        double t12 = denominator < 1.0e-10 ? 0.0 : l2 / denominator;
        Vec3 p12 = slerp(s1, s2, t12);
        return slerp(p12, s3, l3).withLength(radius);
    }

    public Vec2 triA() {
        return TRI_A;
    }

    public Vec2 triB() {
        return TRI_B;
    }

    public Vec2 triC() {
        return TRI_C;
    }
}
