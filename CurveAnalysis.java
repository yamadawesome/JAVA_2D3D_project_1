import java.util.ArrayList;
import java.util.List;

public class CurveAnalysis {
    public static List<double[]> computeTangents(List<double[]> vertices) {
        List<double[]> tangents = new ArrayList<>();
        int n = vertices.size();

        for (int i = 0; i < n; i++) {
            double[] current = vertices.get(i);
            double[] next = vertices.get((i + 1) % n);
            double dx = next[0] - current[0];
            double dy = next[1] - current[1];
            double length = Math.sqrt(dx * dx + dy * dy);

            tangents.add(new double[]{dx / length, dy / length});
        }
        return tangents;
    }

    public static List<double[]> computeNormals(List<double[]> tangents) {
        List<double[]> normals = new ArrayList<>();
        for (double[] tangent : tangents) {
            normals.add(new double[]{-tangent[1], tangent[0]});
        }
        return normals;
    }

    public static List<Double> computeCurvatures(List<double[]> tangents) {
        List<Double> curvatures = new ArrayList<>();
        int n = tangents.size();

        for (int i = 0; i < n; i++) {
            double[] current = tangents.get(i);
            double[] next = tangents.get((i + 1) % n);

            double dx = next[0] - current[0];
            double dy = next[1] - current[1];
            double curvature = Math.sqrt(dx * dx + dy * dy);

            curvatures.add(curvature);
        }
        return curvatures;
    }
}
