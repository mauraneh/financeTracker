package com.example.financemanager.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Income {
    private final LocalDate date;
    private final float total;
    private final float salary;
    private final float aids;
    private final float freelance;
    private final float passive;
    private final float other;

    private final static String PRICE_FORMAT = "%.2f €";

    private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");

    public Income(LocalDate date, float salary, float aids, float freelance, float passive, float other) {
        this.date = date;
        this.total = salary + aids + freelance + passive + other;
        this.salary = salary;
        this.aids = aids;
        this.freelance = freelance;
        this.passive = passive;
        this.other = other;
    }

    public StringProperty dateProperty() {
        return new SimpleStringProperty(date.format(DATE_FORMAT));
    }

    public StringProperty totalProperty() {
        return formatAmount(total);
    }

    private SimpleStringProperty formatAmount(float amount) {
        return new SimpleStringProperty(String.format(PRICE_FORMAT, amount));
    }

    public StringProperty salaryProperty() {
        return formatAmount(salary);
    }

    public StringProperty aidsProperty() {
        return formatAmount(aids);
    }

    public StringProperty freelanceProperty() {
        return formatAmount(freelance);
    }

    public StringProperty passiveProperty() {
        return formatAmount(passive);
    }

    public StringProperty otherProperty() {
        return formatAmount(other);
    }


    public LocalDate getDate() {
        return date;
    }

    public float getTotal() {
        return total;
    }

    public float getSalary() {
        return salary;
    }

    public float getAids() {
        return aids;
    }

    public float getFreelance() {
        return freelance;
    }

    public float getPassive() {
        return passive;
    }

    public float getOther() {
        return other;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "date=" + date +
                ", total=" + total +
                ", salary=" + salary +
                ", aids=" + aids +
                ", freelance=" + freelance +
                ", passive=" + passive +
                ", other=" + other +
                '}';
    }

    public int compareTo(Income income) {
        return -this.date.compareTo(income.date);
    }
}
