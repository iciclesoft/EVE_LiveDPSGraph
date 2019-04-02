/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package livedpsgraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public final class ReverseLineInputStream extends InputStream {

    RandomAccessFile in;

    long currentLineStart = -1;
    long currentLineEnd = -1;
    long currentPos = -1;
    long lastPosInFile = -1;

    public ReverseLineInputStream(File file) throws FileNotFoundException {
        in = new RandomAccessFile(file, "r");
        long len = file.length();
        currentLineStart = len;
        currentLineEnd = len;
        lastPosInFile = len - 1;
        currentPos = currentLineEnd;
    }

    public boolean findPrevLine() throws IOException {
        currentLineEnd = currentLineStart;
        // There are no more lines, since we are at the beginning of the file and no lines.
        if (currentLineEnd == 0) {
            currentLineEnd = -1;
            currentLineStart = -1;
            currentPos = -1;
            return false;
        }

        long filePointer = currentLineStart - 1;

        while (true) {
            filePointer--;

            // we are at start of file so this is the first line in the file.
            if (filePointer < 0) {
                break;
            }

            in.seek(filePointer);
            int readByte = in.readByte();

            // We ignore last LF in file. search back to find the previous LF.
            if (readByte == 0xA && filePointer != lastPosInFile) {
                break;
            }
        }
        // we want to start at pointer +1 so we are after the LF we found or at 0, the start of the file.   
        currentLineStart = filePointer + 1;
        currentPos = currentLineStart;
        return true;
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (currentPos < currentLineEnd) {
//            in.seek(currentPos++);
            int readBytes = in.read(b);
            currentPos += readBytes;
            return readBytes;
        } else {
            return -1;
        }
    }

    @Override
    public int read() throws IOException {
        if (currentPos < currentLineEnd) {
            in.seek(currentPos++);
            return in.readByte();
        } else {
            return -1;
        }
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(ReverseLineInputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            super.close();
        } catch (IOException ex) {
            Logger.getLogger(ReverseLineInputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
