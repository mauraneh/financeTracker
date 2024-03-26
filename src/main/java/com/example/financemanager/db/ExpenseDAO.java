package com.example.financemanager.db;

import com.example.financemanager.model.Expense;
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

public class ExpenseDAO {

    private static final Logger log = LoggerFactory.getLogger(ExpenseDAO.class);

    private static final String tableName = "expense";
    private static final String dateColumn = "date";
    private static final String housingColumn = "housing";
    private static final String foodColumn = "food";
    private static final String goingOutColumn = "goingOut";
    private static final String transportationColumn = "transportation";
    private static final String travelColumn = "travel";
    private static final String taxColumn = "tax";
    private static final String otherColumn = "other";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final ObservableList<Expense> expenses;

    static {
        expenses = FXCollections.observableArrayList();
        fetchAllDataFromDB();
    }

    public static ObservableList<Expense> getExpenses() {
        return FXCollections.unmodifiableObservableList(expenses.sorted(Expense::compareTo));
    }

    private static void fetchAllDataFromDB() {

        String query = "SELECT * FROM " + tableName;

        try (Connection connection = Database.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            expenses.clear();
            while (rs.next()) {
                expenses.add(new Expense(
                        LocalDate.parse(rs.getString(dateColumn), DATE_FORMAT),
                        rs.getFloat(housingColumn),
                        rs.getFloat(foodColumn),
                        rs.getFloat(goingOutColumn),
                        rs.getFloat(transportationColumn),
                        rs.getFloat(travelColumn),
                        rs.getFloat(taxColumn),
                        rs.getFloat(otherColumn)));
            }
        } catch (SQLException e) {
            log.error("Could not load Expenses from database", e);
            expenses.clear();
        }
    }

    public static void insertExpense(Expense expense) {
        //update database
        String query = "INSERT INTO expense(date, housing, food, goingOut, transportation, travel, tax, other) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = Database.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, expense.getDate().format(DATE_FORMAT));
            statement.setFloat(2, expense.getHousing());
            statement.setFloat(3, expense.getFood());
            statement.setFloat(4, expense.getGoingOut());
            statement.setFloat(5, expense.getTransportation());
            statement.setFloat(6, expense.getTravel());
            statement.setFloat(7, expense.getTax());
            statement.setFloat(8, expense.getOther());

            statement.executeUpdate();
        } catch (SQLException e) {
            log.error("Could not insert Expenses in database", e);
        }

        //update cache
        expenses.add(expense);
    }

    public static List<Expense> findLastExpensesEndingAtCurrentMonth(int numberOfLine, LocalDate currentMonth) {
        String query = "SELECT * FROM " + tableName
                + " WHERE " + dateColumn + " <= '" + currentMonth.format(DATE_FORMAT)
                + "' ORDER BY " + dateColumn + " DESC LIMIT " + numberOfLine;

        List<Expense> lastExpenses = new ArrayList<>();

        try (Connection connection = Database.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                lastExpenses.add(new Expense(
                        LocalDate.parse(rs.getString(dateColumn), DATE_FORMAT),
                        rs.getFloat(housingColumn),
                        rs.getFloat(foodColumn),
                        rs.getFloat(goingOutColumn),
                        rs.getFloat(transportationColumn),
                        rs.getFloat(travelColumn),
                        rs.getFloat(taxColumn),
                        rs.getFloat(otherColumn)));
            }
        } catch (SQLException e) {
            log.error("Could not load Expenses from database", e);
        }
        return lastExpenses;
    }

    public static void deleteExpense(Expense selectedExpense) {
        String query = "DELETE FROM " + tableName + " WHERE " + dateColumn + " = ?";

        try (Connection connection = Database.connect()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, selectedExpense.getDate().format(DATE_FORMAT));
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error("Could not delete Expense from database", e);
        }

        expenses.remove(selectedExpense);
    }
}