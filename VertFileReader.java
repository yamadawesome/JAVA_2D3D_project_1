import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class VertFileReader {
    public static class CurveComponent {
        public List<double[]> vertices = new ArrayList<>();
    }

    public static List<CurveComponent> loadVertFile(String filePath) throws IOException {
        List<CurveComponent> components = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        try {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("File is empty or invalid format: " + filePath);
            }

            int numberOfComponents = Integer.parseInt(line.trim());
            for (int i = 0; i < numberOfComponents; i++) {
                line = reader.readLine();
                if (line == null) {
                    throw new IOException("Unexpected end of file while reading number of vertices");
                }

                int numberOfVertices = Integer.parseInt(line.trim());
                CurveComponent component = new CurveComponent();
                for (int j = 0; j < numberOfVertices; j++) {
                    line = reader.readLine();
                    if (line == null) {
                        throw new IOException("Unexpected end of file while reading vertex data");
                    }

                    String[] parts = line.trim().split("\\s+");
                    if (parts.length != 2) {
                        throw new IOException("Invalid vertex data: " + line);
                    }

                    double x = Double.parseDouble(parts[0]);
                    double y = Double.parseDouble(parts[1]);
                    component.vertices.add(new double[]{x, y});
                }
                components.add(component);
            }
        } finally {
            reader.close();
        }
        return components;
    }
}
