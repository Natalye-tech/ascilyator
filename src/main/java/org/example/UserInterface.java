package org.example;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class UserInterface extends JFrame implements ActionListener {
    private JTextField tfInductance;
    private JTextField tfCapacitance;
    private JTextField tfInitialResistance;
    private JTextField tfFinalResistance;
    private JTextField tfInitialCharge;
    private JTextField tfInitialCurrent;
    private JTextField tfNumberOfSteps;
    private JButton btnStartSimulation;
    private JButton btnResetSimulation;
    private JLabel lblInfo;

    // Объявляем panel глобально, чтобы она была доступна в других методах
    private JPanel inputPanel;

    private double inductance = 1.0;       // Индуктивность L
    private double capacitance = 1.0;      // Ёмкость C
    private double initialResistance = 0.1;// Начальное сопротивление
    private double finalResistance = 0.5;  // Конечное сопротивление
    private double initialCharge = 1.0;    // Начальный заряд Q0
    private double initialCurrent = 0.0;   // Начальный ток I0
    private double timeStep = 0.01;        // Шаг по времени
    private int numberOfSteps = 1000;      // Количество шагов

    public UserInterface() {
        initUI();
    }

    private void initUI() {
        setTitle("Комбинированная симуляция RLC-цепи");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));

        // Заменяем иконку окна
        Image icon = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/icons/formula.png"));
        setIconImage(icon);

        // Основной контейнер
        Container container = getContentPane();
        container.setLayout(new BorderLayout());

        // Верхняя панель с кнопками и полями ввода
        inputPanel = new JPanel(); // Здесь определяем inputPanel
        inputPanel.setBorder(new EmptyBorder(10, 10, 30, 10));
        inputPanel.setLayout(new FlowLayout()); // Горизонтальное расположение элементов

        // Поля ввода
        tfInductance = new JTextField(Double.toString(inductance), 10);
        inputPanel.add(new JLabel("Индуктивность (Генри): "));
        tfInductance.setPreferredSize(new Dimension(500, 20)); // Ширина 150 пикселей, высота 25 пикселей
        inputPanel.add(tfInductance);

        tfCapacitance = new JTextField(Double.toString(capacitance), 10); // Тут присваиваем начальное значение емкости
        inputPanel.add(new JLabel("Ёмкость (Фарад): "));
        inputPanel.add(tfCapacitance);

        tfInitialResistance = new JTextField(Double.toString(initialResistance), 10);
        inputPanel.add(new JLabel("Начальное сопротивление (Ом): "));
        inputPanel.add(tfInitialResistance);

        tfFinalResistance = new JTextField(Double.toString(finalResistance), 10);
        inputPanel.add(new JLabel("Конечное сопротивление (Ом): "));
        inputPanel.add(tfFinalResistance);

        tfInitialCharge = new JTextField(Double.toString(initialCharge), 10);
        inputPanel.add(new JLabel("Начальный заряд (Кулон): "));
        inputPanel.add(tfInitialCharge);

        tfInitialCurrent = new JTextField(Double.toString(initialCurrent), 10);
        inputPanel.add(new JLabel("Начальный ток (Ампер): "));
        inputPanel.add(tfInitialCurrent);

        tfNumberOfSteps = new JTextField(Integer.toString(numberOfSteps), 10);
        inputPanel.add(new JLabel("Количество шагов: "));
        inputPanel.add(tfNumberOfSteps);

        // Кнопки управления
        btnStartSimulation = new JButton("Старт");
        btnStartSimulation.addActionListener(this);
        btnResetSimulation = new JButton("Сброс");
        btnResetSimulation.addActionListener(this);

        inputPanel.add(btnStartSimulation);
        inputPanel.add(btnResetSimulation); // Новая кнопка

        // Центральная панель с двумя областями графика
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1, 3)); // Одна строка, три столбца

        // Изначально центр пустой
        container.add(inputPanel, BorderLayout.NORTH);
        container.add(centerPanel, BorderLayout.CENTER);

        // Информационная панель снизу
        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        lblInfo = new JLabel("Нажмите 'Старт', чтобы начать симуляцию.");
        infoPanel.add(lblInfo);
        container.add(infoPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null); // Центруем окно
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == btnStartSimulation) {
            startSimulation();
        } else if (source == btnResetSimulation) {
            resetSimulation();
        }
    }

    private void startSimulation() {
        // Получаем новые параметры из полей ввода
        try {
            inductance = Double.parseDouble(tfInductance.getText());
            capacitance = Double.parseDouble(tfCapacitance.getText()); // Читаем значение ёмкости
            initialResistance = Double.parseDouble(tfInitialResistance.getText());
            finalResistance = Double.parseDouble(tfFinalResistance.getText());
            initialCharge = Double.parseDouble(tfInitialCharge.getText());
            initialCurrent = Double.parseDouble(tfInitialCurrent.getText());
            numberOfSteps = Integer.parseInt(tfNumberOfSteps.getText());

            // Создание центральной панели с графиками
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new GridLayout(1, 3)); // Три графика в ряд

            // ЧИСЛЕННЫЙ МЕТОД
            NumericalMethod numericalMethod = new NumericalMethod(inductance, capacitance, initialResistance, finalResistance,
                    initialCharge, initialCurrent, timeStep, numberOfSteps);
            List<XYSeries> numericalSeries = numericalMethod.calculateAllSeries();
            XYSeriesCollection numericalDataset = new XYSeriesCollection();
            numericalDataset.addSeries(numericalSeries.get(0)); // Заряд
            numericalDataset.addSeries(numericalSeries.get(1)); // Ток
            numericalDataset.addSeries(numericalSeries.get(2)); // Магнитное поле

            // Рисуем графики
            JFreeChart numericalChart = ChartFactory.createXYLineChart(
                    "Численный метод",
                    "Время (секунды)",
                    "",
                    numericalDataset,
                    PlotOrientation.VERTICAL,
                    true, false, false
            );

            // Настройки рендера для численного метода
            XYPlot numericalPlot = numericalChart.getXYPlot();
            XYLineAndShapeRenderer numericalRenderer = (XYLineAndShapeRenderer) numericalPlot.getRenderer();
            numericalRenderer.setSeriesPaint(0, Color.BLUE); // Линия заряда синего цвета
            numericalRenderer.setSeriesStroke(0, new BasicStroke(2.0f)); // Толщина линии заряда 2px
            numericalRenderer.setSeriesPaint(1, Color.RED); // Линия тока красного цвета
            numericalRenderer.setSeriesStroke(1, new BasicStroke(2.0f)); // Толщина линии тока 2px
            numericalRenderer.setSeriesPaint(2, Color.GREEN); // Линия магнитного поля зелёного цвета
            numericalRenderer.setSeriesStroke(2, new BasicStroke(2.0f)); // Толщина линии магнитного поля 2px

            ChartPanel numericalChartPanel = new ChartPanel(numericalChart);
            centerPanel.add(numericalChartPanel);

            // АНАЛИТИЧЕСКИЙ МЕТОД
            AnalyticalMethod analyticalMethod = new AnalyticalMethod(inductance, capacitance, initialResistance, finalResistance,
                    initialCharge, initialCurrent, timeStep, numberOfSteps);
            List<XYSeries> analyticalSeries = analyticalMethod.calculateAllSeries();
            XYSeriesCollection analyticalDataset = new XYSeriesCollection();
            analyticalDataset.addSeries(analyticalSeries.get(0)); // Заряд
            analyticalDataset.addSeries(analyticalSeries.get(1)); // Ток
            analyticalDataset.addSeries(analyticalSeries.get(2)); // Магнитное поле

            // Рисуем графики
            JFreeChart analyticalChart = ChartFactory.createXYLineChart(
                    "Аналитический метод",
                    "Время (секунды)",
                    "",
                    analyticalDataset,
                    PlotOrientation.VERTICAL,
                    true, false, false
            );

            // Настройки рендера для аналитического метода
            XYPlot analyticalPlot = analyticalChart.getXYPlot();
            XYLineAndShapeRenderer analyticalRenderer = (XYLineAndShapeRenderer) analyticalPlot.getRenderer();
            analyticalRenderer.setSeriesPaint(0, Color.BLUE); // Линия заряда синего цвета
            analyticalRenderer.setSeriesStroke(0, new BasicStroke(2.0f)); // Толщина линии заряда 2px
            analyticalRenderer.setSeriesPaint(1, Color.RED); // Линия тока красного цвета
            analyticalRenderer.setSeriesStroke(1, new BasicStroke(2.0f)); // Толщина линии тока 2px
            analyticalRenderer.setSeriesPaint(2, Color.GREEN); // Линия магнитного поля зелёного цвета
            analyticalRenderer.setSeriesStroke(2, new BasicStroke(2.0f)); // Толщина линии магнитного поля 2px

            ChartPanel analyticalChartPanel = new ChartPanel(analyticalChart);
            centerPanel.add(analyticalChartPanel);

            // Контейнер с графиками
            Container contentPane = getContentPane();
            contentPane.removeAll();
            contentPane.add(inputPanel, BorderLayout.NORTH); // Используем глобальный inputPanel
            contentPane.add(centerPanel, BorderLayout.CENTER);

            // Информационная панель снизу
            JPanel infoPanel = new JPanel();
            infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            lblInfo = new JLabel("Симуляция запущена.");
            infoPanel.add(lblInfo);
            contentPane.add(infoPanel, BorderLayout.SOUTH);

            pack();
            revalidate();
            repaint();
        } catch (NumberFormatException exc) {
            JOptionPane.showMessageDialog(this, "Некорректный ввод.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetSimulation() {
        // Восстановление стандартных параметров
        inductance = 1.0;
        capacitance = 1.0;
        initialResistance = 0.1;
        finalResistance = 0.5;
        initialCharge = 1.0;
        initialCurrent = 0.0;
        numberOfSteps = 1000;

        // Устанавливаем стандартные значения в поля ввода
        tfInductance.setText(Double.toString(inductance));
        tfCapacitance.setText(Double.toString(capacitance));
        tfInitialResistance.setText(Double.toString(initialResistance));
        tfFinalResistance.setText(Double.toString(finalResistance));
        tfInitialCharge.setText(Double.toString(initialCharge));
        tfInitialCurrent.setText(Double.toString(initialCurrent));
        tfNumberOfSteps.setText(Integer.toString(numberOfSteps));

        // Скрываем графики и возвращаем окно в начальное состояние
        JPanel emptyCenterPanel = new JPanel();
        emptyCenterPanel.setLayout(new GridLayout(1, 3)); // Готовим панель для будущих графиков
        Container contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.add(inputPanel, BorderLayout.NORTH); // Используем тот же самый inputPanel
        contentPane.add(emptyCenterPanel, BorderLayout.CENTER);

        // Информационная панель снизу
        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        lblInfo = new JLabel("Нажмите 'Старт', чтобы начать симуляцию.");
        infoPanel.add(lblInfo);
        contentPane.add(infoPanel, BorderLayout.SOUTH);

        pack();
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserInterface ui = new UserInterface();
        });
    }
}