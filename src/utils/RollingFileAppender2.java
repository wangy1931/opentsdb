package net.opentsdb.utils;

import ch.qos.logback.core.rolling.RollingFileAppender;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenyinghao on 2019/12/25.
 */
public class RollingFileAppender2<E> extends RollingFileAppender<E> {
    @Override
    public synchronized void setFile(
        String fileName) {

        super.setFile(fileName);
        try {
            File file = new File(fileName);
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdir();
                System.out.println("To create a new dir " + dir.getName());
            }

            if (!file.exists()) {
                file.createNewFile();
                System.out.println("To create a new file " + fileName);
            }

            Set<PosixFilePermission> posixFilePermissionSet = new HashSet<PosixFilePermission>();
            posixFilePermissionSet.add(PosixFilePermission.OWNER_READ);
            posixFilePermissionSet.add(PosixFilePermission.OWNER_WRITE);
            posixFilePermissionSet.add(PosixFilePermission.GROUP_READ);

            Files.setPosixFilePermissions(file.toPath(), posixFilePermissionSet);
        } catch (IOException ioe) {
            System.out.println("IOException in setting log file (" + fileName + ") permission. " + ioe);
        }
    }
}
