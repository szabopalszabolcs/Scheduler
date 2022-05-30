public class Room {

    private final int[][][] scheduleRoom;
    private String roomName;
    private int roomId;

    public Room(int id, String name) {
        final int HOURS = 7,DAYS = 12;
        this.roomId = id;
        this.roomName = name;
        scheduleRoom = new int[2][HOURS][DAYS];
        for(int i=0;i<2;i++)
            for(int j=0;j<HOURS;j++)
                for(int k=0;k<DAYS;k++)
                    scheduleRoom[i][j][k]=-1;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setRoomId(int id) { this.roomId=id; }

    public int getActivityRoom(int semester,int hour,int day){
        try {
            return scheduleRoom[semester-1][hour][day];
        }
        catch (Exception ex) {
            return -1;
        }
    }

    public boolean setActivityRoom(int semester,int hour,int day, int activity) {
        try {
            scheduleRoom[semester-1][hour][day]=activity;
            return true;
        }
        catch (Exception ex){
            Utility.message("Activitatea nu a fost adăugată");
            return false;
        }
    }

}
