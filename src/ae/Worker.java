/*
 * Copyright (c) 2021. Aleksey Eremin
 *
 */
/*
    Чтение почты на предмет получения файла c нужным расширением
    в письме с темой, содержащей заданную строку
 */

package ae;

import javax.mail.*;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.geronimo.mail.util.RFC2231Encoder;

/*
  Обработка почты и запись результата
  ---------------------------------------------------------------------
  https://mail.yandex.ru/?uid=861406129#setup/client
  разрешить доступ почтовых программ
    Все настройки  >  Почтовые программы.
        Разрешить доступ к почтовому ящику с помощью почтовых клиентов

 */
public class Worker {

  /**
   * Читаем файлы из сообщений с заданной темой (регистр игнорируем), записываем файлы вложений
   * с заданным именем вложения (регистр игнориуем) в каталог
   * @param subjectStr      regex строка темы
   * @param attachStr       regex файла вложения
   * @param outDir          выходной каталог
   * @param deleteMsg       удалять сообщение, если оно было записано
   * @return  кол-во прочитанных вложений в письмах, -1 ошибка чтения почты
   */
  int read(String subjectStr, String attachStr, String outDir, boolean deleteMsg)
  {
    final SimpleDateFormat sformat = new SimpleDateFormat("yyMMddHHmmss");
    int cnt = 0;  // количество писем с нужной темой
    //
    final Pattern patternSubject;
    final Pattern patternAttach;
    try {
      patternSubject = Pattern.compile(subjectStr, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
      patternAttach  = Pattern.compile(attachStr,  Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
    } catch (Exception e) {
      System.err.println("Неправильный шаблон  " + e.getMessage());
      return -1;  // ошибка чтения почты
    }
    // @see http://javatutor.net/articles/receiving-mail-with-mail-api
    // http://prostoitblog.ru/poluchenie-pochti-java-mail/
    // https://www.pvsm.ru/java/16472
    // http://toolkas.blogspot.com/2019/02/java.html
    // http://java-online.ru/javax-mail.xhtml
    // http://ryakovlev.blogspot.com/2014/11/java_17.html

    try {
      // Получить store
      Store store = Postman.openStore(); // открыть хранилище почтового сервера
      // Получить folder
      Folder folder;
      // Получить каталог
      Message[] messages;
      assert store != null;
      // будем искать письма с вложениями и заполнять коллекцию
      folder = store.getFolder("INBOX");
      folder.open(deleteMsg? Folder.READ_WRITE: Folder.READ_ONLY); //  Folder.READ_WRITE, READ_ONLY
      messages = folder.getMessages();
      //
      // читаем сообщений
      for(Message mess: messages) {
        R.sleep(400);
        // дата письма @see https://javaee.github.io/javamail/docs/api/javax/mail/Message.html#getSentDate--
        Date dt = mess.getSentDate();       // дата отправки письма
        //if(dt == null) dt = new Date();     // ну просто сейчас :-)
        String subj = mess.getSubject();    // тема письма
        String menum = "(" + mess.getMessageNumber() + ")"; // номер сообщения в папке
        R.out("Сообщение " + menum + "  Date: " + dt + "  Subj: " + subj);
        // проверим тему (игнор регистра)
        if(patternSubject.matcher(subj).find()) {
          // Письмо с вложениями?
          Object content = mess.getContent();
          if(content instanceof Multipart) {    // письмо может содержать вложения
            Multipart mp = (Multipart) content;
            cnt++; // кол-во писем с нужной темой
            // прочитаем все вложения
            int sootv = 0;  // кол-во подходящих вложений
            int n = mp.getCount();
            for(int i = 0; i < n; i++) {
              BodyPart bp = mp.getBodyPart(i);        // часть сообщения
              String fileAttach = bp.getFileName();   // имя файла вложения
              if(fileAttach != null && fileAttach.length() > 0) {
                // имеем дело с частью - вложением файла
                String attach = decodeString(fileAttach);  // раскодируем на всякий случай имя файла
                // проверим расширение вложения (игнор регистра)
                if(patternAttach.matcher(attach).find()) {
                  // префикс для сохранения вложений письма (дата письма и последовательный номер письма)
                  String prefix = String.format("%s%03d_", sformat.format(dt), cnt);
                  // записать вложение в вых. каталог
                  String filename = writeAttachFile(bp, outDir, prefix + attach);
                  if(filename != null) {
                    sootv++;  // записано в данном письме
                    R.out("  " + sootv + ". записан файл " + filename);
                  }
                }
              }
            }
            // если были соответствующие вложения и нужно удалить
            if(deleteMsg  &&  sootv > 0) {
              mess.setFlag(Flags.Flag.DELETED, true); // ставим метку на удаление
              R.out("  сообщение " + menum + " удалено");
            }
          }
        }
      }
      folder.close(true);  // закрыть папку и удалить сообщения
      store.close();
    } catch (Exception e) {
      System.err.println("Ошибка чтения почты  " + e.getMessage());
      return -1;  // ошибка чтения почты
    }
    return cnt;
  }

  /**
   * Записать файл вложением из части сообщения в указанный каталог
   * @param bp        часть сообщения
   * @param outDir    выходной каталог
   * @param fileName  имя выходного файла
   * @return имя записанного файла
   */
  private String writeAttachFile(BodyPart bp, String outDir, String fileName)
  {
    try {
        File fout = new File(outDir, fileName);
        InputStream inps = bp.getInputStream();
        FileOutputStream outs = new FileOutputStream(fout);
        byte[] buf = new byte[8192];
        int bytesRead;
        while ((bytesRead = inps.read(buf)) != -1) {
          outs.write(buf, 0, bytesRead);
        }
        outs.close();
        inps.close();
        return fout.getPath();
    } catch (Exception e) {
      System.out.println("?-Error-writeAttachFile() " + e.getMessage());
    }
    return null;
  }

  /**
   * декодируем строку, по разным RFC
   * @param inp закодированная по MIME строка
   * @return раскодированная строка (если получилось)
   */
  private String  decodeString(String inp)
  {
    String out;
    try {
      // https://geronimo.apache.org/maven/specs/geronimo-javamail_1.4_spec/1.6/apidocs/javax/mail/internet/MimeUtility.html#decodeText(java.lang.String)
      // https://geronimo.apache.org/maven/specs/geronimo-javamail_1.4_spec/1.6/apidocs/org/apache/geronimo/mail/util/RFC2231Encoder.html
      out = MimeUtility.decodeText(inp);  // раскодируем на всякий случай имя файла
      if(out.contains("%")) {
        RFC2231Encoder enc = new RFC2231Encoder();
        out = enc.decode(inp);
      }
    } catch (Exception e) {
      System.err.println("?-Warning-неудачное преобразование MIME: " + e.getMessage());
      return inp;
    }
    return out;
  }

} // end of class
