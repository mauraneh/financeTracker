package com.example.financemanager.controller;

import com.example.financemanager.db.ExpenseDAO;
import com.example.financemanager.db.IncomeDAO;
import com.example.financemanager.model.Expense;
import com.example.financemanager.model.Income;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;

import java.util.Optional;

import static com.example.financemanager.db.Database.log;

public class IncomeController {

    @FXML
    private TableView<Income> incomeTable;

    @FXML
    public void initialize() {
        incomeTable.setItems(IncomeDAO.getIncomes());
    }
    
    @FXML
    public void addIncome(ActionEvent event) {
        Dialog<Income> addPersonDialog = new IncomeDialog();
        Optional<Income> result = addPersonDialog.showAndWait();
        result.ifPresent(IncomeDAO::insertIncome);

        log.debug(result.toString());
        event.consume();
    }}
