package com.example.financemanager.db;

import com.example.financemanager.model.Expense;
import com.example.financemanager.model.Income;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncomeDAO {

    private static final Logger log = LoggerFactory.getLogger(IncomeDAO.class);

    private static final String tableName = "income";
    private static final String dateColumn = "date";
    private static final String salaryColumn = "salary";
    private static final String aidsColumn = "aids";
    private static final String freelanceColumn = "freelance";
    private static final String passiveColumn = "passive";
    private static final String otherColumn = "other";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final ObservableList<Income> incomes;

    static {
        incomes = FXCollections.observableArrayList();
        fetchAllDataFromDB();
    }

    public static ObservableList<Income> getIncomes() {
        return FXCollections.unmodifiableObservableList(incomes.sorted(Income::compareTo));
    }

    private static void fetchAllDataFromDB() {

        String query = "SELECT * FROM " + tableName;

        try (Connection connection = Database.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            incomes.clear();
            while (rs.next()) {
                incomes.add(new Income(
                        LocalDate.parse(rs.getString(dateColumn), DATE_FORMAT),
                        rs.getFloat(salaryColumn),
                        rs.getFloat(aidsColumn),
                        rs.getFloat(freelanceColumn),
                        rs.getFloat(passiveColumn),
                        rs.getFloat(otherColumn)));
            }
        } catch (SQLException e) {
            log.error("Could not load Expenses from database", e);
            incomes.clear();
        }
    }

    public static void insertIncome(Income income) {
        //update database
        String query = "INSERT INTO income(date, salary, aids, freelance, passive, other) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = Database.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, income.getDate().format(DATE_FORMAT));
            statement.setFloat(2, income.getSalary());
            statement.setFloat(3, income.getAids());
            statement.setFloat(4, income.getFreelance());
            statement.setFloat(5, income.getPassive());
            statement.setFloat(6, income.getOther());

            statement.executeUpdate();
        } catch (SQLException e) {
            log.error("Could not insert Incomes in database", e);
        }

        //update cache
        incomes.add(income);
    }

    public static List<Income> findLastIncomesEndingAtCurrentMonth(int numberOfLine, LocalDate currentMonth) {
        String query = "SELECT * FROM " + tableName
                + " WHERE " + dateColumn + " <= '" + currentMonth.format(DATE_FORMAT)
                + "' ORDER BY " + dateColumn + " DESC LIMIT " + numberOfLine;

        List<Income> lastIncomes = new ArrayList<>();

        try (Connection connection = Database.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                lastIncomes.add(new Income(
                        LocalDate.parse(rs.getString(dateColumn), DATE_FORMAT),
                        rs.getFloat(salaryColumn),
                        rs.getFloat(aidsColumn),
                        rs.getFloat(freelanceColumn),
                        rs.getFloat(passiveColumn),
                        rs.getFloat(otherColumn)));
            }
        } catch (SQLException e) {
            log.error("Could not load Incomes from database", e);
        }
        return lastIncomes;
    }

    public static void deleteIncome(Income selectedIncome) {
        //update database
        String query = "DELETE FROM " + tableName + " WHERE " + dateColumn + " = ?";

        try (Connection connection = Database.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, selectedIncome.getDate().format(DATE_FORMAT));

            statement.executeUpdate();
        } catch (SQLException e) {
            log.error("Could not delete Income from database", e);
        }

        //update cache
        incomes.remove(selectedIncome);
    }
}