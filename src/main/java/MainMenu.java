import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.controlsfx.control.SearchableComboBox;

import java.util.ArrayList;

public class MainMenu {

    ArrayList<Professor> professors = new ArrayList<>();
    ArrayList<Group> groups = new ArrayList<>();
    ArrayList<Activity> activities = new ArrayList<>();
    ArrayList<Room> rooms = new ArrayList<>();
    int years;
    Scenes scenes;

    public void createMainMenu() {

        Stage mainStage = new Stage();

        GridPane mainGrid = new GridPane();
        mainGrid.setPadding(new Insets(20, 20, 20, 20));
        mainGrid.setAlignment(Pos.CENTER);
        mainGrid.setVgap(15);
        mainGrid.setHgap(20);

        Scene mainScene = new Scene(mainGrid);

        Button readFile = new Button("Citește fișierul stat de funcțiuni");
        readFile.setPrefSize(300, 30);

        ComboBox<Integer> semesterCombo = new ComboBox<>();
        semesterCombo.getItems().add(1);
        semesterCombo.getItems().add(2);
        semesterCombo.setPrefSize(100, 30);

        SearchableComboBox<String> profCombo=new SearchableComboBox<>();
        profCombo.setPrefSize(300,30);

        Button chooseProfesor=new Button("Alege profesorul");
        chooseProfesor.setPrefSize(300,30);

        SearchableComboBox<String> groupCombo=new SearchableComboBox<>();
        groupCombo.setPrefSize(300,30);

        Button chooseGroup=new Button("Alege grupa");
        chooseGroup.setPrefSize(300,30);

        Button saveData=new Button("Salvare date");
        saveData.setPrefSize(300,30);

        Button loadData=new Button("Încărcare date");
        loadData.setPrefSize(300,30);

        Button exit = new Button("Închidere");
        exit.setPrefSize(300, 30);

        ComboBox<Integer> yearCombo = new ComboBox<>();
        yearCombo.getItems().add(1);
        yearCombo.setPrefSize(300, 30);

        Button chooseYear = new Button("Alege anul de studiu");
        chooseYear.setPrefSize(300, 30);

        HBox semesterChooser = new HBox();
        semesterChooser.setSpacing(10);

        Button semesterText = new Button("Alege semestru");
        semesterText.setPrefSize(190, 30);

        Button verificareOrar = new Button("Verificare orar");
        verificareOrar.setPrefSize(300, 30);

        Button addRoom = new Button("Adăugare sală");
        addRoom.setPrefSize(300, 30);

        Button nameGroups = new Button("Redenumire grupe");
        nameGroups.setPrefSize(300, 20);

        Button writeFile = new Button("Exportă orarul");
        writeFile.setPrefSize(300, 20);

        mainScene.setOnKeyTyped(event -> {
            if (event.getCharacter().equals("m")) {
                mainStage.toFront();
            }
        });

        readFile.setOnAction(event -> {

            professors.clear();
            groups.clear();
            rooms.clear();
            if (!profCombo.getItems().isEmpty()) profCombo.getItems().clear();
            if (!groupCombo.getItems().isEmpty()) groupCombo.getItems().clear();
            if (!yearCombo.getItems().isEmpty()) yearCombo.getItems().clear();

            Pair<String,String> dataPair=Utility.readFile();
            if (dataPair!=null) {
                String file=dataPair.getKey();
                String faculty=dataPair.getValue();
                activities = Utility.readXls(file, professors, groups, rooms, faculty);
            }
            else {
                Utility.message("Format fișier necorespunzător");
                return;
            }

            if (activities!=null&&activities.size()>0)
                years = Utility.maxYear(activities);
            else
                years=0;
            if (activities!=null&&activities.size()>0) {
                for (Professor professor : professors) {
                    profCombo.getItems().add(professor.getName());
                }
                if (professors.size()>0)
                    profCombo.setValue(professors.get(0).getName());
                for (Group group:groups){
                    groupCombo.getItems().add(group.getGroupName());
                }
                if (groups.size()>0)
                    groupCombo.setValue(groups.get(0).getGroupName());
                for (int i=0;i<years;i++) {
                    yearCombo.getItems().add(i+1);
                }
                if (years>0)
                    yearCombo.setValue(1);
                semesterCombo.setValue(1);
                scenes =new Scenes(professors,activities,groups,rooms);
                Utility.message("Datele au fost citite cu succes");
            }
            else Utility.message("Citire date eșuată");
        });

        writeFile.setOnAction(event -> {
            String fileName = Utility.saveFile();
            Utility.writeXls(fileName, professors, groups, activities, rooms, semesterCombo.getValue());
        });

        loadData.setOnAction(event -> {

            String fileName = Utility.openFile();
            if (fileName==null) {
                return;
            }
            activities=Utility.loadActivities(fileName+"act");
            if (activities==null) return;
            professors=Utility.loadProfessors(fileName+"prf");
            if (professors==null) return;
            groups=Utility.loadGroups(fileName+"grp");
            if (groups==null) return;
            rooms=Utility.loadRooms(fileName+"rms");
            if (rooms==null) return;
            years = Utility.maxYear(activities);
            if (activities!=null) {
                profCombo.getItems().clear();
                groupCombo.getItems().clear();
                yearCombo.getItems().clear();

                for (Professor professor : professors) {
                    profCombo.getItems().add(professor.getName());
                }
                if (professors.get(0)!=null)
                    profCombo.setValue(professors.get(0).getName());
                for (Group group:groups){
                    groupCombo.getItems().add(group.getGroupName());
                }
                if (groups.get(0)!=null)
                    groupCombo.setValue(groups.get(0).getGroupName());
                for (int i=0;i<years;i++) {
                    yearCombo.getItems().add(i+1);
                }
                if (years>0)
                    yearCombo.setValue(1);
                semesterCombo.setValue(1);
                scenes =new Scenes(professors,activities,groups,rooms);
                Utility.message("Datele au fost încărcate cu succes");
            } else Utility.message("Încărcare date eșuată");
        });

        saveData.setOnAction(event -> {

            String fileName = Utility.saveFile();
            if (Utility.saveData(fileName, professors, groups, activities, rooms))
                Utility.message("Salvare reușită");
        });

        addRoom.setOnAction(event -> {
            if (scenes != null) {
                scenes.addRoom();
            }
        });

        nameGroups.setOnAction(event -> {
            if (scenes != null) {
                scenes.renameGroup();
            }
        });

        verificareOrar.setOnAction(event -> {
            if (activities.size() > 0) {
                if (semesterCombo.getValue() != null) {
                    scenes.verifySchedule(semesterCombo.getValue());
                } else {
                    Utility.message("Selectați vă rog un semestru");
                }
            } else {
                Utility.message("Nu există date");
            }
        });

        chooseProfesor.setOnAction(event -> {
            try {
                int indexSelected = profCombo.getSelectionModel().getSelectedIndex();
                int semester = semesterCombo.getSelectionModel().getSelectedItem();
                if (scenes.searchForStage(professors.get(indexSelected).getName()) == null) {
                    scenes.professorsScheduleScene(indexSelected, semester);
                }
            } catch (Exception ex) {
                Utility.message("Generare orar eșuată");
            }
        });

        chooseYear.setOnAction(event -> {
            try{
                int yearSelected=yearCombo.getSelectionModel().getSelectedItem();
                int semester=semesterCombo.getSelectionModel().getSelectedItem();
                if (scenes.searchForStage("Orar anul "+yearSelected)==null) {
                    scenes.yearScheduleScene(yearSelected, semester);
                }
            }
            catch (Exception ex){
                Utility.message("Generare orar eșuată");
            }
        });

        chooseGroup.setOnAction(event -> {
            try{
                int indexSelected=groupCombo.getSelectionModel().getSelectedIndex();
                int semester=semesterCombo.getSelectionModel().getSelectedItem();
                if (scenes.searchForStage(groups.get(indexSelected).getGroupName())==null) {
                    scenes.groupsScheduleScene(indexSelected, semester);
                }
            }
            catch (Exception ex){
                Utility.message("Generare orar eșuată");
            }
        });

        exit.setOnAction(event -> {
            Platform.exit();
            System.exit(0);
        });

        mainGrid.add(readFile, 1, 1);
        mainGrid.add(writeFile, 2, 1);
        mainGrid.add(loadData, 1, 2);
        mainGrid.add(saveData, 2, 2);
        mainGrid.add(addRoom, 1, 3);
        mainGrid.add(nameGroups, 2, 3);
        mainGrid.add(semesterChooser, 1, 4);
        semesterChooser.getChildren().addAll(semesterText, semesterCombo);
        mainGrid.add(verificareOrar, 2, 4);
        mainGrid.add(profCombo, 1, 5);
        mainGrid.add(chooseProfesor, 2, 5);
        mainGrid.add(yearCombo, 1, 6);
        mainGrid.add(chooseYear, 2, 6);
        mainGrid.add(groupCombo, 1, 7);
        mainGrid.add(chooseGroup, 2, 7);
        mainGrid.add(exit, 2, 8);

        mainStage.setScene(mainScene);
        mainStage.setOnCloseRequest(Event::consume);
        mainStage.setTitle("Meniu principal");
        mainStage.show();

    }

}