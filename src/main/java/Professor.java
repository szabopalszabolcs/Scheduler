public class Professor {


    private final int[][][] scheduleProfessor;
    private final String name;
    private String shortName;
    private int[] activitiesOfProfessor;
    private int idProfessor;

    public Professor(int id, String name){
        final int HOURS=7,DAYS=12;
        this.idProfessor = id;
        this.name = name;
        String[] names=name.split(" ");
        this.shortName = names[0].substring(0,1)+names[0].substring(1).toLowerCase();
        for (int i=1;i<names.length;i++){
            this.shortName += "_" + names[i].substring(0, 1);
        }
        this.scheduleProfessor = new int[2][HOURS][DAYS];
        for(int i=0;i<2;i++)
            for(int j=0;j<HOURS;j++)
                for(int k=0;k<DAYS;k++)
                    scheduleProfessor[i][j][k]=-1;
        activitiesOfProfessor = new int[0];
    }

    public int getIdProfessor() {
        return idProfessor;
    }

    public void setIdProfessor(int id) {
        this.idProfessor=id;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public int[] getActivitiesOfProfessor() {
        return activitiesOfProfessor;
    }

    public int getActivityProfessor(int semester, int hour, int day) {
        try {
            return scheduleProfessor[semester-1][hour][day];
        }
        catch (Exception ex) {
            return -1;
        }
    }

    public boolean setActivityProfessor(int semester, int hour, int day, int activity) {
        try {
            scheduleProfessor[semester-1][hour][day]=activity;
            return true;
        }
        catch (Exception ex){
            Utility.message("Activitatea nu a fost adăugată");
            return false;
        }
    }

    public boolean addActivity (int activity) {
        for (int act:activitiesOfProfessor) {
            if (activity==act)
                return false;
        }
        int size= activitiesOfProfessor.length;
        int[] newActivites=new int[size+1];
        System.arraycopy(activitiesOfProfessor, 0, newActivites, 0, size);
        newActivites[size]=activity;
        activitiesOfProfessor =new int[size+1];
        System.arraycopy(newActivites, 0, activitiesOfProfessor, 0, size + 1);
        return true;
    }

    public boolean removeActivity (int activity) {
        int size= activitiesOfProfessor.length;
        int[] newActivities=new int[size-1];
        int j=0;
        for (int i=0;i<size;i++) {
            try {
                if (activitiesOfProfessor[i]!=activity) {
                    newActivities[j]= activitiesOfProfessor[i];
                    j++;
                }
            }
            catch (Exception ex) {
                Utility.message("Activitatea nu a fost ștearsă");
                return false;
            }
        }
        activitiesOfProfessor = new int[size-1];
        if (size - 1 >= 0) System.arraycopy(newActivities, 0, activitiesOfProfessor, 0, size - 1);
        return true;
    }

}