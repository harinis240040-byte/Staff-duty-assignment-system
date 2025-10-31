package model;

public class Staff {
    private int id;
    private String name;
    private String department;
    private String shiftType;
    private String dutyDate;

    public Staff(int id, String name, String department, String shiftType, String dutyDate) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.shiftType = shiftType;
        this.dutyDate = dutyDate;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getShiftType() { return shiftType; }
    public String getDutyDate() { return dutyDate; }
}
