package org.example;

import org.jfree.data.xy.XYSeries;
import java.util.ArrayList;
import java.util.List;

public class AnalyticalMethod {
    private final double inductance;       // Индуктивность L
    private final double capacitance;      // Ёмкость C
    private final double initialResist;    // Начальное сопротивление
    private final double finalResist;      // Конечное сопротивление
    private final double initialCharge;    // Начальный заряд Q0
    private final double initialCurrent;   // Начальный ток I0
    private final double timeStep;         // Шаг по времени
    private final int numberOfSteps;       // Количество шагов

    private final double gamma;            // Коэффициент затухания
    private final double omegaDamped;      // Собственная частота затухающих колебаний

    public AnalyticalMethod(double inductance, double capacitance, double initialResist, double finalResist,
                            double initialCharge, double initialCurrent, double timeStep, int numberOfSteps) {
        this.inductance = inductance;
        this.capacitance = capacitance;
        this.initialResist = initialResist;
        this.finalResist = finalResist;
        this.initialCharge = initialCharge;
        this.initialCurrent = initialCurrent;
        this.timeStep = timeStep;
        this.numberOfSteps = numberOfSteps;

        // Пересчитываем коэффициенты
        gamma = initialResist / (2 * inductance); // Начальное значение гамма
        omegaDamped = Math.sqrt(Math.max(0, 1.0 / (inductance * capacitance) - gamma * gamma));
    }

    // Метод, рассчитывающий все три графика одновременно
    public List<XYSeries> calculateAllSeries() {
        XYSeries chargeSeries = new XYSeries("Заряд (аналитический)");
        XYSeries currentSeries = new XYSeries("Ток (аналитический)");
        XYSeries magneticFieldSeries = new XYSeries("Магнитное поле (Тесла)");

        double mu0 = 4 * Math.PI * 1e-7; // Магнитная постоянная
        double n = 1000000; // Плотность витков

        for (int i = 0; i <= numberOfSteps; i++) {
            double time = i * timeStep;

            // Интерполируем сопротивление
            double resistAtTime = interpolateResistance(i, numberOfSteps);
            double gammaAtTime = resistAtTime / (2 * inductance); // Рассчитываем новое значение гаммы
            double omegaDampedAtTime = Math.sqrt(Math.max(0, 1.0 / (inductance * capacitance) - gammaAtTime * gammaAtTime));

            // Расчёт заряда
            double charge = initialCharge * Math.exp(-gammaAtTime * time) * Math.cos(omegaDampedAtTime * time);
            chargeSeries.add(time, charge);

            // Расчёт тока
            double current = -Math.exp(-gammaAtTime * time) *
                    (
                            gammaAtTime * initialCharge * Math.cos(omegaDampedAtTime * time) -
                                    omegaDampedAtTime * initialCharge * Math.sin(omegaDampedAtTime * time)
                    );
            currentSeries.add(time, current);

            // Расчёт магнитного поля
            double magneticField = mu0 * n * current;
            magneticFieldSeries.add(time, magneticField);
        }

        return List.of(chargeSeries, currentSeries, magneticFieldSeries);
    }

    // Линейная интерполяция сопротивления
    private double interpolateResistance(int currentStep, int totalSteps) {
        return initialResist + ((finalResist - initialResist) * currentStep / totalSteps);
    }
}