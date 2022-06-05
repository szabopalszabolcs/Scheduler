import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class NoSheetException extends Exception{
    NoSheetException(String ex){
        super(ex);
        Utility.message("Format fișier stat de funcții necorespunzător");
    }
}

public class Utility {

    private Utility() {
        throw new UnsupportedOperationException();
    }

    static final int HOURS = 7, DAYS = 12;
    static int profIndex = 0, groupIndex = 0, activityIndex = 0;

    static int max(int a, int b, int c, int d) {
        if (a > b) b = a;
        if (b > c) c = b;
        if (c > d) d = c;
        return d;
    }

    public static Pair<String, String> readFile() {

        Stage chooserStage=new Stage();
        chooserStage.setTitle("Deschidere fișier stat de funcțiuni");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel files", "*.xls"));
        File selectedFile = fileChooser.showOpenDialog(chooserStage);
        if (selectedFile == null) {
            chooserStage.close();
            return null;
        }

        String faculty;
        String fileName=selectedFile.getAbsolutePath();

        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(fileInputStream);
            int numberOfSheets = hssfWorkbook.getNumberOfSheets();
            int sheetToRead = -1;
            for (int i = 0; i < numberOfSheets; i++) {
                if (hssfWorkbook.getSheetName(i).equals("DateC")) {
                    sheetToRead = i;
                    i = numberOfSheets;
                }
            }
            if (sheetToRead == -1) {
                throw new NoSheetException("Sheet inexistent");
            }
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(sheetToRead);
            Row row=hssfSheet.getRow(2);
            faculty=row.getCell(12).toString();
            if (faculty.length()<1) {
                return null;
            }
        }
        catch (Exception ex) {
            return null;
        }
        return new Pair<>(fileName,faculty);
    }

    public static ArrayList<Activity> readXls(String fileName, ArrayList<Professor> professors, ArrayList<Group> groups, ArrayList<Room> rooms, String faculty){

        ArrayList<Activity> activities=new ArrayList<>();
        File file=new File(fileName);
        profIndex=0;
        groupIndex=0;
        activityIndex=0;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(fileInputStream);
            int numberOfSheets=hssfWorkbook.getNumberOfSheets();
            int sheetToRead=-1;

            for (int i=0;i<numberOfSheets;i++){
                if (hssfWorkbook.getSheetName(i).equals("Centr")) {
                    sheetToRead=i;
                    i=numberOfSheets;
                }
            }
            if (sheetToRead==-1) {
                throw new NoSheetException("Sheet inexistent");
            }
            HSSFSheet hssfSheet=hssfWorkbook.getSheetAt(sheetToRead);
//start row
            int r=10;
            while (!hssfSheet.getRow(r).getCell(1).toString().equals("")){
                Row firstRow=hssfSheet.getRow(r);

                if (faculty.equals(firstRow.getCell(2).toString())){
                    String ok=firstRow.getCell(17).toString();
                    if (ok.equals("ok")) {
                        Row secondRow = hssfSheet.getRow(r + 1);
                        String subject = firstRow.getCell(1).toString().trim();
                        String departament = firstRow.getCell(4).toString().trim();
                        String[] speciality = firstRow.getCell(5).toString().split("\\+");
                        int year = (int) firstRow.getCell(6).getNumericCellValue();
                        String[] codeFormation = secondRow.getCell(1).toString().split(",");
                        String codeSubject = codeFormation[0];
                        int fCrs = Integer.parseInt(codeFormation[1]);
                        int fSem = Integer.parseInt(codeFormation[2]);
                        int fLab = Integer.parseInt(codeFormation[3]);
                        int fPrc = Integer.parseInt(codeFormation[4]);
                        int s1C, s1S, s1L, s1P, s2C, s2S, s2L, s2P;
                        if (secondRow.getCell(8, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null)
                            s1C = (int) secondRow.getCell(8).getNumericCellValue();
                        else s1C = 0;
                        if (secondRow.getCell(9, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null)
                            s1S = (int) secondRow.getCell(9).getNumericCellValue();
                        else s1S = 0;
                        if (secondRow.getCell(10, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null)
                            s1L = (int) secondRow.getCell(10).getNumericCellValue();
                        else s1L = 0;
                        if (secondRow.getCell(11, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null)
                            s1P = (int) secondRow.getCell(11).getNumericCellValue();
                        else s1P = 0;
                        if (secondRow.getCell(13, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null)
                            s2C = (int) secondRow.getCell(13).getNumericCellValue();
                        else s2C = 0;
                        if (secondRow.getCell(14, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null)
                            s2S = (int) secondRow.getCell(14).getNumericCellValue();
                        else s2S = 0;
                        if (secondRow.getCell(15, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null)
                            s2L = (int) secondRow.getCell(15).getNumericCellValue();
                        else s2L = 0;
                        if (secondRow.getCell(16, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null)
                            s2P = (int) secondRow.getCell(16).getNumericCellValue();
                        else s2P = 0;

                        int semester = 0;
                        if (s1C + s1S + s1L + s1P > 0) semester = 1;
                        else if (s2C + s2S + s2L + s2P > 0) semester = 2;
//profesori care predau la aceasta disciplina si grupe
                        if (semester > 0) {
                            int c = 27;
                            while (!secondRow.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).toString().equals("")) {
                                c++;
                            }
                            int nrProfs = c - 27;
                            if (nrProfs > 0) {
                                Professor[] actualProfs = new Professor[nrProfs];
                                String[] profNames = new String[nrProfs];
                                int[] crsTime = new int[nrProfs];
                                int[] semTime = new int[nrProfs];
                                int[] labTime = new int[nrProfs];
                                int[] prcTime = new int[nrProfs];
                                int crsTotal = 0;
                                int semTotal = 0;
                                int labTotal = 0;
                                int prcTotal = 0;

                                for (int i = 0; i < nrProfs; i++) {
                                    String[] profData = secondRow.getCell(27 + i).toString().split(";");
                                    profNames[i] = profData[0];
                                    crsTime[i] = Integer.parseInt(profData[1]);
                                    crsTotal += crsTime[i];
                                    semTime[i] = Integer.parseInt(profData[2]);
                                    semTotal += semTime[i];
                                    labTime[i] = Integer.parseInt(profData[3]);
                                    labTotal += labTime[i];
                                    prcTime[i] = Integer.parseInt(profData[4]);
                                    prcTotal += prcTime[i];
                                    actualProfs[i] = new Professor(0, profNames[i]);
                                    actualProfs[i] = addIfNotInProfs(actualProfs[i], professors);
                                }

                                for (int i = 0; i < nrProfs; i++) {
                                    for (int j = i + 1; j < nrProfs; j++)
                                        if (actualProfs[i] != null && actualProfs[j] != null && actualProfs[i].equals(actualProfs[j])) {
                                            crsTime[i] += crsTime[j];
                                            crsTime[j] = 0;
                                            semTime[i] += semTime[j];
                                            semTime[j] = 0;
                                            labTime[i] += labTime[j];
                                            labTime[j] = 0;
                                            prcTime[i] += prcTime[j];
                                            prcTime[j] = 0;
                                            actualProfs[j] = null;
                                        }
                                }

                                int numberOfGroups = max(fCrs, fSem, fLab, fPrc);
                                Group[][] actualGroups = new Group[4][numberOfGroups * nrProfs];
                                for (int i = 0; i < numberOfGroups; i++) {
                                    actualGroups[0][i] = new Group(0, speciality[0], year, i + 1);
                                    actualGroups[0][i] = addIfNotInGroup(actualGroups[0][i], groups);
                                    for (int j = 1; j < 4; j++) {
                                        actualGroups[j][i] = actualGroups[0][i];
                                    }
                                }
                                for (int i = 1; i < nrProfs; i++) {
                                    for (int k = 0; k < numberOfGroups; k++) {
                                        for (int j = 0; j < 4; j++) {
                                            actualGroups[j][i * numberOfGroups + k] = actualGroups[j][k];
                                        }
                                    }
                                }
                                for (int i = 0; i < nrProfs; i++) {
                                    if (crsTime[i] > 0) {
                                        int type = 1;
                                        createActivities(activities, professors, groups, i, numberOfGroups, fCrs, actualGroups, subject, codeSubject, actualProfs, type, semester, year, crsTotal, crsTime, s1C + s2C);
                                    }
                                    if (semTime[i] > 0) {
                                        int type = 2;
                                        createActivities(activities, professors, groups, i, numberOfGroups, fSem, actualGroups, subject, codeSubject, actualProfs, type, semester, year, semTotal, semTime, s1C + s2C);
                                    }
                                    if (labTime[i] > 0) {
                                        int type = 3;
                                        createActivities(activities, professors, groups, i, numberOfGroups, fLab, actualGroups, subject, codeSubject, actualProfs, type, semester, year, labTotal, labTime, s1C + s2C + 1);
                                    }
                                    if (prcTime[i] > 0) {
                                        int type = 4;
                                        createActivities(activities, professors, groups, i, numberOfGroups, fPrc, actualGroups, subject, codeSubject, actualProfs, type, semester, year, prcTotal, prcTime, s1C + s2C + 1);
                                    }
                                }
                                if (s1C + s2C == 0) {
                                    List<Group> groupsToAddToCourse=new ArrayList<>();
                                    boolean isIn;
                                    for (Group newGroup:actualGroups[0]) {
                                        isIn=false;
                                        for(Group oldGroup:groupsToAddToCourse) {
                                            if (oldGroup == newGroup) {
                                                isIn = true;
                                                break;
                                            }
                                        }
                                        if (!isIn) {
                                            groupsToAddToCourse.add(newGroup);
                                        }
                                    }
                                    Group[] groupsToAdd = groupsToAddToCourse.toArray(new Group[0]);
                                    for (Activity completable:activities){
                                        if (completable.getCodeSubject().equals(codeSubject)) {
                                            if (completable.getType()==1) {
                                                completable.addGroups(groupsToAdd);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                r=r+2;
            }
        }
        catch (Exception ex){
            return null;
        }
        return activities;
    }

    static Group addIfNotInGroup(Group group, ArrayList<Group> groups){
        for (Group nextGroup:groups){
            if (group.getGroupName().equals(nextGroup.getGroupName())){
                return nextGroup;
            }
        }
        group.setIdGroup(groupIndex);
        groups.add(group);
        groupIndex++;
        return group;
    }

    static Professor addIfNotInProfs(Professor professor, ArrayList<Professor> professors) {
        for (Professor nextProfessor : professors) {
            if (professor.getName().equals(nextProfessor.getName())) {
                return nextProfessor;
            }
        }
        professor.setIdProfessor(profIndex);
        professors.add(professor);
        profIndex++;
        return professor;
    }

    static void createActivities(ArrayList<Activity> activities, ArrayList<Professor> professors, ArrayList<Group> groups, int profNumber, int numberOfGroups, int fAct, Group[][] actualGroups, String subject, String codeSubject, Professor[] actualProfs, int type, int semester, int year, int actTotal, int[] actTime, int numberOfCourses) {

        float nrAct=(float) fAct*actTime[profNumber]/actTotal;

        for (int j = 0; j < nrAct ; j++) {
            int groupTeam = (numberOfGroups/fAct);
            Group[] groupsToAdd = new Group[groupTeam];
            int[] groupIdToAdd=new int[groupTeam];
            int k = 0, l = 0;
            while (k < groupTeam) {
                if (actualGroups[type-1][j + k + l]!=null) {
                    groupsToAdd[k] = actualGroups[type-1][j + k + l];
                    groupIdToAdd[k] = actualGroups[type-1][j + k + l].getIdGroup();
                    actualGroups[type-1][j + k + l]=null;
                    k++;
                }
                else {
                    l++;
                }
            }
            float activityTime;
            if (nrAct<1)
                activityTime=(float) fAct/actTotal;
            else
                activityTime=(float) actTotal/fAct/2;
            activityTime*=2;
            if (activityTime<1) activityTime=1;
            Activity newActivity = new Activity(activityIndex, subject, codeSubject, actualProfs[profNumber].getIdProfessor(), type, groupIdToAdd, semester, year, (int) activityTime, (activityTime >=2));
            if(numberOfCourses==-1){
                for (Activity nextActivity:activities){
                    if((newActivity.getSubject().equals(nextActivity.getSubject())&&(nextActivity.getType()==1))){
                        nextActivity.addGroups(groupsToAdd);
                    }
                }
            }
            activities.add(newActivity);
            professors.get(newActivity.getProfessorId()).addActivity(activities.indexOf(newActivity));
            activityIndex++;
            for (int group : newActivity.getGroupsId()) {
                groups.get(group).addActivity(activities.indexOf(newActivity));
            }
        }
    }

    public static String openFile() {

        Stage stage=new Stage();
        stage.setTitle("Deschidere fișier date");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Data files","*.act", "*.grp", "*.prf", "*.rms"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            stage.close();
            return null;
        }

        String fileName=selectedFile.getAbsolutePath();
        String[] fileNamePieces =fileName.split("\\.");
        fileName="";
        if (fileNamePieces.length>1) {
            for (int i = 0; i < fileNamePieces.length - 1; i++) {
                fileName = fileName + fileNamePieces[i] + ".";
            }
        }
        else {
            fileName=fileNamePieces[0];
        }

        return fileName;

    }

    public static ArrayList<Activity> loadActivities(String file) {

        ArrayList<Activity> activities;
        Gson gson=new Gson();
        try {
            Reader actReader = Files.newBufferedReader(Paths.get(file));
            activities = gson.fromJson(actReader, new TypeToken<ArrayList<Activity>>() {}.getType());
            actReader.close();
        }
        catch (Exception ex) {
            Utility.message("Citire activități eșuată");
            return null;
        }
        return activities;
    }

    public static ArrayList<Professor> loadProfessors(String file) {

        ArrayList<Professor> professors;
        Gson gson=new Gson();
        try {
            Reader profReader = Files.newBufferedReader(Paths.get(file));
            professors = gson.fromJson(profReader, new TypeToken<ArrayList<Professor>>() {}.getType());
            profReader.close();
        }
        catch (Exception ex) {
            Utility.message("Citire profesori eșuată");
            return null;
        }
        return professors;
    }

    public static ArrayList<Group> loadGroups(String file) {

        ArrayList<Group> groups;
        Gson gson=new Gson();
        try {
            Reader grpReader = Files.newBufferedReader(Paths.get(file));
            groups = gson.fromJson(grpReader, new TypeToken<ArrayList<Group>>() {}.getType());
            grpReader.close();
        }
        catch (Exception ex) {
            Utility.message("Citire grupe eșuată");
            return null;
        }
        return groups;
    }

    public static ArrayList<Room> loadRooms(String file) {

        ArrayList<Room> rooms;
        Gson gson=new Gson();
        try {
            Reader rmReader = Files.newBufferedReader(Paths.get(file));
            rooms = gson.fromJson(rmReader, new TypeToken<ArrayList<Room>>() {}.getType());
            rmReader.close();
        }
        catch (Exception ex) {
            Utility.message("Citire săli eșuată");
            return null;
        }
        return rooms;
    }

    public static String saveFile() {

        Stage stage=new Stage();
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile == null) {
            stage.close();
            return null;
        }

        String fileName=selectedFile.getAbsolutePath();
        String[] fileNamePieces =fileName.split("\\.");
        fileName="";
        if (fileNamePieces.length>1) {
            for (int i = 0; i < fileNamePieces.length - 1; i++) {
                fileName = fileName + fileNamePieces[i];
            }
        }
        else {
            fileName=fileNamePieces[0];
        }

        return fileName;

    }

    public static boolean saveData(String file, ArrayList<Professor> professors, ArrayList<Group> groups, ArrayList<Activity> activities, ArrayList<Room> rooms) {

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        try {
            FileWriter actWriter=new FileWriter(file+".act");
            gson.toJson(activities,actWriter);
            actWriter.close();
        } catch (Exception ex) {
            Utility.message("Salvare activități eșuată");
            return false;
        }

        try {
            FileWriter profWriter=new FileWriter(file+".prf");
            gson.toJson(professors,profWriter);
            profWriter.close();
        } catch (Exception ex) {
            Utility.message("Salvare profesori eșuată");
            return false;
        }

        try {
            FileWriter groupWriter=new FileWriter(file+".grp");
            gson.toJson(groups,groupWriter);
            groupWriter.close();
        } catch (Exception ex) {
            Utility.message("Salvare grupe eșuată");
            return false;
        }

        try {
            FileWriter roomWriter = new FileWriter(file + ".rms");
            gson.toJson(rooms, roomWriter);
            roomWriter.close();
        } catch (Exception ex) {
            Utility.message("Salvare săli eșuată");
            return false;
        }

        return true;
    }

    public static boolean writeXls(String file, ArrayList<Professor> professors, ArrayList<Group> groups,
                                   ArrayList<Activity> activities, ArrayList<Room> rooms, int semester) {

        file = file + ".xls";

        int numberOfYears = Utility.maxYear(activities);
        ArrayList<Integer> listToAdd = new ArrayList<>();
        //ArrayList<Integer[]> groupsOfYear=new ArrayList<Integer[]>();
        Integer[][] groupsOfYear = new Integer[numberOfYears][];

        for (int i = 0; i < numberOfYears; i++) {
            listToAdd.clear();
            for (Group group : groups) {
                if (group.getYear() == i + 1) {
                    listToAdd.add(group.getIdGroup());
                }
            }
            Integer[] arrayToAdd = new Integer[listToAdd.size()];
            for (int j = 0; j < listToAdd.size(); j++) {
                arrayToAdd[j] = listToAdd.get(j);
            }
            groupsOfYear[i] = arrayToAdd;
        }

        String[] days = {"LUNI", "MARTI", "MIERCURI", "JOI", "VINERI", "SAMBATA"};
        String[] names = {"Anul", "Spec.", "Grupa", "Sgr."};
        String[] hours = {"8-9,50", "10-11,50", "12-13,50", "14-15,50", "16-17,50", "18-19,50", "20-21,50"};

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("OrarSt");

        CellStyle style = workbook.createCellStyle();
        HSSFFont font = workbook.createFont();
        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 10);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setFont(font);

        int numberOfColumns = names.length + days.length * hours.length;

        for (int column = 0; column < numberOfColumns; column++) {
            sheet.setColumnWidth(column, 3452);
        }

        int rowNumber = 1;

        for (int year = 0; year < numberOfYears; year++) {
            Row daysRow = sheet.createRow(rowNumber++);
            Row hoursRow = sheet.createRow(rowNumber++);
            for (int j = 0; j < names.length; j++) {
                Cell cell = hoursRow.createCell(j);
                cell.setCellStyle(style);
                cell.setCellValue(names[j]);
            }
            for (int j = 0; j < HOURS * DAYS / 2; j++) {
                Cell daysCell = daysRow.createCell(4 + j);
                daysCell.setCellValue(days[j / 7]);
                daysCell.setCellStyle(style);
                Cell hoursCell = hoursRow.createCell(4 + j);
                hoursCell.setCellValue(hours[j % 7]);
                hoursCell.setCellStyle(style);
            }
            sheet.addMergedRegion(new CellRangeAddress(rowNumber - 2, rowNumber - 2, 0, 3));
            for (int j = 0; j < 6; j++) {
                sheet.addMergedRegion(new CellRangeAddress(rowNumber - 2, rowNumber - 2, j * 7 + 4, j * 7 + 10));
            }
            for (int groupNo = 0; groupNo < groupsOfYear[year].length; groupNo++) {
                for (int subRow = 0; subRow < 4; subRow++) {
                    Group group = groups.get(groupsOfYear[year][groupNo]);
                    Row row = sheet.createRow(rowNumber++);
                    font.setFontHeightInPoints((short) 10);
                    style.setFont(font);
                    Cell c0 = row.createCell(0);
                    c0.setCellValue(group.getYear());
                    c0.setCellStyle(style);
                    Cell c1 = row.createCell(1);
                    c1.setCellValue(group.getSpeciality().toUpperCase());
                    c1.setCellStyle(style);
                    Cell c2 = row.createCell(2);
                    c2.setCellValue(group.getGroupName());
                    c2.setCellStyle(style);
                    Cell c3 = row.createCell(3);
                    c3.setCellStyle(style);
                    c3.setCellValue((subRow < 2) ? "A" : "B");
                    font.setFontHeightInPoints((short) 9);
                    style.setFont(font);
                    for (int column = 0; column < HOURS * DAYS / 2; column++) {
                        int day = column / HOURS * 2 + subRow % 2;
                        int hour = column % HOURS;
                        String cellValue = "";
                        if (group.getActivityGroup(semester, hour, day) >= 0) {
                            Activity activity = activities.get(group.getActivityGroup(semester, hour, day));
                            String professor = professors.get(activity.getProfessorId()).getShortName();
                            String room = ((activity.getClassRoomId() == -1) ? "_" : rooms.get(activity.getClassRoomId()).getRoomName());
                            String activityShort = activity.getCodeSubject();
                            String activityType = activity.getTypeChar();
                            cellValue = activityShort + "," + activityType + "," + room + ", " + professor;
                        }
                        Cell cell = row.createCell(column + 4);
                        cell.setCellStyle(style);
                        cell.setCellValue(cellValue);
                    }
                }
                sheet.addMergedRegion(new CellRangeAddress(rowNumber - 4, rowNumber - 1, 0, 0));
                sheet.addMergedRegion(new CellRangeAddress(rowNumber - 4, rowNumber - 1, 1, 1));
                sheet.addMergedRegion(new CellRangeAddress(rowNumber - 4, rowNumber - 1, 2, 2));
                sheet.addMergedRegion(new CellRangeAddress(rowNumber - 4, rowNumber - 3, 3, 3));
                sheet.addMergedRegion(new CellRangeAddress(rowNumber - 2, rowNumber - 1, 3, 3));
            }
            rowNumber++;
        }

        for (int column = 0; column < 4; column++) {
            sheet.autoSizeColumn(column);
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            workbook.close();
            fileOutputStream.close();
            Utility.message("Exportarea s-a terminat cu succes");
        } catch (Exception exception) {
            Utility.message("Exportarea datelor a eșuat");
            return false;
        }
        return true;
    }

    public static IndexedLabel createGroupLabel(Activity currentActivity, Professor professor, ArrayList<Group> groups,
                                                ArrayList<Room> rooms) {

        int[] groupId = new int[groups.size()];

        for (int i = 0; i < groups.size(); i++) {
            groupId[i] = groups.get(i).getIdGroup();
        }

        IndexedLabel lbl = new IndexedLabel(currentActivity.getIdActivity(), professor.getIdProfessor(),groupId);

        int time=currentActivity.getTime();
        lbl.setPrefSize(80,40*time);
        lbl.setFont(Font.font(8));
        lbl.setTextAlignment(TextAlignment.CENTER);
        lbl.setAlignment(Pos.CENTER);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-border:black;");
        lbl.setText(professor.getShortName()+"\n"+currentActivity.getCodeSubject()+","+currentActivity.getTypeChar());
        switch (currentActivity.getType()) {
            case 1:
                lbl.setStyle("-fx-background-color:LIGHTSALMON;");
                break;
            case 2:
                lbl.setStyle("-fx-background-color:LIGHTBLUE;");
                break;
            case 3:
                lbl.setStyle("-fx-background-color:LIGHTGREEN;");
                break;
            case 4:
                lbl.setStyle("-fx-background-color:LIGHTORANGE;");
                break;
            default:
                lbl.setStyle("-fx-background-color:BLACK;");
        }
        String tipText = "";
        tipText += professor.getName() + "\n";
        tipText += currentActivity.getSubject() + "\n";
        for (int group : currentActivity.getGroupsId()) {
            tipText += groups.get(group).getGroupName() + " ";
        }
        if (currentActivity.getClassRoomId() == -1) {
            tipText += "\nSala : -";
        } else {
            tipText += "\nSala : " + rooms.get(currentActivity.getClassRoomId()).getRoomName();
        }
        Tooltip tooltip = new Tooltip(tipText);
        tooltip.setFont(Font.font(String.valueOf(FontFamily.SCRIPT), FontWeight.BOLD, FontPosture.REGULAR, 10));
        tooltip.setTextAlignment(TextAlignment.CENTER);
        lbl.setTooltip(tooltip);
        return lbl;
    }

    public static IndexedLabel createProfLabel(Activity currentActivity, Professor professor, ArrayList<Group> groups,ArrayList<Room> rooms) {

        int[] groupId=new int[groups.size()];

        for (int i=0;i<groups.size();i++) {
            groupId[i]=groups.get(i).getIdGroup();
        }

        IndexedLabel lbl = new IndexedLabel(currentActivity.getIdActivity(), professor.getIdProfessor(),groupId);

        int time=currentActivity.getTime();
        lbl.setPrefSize(80,40*time);
        lbl.setFont(Font.font(8));
        lbl.setTextAlignment(TextAlignment.CENTER);
        lbl.setAlignment(Pos.CENTER);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-border:black;");
        StringBuilder groupsNames= new StringBuilder();
        for (int g = 0; g<currentActivity.getGroupsId().length; g++) {
            groupsNames.append(groups.get(currentActivity.getGroupsId()[g]).getGroupName()).append(" ");
        }
        lbl.setText(groupsNames+"\n"+currentActivity.getCodeSubject()+","+currentActivity.getTypeChar());
        switch (currentActivity.getType()) {
            case 1:
                lbl.setStyle("-fx-background-color:LIGHTSALMON;");
                break;
            case 2:
                lbl.setStyle("-fx-background-color:LIGHTBLUE;");
                break;
            case 3:
                lbl.setStyle("-fx-background-color:LIGHTGREEN;");
                break;
            case 4:
                lbl.setStyle("-fx-background-color:LIGHTORANGE;");
                break;
            default:
                lbl.setStyle("-fx-background-color:BLACK;");
        }
        String tipText = "";
        tipText += professor.getName() + "\n";
        tipText += currentActivity.getSubject() + "\n";
        for (int group : currentActivity.getGroupsId()) {
            tipText += groups.get(group).getGroupName() + " ";
        }
        if (currentActivity.getClassRoomId() == -1) {
            tipText += "\nSala : -";
        } else {
            tipText += "\nSala : " + rooms.get(currentActivity.getClassRoomId()).getRoomName();
        }
        Tooltip tooltip = new Tooltip(tipText);
        tooltip.setFont(Font.font(String.valueOf(FontFamily.SCRIPT), FontWeight.BOLD, FontPosture.REGULAR, 10));
        tooltip.setTextAlignment(TextAlignment.CENTER);
        lbl.setTooltip(tooltip);
        return lbl;
    }

    public static IndexedLabel createYearLabel(Activity currentActivity, Professor professor, ArrayList<Group> groups,ArrayList<Room> rooms) {
        int[] groupId=new int[groups.size()];
        String classRoom;
        for (int i=0;i<groups.size();i++) {
            groupId[i]=groups.get(i).getIdGroup();
        }
        IndexedLabel lbl = new IndexedLabel(currentActivity.getIdActivity(), professor.getIdProfessor(),groupId);
        lbl.setPrefSize(60, 25);
        lbl.setFont(Font.font(8));
        lbl.setTextAlignment(TextAlignment.CENTER);
        lbl.setAlignment(Pos.CENTER);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-border:black;");
        if (currentActivity.getClassRoomId()>=0) {
            classRoom=rooms.get(currentActivity.getClassRoomId()).getRoomName();
        }
        else {
            classRoom="";
        }
        lbl.setText(currentActivity.getCodeSubject()+","+currentActivity.getTypeChar()+","+classRoom+",\n"+professor.getShortName());
        switch (currentActivity.getType()) {
            case 1:
                lbl.setStyle("-fx-background-color:LIGHTSALMON;");
                break;
            case 2:
                lbl.setStyle("-fx-background-color:LIGHTBLUE;");
                break;
            case 3:
                lbl.setStyle("-fx-background-color:LIGHTGREEN;");
                break;
            case 4:
                lbl.setStyle("-fx-background-color:LIGHTORANGE;");
                break;
            default:
                lbl.setStyle("-fx-background-color:BLACK;");
        }
        String tipText = "";
        tipText += professor.getName() + "\n";
        tipText += currentActivity.getSubject() + "\n";
        for (int group : currentActivity.getGroupsId()) {
            tipText += groups.get(group).getGroupName() + " ";
        }
        if (currentActivity.getClassRoomId() == -1) {
            tipText += "\nSala : -";
        } else {
            tipText += "\nSala : " + rooms.get(currentActivity.getClassRoomId()).getRoomName();
        }
        Tooltip tooltip = new Tooltip(tipText);
        tooltip.setFont(Font.font(String.valueOf(FontFamily.SCRIPT), FontWeight.BOLD, FontPosture.REGULAR, 10));
        tooltip.setTextAlignment(TextAlignment.CENTER);
        lbl.setTooltip(tooltip);
        return lbl;
    }

    static void message(String message){

        Label messageLabel=new Label(message);
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        Button okButton=new Button("Ok");
        okButton.setPrefWidth(100);
        VBox verticalBox=new VBox();
        verticalBox.setSpacing(30);
        verticalBox.setPrefSize(300,150);
        verticalBox.setAlignment(Pos.CENTER);
        verticalBox.getChildren().addAll(messageLabel,okButton);
        Scene messageScene=new Scene(verticalBox);
        Stage messageStage=new Stage();
        messageStage.setScene(messageScene);
        messageStage.setTitle("Mesaj aplicație");
        messageStage.show();
        okButton.setOnAction(event -> messageStage.close());

    }

    public static int maxYear(ArrayList<Activity> activities) {
        int max=0;
        for (Activity activity:activities) {
            if (activity.getYearOfStudy()>max) {
                max=activity.getYearOfStudy();
            }
        }
        return max;
    }

}