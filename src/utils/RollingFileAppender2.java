package net.opentsdb.utils;

import ch.qos.logback.core.rolling.RollingFileAppender;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            Set<PosixFilePermission> posixFilePermissionSet = new HashSet<PosixFilePermission>();
            posixFilePermissionSet.add(PosixFilePermission.OWNER_READ);
            posixFilePermissionSet.add(PosixFilePermission.OWNER_WRITE);
            posixFilePermissionSet.add(PosixFilePermission.OWNER_EXECUTE);
            posixFilePermissionSet.add(PosixFilePermission.GROUP_READ);
            posixFilePermissionSet.add(PosixFilePermission.GROUP_EXECUTE);
            if (file.exists()) {
                Files.setPosixFilePermissions(file.toPath(), posixFilePermissionSet);
            }
        } catch (IOException ioe) {
            Logger log = LoggerFactory.getLogger(net.opentsdb.utils.RollingFileAppender2.class);
            log.error("IOException in setting log file permission. {}", ioe.getMessage());
        }
    }
}
