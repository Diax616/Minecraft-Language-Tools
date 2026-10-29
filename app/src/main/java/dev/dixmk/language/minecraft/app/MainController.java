package dev.dixmk.language.minecraft.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import dev.dixmk.language.minecraft.logic.LangDiff;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainController {
	@FXML private ToggleGroup taskToggleGroup;
	@FXML private RadioButton pruneRadioButton;
	@FXML private CheckBox emptyValuesCheckBox;
	@FXML private CheckBox sourceOrderCheckBox;
	@FXML private CheckBox inPlaceCheckBox;
	@FXML private Button startButton;
	@FXML private TextField sourceTextField;
	@FXML private TextField targetTextField;
	@FXML private TextField resultTextField;
	@FXML private Button saveResultJson;
	@FXML private TextArea outputTextArea;

	private Path sourcePath;
	private Path targetPath;
	private Path resultPath;
	private Path lastDirectory;
	
	private HostServices hostServices;
	@FXML
	private void initialize() {
		taskToggleGroup.selectedToggleProperty().addListener((obs, old, selected) -> refresh());
		inPlaceCheckBox.selectedProperty().addListener((obs, old, selected) -> refresh());
		refresh();
	}

	@FXML
	private void onActionBrowseSourceJson(ActionEvent event) {
		Path path = chooseJson(event, "Select source lang JSON", false);
		if (path != null) {
			sourcePath = path;
			sourceTextField.setText(path.toString());
			refresh();
		}
	}

	@FXML
	private void onActionBrowseTargetJson(ActionEvent event) {
		Path path = chooseJson(event, "Select target lang JSON", false);
		if (path != null) {
			targetPath = path;
			targetTextField.setText(path.toString());
			refresh();
		}
	}

	@FXML
	private void onActionSaveResultJson(ActionEvent event) {
		Path path = chooseJson(event, "Save result as", true);
		if (path != null) {
			resultPath = path;
			resultTextField.setText(path.toString());
			refresh();
		}
	}

	@FXML
	private void onActionStartButton() {
		try {
			String summary;
			if (isPrune()) {
				Path output = isInPlace() ? targetPath : resultPath;
				summary = LangDiff.prune(sourcePath, targetPath, output, sourceOrderCheckBox.isSelected());
			} else {
				summary = LangDiff.missing(sourcePath, targetPath, resultPath, emptyValuesCheckBox.isSelected());
			}
			outputTextArea.setText(summary);
		} catch (IOException e) {
			outputTextArea.setText("error: " + e.getMessage());
		}
	}

	@FXML
	private void onActionAboutMenuItem() {	
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("About");
		alert.setHeaderText("MC Language Tools");
		alert.setContentText("Compare Minecraft lang files: find the keys a translation is missing, "
				+ "or prune the ones the source no longer has.");
		alert.setOnShown(event -> {
		    Stage alertStage = (Stage) alert.getDialogPane()
		                                    .getScene()
		                                    .getWindow();
		    alertStage.getIcons().add(Icons.WARDEN_WOMAN);
		});
		alert.showAndWait();
	}

	@FXML
	private void onActionAboutAutorItem() {
		Hyperlink github = new Hyperlink("https://github.com/Diax616/Minecraft-Language-Tools");
		github.setOnAction(e -> hostServices.showDocument(github.getText()));
		Hyperlink deviantart = new Hyperlink("https://www.deviantart.com/dixmk");
		deviantart.setOnAction(e -> hostServices.showDocument(deviantart.getText()));			
		VBox content = new VBox(8, new Label("DixMK"), new Label("Java programmer and cheap artist that's rooted in the pregnancy fetish."), github, deviantart);
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Author");
		alert.setHeaderText("MC Language Tools");
		alert.getDialogPane().setContent(content);
		alert.setOnShown(event -> {
		    Stage alertStage = (Stage) alert.getDialogPane()
		                                    .getScene()
		                                    .getWindow();
		    alertStage.getIcons().add(Icons.WARDEN_WOMAN);
		});
		alert.showAndWait();
	}
	
	private boolean isPrune() {
		return taskToggleGroup.getSelectedToggle() == pruneRadioButton;
	}

	private boolean isInPlace() {
		return isPrune() && inPlaceCheckBox.isSelected();
	}

	/** Show the options of the selected task and enable Start only when it can run. */
	private void refresh() {
		boolean prune = isPrune();
		show(emptyValuesCheckBox, !prune);
		show(sourceOrderCheckBox, prune);
		show(inPlaceCheckBox, prune);

		boolean inPlace = isInPlace();
		resultTextField.setDisable(inPlace);
		saveResultJson.setDisable(inPlace);

		startButton.setDisable(sourcePath == null || targetPath == null || (!inPlace && resultPath == null));
	}

	private static void show(Node node, boolean shown) {
		node.setVisible(shown);
		node.setManaged(shown);
	}

	private Path chooseJson(ActionEvent event, String title, boolean save) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle(title);
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
		if (lastDirectory != null && Files.isDirectory(lastDirectory)) {
			chooser.setInitialDirectory(lastDirectory.toFile());
		}

		Window window = ((Node) event.getSource()).getScene().getWindow();
		File file = save ? chooser.showSaveDialog(window) : chooser.showOpenDialog(window);
		if (file == null) {
			return null;
		}

		Path path = file.toPath();
		if (path.getParent() != null) {
			lastDirectory = path.getParent();
		}
		return path;
	}
	
	public void setHostServices(HostServices hostServices) {
	    this.hostServices = hostServices;
	}
}
