package airport.ui;

import airport.db.PassengerDAO;
import airport.exception.AirportException;
import airport.util.Validate;
import airport.model.Passenger;
import airport.service.BookingService;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/** Manage Passengers screen. */
public class PassengerPanel extends CrudPanel {
    private final PassengerDAO dao = new PassengerDAO();
    private List<Passenger> data = new ArrayList<>();   // ArrayList of passengers

    private final JTextField name = Theme.field(15), age = Theme.field(15), phone = Theme.field(15),
            email = Theme.field(15), passport = Theme.field(15), nationality = Theme.field(15);
    private final JComboBox<String> gender = Theme.combo();

    public PassengerPanel(Runnable goHome) {
        super("Manage Passengers", "Add, edit, search and remove passenger records",
                new String[]{"ID", "Name", "Age", "Gender", "Phone", "Email", "Passport", "Nationality"}, goHome);
        gender.addItem("Male"); gender.addItem("Female"); gender.addItem("Other");
        setFormTitle("Passenger details");
        Theme.hint(name, "e.g. Aarav Sharma");
        Theme.hint(age, "18 or above is typical");
        Theme.hint(phone, "10-digit mobile number");
        Theme.hint(email, "name@example.com");
        Theme.hint(passport, "needed for international flights");
        nationality.setText("Indian");
        init();
    }

    @Override protected void setWidths() { Theme.widths(table, 40, 130, 45, 65, 100, 190, 90, 90); }

    @Override
    protected List<Object[]> fetchRows() throws AirportException {
        data = dao.search(keyword.getText());
        List<Object[]> rows = new ArrayList<>();
        for (Passenger p : data)
            rows.add(new Object[]{p.getId(), p.getName(), p.getAge(), p.getGender(), p.getPhone(),
                    p.getEmail() == null ? "" : p.getEmail(), p.getPassportNo() == null ? "" : p.getPassportNo(), p.getNationality()});
        return rows;
    }

    @Override
    protected void buildForm(FormBuilder fb) {
        fb.add("Full name", name);
        fb.add("Age", age);
        fb.add("Gender", gender);
        fb.add("Phone", phone);
        fb.add("Email (optional)", email);
        fb.add("Passport no. (optional)", passport);
        fb.add("Nationality", nationality);
    }

    @Override
    protected void fillForm(int row) {
        Passenger p = data.get(row);
        name.setText(p.getName()); age.setText(String.valueOf(p.getAge()));
        gender.setSelectedItem(p.getGender()); phone.setText(p.getPhone());
        email.setText(p.getEmail() == null ? "" : p.getEmail());
        passport.setText(p.getPassportNo() == null ? "" : p.getPassportNo());
        nationality.setText(p.getNationality());
    }

    @Override
    protected void clearForm() {
        name.setText(""); age.setText(""); phone.setText(""); email.setText(""); passport.setText("");
        nationality.setText("Indian"); gender.setSelectedIndex(0);
    }

    private Passenger readForm(int id) throws AirportException {
        return new Passenger(id, Validate.required(name.getText(), "Name"),
                Validate.integer(age.getText(), "Age", 1, 119), (String) gender.getSelectedItem(),
                Validate.phone(phone.getText()), Validate.email(email.getText()),
                passport.getText().trim(), Validate.required(nationality.getText(), "Nationality"));
    }

    @Override
    protected void addExtraButtons(JPanel buttons) {
        AppButton trips = new AppButton("Trips", Theme.TEAL);
        trips.addActionListener(e -> {
            int id = selectedId();
            if (id < 0) { Theme.error(this, "Select a passenger in the table first."); return; }
            try {
                String name = data.stream().filter(p -> p.getId() == id).map(Passenger::getName).findFirst().orElse("Passenger");
                HistoryDialog.show(this, name, new BookingService().getByPassenger(id));
            } catch (AirportException ex) {
                Theme.error(this, ex.getMessage());
            }
        });
        buttons.add(trips);
    }

    @Override protected void add() throws AirportException { dao.add(readForm(0)); }
    @Override protected void update(int id) throws AirportException { dao.update(readForm(id)); }
    @Override protected void delete(int id) throws AirportException { dao.delete(id); }
}
