/*
 * Copyright (c) 2017. Eremin
 * 16.03.2017 21:03
 * 03.04.2019
 */

package ae;

import java.util.Properties;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Ресурсный класс
*/
/*
Modify:
  06.10.21  ключи
  15.05.23  пароль для приложения в аккаунте pfoobmen и удалил поддержку sqlite
  20.07.23  расширения регистронезависимые, сохраняем все подходящие вложения
  01.08.23  берет параметры из properties
  14.08.23  перед чтением письма с нужной темой сделать паузу 1 сек

*/

public class R {
  final static String Ver = "1.5"; // номер версии

  // разделитель имени каталогов
  //public  final   static String sep = System.getProperty("file.separator");
  //private final static  String tempdir = System.getProperty("java.io.tmpdir");
  //public  final static String  TmpDir = tempdir.endsWith(sep)? tempdir: tempdir+sep;

  // заданные ключи
  static public boolean   Key_Verbose = false;  // расширенный вывод диагностики

  public static String Email      = _r.email;       // адрес почты
  //
  public static String PostProtocol = _r.protocol;  // imap pop3 протокол сервера
  public static String PostUser   = _r.postuser;    // имя пользователя посылки почтового сервера
  public static String PostPwd    = _r.postpwd;     // пароль пользователя почтового сервера
  public static String PostServer = _r.postserver;  // адрес почтового сервера
  public static String PostPort   = _r.postport;    // порт
  public static String PostSSL    = _r.postssl;     // используется протокол SSL
  //
  public static String SmtpUser   = _r.smtpuser;   // имя пользователя получения от почтового сервера
  public static String SmtpPwd    = _r.smtppwd;    // пароль пользователя почтового сервера
  public static String SmtpServer = _r.smtpserver;  // адрес почтового сервера
  public static String SmtpPort   = _r.smtpport;    // (25) порт почтового сервера
  public static String SmtpSSL    = _r.smtpssl;     // используется протокол SSL SMTP сервера

    /**
     * загрузка значений параметров по-умолчанию из файла res/default.properties
     */
    static void loadDefault()
    {
        // http://stackoverflow.com/questions/2815404/load-properties-file-in-jar
        Properties props = new Properties();
        try {
            props.load(R.class.getResourceAsStream("res/default.properties"));
            // почта
            //SmtpServer = r2s(props, "SmtpServer", SmtpServer);
            //SmtpMailTo = r2s(props, "SmtpMailTo", SmtpMailTo);
            Email           = r2s(props,"Email", Email);                // адрес почты
            PostProtocol    = r2s(props,"PostProtocol", PostProtocol);  // imap pop3 протокол сервера
            PostUser        = r2s(props,"PostUser", PostUser);          // имя пользователя посылки почтового сервера
            PostPwd         = r2s(props,"PostPwd", PostPwd);            // пароль пользователя почтового сервера
            PostServer      = r2s(props,"PostServer", PostServer);      // адрес почтового сервера
            PostPort        = r2s(props,"PostPort", PostPort);          // порт
            PostSSL         = r2s(props,"PostSSL", PostSSL);            // используется протокол SSL
            //
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // Отобразим версию
        System.out.println("Ver. " + Ver + "     e-mail: " + Email);
    }

    /**
     * Выдать строковое значение из файла свойств, либо, если там
     * нет такого свойства, вернуть значение по-умолчанию
     * @param p                     свойства
     * @param NameProp              имя свойства
     * @param strResourceDefault    значение по-умолчанию
     * @return  значение свойства, а если его нет, то значение по-умолчанию
     */
    private static String r2s(Properties p, String NameProp, String strResourceDefault)
    {
        String str = p.getProperty(NameProp);
        if(str == null) {
            str = strResourceDefault;
        }
        return str;
    }

    /**
     * Выдать числовое (long) значение из файла свойств, либо, если там
     * нет такого свойства, вернуть значение по-умолчанию
     * @param p                     свойства
     * @param NameProp              имя свойства
     * @param lngResourceDefault    значение по-умолчанию
     * @return  значение свойства, а если его нет, то значение по-умолчанию
     */
    private long r2s(Properties p, String NameProp, long lngResourceDefault)
    {
        String str = p.getProperty(NameProp);
        if(str == null) {
            str = String.valueOf(lngResourceDefault);
        }
        return Long.parseLong(str);
    }

    /**
     * Выдать числовое (int) значение из файла свойств, либо, если там
     * нет такого свойства, вернуть значение по-умолчанию
     * @param p                     свойства
     * @param NameProp              имя свойства
     * @param intResourceDefault    значение по-умолчанию
     * @return  значение свойства, а если его нет, то значение по-умолчанию
     */
    private int r2s(Properties p, String NameProp, int intResourceDefault)
    {
        String str = p.getProperty(NameProp);
        if(str == null) {
            str = String.valueOf(intResourceDefault);
        }
        return Integer.parseInt(str);
    }


  /**
   * Пауза выполнения программы
   * @param time   время задержки, мсек
   */
  static public void sleep(long time)
  {
      try {
          Thread.sleep(time);
      } catch (InterruptedException e) {
          e.printStackTrace();
      }
  }

  /**
   * прочитать ресурсный файл
   * by novel  <a href="http://skipy-ru.livejournal.com/5343.html">...</a>
   * <a href="https://docs.oracle.com/javase/tutorial/deployment/webstart/retrievingResources.html">...</a>
   * @param nameRes - имя ресурсного файла
   * @return -содержимое ресурсного файла
   */
  public String readRes(String nameRes)
  {
      String str = null;
      ByteArrayOutputStream buf = readResB(nameRes);
      if(buf != null) {
          str = buf.toString();
      }
      return str;
  }

  /**
   * Поместить ресурс в байтовый массив
   * @param nameRes - название ресурса (относительно каталога пакета)
   * @return - байтовый массив
   */
  private ByteArrayOutputStream readResB(String nameRes)
  {
      try {
          // Get current classloader
          InputStream is = getClass().getResourceAsStream(nameRes);
          if(is == null) {
              System.out.println("Not found resource: " + nameRes);
              return null;
          }
          // https://habrahabr.ru/company/luxoft/blog/278233/ п.8
          BufferedInputStream bin = new BufferedInputStream(is);
          ByteArrayOutputStream bout = new ByteArrayOutputStream();
          int len;
          byte[] buf = new byte[512];
          while((len=bin.read(buf)) != -1) {
              bout.write(buf,0,len);
          }
          return bout;
      } catch (IOException ex) {
          ex.printStackTrace();
      }
      return null;
  }

  /**
   * Записать в файл текст из строки
   * @param strTxt - строка текста
   * @param fileName - имя файла
   * @return      true - записано, false - ошибка
   */
  public static boolean  writeStr2File(String strTxt, String fileName)
  {
      File f = new File(fileName);
      try {
          PrintWriter out = new PrintWriter(f);
          out.write(strTxt);
          out.close();
      } catch(IOException ex) {
        System.err.println("?Error-writeStr2File() " + ex.getMessage());
        return false;
      }
      return true;
  }

  /**
   *  Записать в файл ресурсный файл
   * @param nameRes   имя ресурса (от корня src)
   * @param fileName  имя файла, куда записывается ресурс
   * @return  true - запись выполнена, false - ошибка
   */
  public boolean writeRes2File(String nameRes, String fileName)
  {
      boolean b = false;
      ByteArrayOutputStream buf = readResB(nameRes);
      if(buf != null) {
          try {
              FileOutputStream fout = new FileOutputStream(fileName);
              buf.writeTo(fout);
              fout.close();
              b = true;
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
      return b;
  }

  /**
   * Загружает текстовый ресурс в заданной кодировке
   * @param name      имя ресурса
   * @param code_page кодировка, например "Cp1251"
   * @return          строка ресурса
   */
  public String getText(String name, String code_page)
  {
      StringBuilder sb = new StringBuilder();
      try {
          InputStream is = this.getClass().getResourceAsStream(name);  // Имя ресурса
          BufferedReader br = new BufferedReader(new InputStreamReader(is, code_page));
          String line;
          while ((line = br.readLine()) !=null) {
              sb.append(line);  sb.append("\n");
          }
      } catch (IOException ex) {
          ex.printStackTrace();
      }
      return sb.toString();
  }


  /**
   * преобразовать секунды UNIX эпохи в строку даты
   * @param unix  секунды эпохи UNIX
   * @return дата и время в формате SQL (ГГГГ-ММ-ДД ЧЧ:ММ:СС)
   */
  public static String unix2datetimestr(int unix)
  {
    Date date = new Date(unix*1000L);
    // format of the date
    SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //jdf.setTimeZone(TimeZone.getTimeZone("GMT-4"));
    return jdf.format(date);
  }


  /////////////////////////////////////////////////////////////////////////////////

  /**
   * Скопировать входной поток в строку
   * @param inputStream входной поток
   * @return выходная строка
   */
  public static String InputStream2String(InputStream inputStream)
  {
    // http://qaru.site/questions/226/readconvert-an-inputstream-to-a-string
    Scanner s = new Scanner(inputStream).useDelimiter("\\A");
    String txt = s.hasNext() ? s.next() : "";
    return txt;
  }

  /**
   * Выделить e-mail из входной строки
   * @param inputStr входная строка
   * @return строка e-mail или null если адреса нет
   */
  public static String  extractEmail(String inputStr)
  {
    // регулярное выражение для выделения эл. адреса
    Pattern email_pattern = Pattern.compile("[a-z0-9_.\\-]+@[a-z0-9.\\-]+\\.[a-z]{2,4}",Pattern.CASE_INSENSITIVE);
    Matcher mat = email_pattern.matcher(inputStr);
    if(mat.find()) {
      String m = mat.group();
      return m;
    }
    return null;
  }

  /**
   * Выдать имя файла c расширением
   * @param fullname полное имя файла
   * @return имя файла
   */
  public static String getFileName(String fullname)
  {
    File f = new File(fullname);
    return f.getName();
  }

  /**
   * Выдать расширение файла
   * @param fullname полное имя файла
   * @return расширение
   */
  public static String getFileExtension(String fullname)
  {
    Pattern file_extension = Pattern.compile("\\.\\w{1,4}$", Pattern.CASE_INSENSITIVE);
    Matcher mt = file_extension.matcher(fullname);
    String fext = mt.find() ? mt.group() : ""; // расширение
    return fext;
  }

   /**
   * Вывод строки, в зависимости от флага Key_Verbose
   * @param message сообщение
   */
  public static void printStr(String message)
  {
    if(Key_Verbose) {
      System.out.println(message);
    }
  }

} // end of class
