import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainWindow extends JFrame {

    private static final Color APP_BG = new Color(247, 247, 245);
    private static final Color WINDOW_BG = new Color(252, 252, 250);
    private static final Color CARD = new Color(255, 255, 255);
    private static final Color FIELD = new Color(249, 249, 247);
    private static final Color BORDER = new Color(231, 231, 226);

    private static final Color TEXT = new Color(40, 41, 38);
    private static final Color MUTED = new Color(143, 144, 139);
    private static final Color SUBTLE = new Color(174, 175, 170);

    private static final Color ACCENT = new Color(225, 164, 45);
    private static final Color ACCENT_SOFT = new Color(251, 238, 205);
    private static final Color CONTROL = new Color(243, 243, 239);
    private static final Color CONTROL_HOVER = new Color(236, 236, 231);
    private static final Color CONTROL_ACTIVE = new Color(255, 255, 255);
    private static final Color CLOSE_HOVER = new Color(231, 78, 78);

    private static final String FONT_FAMILY =
            resolveFontFamily();

    private final MoodStorage storage =
            new MoodStorage();

    private final CardLayout pageLayout =
            new CardLayout();

    private final JPanel pages =
            new JPanel(pageLayout);

    private final SunMoodPanel sunPanel =
            new SunMoodPanel();

    private final JLabel scoreLabel =
            new JLabel("5");

    private final JLabel stateLabel =
            new JLabel();

    private final JTextArea adviceArea =
            new JTextArea();

    private final JTextArea noteArea =
            new JTextArea();

    private final JSlider moodSlider =
            new JSlider(1, 10, 5);

    private final JButton datePickerButton =
            new JButton();

    private final HistoryChartPanel chartPanel =
            new HistoryChartPanel();

    private final JPanel entriesList =
            new JPanel();

    private final JLabel averageValue =
            new JLabel("—");

    private final JLabel daysValue =
            new JLabel("0");

    private final JLabel lastValue =
            new JLabel("—");

    private final JLabel saveHint =
            new JLabel(" ");

    private final SegmentedButton entryNavButton =
            new SegmentedButton("Отметить");

    private final SegmentedButton historyNavButton =
            new SegmentedButton("История");

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private LocalDate selectedDate =
            LocalDate.now();

    private boolean loadingDate = false;

    public MainWindow() {
        super();

        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1220, 790);
        setMinimumSize(new Dimension(1000, 660));
        setLocationRelativeTo(null);

        configureDatePickerButton();

        JPanel window =
                new JPanel(new BorderLayout());

        window.setBackground(WINDOW_BG);
        window.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        window.add(
                createWindowBar(),
                BorderLayout.NORTH
        );

        JPanel root =
                new JPanel(new BorderLayout());

        root.setBackground(APP_BG);
        root.setBorder(
                new EmptyBorder(2, 28, 28, 28)
        );

        pages.setOpaque(false);
        pages.add(createEntryPage(), "entry");
        pages.add(createHistoryPage(), "history");

        root.add(
                pages,
                BorderLayout.CENTER
        );

        window.add(
                root,
                BorderLayout.CENTER
        );

        setContentPane(window);

        selectMainTab("entry");
        updateDateButton();
        updateMood(5);
        loadSelectedDate();
        refreshHistory();
    }

    // ============================================================
    // Window bar
    // ============================================================

    private JPanel createWindowBar() {
        JPanel bar =
                new JPanel(new BorderLayout());

        bar.setBackground(WINDOW_BG);
        bar.setPreferredSize(new Dimension(0, 42));
        bar.setBorder(new EmptyBorder(0, 12, 0, 0));

        JLabel title =
                label("Mood Pulse", 12, Font.PLAIN, MUTED);

        JPanel buttons =
                new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));

        buttons.setOpaque(false);

        WindowButton minimize =
                new WindowButton(WindowButtonType.MINIMIZE);

        WindowButton maximize =
                new WindowButton(WindowButtonType.MAXIMIZE);

        WindowButton close =
                new WindowButton(WindowButtonType.CLOSE);

        minimize.addActionListener(
                e -> setState(Frame.ICONIFIED)
        );

        maximize.addActionListener(e -> {
            if ((getExtendedState() & Frame.MAXIMIZED_BOTH) != 0) {
                setExtendedState(Frame.NORMAL);
            } else {
                setExtendedState(Frame.MAXIMIZED_BOTH);
            }
        });

        close.addActionListener(e -> dispose());

        buttons.add(minimize);
        buttons.add(maximize);
        buttons.add(close);

        bar.add(title, BorderLayout.WEST);
        bar.add(buttons, BorderLayout.EAST);

        final Point[] dragOffset = {null};

        MouseAdapter dragListener =
                new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        dragOffset[0] = e.getPoint();
                    }

                    @Override
                    public void mouseDragged(MouseEvent e) {
                        if (dragOffset[0] == null) {
                            return;
                        }

                        if ((getExtendedState() & Frame.MAXIMIZED_BOTH) != 0) {
                            return;
                        }

                        Point screen = e.getLocationOnScreen();

                        setLocation(
                                screen.x - dragOffset[0].x,
                                screen.y - dragOffset[0].y
                        );
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2
                                && SwingUtilities.isLeftMouseButton(e)) {
                            if ((getExtendedState() & Frame.MAXIMIZED_BOTH) != 0) {
                                setExtendedState(Frame.NORMAL);
                            } else {
                                setExtendedState(Frame.MAXIMIZED_BOTH);
                            }
                        }
                    }
                };

        bar.addMouseListener(dragListener);
        bar.addMouseMotionListener(dragListener);
        title.addMouseListener(dragListener);
        title.addMouseMotionListener(dragListener);

        return bar;
    }

    // ============================================================
    // Navigation
    // ============================================================

    private JPanel createPageNavigation(
            boolean entrySelected
    ) {
        SegmentedButton entry =
                new SegmentedButton(
                        "Отметить"
                );

        SegmentedButton history =
                new SegmentedButton(
                        "История"
                );

        entry.setSelected(
                entrySelected
        );

        history.setSelected(
                !entrySelected
        );

        entry.addActionListener(
                e -> selectMainTab(
                        "entry"
                )
        );

        history.addActionListener(
                e -> selectMainTab(
                        "history"
                )
        );

        JPanel container =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                4,
                                4
                        )
                );

        container.setBackground(
                CONTROL
        );

        container.setBorder(
                new EmptyBorder(
                        2,
                        2,
                        2,
                        2
                )
        );

        container.add(entry);
        container.add(history);

        return container;
    }

    private void selectMainTab(String page) {
        if ("history".equals(page)) {
            refreshHistory();
        }

        pageLayout.show(
                pages,
                page
        );
    }

    // ============================================================
    // Entry page
    // ============================================================

    private JPanel createEntryPage() {
        JPanel page =
                new JPanel(new BorderLayout(0, 10));

        page.setOpaque(false);

        JPanel header =
                new JPanel(
                        new BorderLayout(
                                22,
                                0
                        )
                );

        header.setOpaque(false);

        JPanel titleBlock =
                new JPanel();

        titleBlock.setOpaque(false);

        titleBlock.setLayout(
                new BoxLayout(
                        titleBlock,
                        BoxLayout.Y_AXIS
                )
        );

        titleBlock.add(
                label(
                        "Как настроение?",
                        32,
                        Font.BOLD,
                        TEXT
                )
        );

        titleBlock.add(
                Box.createVerticalStrut(4)
        );

        titleBlock.add(
                label(
                        "Оцени состояние по шкале от 1 до 10",
                        14,
                        Font.PLAIN,
                        MUTED
                )
        );

        JPanel rightHeader =
                new JPanel();

        rightHeader.setOpaque(false);

        rightHeader.setLayout(
                new BoxLayout(
                        rightHeader,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel navigation =
                createPageNavigation(
                        true
                );

        navigation.setAlignmentX(
                Component.RIGHT_ALIGNMENT
        );

        JPanel dateWrap =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                8,
                                0
                        )
                );

        dateWrap.setOpaque(false);

        dateWrap.setAlignmentX(
                Component.RIGHT_ALIGNMENT
        );

        dateWrap.add(
                label(
                        "Дата",
                        12,
                        Font.BOLD,
                        MUTED
                )
        );

        dateWrap.add(
                datePickerButton
        );

        rightHeader.add(navigation);

        rightHeader.add(
                Box.createVerticalStrut(7)
        );

        rightHeader.add(dateWrap);

        header.add(
                titleBlock,
                BorderLayout.WEST
        );

        header.add(
                rightHeader,
                BorderLayout.EAST
        );

        page.add(
                header,
                BorderLayout.NORTH
        );

        RoundedPanel mainCard =
                new RoundedPanel(28, CARD, true);

        mainCard.setLayout(new BorderLayout());
        mainCard.setBorder(new EmptyBorder(22, 34, 20, 34));

        JPanel infoWrap =
                new JPanel(new GridBagLayout());

        infoWrap.setOpaque(false);

        GridBagConstraints info =
                new GridBagConstraints();

        info.gridx = 0;
        info.weightx = 1.0;
        info.fill = GridBagConstraints.HORIZONTAL;
        info.anchor = GridBagConstraints.NORTHWEST;

        stateLabel.setFont(font(14, Font.BOLD));
        stateLabel.setForeground(colorForScore(5));

        info.gridy = 0;
        info.insets = new Insets(0, 0, 4, 0);
        infoWrap.add(stateLabel, info);

        JPanel scoreRow =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        scoreRow.setOpaque(false);

        scoreLabel.setFont(font(58, Font.BOLD));
        scoreLabel.setForeground(TEXT);

        JLabel outOf =
                label(" / 10", 16, Font.PLAIN, MUTED);

        outOf.setBorder(new EmptyBorder(30, 4, 0, 0));

        sunPanel.setPreferredSize(new Dimension(72, 72));
        sunPanel.setMinimumSize(new Dimension(72, 72));
        sunPanel.setMaximumSize(new Dimension(72, 72));

        scoreRow.add(scoreLabel);
        scoreRow.add(outOf);
        scoreRow.add(Box.createHorizontalStrut(18));
        scoreRow.add(sunPanel);

        info.gridy = 1;
        info.insets = new Insets(0, 0, 8, 0);
        infoWrap.add(scoreRow, info);

        adviceArea.setEditable(false);
        adviceArea.setOpaque(false);
        adviceArea.setLineWrap(true);
        adviceArea.setWrapStyleWord(true);
        adviceArea.setFocusable(false);
        adviceArea.setRows(3);
        adviceArea.setFont(font(20, Font.PLAIN));
        adviceArea.setForeground(TEXT);
        adviceArea.setBorder(null);

        info.gridy = 2;
        info.insets = new Insets(0, 0, 18, 0);
        infoWrap.add(adviceArea, info);

        JLabel noteTitle =
                label("Что повлияло на настроение?", 12, Font.BOLD, MUTED);

        info.gridy = 3;
        info.insets = new Insets(0, 0, 8, 0);
        infoWrap.add(noteTitle, info);

        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setFont(font(15, Font.PLAIN));
        noteArea.setForeground(TEXT);
        noteArea.setCaretColor(TEXT);
        noteArea.setBackground(FIELD);
        noteArea.setBorder(new EmptyBorder(14, 14, 14, 14));
        noteArea.setRows(5);

        JScrollPane noteScroll =
                new JScrollPane(noteArea);

        noteScroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        noteScroll.getViewport().setBackground(FIELD);

        noteScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        noteScroll.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
        );

        noteScroll.setPreferredSize(new Dimension(0, 160));

        info.gridy = 4;
        info.weighty = 1.0;
        info.fill = GridBagConstraints.BOTH;
        info.insets = new Insets(0, 0, 8, 0);
        infoWrap.add(noteScroll, info);

        saveHint.setFont(font(12, Font.PLAIN));
        saveHint.setForeground(MUTED);

        info.gridy = 5;
        info.weighty = 0.0;
        info.fill = GridBagConstraints.HORIZONTAL;
        info.insets = new Insets(0, 0, 10, 0);
        infoWrap.add(saveHint, info);

        JButton saveButton =
                primaryButton("Сохранить");

        saveButton.addActionListener(
                e -> saveSelectedDate(saveButton)
        );

        JPanel buttonRow =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        buttonRow.setOpaque(false);
        buttonRow.add(saveButton);

        info.gridy = 6;
        info.insets = new Insets(0, 0, 0, 0);
        infoWrap.add(buttonRow, info);

        mainCard.add(infoWrap, BorderLayout.CENTER);

        JPanel sliderRow =
                new JPanel(new BorderLayout(14, 0));

        sliderRow.setOpaque(false);
        sliderRow.setBorder(new EmptyBorder(18, 4, 0, 4));

        sliderRow.add(
                label("1", 12, Font.BOLD, MUTED),
                BorderLayout.WEST
        );

        moodSlider.setOpaque(false);
        moodSlider.setFocusable(false);
        moodSlider.setUI(new MoodSliderUI(moodSlider));

        moodSlider.addChangeListener(e -> {
            if (!loadingDate) {
                updateMood(moodSlider.getValue());
            }
        });

        sliderRow.add(moodSlider, BorderLayout.CENTER);

        sliderRow.add(
                label("10", 12, Font.BOLD, MUTED),
                BorderLayout.EAST
        );

        mainCard.add(sliderRow, BorderLayout.SOUTH);

        page.add(mainCard, BorderLayout.CENTER);

        return page;
    }

    // ============================================================
    // History page
    // ============================================================

    private JPanel createHistoryPage() {
        JPanel page =
                new JPanel(new BorderLayout(0, 16));

        page.setOpaque(false);

        JPanel header =
                new JPanel(
                        new BorderLayout(
                                22,
                                0
                        )
                );

        header.setOpaque(false);

        header.add(
                label(
                        "История настроения",
                        32,
                        Font.BOLD,
                        TEXT
                ),
                BorderLayout.WEST
        );

        header.add(
                createPageNavigation(
                        false
                ),
                BorderLayout.EAST
        );

        page.add(
                header,
                BorderLayout.NORTH
        );

        JPanel content =
                new JPanel(new BorderLayout(0, 14));

        content.setOpaque(false);

        JPanel stats =
                new JPanel(new GridLayout(1, 3, 12, 0));

        stats.setOpaque(false);

        stats.add(statCard("Среднее", averageValue, "за всё время"));
        stats.add(statCard("Сохранено", daysValue, "дней"));
        stats.add(statCard("Последняя оценка", lastValue, "из 10"));

        content.add(stats, BorderLayout.NORTH);

        chartPanel.setPreferredSize(new Dimension(0, 330));
        content.add(chartPanel, BorderLayout.CENTER);

        RoundedPanel recentCard =
                new RoundedPanel(24, CARD, true);

        recentCard.setLayout(new BorderLayout());
        recentCard.setBorder(new EmptyBorder(18, 20, 16, 20));
        recentCard.setPreferredSize(new Dimension(0, 230));

        recentCard.add(
                label("Последние записи", 16, Font.BOLD, TEXT),
                BorderLayout.NORTH
        );

        entriesList.setOpaque(false);
        entriesList.setLayout(new BoxLayout(entriesList, BoxLayout.Y_AXIS));

        JScrollPane entriesScroll =
                new JScrollPane(entriesList);

        entriesScroll.setBorder(null);
        entriesScroll.setOpaque(false);
        entriesScroll.getViewport().setOpaque(false);

        entriesScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        entriesScroll.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        entriesScroll.getVerticalScrollBar().setUI(new FlatScrollBarUI());
        entriesScroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        entriesScroll.getVerticalScrollBar().setUnitIncrement(16);

        recentCard.add(entriesScroll, BorderLayout.CENTER);

        content.add(recentCard, BorderLayout.SOUTH);

        page.add(content, BorderLayout.CENTER);

        return page;
    }

    private JPanel statCard(
            String title,
            JLabel valueLabel,
            String suffix
    ) {
        RoundedPanel card =
                new RoundedPanel(22, CARD, true);

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel name =
                label(title, 12, Font.BOLD, MUTED);

        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(font(28, Font.BOLD));
        valueLabel.setForeground(TEXT);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel suffixLabel =
                label(suffix, 12, Font.PLAIN, SUBTLE);

        suffixLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(name);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(suffixLabel);
        card.add(Box.createVerticalGlue());

        return card;
    }

    // ============================================================
    // Date picker
    // ============================================================

    private void configureDatePickerButton() {
        datePickerButton.setFont(
                font(
                        13,
                        Font.PLAIN
                )
        );

        datePickerButton.setForeground(TEXT);
        datePickerButton.setBackground(CARD);
        datePickerButton.setFocusPainted(false);
        datePickerButton.setBorderPainted(false);

        datePickerButton.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        datePickerButton.setBorder(
                new EmptyBorder(
                        10,
                        14,
                        10,
                        12
                )
        );

        datePickerButton.setIcon(
                new ChevronDownIcon()
        );

        datePickerButton.setHorizontalTextPosition(
                SwingConstants.LEFT
        );

        datePickerButton.setIconTextGap(10);

        datePickerButton.addActionListener(
                e -> showCalendarPopup()
        );
    }

    private void updateDateButton() {
        datePickerButton.setText(
                selectedDate.format(
                        dateFormatter
                )
        );
    }

    private void showCalendarPopup() {
        JPopupMenu popup =
                new JPopupMenu();

        popup.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(8, 8, 8, 8)
                )
        );

        popup.setBackground(CARD);
        popup.setLayout(new BorderLayout());

        popup.add(
                new CalendarPanel(popup, selectedDate),
                BorderLayout.CENTER
        );

        popup.show(
                datePickerButton,
                0,
                datePickerButton.getHeight() + 6
        );
    }

    private class CalendarPanel extends JPanel {

        private final JPopupMenu popup;
        private YearMonth shownMonth;

        private final JLabel monthLabel =
                new JLabel();

        private final JPanel grid =
                new JPanel(new GridLayout(0, 7, 5, 5));

        CalendarPanel(
                JPopupMenu popup,
                LocalDate initialDate
        ) {
            this.popup = popup;
            this.shownMonth =
                    YearMonth.from(initialDate);

            setOpaque(false);
            setLayout(new BorderLayout(0, 12));
            setPreferredSize(new Dimension(300, 292));

            JPanel top =
                    new JPanel(new BorderLayout());

            top.setOpaque(false);

            JButton prev =
                    flatIconButton("‹");

            JButton next =
                    flatIconButton("›");

            prev.addActionListener(e -> {
                shownMonth =
                        shownMonth.minusMonths(1);
                rebuild();
            });

            next.addActionListener(e -> {
                YearMonth nextMonth =
                        shownMonth.plusMonths(1);

                YearMonth currentMonth =
                        YearMonth.from(LocalDate.now());

                if (!nextMonth.isAfter(currentMonth)) {
                    shownMonth = nextMonth;
                    rebuild();
                }
            });

            monthLabel.setFont(
                    font(14, Font.BOLD)
            );

            monthLabel.setForeground(TEXT);
            monthLabel.setHorizontalAlignment(
                    SwingConstants.CENTER
            );

            top.add(prev, BorderLayout.WEST);
            top.add(monthLabel, BorderLayout.CENTER);
            top.add(next, BorderLayout.EAST);

            grid.setOpaque(false);

            add(top, BorderLayout.NORTH);
            add(grid, BorderLayout.CENTER);

            rebuild();
        }

        private void rebuild() {
            grid.removeAll();

            String monthText =
                    shownMonth.getMonth()
                            .getDisplayName(
                                    TextStyle.FULL,
                                    new Locale("ru")
                            );

            monthLabel.setText(
                    capitalize(monthText)
                    + " "
                    + shownMonth.getYear()
            );

            String[] weekDays = {
                    "Пн",
                    "Вт",
                    "Ср",
                    "Чт",
                    "Пт",
                    "Сб",
                    "Вс"
            };

            for (String day : weekDays) {
                JLabel weekLabel =
                        new JLabel(
                                day,
                                SwingConstants.CENTER
                        );

                weekLabel.setFont(
                        font(11, Font.BOLD)
                );

                weekLabel.setForeground(MUTED);

                grid.add(weekLabel);
            }

            LocalDate firstDay =
                    shownMonth.atDay(1);

            int leading =
                    firstDay
                            .getDayOfWeek()
                            .getValue()
                    - 1;

            for (int i = 0; i < leading; i++) {
                grid.add(new JLabel());
            }

            LocalDate today =
                    LocalDate.now();

            for (
                    int day = 1;
                    day <= shownMonth.lengthOfMonth();
                    day++
            ) {
                LocalDate current =
                        shownMonth.atDay(day);

                boolean isSelected =
                        current.equals(selectedDate);

                boolean isToday =
                        current.equals(today);

                boolean isFuture =
                        current.isAfter(today);

                CalendarDayButton dayButton =
                        new CalendarDayButton(
                                day,
                                isSelected,
                                isToday,
                                isFuture
                        );

                if (!isFuture) {
                    dayButton.addActionListener(e -> {
                        selectedDate = current;
                        updateDateButton();
                        loadSelectedDate();
                        popup.setVisible(false);
                    });
                }

                grid.add(dayButton);
            }

            grid.revalidate();
            grid.repaint();
        }
    }

    private static class CalendarDayButton
            extends JButton {

        private final int day;
        private final boolean selected;
        private final boolean today;
        private final boolean future;
        private boolean hover = false;

        CalendarDayButton(
                int day,
                boolean selected,
                boolean today,
                boolean future
        ) {
            this.day = day;
            this.selected = selected;
            this.today = today;
            this.future = future;

            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setPreferredSize(
                    new Dimension(36, 34)
            );

            setCursor(
                    future
                            ? Cursor.getDefaultCursor()
                            : Cursor.getPredefinedCursor(
                                    Cursor.HAND_CURSOR
                            )
            );

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!CalendarDayButton.this.future) {
                        hover = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (selected) {
                g2.setColor(ACCENT_SOFT);
                g2.fillRoundRect(
                        1,
                        1,
                        getWidth() - 2,
                        getHeight() - 2,
                        11,
                        11
                );

            } else if (hover) {
                g2.setColor(CONTROL_HOVER);
                g2.fillRoundRect(
                        1,
                        1,
                        getWidth() - 2,
                        getHeight() - 2,
                        11,
                        11
                );

            } else if (today) {
                g2.setColor(CONTROL);
                g2.fillRoundRect(
                        1,
                        1,
                        getWidth() - 2,
                        getHeight() - 2,
                        11,
                        11
                );
            }

            g2.setFont(
                    font(
                            12,
                            selected
                                    ? Font.BOLD
                                    : Font.PLAIN
                    )
            );

            g2.setColor(
                    future
                            ? new Color(190, 190, 185)
                            : TEXT
            );

            String text =
                    String.valueOf(day);

            FontMetrics fm =
                    g2.getFontMetrics();

            int x =
                    (getWidth()
                    - fm.stringWidth(text))
                    / 2;

            int y =
                    (getHeight()
                    - fm.getHeight())
                    / 2
                    + fm.getAscent();

            g2.drawString(
                    text,
                    x,
                    y
            );

            g2.dispose();
        }
    }

    private JButton flatIconButton(String text) {
        JButton button =
                new JButton(text);

        button.setFont(font(17, Font.PLAIN));
        button.setForeground(TEXT);
        button.setBackground(CONTROL);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(5, 10, 5, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setOpaque(true);
                button.setBackground(CONTROL_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setOpaque(false);
            }
        });

        return button;
    }

    // ============================================================
    // Logic
    // ============================================================

    private void loadSelectedDate() {
        loadingDate = true;

        MoodEntry found = null;

        for (MoodEntry entry : storage.load()) {
            if (entry.getTimestamp().toLocalDate().equals(selectedDate)) {
                found = entry;
                break;
            }
        }

        if (found != null) {
            moodSlider.setValue(found.getScore());
            noteArea.setText(found.getNote());
            saveHint.setText("Запись на эту дату уже существует");
        } else {
            moodSlider.setValue(5);
            noteArea.setText("");
            saveHint.setText(selectedDate.equals(LocalDate.now()) ? "Сегодня" : "Новая запись");
        }

        updateMood(moodSlider.getValue());

        loadingDate = false;
    }

    private void updateMood(int score) {
        scoreLabel.setText(String.valueOf(score));
        stateLabel.setText(MotivationService.getShortState(score));
        stateLabel.setForeground(colorForScore(score));
        adviceArea.setText(MotivationService.getAdvice(score));
        sunPanel.setScore(score);
    }

    private void saveSelectedDate(JButton button) {
        try {
            storage.saveOrUpdateDate(
                    selectedDate,
                    moodSlider.getValue(),
                    noteArea.getText().trim()
            );

            String previous = button.getText();

            button.setText("Сохранено");
            saveHint.setText("Запись сохранена");

            Timer timer =
                    new Timer(1200, e -> button.setText(previous));

            timer.setRepeats(false);
            timer.start();

            refreshHistory();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Не удалось сохранить запись.",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void refreshHistory() {
        List<MoodEntry> all =
                storage.load();

        chartPanel.setEntries(all);

        if (all.isEmpty()) {
            averageValue.setText("—");
            lastValue.setText("—");
        } else {
            double average =
                    all.stream()
                            .mapToInt(MoodEntry::getScore)
                            .average()
                            .orElse(0.0);

            averageValue.setText(
                    String.format("%.1f", average)
            );

            lastValue.setText(
                    String.valueOf(
                            all.get(all.size() - 1).getScore()
                    )
            );
            lastValue.setForeground(
                    colorForScore(
                            all.get(all.size() - 1).getScore()
                    )
            );
        }

        daysValue.setText(
                String.valueOf(all.size())
        );

        entriesList.removeAll();

        if (all.isEmpty()) {
            JLabel empty =
                    label(
                            "Здесь появятся сохранённые дни",
                            13,
                            Font.PLAIN,
                            MUTED
                    );

            empty.setBorder(new EmptyBorder(18, 0, 0, 0));
            entriesList.add(empty);

        } else {
            for (int i = all.size() - 1; i >= 0; i--) {
                MoodEntry entry = all.get(i);

                JPanel row =
                        new JPanel(new BorderLayout(16, 0));

                row.setOpaque(false);
                row.setBorder(new EmptyBorder(12, 0, 12, 0));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

                JPanel left =
                        new JPanel();

                left.setOpaque(false);
                left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

                JLabel date =
                        label(
                                entry.getTimestamp().toLocalDate().format(dateFormatter),
                                13,
                                Font.BOLD,
                                TEXT
                        );

                JLabel state =
                        label(
                                MotivationService.getShortState(entry.getScore()),
                                12,
                                Font.PLAIN,
                                MUTED
                        );

                left.add(date);
                left.add(Box.createVerticalStrut(4));
                left.add(state);

                String noteText =
                        entry.getNote().isBlank()
                                ? "Без заметки"
                                : shorten(entry.getNote(), 90);

                JLabel note =
                        label(
                                noteText,
                                13,
                                Font.PLAIN,
                                MUTED
                        );

                JLabel score =
                        label(
                                entry.getScore() + "/10",
                                22,
                                Font.BOLD,
                                colorForScore(entry.getScore())
                        );

                score.setHorizontalAlignment(SwingConstants.RIGHT);

                row.add(left, BorderLayout.WEST);
                row.add(note, BorderLayout.CENTER);
                row.add(score, BorderLayout.EAST);

                entriesList.add(row);

                if (i > 0) {
                    JSeparator separator =
                            new JSeparator();

                    separator.setForeground(BORDER);
                    separator.setBackground(BORDER);
                    entriesList.add(separator);
                }
            }
        }

        entriesList.revalidate();
        entriesList.repaint();
    }

    private static Color colorForScore(int score) {
        if (score <= 3) {
            return new Color(204, 76, 76);
        } else if (score <= 7) {
            return new Color(203, 150, 37);
        } else {
            return new Color(70, 156, 96);
        }
    }

    private String shorten(String text, int max) {
        String single =
                text.replace('\n', ' ').trim();

        if (single.length() <= max) {
            return single;
        }

        return single.substring(0, max - 1) + "…";
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private JButton primaryButton(String text) {
        JButton button =
                new JButton(text);

        button.setForeground(new Color(44, 38, 23));
        button.setBackground(ACCENT_SOFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(font(14, Font.BOLD));
        button.setBorder(new EmptyBorder(12, 18, 12, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel label =
                new JLabel(text);

        label.setFont(font(size, style));
        label.setForeground(color);

        return label;
    }

    private static Font font(int size, int style) {
        return new Font(FONT_FAMILY, style, size);
    }

    private static String resolveFontFamily() {
        String[] preferred = {
                "Segoe UI Variable Text",
                "Segoe UI Variable",
                "Segoe UI",
                "Inter",
                "Arial"
        };

        String[] installed =
                GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getAvailableFontFamilyNames();

        for (String wanted : preferred) {
            for (String actual : installed) {
                if (actual.equalsIgnoreCase(wanted)) {
                    return actual;
                }
            }
        }

        return "SansSerif";
    }

    // ============================================================
    // Components
    // ============================================================

    private static class ChevronDownIcon
            implements Icon {

        @Override
        public int getIconWidth() {
            return 12;
        }

        @Override
        public int getIconHeight() {
            return 8;
        }

        @Override
        public void paintIcon(
                Component component,
                Graphics graphics,
                int x,
                int y
        ) {
            Graphics2D g2 =
                    (Graphics2D)
                            graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(MUTED);

            g2.setStroke(
                    new BasicStroke(
                            1.5f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            int centerY =
                    y + 3;

            g2.drawLine(
                    x + 2,
                    centerY,
                    x + 6,
                    centerY + 3
            );

            g2.drawLine(
                    x + 6,
                    centerY + 3,
                    x + 10,
                    centerY
            );

            g2.dispose();
        }
    }

    private static class SegmentedButton extends JButton {
        private boolean selected = false;
        private boolean hover = false;

        SegmentedButton(String text) {
            super(text);

            setFont(font(12, Font.PLAIN));
            setForeground(TEXT);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorder(new EmptyBorder(8, 14, 8, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        public void setSelected(boolean selected) {
            this.selected = selected;
            setFont(font(12, selected ? Font.BOLD : Font.PLAIN));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (selected) {
                g2.setColor(CONTROL_ACTIVE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            } else if (hover) {
                g2.setColor(CONTROL_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }

            g2.dispose();

            super.paintComponent(g);
        }
    }

    private enum WindowButtonType {
        MINIMIZE,
        MAXIMIZE,
        CLOSE
    }

    private static class WindowButton extends JButton {
        private final WindowButtonType type;
        private boolean hover = false;

        WindowButton(WindowButtonType type) {
            this.type = type;

            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setPreferredSize(new Dimension(46, 42));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (hover) {
                g2.setColor(type == WindowButtonType.CLOSE ? CLOSE_HOVER : CONTROL_HOVER);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.setColor(type == WindowButtonType.CLOSE && hover ? Color.WHITE : MUTED);
            g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            switch (type) {
                case MINIMIZE ->
                        g2.drawLine(cx - 6, cy + 2, cx + 6, cy + 2);

                case MAXIMIZE ->
                        g2.drawRect(cx - 6, cy - 5, 12, 10);

                case CLOSE -> {
                    g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                    g2.drawLine(cx + 5, cy - 5, cx - 5, cy + 5);
                }
            }

            g2.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;
        private final boolean border;

        RoundedPanel(int radius, Color fill, boolean border) {
            this.radius = radius;
            this.fill = fill;
            this.border = border;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            if (border) {
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }

            g2.dispose();

            super.paintComponent(g);
        }
    }

    private static class MoodSliderUI extends BasicSliderUI {
        MoodSliderUI(JSlider slider) {
            super(slider);
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int y = trackRect.y + trackRect.height / 2 - 2;

            g2.setColor(BORDER);
            g2.fillRoundRect(trackRect.x, y, trackRect.width, 4, 4, 4);

            int filled = thumbRect.x + thumbRect.width / 2 - trackRect.x;

            g2.setColor(ACCENT);
            g2.fillRoundRect(trackRect.x, y, Math.max(0, filled), 4, 4, 4);

            g2.dispose();
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(Color.WHITE);
            g2.fillOval(thumbRect.x + 1, thumbRect.y + 1, thumbRect.width - 2, thumbRect.height - 2);

            g2.setColor(new Color(210, 210, 204));
            g2.drawOval(thumbRect.x + 1, thumbRect.y + 1, thumbRect.width - 3, thumbRect.height - 3);

            g2.dispose();
        }
    }

    private static class FlatScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(201, 201, 196);
            trackColor = new Color(0, 0, 0, 0);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        private JButton zeroButton() {
            JButton button = new JButton();

            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));

            return button;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(new Color(201, 201, 196));
            g2.fillRoundRect(
                    thumbBounds.x + 1,
                    thumbBounds.y + 1,
                    Math.max(2, thumbBounds.width - 2),
                    Math.max(8, thumbBounds.height - 2),
                    6,
                    6
            );

            g2.dispose();
        }
    }
}
