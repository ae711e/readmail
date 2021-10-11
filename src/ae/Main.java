/*
 * Copyright (c) 2021. Aleksey Eremin
 *
 */

package ae;

/*
    Чтение почты по протоколу pop3 или imap
    Поиск письма с заданной темой
    Запись на диск вложения из найденного письма с заданным расширением
    При необходимости (ключ -d) найденной письмо с записанным вложением удаляется

 */

public class Main {

    public static void main(String[] args) {

	      // write your code here

        // пока обойдемся без БД
        //R.loadDefault();
        //
        String outDir = ".";
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
                        R.Subj_letter = args[i + 1];  // нужная тема
                        break;

                    case "-e":
                        R.Attach_Ext = args[i + 1];  // нужная тема
                        break;

                    case "-d":
                        R.Key_Delete = true;  // удалять письма
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
        Worker  w = new Worker();
        int r;
        r = w.read(outDir);   // прочитать сообщения
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
          "тема письма - " + R.Subj_letter + "\n" +
          "расширение вложения - " + R.Attach_Ext + "\n" +
          "выходной каталог - текущий каталог\n" +
          "\n" +
          "почтовый ящик: " + R.Email + "\n" +
          "" );
    }

} // en of class
