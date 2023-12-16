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
   * Читаем файлы из сообщений с заданной темой, записываем файлы с заданным расширением (регистр игнориуем) в каталог
   * @param subjectStr      строка темы
   * @param extStr        расширение файла вложения
   * @param outDir          выходной каталог
   * @param deleteMsg       удалять сообщение, если оно было записано
   * @return  кол-во прочитанных вложений в письмах, -1 ошибка чтения почты
   */
  int read(String subjectStr, String extStr, String outDir, boolean deleteMsg)
  {
    final String extension = extStr.toLowerCase();  // игнорируем регистр - сделаем нижним регистром
    final SimpleDateFormat sformat = new SimpleDateFormat("yyMMddHHmmss");
    int cnt = 0;  // количество писем с нужной темой
    //ArrayList<String[]> strRes = new ArrayList<>();
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
      // читаем сообщений
      for(Message mess: messages) {
        R.sleep(400);
        // дата письма @see https://javaee.github.io/javamail/docs/api/javax/mail/Message.html#getSentDate--
        Date dt = mess.getSentDate();       // дата отправки письма
        //if(dt == null) dt = new Date();     // ну просто сейчас :-)
        String subj = mess.getSubject();    // тема письма
        String menum = "(" + mess.getMessageNumber() + ")"; // номер сообщения в папке
        R.out("Сообщение " + menum + "  Date: " + dt + "  Subj: " + subj);
        if(subj.contains(subjectStr)) {
          // Письмо с вложениями?
          Object content = mess.getContent();
          if(content instanceof Multipart) {    // письмо может содержать вложения
            Multipart mp = (Multipart) content;
            cnt++; // кол-во писем с нужной темой
            // префикс для сохранения вложений письма (дата письма и последовательный номер письма)
            String prefixAtt = String.format("%s%03d_", sformat.format(dt), cnt);
            // прочитаем все вложения
            int sootv = 0;  // кол-во подходящих вложений
            int n = mp.getCount();
            for(int i = 0; i < n; i++) {
              BodyPart bp = mp.getBodyPart(i);        // часть сообщения
              String fileAttach = bp.getFileName();   // имя файла вложения
              if(fileAttach != null) {
                // имеем дело с частью - вложением файла
                String attach = MimeUtility.decodeText(fileAttach);  // раскодируем на всякий случай имя файла
                // проверим расширение вложения (игнор регистра-все к нижнему регистру)
                String sa = attach.toLowerCase();
                if(sa.endsWith(extension)) {
                  // записать вложение в вых. каталог @see https://javaee.github.io/javamail/docs/api/javax/mail/Message.html#getSentDate--
                  String filename = writeAttachFile(bp, outDir, prefixAtt);
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
   * @param bp      часть сообщения
   * @param outDir  выходной каталог
   * @param prefix  префикс имени выходного файла
   * @return имя записанного файла
   */
  private String writeAttachFile(BodyPart bp, String outDir, String prefix) {
    try {
      if (bp.getFileName() != null) {
        String fn = bp.getFileName();
        // http://www.cyberforum.ru/java-j2se/thread1763814.html
        String fname = MimeUtility.decodeText(fn);  // декодируем имя почтового файла вложения
        // System.out.println("файл вложения: '" + fname + "'");
        // запишем во временный каталог
        File fout = new File(outDir, prefix+fname);
        InputStream inps = bp.getInputStream();
        FileOutputStream outs = new FileOutputStream(fout);
        byte[] buf = new byte[8192];
        int bytesRead;
        while ((bytesRead = inps.read(buf)) != -1) {
          outs.write(buf, 0, bytesRead);
        }
        outs.close();
        inps.close();
        String foutnam;
        foutnam = fout.getPath();
        return foutnam;
      }
    } catch (Exception e) {
      System.out.println("?-Error-writeAttachFile() " + e.getMessage());
    }
    return null;
  }

} // end of class
