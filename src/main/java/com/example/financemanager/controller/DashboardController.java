package com.example.financemanager.controller;

import com.example.financemanager.db.ExpenseDAO;
import com.example.financemanager.db.IncomeDAO;

import com.example.financemanager.model.Expense;
import com.example.financemanager.model.Income;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ChoiceBox;
import javafx.util.Pair;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML
    private PieChart pieChart;

    @FXML
    private LineChart<String, Float> lineChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private ChoiceBox<String> periodChoiceBox;

    @FXML
    private BarChart<String, Number> barChart;

    private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM yy");
    private final static DateTimeFormatter FULL_DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");

    public void initialize() {
        LocalDate date = LocalDate.now();

        loadExpenses(date);

        for (int i = 0; i < 12; i++) {
            periodChoiceBox.getItems().add(date.format(FULL_DATE_FORMAT));
            date = date.minusMonths(1);
        }
        periodChoiceBox.getSelectionModel().selectFirst();
    }

    private List<Expense> loadExpenses(LocalDate currentMonth) {

        List<Expense> lastExpenses = ExpenseDAO.findLastExpensesEndingAtCurrentMonth(12, currentMonth);

        if (lastExpenses.isEmpty()) {
            return null;
        }

        pieChart.getData().clear();
        lineChart.getData().clear();

        pieChart.getData().addAll(
                new PieChart.Data("Logement", lastExpenses.getFirst().getHousing()),
                new PieChart.Data("Nourriture", lastExpenses.getFirst().getFood()),
                new PieChart.Data("Sortie", lastExpenses.getFirst().getGoingOut()),
                new PieChart.Data("Transport", lastExpenses.getFirst().getTransportation()),
                new PieChart.Data("Voyage", lastExpenses.getFirst().getTravel()),
                new PieChart.Data("Impôts", lastExpenses.getFirst().getTax()),
                new PieChart.Data("Autres", lastExpenses.getFirst().getOther())
        );

        XYChart.Series<String, Float> seriesHousing = new XYChart.Series<>();
        seriesHousing.setName("Logement");
        XYChart.Series<String, Float> seriesFood = new XYChart.Series<>();
        seriesFood.setName("Nourriture");
        XYChart.Series<String, Float> seriesGoingOut = new XYChart.Series<>();
        seriesGoingOut.setName("Sortie");
        XYChart.Series<String, Float> seriesTransportation = new XYChart.Series<>();
        seriesTransportation.setName("Transport");
        XYChart.Series<String, Float> seriesTravel = new XYChart.Series<>();
        seriesTravel.setName("Voyage");
        XYChart.Series<String, Float> seriesTax = new XYChart.Series<>();
        seriesTax.setName("Impôts");
        XYChart.Series<String, Float> seriesOther = new XYChart.Series<>();
        seriesOther.setName("Autres");

        lastExpenses.stream().sorted(Comparator.comparing(Expense::getDate)).forEach(expense -> {
            seriesHousing.getData().add(new XYChart.Data<>(expense.getDate().format(DATE_FORMAT), expense.getHousing()));
            seriesFood.getData().add(new XYChart.Data<>(expense.getDate().format(DATE_FORMAT), expense.getFood()));
            seriesGoingOut.getData().add(new XYChart.Data<>(expense.getDate().format(DATE_FORMAT), expense.getGoingOut()));
            seriesTransportation.getData().add(new XYChart.Data<>(expense.getDate().format(DATE_FORMAT), expense.getTransportation()));
            seriesTravel.getData().add(new XYChart.Data<>(expense.getDate().format(DATE_FORMAT), expense.getTravel()));
            seriesTax.getData().add(new XYChart.Data<>(expense.getDate().format(DATE_FORMAT), expense.getTax()));
            seriesOther.getData().add(new XYChart.Data<>(expense.getDate().format(DATE_FORMAT), expense.getOther()));
        });

        lineChart.getData().addAll(
                seriesHousing,
                seriesFood,
                seriesGoingOut,
                seriesTransportation,
                seriesTravel,
                seriesTax,
                seriesOther
        );
        return lastExpenses;
    }

    public void changePeriod(ActionEvent actionEvent) {
        var periodSelected = periodChoiceBox.getSelectionModel().getSelectedItem();
        LocalDate dateSelected = LocalDate.parse("01 " + periodSelected, DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        loadExpenses(dateSelected);
        loadExpensesAndIncomes(dateSelected);
    }

    private void loadExpensesAndIncomes(LocalDate currentMonth) {
        LinkedHashMap<String, Pair<Float, Float>> totalsByMonth = new LinkedHashMap<>();
        // Initialisation de la map avec des zéros pour les 12 derniers mois
        LocalDate date = currentMonth.minusMonths(11);
        for (int i = 0; i < 12; i++) {
            totalsByMonth.put(date.format(DateTimeFormatter.ofPattern("MMM yy")), new Pair<>(0f, 0f));
            date = date.plusMonths(1);
        }
        // Récupération des dépenses des 12 derniers mois
        List<Expense> lastExpenses = ExpenseDAO.findLastExpensesEndingAtCurrentMonth(12, currentMonth);
        for (Expense expense : lastExpenses) {
            String monthKey = expense.getDate().format(DateTimeFormatter.ofPattern("MMM yy"));
            Pair<Float, Float> totals = totalsByMonth.getOrDefault(monthKey, new Pair<>(0f, 0f));
            totalsByMonth.put(monthKey, new Pair<>(totals.getKey() + expense.getTotal(), totals.getValue()));
        }
        // Récupération des revenus des 12 derniers mois
        List<Income> lastIncomes = IncomeDAO.findLastIncomesEndingAtCurrentMonth(12, currentMonth);
        for (Income income : lastIncomes) {
            String monthKey = income.getDate().format(DateTimeFormatter.ofPattern("MMM yy"));
            Pair<Float, Float> totals = totalsByMonth.getOrDefault(monthKey, new Pair<>(0f, 0f));
            totalsByMonth.put(monthKey, new Pair<>(totals.getKey(), totals.getValue() + income.getTotal()));
        }
        // Correction pour la déclaration des séries de données
        XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
        expensesSeries.setName("Frais");
        XYChart.Series<String, Number> incomesSeries = new XYChart.Series<>();
        incomesSeries.setName("Salaire");

        // Ajout des données aux séries
        for (Map.Entry<String, Pair<Float, Float>> entry : totalsByMonth.entrySet()) {
            String month = entry.getKey();
            Pair<Float, Float> totals = entry.getValue();
            expensesSeries.getData().add(new XYChart.Data<>(month, totals.getKey()));
            incomesSeries.getData().add(new XYChart.Data<>(month, totals.getValue()));
        }
        barChart.getData().clear();
        barChart.getData().addAll(expensesSeries, incomesSeries);
    }

}
