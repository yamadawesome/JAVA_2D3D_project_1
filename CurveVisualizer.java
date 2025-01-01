import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

public class CurveVisualizer extends JPanel {
    private final List<VertFileReader.CurveComponent> components;

    private double scale = 50.0; // 初期拡大率
    private int offsetX = 400;   // X方向の平行移動
    private int offsetY = 400;   // Y方向の平行移動
    private double rotationAngle = 0.0; // 回転角度（ラジアン）

    private int dragStartX, dragStartY; // マウスドラッグ開始位置
    private boolean isDragging = false; // ドラッグ中かどうかを判定
    private int selectedVertexIndex = -1; // 選択された頂点のインデックス
    private int selectedComponentIndex = -1; // 選択されたコンポーネントのインデックス

    public CurveVisualizer(List<VertFileReader.CurveComponent> components) {
        this.components = components;

        // マウスホイールでズームを処理
        addMouseWheelListener(e -> {
            if (e.getPreciseWheelRotation() < 0) {
                scale *= 1.1; // ズームイン
            } else {
                scale /= 1.1; // ズームアウト
            }
            repaint();
        });

        // マウスイベントの登録
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStartX = e.getX();
                dragStartY = e.getY();
                isDragging = false; // 初期化
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!isDragging) { // ドラッグ中でない場合のみクリック処理
                    handleMouseClick(e.getX(), e.getY());
                }
                isDragging = false; // ドラッグ終了
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                isDragging = true; // ドラッグ中にフラグを設定

                int deltaX = e.getX() - dragStartX;
                int deltaY = e.getY() - dragStartY;

                offsetX += deltaX;
                offsetY += deltaY;

                dragStartX = e.getX();
                dragStartY = e.getY();

                repaint();
            }
        });

        // キーボードで回転を処理
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    rotationAngle -= Math.toRadians(5); // 左回転
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    rotationAngle += Math.toRadians(5); // 右回転
                }
                repaint();
            }
        });

        setFocusable(true); // キーイベントを受け取るために必要
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 中心点で回転を適用
        g2d.translate(getWidth() / 2.0, getHeight() / 2.0); // 回転の基準を画面中央に
        g2d.rotate(rotationAngle); // 回転適用
        g2d.translate(-getWidth() / 2.0, -getHeight() / 2.0); // 元の座標系に戻す

        // 曲線の描画
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.RED);

        for (VertFileReader.CurveComponent component : components) {
            List<double[]> vertices = component.vertices;

            for (int i = 0; i < vertices.size(); i++) {
                double[] current = vertices.get(i);
                double[] next = vertices.get((i + 1) % vertices.size());

                int x1 = (int) (current[0] * scale) + offsetX;
                int y1 = (int) (current[1] * scale) + offsetY;
                int x2 = (int) (next[0] * scale) + offsetX;
                int y2 = (int) (next[1] * scale) + offsetY;

                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // 選択された点でベクトルを描画
        if (selectedVertexIndex >= 0 && selectedComponentIndex >= 0) {
            VertFileReader.CurveComponent component = components.get(selectedComponentIndex);
            List<double[]> vertices = component.vertices;

            double[] vertex = vertices.get(selectedVertexIndex);

            // 接線と法線の計算
            List<double[]> tangents = CurveAnalysis.computeTangents(vertices);
            List<double[]> normals = CurveAnalysis.computeNormals(tangents);

            double[] tangent = tangents.get(selectedVertexIndex);
            double[] normal = normals.get(selectedVertexIndex);

            int x = (int) (vertex[0] * scale) + offsetX;
            int y = (int) (vertex[1] * scale) + offsetY;

            // 接線ベクトルを描画（青）
            g2d.setColor(Color.BLUE);
            int tx = (int) (x + tangent[0] * scale);
            int ty = (int) (y + tangent[1] * scale);
            drawArrow(g2d, x, y, tx, ty);

            // 法線ベクトルを描画（緑）
            g2d.setColor(Color.GREEN);
            int nx = (int) (x + normal[0] * scale);
            int ny = (int) (y + normal[1] * scale);
            drawArrow(g2d, x, y, nx, ny);
        }
    }

    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        g2d.drawLine(x1, y1, x2, y2);

        double arrowAngle = Math.toRadians(20); // 矢印の角度
        double arrowLength = 10; // 矢印の長さ

        double dx = x2 - x1;
        double dy = y2 - y1;
        double theta = Math.atan2(dy, dx);

        int xLeft = (int) (x2 - arrowLength * Math.cos(theta + arrowAngle));
        int yLeft = (int) (y2 - arrowLength * Math.sin(theta + arrowAngle));

        int xRight = (int) (x2 - arrowLength * Math.cos(theta - arrowAngle));
        int yRight = (int) (y2 - arrowLength * Math.sin(theta - arrowAngle));

        g2d.drawLine(x2, y2, xLeft, yLeft);
        g2d.drawLine(x2, y2, xRight, yRight);
    }

    private void handleMouseClick(int mouseX, int mouseY) {
        // 回転の影響を考慮してクリック位置を変換
        double centerX = getWidth() / 2.0;
        double centerY = getHeight() / 2.0;
        double cosTheta = Math.cos(-rotationAngle);
        double sinTheta = Math.sin(-rotationAngle);

        double transformedX = cosTheta * (mouseX - centerX) - sinTheta * (mouseY - centerY) + centerX;
        double transformedY = sinTheta * (mouseX - centerX) + cosTheta * (mouseY - centerY) + centerY;

        double minDistance = Double.MAX_VALUE;
        selectedVertexIndex = -1;
        selectedComponentIndex = -1;

        for (int c = 0; c < components.size(); c++) {
            List<double[]> vertices = components.get(c).vertices;
            for (int i = 0; i < vertices.size(); i++) {
                double[] vertex = vertices.get(i);
                int x = (int) (vertex[0] * scale) + offsetX;
                int y = (int) (vertex[1] * scale) + offsetY;

                double distance = Math.sqrt(Math.pow(transformedX - x, 2) + Math.pow(transformedY - y, 2));
                if (distance < minDistance && distance < 20) { // クリックが近い場合のみ選択
                    minDistance = distance;
                    selectedVertexIndex = i;
                    selectedComponentIndex = c;
                }
            }
        }

        repaint();
    }

    public static void main(String[] args) {
        try {
            String filePath = "./vert/riderr.vert";
            List<VertFileReader.CurveComponent> components = VertFileReader.loadVertFile(filePath);

            JFrame frame = new JFrame("Curve Visualizer with Drag and Rotate");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 800);
            frame.add(new CurveVisualizer(components));
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
