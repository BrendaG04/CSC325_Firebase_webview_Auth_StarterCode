package com.example.csc325_firebase_webview_auth.view;

import com.example.csc325_firebase_webview_auth.model.Person;
import com.example.csc325_firebase_webview_auth.viewmodel.AccessDataViewModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class AccessFBView {

    @FXML
    private TableView<Person> tableView;
    @FXML
    private TableColumn<Person, String> firstNameCol;
    @FXML
    private TableColumn<Person, String> lastNameCol;
    @FXML
    private TableColumn<Person, String> ageCol;
    @FXML
    private TableColumn<Person, String> deptCol;
    @FXML
    private TableColumn<Person, String> majorCol;
    @FXML
    private TableColumn<Person, String> emailCol;
    @FXML
    private TableColumn<Person, String> imageCol;

    @FXML
    public TextField firstNameField;
    @FXML
    public TextField lastNameField;
    @FXML
    private TextField ageField;
    @FXML
    public TextField deptField;
    @FXML
    public TextField majorField;
    @FXML
    public TextField emailField;
    @FXML
    private TextField imageURLField;
    @FXML
    private ImageView studentImage;
    @FXML
    private Button addButton;

    @FXML
    private Button writeButton;
    @FXML
    private Button readButton;
    @FXML
    private TextArea outputField;
    private boolean key;
    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    private Person person;
    private AccessDataViewModel viewModel;

    public ObservableList<Person> getListOfUsers() {
        return listOfUsers;
    }

    @FXML
    void initialize() {
        viewModel = new AccessDataViewModel();
        firstNameField.textProperty().bindBidirectional(viewModel.userNameProperty());
        majorField.textProperty().bindBidirectional(viewModel.userMajorProperty());

        addButton.disableProperty().bind(viewModel.isWritePossibleProperty().not());

        firstNameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFirstName()));
        lastNameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLastName()));
        ageCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getAge())));
        deptCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDepartment()));
        majorCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMajor()));
        emailCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        imageCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getImageURL()));

        tableView.setItems(listOfUsers);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            } else {
                clearFields();
            }
        });
        readFirebase();
    }

    private void populateFields(Person person) {
        firstNameField.setText(person.getFirstName());
        lastNameField.setText(person.getLastName());
        ageField.setText(String.valueOf(person.getAge()));
        deptField.setText(person.getDepartment());
        majorField.setText(person.getMajor());
        emailField.setText(person.getEmail());
        imageURLField.setText(person.getImageURL());

        if (person.getImageURL() != null && !person.getImageURL().isEmpty()) {
            try {
                Image image;
                String imageURL = person.getImageURL();

                if (imageURL.startsWith("http://") || imageURL.startsWith("https://")) {
                    image = new Image(imageURL, 120, 120, true, true);
                } else {
                    File imageFile = new File(imageURL);
                    if (imageFile.exists()) {
                        image = new Image(new java.io.FileInputStream(imageFile), 120, 120, true, true);
                    } else {
                        studentImage.setImage(null);
                        return;
                    }
                }
                studentImage.setImage(image);
            } catch (Exception e) {
                studentImage.setImage(null);
            }
        } else {
            studentImage.setImage(null);
        }
    }

    @FXML
    private void addRecord(ActionEvent event) {
        addData();
    }

    @FXML
    private void readRecord(ActionEvent event) {
        readFirebase();
    }

    @FXML
    private void regRecord(ActionEvent event) {
        registerUser();
    }

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("/files/WebContainer.fxml");
    }

    public void addData() {
        if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setContentText("First Name and Last Name are required");
            alert.showAndWait();
            return;
        }

        String docId = UUID.randomUUID().toString();
        DocumentReference docRef = App.fstore.collection("References").document(docId);

        int age = 0;
        try {
            age = ageField.getText().isEmpty() ? 0 : Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setContentText("Please enter a valid number for age");
            alert.showAndWait();
            return;
        }

        String imageURL = imageURLField.getText().trim().isEmpty()
                ? "/files/profile_empty.png"
                : imageURLField.getText().trim();

        Map<String, Object> data = new HashMap<>();
        data.put("First Name", firstNameField.getText().trim());
        data.put("Last Name", lastNameField.getText().trim());
        data.put("Age", age);
        data.put("Department", deptField.getText().trim());
        data.put("Major", majorField.getText().trim());
        data.put("Email", emailField.getText().trim());
        data.put("Image", imageURL);

        try {
            ApiFuture<WriteResult> writeResult = docRef.set(data);
            writeResult.get();

            Person p = new Person(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    age,
                    deptField.getText().trim(),
                    majorField.getText().trim(),
                    emailField.getText().trim(),
                    imageURL
            );
            p.setDocumentId(docId);
            listOfUsers.add(p);
            tableView.refresh();
            clearFields();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setContentText("Person added successfully!");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public boolean readFirebase()
    {
        key = false;

        //asynchronously retrieve all documents
        ApiFuture<QuerySnapshot> future =  App.fstore.collection("References").get();
        // future.get() blocks on response
        List<QueryDocumentSnapshot> documents;
        try
        {
            documents = future.get().getDocuments();
            if(documents.size()>0)
            {
                System.out.println("Outing....");
                listOfUsers.clear();
                for (QueryDocumentSnapshot document : documents)
                {
                    System.out.println(document.getId() + " => " + document.getData().get("First Name"));
                    person  = new Person(String.valueOf(document.getData().get("First Name")),
                            String.valueOf(document.getData().get("Last Name")),
                            Integer.parseInt(document.getData().get("Age").toString()),
                            String.valueOf(document.getData().get("Department")),
                            String.valueOf(document.getData().get("Major")),
                            String.valueOf(document.getData().get("Email")),
                            String.valueOf(document.getData().get("Image"))
                    );
                    person.setDocumentId(document.getId());
                    listOfUsers.add(person);
                }
            }
            else
            {
                System.out.println("No data");
            }
            key=true;

        }
        catch (InterruptedException | ExecutionException ex)
        {
            ex.printStackTrace();
        }
        return key;
    }

    public void sendVerificationEmail() {
        try {
            UserRecord user = App.fauth.getUser("name");
            //String url = user.getPassword();

        } catch (Exception e) {
        }
    }

    public boolean registerUser() {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail("user@example.com")
                .setEmailVerified(false)
                .setPassword("secretPassword")
                .setPhoneNumber("+11234567890")
                .setDisplayName("John Doe")
                .setDisabled(false);

        UserRecord userRecord;
        try {
            userRecord = App.fauth.createUser(request);
            System.out.println("Successfully created new user: " + userRecord.getUid());
            return true;

        } catch (FirebaseAuthException ex) {
            // Logger.getLogger(FirestoreContext.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @FXML
    private void handleDelete() {
        Person selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.getDocumentId() != null && !selected.getDocumentId().isEmpty()) {
                try {
                    ApiFuture<WriteResult> writeResult = App.fstore
                            .collection("References")
                            .document(selected.getDocumentId())
                            .delete();
                } catch (Exception e) {
                    System.err.println("Error deleting from Firebase: " + e.getMessage());
                }
            }
            listOfUsers.remove(selected);
            clearFields();
        }
    }


    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        ageField.clear();
        deptField.clear();
        majorField.clear();
        emailField.clear();
        imageURLField.clear();
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/files/profile_empty.png"), 120, 120, true, true);
            studentImage.setImage(defaultImage);
        } catch (Exception e) {
            studentImage.setImage(null);
        }
    }
    @FXML
    private void handleClear() {
        clearFields();
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleEdit() {
        Person selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setFirstName(firstNameField.getText());
            selected.setLastName(lastNameField.getText());
            selected.setAge(Integer.parseInt(ageField.getText().isEmpty() ? "0" : ageField.getText()));
            selected.setDepartment(deptField.getText());
            selected.setMajor(majorField.getText());
            selected.setEmail(emailField.getText());
            selected.setImageURL(imageURLField.getText());

            if (selected.getDocumentId() != null && !selected.getDocumentId().isEmpty()) {
                try {
                    Map<String, Object> data = new HashMap<>();
                    data.put("First Name", selected.getFirstName());
                    data.put("Last Name", selected.getLastName());
                    data.put("Age", selected.getAge());
                    data.put("Department", selected.getDepartment());
                    data.put("Major", selected.getMajor());
                    data.put("Email", selected.getEmail());
                    data.put("Image", selected.getImageURL());

                    ApiFuture<WriteResult> writeResult = App.fstore
                            .collection("References")
                            .document(selected.getDocumentId())
                            .set(data);
                    System.out.println("Updated in Firebase: " + selected.getDocumentId());

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setContentText("Record updated successfully");
                    alert.showAndWait();
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }

            tableView.refresh();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setContentText("Please select a student to edit!");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Student Management System");
        alert.setContentText("CSC 325 JavaFX project\n using JavaFX + CSS.");
        alert.showAndWait();
    }

    @FXML
    private void handleUploadImage() {
        Alert choiceAlert = new Alert(Alert.AlertType.CONFIRMATION);
        choiceAlert.setTitle("Add Image");
        choiceAlert.setContentText("Select from computer or paste a public image URL?");

        javafx.scene.control.ButtonType fileButton = new javafx.scene.control.ButtonType("Select File");
        javafx.scene.control.ButtonType urlButton = new javafx.scene.control.ButtonType("Paste URL");
        javafx.scene.control.ButtonType cancelButton = new javafx.scene.control.ButtonType("Cancel");

        choiceAlert.getButtonTypes().setAll(fileButton, urlButton, cancelButton);

        java.util.Optional<javafx.scene.control.ButtonType> result = choiceAlert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == fileButton) {
                selectImageFromFile();
            } else if (result.get() == urlButton) {
                pasteImageURL();
            }
        }
    }

    private void selectImageFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");

        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp");
        fileChooser.getExtensionFilters().add(imageFilter);

        File selectedFile = fileChooser.showOpenDialog(imageURLField.getScene().getWindow());

        if (selectedFile != null) {
            try {
                String filePath = selectedFile.getAbsolutePath();
                Image image = new Image(new java.io.FileInputStream(selectedFile), 120, 120, true, true);

                imageURLField.setText(filePath);
                studentImage.setImage(image);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Image selected: " + selectedFile.getName());
                alert.showAndWait();

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void pasteImageURL() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Paste Image URL");
        dialog.setHeaderText("Enter public image URL");
        dialog.setContentText("Image URL:");

        java.util.Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String imageUrl = result.get().trim();
            try {
                imageURLField.setText(imageUrl);

                Image image = new Image(imageUrl, 120, 120, true, true);
                studentImage.setImage(image);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Image URL added successfully!");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Failed to load image from URL. Make sure it's a valid public image URL.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Logout");
        confirmAlert.setHeaderText("Are you sure?");
        confirmAlert.setContentText("Do you want to logout?");

        java.util.Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            try {
                clearFields();
                tableView.getSelectionModel().clearSelection();
                listOfUsers.clear();

                App.setRoot("/files/login.fxml");
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
