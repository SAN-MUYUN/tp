package seedu.address.ui;


import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonContainsKeywordsPredicate;
import seedu.address.ui.modulefolders.ModuleFolders;
import seedu.address.ui.personlist.PersonListPanel;
import seedu.address.ui.topnav.HelpWindow;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> implements GuiFunctionHandler {

    private static final String FXML = "MainWindow.fxml";
    private static final String MODULE_PRESS_RESPONSE_PREFIX =
            "Viewing contacts list filtered by module code";
    private static final String FAVOURITE_FILE_PRESS_RESPONSE =
            "Viewing contacts list filtered by favourite.";
    private static final String FILTER_CLEAR_RESPONSE =
            "Displaying all contacts.";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;
    private ModuleFolders moduleFolders;
    private Sidebar sidebar;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    @FXML
    private VBox sidebarPlaceholder;

    @FXML
    private StackPane switchWindowPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage} and {@code Logic}.
     */
    public MainWindow(Stage primaryStage, Logic logic) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow();

        setTabEvent();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the Tab event functionality in MainWindow.
     */
    private void setTabEvent() {
        // Handles the bug reported below.
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl
                    && event.getCode() == KeyCode.TAB) {
                handleSwitchTab(event);
                event.consume();
            }
        });
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in later version of SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField, and
         * ResultDisplay contains a TextArea, thus some accelerators (e.g F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add following event filter to capture such key events and open
         * help window purposely so to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(logic.getAddressBookFilePath());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());

        sidebar = new Sidebar(this);
        sidebarPlaceholder.getChildren().add(sidebar.getRoot());

        personListPanel = new PersonListPanel(logic.getFilteredPersonList());

        moduleFolders = new ModuleFolders(logic.getUnfilteredPersonList(), this);

        setSwitchWindowPlaceholder("Modules");

        resultDisplay.setFeedbackToUser("Hi! Welcome to AcademySource.");
    }

    @Override
    public void setSwitchWindowPlaceholder(String selectedButton) {

        boolean isValidState = selectedButton.equals("Contacts") || selectedButton.equals("Modules");
        assert isValidState : "an invalid button state is passed into selectedButton.";

        switchWindowPlaceholder.getChildren().clear();

        if (selectedButton.equals("Modules")) {
            switchWindowPlaceholder.getChildren().add(moduleFolders.getRoot());
            sidebar.changeButtonState("Modules");
        } else {
            switchWindowPlaceholder.getChildren().add(personListPanel.getRoot());
            sidebar.changeButtonState("Contacts");
        }
    }

    @Override
    public void filterListByModuleCode(String moduleCode) {
        List<String> moduleCodeList = new ArrayList<>();
        moduleCodeList.add(moduleCode);
        Map<PersonContainsKeywordsPredicate.SearchField, List<String>> searchFieldMap = new HashMap<>();
        searchFieldMap.put(PersonContainsKeywordsPredicate.SearchField.MODULE, moduleCodeList);
        logic.updatePredicateViaGui(
                new PersonContainsKeywordsPredicate(searchFieldMap)
        );
        String modulePressResponse = String.format("%s %s.", MODULE_PRESS_RESPONSE_PREFIX, moduleCode);
        resultDisplay.setFeedbackToUser(modulePressResponse);
    }

    @Override
    public void filterListByFavourites() {
        logic.updatePredicateViaGui(Person::getIsFavourite);
        resultDisplay.setFeedbackToUser(FAVOURITE_FILE_PRESS_RESPONSE);
    }

    @Override
    public void clearFilter() {
        logic.updatePredicateViaGui(PREDICATE_SHOW_ALL_PERSONS);
        resultDisplay.setFeedbackToUser(FILTER_CLEAR_RESPONSE);
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    @FXML
    private void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            if (!commandResult.isShowHelp()) {
                this.setSwitchWindowPlaceholder("Contacts");
            }
            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        }
    }

    /**
     * Handles the tab event which triggers the tab switching functionality.
     * Tab will act as a toggle between Modules tab and Contacts tab.
     *
     * @param event the key press event.
     */
    @FXML
    private void handleSwitchTab(KeyEvent event) {
        if (event.getCode() != KeyCode.TAB) {
            return;
        }

        if (sidebar.getSelectedButtonText().equals("Contacts")) {
            setSwitchWindowPlaceholder("Modules");
        } else {
            setSwitchWindowPlaceholder("Contacts");

            // ensure consistent behaviour between tab press and button press contact
            logic.updatePredicateViaGui(PREDICATE_SHOW_ALL_PERSONS);
            resultDisplay.setFeedbackToUser(FILTER_CLEAR_RESPONSE);
        }
    }
}
