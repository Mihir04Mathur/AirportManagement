package airport.ui;

import airport.util.FormatUtil;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/** Date + time editor (arrow keys / mouse wheel change the selected part). Replaces typing dates by hand. */
public class DateTimeSpinner extends JSpinner {
    private final int defaultHoursAhead;

    public DateTimeSpinner(int defaultHoursAhead) {
        super(new SpinnerDateModel());
        this.defaultHoursAhead = defaultHoursAhead;
        JSpinner.DateEditor editor = new JSpinner.DateEditor(this, "yyyy-MM-dd HH:mm");
        editor.getTextField().setFont(Theme.font(Font.PLAIN, 13));
        setEditor(editor);
        setPreferredSize(new Dimension(150, 34));
        reset();
    }

    public void reset() {
        setValue(toDate(LocalDateTime.now().plusHours(defaultHoursAhead).withMinute(0).withSecond(0).withNano(0)));
    }

    /** Value as "yyyy-MM-dd HH:mm". */
    public String getText() {
        try { commitEdit(); } catch (ParseException ignored) { /* keep the last valid value */ }
        return LocalDateTime.ofInstant(((Date) getValue()).toInstant(), ZoneId.systemDefault()).format(FormatUtil.DATE_TIME);
    }

    public void setText(String text) {
        LocalDateTime t = FormatUtil.parse(text);
        if (t == null) reset(); else setValue(toDate(t));
    }

    private static Date toDate(LocalDateTime t) { return Date.from(t.atZone(ZoneId.systemDefault()).toInstant()); }
}
