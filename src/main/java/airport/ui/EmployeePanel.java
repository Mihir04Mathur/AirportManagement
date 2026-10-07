package airport.ui;

import airport.db.EmployeeDAO;
import airport.exception.AirportException;
import airport.util.Validate;
import airport.model.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/** Manage Employees screen (Pilot / Cabin Crew / Airport Staff). */
public class EmployeePanel extends CrudPanel {
    private static final String[] ROLES = {"Pilot", "Cabin Crew", "Airport Staff"};
    private final EmployeeDAO dao = new EmployeeDAO();
    private List<Employee> data = new ArrayList<>();

    private final JComboBox<String> role = Theme.combo();
    private final JTextField name = Theme.field(15), age = Theme.field(15), phone = Theme.field(15),
            email = Theme.field(15), salary = Theme.field(15), license = Theme.field(15), hours = Theme.field(15);
    private final JComboBox<String> crewRank = Theme.combo(), department = Theme.combo();
    private JPanel pilotBox, crewBox, staffBox;

    public EmployeePanel(Runnable goHome) {
        super("Manage Employees", "Pilots, cabin crew and airport staff",
                new String[]{"ID", "Name", "Role", "Age", "Phone", "Email", "Salary", "Details"}, goHome);
        for (String r : ROLES) role.addItem(r);
        for (String r : new String[]{"Purser", "Senior Attendant", "Flight Attendant"}) crewRank.addItem(r);
        for (String d : new String[]{"Check-in Counter", "Ground Handling", "Security", "Baggage", "Air Traffic Control", "Maintenance"}) department.addItem(d);
        role.addActionListener(e -> updateVisibility());
        setFormTitle("Employee details");
        Theme.hint(name, "e.g. Priya Nair");
        Theme.hint(age, "18 to 69");
        Theme.hint(phone, "10-digit mobile number");
        Theme.hint(email, "name@airline.in");
        Theme.hint(salary, "e.g. 85000");
        Theme.hint(license, "e.g. ATPL-IN-12345");
        Theme.hint(hours, "total flying hours");
        init();
        updateVisibility();
    }

    @Override protected void setWidths() { Theme.widths(table, 40, 140, 100, 45, 100, 190, 100, 190); }

    @Override
    protected List<Object[]> fetchRows() throws AirportException {
        String k = keyword.getText().trim().toLowerCase();
        data = new ArrayList<>();
        List<Object[]> rows = new ArrayList<>();
        for (Employee e : dao.getAll()) {
            if (!k.isEmpty() && !(e.getName().toLowerCase().contains(k) || e.getRole().toLowerCase().contains(k))) continue;
            data.add(e);
            rows.add(new Object[]{e.getId(), e.getName(), e.getRole(), e.getAge(), e.getPhone(),
                    e.getEmail() == null ? "" : e.getEmail(), Theme.money(e.getSalary()), e.getDetails()});
        }
        return rows;
    }

    @Override
    protected void buildForm(FormBuilder fb) {
        fb.add("Role", role);
        fb.add("Full name", name);
        fb.add("Age", age);
        fb.add("Phone", phone);
        fb.add("Email (optional)", email);
        fb.add("Monthly salary (₹)", salary);
        pilotBox = new JPanel(new java.awt.GridLayout(0, 1, 0, 10)); pilotBox.setOpaque(false);
        pilotBox.add(wrap("Licence no.", license)); pilotBox.add(wrap("Flying hours", hours));
        crewBox = new JPanel(new java.awt.GridLayout(0, 1)); crewBox.setOpaque(false);
        crewBox.add(wrap("Crew rank", crewRank));
        staffBox = new JPanel(new java.awt.GridLayout(0, 1)); staffBox.setOpaque(false);
        staffBox.add(wrap("Department", department));
        fb.addGroup(pilotBox); fb.addGroup(crewBox); fb.addGroup(staffBox);
    }

    private JPanel wrap(String label, JComponent c) {
        JPanel p = new JPanel(new java.awt.BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(Theme.label(label), java.awt.BorderLayout.NORTH);
        p.add(c, java.awt.BorderLayout.CENTER);
        return p;
    }

    private void updateVisibility() {
        int i = role.getSelectedIndex();
        pilotBox.setVisible(i == 0); crewBox.setVisible(i == 1); staffBox.setVisible(i == 2);
    }

    @Override
    protected void fillForm(int row) {
        Employee e = data.get(row);
        name.setText(e.getName()); age.setText(String.valueOf(e.getAge()));
        phone.setText(e.getPhone()); email.setText(e.getEmail() == null ? "" : e.getEmail());
        salary.setText(String.valueOf(e.getSalary()));
        if (e instanceof Pilot) {
            role.setSelectedIndex(0);
            license.setText(((Pilot) e).getLicenseNo());
            hours.setText(String.valueOf(((Pilot) e).getFlyingHours()));
        } else if (e instanceof CabinCrew) {
            role.setSelectedIndex(1);
            crewRank.setSelectedItem(((CabinCrew) e).getCrewRank());
        } else {
            role.setSelectedIndex(2);
            department.setSelectedItem(((AirportStaff) e).getDepartment());
        }
    }

    @Override
    protected void clearForm() {
        name.setText(""); age.setText(""); phone.setText(""); email.setText(""); salary.setText("");
        license.setText(""); hours.setText(""); role.setSelectedIndex(0);
        crewRank.setSelectedIndex(0); department.setSelectedIndex(0);
    }

    /** Builds the right subclass of Employee from the form (polymorphism). */
    private Employee readForm(int id) throws AirportException {
        String n = Validate.required(name.getText(), "Name");
        int a = Validate.integer(age.getText(), "Age", 18, 69);
        String ph = Validate.phone(phone.getText());
        String em = Validate.email(email.getText());
        double sal = Validate.decimal(salary.getText(), "Salary", 0);
        switch (role.getSelectedIndex()) {
            case 0:
                return new Pilot(id, n, a, ph, em, sal, Validate.required(license.getText(), "Licence no."),
                        Validate.integer(hours.getText(), "Flying hours", 0, 60000));
            case 1:
                return new CabinCrew(id, n, a, ph, em, sal, (String) crewRank.getSelectedItem());
            default:
                return new AirportStaff(id, n, a, ph, em, sal, (String) department.getSelectedItem());
        }
    }

    @Override protected void add() throws AirportException { dao.add(readForm(0)); }
    @Override protected void update(int id) throws AirportException { dao.update(readForm(id)); }
    @Override protected void delete(int id) throws AirportException { dao.delete(id); }
}
