package app.barbman.core.controller.salary;

import app.barbman.core.dto.SalaryDTO;
import app.barbman.core.model.Salary;
import app.barbman.core.model.User;
import app.barbman.core.repositories.expense.ExpenseRepositoryImpl;
import app.barbman.core.repositories.users.UsersRepository;
import app.barbman.core.repositories.users.UsersRepositoryImpl;
import app.barbman.core.repositories.expense.ExpenseRepository;;
import app.barbman.core.repositories.performedservice.PerformedServiceRepository;
import app.barbman.core.repositories.performedservice.PerformedServiceRepositoryImpl;
import app.barbman.core.repositories.salaries.SalariesRepository;
import app.barbman.core.repositories.salaries.SalariesRepositoryImpl;
import app.barbman.core.service.sueldos.SueldosService;
import app.barbman.core.util.NumberFormatterUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * IDK WHAT THE FUCK THIS DOES, I WROTE THIS LIKE A MONTH AGO
 */
public class ConfirmSalaryController implements Initializable {

    @FXML
    private ChoiceBox<String> paymentTypeChoiceBox;
    @FXML
    private TextField bonusField;
    @FXML
    private TextField manualAmountField;
    @FXML
    private Label lblProduction;
    @FXML
    private Label lblSalaryAdvance;
    @FXML
    private Label lblFinalAmount;
    @FXML
    private Button saveBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private VBox manualMontoBox; // monto manual


    private static final Logger logger = LogManager.getLogger(ConfirmSalaryController.class);
    private static final String PREFIX = "[CONFIRM-SALARY]";

    private final SalariesRepository salariesRepository = new SalariesRepositoryImpl();
    private final PerformedServiceRepository performedServiceRepository = new PerformedServiceRepositoryImpl();
    private final ExpenseRepository expenseRepository = new ExpenseRepositoryImpl();
    private final UsersRepository usersRepository = new UsersRepositoryImpl();
    private final SueldosService sueldosService = new SueldosService(salariesRepository, performedServiceRepository, expenseRepository);
    private SueldosController parentController;

    // User y sueldo seleccionado desde la tabla principal
    private User user;
    private SalaryDTO salaryDTO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        paymentTypeChoiceBox.setItems(FXCollections.observableArrayList("efectivo", "transferencia"));
        paymentTypeChoiceBox.setValue("efectivo");

        // This hides the "manual amount" box by default. It's shown only for paymentType = 0
        manualMontoBox.setVisible(false);
        manualMontoBox.setManaged(false);

        // Format the manualAmountField
        NumberFormatterUtil.applyToTextField(manualAmountField);

        // Formateador para bono
        NumberFormatterUtil.applyToTextField(bonusField);

    }

    public void setSueldoDTO(SalaryDTO dto) {
        this.salaryDTO = dto;
        this.user = usersRepository.findById(dto.getUserId());

        // rango semanal
        java.time.LocalDate hoy = java.time.LocalDate.now();
        java.time.LocalDate lunes = hoy.with(java.time.DayOfWeek.MONDAY);
        java.time.LocalDate sabado = lunes.plusDays(5);

        double adelantos = expenseRepository.getTotalAdelantos(dto.getUserId(), lunes, sabado);

        lblProduction.setText("Producción: " + NumberFormatterUtil.format(dto.getTotalProduction()) + " Gs");
        lblSalaryAdvance.setText("Adelantos: " + NumberFormatterUtil.format(adelantos) + " Gs");
        lblFinalAmount.setText("Salary final: " + NumberFormatterUtil.format(dto.getAmountPaid()) + " Gs");

        if (user != null && user.getPaymentType() == 0) {
            manualMontoBox.setVisible(true);
            manualMontoBox.setManaged(true);
            logger.info("[SUELDOS-PAGO] Tipo de cobro = 0, habilitado campo de monto manual.");
        }

    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    public void onPay() {
        try {
            if (user == null) {
                throw new IllegalArgumentException("Can't find that user");
            }

            double bonus = 0;
            if (!bonusField.getText().isBlank()) {
                bonus = Double.parseDouble(bonusField.getText().replace(".", ""));
            }

            // manualAmount initializes on -1 to indicate "not set"
            // if >= 0, then use it to save that amount
            double manualAmount = -1;
            if (manualAmountField.getText().isBlank()) {
                throw new IllegalArgumentException("You have to enter a manual amount.");
            }
            manualAmount = Double.parseDouble(manualAmountField.getText().replace(".", ""));

            Salary salary = sueldosService.calcularSueldo(user, bonus);
            if (manualAmount >= 0) { // I'm setting this on frontend ?? I'm too bad at this
                salary.setAmountPaid(manualAmount);
            }

            int formaPago = paymentTypeChoiceBox.getValue(); // TODO: set this to int based on choicebox value
            // Guardar pago con bonus
            sueldosService.pagarSueldo(salary, formaPago, bonus);

            logger.info("{} Salary registered successfully -> User: {}, Amount: {}, PaymentMethod: {}",
                    PREFIX ,user.getName(), salary.getAmountPaid(), formaPago);

        } catch (Exception e) {
            logger.error("{} Error registering salary: {}", PREFIX, e.getMessage(), e);
            mostrarAlerta("Error registering salary: " + e.getMessage());
        }

        if (parentController != null) {
            parentController.recargarTabla();
        }

        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    public void setParentController(SueldosController parent) {
        this.parentController = parent;
    }

    private void mostrarAlerta(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
