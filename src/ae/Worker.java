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
 */
public class Worker {

  /**
   * Чтение последнего по времени файла и запись его в каталог
   * @param subjectStr  строка темы
   * @param extenStr    расширение файла вложения
   * @param outDir      выходной каталог
   * @param deleteMsg   удалять сообщение, если оно было записано
   * @return  была ли запись
   */
  int read(String subjectStr, String extenStr, String outDir, boolean deleteMsg)
  {
    final SimpleDateFormat sformat = new SimpleDateFormat("yyMMddHHmmss_");
    int result = 0;
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
      folder.open(Folder.READ_WRITE); //  READ_ONLY
      messages = folder.getMessages();
      // Отобразить поля from (только первый отправитель) и subject сообщений
      for(Message mess: messages) {
        //String fuel = m.getFrom()[0].toString(); // первый отправитель
        //String from = extractEmail(fuel);  // выделим чистый e-mail
        // дата письма
        // @see https://javaee.github.io/javamail/docs/api/javax/mail/Message.html#getSentDate--
        Date dt = mess.getSentDate();
        if(dt == null) dt = mess.getReceivedDate();  // как вариант
        if(dt == null) dt = new Date(); // ну просто сейчас :-)
        String prefixAtt = sformat.format(dt); // префикс для сохранения вложений (дата письма)
        // тема письма
        String subj = mess.getSubject();
        //
        R.printStr("письмо - дата: " + dt + ". тема: " + subj);
        // проверим тему
        int sootv = 0;  // кол-во подходящих вложений
        if(subj.contains(subjectStr)) {
          // Письмо с изображением
          Object content = mess.getContent();
          if(content instanceof Multipart) {
            // письмо может содержать вложения
            // поищем их
            Multipart mp = (Multipart) content;
            // ***************************************************
            // прочитаем все вложения
            int n = mp.getCount();
            for (int i = 0; i < n; i++) {
              BodyPart bp = mp.getBodyPart(i); // часть сообщения
              String fileAttach = bp.getFileName();
              if (fileAttach != null) {
                // -----------------------------------------------
                // имеем дело с частью - вложением файла
                String attach = MimeUtility.decodeText(fileAttach);  // раскодируем на всякий случай имя файла
                // проверим расширение вложения (все расширения к нижнему регистру)
                String lowercase = attach.toLowerCase();
                if (lowercase.endsWith(extenStr)) {
                  // @see https://javaee.github.io/javamail/docs/api/javax/mail/Message.html#getSentDate--
                  // записать вложение в выходной каталог
                  String sfln;
                  sfln = writeAttachFile(bp, outDir, prefixAtt);
                  if (sfln != null) {
                    System.out.println("  записан файл " + sfln);
                    sootv++;  // записано в данном письме
                    // всеобщий подсчет записанных вложений
                    result++;
                  }
                }
              }
            }
          }
          // если были соответствующие вложения и нужно надо удалить
          // ставим метку на удаление
          if (sootv > 0 && deleteMsg) {
            mess.setFlag(Flags.Flag.DELETED, true);
          }
        }
      }
      // удалить
//      if(R.Key_Delete) {
//        folder.expunge();
//      }
      folder.close(true);  // false
      store.close();
    } catch (Exception e) {
      System.err.println("Ошибка чтения почты: " + e.getMessage());
    }
    return result;
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
