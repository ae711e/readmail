/*
 * Copyright (c) 2021. Aleksey Eremin
 *
 */

package ae;

/*
    Чтение почты по протоколу pop3 или imap
    Поиск письма с заданной темой
    Запись на диск вложений из найденного письма с заданным расширением
    При необходимости (ключ -d) найденное письмо с записанными вложениемя удаляется

 */

public class Main {

    public static void main(String[] args) {
        // write your code here
        //
        String outDir = ".";
        String Subj   = "???";
        String Extn   = "???";
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
                        Subj = args[i + 1];  // нужная тема
                        break;

                    case "-e":
                        Extn = args[i + 1].toLowerCase();  // нужное расширение
                        break;

                    case "-d":
                        Delete = true;  // удалять письма
                        break;

                    case "-o":
                        outDir = args[i + 1];  // выходной каталог
                        break;

                    case "-?":
                        printHelp();
                        System.exit(3);
                        break;

                    default:
                        //
                        break;
                }

            }
        } catch (Exception e) {
            System.err.println("Неправильный формат командной строки\n" +
              "Попробуй ключ -?");
            System.exit(2);
        }
        //
        R.loadDefault();

        Worker  w = new Worker();
        int r;
        r = w.read(Subj, Extn, outDir, Delete);   // прочитать сообщения
        if(r == 0) {
            System.err.println("Сообщений нет");
            System.exit(1);
        }
    }

    /**
     * печать помощи
     */
    private static void printHelp()
    {
        System.out.println("Чтение писем и запись вложений. Версия " + R.Ver + "\n" +
          "Формат командной строки:\n" +
          "[-v] [-s \"тема письма\"] [-e \"расширение вложения\"] [-o выходной_каталог] [-d]\n" +
          "-d - удалить письмо, если его вложение записано\n" +
          "Значения по умолчанию:\n" +
          "выходной каталог - текущий каталог\n" +
          "\n" +
          "почтовый ящик: " + R.Email + "\n" +
          "" );
    }

} // en of class
