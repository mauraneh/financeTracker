package com.example.financemanager.controller;

import com.example.financemanager.FinanceTrackerApplication;
import com.example.financemanager.model.Income;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class IncomeDialog extends Dialog<Income> {
    @FXML
    private TextField dateField;

    @FXML
    private TextField salaryField;

    @FXML
    private TextField aidsField;

    @FXML
    private TextField freelanceField;

    @FXML
    private TextField passiveField;

    @FXML
    private TextField otherField;

    @FXML
    private ButtonType createButton;

    public IncomeDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(FinanceTrackerApplication.class.getResource("income-dialog.fxml"));
            loader.setController(this);

            DialogPane dialogPane = loader.load();

            setTitle("Ajouter des revenus");
            setDialogPane(dialogPane);
            initResultConverter();

            // Disable button when all fields are not filled
            computeIfButtonIsDisabled();

            // Ensure only numeric input in the fields
            forceDoubleFormat();

            forceDateFormat();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void forceDateFormat() {
        UnaryOperator<TextFormatter.Change> dateValidationFormatter = t -> {
            if (t.isAdded()) {
                if (t.getControlText().length() > 8) {
                    t.setText("");
                } else if (t.getControlText().matches(".*[0-9]{2}")) {
                    if (t.getText().matches("[^/]")) {
                        t.setText("");
                    }
                } else if (t.getText().matches("[^0-9]")) {
                    t.setText("");
                }
            }
            return t;
        };
        dateField.setTextFormatter(new TextFormatter<>(dateValidationFormatter));
    }

    private void forceDoubleFormat() {
        UnaryOperator<TextFormatter.Change> numberValidationFormatter = t -> {
            if (t.isReplaced())
                if(t.getText().matches("[^0-9]"))
                    t.setText(t.getControlText().substring(t.getRangeStart(), t.getRangeEnd()));

            if (t.isAdded()) {
                if (t.getControlText().contains(".")) {
                    if (t.getText().matches("[^0-9]")) {
                        t.setText("");
                    }
                } else if (t.getText().matches("[^0-9.]")) {
                    t.setText("");
                }
            }
            return t;
        };
        salaryField.setTextFormatter(new TextFormatter<>(numberValidationFormatter));
        aidsField.setTextFormatter(new TextFormatter<>(numberValidationFormatter));
        freelanceField.setTextFormatter(new TextFormatter<>(numberValidationFormatter));
        passiveField.setTextFormatter(new TextFormatter<>(numberValidationFormatter));
        otherField.setTextFormatter(new TextFormatter<>(numberValidationFormatter));
    }

    private void computeIfButtonIsDisabled() {
        getDialogPane().lookupButton(createButton).disableProperty().bind(
                Bindings.createBooleanBinding(() -> dateField.getText().trim().isEmpty(), dateField.textProperty())
                        .or(Bindings.createBooleanBinding(() -> salaryField.getText().trim().isEmpty(), salaryField.textProperty())
                                .or(Bindings.createBooleanBinding(() -> aidsField.getText().trim().isEmpty(), aidsField.textProperty())
                                        .or(Bindings.createBooleanBinding(() -> freelanceField.getText().trim().isEmpty(), freelanceField.textProperty())
                                                .or(Bindings.createBooleanBinding(() -> passiveField.getText().trim().isEmpty(), passiveField.textProperty())
                                                        .or(Bindings.createBooleanBinding(() -> otherField.getText().trim().isEmpty(), otherField.textProperty())
                                                        ))))));
    }

    private void initResultConverter() {
        setResultConverter(buttonType -> {
            if (!Objects.equals(ButtonBar.ButtonData.OK_DONE, buttonType.getButtonData())) {
                return null;
            }

            return new Income(
                    LocalDate.parse(dateField.getText(), DateTimeFormatter.ofPattern("dd/MM/yy")),
                    Float.parseFloat(salaryField.getText()),
                    Float.parseFloat(aidsField.getText()),
                    Float.parseFloat(freelanceField.getText()),
                    Float.parseFloat(passiveField.getText()),
                    Float.parseFloat(otherField.getText())
            );
        });
    }

    @FXML
    private void initialize() {

    }
}
