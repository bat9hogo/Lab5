import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Stack;
import javax.swing.*;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel implements MouseListener, MouseMotionListener {
    // Список координат точек для построения графика
    private Double[][] graphicsData;
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean rotate = false;
    private boolean showMarkers = true;
    private boolean showGrid = false;
    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Используемый масштаб отображения
    private double scale;
    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private BasicStroke zoomStoke;
    // Различные шрифты отображения надписей
    private Font axisFont;

    private Double[] closestPoint = null;
    private String coordinatesText = "";
    private Point2D.Double zoompoint1,zoompoint2;


    private ArrayList<Double[]> markers = new ArrayList<>();
    private Stack<ArrayList<Double[]>> history = new Stack<>();

    private Point2D.Double scale1,scale2;
    private boolean graphicIsOpen;

    private Point2D.Double selectedMarker;
    private boolean changeSelectedMarker = false;
    private Point2D.Double movedMarker;

    public GraphicsDisplay() {
// Цвет заднего фона области отображения - белый
        setBackground(Color.WHITE);
// Сконструировать необходимые объекты, используемые в рисовании
// Перо для рисования графика
        float[] dash = {21,10,3,10,12,10,3,10,21,10};
        graphicsStroke = new BasicStroke(4.0f, BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER, 22.0f, dash, 0.0f);
// Перо для рисования осей координат
        axisStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Перо для рисования контуров маркеров
        markerStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Шрифт для подписей осей координат
        axisFont = new Font("Serif", Font.BOLD, 36);

        zoomStoke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    // Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
    // главного окна приложения в случае успешной загрузки данных
    public void showGraphics(Double[][] graphicsData) {
// Сохранить массив точек во внутреннем поле класса
        this.graphicsData = graphicsData;
        graphicIsOpen = true;
// Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
        repaint();
    }

    // Методы-модификаторы для изменения параметров отображения графика
// Изменение любого параметра приводит к перерисовке области
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int button = e.getButton();
        if(button == MouseEvent.BUTTON1 && graphicIsOpen && selectedMarker == null) {
            zoompoint1 = new Point2D.Double(e.getX(), e.getY());
            scale1 = zoompoint1;
            repaint();
        }
        if(button == MouseEvent.BUTTON1 && graphicIsOpen && selectedMarker != null){
            changeSelectedMarker = true;
            movedMarker = new Point2D.Double(e.getX(),e.getY());
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(graphicIsOpen) {
            scale2 = zoompoint2;
            zoompoint2 = null;
            changeSelectedMarker = false;
            movedMarker = null;
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(graphicIsOpen) {
            if(zoompoint1 != null) {
                zoompoint2 = new Point2D.Double(e.getX(), e.getY());
                repaint();
            }
            if(changeSelectedMarker){
                movedMarker.y = (new Point2D.Double(e.getX(),e.getY())).y;
                repaint();
            }
            for(Double[] point : graphicsData){
                Point2D.Double center = xyToPoint(point[0],point[1]);
                if((center.getX() -5) < e.getX() && e.getX() < (center.getX() +5)
                        && (center.getY() -5) < e.getY() && e.getY() < (center.getY() +5)){
                    selectedMarker = center;
                    repaint();
                    break;
                }else{
                    selectedMarker = null;
                    repaint();
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if(graphicIsOpen) {
            closestPoint = findClosestPoint(e.getX(), e.getY());
            if (closestPoint != null) {
                coordinatesText = "X: " + closestPoint[0] + " Y: " + closestPoint[1];
            } else {
                coordinatesText = "";
            }
            repaint();

            for(Double[] point : graphicsData){
                Point2D.Double center = xyToPoint(point[0],point[1]);
                if((center.getX() -5) < e.getX() && e.getX() < (center.getX() +5)
                        && (center.getY() -5) < e.getY() && e.getY() < (center.getY() +5)){
                    selectedMarker = center;
                    repaint();
                    break;
                }else{
                    selectedMarker = null;
                    repaint();
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int button = e.getButton();
        if(button == MouseEvent.BUTTON3 && !history.isEmpty()){
            ArrayList<Double[]> temp = history.pop();
            graphicsData = temp.toArray(graphicsData);
            repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    private Double[] findClosestPoint(int mouseX, int mouseY) {
        if (graphicsData == null) return null;
        Double[] closest = null;
        double minDistance = Double.MAX_VALUE;
        for (Double[] point : graphicsData) {
            int x = (int) ((point[0] - minX) * scale);  // Преобразование координат
            int y = (int) ((maxY - point[1]) * scale);  // Преобразование координат
            double distance = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
            if (distance < 10) {  // Пороговое значение для "близости"
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = point;
                }
            }
        }
        return closest;
    }

    public void graphicZoom(){
        if(scale1 != null && scale2 != null) {
            ArrayList<Double[]> tempList = new ArrayList<>();
            for (Double[] point : graphicsData) {
                Point2D.Double temp = xyToPoint(point[0],point[1]);
                tempList.add(new Double[]{point[0],point[1]});

                if (temp.getX() > scale1.getX() && temp.getX() < scale2.getX()
                        && temp.getY() >= scale1.getY() && temp.getY() <= scale2.getY()) {
                    markers.add(new Double[]{point[0], point[1]});

                }
            }
            history.add(tempList);
            graphicsData = new Double[markers.size()][1];
            graphicsData = markers.toArray(graphicsData);
            repaint();
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (graphicsData == null || graphicsData.length == 0) return;

        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;

        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1] > maxY) {
                maxY = graphicsData[i][1];
            }
        }

        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);

        scale = Math.min(scaleX, scaleY);

        if (scale == scaleX) {
            double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale == scaleY) {

            double xIncrement = (getSize().getWidth() / scale - (maxX -
                    minX)) / 2;
            maxX += xIncrement;
            minX -= xIncrement;
        }

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();

        if (rotate) {
            // Сохраняем текущее преобразование
            canvas.translate(getSize().getWidth()/2, getSize().getHeight()/2);
            // Поворачиваем на 90 градусов против часовой стрелки
            canvas.rotate(-Math.PI/2);
            // Масштабируем, чтобы график занимал всё окно
            if (getSize().getWidth() > getSize().getHeight()) {
                canvas.scale(getSize().getHeight()/getSize().getWidth(),
                        getSize().getWidth()/getSize().getHeight());
            } else {
                canvas.scale(getSize().getHeight()/getSize().getWidth(),
                        getSize().getWidth()/getSize().getHeight());
            }
            // Возвращаем в центр
            canvas.translate(-getSize().getWidth()/2, -getSize().getHeight()/2);
        }

        if (showAxis) paintAxis(canvas);

        if(showGrid) paintGrid(canvas);

// Затем отображается сам график
        paintGraphics(canvas);
// Затем (если нужно) отображаются маркеры точек, по которым строился график.
        if (showMarkers) paintMarkers(canvas);

        if (closestPoint != null) {
            coordinatesText = String.format("X: %.2f, Y: %.2f", closestPoint[0], closestPoint[1]);
            // Рисуем текст рядом с курсором
            canvas.setColor(Color.BLACK);
            canvas.setFont(new Font("Serif", Font.BOLD, 16));
            canvas.drawString(coordinatesText, getMousePosition().x + 10, getMousePosition().y);
        }

// Шаг 9 - Восстановить старые настройки холста
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    private void paintGrid(Graphics2D canvas) {
        // Шаги сетки для осей X и Y
        double gridStepX = calculateGridStep(maxX - minX);
        double gridStepY = calculateGridStep(maxY - minY);

        // Начальные координаты сетки
        double startX = Math.floor(minX / gridStepX) * gridStepX;
        double startY = Math.floor(minY / gridStepY) * gridStepY;

        // Сохраняем старые настройки
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Font oldFont = canvas.getFont();

        // Настройки для сетки
        canvas.setStroke(new BasicStroke(0.5f));
        canvas.setColor(Color.LIGHT_GRAY);
        canvas.setFont(new Font("Dialog", Font.PLAIN, 10));

        // Рисуем вертикальные линии сетки
        double x = startX;
        while (x <= maxX) {
            Point2D.Double point1 = xyToPoint(x, minY);
            Point2D.Double point2 = xyToPoint(x, maxY);
            canvas.setColor(Color.LIGHT_GRAY);
            // Основная линия сетки
            canvas.drawLine((int)point1.x, (int)point1.y, (int)point2.x, (int)point2.y);

            // Рисуем деления внутри ячейки
            if (x + gridStepX <= maxX) {
                double subStep = gridStepX / 10;
                for (int i = 1; i < 10; i++) {
                    double subX = x + i * subStep;
                    Point2D.Double subPoint1 = xyToPoint(subX, minY);
                    Point2D.Double subPoint2 = xyToPoint(subX, maxY);
                    canvas.setColor(Color.LIGHT_GRAY);
                    // Для пятого деления (середина) делаем линию длиннее
                    if (i == 5) {
                        canvas.setStroke(new BasicStroke(0.5f));
                        canvas.drawLine((int)subPoint1.x, (int)subPoint1.y,
                                (int)subPoint2.x, (int)subPoint2.y);
                    } else {
                        // Короткие штрихи для остальных делений
                        canvas.setStroke(new BasicStroke(0.3f));
                        double shortLineLength = (maxY - minY) / 50;
                        Point2D.Double subPoint2Short = xyToPoint(subX, minY + shortLineLength);
                        canvas.drawLine((int)subPoint1.x, (int)subPoint1.y,
                                (int)subPoint2Short.x, (int)subPoint2Short.y);
                    }
                }
            }

            canvas.setColor(Color.BLACK);
            // Подписи координат
            canvas.drawString(String.format("%.2f", x), (int)point1.x - 20, getHeight() - 5);
            x += gridStepX;
        }
        canvas.setColor(Color.LIGHT_GRAY);
        // Рисуем горизонтальные линии сетки
        double y = startY;
        while (y <= maxY) {
            Point2D.Double point1 = xyToPoint(minX, y);
            Point2D.Double point2 = xyToPoint(maxX, y);
            canvas.setColor(Color.LIGHT_GRAY);
            // Основная линия сетки
            canvas.drawLine((int)point1.x, (int)point1.y, (int)point2.x, (int)point2.y);

            // Рисуем деления внутри ячейки
            if (y + gridStepY <= maxY) {
                double subStep = gridStepY / 10;
                for (int i = 1; i < 10; i++) {
                    double subY = y + i * subStep;
                    Point2D.Double subPoint1 = xyToPoint(minX, subY);
                    Point2D.Double subPoint2 = xyToPoint(maxX, subY);
                    canvas.setColor(Color.LIGHT_GRAY);
                    // Для пятого деления (середина) делаем линию длиннее
                    if (i == 5) {
                        canvas.setStroke(new BasicStroke(0.5f));
                        canvas.drawLine((int)subPoint1.x, (int)subPoint1.y,
                                (int)subPoint2.x, (int)subPoint2.y);
                    } else {
                        // Короткие штрихи для остальных делений
                        canvas.setStroke(new BasicStroke(0.3f));
                        double shortLineLength = (maxX - minX) / 50;
                        Point2D.Double subPoint2Short = xyToPoint(minX + shortLineLength, subY);
                        canvas.drawLine((int)subPoint1.x, (int)subPoint1.y,
                                (int)subPoint2Short.x, (int)subPoint2Short.y);
                    }
                }
            }
            canvas.setColor(Color.BLACK);
            // Подписи координат
            canvas.drawString(String.format("%.2f", y), 5, (int)point1.y + 5);
            y += gridStepY;
        }
        canvas.setColor(Color.LIGHT_GRAY);
        // Восстанавливаем настройки
        canvas.setStroke(oldStroke);
        canvas.setColor(oldColor);
        canvas.setFont(oldFont);
    }

    // Вспомогательная функция для расчета оптимального шага сетки
    private double calculateGridStep(double range) {
        // Желаемое количество делений (от 5 до 20)
        int desiredDivisions = 5;

        // Находим приближенный шаг
        double roughStep = range / desiredDivisions;

        // Получаем порядок величины шага
        double power = Math.floor(Math.log10(roughStep));
        double magnitude = Math.pow(5, power);

        // Нормализованный шаг (от 0.1 до 1.0)
        double normalizedStep = roughStep / magnitude;

        // Выбираем ближайший "красивый" шаг
        double[] steps = {0.1, 0.2, 0.5, 1.0, 20.0,50.0,100.0};
        double bestStep = steps[0];
        double minDiff = Math.abs(normalizedStep - steps[0]);

        for (double step : steps) {
            double diff = Math.abs(normalizedStep - step);
            if (diff < minDiff) {
                minDiff = diff;
                bestStep = step;
            }
        }

        return bestStep * magnitude;
    }

    // Отрисовка графика по прочитанным координатам
    protected void paintGraphics(Graphics2D canvas) {
// Выбрать линию для рисования графика
        canvas.setStroke(graphicsStroke);
// Выбрать цвет линии
        canvas.setColor(Color.RED);

        GeneralPath graphics = new GeneralPath();
        boolean markerChanged = false;
        for (int i = 0; i < graphicsData.length; i++) {
// Преобразовать значения (x,y) в точку на экране point
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);

            if(changeSelectedMarker && selectedMarker != null &&
            Math.abs(point.getX() - selectedMarker.getX()) < 5){
                double newY = pointToXY(new Point2D.Double(point.getX(), movedMarker.getY())).getY();
                graphicsData[i][1] = newY;
                point = xyToPoint(graphicsData[i][0], newY);
                markerChanged = true;
            }
            if (i > 0) {
// Не первая итерация цикла - вести линию в точку point
                graphics.lineTo(point.getX(), point.getY());
            } else {
// Первая итерация цикла - установить начало пути в точку point
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        if(markerChanged){
            repaint();
        }
// Отобразить график
        canvas.draw(graphics);

        if(scale1 != null && scale2 != null){
            graphicZoom();
            markers.clear();
            zoompoint1 = null;
            zoompoint2 = null;
            scale1 = null;
            scale2 = null;
        }
        if(zoompoint1 != null && zoompoint2 != null){
            canvas.setStroke(zoomStoke);
            canvas.setColor(Color.BLACK);
            canvas.drawRect((int) Math.min(zoompoint1.getX(), zoompoint2.getX()),(int) Math.min(zoompoint1.getY(), zoompoint2.getY()),
                    (int) Math.abs(zoompoint2.getX() - zoompoint1.getX()),(int) Math.abs(zoompoint2.getY() - zoompoint1.getY()));
        }
    }



    protected void paintMarkers(Graphics2D canvas) {
        // Установить специальное перо для черчения контуров маркеров
        canvas.setStroke(markerStroke);
        // Выбрать красный цвет для контуров маркеров
        // Организовать цикл по всем точкам графика
        for (int i = 0; i < graphicsData.length; i++) {
            // Получить координаты точки
            Double[] point = graphicsData[i];
            Point2D.Double center = xyToPoint(point[0], point[1]);

            if (hasOnlyEvenDigits(point[1])) {
                canvas.setColor(Color.BLUE);
            } else {
                canvas.setColor(Color.BLACK); // Иначе красным
            }
            // Размер основных линий креста (половина полного размера)
            int mainSize = 5; // полный размер будет 11 точек
            // Размер перпендикулярных линий на концах (полная длина)
            int crossSize = 4; // по 2 точки в каждую сторону

            // Рисуем горизонтальную линию креста
            canvas.draw(new Line2D.Double(
                    center.x - mainSize, center.y,
                    center.x + mainSize, center.y));

            // Рисуем вертикальную линию креста
            canvas.draw(new Line2D.Double(
                    center.x, center.y - mainSize,
                    center.x, center.y + mainSize));

            // Рисуем перпендикулярные линии на концах
            // Верхний конец
            canvas.draw(new Line2D.Double(
                    center.x - crossSize/2, center.y + mainSize,
                    center.x + crossSize/2, center.y + mainSize
            ));

            // Нижний конец
            canvas.draw(new Line2D.Double(
                    center.x - crossSize/2, center.y - mainSize,
                    center.x + crossSize/2, center.y - mainSize
            ));

            // Левый конец
            canvas.draw(new Line2D.Double(
                    center.x - mainSize, center.y - crossSize/2,
                    center.x - mainSize, center.y + crossSize/2
            ));

            // Правый конец
            canvas.draw(new Line2D.Double(
                    center.x + mainSize, center.y - crossSize/2,
                    center.x + mainSize, center.y + crossSize/2
            ));
        }
    }

    protected boolean hasOnlyEvenDigits(double value) {
        // Получаем целую часть числа
        int integerPart = (int) Math.abs(value);

        // Преобразуем число в строку для анализа цифр
        String numStr = String.valueOf(integerPart);

        // Проверяем каждую цифру
        for (int i = 0; i < numStr.length(); i++) {
            int digit = Character.getNumericValue(numStr.charAt(i));
            if (digit % 2 != 0) { // Если цифра нечётная
                return false;
            }
        }
        return true;
    }


    protected void paintAxis(Graphics2D canvas) {
// Установить особое начертание для осей

        canvas.setStroke(axisStroke);
// Оси рисуются чѐрным цветом
        canvas.setColor(Color.BLACK);
// Стрелки заливаются чѐрным цветом
        canvas.setPaint(Color.BLACK);
// Подписи к координатным осям делаются специальным шрифтом
        canvas.setFont(axisFont);
// Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
        FontRenderContext context = canvas.getFontRenderContext();
// Определить, должна ли быть видна ось Y на графике
        if (minX <= 0.0 && maxX >= 0.0) {
// Она должна быть видна, если левая граница показываемой области (minX) <= 0.0,
            // а правая (maxX) >= 0.0
// Сама ось - это линия между точками (0, maxY) и (0, minY)
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
// Стрелка оси Y
            GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на верхний конец оси Y
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести левый "скат" стрелки в точку с относительными координатами (5,20)
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5,
                    arrow.getCurrentPoint().getY() + 20);
// Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10,
                    arrow.getCurrentPoint().getY());
// Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси Y
// Определить, сколько места понадобится для надписи "y"
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("y", (float) labelPos.getX() + 10,
                    (float) (labelPos.getY() - bounds.getY()));
        }
// Определить, должна ли быть видна ось X на графике
        if (minY <= 0.0 && maxY >= 0.0) {
// Она должна быть видна, если верхняя граница показываемой области (maxX) >= 0.0,
// а нижняя (minY) <= 0.0
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
                    xyToPoint(maxX, 0)));
// Стрелка оси X
            GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на правый конец оси X
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести верхний "скат" стрелки в точку с относительными координатами (-20,-5)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20,
                    arrow.getCurrentPoint().getY() - 5);
// Вести левую часть стрелки в точку с относительными координатами (0, 10)
            arrow.lineTo(arrow.getCurrentPoint().getX(),
                    arrow.getCurrentPoint().getY() + 10);
// Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси X
// Определить, сколько места понадобится для надписи "x"
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("x", (float) (labelPos.getX() -
                    bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));
        }
    }

    protected Point2D.Double xyToPoint(double x, double y) {
// Вычисляем смещение X от самой левой точки (minX)
        double deltaX = x - minX;
// Вычисляем смещение Y от точки верхней точки (maxY)
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scale, deltaY * scale);
    }

    protected Point2D.Double pointToXY(Point2D.Double point){
        double x = point.getX() / scale + minX;
        double y = maxY - point.getY() / scale;
        return new Point2D.Double(x,y);
    }

    protected void saveToGraphicsFile(File selectedFile) {
        try {
            // Создать новый байтовый поток вывода, направленный в указанный файл
            DataOutputStream out = new DataOutputStream(new FileOutputStream(selectedFile));

            // Записать в поток вывода попарно значение X в точке, значение многочлена в точке
            for (int i = 0; i<graphicsData.length; i++) {
                out.writeDouble(graphicsData[i][0]);
                out.writeDouble((graphicsData[i][1]));
            }
            // Закрыть поток вывода
            out.close();
        } catch (Exception e) {
            // Исключительную ситуацию "ФайлНеНайден" в данном случае можно не обрабатывать,
            // так как мы файл создаѐм, а не открываем для чтения
        }
    }
}