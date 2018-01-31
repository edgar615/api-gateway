package com.github.edgar615.direwolves.verticle;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class WatchServiceTest {

  public static void main(String[] a) {

    final Path path = Paths.get("h:/test");

//        文件改变可能会触发两次事件（我的理解：文件内容的变更，元数据的变更），可以通过文件的时间戳来控制
//    在文件变化事件发生后，如果立即读取文件，可能所获内容并不完整，建议的做法判断文件的 length > 0
    try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
      path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
      while (true) {
        final WatchKey key = watchService.take();

        for (WatchEvent<?> watchEvent : key.pollEvents()) {

          final WatchEvent.Kind<?> kind = watchEvent.kind();

          if (kind == StandardWatchEventKinds.OVERFLOW) {
            continue;
          }
          //创建事件
          if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            System.out.println(StandardWatchEventKinds.ENTRY_CREATE);
          }
          //修改事件
          if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            System.out.println(StandardWatchEventKinds.ENTRY_MODIFY);
          }
          //删除事件
          if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            System.out.println(StandardWatchEventKinds.ENTRY_DELETE);
          }
          // get the filename for the event
          final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
          System.out.println(watchEvent.context());
          final Path filename = watchEventPath.context();
          // print it out
          System.out.println(kind + " -> " + filename);
//          try {
//            System.out.println(new String(Files.readAllBytes(Paths.get("h:/test", filename.toString()))));
//          } catch (IOException e) {
//            e.printStackTrace();
//          }

//          final Path changed = (Path) event.context();
//          Path absolute = path.resolve(changed);
//          File configFile = absolute.toFile();
//          long lastModified = configFile.lastModified();
//          logger.info(lastModified + "----------------");
//          // 利用文件时间戳，防止触发两次
//          if (changed.endsWith(getLicenseName()) && lastModified != LAST_MOD && configFile
// .length > 0) {
//            logger.info("----------------- reloading -----------------");
//            LAST_MOD = lastModified; // 保存上一次时间戳
//            UPDATED = true; // 设置标志位
//          }

        }
        // reset the keyf
        boolean valid = key.reset();
        // exit loop if the key is not valid (if the directory was
        // deleted,for
        if (!valid) {
          break;
        }
      }

    } catch (IOException | InterruptedException ex) {
      System.err.println(ex);
    }

  }

}