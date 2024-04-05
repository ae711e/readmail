/*
 * Copyright (c) 2021. Aleksey Eremin
 *
 * 14.12.2023
 *
 */

package ae;

/*
    Чтение почты по протоколу IMAP
    Поиск письма с заданной темой
    Запись на диск вложений из найденного письма с заданным расширением (регистр расширения игнорируется)
    При необходимости (ключ -d) найденное письмо с записанными вложением удаляется

    Возвращает статус:
    0 - были прочитаны файлы вложений
    1 - ошибка чтения почты
    2 - не было вложений
    3 - неправильный формат командной строки

 */

public class Main {

    public static void main(String[] args) {
        // write your code here
        //
        String InBox  = "INBOX";
        String outDir = ".";
        String Subj   = "test";
        String Extn   = "txt";
        boolean Delete = false;
        // Ключи:
        // буквенные ключи
        // -v       - вывести параметры
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-v":
                        R.Key_Verbose = true;    // подробный вывод
                        break;

                    case "-s":
                        Subj = args[++i];  // нужная тема
                        break;

                    case "-a":
                        Extn = args[++i];  // нужное вложение
                        break;

                    case "-d":
                        Delete = true;  // удалять письма
                        break;

                    case "-o":
                        outDir = args[++i];  // выходной каталог
                        break;

                    case "-f":
                        InBox  = args[++i];
                        break;

                    case "-?":
                        printHelp();
                        System.exit(3);
                        break;

                    default:
                        throw new IllegalArgumentException();

                }

            }
        } catch (Exception e) {
            System.err.println("Неправильный формат командной строки\n" +
              "Попробуй ключ -?");
            System.exit(3);
        }
        //
        R.loadDefault();

        // Отобразим версию
        R.out("Ver. " + R.Ver + "  e-mail: " + R.Email);
        R.out("Subject: " + Subj + "   Attachment: " + Extn);

        Worker  w = new Worker();
        int r;
        r = w.read(InBox, Subj, Extn, outDir, Delete);   // прочитать сообщения
        if(r == 0) {
            R.out("не было сообщений");
            System.exit(2);
        }
        if(r < 0) {
            // Ошибка чтения почты
            System.exit(1);
        }
    }

    /**
     * печать помощи
     */
    private static void printHelp()
    {
        System.out.println("Чтение писем и запись их вложений. Версия " + R.Ver + "\n" +
          "Формат командной строки:\n" +
          "[-v] [-s \"regex тема письма\"] [-a \"regex имя файла вложения\"] [-o выходной_каталог] [-d] [-f INBOX]\n" +
          "-d - удалить письмо, если его вложение записано\n" +
          "-f INBOX входная папка писем\n" +
          "Регистр темы письма и имени вложения игнорируются.\n" +
          "Пример для ключа\n" +
                " -s \".*_ПФО форма отчета Android VPN.*\"\n" +
                " -a \"[0-9]{2}_ПФО форма отчета Android VPN.*xlsx\"\n" +
                "\n" +
          "Значения по умолчанию:\n" +
          "выходной каталог - текущий каталог\n" +
          "почтовый ящик: " + R.Email + "\n" +
          "\n" );
    }

} // end of class
