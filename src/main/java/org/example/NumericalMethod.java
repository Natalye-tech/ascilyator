package org.example;

import org.jfree.data.xy.XYSeries;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class NumericalMethod {
    private final double inductance;       // Индуктивность L
    private final double capacitance;      // Ёмкость C
    private final double initialResist;    // Начальное сопротивление
    private final double finalResist;      // Конечное сопротивление
    private final double initialCharge;    // Начальный заряд Q0
    private final double initialCurrent;   // Начальный ток I0
    private final double timeStep;         // Шаг по времени
    private final int numberOfSteps;       // Количество шагов

    public NumericalMethod(double inductance, double capacitance, double initialResist, double finalResist,
                           double initialCharge, double initialCurrent, double timeStep, int numberOfSteps) {
        this.inductance = inductance;
        this.capacitance = capacitance;
        this.initialResist = initialResist;
        this.finalResist = finalResist;
        this.initialCharge = initialCharge;
        this.initialCurrent = initialCurrent;
        this.timeStep = timeStep;
        this.numberOfSteps = numberOfSteps;
    }

    // Метод, рассчитывающий все три графика одновременно
    public List<XYSeries> calculateAllSeries() {
        XYSeries chargeSeries = new XYSeries("Заряд (численный)");
        XYSeries currentSeries = new XYSeries("Ток (численный)");
        XYSeries magneticFieldSeries = new XYSeries("Магнитное поле (Тесла)");

        double mu0 = 4 * Math.PI * 1e-7; // Магнитная постоянная
        double n = 1000000; // Плотность витков

        // Выполняем цикл Рунге-Кутта только один раз
        double q = initialCharge;
        double I = initialCurrent;
        for (int i = 0; i <= numberOfSteps; i++) {
            double time = i * timeStep;

            // Интерполируем сопротивление
            double resistAtTime = interpolateResistance(i, numberOfSteps);

            // Метод Рунге-Кутта
            double k1_q = derivativeQ(q, I) * timeStep;
            double l1_I = derivativeI(q, I, resistAtTime) * timeStep;

            double k2_q = derivativeQ(q + k1_q / 2, I + l1_I / 2) * timeStep;
            double l2_I = derivativeI(q + k1_q / 2, I + l1_I / 2, resistAtTime) * timeStep;

            double k3_q = derivativeQ(q + k2_q / 2, I + l2_I / 2) * timeStep;
            double l3_I = derivativeI(q + k2_q / 2, I + l2_I / 2, resistAtTime) * timeStep;

            double k4_q = derivativeQ(q + k3_q, I + l3_I) * timeStep;
            double l4_I = derivativeI(q + k3_q, I + l3_I, resistAtTime) * timeStep;

            // Накапливаем заряд и ток
            q += (k1_q + 2 * k2_q + 2 * k3_q + k4_q) / 6.0;
            I += (l1_I + 2 * l2_I + 2 * l3_I + l4_I) / 6.0;

            // Формирование графиков
            chargeSeries.add(time, q);
            currentSeries.add(time, I);
            magneticFieldSeries.add(time, mu0 * n * I);
        }

        return List.of(chargeSeries, currentSeries, magneticFieldSeries);
    }

    // Линейная интерполяция сопротивления
    private double interpolateResistance(int currentStep, int totalSteps) {
        return initialResist + ((finalResist - initialResist) * currentStep / totalSteps);
    }

    // Производные
    private double derivativeQ(double q, double I) { return I; }
    private double derivativeI(double q, double I, double resistance) {
        return -(resistance / inductance) * I - (1.0 / (inductance * capacitance)) * q;
    }
}