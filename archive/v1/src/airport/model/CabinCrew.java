package airport.model;

public class CabinCrew extends Employee {
    private String crewRank;

    public CabinCrew() { }

    public CabinCrew(int id, String name, int age, String phone, String email,
                     double salary, String crewRank) {
        super(id, name, age, phone, email, salary);
        this.crewRank = crewRank;
    }

    @Override public String getRole() { return "Cabin Crew"; }
    @Override public String getRoleCode() { return "CABIN_CREW"; }
    @Override public String getDetails() { return "Rank: " + crewRank; }

    public String getCrewRank() { return crewRank; }
    public void setCrewRank(String crewRank) { this.crewRank = crewRank; }
}
