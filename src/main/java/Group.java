public class Group {

    private int idGroup;
    private String groupName;
    private final int[][][] scheduleGroup;
    private int[] activitiesOfGroup;
    private final String speciality;
    private final int year;
    private final int groupNumber;

    public Group(int id, String speciality, int year, int groupNumber){
        final int HOURS = 7,DAYS = 12;
        this.idGroup=id;
        this.groupName=speciality+year+groupNumber;
        this.scheduleGroup =new int[2][HOURS][DAYS];
        for(int i=0;i<2;i++)
            for(int j=0;j<HOURS;j++)
                for(int k=0;k<DAYS;k++)
                    scheduleGroup[i][j][k]=-1;
        activitiesOfGroup = new int[0];
        this.speciality=speciality;
        this.year=year;
        this.groupNumber=groupNumber;
    }

    public String getSpeciality() {
        return speciality;
    }

    public int getYear() {
        return year;
    }

    public String getGroupName(){
        return groupName;
    }

    public void setGroupName(String name){
        this.groupName=name;
    }

    public int getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(int id) {
        this.idGroup=id;
    }

    public int[] getActivitiesOfGroup() {
        return activitiesOfGroup;
    }

    public int getActivityGroup(int semester,int hour,int day){
        try {
            return scheduleGroup[semester-1][hour][day];
        }
        catch (Exception ex) {
            return -1;
        }
    }

    public boolean setActivityGroup(int semester,int hour,int day, int activity) {
        try {
            scheduleGroup[semester-1][hour][day]=activity;
            return true;
        }
        catch (Exception ex){
            Utility.message("Activitatea nu a fost adăugată");
            return false;
        }
    }

    public boolean addActivity (int activity) {
        for (int act:activitiesOfGroup) {
            if (activity==act)
                return false;
        }
        int size=activitiesOfGroup.length;
        int[] newActivities=new int[size+1];
        System.arraycopy(activitiesOfGroup, 0, newActivities, 0, size);
        newActivities[size]=activity;
        activitiesOfGroup=new int[size+1];
        System.arraycopy(newActivities, 0, activitiesOfGroup, 0, size + 1);
        return true;
    }

    public boolean removeActivity (int activity) {
        int size=activitiesOfGroup.length;
        int[] newActivities=new int[size-1];
        int j=0;
        for (int i=0;i<size;i++) {
            try {
                if (activitiesOfGroup[i]!=activity) {
                    newActivities[j]=activitiesOfGroup[i];
                    j++;
                }
            }
            catch (Exception ex) {
                Utility.message("Activitatea nu a fost ștearsă");
                return false;
            }
        }
        activitiesOfGroup = new int[size-1];
        if (size - 1 >= 0) System.arraycopy(newActivities, 0, activitiesOfGroup, 0, size - 1);
        return true;
    }

}