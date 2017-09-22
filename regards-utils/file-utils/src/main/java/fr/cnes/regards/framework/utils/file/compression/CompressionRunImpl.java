/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.utils.file.compression;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 *
 * Class CompressionRunImpl
 *
 * Classe représentant le traintement asynchrone d'une compression dans un thread.
 */
public class CompressionRunImpl implements Runnable {

    private AbstractRunnableCompression compression_ = null;

    private final List<File> fileList_;

    private final File compressedFile_;

    private final File rootDirectory_;

    private final boolean flatArchive_;

    private final Charset charset_;

    private final CompressManager compressManager_ = new CompressManager();

    public CompressManager getCompressManager() {
        return compressManager_;
    }

    public CompressionRunImpl(AbstractRunnableCompression pCompression, List<File> pFileList, File pCompressedFile,
            File pRootDirectory, boolean pFlatArchive, Charset pCharset) {
        compression_ = pCompression;
        fileList_ = pFileList;
        compressedFile_ = pCompressedFile;
        rootDirectory_ = pRootDirectory;
        flatArchive_ = pFlatArchive;
        charset_ = pCharset;
    }

    @Override
    public void run() {

        try {
            System.out.println("Runnin compress thread");
            compressManager_.setThread(Thread.currentThread());
            compression_.runCompress(fileList_, compressedFile_, rootDirectory_, flatArchive_, charset_,
                                     compressManager_);
        } catch (CompressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}