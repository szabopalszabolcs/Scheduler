import java.util.ArrayList;
import java.util.Arrays;

public class Activity {

    private final int idActivity;
    private final String subject;
    private final String codeSubject;
    private final int professorId;
    private final int type;
    private int[] groupsId;
    private final int semester;
    private final int yearOfStudy;
    private final int time;
    private final boolean weekly;
    private int classRoomId;

    public Activity(int idActivity, String subject, String codeSubject, int professorId, int tip, int[] groupsId, int semester, int yearOfStudy, int time, boolean weekly){
        this.idActivity=idActivity;
        this.subject = subject;
        this.codeSubject = codeSubject;
        this.professorId = professorId;
        this.type = tip;
        this.groupsId = groupsId;
        this.semester = semester;
        this.yearOfStudy = yearOfStudy;
        this.time = time;
        this.weekly = weekly;
        this.classRoomId =-1;
    }

    @Override
    public String toString() {
        return  idActivity +
                "-" + subject +
                "," + codeSubject +
                "," + type +
                "," + professorId +
                "," + Arrays.toString(groupsId) +
                ", semestru=" + semester +
                ", an=" + yearOfStudy +
                ", durata=" + time +
                ", saptamanal=" + weekly;
    }

    public void addGroups(Group[] newGroups){
        ArrayList<Integer> groupsToAdd=new ArrayList<>();
        for (int idGroup : groupsId) {
            groupsToAdd.add(idGroup);
            for (Group newGroup : newGroups) {
                if (newGroup.getIdGroup()==idGroup) {
                    newGroup.setGroupName("");
                }
            }
        }
        for (Group group : newGroups) {
            if (!group.getGroupName().equals("")) {
                groupsToAdd.add(group.getIdGroup());
            }
        }
        int[] newGroup=new int[groupsToAdd.size()];
        for (int i=0;i<newGroup.length;i++)
            newGroup[i] = groupsToAdd.get(i);
        groupsId = newGroup;
    }

    public String getCodeSubject(){
        return codeSubject;
    }

    public String getSubject() {
        return subject;
    }

    public int getTime() {
        return time;
    }

    public int getType(){
        return type;
    }

    public int getProfessorId() {
        return professorId;
    }

    public int[] getGroupsId() {
        return groupsId;
    }

    public int getSemester() {
        return semester;
    }

    public int getYearOfStudy() { return yearOfStudy; }

    public int getIdActivity() {
        return idActivity;
    }

    public String getTypeChar() {
        String[] typeChar={"C","S","L","P"};
        try {
            return typeChar[this.type-1];
        }
        catch (Exception ex) {
            return "X";
        }
    }

    public int getClassRoomId() {
        return classRoomId;
    }

    public void setClassRoomId(int classRoom) {
        this.classRoomId =classRoom;
    }

}