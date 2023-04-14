package helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.String.format;


public class DateTimeHelper extends AbstractHelper {


    /******************************************************************************************************************
     *                                            Поля класса
     ******************************************************************************************************************/
    private DateTimeFormatter formatter;
    private LocalDateTime dateTime;
    private Locale locale = new Locale("ru");

    private static List<String> holidays = Arrays.asList(
            "2023-01-01",
            "2023-01-02",
            "2023-01-03",
            "2023-01-04",
            "2023-01-05",
            "2023-01-06",
            "2023-01-07",
            "2023-01-08",
            "2023-01-14",
            "2023-01-15",
            "2023-01-21",
            "2023-01-22",
            "2023-01-28",
            "2023-01-29",
            "2023-02-04",
            "2023-02-05",
            "2023-02-11",
            "2023-02-12",
            "2023-02-18",
            "2023-02-19",
            "2023-02-23",
            "2023-02-24",
            "2023-02-25",
            "2023-02-26",
            "2023-03-04",
            "2023-03-05",
            "2023-03-08",
            "2023-03-11",
            "2023-03-12",
            "2023-03-18",
            "2023-03-19",
            "2023-03-25",
            "2023-03-26",
            "2023-04-01",
            "2023-04-02",
            "2023-04-08",
            "2023-04-09",
            "2023-04-15",
            "2023-04-16",
            "2023-04-22",
            "2023-04-23",
            "2023-04-29",
            "2023-04-30",
            "2023-05-01",
            "2023-05-06",
            "2023-05-07",
            "2023-05-08",
            "2023-05-09",
            "2023-05-13",
            "2023-05-14",
            "2023-05-20",
            "2023-05-21",
            "2023-05-27",
            "2023-05-28",
            "2023-06-03",
            "2023-06-04",
            "2023-06-10",
            "2023-06-11",
            "2023-06-12",
            "2023-06-17",
            "2023-06-18",
            "2023-06-24",
            "2023-06-25",
            "2023-07-01",
            "2023-07-02",
            "2023-07-08",
            "2023-07-09",
            "2023-07-15",
            "2023-07-16",
            "2023-07-22",
            "2023-07-23",
            "2023-07-29",
            "2023-07-30",
            "2023-08-05",
            "2023-08-06",
            "2023-08-12",
            "2023-08-13",
            "2023-08-19",
            "2023-08-20",
            "2023-08-26",
            "2023-08-27",
            "2023-09-02",
            "2023-09-03",
            "2023-09-09",
            "2023-09-10",
            "2023-09-16",
            "2023-09-17",
            "2023-09-23",
            "2023-09-24",
            "2023-09-30",
            "2023-10-01",
            "2023-10-07",
            "2023-10-08",
            "2023-10-14",
            "2023-10-15",
            "2023-10-21",
            "2023-10-22",
            "2023-10-28",
            "2023-10-29",
            "2023-11-04",
            "2023-11-05",
            "2023-11-06",
            "2023-11-11",
            "2023-11-12",
            "2023-11-18",
            "2023-11-19",
            "2023-11-25",
            "2023-11-26",
            "2023-12-02",
            "2023-12-03",
            "2023-12-09",
            "2023-12-10",
            "2023-12-16",
            "2023-12-17",
            "2023-12-23",
            "2023-12-24",
            "2023-12-30",
            "2023-12-31"
    );


    /******************************************************************************************************************
     *                                           Методы класса
     ******************************************************************************************************************/


    /**
     * Печатает дату в требуемом формате
     *
     * @param format формат печати
     * @return строку содержащую требуемую дату в требуемом формате
     */
    private DateTimeHelper setFormat(String format) {
        Map<String, String> pattern = new HashMap<>();
        pattern.put("more", "dd.MM.yyyy' 'HH:mm:ss:ms");
        pattern.put("standard", "dd.MM.yyyy' 'HH:mm");
        pattern.put("_standard", "dd.MM.yyyy' 'HH mm");
        pattern.put("dateFullMonthAndTime", "dd' 'MMMM', 'HH:mm");
        pattern.put("dateTimeUTC", "yyyy-MM-dd'T'HH:mm:ss'Z'");
        pattern.put("dateTimeUTCplus6", "yyyy-MM-dd'T'HH:mm:ss'+06:00'");
        pattern.put("only-date", "dd.MM.yyyy");
        pattern.put("only-date2", "yyyy-MM-dd");
        pattern.put("only-time", "HH:mm");
        pattern.put("HH", "HH");
        pattern.put("mm", "mm");
        pattern.put("onlyYear", "yyyy");
        formatter = DateTimeFormatter.ofPattern(pattern.get(format)).withLocale(Locale.forLanguageTag("RU"));
        return this;
    }

    /**
     * Прибавляет к дате дни
     *
     * @param days количество дней добавляемых к дате
     * @return результат сложения (исключает выходные дни)
     */
    private DateTimeHelper plusDaysWithoutDayOff(int days) {
        dateTime = dateTime.plusDays(days);
        while (isDayOff(dateTime)) {
            dateTime = dateTime.plusDays(1);
        }
        return this;
    }

    /**
     * Вычитает дни от даты
     *
     * @param days количество дней
     * @return результат сложения (исключает выходные дни)
     */
    private DateTimeHelper minusDaysWithoutDayOff(int days) {
        dateTime = dateTime.minusDays(days);
        while (isDayOff(dateTime)) {
            dateTime = dateTime.minusDays(1);
        }
        return this;
    }

    public boolean isDayOff() {
        logger.info(">>> Проверка на выходной день");
        return now().isDayOff(dateTime);
    }

    /**
     * Прибавляет к дате дни
     *
     * @param days количество дней добавляемых к дате
     * @return результат сложения (исключает выходные дни)
     */
    private DateTimeHelper plusDaysWithDayOff(int days) {
        dateTime = dateTime.plusDays(days);
        return this;
    }

    /**
     * Прибавляет к времени минуты
     *
     * @param min количество минут добавляемых к дате
     * @return результат сложения
     */
    private DateTimeHelper plusMin(int min) {
        dateTime = dateTime.plusMinutes(min);
        return this;
    }

    /**
     * Прибавляет к времени часы
     *
     * @param h количество часов добавляемых к дате
     * @return результат сложения
     */
    private DateTimeHelper plusH(int h) {
        dateTime = dateTime.plusHours(h);
        return this;
    }

    /**
     * Прибавляет к времени месяцы
     *
     * @param m количество месяцев добавляемых к дате
     * @return результат сложения
     */
    private DateTimeHelper plusMonth(int m) {
        dateTime = dateTime.plusMonths(m);
        return this;
    }

    private String print() {
        if (formatter == null)
            throw new IllegalStateException("Не задан формат");
        return dateTime.format(formatter);
    }

    /**
     * текущая дата
     */
    private DateTimeHelper now() {
        dateTime = LocalDateTime.now();
        return this;
    }

    /**
     * печатает дату с прибавлением дней с обходом выходных
     *
     * @param days    прибавление дней к сегодняшней даты
     * @param pattern формат печати
     */
    public String dateStampPlusDaysWithoutDayOff(int days, String pattern) {
        int hourInc = 1;
        if (Boolean.TRUE.equals(isBigTime())) {
            logger.info(">>> Текущее время более 23:00. Для обхода возможной серверной проблемы, прибавляется дополнительное количество часов ");
            hourInc = 2;
        }
        return now()
                .plusH(hourInc)
                .plusDaysWithoutDayOff(days)
                .setFormat(pattern)
                .print();
    }

    /**
     * Печатает дату с убавлением дней с обходом выходных
     *
     * @param days    убавление дней
     * @param pattern формат печати
     */
    public String dateStampMinusDaysWithoutDayOff(int days, String pattern) {
        if (Boolean.TRUE.equals(isLittleTime()))
            return now()
                    .minusDaysWithoutDayOff(days)
                    .plusH(5)
                    .setFormat(pattern)
                    .print();
        else return now()
                .minusDaysWithoutDayOff(days)
                .setFormat(pattern)
                .print();
    }

    /**
     * печатает дату с прибавлением дней без обхода выходных
     *
     * @param days    прибавление дней к сегодняшней даты
     * @param pattern формат печати
     */
    public String dateStampPlusDaysWithDayOff(int days, String pattern) {
        if (Boolean.TRUE.equals(isLittleTime()))
            return now()
                    .plusDaysWithDayOff(days)
                    .plusH(5)
                    .setFormat(pattern)
                    .print();
        else return now()
                .plusDaysWithDayOff(days)
                .setFormat(pattern)
                .print();
    }

    /**
     * @param pattern формат даты
     * @return возвращает текующее время
     */
    public String getCurrentDateTime(String pattern) {
        return now()
                .setFormat(pattern)
                .print();
    }

    //todo метод обхода бага
    private Boolean isLittleTime() {
        return Integer.parseInt(now().setFormat("HH").print()) < 1;
    }

    private Boolean isBigTime() {
        return Integer.parseInt(now().setFormat("HH").print()) > 23;
    }

    //TODO подумать как улучшить датахелпер и сделать это

    /**
     * печатает дату с прибавлением минут
     *
     * @param min     прибавление минут ко времени
     * @param pattern формат печати
     */
    public String dateStampPlusMin(int min, String pattern) {
        return now()
                .plusMin(min)
                .setFormat(pattern)
                .print();
    }

    /**
     * печатает дату с прибавлением часов
     *
     * @param h       прибавление минут ко времени
     * @param pattern формат печати
     */
    public String dateStampPlusH(int h, String pattern) {
        return now()
                .plusH(h)
                .setFormat(pattern)
                .print();
    }

    /**
     * печатает дату с прибавлением месяцев
     *
     * @param m       прибавление месяцев ко времени
     * @param pattern формат печати
     */
    public String dateStampPlusMonth(int m, String pattern) {
        return now()
                .plusMonth(m)
                .setFormat(pattern)
                .print();
    }

    /**
     * Проверяет день недели на выходной
     *
     * @return true, если выходной
     * false, если не выходной
     */
    public static boolean isDayOff(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTime.getDayOfWeek().name().equals("SUNDAY")
                || holidays.contains(dateTime.format(formatter))
                || dateTime.getDayOfWeek().name().equals("SATURDAY");
    }

    /**
     * Получает дату 1го дня текущего месяца
     *
     * @param format требуемый формат
     * @return ссылка на экземпляр этого класса
     */
    public String getFirstDayInCurrentMonth(String format) {
        setFormat(format);
        logger.info(">>> Получает дату 1го дня текущего месяца");
        return YearMonth.now().atDay(1).format(formatter);
    }

    /**
     * Получает дату последнего дня текущего месяца
     *
     * @param format требуемый формат
     * @return ссылка на экземпляр этого класса
     */
    public String getLastDayInCurrentMonth(String format) {
        setFormat(format);
        logger.info(">>> Получает дату последнего дня текущего месяца");
        return YearMonth.now().atEndOfMonth().format(formatter);
    }

    /**
     * Получает дату последнего дня прошлого месяца
     *
     * @param format требуемый формат
     * @return ссылка на экземпляр этого класса
     */
    public String getLastDayInPastMonth(String format) {
        setFormat(format);
        logger.info(">>> Получает дату последнего дня текущего месяца");
        return YearMonth.now().minusMonths(1).atEndOfMonth().format(formatter);
    }

    /**
     * Получает дату последнего дня прошлого месяца
     *
     * @param format требуемый формат
     * @return ссылка на экземпляр этого класса
     */
    public String getLastDayInFutureMonth(String format) {
        setFormat(format);
        logger.info(">>> Получает дату последнего дня текущего месяца");
        return YearMonth.now().plusMonths(1).atEndOfMonth().format(formatter);
    }

    /**
     * Изменяет формат даты
     *
     * @param date          текстовое значение с датой
     * @param currentFormat текущий формат
     * @param newFormat     требуемый формат
     */
    public String changeDateFormat(String date, String currentFormat, String newFormat) {
        logger.info(format("Изменяет форматы даты {%s} с {%s} на {%s}", date, currentFormat, newFormat));
        LocalDate localDate = null;
        try {
            localDate = new SimpleDateFormat(
                    currentFormat,
                    Locale.forLanguageTag("ru")
            ).parse(date).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert localDate != null;
        return localDate.format(DateTimeFormatter.ofPattern(newFormat, locale));
    }


}