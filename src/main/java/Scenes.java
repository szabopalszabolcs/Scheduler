import com.sun.javafx.stage.StageHelper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.Event;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.SearchableComboBox;

import java.util.ArrayList;
import java.util.Objects;

public class Scenes {

    private final int HOURS=7,DAYS=12;
    ArrayList<Activity> activities;
    ArrayList<Group> groups;
    ArrayList<Professor> professors;
    ArrayList<Room> rooms;
    public static IndexedLabel draggingLabel;
    private final DataFormat labelFormat;
    boolean dropped = false;

    public Scenes(ArrayList<Professor> professors, ArrayList<Activity> activities, ArrayList<Group> groups, ArrayList<Room> rooms) {
        this.professors = professors;
        this.groups=groups;
        this.activities=activities;
        this.rooms=rooms;
        DataFormat dataFormat = DataFormat.lookupMimeType("Unitbv");
        if (dataFormat==null)
            labelFormat=new DataFormat("Unitbv");
        else
            labelFormat=dataFormat;
    }

    public boolean onScheduleOfProfessor(int activityId, int professorId, int semester) {
        Professor professor = professors.get(professorId);
        for (int i=0;i<HOURS;i++)
            for (int j=0;j<DAYS;j++)
                if (activityId== professor.getActivityProfessor(semester,i,j))
                    return true;
        return false;
    }

    public boolean onScheduleOfGroup(int activityId, int groupId, int semester) {
        Group group = groups.get(groupId);
        for (int i=0;i<HOURS;i++)
            for (int j=0;j<DAYS;j++)
                if (activityId==group.getActivityGroup(semester,i,j))
                    return true;
        return false;
    }

    public void addRoom() {

        Stage stage = new Stage();
        stage.setTitle("Adăugare/Ștergere sală");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20, 20, 20, 20));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        Scene scene=new Scene(gridPane);

        Button add=new Button("Adaugă sala");
        add.setPrefSize(200,30);

        Label newRoomLabel=new Label("Întroduceți numele sălii");
        newRoomLabel.setPrefSize(200,20);

        TextField newRoomName=new TextField();
        newRoomName.setPrefSize(200,30);

        Label searchRoomLabel=new Label("Alegeți sala");
        searchRoomLabel.setPrefSize(200,20);

        SearchableComboBox<String> roomCombo=new SearchableComboBox<String>();
        roomCombo.setPrefSize(200,30);
        for (Room room:rooms) {
            roomCombo.getItems().add(room.getRoomName());
        }

        Button remove=new Button("Șterge sala");
        remove.setPrefSize(200,30);

        Button close=new Button("Închide fereastra");
        close.setPrefSize(200,30);

        add.setOnAction(event -> {
            if (!newRoomName.getText().equals("")) {
                boolean noSuchRoom=true;
                for (Room room:rooms) {
                    if (room.getRoomName().equals(newRoomName.getText())) {
                        Utility.message("Sala este deja adăugată");
                        noSuchRoom=false;
                        break;
                    }
                }
                if (noSuchRoom) {
                    Room newRoom = new Room(rooms.size(), newRoomName.getText());
                    roomCombo.getItems().add(newRoom.getRoomName());
                    rooms.add(newRoom);
                }
            }
            newRoomName.setText("");
        });

        remove.setOnAction(event -> {
            String selectedRoom=roomCombo.getValue();
            if (!selectedRoom.equals("")) {
                boolean roomFound=false;

                for (int i=0;i<rooms.size();i++) {
                    if (roomFound) {
                        int newId = rooms.get(i).getRoomId() - 1;
                        rooms.get(i).setRoomId(newId);
                    }
                    else {
                        if (rooms.get(i).getRoomName().equals(selectedRoom)) {
                            for (Activity activity : activities) {
                                if (activity.getClassRoomId() == i) {
                                    activity.setClassRoomId(-1);
                                }
                                if (activity.getClassRoomId() > i) {
                                    int newRoomId = activity.getClassRoomId() - 1;
                                    activity.setClassRoomId(newRoomId);
                                }
                            }
                            rooms.remove(i);
                            roomFound = true;
                            i--;
                        }
                    }
                }
                roomCombo.getSelectionModel().clearSelection();
                roomCombo.setValue(null);
                roomCombo.getItems().remove(selectedRoom);

            }
        });

        close.setOnAction(event -> stage.close());

        gridPane.add(newRoomLabel,1,1);
        gridPane.add(newRoomName,1,2);
        gridPane.add(add,2,2);
        gridPane.add(searchRoomLabel,1,3);
        gridPane.add(roomCombo,1,4);
        gridPane.add(remove,2,4);
        gridPane.add(close,2,6);

        stage.setScene(scene);
        stage.setOnCloseRequest(Event::consume);
        stage.show();

    }

    private void dragTextArea(IndexedLabel ta) {
        ta.setOnDragDetected(e -> {
            Dragboard db = ta.startDragAndDrop(TransferMode.MOVE);
            db.setDragView(ta.snapshot(null,null));
            ClipboardContent content = new ClipboardContent();
            content.put(labelFormat,"");
            db.setContent(content);
            draggingLabel=ta;
        });
        ta.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getClickCount() == 2) {
                    Stage stage = searchForStage(professors.get(ta.getProfessorId()).getName());
                    if (stage == null) {
                        professorsScheduleScene(ta.getProfessorId(), activities.get(ta.getActivityId()).getSemester());
                    }
                }
            }
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                changeRoom(ta);
            }
        });
    }

    private void changeRoom(IndexedLabel chosenLabel) {

        ArrayList<Room> availableRooms = new ArrayList<>();
        ArrayList<Integer> hours = new ArrayList<>(), days = new ArrayList<>();
        int semester = activities.get(chosenLabel.getActivityId()).getSemester();
        int professorId = chosenLabel.getProfessorId();
        Professor professor = professors.get(professorId);
        int activityId = chosenLabel.getActivityId();
        Activity activity = activities.get(activityId);

        for (int hour = 0; hour < HOURS; hour++) {
            for (int day = 0; day < DAYS; day++) {
                if (professor.getActivityProfessor(semester, hour, day) == chosenLabel.getActivityId()) {
                    hours.add(hour);
                    days.add(day);
                }
            }
        }

        for (Room room : rooms) {
            boolean ok = true;
            for (int i = 0; i < hours.size(); i++) {
                if (room.getActivityRoom(semester, hours.get(i), days.get(i)) != -1) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                availableRooms.add(room);
            }
        }

        Stage stage = new Stage();
        stage.setTitle("Alegere sală");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20, 20, 20, 20));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        Scene scene = new Scene(gridPane);

        Button choose = new Button("Alege sala");
        choose.setPrefSize(200, 30);

        Label chooseRoomLabel = new Label("Alegeți sala");
        chooseRoomLabel.setPrefSize(200, 20);

        SearchableComboBox<String> roomCombo = new SearchableComboBox<String>();
        roomCombo.setPrefSize(200, 30);


        for (Room room : availableRooms) {
            roomCombo.getItems().add(room.getRoomName());
        }

        Button close = new Button("Închide fereastra");
        close.setPrefSize(200, 30);

        choose.setOnAction(event -> {
            String selectedRoomName = roomCombo.getValue();
            int selectedRoomId = -1;
            for (Room room : rooms) {
                if (room.getRoomName().equals(selectedRoomName)) {
                    selectedRoomId = room.getRoomId();
                }
            }
            if (selectedRoomId >= 0) {
                activity.setClassRoomId(selectedRoomId);
                for (int t = 0; t < hours.size(); t++) {
                    rooms.get(selectedRoomId).setActivityRoom(semester, hours.get(t), days.get(t), activityId);
                }
            }
            stage.close();
        });

        close.setOnAction(event -> stage.close());

        gridPane.add(choose, 2, 2);
        gridPane.add(chooseRoomLabel, 1, 1);
        gridPane.add(roomCombo, 1, 2);
        gridPane.add(close, 2, 3);

        stage.setScene(scene);
        stage.setOnCloseRequest(Event::consume);
        stage.show();

    }

    public void renameGroup() {

        Stage stage = new Stage();
        stage.setTitle("Denumire grupe");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20, 20, 20, 20));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        Scene scene = new Scene(gridPane);

        Button renameGroup = new Button("Schimba denumirea");
        renameGroup.setPrefSize(200, 30);

        Label chooseGroupLabel = new Label("Alegeți grupa");
        chooseGroupLabel.setPrefSize(200, 20);

        Label newNameLabel = new Label("Nume nou grupă");
        newNameLabel.setPrefSize(200, 20);

        TextField newName = new TextField();
        newName.setPrefSize(200, 30);

        SearchableComboBox<String> groupCombo = new SearchableComboBox<String>();
        groupCombo.setPrefSize(200, 30);
        for (Group group : groups) {
            groupCombo.getItems().add(group.getGroupName());
        }

        Button close = new Button("Închide fereastra");
        close.setPrefSize(200, 30);

        renameGroup.setOnAction(event -> {

            String newGroupName = newName.getText();

            if (newGroupName.trim().equals("")) {
                Utility.message("Nume de grupă invalid");
                newName.clear();
            } else {
                for (Group group : groups) {
                    if (newGroupName.trim().equals(group.getGroupName())) {
                        Utility.message("Nume de grupă existent");
                        newName.clear();
                    }
                }
            }
            if (!newName.getText().isEmpty()) {
                String nameToChange = groupCombo.getValue();
                if (nameToChange.isEmpty()) {
                    Utility.message("Nu este selectată nici o grupă");
                } else {
                    for (Group group : groups) {
                        if (group.getGroupName().equals(nameToChange)) {
                            group.setGroupName(newGroupName);
                            break;
                        }
                    }
                    groupCombo.getItems().clear();
                    for (Group group : groups) {
                        groupCombo.getItems().add(group.getGroupName());
                    }
                }
            }
        });

        close.setOnAction(event -> stage.close());

        gridPane.add(chooseGroupLabel, 1, 1);
        gridPane.add(newNameLabel, 2, 1);
        gridPane.add(groupCombo, 1, 2);
        gridPane.add(newName, 2, 2);
        gridPane.add(renameGroup, 2, 3);
        gridPane.add(close, 2, 4);

        stage.setScene(scene);
        stage.setOnCloseRequest(Event::consume);
        stage.show();

    }

    public void professorsScheduleScene(int professorId, int semester) {

        Professor professor = professors.get(professorId);
        Stage scheduleStage = new Stage();
        HBox horizontalBox = new HBox();
        GridPane classesGrid = new GridPane();
        GridPane scheduleGrid = new GridPane();
        StackPane[][] scheduleMatrix=new StackPane[HOURS+1][DAYS+1];

        String[] ore={"Zi \\ Ora","8-9,50","10-11,50","12-13,50","14-15,50","16-17,50","18-19,50","20-21,50"};
        String[] zile={"Luni","Marti","Miercuri","Joi","Vineri","Sambata"};

        for (int i=0;i<HOURS+1;i++) {
            scheduleMatrix[i][0]=new StackPane();
            scheduleMatrix[i][0].setPrefSize(80,40);
            scheduleMatrix[i][0].setStyle("-fx-border-color:black; -fx-background-color:beige; -fx-padding:5");
            scheduleMatrix[i][0].getChildren().add(new Label((ore[i])));
            scheduleGrid.add(scheduleMatrix[i][0], i, 0);
        }

        for (int j=1;j<DAYS/2+1;j++) {
            scheduleMatrix[0][j]=new StackPane();
            scheduleMatrix[0][j].setStyle("-fx-border-color:black; -fx-background-color:beige; -fx-padding:5");
            scheduleMatrix[0][j].getChildren().add(new Label((zile[j-1])));
            scheduleGrid.add(scheduleMatrix[0][j], 0, j*2-1,1,2);
        }


        for (int i=1;i<HOURS+1;i++) {
            for (int j=1;j<DAYS+1;j++){
                scheduleMatrix[i][j]=new StackPane();
                scheduleMatrix[i][j].setStyle("-fx-border-color:black");
                scheduleMatrix[i][j].setPrefSize(80,40);
                scheduleMatrix[i][j].setAlignment(Pos.TOP_CENTER);
                addDropHandlingProfSchedule(scheduleMatrix[i][j]);
                int presentActivityId=professor.getActivityProfessor(semester,i-1,j-1);
                if (presentActivityId!=-1) {
                    Activity presentActivity=activities.get(presentActivityId);
                    IndexedLabel lbl=Utility.createProfLabel(presentActivity, professor, groups,rooms);
                    scheduleMatrix[i][j].getChildren().add(lbl);
                    dragTextArea(lbl);
                }
                scheduleGrid.add(scheduleMatrix[i][j], i, j);
            }
        }

        int nrActivities=0;
        for (int activity: professor.getActivitiesOfProfessor()) {
            if (activities.get(activity).getSemester() == semester) {
                nrActivities++;
            }
        }
        StackPane[] classesArray=new StackPane[nrActivities];
        int sqr,multiplier=0;
        if (Math.floor((Math.sqrt(nrActivities)))==Math.sqrt(nrActivities)) {
            sqr = (int) Math.floor(Math.sqrt(nrActivities));
        }
        else {
            sqr = (int) Math.floor(Math.sqrt(nrActivities)) + 1;
        }
        int count=0;
        for (int i = 0; i< professor.getActivitiesOfProfessor().length; i++) {
            Activity currentActivity = activities.get(professor.getActivitiesOfProfessor()[i]);
            if (currentActivity.getSemester() == semester) {
                classesArray[count] = new StackPane();
                classesArray[count].setPrefWidth(80);
                classesArray[count].setMinHeight(40);
                classesArray[count].setAlignment(Pos.CENTER);
                classesArray[count].setStyle("-fx-border-color:black");
                addDropHandlingClasses(classesArray[count], professorId);
                classesGrid.add(classesArray[count], count % sqr, count / sqr);
                if (!onScheduleOfProfessor(professor.getActivitiesOfProfessor()[i], professorId, semester)) {
                    IndexedLabel lbl = Utility.createProfLabel(currentActivity, professor, groups,rooms);
                    classesArray[count].getChildren().add(lbl);
                    dragTextArea(lbl);
                }
                count++;
            }
        }
        for (int i=sqr-1;i<sqr+1;i++) {
            if (sqr*i>count) {
                multiplier=i;
                break;
            }
        }
        if (multiplier==0) multiplier=sqr;
        for (int i=count;i<sqr*multiplier;i++) {
            StackPane pane = new StackPane();
            pane.setPrefWidth(80);
            pane.setMinHeight(40);
            pane.setAlignment(Pos.CENTER);
            addDropHandlingClasses(pane,professorId);
            classesGrid.add(pane, i % sqr, i / sqr);
        }

        horizontalBox.getChildren().addAll(scheduleGrid, classesGrid);
        horizontalBox.setPadding(new Insets(20, 20, 20, 20));
        horizontalBox.setAlignment(Pos.CENTER);
        horizontalBox.setSpacing(20);
        Scene scheduleScene = new Scene(horizontalBox);
        scheduleStage.setScene(scheduleScene);
        scheduleStage.setTitle(professor.getName() + " semestrul " + semester);
        scheduleStage.show();

        scheduleScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                searchForStage("Meniu principal").toFront();
            }
            if (event.getCode() == KeyCode.TAB) {
                synchronized (this) {
                    SortedList<Stage> stages = StageHelper.getStages().sorted();
                    for (int i = 0; i < stages.size(); i++) {
                        if (stages.get(i).equals(scheduleStage)) {
                            if (stages.get((i + 1) % stages.size()).getTitle().equals("Meniu principal"))
                                i++;
                            stages.get((i + 1) % stages.size()).toFront();
                            break;
                        }
                    }
                }
            }
        });
    }

    private void addDropHandlingClasses(StackPane pane,int professorId) {
        pane.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasContent(labelFormat)&&pane.getChildren().isEmpty()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
        });
        pane.setOnDragDropped(e -> {
            if (draggingLabel.getProfessorId()==professorId) {
                try {
                    ObservableList<Node> childrens;
                    StackPane parentOfLabel = (StackPane) draggingLabel.getParent();
                    StackPane actualPane;
                    IndexedLabel actualLabel;
                    GridPane gridOfOrigin = (GridPane) parentOfLabel.getParent();
                    childrens = gridOfOrigin.getChildren();
                    for (Node node:childrens){
                        if (node.getClass()==pane.getClass()) {
                            actualPane = (StackPane) node;
                            if (!actualPane.getChildren().isEmpty()) {
                                if (actualPane.getChildren().get(0).getClass()==IndexedLabel.class) {
                                    actualLabel = (IndexedLabel) actualPane.getChildren().get(0);
                                    if (actualLabel.getActivityId() == draggingLabel.getActivityId()) {
                                        ((StackPane) actualLabel.getParent()).getChildren().remove(actualLabel);
                                    }
                                }
                            }
                        }
                    }
                }
                catch (Exception exception) {
                    Utility.message("Removing not succeded");
                }
                Activity activity=activities.get(draggingLabel.getActivityId());
                Professor professor=professors.get(professorId);
                int semester=activity.getSemester();
                draggingLabel.setPrefSize(80,40*activity.getTime());
                pane.getChildren().add(draggingLabel);
                e.setDropCompleted(true);
                dragTextArea(draggingLabel);
                for (int i=0;i<HOURS;i++)
                    for (int j=0;j<DAYS;j++){
                        if (activity.getIdActivity()==professor.getActivityProfessor(semester,i,j))
                            professor.setActivityProfessor(semester,i,j,-1);
                        for (int k = 0; k< Objects.requireNonNull(activity).getGroupsId().length; k++)
                            if (activity.getIdActivity()==groups.get(activity.getGroupsId()[k]).getActivityGroup(semester,i,j))
                                groups.get(activity.getGroupsId()[k]).setActivityGroup(semester,i,j,-1);
                    }
            }
        });
    }

    private boolean isMovableToProfSchedule(int col, int row, int time, Activity activity) {

        boolean isRoom=false;
        Room room=null;
        int semester = activity.getSemester();
        Professor professor = professors.get(activity.getProfessorId());
        if (activity.getClassRoomId()>=0) {
            isRoom=true;
            room = rooms.get(activity.getClassRoomId());
        }
        if (row == 0 || col == 0) {
            return false;
        }
        if (col + (time - 1) / 2 > 7) {
            return false;
        }
        int add;
        int X,Y;
        switch (time%2) {
            case 1:
                if (row % 2 == 0)
                    add=-1;
                else
                    add=1;
                for (int t=0;t<time;t++) {
                    X=row+(t%2)*add;
                    Y=col+(t+1)/2;
                    if (professor.getActivityProfessor(semester,Y-1, X-1) != -1
                            && professor.getActivityProfessor(semester,Y-1, X-1) != activity.getIdActivity()) {
                        Activity act=activities.get(professor.getActivityProfessor(semester,Y-1,X-1));
                        Utility.message(professor.getName()+" are alta activitate\n\n"+act.getSubject());
                        return false;
                    }
                    for (int j = 0; j<activity.getGroupsId().length; j++) {
                        if (groups.get(activity.getGroupsId()[j]).getActivityGroup(semester, Y - 1, X - 1) != -1
                                && groups.get(activity.getGroupsId()[j]).getActivityGroup(semester, Y - 1, X - 1) != activity.getIdActivity()) {
                            Activity act=activities.get(groups.get(activity.getGroupsId()[j]).getActivityGroup(semester,Y-1,X-1));
                            Utility.message("Grupa "+groups.get(activity.getGroupsId()[j]).getGroupName()+" are alta activitate\n\n"+act.getSubject());
                            return false;
                        }
                    }
                    if (isRoom) {
                        if (room.getActivityRoom(semester,Y-1,X-1) != -1
                                && room.getActivityRoom(semester,Y-1,X-1) != activity.getIdActivity()) {
                            Activity act=activities.get(room.getActivityRoom(semester,Y-1,X-1));
                            Utility.message(("Sala "+room.getRoomName()+" este ocupata\n\n"+act.getSubject()));
                            return false;
                        }
                    }
                }
                break;
            case 0:
                if (row % 2 == 0)
                    row--;
                for (int t=0;t<time;t++) {
                    X=row+t%2;
                    Y=col+t/2;
                    if (professor.getActivityProfessor(semester,Y-1, X-1) != -1
                            && professor.getActivityProfessor(semester,Y-1, X-1) != activity.getIdActivity()) {
                        Activity act=activities.get(professor.getActivityProfessor(semester,Y-1,X-1));
                        Utility.message(professor.getName()+" are alta activitate\n\n"+act.getSubject());
                        return false;
                    }
                    for (int j = 0; j<activity.getGroupsId().length; j++) {
                        if (groups.get(activity.getGroupsId()[j]).getActivityGroup(semester, Y - 1, X - 1) != -1
                                && groups.get(activity.getGroupsId()[j]).getActivityGroup(semester, Y - 1, X - 1) != activity.getIdActivity()) {
                            Activity act=activities.get(groups.get(activity.getGroupsId()[j]).getActivityGroup(semester,Y-1,X-1));
                            Utility.message("Grupa "+groups.get(activity.getGroupsId()[j]).getGroupName()+" are alta activitate\n\n"+act.getSubject());
                            return false;
                        }
                    }
                    if (isRoom) {
                        if (room.getActivityRoom(semester,Y-1,X-1) != -1
                                && room.getActivityRoom(semester,Y-1,X-1) != activity.getIdActivity()) {
                            Activity act=activities.get(room.getActivityRoom(semester,Y-1,X-1));
                            Utility.message(("Sala "+room.getRoomName()+" este ocupata\n\n"+act.getSubject()));
                            return false;
                        }
                    }
                }
                break;
            default:
                break;
        }

        return true;
    }

    private void addDropHandlingProfSchedule(StackPane pane) {
        pane.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasContent(labelFormat)&&draggingLabel!=null&&pane.getChildren().isEmpty()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
        });
        pane.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            Activity activity=activities.get(draggingLabel.getActivityId());
            if (db.hasContent(labelFormat)) {
                int     row= GridPane.getRowIndex(pane),
                        col= GridPane.getColumnIndex(pane),
                        time = activity.getTime();
                if (isMovableToProfSchedule(col,row,time,activity)){
                    Pane parentOfLabel=(Pane) draggingLabel.getParent();
                    parentOfLabel.getChildren().clear();
                    moveToProfSchedule(pane,col,row,activity,(Stage) pane.getScene().getWindow());
                    draggingLabel = null;
                    e.setDropCompleted(true);
                }
            }
        });
    }

    private void moveToProfSchedule(StackPane pane,int col, int row,Activity activity,Stage frontStage) {

        StackPane secondPane, actualPane;
        GridPane grid;
        IndexedLabel actualLabel;
        IndexedLabel[] labels;
        int add;
        ObservableList<Node> childrens;
        int X, Y, nodeX, nodeY;
        Room room=null;
        boolean isRoom=false;
        if (activity.getClassRoomId()>=0) {
            isRoom=true;
            room=rooms.get(activity.getClassRoomId());
        }
        Professor professor = professors.get(activity.getProfessorId());
        int semester = activity.getSemester();
        int time = activity.getTime();
        switch (time % 2) {
            case 1:
                grid = (GridPane) pane.getParent();
                labels = new IndexedLabel[time];
                for (int i = 0; i < time; i++) {
                    labels[i] = Utility.createProfLabel(activity, professor, groups, rooms);
                    dragTextArea(labels[i]);
                }
                childrens = grid.getChildren();
                for (Node node : childrens) {
                    if (node.getClass() == pane.getClass()) {
                        actualPane = (StackPane) node;
                        if (!actualPane.getChildren().isEmpty()) {
                            if (actualPane.getChildren().get(0).getClass() == draggingLabel.getClass()) {
                                actualLabel = (IndexedLabel) actualPane.getChildren().get(0);
                                if (actualLabel.getActivityId() == draggingLabel.getActivityId()) {
                                    ((Pane) actualLabel.getParent()).getChildren().remove(actualLabel);
                                }
                            }
                        }
                    }
                }
                for (int i = 0; i < HOURS; i++)
                    for (int j = 0; j < DAYS; j++) {
                        if (activity.getIdActivity() == professor.getActivityProfessor(semester, i, j)) {
                            professor.setActivityProfessor(semester, i, j, -1);
                        }
                        for (int k = 0; k < Objects.requireNonNull(activity).getGroupsId().length; k++) {
                            if (activity.getIdActivity() == groups.get(activity.getGroupsId()[k]).getActivityGroup(semester, i, j)) {
                                groups.get(activity.getGroupsId()[k]).setActivityGroup(semester, i, j, -1);
                            }
                        }
                        if (isRoom) {
                            if (activity.getIdActivity() == room.getActivityRoom(semester,i,j)) {
                                room.setActivityRoom(semester,i,j,-1);
                            }
                        }
                    }

                if (row % 2 == 0)
                    add = -1;
                else
                    add = 1;
                for (int t = 0; t < time; t++) {
                    X = row + (t % 2) * add;
                    Y = col + (t + 1) / 2;
                    childrens = grid.getChildren();
                    for (Node node : childrens) {
                        nodeX = GridPane.getRowIndex(node);
                        nodeY = GridPane.getColumnIndex(node);
                        if (nodeX == X && nodeY == Y) {
                            secondPane = (StackPane) node;
                            secondPane.getChildren().add(labels[t]);
                        }
                        if (nodeX >= X && nodeY >= Y) {
                            break;
                        }
                    }
                    professor.setActivityProfessor(semester, Y - 1, X - 1, activity.getIdActivity());
                    for (int j = 0; j < activity.getGroupsId().length; j++) {
                        groups.get(activity.getGroupsId()[j]).setActivityGroup(semester, Y - 1, X - 1, activity.getIdActivity());
                    }
                    if (isRoom) {
                        room.setActivityRoom(semester,Y-1,X-1,activity.getIdActivity());
                    }
                }
                break;
            case 0:
                grid = (GridPane) pane.getParent();
                labels = new IndexedLabel[time];
                for (int i = 0; i < time; i++) {
                    labels[i] = Utility.createProfLabel(activity, professor, groups, rooms);
                    dragTextArea(labels[i]);
                }
                childrens = grid.getChildren();
                for (Node node : childrens) {
                    if (node.getClass() == pane.getClass()) {
                        actualPane = (StackPane) node;
                        if (!actualPane.getChildren().isEmpty()) {
                            if (actualPane.getChildren().get(0).getClass() == draggingLabel.getClass()) {
                                actualLabel = (IndexedLabel) actualPane.getChildren().get(0);
                                if (actualLabel.getActivityId() == draggingLabel.getActivityId()) {
                                    ((Pane) actualLabel.getParent()).getChildren().remove(actualLabel);
                                }
                            }
                        }
                    }
                }
                for (int i = 0; i < HOURS; i++)
                    for (int j = 0; j < DAYS; j++) {
                        if (activity.getIdActivity() == professor.getActivityProfessor(semester, i, j)) {
                            professor.setActivityProfessor(semester, i, j, -1);
                        }
                        for (int k = 0; k < Objects.requireNonNull(activity).getGroupsId().length; k++) {
                            if (activity.getIdActivity() == groups.get(activity.getGroupsId()[k]).getActivityGroup(semester, i, j)) {
                                groups.get(activity.getGroupsId()[k]).setActivityGroup(semester, i, j, -1);
                            }
                        }
                        if (isRoom) {
                            if (activity.getIdActivity() == room.getActivityRoom(semester,i,j)) {
                                room.setActivityRoom(semester,i,j,-1);
                            }
                        }
                    }
                if (row % 2 == 0)
                    row--;
                for (int t = 0; t < time; t++) {
                    X = row + t % 2;
                    Y = col + t / 2;
                    childrens = grid.getChildren();
                    for (Node node : childrens) {
                        nodeX = GridPane.getRowIndex(node);
                        nodeY = GridPane.getColumnIndex(node);
                        if (nodeX == X && nodeY == Y) {
                            secondPane = (StackPane) node;
                            secondPane.getChildren().add(labels[t]);
                        }
                        if (nodeX >= X && nodeY >= Y) {
                            break;
                        }
                    }
                    professor.setActivityProfessor(semester, Y - 1, X - 1, activity.getIdActivity());
                    for (int j = 0; j < activity.getGroupsId().length; j++) {
                        groups.get(activity.getGroupsId()[j]).setActivityGroup(semester, Y - 1, X - 1, activity.getIdActivity());
                    }
                    if (isRoom) {
                        room.setActivityRoom(semester,Y-1,X-1,activity.getIdActivity());
                    }
                }
                break;
            default:
                break;
        }

        Stage stageToRefresh=searchForStage("Orar anul "+activity.getYearOfStudy());
        if (stageToRefresh!=null) {
            stageToRefresh.close();
            yearScheduleScene(activity.getYearOfStudy(), semester);
        }
        for (int group:activity.getGroupsId()) {
            stageToRefresh = searchForStage(groups.get(group).getGroupName());
            if (stageToRefresh!=null) {
                stageToRefresh.close();
                groupsScheduleScene(group, semester);
            }
        }

        frontStage.toFront();

    }

    public void groupsScheduleScene(int groupId, int semester) {

        Group group = groups.get(groupId);
        Stage scheduleStage=new Stage();
        HBox horizontalBox=new HBox();
        GridPane classesGrid=new GridPane();
        GridPane scheduleGrid=new GridPane();
        StackPane[][] scheduleMatrix=new StackPane[HOURS+1][DAYS+1];

        String[] ore={"Zi \\ Ora","8-9,50","10-11,50","12-13,50","14-15,50","16-17,50","18-19,50","20-21,50"};
        String[] zile={"Luni","Marti","Miercuri","Joi","Vineri","Sambata"};

        for (int i=0;i<HOURS+1;i++) {
            scheduleMatrix[i][0]=new StackPane();
            scheduleMatrix[i][0].setPrefSize(80,40);
            scheduleMatrix[i][0].setStyle("-fx-border-color:black; -fx-background-color:beige; -fx-padding:5");
            scheduleMatrix[i][0].getChildren().add(new Label((ore[i])));
            scheduleGrid.add(scheduleMatrix[i][0], i, 0);
        }

        for (int j=1;j<DAYS/2+1;j++) {
            scheduleMatrix[0][j]=new StackPane();
            scheduleMatrix[0][j].setStyle("-fx-border-color:black; -fx-background-color:beige; -fx-padding:5");
            scheduleMatrix[0][j].getChildren().add(new Label((zile[j-1])));
            scheduleGrid.add(scheduleMatrix[0][j], 0, j*2-1,1,2);
        }


        for (int i=1;i<HOURS+1;i++) {
            for (int j=1;j<DAYS+1;j++){
                scheduleMatrix[i][j]=new StackPane();
                scheduleMatrix[i][j].setStyle("-fx-border-color:black");
                scheduleMatrix[i][j].setPrefSize(80,40);
                scheduleMatrix[i][j].setAlignment(Pos.TOP_CENTER);
                addDropHandlingGroupSchedule(scheduleMatrix[i][j]);
                int presentActivityId=group.getActivityGroup(semester,i-1,j-1);
                if (presentActivityId!=-1) {
                    Activity presentActivity=activities.get(presentActivityId);
                    IndexedLabel lbl=Utility.createGroupLabel(presentActivity,professors.get(presentActivity.getProfessorId()),groups,rooms);
                    scheduleMatrix[i][j].getChildren().add(lbl);
                    dragTextArea(lbl);
                }
                scheduleGrid.add(scheduleMatrix[i][j], i, j);
            }
        }

        int nrActivities=0;
        for (int activity: group.getActivitiesOfGroup()) {
            if (activities.get(activity).getSemester() == semester) {
                nrActivities++;
            }
        }
        StackPane[] classesArray=new StackPane[nrActivities];
        int sqr,multiplier=0;
        if (Math.floor((Math.sqrt(nrActivities)))==Math.sqrt(nrActivities)) {
            sqr = (int) Math.floor(Math.sqrt(nrActivities));
        }
        else {
            sqr = (int) Math.floor(Math.sqrt(nrActivities)) + 1;
        }
        int count=0;
        for (int i = 0; i<group.getActivitiesOfGroup().length; i++) {
            Activity currentActivity = activities.get(group.getActivitiesOfGroup()[i]);
            Professor professor = professors.get(currentActivity.getProfessorId());
            if (currentActivity.getSemester() == semester) {
                classesArray[count] = new StackPane();
                classesArray[count].setPrefWidth(80);
                classesArray[count].setMinHeight(40);
                classesArray[count].setAlignment(Pos.CENTER);
                classesArray[count].setStyle("-fx-border-color:black");
                addDropHandlingClassesGroup(classesArray[count], groupId);
                classesGrid.add(classesArray[count], count % sqr, count / sqr);
                if (!onScheduleOfGroup(group.getActivitiesOfGroup()[i], groupId, semester)) {
                    IndexedLabel lbl = Utility.createGroupLabel(currentActivity, professor, groups,rooms);
                    classesArray[count].getChildren().add(lbl);
                    dragTextArea(lbl);
                }
                count++;
            }
        }
        for (int i=sqr-1;i<sqr+1;i++) {
            if (sqr*i>count) {
                multiplier=i;
                break;
            }
        }
        if (multiplier==0) multiplier=sqr;
        for (int i=count;i<sqr*multiplier;i++) {
            StackPane pane = new StackPane();
            pane.setPrefWidth(80);
            pane.setMinHeight(40);
            pane.setAlignment(Pos.CENTER);
            addDropHandlingClassesGroup(pane,groupId);
            classesGrid.add(pane, i % sqr, i / sqr);
        }

        horizontalBox.getChildren().addAll(scheduleGrid, classesGrid);
        horizontalBox.setPadding(new Insets(20, 20, 20, 20));
        horizontalBox.setAlignment(Pos.CENTER);
        horizontalBox.setSpacing(20);
        Scene scheduleScene = new Scene(horizontalBox);
        scheduleStage.setScene(scheduleScene);
        scheduleStage.setTitle("Orar grupa " + group.getGroupName() + " semestrul " + semester);
        scheduleStage.show();

        scheduleScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                searchForStage("Meniu principal").toFront();
            }
            if (event.getCode() == KeyCode.TAB) {
                synchronized (this) {
                    SortedList<Stage> stages = StageHelper.getStages().sorted();
                    for (int i = 0; i < stages.size(); i++) {
                        if (stages.get(i).equals(scheduleStage)) {
                            if (stages.get((i + 1) % stages.size()).getTitle().equals("Meniu principal"))
                                i++;
                            stages.get((i + 1) % stages.size()).toFront();
                            break;
                        }
                    }
                }
            }
        });

    }

    private void addDropHandlingClassesGroup(StackPane pane,int groupId) {
        pane.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasContent(labelFormat)&&pane.getChildren().isEmpty()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
        });
        pane.setOnDragDropped(e -> {
            boolean ok=false;
            for (int groupOfLabel:draggingLabel.getGroupsId()) {
                if (groupOfLabel==groupId) {
                    ok=true;
                    break;
                }
            }
            if (ok) {
                try {
                    ObservableList<Node> childrens;
                    StackPane parentOfLabel = (StackPane) draggingLabel.getParent();
                    StackPane actualPane;
                    IndexedLabel actualLabel;
                    GridPane gridOfOrigin = (GridPane) parentOfLabel.getParent();
                    childrens = gridOfOrigin.getChildren();
                    for (Node node:childrens){
                        if (node.getClass()==pane.getClass()) {
                            actualPane = (StackPane) node;
                            if (!actualPane.getChildren().isEmpty()) {
                                if (actualPane.getChildren().get(0).getClass()==IndexedLabel.class) {
                                    actualLabel = (IndexedLabel) actualPane.getChildren().get(0);
                                    if (actualLabel.getActivityId() == draggingLabel.getActivityId()) {
                                        ((StackPane) actualLabel.getParent()).getChildren().remove(actualLabel);
                                    }
                                }
                            }
                        }
                    }
                }
                catch (Exception exception) {
                    Utility.message("Removing not succeded");
                }
                Activity activity=activities.get(draggingLabel.getActivityId());
                Professor professor=professors.get(activity.getProfessorId());
                int semester=activity.getSemester();
                draggingLabel.setPrefSize(80,40*activity.getTime());
                pane.getChildren().add(draggingLabel);
                e.setDropCompleted(true);
                dragTextArea(draggingLabel);
                for (int i=0;i<HOURS;i++)
                    for (int j=0;j<DAYS;j++){
                        if (activity.getIdActivity()==professor.getActivityProfessor(semester,i,j))
                            professor.setActivityProfessor(semester,i,j,-1);
                        for (int k = 0; k< Objects.requireNonNull(activity).getGroupsId().length; k++)
                            if (activity.getIdActivity()==groups.get(activity.getGroupsId()[k]).getActivityGroup(semester,i,j))
                                groups.get(activity.getGroupsId()[k]).setActivityGroup(semester,i,j,-1);
                    }
            }
        });
    }

    private boolean isMovableToGroupSchedule(int col, int row, int time, Activity activity) {

        boolean isRoom=false;
        Room room=null;
        int semester = activity.getSemester();
        Professor professor = professors.get(activity.getProfessorId());
        if (activity.getClassRoomId()>=0) {
            isRoom=true;
            room = rooms.get(activity.getClassRoomId());
        }
        if (row == 0 || col == 0) {
            return false;
        }
        if (col + (time - 1) / 2 > 7) {
            return false;
        }
        int add;
        int X,Y;
        switch (time%2) {
            case 1:
                if (row % 2 == 0)
                    add=-1;
                else
                    add=1;
                for (int t=0;t<time;t++) {
                    X=row+(t%2)*add;
                    Y=col+(t+1)/2;
                    if (professor.getActivityProfessor(semester,Y-1, X-1) != -1
                            && professor.getActivityProfessor(semester,Y-1, X-1) != activity.getIdActivity()) {
                        Activity act=activities.get(professor.getActivityProfessor(semester,Y-1,X-1));
                        Utility.message(professor.getName()+" are alta activitate\n\n"+act.getSubject());
                        return false;
                    }
                    for (int j = 0; j<activity.getGroupsId().length; j++) {
                        if (groups.get(activity.getGroupsId()[j]).getActivityGroup(semester, Y - 1, X - 1) != -1
                                && groups.get(activity.getGroupsId()[j]).getActivityGroup(semester, Y - 1, X - 1) != activity.getIdActivity()) {
                            Activity act=activities.get(groups.get(activity.getGroupsId()[j]).getActivityGroup(semester,Y-1,X-1));
                            Utility.message("Grupa "+groups.get(activity.getGroupsId()[j]).getGroupName()+" are alta activitate\n\n"+act.getSubject());
                            return false;
                        }
                    }
                    if (isRoom) {
                        if (room.getActivityRoom(semester,Y-1,X-1) != -1
                                && room.getActivityRoom(semester,Y-1,X-1) != activity.getIdActivity()) {
                            Activity act=activities.get(room.getActivityRoom(semester,Y-1,X-1));
                            Utility.message(("Sala "+room.getRoomName()+" este ocupata\n\n"+act.getSubject()));
                            return false;
                        }
                    }
                }
                break;
            case 0:
                if (row % 2 == 0)
                    row--;
                for (int t=0;t<time;t++) {
                    X=row+t%2;
                    Y=col+t/2;
                    if (professor.getActivityProfessor(semester,Y-1, X-1) != -1
                            && professor.getActivityProfessor(semester,Y-1, X-1) != activity.getIdActivity()) {
                        Activity act=activities.get(professor.getActivityProfessor(semester,Y-1,X-1));
                        Utility.message(professor.getName()+" are alta activitate\n\n"+act.getSubject());
                        return false;
                    }
                    for (int j = 0; j<activity.getGroupsId().length; j++) {
                        if (groups.get(activity.getGroupsId()[j]).getActivityGroup(semester, Y - 1, X - 1) != -1
                                && groups.get(activity.getGroupsId()[j]).getActivityGroup(semester, Y - 1, X - 1) != activity.getIdActivity()) {
                            Activity act=activities.get(groups.get(activity.getGroupsId()[j]).getActivityGroup(semester,Y-1,X-1));
                            Utility.message("Grupa "+groups.get(activity.getGroupsId()[j]).getGroupName()+" are alta activitate\n\n"+act.getSubject());
                            return false;
                        }
                    }
                    if (isRoom) {
                        if (room.getActivityRoom(semester,Y-1,X-1) != -1
                                && room.getActivityRoom(semester,Y-1,X-1) != activity.getIdActivity()) {
                            Activity act=activities.get(room.getActivityRoom(semester,Y-1,X-1));
                            Utility.message(("Sala "+room.getRoomName()+" este ocupata\n\n"+act.getSubject()));
                            return false;
                        }
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void addDropHandlingGroupSchedule(StackPane pane) {
        pane.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasContent(labelFormat)&&draggingLabel!=null&&pane.getChildren().isEmpty()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
        });
        pane.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            Activity activity=activities.get(draggingLabel.getActivityId());
            if (db.hasContent(labelFormat)) {
                int     row= GridPane.getRowIndex(pane),
                        col= GridPane.getColumnIndex(pane),
                        time = activity.getTime();
                if (isMovableToGroupSchedule(col,row,time,activity)){
                    Pane parentOfLabel=(Pane) draggingLabel.getParent();
                    parentOfLabel.getChildren().clear();
                    moveToGroupSchedule(pane,col,row,activity, (Stage) pane.getScene().getWindow());
                    draggingLabel = null;
                    e.setDropCompleted(true);
                }
            }
        });
    }

    private void moveToGroupSchedule(StackPane pane, int col, int row, Activity activity, Stage frontStage) {

        StackPane secondPane, actualPane;
        GridPane grid;
        IndexedLabel actualLabel;
        IndexedLabel[] labels;
        int add;
        ObservableList<Node> childrens;
        int X, Y, nodeX, nodeY;
        Room room = null;
        boolean isRoom = false;
        if (activity.getClassRoomId() >= 0) {
            isRoom = true;
            room = rooms.get(activity.getClassRoomId());
        }
        Professor professor = professors.get(activity.getProfessorId());
        int semester = activity.getSemester();
        int time = activity.getTime();


        switch (time % 2) {
            case 1:
                grid = (GridPane) pane.getParent();
                labels = new IndexedLabel[time];
                for (int i = 0; i < time; i++) {
                    labels[i] = Utility.createGroupLabel(activity, professor, groups, rooms);
                    dragTextArea(labels[i]);
                }
                childrens = grid.getChildren();
                for (Node node : childrens) {
                    if (node.getClass() == pane.getClass()) {
                        actualPane = (StackPane) node;
                        if (!actualPane.getChildren().isEmpty()) {
                            if (actualPane.getChildren().get(0).getClass() == draggingLabel.getClass()) {
                                actualLabel = (IndexedLabel) actualPane.getChildren().get(0);
                                if (actualLabel.getActivityId() == draggingLabel.getActivityId()) {
                                    ((Pane) actualLabel.getParent()).getChildren().remove(actualLabel);
                                }
                            }
                        }
                    }
                }
                for (int i = 0; i < HOURS; i++)
                    for (int j = 0; j < DAYS; j++) {
                        if (activity.getIdActivity() == professor.getActivityProfessor(semester, i, j)) {
                            professor.setActivityProfessor(semester, i, j, -1);
                        }
                        for (int k = 0; k < Objects.requireNonNull(activity).getGroupsId().length; k++) {
                            if (activity.getIdActivity() == groups.get(activity.getGroupsId()[k]).getActivityGroup(semester, i, j)) {
                                groups.get(activity.getGroupsId()[k]).setActivityGroup(semester, i, j, -1);
                            }
                        }
                        if (isRoom) {
                            if (activity.getIdActivity() == room.getActivityRoom(semester, i, j)) {
                                room.setActivityRoom(semester, i, j, -1);
                            }
                        }
                    }

                if (row % 2 == 0)
                    add = -1;
                else
                    add = 1;
                for (int t = 0; t < time; t++) {
                    X = row + (t % 2) * add;
                    Y = col + (t + 1) / 2;
                    childrens = grid.getChildren();
                    for (Node node : childrens) {
                        nodeX = GridPane.getRowIndex(node);
                        nodeY = GridPane.getColumnIndex(node);
                        if (nodeX == X && nodeY == Y) {
                            secondPane = (StackPane) node;
                            secondPane.getChildren().add(labels[t]);
                        }
                        if (nodeX >= X && nodeY >= Y) {
                            break;
                        }
                    }
                    professor.setActivityProfessor(semester, Y - 1, X - 1, activity.getIdActivity());
                    for (int j = 0; j < activity.getGroupsId().length; j++) {
                        groups.get(activity.getGroupsId()[j]).setActivityGroup(semester, Y - 1, X - 1, activity.getIdActivity());
                    }
                    if (isRoom) {
                        room.setActivityRoom(semester, Y - 1, X - 1, activity.getIdActivity());
                    }
                }
                break;
            case 0:
                grid = (GridPane) pane.getParent();
                labels = new IndexedLabel[time];
                for (int i = 0; i < time; i++) {
                    labels[i] = Utility.createGroupLabel(activity, professor, groups, rooms);
                    dragTextArea(labels[i]);
                }
                childrens = grid.getChildren();
                for (Node node : childrens) {
                    if (node.getClass() == pane.getClass()) {
                        actualPane = (StackPane) node;
                        if (!actualPane.getChildren().isEmpty()) {
                            if (actualPane.getChildren().get(0).getClass() == draggingLabel.getClass()) {
                                actualLabel = (IndexedLabel) actualPane.getChildren().get(0);
                                if (actualLabel.getActivityId() == draggingLabel.getActivityId()) {
                                    ((Pane) actualLabel.getParent()).getChildren().remove(actualLabel);
                                }
                            }
                        }
                    }
                }
                for (int i = 0; i < HOURS; i++)
                    for (int j = 0; j < DAYS; j++) {
                        if (activity.getIdActivity() == professor.getActivityProfessor(semester, i, j)) {
                            professor.setActivityProfessor(semester, i, j, -1);
                        }
                        for (int k = 0; k < Objects.requireNonNull(activity).getGroupsId().length; k++) {
                            if (activity.getIdActivity() == groups.get(activity.getGroupsId()[k]).getActivityGroup(semester, i, j)) {
                                groups.get(activity.getGroupsId()[k]).setActivityGroup(semester, i, j, -1);
                            }
                        }
                        if (isRoom) {
                            room.setActivityRoom(semester, i, j, -1);
                        }
                    }
                if (row % 2 == 0)
                    row--;
                for (int t = 0; t < time; t++) {
                    X = row + t % 2;
                    Y = col + t / 2;
                    childrens = grid.getChildren();
                    for (Node node : childrens) {
                        nodeX = GridPane.getRowIndex(node);
                        nodeY = GridPane.getColumnIndex(node);
                        if (nodeX == X && nodeY == Y) {
                            secondPane = (StackPane) node;
                            secondPane.getChildren().add(labels[t]);
                        }
                        if (nodeX >= X && nodeY >= Y) {
                            break;
                        }
                    }
                    professor.setActivityProfessor(semester, Y - 1, X - 1, activity.getIdActivity());
                    for (int j = 0; j < activity.getGroupsId().length; j++) {
                        groups.get(activity.getGroupsId()[j]).setActivityGroup(semester, Y - 1, X - 1, activity.getIdActivity());
                    }
                    if (isRoom) {
                        room.setActivityRoom(semester, Y - 1, X - 1, activity.getIdActivity());
                    }
                }
                break;
            default:
                break;
        }

        Stage stageToRefresh = searchForStage("Orar anul " + activity.getYearOfStudy());
        if (stageToRefresh != null) {
            stageToRefresh.close();
            yearScheduleScene(activity.getYearOfStudy(), semester);
        }
        stageToRefresh = searchForStage(professor.getName());
        if (stageToRefresh != null) {
            stageToRefresh.close();
            professorsScheduleScene(professor.getIdProfessor(), semester);
        }
        frontStage.toFront();
    }

    public void yearScheduleScene(int year, int semester) {

        Activity presentActivity;
        Professor professor;
        int presentActivityId;
        ArrayList<Group> groupsOfYear=new ArrayList<>();
        IndexedLabel lbl;

        for (Group g:groups) {
            if (g.getYear()==year) {
                groupsOfYear.add(g);
            }
        }

        int numberOfGroups = groupsOfYear.size();

        String[] legenda={"Anul","Specia\nlizare","Grupa","Subgrupa"};
        String[] ore={"8-9,50","10-11,50","12-13,50","14-15,50","16-17,50","18-19,50","20-21,50"};
        String[] zile={"Luni","Marti","Miercuri","Joi","Vineri","Sambata"};
        Label textLabel;

        Stage scheduleStage=new Stage();

        GridPane windowGrid=new GridPane();
        windowGrid.setAlignment(Pos.CENTER);
        windowGrid.setPadding(new Insets(10,10,10,10));
        windowGrid.getColumnConstraints().add(new ColumnConstraints(240));
        windowGrid.getRowConstraints().add(new RowConstraints(50));

        GridPane legendGrid=new GridPane();
        legendGrid.setAlignment(Pos.CENTER);
        StackPane[] legendList=new StackPane[legenda.length];

        for (int i=0;i<legendList.length;i++) {
            legendList[i]=new StackPane();
            legendList[i].setStyle("-fx-border-color:black; -fx-background-color:beige; -fx-padding:5");
            legendList[i].setMinSize(60,50);
            textLabel=new Label((legenda[i]));
            textLabel.setFont(new Font(10));
            legendList[i].getChildren().add(textLabel);
            legendGrid.add(legendList[i],i,0);
        }

        ScrollPane legendPane=new ScrollPane();
        legendPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        legendPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        legendPane.setContent(legendGrid);
        windowGrid.add(legendPane,0,0);

        GridPane headerGrid=new GridPane();
        headerGrid.setAlignment(Pos.CENTER);
        StackPane[][] headerMatrix=new StackPane[6*HOURS][2];

        for (int i=0;i<DAYS/2;i++) {
            headerMatrix[i][0]=new StackPane();
            headerMatrix[i][0].setStyle("-fx-border-color:black; -fx-background-color:beige;");
            headerMatrix[i][0].setPrefSize(60,25);
            textLabel=new Label((zile[i]));
            textLabel.setFont(new Font(10));
            headerMatrix[i][0].getChildren().add(textLabel);
            headerGrid.add(headerMatrix[i][0],i*7,1,7,1);
        }

        for (int i=0;i<DAYS/2*HOURS;i++) {
            headerMatrix[i][1]=new StackPane();
            headerMatrix[i][1].setStyle("-fx-border-color:#000000; -fx-background-color:#f5f5dc");
            headerMatrix[i][1].setPrefSize(60,25);
            textLabel=new Label(ore[i%HOURS]);
            textLabel.setFont(new Font(10));
            headerMatrix[i][1].getChildren().add(textLabel);
            headerGrid.add(headerMatrix[i][1], i, 2);
        }

        ScrollPane headerScroll=new ScrollPane();
        headerScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        headerScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        headerScroll.setContent(headerGrid);
        windowGrid.add(headerScroll,1,0);

        GridPane leftGrid=new GridPane();
        headerGrid.setAlignment(Pos.CENTER);
        StackPane[][] leftMatrix=new StackPane[legenda.length][numberOfGroups*4];

        for (int i=0;i<legenda.length;i++) {
            for (int j=0;j<numberOfGroups*2;j++) {
                leftMatrix[i][j]=new StackPane();
                leftMatrix[i][j].setStyle("-fx-border-color:black; -fx-background-color:beige");
                switch (i) {
                    case 0:
                        if(j%2==0) {
                            leftMatrix[i][j].setPrefSize(60,100);
                            textLabel=new Label(Integer.toString(groupsOfYear.get(j/2).getYear()));
                            textLabel.setFont(new Font(10));
                            leftMatrix[i][j].getChildren().add(textLabel);
                            leftGrid.add(leftMatrix[i][j], i, j,1,2);
                        }
                        break;
                    case 1:
                        if(j%2==0) {
                            leftMatrix[i][j].setPrefSize(60,100);
                            textLabel=new Label(groupsOfYear.get(j/2).getSpeciality());
                            textLabel.setFont(new Font(10));
                            leftMatrix[i][j].getChildren().add(textLabel);
                            leftGrid.add(leftMatrix[i][j], i, j,1,2);
                        }
                        break;
                    case 2:
                        if(j%2==0) {
                            leftMatrix[i][j].setPrefSize(60,100);
                            textLabel=new Label(groupsOfYear.get(j/2).getGroupName());
                            textLabel.setFont(new Font(10));
                            leftMatrix[i][j].getChildren().add(textLabel);
                            leftGrid.add(leftMatrix[i][j], i, j,1,2);
                        }
                        break;
                    case 3:
                        if(j%2==0) {
                            leftMatrix[i][j].setPrefSize(60,50);
                            textLabel=new Label("A");
                            textLabel.setFont(new Font(10));
                            leftMatrix[i][j].getChildren().add(textLabel);
                            leftGrid.add(leftMatrix[i][j], i, j,1,1);
                        }
                        else {
                            leftMatrix[i][j].setPrefSize(60,50);
                            textLabel=new Label("B");
                            textLabel.setFont(new Font(10));
                            leftMatrix[i][j].getChildren().add(textLabel);
                            leftGrid.add(leftMatrix[i][j], i, j,1,1);
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        ScrollPane leftScroll=new ScrollPane();
        leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScroll.setContent(leftGrid);
        windowGrid.add(leftScroll,0,1);

        GridPane scheduleGrid=new GridPane();
        scheduleGrid.setAlignment(Pos.CENTER);
        StackPane[][] scheduleMatrix=new StackPane[DAYS/2*HOURS][numberOfGroups*4];

        for (int i=0;i<DAYS/2*HOURS;i++)
            for (int j=0;j<numberOfGroups*4;j++){

                int day=i/HOURS;
                Group presentGroup = groupsOfYear.get(j/4);

                scheduleMatrix[i][j]=new StackPane();
                scheduleMatrix[i][j].setPrefSize(60,25);
                scheduleMatrix[i][j].setStyle("-fx-border-color:black");
                scheduleMatrix[i][j].setAlignment(Pos.TOP_CENTER);

                presentActivityId = presentGroup.getActivityGroup(semester,i%HOURS,day*2);

                addDropHandlingYearSchedule(scheduleMatrix[i][j],groupsOfYear);

                if (presentActivityId!=-1) {
                    presentActivity=activities.get(presentActivityId);
                    professor=professors.get(presentActivity.getProfessorId());
                    addDropHandlingYearSchedule(scheduleMatrix[i][j],groupsOfYear);
                    lbl=Utility.createYearLabel(presentActivity, professor, groups,rooms);
                    scheduleMatrix[i][j].getChildren().add(lbl);
                    dragTextArea(lbl);
                }
                scheduleGrid.add(scheduleMatrix[i][j], i, j);

                j++;

                scheduleMatrix[i][j]=new StackPane();
                scheduleMatrix[i][j].setPrefSize(60,25);
                scheduleMatrix[i][j].setStyle("-fx-border-color:black");
                scheduleMatrix[i][j].setAlignment(Pos.TOP_CENTER);

                presentActivityId = presentGroup.getActivityGroup(semester,i%HOURS,day*2+1);

                addDropHandlingYearSchedule(scheduleMatrix[i][j],groupsOfYear);

                if (presentActivityId!=-1) {
                    presentActivity=activities.get(presentActivityId);
                    professor=professors.get(presentActivity.getProfessorId());
                    addDropHandlingYearSchedule(scheduleMatrix[i][j],groupsOfYear);
                    lbl=Utility.createYearLabel(presentActivity, professor, groups,rooms);
                    scheduleMatrix[i][j].getChildren().add(lbl);
                    dragTextArea(lbl);
                }
                scheduleGrid.add(scheduleMatrix[i][j], i, j);
            }

        ScrollPane scheduleScroll=new ScrollPane();
        scheduleScroll.pannableProperty().set(true);
        scheduleScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scheduleScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scheduleScroll.setContent(scheduleGrid);

        setupScrolling(scheduleScroll);

        windowGrid.add(scheduleScroll,1,1);

        headerScroll.hvalueProperty().bindBidirectional(scheduleScroll.hvalueProperty());
        leftScroll.vvalueProperty().bindBidirectional(scheduleScroll.vvalueProperty());

        Scene scheduleScene = new Scene(windowGrid);
        scheduleScroll.autosize();
        scheduleStage.setScene(scheduleScene);
        scheduleStage.setTitle("Orar anul " + year + " semestrul " + semester);

        scheduleStage.setX(10);
        scheduleStage.setY(10);
        scheduleStage.setHeight(Screen.getPrimary().getBounds().getHeight()-50);
        scheduleStage.setWidth(Screen.getPrimary().getBounds().getWidth()-20);

        scheduleStage.show();

        scheduleScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                searchForStage("Meniu principal").toFront();
            }
            if (event.getCode() == KeyCode.TAB) {
                synchronized (this) {
                    SortedList<Stage> stages = StageHelper.getStages().sorted();
                    for (int i = 0; i < stages.size(); i++) {
                        if (stages.get(i).equals(scheduleStage)) {
                            if (stages.get((i + 1) % stages.size()).getTitle().equals("Meniu principal"))
                                i++;
                            stages.get((i + 1) % stages.size()).toFront();
                            break;
                        }
                    }
                }
            }
        });

    }

    private void setupScrolling(ScrollPane scheduleScroll) {
        final double[] xDirection = {0};
        final double[] yDirection = {0};
        Timeline scrolltimeline = new Timeline();
        scrolltimeline.setCycleCount(Timeline.INDEFINITE);
        scrolltimeline.getKeyFrames().add(new KeyFrame(Duration.millis(20), "Scroll", (ActionEvent) -> dragScroll(scheduleScroll, xDirection[0], yDirection[0])));
        scheduleScroll.setOnDragExited(event -> {
            Bounds scrollBound=scheduleScroll.getLayoutBounds();
            Point2D leftUpper = scheduleScroll.localToScene(scrollBound.getMinX(), scrollBound.getMinY());
            Point2D rightDowner = scheduleScroll.localToScene(scrollBound.getMaxX(), scrollBound.getMaxY());
            if (event.getSceneX() >rightDowner.getX()) {
                xDirection[0] =1.0 / 200;
            }
            else if (event.getSceneX() < leftUpper.getX()) {
                xDirection[0] =-1.0 / 200;
            }
            else {
                xDirection[0] =0;
            }
            if (event.getSceneY() > rightDowner.getY() ) {
                yDirection[0] = 1.0 / 200;
            }
            else if (event.getSceneY() < leftUpper.getY() ){
                yDirection[0] =-1.0 / 200;
            }
            else {
                yDirection[0] = 0;
            }
            if (!dropped) {
                scrolltimeline.play();
            }
        });
        scheduleScroll.setOnDragEntered(event -> {
            scrolltimeline.stop();
            dropped = false;
        });
        scheduleScroll.setOnDragDone(event -> scrolltimeline.stop());
        scheduleScroll.setOnDragDropped(event -> {
            scrolltimeline.stop();
            dropped=true;
        });
    }

    private void dragScroll(ScrollPane scheduleScroll,double xDirection,double yDirection) {
        ScrollBar verticalScrollBar = getVerticalScrollbar(scheduleScroll);
        if (verticalScrollBar != null) {
            double newValue = verticalScrollBar.getValue() + yDirection;
            newValue = Math.min(newValue, 1.0);
            newValue = Math.max(newValue, 0.0);
            verticalScrollBar.setValue(newValue);
        }
        ScrollBar horizontalScrollBar = getHorizontalScrollbar(scheduleScroll);
        if (horizontalScrollBar != null) {
            double newValue = horizontalScrollBar.getValue() + xDirection;
            newValue = Math.min(newValue, 1.0);
            newValue = Math.max(newValue, 0.0);
            horizontalScrollBar.setValue(newValue);
        }
    }

    private ScrollBar getVerticalScrollbar(ScrollPane scheduleScroll) {
        ScrollBar result = null;
        for (Node n : scheduleScroll.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) n;
                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                    result = bar;
                }
            }
        }
        return result;
    }

    private ScrollBar getHorizontalScrollbar(ScrollPane scheduleScroll) {
        ScrollBar result = null;
        for (Node n : scheduleScroll.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) n;
                if (bar.getOrientation().equals(Orientation.HORIZONTAL)) {
                    result = bar;
                }
            }
        }
        return result;
    }

    private ArrayList<Integer> isMovableToYearSchedule(int col, int row, int time, Activity activity,ArrayList<Group> groupsOfThisGrid) {

        boolean isRoom=false;
        Room room=null;
        int semester = activity.getSemester();
        Professor professor = professors.get(activity.getProfessorId());
        if (activity.getClassRoomId()>=0) {
            isRoom=true;
            room = rooms.get(activity.getClassRoomId());
        }
        ArrayList<Integer> rows=new ArrayList<>();
        boolean rowOk=false;
        int groupOfThisRow=groupsOfThisGrid.get(row/4).getIdGroup();
        for (int g:activity.getGroupsId()) {
            if (g==groupOfThisRow) {
                rowOk=true;
                break;
            }
        }

        if (!rowOk) {
            Utility.message("Grupa gresita");
            return null;
        }

        if ((col + (time-1)/2) / HOURS > col / HOURS) {
            return null;
        }
        int dir,add;
        int X,Y,colProfSchedule,rowProfSchedule;
        switch (time%2) {
            case 1:
                if (row % 2 == 1) {
                    dir=-1;
                    add=1;
                }
                else {
                    dir = 1;
                    add=0;
                }
                for (int t=0;t<time;t++) {
                    X=row+(t%2)*dir+add;
                    Y=col+(t+1)/2;
                    colProfSchedule=Y%HOURS;
                    rowProfSchedule=Y/HOURS*2+X-row;
                    if (professor.getActivityProfessor(semester, colProfSchedule, rowProfSchedule) != -1
                            && professor.getActivityProfessor(semester, colProfSchedule, rowProfSchedule) != activity.getIdActivity()) {
                        Activity act=activities.get(professor.getActivityProfessor(semester,colProfSchedule,rowProfSchedule));
                        Utility.message(professor.getName()+" are alta activitate\n\n"+act.getSubject());
                        return null;
                    }
                    for (int j = 0; j<activity.getGroupsId().length; j++) {
                        if (groups.get(activity.getGroupsId()[j]).getActivityGroup(semester, colProfSchedule, rowProfSchedule) != -1
                                && groups.get(activity.getGroupsId()[j]).getActivityGroup(semester, colProfSchedule, rowProfSchedule) != activity.getIdActivity()) {
                            Activity act=activities.get(groups.get(activity.getGroupsId()[j]).getActivityGroup(semester,colProfSchedule,rowProfSchedule));
                            Utility.message("Grupa "+groups.get(activity.getGroupsId()[j]).getGroupName()+" are alta activitate\n\n"+act.getSubject());
                            return null;
                        }
                    }
                    if (isRoom) {
                        if (room.getActivityRoom(semester,colProfSchedule,rowProfSchedule) != -1
                                && room.getActivityRoom(semester, colProfSchedule, rowProfSchedule) != activity.getIdActivity()) {
                            Activity act=activities.get(room.getActivityRoom(semester,colProfSchedule,rowProfSchedule));
                            Utility.message(("Sala "+room.getRoomName()+" este ocupata\n\n"+act.getSubject()));
                            return null;
                        }
                    }
                }
                break;
            case 0:
                if (row % 2 == 1)
                    row--;
                for (int t=0;t<time;t++) {
                    X=row+t%2;
                    Y=col+t/2;
                    colProfSchedule=Y%HOURS;
                    rowProfSchedule=Y/HOURS*2+X-row;
                    if (professor.getActivityProfessor(semester,colProfSchedule, rowProfSchedule) != -1
                            && professor.getActivityProfessor(semester,colProfSchedule, rowProfSchedule) != activity.getIdActivity()) {
                        Activity act=activities.get(professor.getActivityProfessor(semester,colProfSchedule,rowProfSchedule));
                        Utility.message(professor.getName()+" are alta activitate\n\n"+act.getSubject());
                        return null;
                    }
                    for (int j = 0; j<activity.getGroupsId().length; j++) {
                        if (groups.get(activity.getGroupsId()[j]).getActivityGroup(semester, colProfSchedule, rowProfSchedule) != -1
                                && groups.get(activity.getGroupsId()[j]).getActivityGroup(semester, colProfSchedule, rowProfSchedule) != activity.getIdActivity()) {
                            Activity act=activities.get(groups.get(activity.getGroupsId()[j]).getActivityGroup(semester,colProfSchedule,rowProfSchedule));
                            Utility.message("Grupa "+groups.get(activity.getGroupsId()[j]).getGroupName()+" are alta activitate\n\n"+act.getSubject());
                            return null;
                        }
                    }
                    if (isRoom) {
                        if (room.getActivityRoom(semester,colProfSchedule,rowProfSchedule) != -1
                                && room.getActivityRoom(semester,colProfSchedule,rowProfSchedule) != activity.getIdActivity()) {
                            Activity act=activities.get(room.getActivityRoom(semester,colProfSchedule,rowProfSchedule));
                            Utility.message(("Sala "+room.getRoomName()+" este ocupata\n\n"+act.getSubject()));
                            return null;
                        }
                    }
                }
                break;
            default:
                break;
        }

        int semigroup;
        if (row%4<2)
            semigroup=2;
        else
            semigroup=-2;
        for (int i=0;i<groupsOfThisGrid.size();i++) {
            for (int g:activity.getGroupsId()) {
                if (g==groupsOfThisGrid.get(i).getIdGroup()) {
                    rows.add(row+(i-row/4)*4);
                    rows.add(row+(i-row/4)*4+semigroup);
                }
            }
        }
        return rows;
    }

    private void addDropHandlingYearSchedule(StackPane pane, ArrayList<Group> groupsOfThisGrid) {
        pane.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasContent(labelFormat)&&draggingLabel!=null) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
        });
        pane.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            Activity activity=activities.get(draggingLabel.getActivityId());
            if (db.hasContent(labelFormat)) {
                int     row = GridPane.getRowIndex(pane),
                        col = GridPane.getColumnIndex(pane),
                        time = activity.getTime();
                ArrayList<Integer> rows= isMovableToYearSchedule(col,row,time,activity,groupsOfThisGrid);
                if (rows!=null){
                    Pane parentOfLabel=(Pane) draggingLabel.getParent();
                    parentOfLabel.getChildren().clear();
                    moveToYearSchedule(pane,col,rows,activity, (Stage) pane.getScene().getWindow());
                    draggingLabel = null;
                    e.setDropCompleted(true);
                }
            }
        });
    }

    private void moveToYearSchedule(StackPane thisPane, int col, ArrayList<Integer> rows, Activity activity, Stage frontStage) {

        StackPane stackPane,actualPane;
        GridPane grid;
        IndexedLabel actualLabel;
        IndexedLabel[] labels;
        int dir;
        ObservableList<Node> childes;
        int X,Y,nodeX,nodeY;
        Room room=null;
        boolean isRoom=false;
        if (activity.getClassRoomId()>=0) {
            isRoom=true;
            room=rooms.get(activity.getClassRoomId());
        }
        Professor professor=professors.get(activity.getProfessorId());
        int semester=activity.getSemester();
        int time=activity.getTime();

        switch (time%2) {

            case 1:

                if (thisPane.getParent().getClass().getName().equals("javafx.scene.layout.GridPane"))
                    grid=(GridPane) thisPane.getParent();
                else {
                    return;
                }
                labels=new IndexedLabel[time*rows.size()];
                for (int i=0;i<(time*rows.size());i++) {
                    labels[i] = Utility.createYearLabel(activity, professor, groups,rooms);
                    dragTextArea(labels[i]);
                }
                childes = grid.getChildren();
                for (Node node:childes){
                    if (node.getClass()==thisPane.getClass()) {
                        actualPane = (StackPane) node;
                        if (!actualPane.getChildren().isEmpty()) {
                            if (actualPane.getChildren().get(0).getClass()==draggingLabel.getClass()) {
                                actualLabel = (IndexedLabel) actualPane.getChildren().get(0);
                                if (actualLabel.getActivityId() == draggingLabel.getActivityId()) {
                                    ((Pane) actualLabel.getParent()).getChildren().remove(actualLabel);
                                }
                            }
                        }
                    }
                }
                for (int i=0;i<HOURS;i++) {
                    for (int j=0;j<DAYS;j++) {
                        if (activity.getIdActivity() == professor.getActivityProfessor(semester, i, j)) {
                            professor.setActivityProfessor(semester, i, j, -1);
                        }
                        for (int k = 0; k < Objects.requireNonNull(activity).getGroupsId().length; k++) {
                            if (activity.getIdActivity() == groups.get(activity.getGroupsId()[k]).getActivityGroup(semester, i, j)) {
                                groups.get(activity.getGroupsId()[k]).setActivityGroup(semester, i, j, -1);
                            }
                        }
                        if (isRoom) {
                            if (activity.getIdActivity() == room.getActivityRoom(semester,i,j)) {
                                room.setActivityRoom(semester,i,j,-1);
                            }
                        }
                    }
                }

                for (int i=0; i<rows.size();i++) {
                    int row=rows.get(i);
                    int add;
                    if (row % 2 == 1) {
                        dir = -1;
                        add=1;
                    }
                    else {
                        dir = 1;
                        add=0;
                    }
                    for (int t = 0; t < time; t++) {
                        X = row + (t % 2) * dir;
                        Y = col + (t + 1) / 2;
                        childes = grid.getChildren();
                        for (Node node : childes) {
                            nodeX = GridPane.getRowIndex(node);
                            nodeY = GridPane.getColumnIndex(node);
                            if (nodeX == X && nodeY == Y) {
                                stackPane = (StackPane) node;
                                stackPane.getChildren().add(labels[t+i*time]);
                            }
                            if (nodeX >= X && nodeY >= Y) {
                                break;
                            }
                        }
                        professor.setActivityProfessor(semester, Y % HOURS, Y / HOURS * 2 + X - row+add, activity.getIdActivity());
                        for (int j = 0; j < activity.getGroupsId().length; j++)
                            groups.get(activity.getGroupsId()[j]).setActivityGroup(semester, Y % HOURS, Y / HOURS * 2 + X - row+add, activity.getIdActivity());
                    }
                }
                break;

            case 0:

                grid=(GridPane) thisPane.getParent();
                labels=new IndexedLabel[time*rows.size()];
                for (int i=0;i<time*rows.size();i++) {
                    labels[i] = Utility.createYearLabel(activity, professor, groups,rooms);
                    dragTextArea(labels[i]);
                }
                childes = grid.getChildren();
                for (Node node:childes){
                    if (node.getClass()==thisPane.getClass()) {
                        actualPane = (StackPane) node;
                        if (!actualPane.getChildren().isEmpty()) {
                            if (actualPane.getChildren().get(0).getClass()==draggingLabel.getClass()) {
                                actualLabel = (IndexedLabel) actualPane.getChildren().get(0);
                                if (actualLabel.getActivityId() == draggingLabel.getActivityId()) {
                                    ((Pane) actualLabel.getParent()).getChildren().remove(actualLabel);
                                }
                            }
                        }
                    }
                }
                for (int i=0;i<HOURS;i++)
                    for (int j=0;j<DAYS;j++){
                        if (activity.getIdActivity()==professor.getActivityProfessor(semester,i,j))
                            professor.setActivityProfessor(semester,i,j,-1);
                        for (int k = 0; k< Objects.requireNonNull(activity).getGroupsId().length; k++)
                            if (activity.getIdActivity()==groups.get(activity.getGroupsId()[k]).getActivityGroup(semester,i,j))
                                groups.get(activity.getGroupsId()[k]).setActivityGroup(semester,i,j,-1);
                    }

                for (int i=0; i<rows.size();i++) {
                    int row=rows.get(i);
                    if (row % 2 == 1)
                        row--;
                    for (int t = 0; t < time; t++) {
                        X = row + t % 2;
                        Y = col + t / 2;
                        childes = grid.getChildren();
                        for (Node node : childes) {
                            nodeX = GridPane.getRowIndex(node);
                            nodeY = GridPane.getColumnIndex(node);
                            if (nodeX == X && nodeY == Y) {
                                stackPane = (StackPane) node;
                                stackPane.getChildren().add(labels[t+i*time]);
                            }
                            if (nodeX >= X && nodeY >= Y) {
                                break;
                            }
                        }
                        professor.setActivityProfessor(semester, Y % HOURS, Y / HOURS * 2 + X - row, activity.getIdActivity());
                        for (int j = 0; j < activity.getGroupsId().length; j++)
                            groups.get(activity.getGroupsId()[j]).setActivityGroup(semester, Y % HOURS, Y / HOURS * 2 + X - row, activity.getIdActivity());
                    }
                }
                break;
            default:
                break;
        }

        Stage stageToRefresh=searchForStage(professor.getName());
        if (stageToRefresh!=null) {
            stageToRefresh.close();
            professorsScheduleScene(professor.getIdProfessor(), semester);
        }
        for (int group:activity.getGroupsId()) {
            stageToRefresh = searchForStage(groups.get(group).getGroupName());
            if (stageToRefresh!=null) {
                stageToRefresh.close();
                groupsScheduleScene(group, semester);
            }
        }
        frontStage.toFront();
    }

    public Stage searchForStage(String title) {

        synchronized (this) {
            for (Stage stage : StageHelper.getStages()) {
                if (stage.getTitle().contains(title)) {
                    return stage;
                }
            }
        }
        return null;
    }

}